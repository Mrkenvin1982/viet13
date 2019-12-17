/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.XiToCommand;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.object.BoardLogInGame;
import game.vn.common.constant.Service;
import game.vn.game.xito.gamestate.GameFlogState;
import game.vn.game.xito.gamestate.GamePreFlogState;
import game.vn.game.xito.gamestate.GameReadyState;
import game.vn.game.xito.gamestate.IXiToGameState;
import game.vn.game.xito.lang.XiToLanguage;
import game.vn.game.xito.message.XiToGameMessageFactory;
import game.vn.game.xito.object.DeskXiTo;
import game.vn.game.xito.object.Round;
import game.vn.game.xito.object.WinnerInfor;
import game.vn.game.xito.object.XiToCard;
import game.vn.game.xito.object.XiToGameUtil;
import game.vn.game.xito.object.XiToPlayer;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;

/**
 * class xử lý logic chính của game xì tố
 *
 * @author tuanp
 */
public class XiToController extends GameController {
    /**
     * Tổng cộng 5 tố 1 vòng tố đầu tiên: mỗi người tố bằng tiền cược của bàn 4
     * vòng chia bài: 1 vòng đầu 2 lá và 3 vòng 1 lá. Hết vòng chia
     *
     * bài thì tố lại từ đầu
     */
    public final static byte MAX_ROUND = 5;
    // tu 7 toi A
    public final static byte CARD_ID_XI_TO = 20;
    // mỗi Player chỉ có 5 lá bài
    public final static byte MAX_CARD_PER_PLAYER = 5;
    //may chia bai
    private DeskXiTo deskXiTo;
    //tien cuoc sau cung
    private BigDecimal lastBetStack = BigDecimal.ZERO;
    //danh sach vong choi cua ban
    private List<Round> rounds;
    //Danh sách người đang chơi
    //key:idDDBUser; value:XiToPlayer
    private final Map<String, XiToPlayer> players;
    private final XiToGameMessageFactory messageFactory;
    private IXiToGameState preFlogState;
    private IXiToGameState flogState;
    private IXiToGameState state;
    private final Logger log;
    private User userWinner;
    //số người chơi khi bắt đầu ván
    private int countStartGamePlayers;
    //số người chơi đã úp bỏ, dùng để tính trường hợp stop game khi tất cả úp bỏ
    private int countFold;
    private int countLeave;
    //mỗi user chỉ có 5 lá, dùng cho hàm dealRemainCard
    private int countRemainDealCard = MAX_CARD_PER_PLAYER;
    private final BoardLogInGame boardLog;
    private String logTextLever = "";

    /**
     *
     * @param room
     * @param gameEx
     */
    public XiToController(Room room, XiToGame gameEx) {
        super(room, gameEx);
        this.messageFactory = new XiToGameMessageFactory(this);
        this.players = new HashMap<>();
        this.rounds = new ArrayList<>();
        this.log = this.game.getLogger();
        boardLog = new BoardLogInGame();
        reset();
    }
    
    private void reset() {
        this.boardLog.clear();
        this.countFold = 0;
        this.countLeave = 0;
        this.countRemainDealCard = MAX_CARD_PER_PLAYER;
        this.countStartGamePlayers = 0;
        this.userWinner = null;
        this.rounds.clear();
        if (XiToConfig.getInstance().isTest()) {
            this.deskXiTo = new DeskXiTo("test");
        } else {
            this.deskXiTo = new DeskXiTo(CARD_ID_XI_TO);
        }
        this.preFlogState = new GamePreFlogState(this);
        this.flogState = new GameFlogState(this);
        this.state = new GameReadyState();

        for(XiToPlayer p : players.values()) {
            p.reset();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="set-get-add">
    public IXiToGameState getState() {
        return state;
    }

    public BigDecimal getTotalBetStack() {
        BigDecimal totalBetStack = BigDecimal.ZERO;
        for (Round round : rounds) {
            totalBetStack = Utils.add(totalBetStack, round.getStack());
        }
        return totalBetStack;
    }

    public void addRound(Round round) {
        this.rounds.add(round);
    }

    public List<Round> getRounds() {
        return rounds;
    }

    /**
     * Lấy round hiện tại, cũng là round sau cùng trong list
     *
     * @return
     */
    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        }
        return rounds.get(rounds.size() - 1);
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public BigDecimal getMinRaise() throws Exception {
        return Utils.add(getCurrentRound().getMaxBetStackOfPlayer(), getMoney());
    }

    public BigDecimal getLastBetStack() {
        return lastBetStack;
    }

    public void setLastBetStack(BigDecimal lastBetStack) {
        this.lastBetStack = lastBetStack;
    }

    public XiToPlayer getXiToPlayer(String idDBUser) {
        return players.get(idDBUser);
    }

    public void removePlayer(String idDBUser) {
        players.remove(idDBUser);
    }

    public XiToGameMessageFactory getMessageFactory() {
        return messageFactory;
    }

    public IXiToGameState getPreFlogState() {
        return preFlogState;
    }

    public IXiToGameState getFlogState() {
        return flogState;
    }

    public void setStateXiTo(IXiToGameState state) {
        this.state = state;
    }

    public DeskXiTo getDeskXiTo() {
        return deskXiTo;
    }

    /**
     * get min bet money default
     *
     * @return
     */
    public double getMinBet() {
        return getMoney().doubleValue();
    }

    @Override
    public int getMaxBet() {
        return super.getMaxBet();
    }
    
    public boolean updateMoney(User u, BigDecimal value, int reasonId, String displayText, BigDecimal tax, List<Short> arrayCardIds) {
        if (value.signum() == 0 || u==null) {
            return false;
        }
        if (super.updateMoney(u, value, reasonId, tax,arrayCardIds)) {
            sendBonusMoneyMessageToAll(getIdDBOfUser(u), value.doubleValue(), displayText);
            log.debug(u.getName() + " value: " + value );
            return true;
        }
        log.error("updateMoney error: user: " + u.getName() + " value: " + value );
        return false;
    }

    /**
     * Gửi message bonus money cho cả bàn
     *
     * @param userId
     * @param value
     * @param displayText
     */
    public void sendBonusMoneyMessageToAll(String userId, double value, String displayText) {
        SFSObject ob=this.getBonusMoney(userId, value, displayText);
        sendAllUserMessage(ob);
    }

    /**
     * Gửi message bonus money cho một mình user
     *
     * @param user
     * @param value
     * @param displayText
     */
    public void sendBonusMoneyMessage(User user, int value, String displayText) {
        if(user==null){
            return;
        }
        SFSObject m = getBonusMoney(getIdDBOfUser(user), value, displayText);
        sendUserMessage(m, user);
    }

    @Override
    protected BigDecimal[] setMoneyMinusTax(BigDecimal money, int taxPercent) {
        return super.setMoneyMinusTax(money, taxPercent);
    }
    @Override
    public void startGame() {
         try {
            this.log.debug("-------------START GAME XI TO---------------------");
            logTextLever = "";
             super.startGame();
             processStartGame();
        } catch (Exception e) {
            log.error("startgame error:" + boardLog.getLog(), e);
        }
    }

    public List<User>getPlayers(){
        return this.getPlayersList();
    }

    @Override
    public void update() {
        try {
            super.update();
            this.log.debug("-------------UPDATE GAME XI TO ---------------------");
            
            //tự động start game
            if (isCanStart()) {
                startGame();
            }
            if (isFlog() && isTimeout()){
                User user = getCurrentPlayer();
                if (user == null) {
                    log.error("XiToGame.update: current user is null:" + boardLog.getLog());
                    log.error(getStringCardsOfUsers());
                    stopGame();
                    return;
                }
                XiToPlayer player = getXiToPlayer(getIdDBOfUser(user));
                if (player == null) {
                    log.error("XiToGame.update: current player is null:" + boardLog.getLog());
                    stopGame();
                    return;
                }
                if (player.isTurn()) {
                    checkAndFold(user);
                    checkIsIncompleteAndStopGame();
                    checkFinishRoundAndAddRound();
                }else{
                    checkIsIncompleteAndStopGame();
                }  
            }
            //het 15s gui message show card
            //het 15s gui message show card
            if (isPreFlog() && ((System.currentTimeMillis() - getCurrentMoveTime()))
                    / 1000 >= XiToConfig.getInstance().getPreFlogTime()) {
                this.state.preFlog();
            }
        } catch (Exception e) {
            log.error("update error: " + boardLog.getLog(), e);
        }
    }

    
    @Override
    public void nextTurn() {
        super.nextTurn();
        this.setStateGame(this.getWaittingGameState());
    }

    /**
     * Override hàm này để public và thêm logic
     */
    @Override
    public synchronized void stopGame() {
        if (!isPlaying()) {
            return;
        }
        try {
            this.log.debug("-------------STOP GAME XI TO---------------------");
            processStopGame();
        } catch (Exception e) {
            log.error("stop game error:" + boardLog.getLog(), e);
        } finally {
            super.stopGame();
            for(User user: getPlayersList()) {
                checkNoActionBetGame(user);
            }
            reset();
            processCountDownStartGame();
        }
    }

    @Override
    public boolean isInturn(User u) {
        return super.isInturn(u);
    }
    @Override
    public void leave(User user) {
        try {
            boolean isPlayer = isPlayerState(user);
            super.leave(user);
            this.log.debug("-------------LEAVE GAME XI TO "+user.getName()+"---------------------");
            //leave = winner thi reset winner
            if (Utils.isEqual(user, userWinner)) {
                userWinner = null;
            }
            XiToPlayer xtp = getXiToPlayer(getIdDBOfUser(user));
            /**
             * Ghi log rời bàn của những user đang chơi trong ván
             */
            if (isPlaying() && xtp != null && xtp.getHoldCardsId().size() > 0) {
                logTextLever += logTextLever.isEmpty() ? user.getName() : ", " + user.getName();
                updateLogGameForUser(user,CommonMoneyReasonUtils.BO_CUOC,xtp.getHoldCardsIdToList());
            }
            if (isPlayer) {
                checkAndFold(user);
                incrCountLeave(xtp);
                if (xtp != null) {
                    xtp.setState(xtp.getLeaveState());
                    players.put(getIdDBOfUser(user), xtp);
                }
                checkIsIncompleteAndStopGame();
            }
        } catch (Exception e) {
            log.error("leave: " + boardLog.getLog(), e);
        } finally {
            this.players.remove(getIdDBOfUser(user));
            forceLogoutUser(user);
            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "leave", 0);
        }
    }

    /**
     * Nếu chưa đến lượt player mà player đó leave hoặc standup thì tăng số
     * lượng leave. Trường hợp player vào bàn đang chơi và leave ngay thì không
     * tăng Trường hợp đến lượt player mà player đó leave hoặc standup thì không
     * tăng
     *
     * @param player
     */
    public void incrCountLeave(XiToPlayer player) {
        if (player != null && isPlaying()) {
            // nếu trạng thái hiện tại của player là betting nghĩa là player đó đã tham gia chơi mà chưa fold
            if (player.isWaitingToNextTurn() || player.isAllIn()) {
                this.countLeave++;
            }
        }
    }
   
    @Override
    public void processMessage(User user, ISFSObject sfsObj) {
        try {
            super.processMessage(user, sfsObj);
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            if (idAction != XiToCommand.BET_MONEY_INFO) {
                resetNoActionTime(user);
            }
                switch (idAction) {
                    case XiToCommand.BET:
                        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                            double betStack = sfsObj.getDouble(XiToCommand.STACK_KEY);
                            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
                            if(xitoPlayer==null){
                                break;
                            }
                            if (!xitoPlayer.getActions().contains(XiToPlayer.BET)) {
                                log.error("Wrong action-" + "user: " + user.getName() + " bet" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            if (xitoPlayer.isWaitingToNextTurn()) {
                                log.error("user: " + user.getName() + " isWaitingToNextTurn" + " - boardLog:" + boardLog.getLog());
                                break;
                            }
                            boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "bet", betStack);
                            checkToAddNewRound();
                            xitoPlayer.bet(new BigDecimal(String.valueOf(betStack)));
//                            checkIsCompleteAndStopGame();
//                            checkIsIncompleteAndStopGame();
//                            checkFinishRoundAndAddRound();
                        }
                        break;
                    case XiToCommand.RAISE:
                        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                            double raiseStack = sfsObj.getDouble(XiToCommand.STACK_KEY);
                            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
                            if(xitoPlayer==null){
                                return;
                            }
                            if (!xitoPlayer.getActions().contains(XiToPlayer.RAISE)) {
                                log.error("Wrong action-" + "user: " + user.getName() + " raise" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            if (xitoPlayer.isWaitingToNextTurn()) {
                                log.error("user: " + user.getName() + " isWaitingToNextTurn" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "raise", raiseStack);
                            xitoPlayer.raise(new BigDecimal(String.valueOf(raiseStack)));
                            checkIsCompleteAndStopGame();
                            checkIsIncompleteAndStopGame();
                            checkFinishRoundAndAddRound();
                        }
                        break;
                    case XiToCommand.CALL:
                        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
                            if(xitoPlayer==null){
                                break;
                                
                            }
                            if (!xitoPlayer.getActions().contains(XiToPlayer.CALL)) {
                                log.error("Wrong action-" + "user: " + user.getName() + " call" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            if (xitoPlayer.isWaitingToNextTurn()) {
                                log.error("user: " + user.getName() + " isWaitingToNextTurn" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "call", 0);
                            checkToAddNewRound();
                            xitoPlayer.call();
                            checkIsCompleteAndStopGame();
                            checkIsIncompleteAndStopGame();
                            checkFinishRoundAndAddRound();
                        }
                        break;
                    case XiToCommand.ALL_IN:
                        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
                            if(xitoPlayer==null){
                                return;
                            }
                            if (!xitoPlayer.getActions().contains(XiToPlayer.ALLIN)) {
                                log.error("Wrong action-" + "user: " + user.getName() + " allin" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "all in", xitoPlayer.getStack().doubleValue());
                            checkToAddNewRound();
                            xitoPlayer.allIn();
                            checkIsCompleteAndStopGame();
                            checkIsIncompleteAndStopGame();
                            checkFinishRoundAndAddRound();
                        }
                        break;
                    case XiToCommand.CHECK:
                        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {

                            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
                            if(xitoPlayer==null){
                                break;
                            }
                            if (!xitoPlayer.getActions().contains(XiToPlayer.CHECK)) {
                                log.error("Wrong action-" + "user: " + user.getName() + " check" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }

                            if (xitoPlayer.isWaitingToNextTurn()) {
                                log.error("user: " + user.getName() + " isWaitingToNextTurn" + "\nboardLog:" + boardLog.getLog());
                                break;
                            }
                            checkToAddNewRound();
                            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "check", 0);
                            xitoPlayer.check();
                            checkIsCompleteAndStopGame();
                            checkFinishRoundAndAddRound();
                        }
                        break;
                    case XiToCommand.BET_MONEY_INFO:
                        //Client lấy thông tin trước khi set lại tiền cược của bàn
                        sendBetMoneyInfoMessage(user);
                        break;
                    case XiToCommand.FOLD:
                        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "fold", 0);
                            checkToAddNewRound();
                            checkAndFold(user);
                            checkFinishRoundAndAddRound();
                            if (isAllFolded()) {
                                stopGame();
                            }
                            checkIsIncompleteAndStopGame();
                        }
                        break;
                    case XiToCommand.SHOW_ONE_CARD:
                        byte cardIdShow = sfsObj.getByte(SFSKey.INFOR_CARD);
                        showCard(getXiToPlayer(getIdDBOfUser(user)), cardIdShow);
                        break;
                }
        } catch (Exception e) {
            log.error("processMessage error: " + user.getName() + " -boardlog:" + boardLog.getLog(), e);
        }
    }

    /**
     * Chỉ còn một người chơi, còn lại đã bỏ bài thì return true
     *
     * @return
     */
    public boolean isAllFolded() {
        //nếu còn 1 player in turn
        if (countStartGamePlayers - countFold - countLeave == 1) {
            return true;
        }
        return false;
    }

    public boolean isPreFlog() {
        return this.state.equals(preFlogState) && isPlaying();
    }

    public boolean isFlog() {
        return this.state.equals(flogState) && isPlaying();
    }

    /**
     * Lấy chuỗi bài những người tham gia ván chơi, dùng ghi log
     *
     * @return
     */
    public String getStringCardsOfUsers() {
        String result = "";
        try {
            for (XiToPlayer player : players.values()) {
                if (!isInturn(player.getUser()) && !player.isAllIn()) {
                    continue;
                }
                result += player.getUser().getName() + " "
                        + XiToLanguage.getMessage(player.getResultCards().getStrValue(), getLocaleOfUser(player.getUser()))
                        + " " + player.getCards() + " - ";
            }
        } catch (Exception e) {
            log.error("getStringCardsOfUsers", e);
        }
        return result;
    }
    /**
     * Tổng số stack đã tố của user
     */
    public void sendUpdatePotMessage() {
        try {
            SFSObject updatePotMessage = messageFactory.getUpdatePotMessage();
            sendAllUserMessage(updatePotMessage);
        } catch (Exception e) {
            this.log.error("sendUpdatePotMessage() error:", e);
        }
    }
    
    private void sendBoardPlaying(User user){
        try {
            SFSObject playingMessage = messageFactory.getPlayingMessage();
            sendUserMessage(playingMessage, user);
        } catch (Exception e) {
            this.log.error("sendUpdatePotMessage() error:", e);
        }
    }

    /**
     * xử lý trước khi gọi hàm super.stopGame
     *
     * @throws Exception
     */
    private void processStopGame() throws Exception {
        determineWinner();
        //người có bài lớn nhất game
        XiToPlayer xitoWinner = getXiToPlayer(getIdDBOfUser(userWinner));
        BigDecimal beforeWinStack = xitoWinner.getStack();
        List<Round> listRound = getRounds();
        log.debug("processStopGame : winner: " + xitoWinner.getUser().getName() + " beforeWinStack " + beforeWinStack);
        log.debug("num rounds: " + listRound.size());
        //danh sách user thắng trong ván
        Map<String, WinnerInfor> winners = new HashMap<>();
        
        int countRound = 0;
        for (Round round : listRound) {
            XiToPlayer roundWinner = round.findWinner();
            if (roundWinner == null) {
                List<String> playerInRounds = new ArrayList<>(round.getIdDBUsers());
                if (!playerInRounds.isEmpty() && playerInRounds.size() == 1) {
                    User u = getUserPlaying(getSeatNumber(playerInRounds.get(0)));
                    if (u != null) {
                        updateMoney(u, round.getStack(playerInRounds.get(0)), CommonMoneyReasonUtils.TRA_TIEN, "", BigDecimal.ZERO,null);
                    }
                }
                continue;
            }

            //xử lý trường hợp người thắng là allin    
            if (roundWinner.isAllIn()) {
                while (round.getIdDBUsers().size() > 1) {
                    XiToPlayer roundWinnerCheck = round.findWinner();
                    if (roundWinnerCheck == null) {
                        break;
                    }
                    String idDBRoundWinnerCheck=getIdDBOfUser(roundWinnerCheck.getUser());
                    //số tiền người thắng vòng nhận được
                    BigDecimal winStack = BigDecimal.ZERO;
                    //user đang chơi trong vòng ngoại trừ người thắng vòng
                    for (String idDDBUser : round.getIdDBUsers()) {  
                        if (idDDBUser.equals(idDBRoundWinnerCheck)) {
                            continue;
                        }
                        //tìm stack còn dư trong trường hơp người thắng bet ít hơn người thua
                        BigDecimal exchange = Utils.subtract(round.getStack(idDDBUser), round.getStack(idDBRoundWinnerCheck));
                        if (exchange.signum() > 0) {
                            winStack = Utils.add(winStack, (round.getStack(idDBRoundWinnerCheck)));
                            round.minusStack(idDDBUser, round.getStack(idDBRoundWinnerCheck));
                        } else //trường hợp người thắng bet nhiều hơn hoặc bằng người thua
                        {
                            winStack = Utils.add(winStack, round.getStack(idDBRoundWinnerCheck));
                            winStack = Utils.add(winStack, exchange);
                            round.minusStack(idDDBUser, round.getStack(idDDBUser));
                        }
                    }

                    //số stack còn lại người thắng vòng ăn hết
                    //chỉ tính thuế trên stack người chơi thắng
                    BigDecimal []arrResultMoney = setMoneyMinusTax(winStack, getTax());
                    winStack = Utils.add(arrResultMoney[MONEY], round.getStack(idDBRoundWinnerCheck));
                    addWinStackToWinner(idDBRoundWinnerCheck, winStack, arrResultMoney[TAX], winners);
                    //remove để xét những user thắng còn lại
                    round.getMapStack().remove(roundWinnerCheck.getIdBDUSer());
                    //vòng 1 thì không xét user thắng kế tiếp
                    if (countRound == 0) {
                        break;
                    }
                }
                //cong lai tien dư cho player trong bàn
                for (String idDBUser : round.getIdDBUsers()) {
                    XiToPlayer xitoPlayer = getXiToPlayer(idDBUser);
                    /**
                     * trường hợp vòng này còn 1 user cũng là người thắng thìz
                     * trả stack không update tiền
                     */
                    if (xitoPlayer != null) {
                        if (xitoPlayer.equals(xitoWinner) && round.getIdDBUsers().size() == 1) {
                            addWinStackToWinner(idDBUser, round.getStack(idDBUser), BigDecimal.ZERO, winners);
                            continue;
                        }
                    }
                    //trả stack cho người thua còn dư
                    if (xitoPlayer != null && updateMoney(xitoPlayer.getUser(), round.getStack(idDBUser),
                            CommonMoneyReasonUtils.TRA_TIEN, "", BigDecimal.ZERO, null )) {
                        log.debug("Trả lại tiền: " + xitoPlayer.getUser().getName() + " win stack: " + round.getStack(idDBUser));
                    }
                }

                countRound++;
            } else {
                //số stack còn lại người thắng vòng ăn hết
                //chỉ tính thuế trên stack người chơi thắng
                String idDBRoundWinner=getIdDBOfUser(roundWinner.getUser());
                BigDecimal winStack = Utils.subtract(round.getStack() , round.getStack(idDBRoundWinner));
                BigDecimal[] arrResultMoney = setMoneyMinusTax(winStack, getTax());
                winStack = Utils.add(arrResultMoney[MONEY], round.getStack(idDBRoundWinner));
                
                addWinStackToWinner(idDBRoundWinner, winStack, arrResultMoney[TAX], winners);
                countRound++;
                log.debug("roundWinner: " + roundWinner.getUser().getName() + " win stack: " + winStack + " , " + round.getStack(idDBRoundWinner) + " ," + round.getStack());
            }
        }
        
        /**
         * cộng tiền cho các user thắng trong vòng
         */
        for (String idDBUser : winners.keySet()) {
            XiToPlayer xitoPlayer = getXiToPlayer(idDBUser);
            if (xitoPlayer == null) {
                continue;
            }
            WinnerInfor winnerInfor = winners.get(idDBUser);
            if (updateMoney(xitoPlayer.getUser(), winnerInfor.getStackWin(),
                    CommonMoneyReasonUtils.THANG, "", winnerInfor.getTaxWin(), xitoPlayer.getHoldCardsIdToList())) {
                xitoPlayer.setWinStack(winnerInfor.getStackWin());
                updateAchievement(xitoPlayer.getUser(), CommonMoneyReasonUtils.THANG);
            }
        }
        
        //add log cho loser
        for (User p : getAllPlayers()) {
            if (p == null) {
                continue;
            }
            String idDB = getIdDBOfUser(p);
            XiToPlayer xtPlayer = getXiToPlayer(idDB);
            if (xtPlayer == null || winners.containsKey(idDB)) {
                continue;
            }
            
            if(!isInturn(p) && !xtPlayer.isAllIn()){
                continue;
            }
            updateLogGameForUser(p, CommonMoneyReasonUtils.THUA, xtPlayer.getHoldCardsIdToList());
        }
        sendMessageResult();
    }
    
    /**
     * Cộng tiền vào hash map cho winners
     * chỉ sử dụng trong hàm processStopGame()
     * @param idWinner
     * @param winStack
     * @param tax
     * @param winners 
     */
    private void addWinStackToWinner(String idWinner, BigDecimal winStack, BigDecimal tax, Map<String, WinnerInfor> winners) {
        if (winStack.signum() > 0) {
            WinnerInfor winnerInfor;
            if (winners.containsKey(idWinner)) {
                winnerInfor = winners.get(idWinner);
            } else {
                winnerInfor = new WinnerInfor();
            }
            winnerInfor.addStackWin(winStack);
            winnerInfor.addTaxWin(tax);
            winners.put(idWinner, winnerInfor);
        }
    }

    /**
     * kiếm người thắng ván chơi, và set winner cho nó
     *
     * @return
     * @throws Exception
     */
    private void determineWinner() throws Exception {
        XiToPlayer winner = null;
        List<User> list = getPlayers();
        for (User player : list) {
            XiToPlayer xtPlayer = getXiToPlayer(getIdDBOfUser(player));
            //null trong trường hợp user đã thoát ra khỏi ván chơi
            if (xtPlayer == null) {
                continue;
            }
            if (!isInturn(xtPlayer.getUser()) && !xtPlayer.isAllIn()) {
                continue;
            }
            if (winner == null) {
                winner = xtPlayer;
                continue;
            }
            winner = XiToGameUtil.comparePlayerScore(winner, xtPlayer);

        }
        if (winner != null) {
            this.userWinner = winner.getUser();
        }
    }

    private void sendMessageResult() throws Exception {
        SFSObject messageResultEn = this.messageFactory.getResultMessage(GlobalsUtil.ENGLISH_LOCALE);
        SFSObject messageResultVi = this.messageFactory.getResultMessage(GlobalsUtil.VIETNAMESE_LOCALE);
        SFSObject messageResultZh = this.messageFactory.getResultMessage(GlobalsUtil.CHINESE_LOCALE);
        sendToAllWithLocale(messageResultEn, messageResultVi, messageResultZh);
    }

    private void sendMessagePreflog(XiToPlayer player) throws Exception {
        SFSObject m = this.messageFactory.getPreFlopMessage(player);
        sendUserMessage(m, player.getUser());
    }

    /**
     * Xử lý trước khi gọi hàm super.startGame
     *
     * @throws Exception
     */
    private void processStartGame() throws Exception {
        sendMessageStartGame();
        //add round
        addRound(new Round(this));
        //thong bao cho moi user
        for (User player : this.getPlayers()) {
            if (isInturn(player)) {
                this.countStartGamePlayers++;
                XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(player));
                //set xito player is turn tương ứng player trong core
                xitoPlayer.setState(xitoPlayer.getTurnState());
                xitoPlayer.minusStack(getMoney());
                updateMoney(xitoPlayer.getUser(), getMoney().negate(),CommonMoneyReasonUtils.DAT_CUOC, "", BigDecimal.ZERO,null);
                //chia moi thang 2 la
                XiToCard card1 = this.deskXiTo.dealCard();
                xitoPlayer.addCard(card1);
                XiToCard card2 = this.deskXiTo.dealCard();
                xitoPlayer.addCard(card2);
                log.debug("card of user " + xitoPlayer.getUser().getName() + " :card1 id=" + card1.getId() + " cardid2 =" + card2.getId());
                //gui ket qua ve
                sendMessagePreflog(xitoPlayer);
            }
        }
        
        for(User u : getAllWaiter()){
             XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(u));
             if(xitoPlayer == null){
                 continue;
             }
            SFSObject m = this.messageFactory.getPreFlopForSitOutMessage(xitoPlayer);
            sendUserMessage(m, u);
        }
        
       

        this.countRemainDealCard = this.countRemainDealCard - 2;
//        sendToAllViewer(this.messageFactory.getViewerMessage());
        log.debug("this.countStartGamePlayers: " + this.countStartGamePlayers);
        //change game state
        this.state = preFlogState;
        setCurrentMoveTime();
        setStateGame(this.getWaittingGameState());
        sendStartGameViewerMessge();
        sendUpdatePotMessage();
    }

    private void sendMessageStartGame() throws Exception {
        try {
            SFSObject message = this.messageFactory.getStartGameMessage();
            sendAllUserMessage(message);
        } catch (Exception e) {
            this.log.error("sendMessageStartGame() error: ", e);
        }
    }

    /**
     * tăng số người đã úp bỏ
     */
    public void incrCountFold() {
        this.countFold++;
    }

    /**
     * Player chọn mở một lá bài trong vòng preflog <br />
     * Kiem tra: neu tat ca nguoi choi deu chon xong card thi chuyen sang flog
     *
     * @param player
     * @param cardId
     */
    private void showCard(XiToPlayer xitoPlayer, byte cardId) throws Exception {
        if(xitoPlayer==null){
            return;
        }
        //luc nay nguoi choi chi co 2 la bai
        //1 la show, 1 la hide
        for (XiToCard card : xitoPlayer.getHoldCards().values()) {
            if (card.getId() == cardId) {
                xitoPlayer.setShowedCard(card);
            } else {
                xitoPlayer.setHideCard(card);
            }
        }

        //Kiem tra: neu tat ca nguoi choi deu chon xong card thi chuyen sang flog
        for (User  player : getPlayers()) {
            XiToPlayer p = getXiToPlayer(getIdDBOfUser(player));
            if (!isInturn(player) && !p.isAllIn()) {
                continue;
            }
            if (p.getShowedCard() == null) {
                return;
            }
        }
        this.state.preFlog();
    }

    /**
     * gửi thông báo mua stack
     *
     * @param user
     */
    public void sendBuyStackMessage(User user) {
        try {
//            String info = XiToLanguage.getMessage(XiToLanguage.REBUY_STACK, getLocaleOfUser(user));
//
//            info = String.format(info, Utils.formatNumber(this.getMinStack()), 
//                    Utils.formatNumber(this.getMaxStack()), Utils.formatNumber(this.getMinStack()), getCurrency(getLocaleOfUser(user)));
//
//            SFSObject stackMessage = this.messageFactory.getBuyStackMessage(getMinStack(), info, getMinStack(), getMaxStack(), user);
//            sendUserMessage(stackMessage, user);
        } catch (Exception e) {
            this.log.error("sendBuyStackMessage() error:", e);
        }
    }

    /**
     * gửi khi có người mới vào bàn
     *
     * @throws Exception
     */
    private void sendViewerMessage(User user) {
        try {
            SFSObject viewerMessage = this.messageFactory.getViewerMessage();
            sendUserMessage(viewerMessage, user);
        } catch (Exception e) {
            this.log.error("sendViewerMessage() error: ", e);
        }
    }

    /**
     * thông báo ko đủ win khi vào bàn chơi
     *
     * @param user
     * @throws Exception
     */
    private void sendSittingMessage(User user) {
        try {
            String mess = XiToLanguage.getMessage(XiToLanguage.NOT_ENOUGH_WIN_TO_PLAY, getLocaleOfUser(user));
            mess = String.format(mess,getCurrency(getLocaleOfUser(user)));
            SFSObject sitMessage = this.messageFactory.getSittingMessage(mess, user);
            sendUserMessage(sitMessage, user);
        } catch (Exception e) {
            this.log.error("sendSittingMessage() error: ", e);
        }
    }

    /**
     * override hàm này để public
     *
     * @param currentPlayer
     */
    @Override
    public void setCurrentPlayer(User currentPlayer) {
        super.setCurrentPlayer(currentPlayer);
    }


    /**
     * Kiểm tra ván nên kết thúc khi chỉ còn 1 người chơi
     *
     * @return
     */
    public boolean canStop() {
        if (!isPlaying()) {
            return false;
        }
        return getInTurnPlayers().size() <= 1;
    }
    /**
     * Override để public hàm này
     *
     * @param user
     * @param inTurnStatus
     */
    @Override
    public void setInturn(User user, boolean inTurnStatus) {
        super.setInturn(user, inTurnStatus);
    }


    /**
     * Override để chuyển sang public
     *
     * @return
     */
    @Override
    public User getCurrentPlayer() {
        return super.getCurrentPlayer();
    }

    /**
     * Client lấy thông tin trước khi set lại tiền cược của bàn
     *
     * @param user
     * @throws Exception
     */
    private void sendBetMoneyInfoMessage(User user) throws Exception {
        SFSObject m = this.messageFactory.getBetMoneyInfoMessage();
        sendUserMessage(m, user);
    }

    /**
     * Kiểm tra nếu tới lược user thì úp bỏ, dùng khi leave và stand up
     *
     * @param user
     * @throws Exception
     */
    private void checkAndFold(User user) throws Exception {
        if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
            xitoPlayer.fold();
        }
    }

    private void sendFlogMessage(Map<String, Byte> map) throws Exception {
        SFSObject flogMessage = this.messageFactory.getFlopMessage(map);
        sendAllUserMessage(flogMessage);
    }

    /**
     * Kiểm tra đã kết thúc vòng thì thêm vòng mới
     *
     * @throws Exception
     */
    private void checkFinishRoundAndAddRound() throws Exception {
        if (isPlaying() && isFinishRound() && rounds.size() < MAX_ROUND) {
            addRound(new Round(this));
        }
    }

    /**
     * Tat ca da all-in, kiem tra xem con may la chua chia, chia tất
     *
     * @throws Exception
     */
    public void dealRemainCard() throws Exception {
        this.sendUpdatePotMessage();
        for (int i = 0; i < this.countRemainDealCard; i++) {
            //key: idDDBUser, value: cardId
            Map<String, Byte> map = new HashMap<>();
            for (User user : getPlayers()) {
                String idDB = getIdDBOfUser(user);
                XiToPlayer xtPlayer = getXiToPlayer(idDB);

                if (!isInturn(xtPlayer.getUser()) && !xtPlayer.isAllIn()) {
                    continue;
                }

                XiToCard card = deskXiTo.dealCard();
                xtPlayer.addCard(card);
                map.put(idDB, card.getId());
            }
            //gui bai ve cho client
            sendFlogMessage(map);
        }
    }

    /**
     * chia moi nguoi mot la, gui thong tin diem cua bai hien tai ve cho user
     *
     * @throws Exception
     */
    public void dealFlogCard() throws Exception {
        //-> chia bai, bat dau luot
        //key: idDDBUser, value: cardId
        Map<String, Byte> map = new HashMap<>();
        for (User player : getPlayers()) {
            String idDb = getIdDBOfUser(player);
            XiToPlayer xtPlayer = getXiToPlayer(idDb);
            if (!isInturn(xtPlayer.getUser()) && !xtPlayer.isAllIn()) {
                continue;
            }

            XiToCard card = deskXiTo.dealCard();
            xtPlayer.addCard(card);
            map.put(idDb, card.getId());

            SFSObject handEvalMessage = xtPlayer.getPlayerMessageFactory().getHandEvalMessage();
            sendUserMessage(handEvalMessage, xtPlayer.getUser());
        }
        //gui bai ve cho client
        sendFlogMessage(map);
        this.countRemainDealCard--;
    }

    /**
     * đk 1: số tẩy đã đặt của những người in turn phải = nhau <br />
     * đk 2: số lượng người chơi trong vòng ko tính người đã thoát ra hoặc úp bỏ
     * phải bằng số lượng người in turn và all in
     *
     * @return
     */
    public boolean isFinishRound() throws Exception {
        int count1 = 0; //số lượng người chơi trong vòng ko tính người đã thoát ra hoặc úp bỏ
        Round round = getCurrentRound();
        BigDecimal bet1 = round.getMaxBetStackOfPlayer();

        for (String idDBUser : round.getIdDBUsers()) {
            XiToPlayer xtPlayer = getXiToPlayer(idDBUser);
            //đoạn này dùng để tính count1
            //null trong trường hợp user đã thoát ra khỏi ván chơi
            if (xtPlayer == null) {
                continue;
            }
            if (!isInturn(xtPlayer.getUser()) && !xtPlayer.isAllIn()) {
                continue;
            }
            count1++;

            //đoạn này dùng để so sánh stack, ko tính thằng all-in
            if (xtPlayer.isAllIn() && round.getStack(idDBUser).compareTo(round.getMaxBetStackOfPlayer()) < 0) {
                continue;
            }
            //lưu biến tẩy đã đặt của user để so sánh
            //số tẩy đã đặt của những người in turn phải = nhau
            if (bet1.compareTo(BigDecimal.ONE.negate()) == 0) {
                bet1 = round.getStack(idDBUser);
                continue;
            }

            if (bet1.compareTo(round.getStack(idDBUser)) != 0) {
                return false;
            }
        }

        int count2 = 0; //số lượng người in turn và all in
        for (User user : getPlayers()) {
            XiToPlayer xtPlayer = getXiToPlayer(getIdDBOfUser(user));
            if (isInturn(user)) {
                //thằng này in turn mà chưa tố lần nào thì vòng này chưa finish
                if (!round.isContain(getIdDBOfUser(user)) && this.getInTurnPlayers().size() > 1) {
                    return false;
                }

                count2++;
                continue;
            } else {
                //xet o vong 1 nếu toi lượt người tố mà rời bàn thì chuyển quyền cho người kế tiếp
                if (getRounds().size() == 1) {
                    if (this.getInTurnPlayers().size() <= 1) {
                        if (!this.isAllFolded()) {
                            //Kết thúc ván mà chưa chia ra đủ 5 lá
                            this.dealRemainCard();
                        }
                        this.stopGame();
                    }
                    return false;
                }
            }

            if (xtPlayer.isAllIn() && round.isContain(getIdDBOfUser(user))) {
                count2++;
            }
        }

        //đk 2: số lượng người chơi trong vòng ko tính người đã thoát ra hoặc úp bỏ
        //phải bằng số lượng người in turn và all in
        if (count1 == count2) {
            return true;
        }

        //còn 2 thang nhưng có 1 thằng allin
        if (this.getInTurnPlayers().size() == 1) {
            //có thằng allin thì chưa kết thúc vòng
            if (count2 > this.getInTurnPlayers().size()) {
                return false;
            } else //kết thúc vòng    
            {
                return true;
            }
        }
        //đk 3: úp bỏ hết
        if (countStartGamePlayers - countFold <= 1) {
            return true;
        }
        return false;
    }

    /**
     * Nếu là vòng đầu tiên thì thêm vòng mới vào <br />
     * Dùng khi tố, tố hết và bỏ lượt tố
     *
     */
    private void checkToAddNewRound() {
        //nếu là vòng đầu tiên thì thêm vòng mới vào
        if (rounds.size() == 1) {
            addRound(new Round(this));
        }
    }

    /**
     * Game đã chơi đủ 4 vòng
     *
     * @return
     */
    private boolean isCompleteGame() throws Exception {
        if (rounds.size() == MAX_ROUND && isFinishRound()) {
            return true;
        }
        return false;
    }

    /**
     * chơi đủ 4 vòng, stop game
     *
     */
    private void checkIsCompleteAndStopGame() throws Exception {
        if (isCompleteGame()) {
            stopGame();
        }
    }

    /**
     * chơi chưa đủ 4 vòng, nhưng ko còn ai chơi, stop game
     *
     */
    private void checkIsIncompleteAndStopGame() throws Exception {
        if (canStop()) {
            // kiểm tra số người còn chơi trong bàn > 1 để ván có 2 người chơi khi
            // 1 người rời bàn kết thúc luôn, ko cần chia hết bài và kiểm tra finishround
            if (getPlayers().size() > 1) {
                if (!getCurrentRound().isNewRound() && !isFinishRound()) {
                    if (getPlayers().size() == 2 && countLeave == 1) {
                        stopGame();
                    }
                    return;
                }
                if (!this.isAllFolded()) {
                    //Kết thúc ván mà chưa chia ra đủ 5 lá
                    this.dealRemainCard();
                }
            }
            stopGame();
        }
    }

    public int getCountRemainDealCard() {
        return countRemainDealCard;
    }

    /**
     * Gửi về danh sách những user inTurn và allin
     *
     * @return
     */
    public List<XiToPlayer> getPlayerInturnAndAllin() {
        List<XiToPlayer> TempPlayers = new ArrayList<>();
        for (XiToPlayer player : players.values()) {
            if (isInturn(player.getUser())
                    || player.isAllIn()) {
                TempPlayers.add(player);
            }
        }
        return TempPlayers;
    }
    
    /**
     * Lấy chuỗi bài những người tham gia ván chơi, dùng ghi log
     *
     * @return
     */
    private String getStringCardsOfUsers(XiToPlayer winnerXiTo) {
        StringBuilder result = new StringBuilder();
        try {
            String valueCardsWinner = XiToLanguage.getMessage(winnerXiTo.getResultCards().getStrValue(),getLocaleOfUser(winnerXiTo.getUser()));
            result.append(String.format("- User %s thắng. %s (%s).\n", winnerXiTo.getUser().getName(), valueCardsWinner, winnerXiTo.getCards()));
            List<User> list = getInTurnPlayers();
            for (User p : list) {
                if (p== null || Utils.isEqual(p, winnerXiTo.getUser())) {
                    continue;
                }
                XiToPlayer xtPlayer = getXiToPlayer(getIdDBOfUser(p));
                String valueCardsPlayer = XiToLanguage.getMessage(xtPlayer.getResultCards().getStrValue(),getLocaleOfUser(p));

                result.append(String.format("- User %s: %s (%s).\n", p.getName(), valueCardsPlayer, xtPlayer.getCards()));
            }
            if (!logTextLever.isEmpty()) {
                result.append(String.format("- User %s thoát game.\n", logTextLever));
            }
        } catch (Exception e) {
            log.error("getStringCardsOfUsers", e);
        }
        return result.toString();
    }
    /**
     * Ghi log cho user thua vì theo logic hiện tại không phát sinh log cho user
     * thua
     *
     */
    @Override
    public void updateLogGameForUser(User u, int reason, List<Short> arrayCardIds) {
        super.updateLogGameForUser(u, reason, arrayCardIds);
    }

    @Override
    public Locale getLocaleOfUser(User user) {
        return super.getLocaleOfUser(user); 
    }

    @Override
    public void sendAllUserMessage(ISFSObject params) {
        super.sendAllUserMessage(params); 
    }

    @Override
    public int getSeatNumber(String idDBUser) {
        return super.getSeatNumber(idDBUser);
    }

    @Override
    public BigDecimal getMoney() {
        return super.getMoney(); 
    }
    
    @Override
    public List<User> getInTurnPlayers() {
        return super.getInTurnPlayers(); 
    }
    public User getWinner() {
        return userWinner;
    }

    @Override
    public boolean join(User user, String pwd) {
        this.log.debug("JOIN GAME XI TO: " + user.getName());
        try {
            if(!super.join(user, pwd)){
                return false;
            }
            if (isPlaying()) {
                sendBoardPlaying(user);  
            }
            addNewXiToPlayer(user);
            processCountDownStartGame();
            return true;
        } catch (Exception e) {
            this.log.error("join() error: ", e);
        }
        return false;
    }
    /**
     *  thời gian schedule 1 lượt chơi của player
     */
    public void setStateWating(){
        setCurrentMoveTime();
        this.setStateGame(this.getWaittingGameState());
    }

    @Override
    public void sendToastMessage(String errString, User user, int i) {
        super.sendToastMessage(errString, user, i); 
    }
    public void getLogger(String info){
        this.log.debug(info);
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user);
    }

    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(XiToConfig.getInstance().getMaxViewer());
    }

    @Override
    public void setCurrentMoveTime() {
        super.setCurrentMoveTime();
    }
    /**
     * add new user đến xi to player
     *
     * @param user
     */
    private void addNewXiToPlayer(User user) {
        boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "join to player", 0);
        XiToPlayer player = new XiToPlayer(user, this);
        this.players.put(getIdDBOfUser(user), player);
    }

    @Override
    protected void waiterBuyStack(User user) {
        super.waiterBuyStack(user); 
        addNewXiToPlayer(user);
    }

    @Override
    public BigDecimal getMoneyFromUser(User user) {
        return super.getMoneyFromUser(user);
    }

    @Override
    public int getTimeLimit() {
        return super.getTimeLimit();
    }
        /**
     * Trả về thời gian còn lại của vòng preflog, dùng trong message return game
     *
     * @return
     */
    public byte getRemainTimePreflog() {
        try {
            long remain = (System.currentTimeMillis() - getTimeToStart()) / 1000;
            if (remain < XiToConfig.getInstance().getPreFlogTime()) {
                return (byte) (XiToConfig.getInstance().getPreFlogTime() - remain);
            }
        } catch (Exception e) {
            log.error("getRemainTimePreflog error: " + boardLog.getLog(), e);
        }
        return 0;
    }

    @Override
    public void onReturnGame(User user) {
        super.onReturnGame(user); 
        try {
            XiToPlayer xitoPlayer = getXiToPlayer(getIdDBOfUser(user));
            if(xitoPlayer==null){
                return;
            }
            xitoPlayer.setUser(user);
            SFSObject ob= xitoPlayer.getReturnMessage();
            sendUserMessage(ob, user);
        } catch (Exception e) {
            log.error("xito.onReturnGame() error: ", e);
        }
    }

    @Override
    protected byte getServiceId() {
        return Service.XI_TO;
    }

    @Override
    protected boolean isCanBuyIn(User player, double buyStackMoney) {
        if(isInturn(player)){
            return false;
        }
        return super.isCanBuyIn(player, buyStackMoney);
    }
    
}
