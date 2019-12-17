/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templatesf
 * and open the template in the editor.
 */
package game.vn.game.blackjack;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.game.blackjack.lang.BlackJackLanguage;
import game.vn.game.blackjack.message.MessageFactory;
import game.vn.game.blackjack.object.BlackJackPlayer;
import game.vn.common.GameController;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.event.EventManager;
import game.vn.common.lang.GameLanguage;
import game.vn.common.object.MoneyManagement;
import game.vn.game.blackjack.object.CardSetTest;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;

/**
 * Xử lý logic game black jack
 * @author tuanp
 */
public class BlackJackGameController extends GameController{

    private static final int DEFAULT_CARD_NUMBER = 2;
    public static final int DEFAULT_MAX_CARD_NUMBER = 5;
    
    //rút card
    private static final byte GET_CARD=0;
    //dằn bài
    private static final byte STOP_GET_CARD=1;
    //cai xet bai tat cả các user
    private static final byte CHECK_ALL=0;
    
    private  transient CardSet cardSet;
    private final BlackJackPlayer[] players ;
    private User whoMove, nextMove;
    //xử lý những message liên quan tới xì zách
    private final MessageFactory messageFactory;
    //xử lý để lưu tiền cược của user
    MoneyManagement bJMoneyBetOfUser;
    //sử dụng để kiểm tra all user đã đặt cược
    private boolean isBetted;
    private final Logger log;
    //danh sach card khi co pots
    List<Card> botDealCards;
    private final Random random;
    private boolean isOpenBot;
    
    public BlackJackGameController(Room room, BlackJackGame gameEx) {
        super(room, gameEx);
        this.messageFactory = new MessageFactory(this);
        this.bJMoneyBetOfUser = new MoneyManagement();
        this.players = new BlackJackPlayer[room.getMaxUsers()];
        for(int i=0;i< this.players.length;i++){
            players[i]= new BlackJackPlayer();
        }
        cardSet = new CardSet();
        this.log= this.game.getLogger();
        botDealCards = new ArrayList<>();
        this.random = new Random();
    }
     /**
     * bat dau lai 1 van choi moi.
     */
    private void reset() {
        this.log.debug("reset game");
        try {
            whoMove = null;
            nextMove = null;
            isBetted = false;
            for (BlackJackPlayer player : players) {
                if(player==null){
                    continue;
                }
                player.reset();
            }
            bJMoneyBetOfUser.resetMoneyIngame();
        } catch (Exception e) {
           this.log.error("BJ reset error r", e);
        }
    }

    @Override
    public synchronized boolean join(User user, String pwd) {
        boolean status=false;
         try {
            if (!super.join(user, pwd)) {
                addToWaitingUserList(user, "");
                status= false;
            }
            if(processJoinBoardSuccess(user)){
                status=true;
                updateBoardPlayingToHazelcast();
            }
        } catch (Exception ex) {
            this.log.error("BJ join got exception: r", ex);
        }
        return status;
    }

    @Override
    public void startGame() {
        try {
            if (!checkOwnerMoney()) {
                String s = GameLanguage.getMessage(GameLanguage.NO_MONEY_OWNER,getLocaleOfUser(this.getOwner()));
                s = String.format(s, getCurrency(getLocaleOfUser(this.getOwner())));
                addToWaitingUserList(this.getOwner(), s);
                return;
            }
            kickUserNotEnoughMoney();
            this.log.debug("-------------BLACK JACK START GAME---------------");
            super.startGame();
            if (isPlaying()) {
                if (getBeginUser() == null) {
                    addAllUserToWaitingUserList();
                    return;
                }
                if(!datCuoc()){
                   this.log.debug("Stop game khi dat cuoc khong thanh cong ");
                   stopGame(); 
                   return;
                }
                /**
                 * Trường hợp 2 người chơi, 1 người đặt cược không thành công bị kick thì ván sẽ stop
                 * lúc đó không chia bài và gửi message start nữa
                 */
                if(isPlaying()){
                    boolean haveBot = false;
                    for(int i=0; i< this.players.length;i++){
                        if(isBot(getUser(i))){
                            haveBot = true;
                        }
                    }
                    isOpenBot = haveBot && isOpenBotGame() ;
                    deal();
                    processStartGame();
                    setCurrentMoveTime();
                    setStateGame(this.getWaittingGameState());
                    isBetted = true;
                    checkXiDach();
                }
                updateBoardPlayingToHazelcast();
            }
        } catch (Exception e) {
            this.log.error("BJ startGame error:", e);
        }
    }

    @Override
    public void update() {
        try {
            super.update();
            if(isCanStart()){
                startGame();
            }
            if (isPlaying() && isTimeout()) {
                if (getCurrentPlayer() == null) {
                    this.log.debug("current user is null");
                    stopGame();
                    return;
                }
                standGetCard(getCurrentPlayer());
            }
        } catch (Exception e) {
            this.log.error("update() erro:", e);
        }
    }
    
    @Override
    public synchronized void leave(User player) {
        boolean isChangeOwner = false;
        try {
            int seat = getSeatNumber(getIdDBOfUser(player));
            boolean isInTurn = isInturn(player) ;
            //lấy chủ phòng rời bài để xử lý trừ tiền
            User leaveOwner = this.getOwner();
            if(Utils.isEqual(this.getOwner(), player)) {
                isChangeOwner = true;
            }
            this.log.debug("Thoát game:"+player.getName());
            super.leave(player);
            
            if (isPlaying() && seat > -1 && isInTurn) {
                if (isChangeOwner) {
                    for (int i = 0; i < players.length; i++) {
                        if (players[i].isPlaying() && i != seat) {
                            players[i].setStatus(BlackJackPlayer.THANG_STATUS);
                            players[i].setReason();
                            congTien(i, seat);
                        }
                    }
                    // win còn dư cộng lại cho thằng chủ bàn
                    if (bJMoneyBetOfUser.getInGameMoney().signum() > 0) {
                        caculateMoneyforOnwer(seat,leaveOwner);
                    }
                    players[seat].setPlaying(false);
                    stopGame();
                } else if (players[seat].isPlaying()) {
                    players[seat].setPlaying(false);
                    if (!checkPlaying()) {
                        stopGame();
                    }
                }
                //nếu user thoát game là mover
                if (Utils.isEqual(player, whoMove)) {
                    processMoverLeave(seat,isChangeOwner);
                }
                
            }
            //reset lại tiền cược khi rời bàn
            bJMoneyBetOfUser.remove(getIdDBOfUser(player));
            if (seat > -1) {
                players[seat].setBetted(false);
            }

        } catch (Exception e) {
            this.log.error("BJ leave() erro:", e);
        } finally {
            forceLogoutUser(player);
            //reset countDown khi chủ bàn rời khỏi bàn
            if (this.getPlayersList().size() > 1 && isChangeOwner) {
                processCountDownStartGame();
                //nếu người rời bàn là chủ bàn thì chọn lại chủ bàn
                processChoseNewOwner();
            }
        }
    }

    @Override
    public synchronized void stopGame() {
        if(!isPlaying()){
            return ;
        }
        //sử dũng để kiểm tra có đổi nhà cái hay không
        User owner = this.getOwner();
        try {
            stopGameInMoney();  
        } catch (Exception ex) {
            this.log.error("BJ stopGame got exception: r", ex);
        } finally {
            super.stopGame();
            //kiểm tra afk của user
            for (User user: getPlayersList()) {
                checkNoActionBetGame(user);
            }
            this.log.debug("-------------BLACK JACK STOP GAME---------------");
            reset();
        }
        for (User user : this.getPlayersList()) {
            // set lại tiền cược của user
            if (!Utils.isEqual(user, this.getOwner())) {
                String idDBUser = getIdDBOfUser(user);
                BigDecimal betMoney = getMoneyFromUser(user).min(getBettingMoney(idDBUser));
                if (betMoney != getBettingMoney(idDBUser)) {
                    try {
                        SFSObject m = this.messageFactory.getBetMoneyMessage(user, betMoney.doubleValue());
                        sendAllUserMessage(m);
                        setBettingMoney(user, betMoney);
                    } catch (Exception ex) {
                        this.log.error("reset bet money erro", ex);
                    }
                }
            }
        }
        BigDecimal countPlayer = new BigDecimal(String.valueOf(this.getPlayersList().size() - 1));
        BigDecimal moneyCheck = Utils.multiply(getMoney(), countPlayer);
        //số tiền của nhà cái >= số tiền tối thiếu *số nhà con
        if (this.getOwner() == null || getMoneyFromUser(this.getOwner()).compareTo(moneyCheck) < 0) {
            processChoseNewOwner();
        }
        
        if (this.getOwner() == null){
            return;
        }
        //gửi thông báo chọn nhà cái mới
        if (!Utils.isEqual(owner, this.getOwner())) {
            sendDialogInforNewOwner();
        }
        /*
         Kiểm tra user có đủ tiền cho ván tiếp theo
         nếu không đủ thì reset tiền cược nhà con về mức cược tối thiểu
         */
        if (!checkOwnerMoney()) {
            resetMoneyBoard();
        }
        processCountDownStartGame();
    }
    @Override
    public void processMessage(User player, ISFSObject sfsObj) {
        super.processMessage(player, sfsObj);
        try {
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            int sNum = getSeatNumber(getIdDBOfUser(player));
            if (isPlaying() && (getCurrentPlayer() != player || sNum < 0)) {
                return;
            }
            switch (idAction) {
                case SFSAction.MOVE:
                    //type 0 -> rút 1 quân bài
                    //type 1 -> thôi ko rút nữa
                    int type = sfsObj.getByte(SFSKey.TYPE);
                    if (type == GET_CARD) {
                        getCard(player);
                    } else if (type == STOP_GET_CARD) {
                        standGetCard(player);
                    }
                    break;
                case SFSAction.CHECK_CARD:
                    //nếu id là 0 --> xét hết
                    //nếu id >1 --> chỉ xét user với userName đó
                    if (Utils.isEqual(player, this.getOwner())) {
                        if (players[sNum].getResult() >= BlackJackPlayer.QUAC) {
                            //nhà cái phải đủ 15 tuổi để xét bài, quắc cho xét luôn chết ráng chịu
                            int typeCheck = sfsObj.getByte(SFSKey.TYPE);

                            if (typeCheck == CHECK_ALL) {//check all
                                stopGame();
                            } else {//check 1 user.
                                int seat=sfsObj.getByte(SFSKey.SEAT_USER);
                                xetBai(seat);
                                if (!checkPlaying()) {
                                    stopGame();
                                } else {
                                    //cộng thêm 5s cho mỗi lần rut
                                    addCurrentMoveTime(5000);
                                    SFSObject msgTimeLimit = this.messageFactory.getNextTurnMessage(getIdDBOfUser(getCurrentPlayer()), getTimeRemain());
                                    sendAllUserMessage(msgTimeLimit);
//                                    sendUserMessage(msgTimeLimit, player);
                                }
                            }
                        } else {
                            String infor=String.format(BlackJackLanguage.getMessage(BlackJackLanguage.BL_CHECK_1, getLocaleOfUser(player)), BlackJackPlayer.TUOI_BAI_CAI);
                            sendToastMessage(infor, player, 3);
                        }
                    }
                    break;
                case SFSAction.BET:
                    //Thời gian countDown tối thiểu để đặt cược tối thiểu là 5s
                    if (getTimeToStart() < BlackJackConfig.getInstance().getMinTimeSetMoney()) {
                        sendToastMessage(BlackJackLanguage.getMessage(BlackJackLanguage.OVER_TIME_SET_MONEY, getLocaleOfUser(player)), player, 3);
                        break;
                    }
                    // Xu ly viec dat tien cua user khong phai la cai.
                    if (Utils.isEqual(player, this.getOwner()) || isPlaying() || sNum < 0) {
                        break;
                    } else {
                        if (players[sNum].isBetted()) {
                            sendToastMessage(BlackJackLanguage.getMessage(BlackJackLanguage.ERRO_SET_MONEY, getLocaleOfUser(player)), player, 3);
                            break;
                        }
                        BigDecimal money = new BigDecimal(String.valueOf(sfsObj.getDouble(SFSKey.MONEY_BET)));
                        money = Utils.getRoundBigDecimal(money);
                        money = checkUserSetMoney(player, money);
                        if (processSetMoney(player, money, 0)) {
                            players[sNum].setBetted(true);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            this.log.error("BLACL JACK processMessage erro:", e);
        }
    }

    public BlackJackPlayer[] getPlayers() {
        return players;
    }

    /**
     * Tiền user đặt cược trước khi start ván
     *
     * @param user
     * @param nMoney
     */
    private void setBettingMoney(User user, BigDecimal nMoney) {
        if (user != null) {
            //kiểm tra nếu là dân thì chỉ được xét cược trước ván
            bJMoneyBetOfUser.bettingMoney(getIdDBOfUser(user), nMoney);
        }
    }
    public BigDecimal getBettingMoney(String idDBUser) {
        return bJMoneyBetOfUser.getBettingMoney(idDBUser);
    }
    private boolean checkOwnerMoney() {
        try {
            BigDecimal money = getMoneyFromUser(this.getOwner());
            BigDecimal totalUserMoney = BigDecimal.ZERO;
            if ((money.compareTo(getMoney()) <  0 || money.signum() == 0) && getMoney().signum()!=0){
                return false;
            }

            //lấy hết tiền đặt cược của người chơi
            for (User u: this.getPlayersList()) {
                if ( u != this.getOwner()) {
                    totalUserMoney = Utils.add(totalUserMoney, getBettingMoney(getIdDBOfUser(u)));
                }
            }
            return money.compareTo(totalUserMoney) >= 0;
        } catch (Exception e) {
            this.log.error("BJ checkOwnerMoney erro :", e);
            return false;
        }
    }
    private void checkPlayerEnoughMoney(User bp) {
        try {
            int nSeat = getSeatNumber(getIdDBOfUser(bp));
        if (nSeat > -1) {
            if (getMoneyFromUser(bp).signum() < 0 || getMoneyFromUser(bp).compareTo(getMoney()) < 0) {
                String s = GameLanguage.getMessage(GameLanguage.NO_MONEY_USER,getLocaleOfUser(bp));
                s = String.format(s, getMinJoinGame());
                sendToastMessage(s, bp,3);
                return;
            }
            if (!Utils.isEqual(bp, this.getOwner()) && !processSetMoney(bp, getBettingMoney(getIdDBOfUser(bp)), 1)) {
                String infor = String.format(BlackJackLanguage.getMessage(BlackJackLanguage.OWNER_NOT_ENOGH_MONEY,getLocaleOfUser(bp)), getCurrencyName(getLocaleOfUser(bp)));
                addToWaitingUserList(bp, infor);
            }
        }
        } catch (Exception e) {
            this.log.error("checkPlayerEnoughMoney() erro:",e);
        }
    }
    /**
     * truong hop user dan lang gui tien dat len server.
     *
     * @param user nguoi gui.
     * @param money so tien dat.
     */
    private boolean processSetMoney(User user, BigDecimal money, int isJoining) {
        boolean status = false;
        try {
            BigDecimal nRoomMoney = getMoney();
            // đặt lớn hơn max cược
            if (money.compareTo(getMaxBoardValue(user)) > 0) {
                String err = String.format(BlackJackLanguage.getMessage(BlackJackLanguage.BET_MONEY,getLocaleOfUser(user)), getMaxBet());
                sendToastMessage(err, user, 3);
                money = getMaxBoardValue(user);
            }
            // trường hợp tiền đặt nhỏ hơn tiền phòng
            if (money.compareTo(nRoomMoney) < 0) {
                String sErr = BlackJackLanguage.getMessage(BlackJackLanguage.NOT_ENOUGH_MONEYBOARD, getLocaleOfUser(user)) + getMoney();
                sendToastMessage(sErr, user, 3);
                return false;
            }
            // Kiem tra tien cua no co du hay khong.
            if (!Utils.isEqual(user, this.getOwner()) && getMoneyFromUser(user).compareTo(money) < 0) {
                String errString= GameLanguage.getMessage(GameLanguage.SET_MONEY_7, getLocaleOfUser(user));
                errString = String.format(errString, Utils.getStringStack(getMoneyFromUser(user).doubleValue(), getMoneyType()), getCurrency(getLocaleOfUser(user)));
                sendToastMessage(errString, user,3);
            }
            money = money.min(getMoneyFromUser(user));
            // tinh tien thang chu ban coi co du tien chung khong
            BigDecimal tienDat = checkTienCai(user);
            BigDecimal tienCon =Utils.subtract(getMoneyFromUser(this.getOwner()), tienDat); //tiền của nhà cái sau khi trừ hết tiền cược
            if (tienCon.signum() < 0 || tienCon.compareTo(nRoomMoney) < 0 ) {
                // Cai khong du tien chung cho dan
                // Reset tien cua dan
                if (Utils.isEqual(user, this.getOwner())) {
                    String errString = GameLanguage.getMessage(GameLanguage.SET_MONEY_8, getLocaleOfUser(user));
                    errString =String.format(errString, getCurrency(getLocaleOfUser(user)));
                    sendToastMessage(errString, user,3);
                }
                return false;
            }
            if (tienCon.compareTo(money) < 0 && tienCon.compareTo(nRoomMoney) >= 0) {
                // Cai khong du tien dat cho thang nay.
                // user chi co the dat so tien con lai cua cai.
                String errString = GameLanguage.getMessage(GameLanguage.SET_MONEY_9, getLocaleOfUser(user));
                errString = String.format(errString, Utils.getStringStack(tienCon.doubleValue(),getMoneyType()) , getCurrency(getLocaleOfUser(user)));
                sendToastMessage(errString, user,3);
                money = tienCon;
            }

            if (getSeatNumber(getIdDBOfUser(user)) == getSeatNumber(getIdDBOfUser(this.getOwner()))) {     //cái thì tiền cược là 0
                money = BigDecimal.ZERO;
            }

            int nSeat = getSeatNumber(getIdDBOfUser(user));
            if (nSeat != -1) {
                setBettingMoney(user, money);
            }

            if (isJoining == 0) {//dan dat tien
                SFSObject ob = this.messageFactory.getBetMoneyMessage(user, money.doubleValue());
                for (User u: this.getPlayersList()) {
                    sendUserMessage(ob, u);
                }
            } else if (isJoining == 1) {
                for (User u: this.getPlayersList()) {
                    // cai dat tien
                    SFSObject ob = this.messageFactory.getBetMoneyMessage(u,getBettingMoney(getIdDBOfUser(u)).doubleValue());
                    sendAllUserMessage(ob);
                    }
                }
            status = true;
        } catch (Exception e) {
            this.log.error("BJ proccessSetMoney room error: ", e);
        }
        return status;
    }
      /**
     * Tinh tong so tien dat cua cai khong tinh thang hien tai.
     *
     * @param user nguoi dat tien.
     * @return Tong so tien cai da dat.
     */
    private BigDecimal checkTienCai(User user) {
        BigDecimal totalMoney = BigDecimal.ZERO;
        try {
            for(User danUser : this.getPlayersList()) {
                if (danUser != null && ! Utils.isEqual(danUser, this.getOwner())
                        && !Utils.isEqual(danUser, user)) {
                    // cong tien nhung thang khac da dat.
                    totalMoney = Utils.add(totalMoney, getBettingMoney(getIdDBOfUser(danUser)));
                }
            }
        } catch (Exception e) {
            this.log.error("checkTienCai erro:", e);
        }
        return totalMoney;
    }
     /**
     * tìm người đầu tiên lật bài (người ngồi ngay sau chủ bàn)
     *
     * @return user
     */
    private User getBeginUser() {
        try {
            User user = getUser(0);
            int nSeatOwner = getSeatNumber(this.getOwner());
            if (user == null) {
                for (User u : this.getPlayersList()) {
                    user = u;
                    if (user != null && getSeatNumber(user) != nSeatOwner) {
                        break;
                    }
                }
            }

            for (User u : this.getPlayersList()) {
                int seat=getSeatNumber(getIdDBOfUser(u));
                if (u != null && seat!= nSeatOwner && seat> nSeatOwner) {
                    return u;
                }
            }
            return user;
        } catch (Exception e) {
            this.log.error("BJ getBeginUser room error: ", e);
            return null;
        }
    }
     /**
     * check và kick những user thấp dưới mức tiền cược
     */
    private void kickUserNotEnoughMoney() {
        //kich những user không đủ tiền cược
        for (User u : this.getPlayersList()) {
            if (u != null) {
                checkPlayerEnoughMoney(u);
            }
        }
    }
     /**
     * 
     */
    private void addAllUserToWaitingUserList() {
        for (User user : this.getPlayersList()) {
            if (user == null) {
                continue;
            }
            addToWaitingUserList(user, "");
        }
    }
     /**
     * Tinh tien dat cuoc cho tat ca cac user.
     */
    private boolean datCuoc() {
        try {
            BigDecimal ownerMoney = BigDecimal.ZERO;
            int countPlayerBet=0;
            for (User user : this.getInTurnPlayers()) {
                if (user != this.getOwner()) {
                    BigDecimal money = getBettingMoney(getIdDBOfUser(user));
                    if (!updateMoney(user, money.negate(), CommonMoneyReasonUtils.DAT_CUOC)){
                        String s= GameLanguage.getMessage(GameLanguage.NO_MONEY_USER,getLocaleOfUser(user));
                        s = String.format(s, getMinJoinGame());
                        addToWaitingUserList(user, s);
                        continue;
                    }
                    ownerMoney = Utils.add(ownerMoney, money);    //cộng hết tiền lại
                    bJMoneyBetOfUser.addInGameMoney(money);
                    countPlayerBet++;
                }
            }
            /**
             * nếu kick hết user chỉ còn thằng cái thì return false de ket thuc game
             */
            if (countPlayerBet==0) {
                return false;
            }
            // Tru tien cai
            setBettingMoney(this.getOwner(), ownerMoney);
            
            if (!updateMoney(this.getOwner(), ownerMoney.negate(), CommonMoneyReasonUtils.CAI_DAT_CUOC)){
                // trả tiền lại cho nhà con 
                for (User user: this.getPlayersList()) {
                    if (user != null && !Utils.isEqual(user, this.getOwner())) {
                        String idDBUser=getIdDBOfUser(user);
                        BigDecimal betMoney = getBettingMoney(idDBUser);
                        players[getSeatNumber(idDBUser)].setStatus(BlackJackPlayer.HOA_STATUS);
                        updateMoney(user, betMoney, CommonMoneyReasonUtils.TRA_TIEN);
                        bJMoneyBetOfUser.addInGameMoney(betMoney.negate());
                    }
                }
                String s = GameLanguage.getMessage(GameLanguage.NO_MONEY_USER,getLocaleOfUser(this.getOwner()));
                s = String.format(s, getMinJoinGame());
                addToWaitingUserList(this.getOwner(), s);
                return false;
            }
            this.log.debug("ownerMoney " + ownerMoney);
            bJMoneyBetOfUser.addInGameMoney(ownerMoney);
            return true;
        } catch (Exception e) {
            this.log.error("BJ datCuoc got exception: room():", e);
        }
        return false;
    }
    
     /**
     * update money dùng reason log mới
     * Sử dung updateMoney để đảm bảo không bị âm win
     * Đảm bảo phải trừ win cho user thua trước
     * Sau đó mới thực hiện công win thắng
     * @param player
     * @param value
     * @param text thông tin hiện ra trên client
     * @param reasonId
     * @param desc ghi lại số điểm của người chơi
     */
    private synchronized boolean updateMoney(User player, BigDecimal value, int reasonId) {
        try {
            if (player == null) {
                return false;
            }
            if (value.signum() == 0) {
                return true;
            }
            BigDecimal[] result = new BigDecimal[2];
            result[MONEY] = value;
            result[TAX] = BigDecimal.ZERO;
            int seatPlayer = getSeatNumber(player);
            if (value.signum() > 0 && (players[seatPlayer].getStatus() != BlackJackPlayer.HOA_STATUS
                    && players[seatPlayer].getStatus() != BlackJackPlayer.THUA_STATUS)) {
                result = getMoneyMinusTax(player, value);
            }
           

            List<Short> arrayCardIDS = new ArrayList<>();
            if (reasonId != CommonMoneyReasonUtils.DAT_CUOC && reasonId != CommonMoneyReasonUtils.CAI_DAT_CUOC) {
                arrayCardIDS = players[seatPlayer].card2List();
                updateBlackJackResult(player, reasonId);
            }
            /**
             * Sử dụng updateMoney để đảm bảo không âm win
             */
            if (super.updateMoney(player, result[MONEY], reasonId, result[TAX], arrayCardIDS)) {
                sendBonusMoney(player, result[MONEY].doubleValue());
                return true;
            }

        } catch (Exception ex) {
            this.log.error("BJ updateMoney() erro:", ex);
        }
        return false;
    }
    
    private BigDecimal[] getMoneyMinusTax(User user, BigDecimal nMoney) {
        BigDecimal[] resultMoney = new BigDecimal[2];
        resultMoney[TAX] = BigDecimal.ZERO;
        BigDecimal moneyCanWin = Utils.subtract(nMoney, getBettingMoney(getIdDBOfUser(user)));
        //user thắng thì log tax va tính tax dựa trên tiền thực tế user ăn được
        if (moneyCanWin.signum() > 0) {
            resultMoney = setMoneyMinusTax(moneyCanWin, getTax());
            resultMoney[MONEY] = Utils.add(resultMoney[MONEY], getBettingMoney(getIdDBOfUser(user)));
        } else {
            //user hòa hoặc thua thì không log va tính thuế
            resultMoney[MONEY] = nMoney;
        }
        return resultMoney;
    }
        
    /**
     * Gửi tiền thắng thua của user cho client hiển thị
     *
     * @param user
     * @param value
     * @param textVi
     * @param textEn
     */
    private void sendBonusMoney(User user, double value) {
        String idDB = getIdDBOfUser(user);
        SFSObject mVi = getBonusMoney(idDB,value, "");
        sendToAllWithLocale(mVi, mVi, mVi);
    }
     /**
     * chia bai cho cac user trong game.
     */
    private void deal() {
        try {
            if (BlackJackConfig.getInstance().isTest()) {
                cardSet = new CardSetTest(BlackJackConfig.getInstance().getTestCase());
            } else {
                cardSet.xaoBai();
            }
           
            if (isOpenBot) {
                 botDealCards.clear();
                 botDealCards.addAll(cardSet.getCards());
                 for (BlackJackPlayer player : players) {
                    for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
                        Card card = botDealCards.get(0);
                        player.receivedCard(card);
                        botDealCards.remove(card);
                    }
                }

            } else {
                for (BlackJackPlayer player : players) {
                    for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
                        Card card = cardSet.dealCard();
                        player.receivedCard(card);
                    }
                }
            }
           
            addEvent();
        } catch (Exception e) {
            this.log.error("BJ deal()error:", e);
        }
    }
    
     private void addEvent() {
        try {
            if (isEnableEvent()) {
                // check xem có user nào đủ điều kiện nhận thưởng sự kiện event Trung thu:
                for (int i = 0; i < players.length; i++) {
                    User u = getUser(i);
                    if (u == null) {
                        continue;
                    }

                    //xì bàng
                    int result = players[i].getResult();
                    if (result == BlackJackPlayer.XI_LAC) {
                        addUserGetEvent(u, EventManager.XI_BANG, players[i].getListCardIds());
                        continue;
                    }
                    //xì dách
                    if (result == BlackJackPlayer.XI_DACH) {
                        addUserGetEvent(u, EventManager.XI_DACH, players[i].getListCardIds());
                        continue;
                    }

                }
            }
        } catch (Exception e) {
            log.error("Event error ", e);
        }
    }
    
    /**
     * Xu ly trong truong hop bat dau van choi moi. trừ tiền cược của user khi
     * bắt đầu ván.
     */
    private void processStartGame() {
        try {
            whoMove = getBeginUser();
            setCurrentPlayer(whoMove);
            int nOwnerSeat = getSeatNumber(getIdDBOfUser(this.getOwner()));
            for(int i=0; i< this.players.length;i++){
                User user= getUser(i);
                if(!isInturn(user)){
                    continue;
                }
                players[i].setPlaying(true);
                //gưi bài den tat ca user
                SFSObject messageMe = this.messageFactory.getStartGameMessage(players[i],getIdDBOfUser(whoMove),getPlayingTime()/1000, getPlayerInturnIds());
                sendUserMessage(messageMe, user);
                sendResult(user, false);
            }
            sendStartGameViewerMessge();
        } catch (Exception ex) {
            this.log.error("BJ processStartGame(): ", ex);
        }
    }
    /**
     * gởi thông báo kết quả tới người chơi trước khi kết thúc ván
     *
     * @param player
     * @param send2All //gởi cho tất cả hoặc chỉ mình player
     */
    private void sendResult(User player, boolean send2All) {
        try {
            int nSeat = getSeatNumber(getIdDBOfUser(player));
            if (nSeat > -1) {
                String idDB = getIdDBOfUser(player);
                int resultId = players[nSeat].getResult();
                if (send2All) {
                    SFSObject resultMessageVi = this.messageFactory.getSendResultMessage(players[nSeat],idDB, players[nSeat].getResultString(GlobalsUtil.VIETNAMESE_LOCALE, resultId));
                    SFSObject resultMessageEn = this.messageFactory.getSendResultMessage(players[nSeat],idDB, players[nSeat].getResultString(GlobalsUtil.ENGLISH_LOCALE, resultId));
                    SFSObject resultMessageZh = this.messageFactory.getSendResultMessage(players[nSeat],idDB, players[nSeat].getResultString(GlobalsUtil.CHINESE_LOCALE, resultId));
                    sendToAllWithLocale(resultMessageEn, resultMessageVi, resultMessageZh);
                } else {
                    SFSObject resultMessage = this.messageFactory.getSendResultMessage(players[nSeat],idDB, players[nSeat].getResultString(getLocaleOfUser(player), resultId));
                    resultMessage.putInt(SFSKey.POINT, players[nSeat].getResult());
                    sendUserMessage(resultMessage, player);
                }
            }
        } catch (Exception ex) {
            this.log.error("BJ ssendResult()error: ", ex);
        }
    }
     /**
     * Kiểm tra xì dách trước mỗi ván chơi.
     */
    private void checkXiDach() {
        try {
            //kiểm tra bài chủ bàn
            if (players[getSeatNumber(getOwner())].getResult() == BlackJackPlayer.XI_LAC
                    || players[getSeatNumber(this.getOwner())].getResult() == BlackJackPlayer.XI_DACH) {   
                //chủ bàn xì dách --> so bài --> kết thúc
                stopGame();
            } else {
                for (User user : this.getPlayersList()) {
                    int nSeat = getSeatNumber(user);
                    int result = players[nSeat].getResult();
                    if (result == BlackJackPlayer.XI_DACH || result == BlackJackPlayer.XI_LAC) {
                        //người chơi xì dách --> so bài chủ bàn rồi tính tiền
                        xetBai(getSeatNumber(user));
                        if (Utils.isEqual(user, whoMove)) { 
                            //nếu người chơi là người đi đầu tiên thì chuyển lượt
                            go2NextPlayer(user);
                        }
                    }
                }
                if (!checkPlaying()) { 
                    //nếu lật hết bài rồi thì kết thúc
                    stopGame();
                }
            }
        } catch (Exception e) {
            this.log.error("BJ checkXiDach() erro:", e);
        }
    }
    /**
     * Hàm này chỉ được phép gọi khi nhà cái gửi command xét bài 1 user hoặc
     * trong trường hợp kết thúc ván. Không được xét bài nếu ai đó thoát ngang
     * Xet bai user: So sánh điểm của cái thời điểm hiện tại và điểm của user.
     * <br> Nếu cái thua thì cộng tiền ngay lập tức cho người bị xét và chuyển
     * status của dân.
     */
    private void xetBai(int seatNum) {
        try {
            if (seatNum == -1) {
                return;
            }
            int seatOwner= getSeatNumber(getIdDBOfUser(this.getOwner()));
            if (seatNum ==seatOwner) {
                return;
            }
            if (!players[seatNum].isPlaying()) {
                return;
            }
            
            User user = getUser(seatNum);
            BlackJackPlayer player = players[seatNum];
            int resultId = player.getResult();
            this.log.debug("xet bai seatNum " + seatNum + " " + user.getName() + " " + players[seatNum].getResultString(getLocaleOfUser(user), resultId) + " " + players[seatNum].getStrCard());
            this.log.debug(user.getName() + " " + player.getResultString(getLocaleOfUser(user), resultId) + " " + player.getStrCard());
            player.checkStatusWithCai(players[seatOwner].getResult());
            processLatBai(getUser(seatNum));
            
            players[seatNum].setReason();
            if (player.getStatus() == BlackJackPlayer.THUA_STATUS) {
                players[seatNum].setPlaying(false);               
                updateLogGameForUser(user,CommonMoneyReasonUtils.THUA,player.card2List());
            } else if (players[seatNum].getStatus() == BlackJackPlayer.THANG_STATUS) {
                congTien(seatNum,seatOwner);
            } else if (players[seatNum].getStatus() == BlackJackPlayer.HOA_STATUS) {
                congTien(seatNum, seatOwner);
            }
            
            try {
                String idDBUser = getIdDBOfUser(user);
                SFSObject resultMessageVi =this.messageFactory.getSendResultMessage(players[seatNum], idDBUser, players[seatNum].getResultString(GlobalsUtil.VIETNAMESE_LOCALE, resultId)) ;
                SFSObject resultMessageEn =this.messageFactory.getSendResultMessage(players[seatNum], idDBUser,players[seatNum].getResultString(GlobalsUtil.ENGLISH_LOCALE, resultId)) ;
                SFSObject resultMessageZh =this.messageFactory.getSendResultMessage(players[seatNum], idDBUser,players[seatNum].getResultString(GlobalsUtil.CHINESE_LOCALE, resultId)) ;
                sendToAllWithLocale(resultMessageEn, resultMessageVi, resultMessageZh);
                
                SFSObject msg = getStatusLoseOrWinUserMessage(user, players[seatNum].getReasonId());
                sendUserMessage(msg, user);
                sendUserMessage(msg, getOwner());
            } catch (Exception ex) {
                this.log.error("BJ xet bai error", ex);
            }
        } catch (Exception e) {
            this.log.error("BJ XetBai got exception: r", e);
        }
    }
     /**
     * Xu ly trong truong hop user lat bai gui len server.
     *
     * @param player nguoi gui.
     * @param cards nhung con bai gui len. Mac dinh luon la 3 con. <br> Neu con
     * nao chua lat thi co gia tri -1.
     */
    private void processLatBai(User player) {
        try {
            if(player ==null){
                return;
            }
            int seat= getSeatNumber(getIdDBOfUser(player));
            if(seat==-1){
                return;
            }
            this.log.debug("processLatBai User " + player.getName());
            SFSObject message = this.messageFactory.getProcessLatBaiMessage(this.players[seat],getIdDBOfUser(player));
            sendAllUserMessage(message);
        } catch (Exception e) {
            this.log.error("BJ proccessLatBai room erro: ", e);
        }
    }
     /**
     * Cong tien cho nhung user thang trong ban.
     */
    private void congTien(int seatNum, int seatOwner) {
        try {
            User user = getUser(seatNum);
            this.log.debug("congTien seat " + seatNum);
            if (user != null) {
                this.log.debug("user " + user.getName());
            }
            if (user != null && players[seatNum].isPlaying()) {
                players[seatNum].setPlaying(false);
                BigDecimal money = getUpdateMoney(user);
                this.log.debug("congTien seatNum " + seatNum + " " + " inGameMoney " + bJMoneyBetOfUser.getInGameMoney() + " " + user.getName() + " " + money);
                if (bJMoneyBetOfUser.getInGameMoney().compareTo(money)< 0) {
                    this.log.error("congTien error seatNum " + seatNum + " " + " inGameMoney " + bJMoneyBetOfUser.getInGameMoney() + " " + user.getName() + " " + money);
                    money = BigDecimal.ZERO;
                }
                this.log.debug("money " + money);
                if (seatNum != seatOwner && money.signum() > 0) {
                    bJMoneyBetOfUser.addInGameMoney(money.negate()); 
                    updateMoney(user, money, players[seatNum].getReasonId());
                }
            }
        } catch (Exception e) {
            this.log.error("BJ ccongTien erro:", e);
        }
    }

     /**
     * Gọi hàm này khi tính tiền khi thắng,thua, hòa
     *
     * @param user
     * @return
     */
    public BigDecimal getUpdateMoney(User user) {
        if(user==null){
            return BigDecimal.ZERO;
        }
        String idDBUser= getIdDBOfUser(user);
        BigDecimal money;
        BlackJackPlayer player = players[getSeatNumber(idDBUser)];

        if (player == null) {
            return BigDecimal.ZERO;
        }

        money = BigDecimal.ZERO;
        if (player.getStatus() == BlackJackPlayer.HOA_STATUS) {
            money = bJMoneyBetOfUser.updateMoney(getBettingMoney(idDBUser));
        } else if (player.getStatus() == BlackJackPlayer.THANG_STATUS) {
            money = Utils.add(getBettingMoney(idDBUser), getBettingMoney(idDBUser));
            money = bJMoneyBetOfUser.updateMoney(money);
        }
        return money;
    }
     /**
     * chuyển lượt cho người kế, nếu người kế là chủ bàn thì kết thúc.
     *
     * @param player
     */
    private void go2NextPlayer(User player) {
        try {
            if (player != null && Utils.isEqual(whoMove, player)) {
                if (player != this.getOwner()) {
                    nextMove = nextPlayer(player);
                    int seat = getSeatNumber(getIdDBOfUser(nextMove));
                    if (seat >= 0 && !players[seat].isPlaying()) {//nếu vô ngang hoặc đã lật bài xong rồi thì qua tiếp người nữa
                        nextMove = nextPlayer(nextMove);
                    }
                } else {
                    stopGame();
                }
                if (nextMove != null) {
                    nextTurn();
                }
            }
        } catch (Exception e) {
            this.log.error("BJ go2NextPlayer() erro:", e);
        }
    }
     /**
     * kiểm tra xem trong bàn chơi còn người nào ko phải chủ bàn chưa được tính
     * tiền
     *
     * @return
     */
    private boolean checkPlaying() {
        try {
            for (User user: this.getPlayersList()) {
                int seat= getSeatNumber(user);
                if(seat==-1 || Utils.isEqual(user, this.getOwner())){
                    continue;
                }
                if (players[seat].isPlaying()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            this.log.error("BJ checkPlaying() error: ", e);
            return false;
        }
    }
    /**
     * Hàm này được gọi để update tiền cho nhà cái khi kết thúc ván
     */
    private void caculateMoneyforOnwer(int nOwnerSeat,User owner) {
        if (nOwnerSeat < 0) {
            return;
        }

        BlackJackPlayer player = players[nOwnerSeat];
        BigDecimal money = bJMoneyBetOfUser.getInGameMoney();
        if(!player.isPlaying()){
            return;
        }
        String idDBOwner=getIdDBOfUser(owner);
        BigDecimal doubleBetting = Utils.add(getBettingMoney(idDBOwner), getBettingMoney(idDBOwner));
        //tiền nhận được phải nhỏ hơn tổng số tiền tối đa ăn được
        if (money.compareTo(doubleBetting) > 0) {
            this.log.debug("congTien error seatNum " + nOwnerSeat + " " + " inGameMoney " + doubleBetting.doubleValue() + " " + owner.getName() + " " + money);
            money = BigDecimal.ZERO;
        }
        // update money và status cho nhà cái
        if (money .compareTo(getBettingMoney(idDBOwner)) == 0) { //hòa
            player.setStatus(BlackJackPlayer.HOA_STATUS);
            updateMoney(owner, money, CommonMoneyReasonUtils.HOA);
        } else if (money.compareTo(getBettingMoney(idDBOwner)) > 0) {//thắng
            player.setStatus(BlackJackPlayer.THANG_STATUS);
            updateMoney(owner, money, CommonMoneyReasonUtils.THANG);

        } else {//thua
            player.setStatus(BlackJackPlayer.THUA_STATUS);
            /**
             * vì khi money==0 thì updateMoney() không thể ghi log nên ghi log
             * chổ này
             */
            if (money.signum() == 0) {
                updateLogGameForUser(owner,CommonMoneyReasonUtils.THUA, players[nOwnerSeat].card2List());
            }
            updateMoney(owner, money, CommonMoneyReasonUtils.THUA);
        }
        
        player.setReason();

        if (money.signum() > 0) {
            bJMoneyBetOfUser.addInGameMoney(money.negate());
        }
        //gửi status cua onwer tới những user trong bàn
        try {
            int resultId = player.getResult();
            SFSObject resultMessageVi =this.messageFactory.getSendResultMessage(player,idDBOwner,player.getResultString(GlobalsUtil.VIETNAMESE_LOCALE, resultId));
            SFSObject resultMessageEn =this.messageFactory.getSendResultMessage(player,idDBOwner,player.getResultString(GlobalsUtil.ENGLISH_LOCALE, resultId));
            SFSObject resultMessageZh =this.messageFactory.getSendResultMessage(player,idDBOwner,player.getResultString(GlobalsUtil.CHINESE_LOCALE, resultId));
            sendToAllWithLocale(resultMessageEn, resultMessageVi, resultMessageZh);
        } catch (Exception ex) {
            this.log.error("BJ xet bai error", ex);
        }
    }
    /**
     * Hàm này được gọi để update tiền cho nhà dân khi kết thúc ván
     *
     * @param seatNum
     */
    private void caculateMoneyForUser(int seatNum) {
        try {
            if (seatNum == -1) {
                return;
            }
            if (this.getOwner()== null ||seatNum == getSeatNumber(getIdDBOfUser(this.getOwner()))) {
                return;
            }
            if (!players[seatNum].isPlaying()) {
                return;
            }
            
            User user = getUser(seatNum);
            BlackJackPlayer player = players[seatNum];
            this.log.debug("Kết thúc ván, xét bài seatNum " + seatNum + " " + user.getName() + " " + player.getResultString(getLocaleOfUser(user), player.getResult()) + " " + players[seatNum].getStrCard());
            player.checkStatusWithCai(players[getSeatNumber(getIdDBOfUser(this.getOwner()))].getResult());
            processLatBai(user);
            //cộng tiền thắng cho nhà dân
            BigDecimal money = getUpdateMoney(user);
            player.setReason();
            if (bJMoneyBetOfUser.getInGameMoney().compareTo(money) < 0) {
                this.log.debug("congTien error seatNum " + seatNum + " " + " inGameMoney " + bJMoneyBetOfUser.getInGameMoney().doubleValue() + " " + user.getName() + " " + money);
                money = BigDecimal.ZERO;
            }
            if (money.signum() > 0) {
                try {
                    int reasonId = player.getReasonId();
                    updateMoney(user, money, reasonId);
                    bJMoneyBetOfUser.addInGameMoney(money.negate());
                } catch (Exception e) {
                   this.log.error("update money erro", e);
                }
            }else{
                updateLogGameForUser(user,CommonMoneyReasonUtils.THUA,players[seatNum].card2List());
            }
            
            sendResultWinOrLose(seatNum);
        } catch (Exception e) {
            this.log.error("BJ caculateMoneyForUs() erro:", e);
        }
    }
    
    private void sendResultWinOrLose(int seat){
        try {
            User user = getUser(seat);
            BlackJackPlayer player = players[seat];
            //gửi kết quả thắng thua về cho user
            int reasonIdOwner = CommonMoneyReasonUtils.HOA;
            if (player.getReasonId() == CommonMoneyReasonUtils.THANG) {
                reasonIdOwner = CommonMoneyReasonUtils.THUA;
            }
            if (player.getReasonId() == CommonMoneyReasonUtils.THUA) {
                reasonIdOwner = CommonMoneyReasonUtils.THANG;
            }
           
            SFSObject msg = getStatusLoseOrWinUserMessage(user, player.getReasonId());
            sendUserMessage(msg, user);
            sendUserMessage(msg, getOwner());
            
            msg = getStatusLoseOrWinUserMessage(getOwner(), reasonIdOwner);
            sendUserMessage(msg, user);
        } catch (Exception e) {
            this.log.error("BJ sendResultWinOrLose() erro:", e);
        }
        
    }
    
    /**
     * Chọn chủ bàn là người có số tiền cao nhất và reset lại mức cược của nhà
     * con về mức cược của bàn
     */
    private void processChoseNewOwner() {
        User userOwner = null;
        BigDecimal maxMoney = getMoney();
        //chọn user có số tiền lớn nhất
        for (User user: this.getPlayersList()) {
            int nseat = getSeatNumber(user);
            if (user == null || nseat == -1) {
                continue;
            }

            if (getMoneyFromUser(user).compareTo(maxMoney) > 0) {
                userOwner = user;
                maxMoney = getMoneyFromUser(user);
            }
        }
        BigDecimal countPlayer = new BigDecimal(String.valueOf(this.getPlayersList().size() - 1));
        BigDecimal moneyCheck = Utils.multiply(getMoney(), countPlayer);
        if (userOwner == null || (getMoneyFromUser(userOwner).compareTo(moneyCheck) < 0)) {
            //kick hết tất cả user ra khỏi bàn
             for (User user: this.getPlayersList()) {
                String infor = String.format(BlackJackLanguage.getMessage(BlackJackLanguage.OWNER_NOT_ENOGH_MONEY,getLocaleOfUser(user)), getCurrencyName(getLocaleOfUser(user)));
                kickUser(user, infor);
            }
            return;
        }
        //xet lại owner
        setOwner(userOwner);
        sendDialogInforNewOwner();
        resetMoneyBoard();
        resetAllPlayerBet();
    }
    /**
     * Xét chủ bàn mới trong trường hợp chủ bàn trước đó không đủ tiền trả cho
     * nhà con
     */
    public void sendDialogInforNewOwner() {
        try {
            for (User user: this.getPlayersList()) {
                String info=String.format(BlackJackLanguage.getMessage(BlackJackLanguage.SET_OWNER,getLocaleOfUser(user)),getUserName(this.getOwner()));
                sendToastMessage(info,user,3);
            }
        } catch (Exception e) {
            this.log.error("sendBoardInforMessage", e);
        }
    }
     /**
     * Reset tiền cược của nhà con về mức cược tối thiểu của bàn
     */
    private void resetMoneyBoard() {
        for (User user: this.getPlayersList()) {
            // set lại tiền cược của user trở về mức thấp nhất
            if (!Utils.isEqual(user, this.getOwner())) {
                try {
                    setBettingMoney(user, getMoney());
                    SFSObject m = this.messageFactory.getBetMoneyMessage(user,getMoney().doubleValue());
                    sendAllUserMessage(m);
                } catch (Exception ex) {
                    this.log.error("resetMoneyBoard() erro:", ex);
                }
            }
        }
    }
    /**
     * Reset trạng thái quickplay=false cho tất cả user
     */
    private void resetAllPlayerBet() {
        for (int i = 0; i < players.length; i++) {
            User user = getUser(i);
            if (user == null) {
                continue;
            }
            players[i].setBetted(false);
        }
    }
    /**
     * Xử lý cho trường hợp mover thoát game
     *
     * @param user
     * @param seat
     */
    private void processMoverLeave(int seat, boolean isOwnerLeave) {
        if (!isOwnerLeave) {
            nextMove = nextPlayerFromSeatNumber(seat);
            int nSeat = getSeatNumber(getIdDBOfUser(nextMove));
            if (nSeat >= 0 && !players[nSeat].isPlaying()) {       //nếu vô ngang hoặc đã lật bài xong rồi thì qua tiếp người nữa
                nextMove = nextPlayer(nextMove);
            }
        } else {
            stopGame();
        }
        if (nextMove != null) {
            nextTurn();
        }
    }
     /**
     * Chuyển tới lượt user kế tiếp dựa vào chổ ngồi
     *
     * @param sNum
     * @return
     */
    public User nextPlayerFromSeatNumber(int sNum) {
        try {
            for (int i = 0; i < players.length; i++) {
                sNum = (sNum + 1) % players.length;
                User p = getUser(sNum);
                if (p != null) {
                    if (players[sNum].isPlaying()) {
                        setCurrentPlayer(p);
                        return p;
                    }
                }
            }
        } catch (Exception e) {
            this.log.error("BJ nextPlayerFromSeatNumber() erro:", e);
        }
        return null;
    }
    
      /**
     * xử lý thắng thua và cộng tiền cho dân và nhà cái khi kết thúc ván
     */
    private void stopGameInMoney() {
        try {
            //User đặt cược không thành công thì sẽ không tính tiền chổ này
            if (!isBetted) {
                throw new Exception("not betted yet");
            }
            String idDBOwner = getIdDBOfUser(this.getOwner());
            /**
             * khi nhà cai rời bàn khi chơi thì không ghi log và tính tiền chổ
             * này vì đã tính trong hàm leave()
             *
             */
            if (this.getOwner()== null || !players[getSeatNumber(idDBOwner)].isPlaying()) {
                return;
            }
            int seatOwner = getSeatNumber(idDBOwner);
            //xét bài và cộng tiền cho nhà dân
            for (int i = 0; i < players.length; i++) {
                if (i != seatOwner && players[i].isPlaying()) {
                    caculateMoneyForUser(i);
                }
            }
            //xét status và công tiền cho nhà cái
            caculateMoneyforOnwer(getSeatNumber(idDBOwner),this.getOwner());
        } catch (Exception e) {
            this.log.error("stopGameInMoney() erro: ", e);
        }finally{
             /**
             * gui message stop game về sau update money vì client gui lên
             * request min-max mức cược của user sau khi stop game nếu gửi về
             * trước thì tiền của user ván này chưa được update
             */
            processStopGame();
        }
    }
    /**
     * Xu ly trong truong hop ket thuc 1 van choi.<br> So bai thang cai voi tung
     * thang dan.
     */
    private void processStopGame() {
        try {
            SFSObject msg = this.messageFactory.getStopGameMessage(players,GlobalsUtil.VIETNAMESE_LOCALE);
            SFSObject msgEn = this.messageFactory.getStopGameMessage(players,GlobalsUtil.ENGLISH_LOCALE);
            SFSObject msgZh = this.messageFactory.getStopGameMessage(players,GlobalsUtil.CHINESE_LOCALE);
            sendToAllWithLocale(msgEn,msg, msgZh);
        } catch (Exception ex) {
            this.log.error("BJ processStopGame() erro:", ex);
        }
    }
      /**
     * Xu ly trong truong hop join board thanh cong. <br> Neu dang choi hien
     * thong tin nhung nguoi dang cho va bai cua no. Phai chac chan la user nay
     * co trong board bang cach kiem tra ham user.getBoard() != null.
     *
     * @param user nguoi moi vua join.
     */
    private boolean processJoinBoardSuccess(User user) {
        // ktra da join board thanh cong hay chua.
        // Bang ham user.getBoard() != null
        try {
            int nSeat = getSeatNumber(user);
            if(nSeat==-1){
                return false;
            }
            if (isPlaying()) {
                //goi thong tin cua no cho tat ca
                SFSObject msg = this.messageFactory.getPlayingMessage(user);
                sendUserMessage(msg, user);
            }
            if (nSeat != -1) {
                if (!players[nSeat].isPlaying()) {
                    // set tien mac dinh cho no.
                    if (!processSetMoney(user, getMoney(), 1)) {
                        String infor = String.format(BlackJackLanguage.getMessage(BlackJackLanguage.OWNER_NOT_ENOGH_MONEY,getLocaleOfUser(user)), getCurrencyName(getLocaleOfUser(user)));
                        addToWaitingUserList(user, infor);
                        return false;
                    } 
                }
            }
            processCountDownStartGame();
            return true;
        } catch (Exception e) {
            this.log.error("BJ processJoinBoardSuccess() error:", e);
        }
        return false;
    }

    @Override
    public void nextTurn() {
        try {
            SFSObject ob= this.messageFactory.getNextTurnMessage(getIdDBOfUser(nextMove), getPlayingTime()/1000);
            sendAllUserMessage(ob);
            whoMove = nextMove;
            setCurrentPlayer(whoMove);
             //neu la chu ban thi lat bai luon
            if (Utils.isEqual(whoMove, this.getOwner())) {
                sendResult(this.getOwner(), true);
                processLatBai(this.getOwner());     
            }
            setCurrentMoveTime();
        } catch (Exception ex) {
            this.log.error("BJ nextTurn() erro:", ex);
        }
    }
     /**
     * xử lý ngừng rút bài của 1 user.
     *
     * @param user
     */
    private void standGetCard(User user) {
        try {
            if (Utils.isEqual(user, this.getOwner())) {
                stopGame();
            } else {      //neu la chu ban thi ket thuc van
                go2NextPlayer(user);
            }
        } catch (Exception e) {
            this.log.error("BJ standGetCard() erro:", e);
        }
    }

    @Override
    protected int getAdvMinPoint() {
        if(isAdvantageRatioByGroupID()){
            return BlackJackConfig.getInstance().getMinPoint();
        }
        return super.getAdvMinPoint(); 
    }
    
    

    /**
     * Xử lý rút bài cho user và bot
     * @return 
     */
    private Card processGetCardForBot(boolean isBot, int advantageRatio, BlackJackPlayer player){
        Card  card = null;
        if(isOpenBot){
            //không là bot
            if(!isBot){
                card = botDealCards.get(0);
                botDealCards.remove(card);
                return card;
            }
            int percent = random.nextInt(100);
            boolean bigger = false;
            if (percent < advantageRatio) {
                bigger = true;
            }

            if (bigger) {
                int minPoint = getAdvMinPoint();
                //danh sách card của player
                List<Card> playerCards = new ArrayList<>(player.getListCards());
                Card minCard = null;//card khi add vao se nhỏ hon minPoint
                
                for (Card c : botDealCards) {
                    playerCards.add(c);
                    int result = player.getResult(playerCards);
                    /**
                     * Đủ min point và không quắc
                     */
                    if(result >= minPoint && result != BlackJackPlayer.QUAC){
                        card = c;
                        botDealCards.remove(c);
                        return card;
                    }
                    if(minCard == null && result < minPoint && result > BlackJackPlayer.QUAC){
                        minCard = c;
                    }
                    playerCards.remove(c);
                }
                //add min cards
                if(minCard!=null){
                    botDealCards.remove(minCard);
                    return minCard;
                }
            } 
            //không có bài thỏa điền kiện va khong nằm trong % loi thế thì lấy ngẩu nhiên
            card = botDealCards.get(0);
            botDealCards.remove(card);
        }else{
            card = cardSet.dealCard();
        }
        
        return card;
    }
    
    /**
     * Xử lý logic cho việc rút bài:<br> - Kiểm tra nếu user đang đủ 21 nút, ngũ
     * linh hoặc quắc thì xử lý như ngừng rút. Nếu là dân: <br> - Gửi con bài
     * rút về cho user. Những người chơi khác sẽ nhận được 1 con bài úp<br> -
     * Gửi về thông tin kết quả cho user đó. Nếu là cái: <br> - Gửi thông tin
     * bài rút và kết quả về cho tất cả user trong bàn.
     *
     * @param user
     * @throws IOException
     */
    private void getCard(User user){
        try {
            if(user==null){
                return;
            }
            String idDBUser = getIdDBOfUser(user);
            int seat = getSeatNumber(idDBUser);
            if (!avaiableGetCard(seat)) {
                standGetCard(user);
                return;
            }
            int advantageRatio = getAdvRatio();
            if(isAdvantageRatioByGroupID()){
                advantageRatio = getAdvantageRatio(user);
            }
            
            Card card = processGetCardForBot(isBot(user), advantageRatio, players[seat]);
            players[seat].receivedCard(card);
            if (Utils.isEqual(user, this.getOwner())) {
                sendResult(user, true);      //tự show tự sướng
                if (!avaiableGetCard(seat)) {
                    stopGame();
                    return;
                } else {
                    processLatBai(user);
                }
            } else {
                sendResult(user, false);
                
                SFSObject msg = this.messageFactory.getProcessGetCardMessage(idDBUser,(byte) -1);
                SFSObject msgMe = this.messageFactory.getProcessGetCardMessage(idDBUser, card.getId());
                
                sendUserMessage(msgMe,user);
              
                //procces rut bai.
                for (User u : this.getAllPlayers()) {
                    if (Utils.isEqual(u, user)) {
                        continue;
                    }
                    sendUserMessage(msg, u);
                }
                
                sendAllViewer(msg);
            }
            //cộng thêm 5s cho mỗi lần rut
            addCurrentMoveTime(5000);
            this.log.debug((System.currentTimeMillis()-getCurrentMoveTime())/1000+"reset time limit: "+ getTimeRemain());
            SFSObject msgTimeLimit = this.messageFactory.getNextTurnMessage(getIdDBOfUser(getCurrentPlayer()), getTimeRemain());
             
            sendAllUserMessage(msgTimeLimit);
//            sendUserMessage(msgTimeLimit,user);
            

        } catch (Exception e) {
            this.log.error("BJ getCard() erro:", e);
        }
    }
    private boolean avaiableGetCard(int seat) {
        try {
            if (seat < 0) {
                return false;
            }
            if (players[seat].getResult() >= BlackJackPlayer.WIN_POINT || players[seat].getResult() == BlackJackPlayer.QUAC
                    || players[seat].getListCards().size() >= DEFAULT_MAX_CARD_NUMBER) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            this.log.error("BJ avaiableGetCard() error", e);
        }
        return true;
    }
    /**
     * Khi tạo bàn set mức cược tối thiểu nào thì mức cược tối đa cho nhà con là
     * gấp 10 lần mức tối thiểu đó(dành cho room Vip). VD : Tạo bàn 500k VIP thì
     * mức cược từ 500k-5M
     *
     * @param user
     */
    private BigDecimal checkUserSetMoney(User user, BigDecimal money) {
        if (Utils.isEqual(user, this.getOwner())) {
            return money;
        }
        BigDecimal maxVip =  Utils.multiply(new BigDecimal(String.valueOf(getMaxBet())), getMoney());
        if (money.compareTo(maxVip) > 0) {
            money = getMoneyFromUser(user).min(maxVip);
            String err = String.format(BlackJackLanguage.getMessage(BlackJackLanguage.BET_MONEY, getLocaleOfUser(user)), getMaxBet());
            sendToastMessage(err, user, 3);
        }
        return money;
    }
    /**
     * Mức cược tối da user có thể đặt trong board
     * dùng de kiem tra lúc user đặt cược
     * @param user
     * @return 
     */
    @Override
    protected BigDecimal getMaxBoardValue(User user) {
        BigDecimal max1 = Utils.multiply(new BigDecimal(String.valueOf(getMaxBet())), getMoney());
        return  max1.min(getMoneyFromUser(user));
    }
     /**
     * Chuyển tới lượt user kế tiếp dựa vào user
     *
     * @param player
     * @return
     */
    @Override
    public User nextPlayer(User player) {
        try {
            if(player==null){
                return null;
            }
            int sNum = getSeatNumber(getIdDBOfUser(player));
            for (int i = 0; i < players.length; i++) {
                sNum = (sNum + 1) % players.length;
                User p = getUser(sNum);
                if (p != null) {
                    if (players[sNum].isPlaying()) {
                        setCurrentPlayer(p);
                        return p;
                    }
                }
            }
        } catch (Exception e) {
            this.log.error("BJ nextPlayer() error:", e);
        }
        return null;
    }
    
    private void sendMessageReturn(User user) {
        try {
            int seat =getSeatNumber(getIdDBOfUser(user));
            if(seat==-1){
                return;
            }
            SFSObject ob= this.messageFactory.getOnReturnMessage(seat,getLocaleOfUser(user));
            sendUserMessage(ob, user);
        } catch (Exception ex) {
            this.log.error("sendMessageReturn error: ", ex);
        }
    }

    @Override
    public void onReturnGame(User user) {
        super.onReturnGame(user);
        if (isPlaying()) {
            sendMessageReturn(user);
        }
    }

    @Override
    public User getCurrentPlayer() {
        return super.getCurrentPlayer(); 
    }

    @Override
    public User getUser(int seat) {
        return super.getUser(seat);
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user);
    }

    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(BlackJackConfig.getInstance().getMaxViewer());
    } 

    @Override
    public int getTimeRemain() {
        return super.getTimeRemain(); 
    }
    @Override
    protected void waiterBuyStack(User user) {
        super.waiterBuyStack(user);
        // set tien mac dinh cho no.
        if (!processSetMoney(user, getMoney(), 1)) {
            String infor = String.format(BlackJackLanguage.getMessage(BlackJackLanguage.OWNER_NOT_ENOGH_MONEY,getLocaleOfUser(user)), getCurrencyName(getLocaleOfUser(user)));
            addToWaitingUserList(user, infor);
        }
    }   

    @Override
    protected byte getServiceId() {
        return Service.BLACKJACK;
    }
    
    @Override
    protected int getFreeSeat() {
        BigDecimal tienDat = checkTienCai(getOwner());
        BigDecimal tienCon =Utils.subtract(getMoneyFromUser(this.getOwner()), tienDat) ; //tiền của nhà cái sau khi trừ hết tiền cược
        if (tienCon.signum() < 0 || tienCon.compareTo(getMoney()) < 0) {
            return 0;
        }
        return super.getFreeSeat();
    }
    
    /**
     * Update kết quả thắng - thua - hòa của game blackJack
     *
     * @param reasonId
     */
    private void updateBlackJackResult(User user, int reasonId) {
         updateAchievement(user, reasonId);
    }

}
