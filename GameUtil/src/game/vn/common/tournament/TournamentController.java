/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.tournament;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.vn.common.GameController;
import game.vn.common.GameExtension;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.log.PlayersDetail;
import game.vn.common.lib.poker.spinandgo.RewardMulti;
import game.vn.common.lib.vip.UserTaxData;
import game.vn.common.log.SendVipDataTask;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import game.vn.util.db.UpdateMoneyResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý logic Tournament Game
 * @author tuanp
 */
public class TournamentController extends GameController{

    private final Map<String, Byte> winners;
    //dung de kiem tra tournamnet đã start
    private boolean started;
    private long rotateTime;
    //số tiền thưởng khi thắng tournament
    private BigDecimal bonusMoney;
    //tổng số ván phải thắng để kết thức tournament
    private int countWinMax;
    private final int taxTour;
    
    public TournamentController(Room room, GameExtension gameEx) {
        super(room, gameEx);
        winners = new HashMap<>();
        bonusMoney= BigDecimal.ZERO;
        taxTour = TournamentManager.getInstance().getTax(getMoney().doubleValue());
    }

    @Override
    public void initMaxUserAndViewer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected byte getServiceId() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    protected void processCountDownStartGame() {
        try {
            if (!isPlaying() && this.getPlayersList().size() > 1) {
                if (!this.getGameState().equals(this.getCountDownStartGameState()) && isStarted()) {
                    setStateGame(this.getCountDownStartGameState());
                    setCurrentMoveTime();
                }
                sendBoardInforMessage(getTimeToStart());
            }
        } catch (Exception e) {
            this.game.getLogger().error("TournamentController.processCountDownStartGame() error:", e);
        }
    }

    @Override
    protected void forceLogoutUser(User playerLeave) {
        try {
            removeBoardPlayingToHazelcast(playerLeave);
            //trả tiền lại cho user
            repayMoneyStask(playerLeave);
        } catch (Exception e) {
            this.game.getLogger().error("TournamentController.forceLogoutUser() error: ", e);
        }
        sendLeaveRoomMessage(playerLeave);
        this.game.getApi().kickUser(playerLeave, null, " ", 0);
    }
    
    protected boolean isStarted(){
        return started;
    }
    
    private void setStarted(boolean isStarted){
        started = isStarted;
    }
    
    /**
     * Tính toán phần thưởng nhận được sau khi thắng ván tournament
     */
    protected void processTournamentBonus() {
        try {
            if (!isStarted() && this.getPlayersList().size() == getMaxPlayers()) {
                BigDecimal sumMoney = BigDecimal.ZERO;
                //trừ tiền user mua vé vào
                for (User u : getAllPlayers()) {
                    //add thông tin chi tiết bàn tới log history
                    if (updateMoney(u, getMoney().negate(), CommonMoneyReasonUtils.BUY_TICKET, BigDecimal.ZERO, new ArrayList<>())) {
                        sumMoney =Utils.add(sumMoney, getMoney());
                    }
                }
                
                //tính thuế %
                BigDecimal[] arrResultMoney = setMoneyMinusTax(sumMoney, getTaxTournament());
                BigDecimal multiTour = new BigDecimal(String.valueOf(TournamentManager.getInstance().getMultiRandom(getMoney().doubleValue(),arrResultMoney[MONEY].doubleValue())));
                //đã hết 1 chu kỳ thì đá user ra
                if(multiTour.signum()==0){
                    //Tất cả user rời khỏi bàn khi kết thúc ván
                    for (User u : getAllPlayers()) {
                        String infor = GameLanguage.getMessage(GameLanguage.CAN_NOT_PLAY_GAME_TOUR, getLocaleOfUser(u));
                        infor = String.format(infor, Utils.formatNumber(getMoney()));
                        kickUser(u, infor);
                    }
                    return;
                }
                int bonusPercent = TournamentManager.getInstance().getRewardMulti(getMoney().doubleValue(), multiTour.intValue());
                if (bonusPercent != 0) {
                    double place_1st = Utils.divide(bonusPercent, 100);
                    
                    BigDecimal percent = new BigDecimal(place_1st);
                    bonusMoney = Utils.multiply(multiTour, getMoney());
                    bonusMoney = Utils.multiply(bonusMoney, percent);
                } else {
                    bonusMoney = Utils.multiply(multiTour, getMoney());
                }
                
                countWinMax = RoomConfig.getInstance().getWinning(getServiceId());
                
                SFSObject smg = this.messageController.getTournamnetInBoard(getBonusMoney().doubleValue(), getServiceId(), getCountWinMax());
                sendAllPlayersMessage(smg);
                sendRotateTimeMessage();

                setStarted(true);
                startGame();
                setMinusMoney(new BigDecimal(String.valueOf(bonusMoney)));
            }
        } catch (Exception e) {
            this.game.getLogger().error("TournamentController.processTournamentBonus() error:", e);
        }
    }
    
    /**
     * Phần thưởng nhận được khi thắng tournament
     * @return 
     */
    protected BigDecimal getBonusMoney(){
        return bonusMoney;
    }

    @Override
    protected int getFreeSeat() {
        if(isStarted()){
            return 0;
        }
        return super.getFreeSeat();
    }

    /**
     * update số ván thắng
     * @param winner 
     */
    protected void updateInforWinner(User winner){
        String idDB=getIdDBOfUser(winner);
        byte value=1;
        if(winners.containsKey(idDB)){
           value = (byte) (winners.get(idDB)+1);
        }
        winners.put(idDB, value);
        
        if (value == getCountWinMax()) {
            processFinishTournament(winner);
        }
    }
    
    /**
     * Số ván thắng tối đa để kết thúc ván
     * @return 
     */
    protected int getCountWinMax(){
        return countWinMax;
    }
    
    /**
     * Xử lý khi kết thức 1 tournament
     *
     * @param winner
     */
    protected void processFinishTournament(User winner) {
        try {
            if (isStarted()) {
                sendResultTourmament(getIdDBOfUser(winner), getBonusMoney().doubleValue());
                updateMoney2WithLocale(winner, getBonusMoney(), "", "","", CommonMoneyReasonUtils.TOI_1, BigDecimal.ZERO,null);
                //update tiền cho winner
                repayMoneyStask(winner);
                bonusMoney=  BigDecimal.ZERO;
                setStarted(false);
                //Tất cả user rời khỏi bàn khi kết thúc ván
                for(User u: getAllPlayers()){
                    leave(u);
                }
            }
        } catch (Exception e) {
            this.game.getLogger().error("TournamentController.processFinishTournament() error ", e);
        }
    }
    
    @Override
    protected void checkMaintainServer(){
        //kiểm tra game có đang bảo trì
        if ((RoomConfig.getInstance().isMaintainGame(getServiceId()) || RoomConfig.getInstance().isMaintainAllGame()) && !isStarted()) {
            for (User user : this.room.getUserList()) {
                kickUser(user,RoomConfig.getInstance().getMaintainInfor(Utils.getUserLocale(user)));
            }
        }
    }
    
    /**
     *
     * @param player
     * @return
     */
    @Override
    protected User nextPlayer(User player) {
        int sNum = getSeatNumber(getIdDBOfUser(player));
        int length = getPlayersSize();
        for (int i = 0; i < length; i++) {
            sNum = (sNum + 1) % length;
            User u = getUser(sNum);
            if (u != null) {
                return u;
            }
        }
        return null;
    }

    @Override
    public boolean join(User user, String pwd) {
        if(isStarted()){
            return false;
        }
        return super.join(user, pwd);
    }
    
    
    /**
     * số ván user đã thắng
     * @param user
     * @return 
     */
    protected byte getWinsOfUser(User user) {
        String idDB = getIdDBOfUser(user);
        if (!winners.containsKey(idDB)) {
            return 0;
        }
        return winners.get(idDB);
    }
    
    //update thông tin thắng thua, gửi về cho client
    protected boolean updateInforWithLocale(User user, String messVi, String messEn) {
        try {
            String idDB = getIdDBOfUser(user);
            if (getLocaleOfUser(user).equals(GlobalsUtil.VIETNAMESE_LOCALE)) {
                SFSObject viMessage = this.getBonusMoney(idDB, 0, messVi);
                sendUserMessage(viMessage, user);
            } else {
                SFSObject engMessage = this.getBonusMoney(idDB, 0, messEn);
                sendUserMessage(engMessage, user);
            }
            return true;
        } catch (Exception e) {
            this.game.getLogger().error("TournamentController.updateInforWithLocale() error ", e);
        }
        return false;
    }

    @Override
    protected boolean isCanStart() {
        if(!isStarted()){
            return false;
        }
        return (((System.currentTimeMillis() - getCurrentMoveTime()) / 1000) > getTimeAutoStartDefault())
                && !isPlaying() && getPlayersList().size() > 1;
    }
    
    /**
     * Đang trong thời gian xoay trúng thưởng
     * @return 
     */
    protected boolean isRoateOverTime() {
        return isPlaying()   && ((System.currentTimeMillis() - rotateTime)/1000) >= RoomConfig.getInstance().getRotateTime(getServiceId());
    }
    
    /**
     * Gửi thông tin thời gian còn lại để báo Xâm
     */
    protected void sendRotateTimeMessage() {
        try {
            rotateTime = System.currentTimeMillis();
            SFSObject m = messageController.getRotateTimeInfoMessage((byte) RoomConfig.getInstance().getRotateTime(getServiceId()));
            sendAllUserMessage(m);
        } catch (Exception e) {
            this.game.getLogger().error("TournamentController.sendRotateTimeMessage() error", e);
        }
    }
    
    /**
     * Gửi thông tin kết quả ván chơi tournament
     * @param idWiner
     * @param bonusMoney 
     */
    private void sendResultTourmament(String idWiner, double bonusMoney){
        try {
            SFSObject ob= messageController.getResultTournament(winners, getPlayersList(), idWiner, bonusMoney);
            sendAllUserMessage(ob);
        } catch (Exception e) {
             this.game.getLogger().error("TournamentController.sendResultTourmament()error", e);
        }
    }

    @Override
    protected BigDecimal getPenalizeLeaver() {
        return BigDecimal.ZERO;
    }

    @Override
    protected int getMinJoinGame() {
        return 1;
    }

    @Override
    protected int getMinJoinOwner() {
        return 1;
    }

    @Override
    protected int getMaxBet() {
        return 0;
    }

    @Override
    protected int getTax() {
        return 0;
    }

    @Override
    protected void addToWaitingUserList(User userRemoved, String reason) {
        // tournament không có user chờ
    }

    @Override
    protected void penalizeLeaver(User playerLeave) {
        if (isStarted()) {
            updateLogGameForUser(playerLeave, CommonMoneyReasonUtils.BO_CUOC, null);
        }
    }

    @Override
    protected synchronized boolean updateMoney(User u, BigDecimal value, int reasonId, BigDecimal tax, List<Short> arrayCardIds) {
        if (value.signum() == 0 || u == null) {
            return false;
        }
        try {
            if (!isPlayerState(u)) {
                return false;
            }
            String idDBUser = getIdDBOfUser(u);
            BigDecimal moneyOfUserBefore = getMoneyFromUser(u);

            if (value.signum() < 0) {
                //nếu trừ tiền, thì -value phải nhỏ hơn hoặc bằng tiền hiện có của user
                if (moneyOfUserBefore.compareTo(value.negate()) <0 ) {
                    return false;
                }
            }

            UpdateMoneyResult rs;
            if (getMoneyType() == MoneyContants.MONEY) {
                rs = Database.instance.callUpdateMoneyStackProcedure(idDBUser, value);
            } else {
                rs = Database.instance.callUpdatePointStackProcedure(idDBUser, value);
            }
            updateMoneyStackOfUser(u, rs.after);

            BigDecimal moneyOfUser = getMoneyFromUser(u);
            this.game.getLogger().info(idDBUser+".Update money: before="+moneyOfUserBefore+", after="+moneyOfUser);
            //add thông tin chi tiết bàn tới log history
            addBoardDetail(u, reasonId, moneyOfUserBefore.doubleValue(), moneyOfUser.doubleValue(), value.doubleValue(), tax.doubleValue(), arrayCardIds);
            //ghi log khi có phát sinh tiền trong game
            addInvoiceDetail(u, reasonId, moneyOfUserBefore.doubleValue(), moneyOfUser.doubleValue(), value.doubleValue(), tax.doubleValue(), arrayCardIds);
            //ghi log thue cho VIP
            if (ServerConfig.getInstance().enableVip() && tax.signum()> 0 && getMoneyType() == MoneyContants.MONEY) {
                UserTaxData userTaxData = new UserTaxData();
                userTaxData.setGameId(reasonId + "");
                userTaxData.setTax(tax.doubleValue());
                userTaxData.setTime(new Date());
                userTaxData.setUserId(getIdDBOfUser(u));
                userTaxData.setServerId(ServerConfig.getInstance().getServerId() + "");

                //gửi lên queue để vip chạy
                executorLog.execute(new SendVipDataTask(userTaxData));
                this.game.getLogger().info("send Vip data : " + userTaxData.getUserId() + " - money : " + value + " - tax : " + userTaxData.getTax());
            }

            /**
             * Gửi về cho client thông tin phát sinh tiền để hiển thị lịch sử
             * trong chat
             */
            if (reasonId != CommonMoneyReasonUtils.DAT_CUOC) {
                sendHistoryBoard(u, value.doubleValue(), reasonId, arrayCardIds);
            }
            
            for (PlayersDetail playerInfor : invoices.getPlayersDetail()) {
                if (playerInfor.getPlayerId().equals(getIdDBOfUser(u))) {
                    playerInfor.setCreditAfter(getMoneyFromUser(u).doubleValue());
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            this.game.getLogger().error("GameController.updateMoney() error:", e);
        }
        return false;
    }

    @Override
    protected void updateLogGameForUser(User u, int reason, List<Short> arrayCardIds) {
        if(isStarted()){
             super.updateLogGameForUser(u, reason, arrayCardIds); 
        }
    }

    @Override
    protected void processBuyStack(User user, double moneyStackInput) {
         throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    protected void checkSendAndAutoBuyStack() {
        //tournament khong gui ve mua tay
    }
    
    protected int getTaxTournament(){
        return taxTour;
    }

    @Override
    protected int getMaxPlayers() {
        return super.getMaxPlayers();
    }

    
}
