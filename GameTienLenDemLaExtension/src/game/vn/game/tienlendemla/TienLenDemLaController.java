/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlendemla;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.GameExtension;
import game.vn.common.card.BotCards;
import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.event.EventManager;
import game.vn.common.object.MoneyManagement;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.game.tienlendemla.language.TienLenLanguage;
import game.vn.game.tienlendemla.message.MessageFactory;
import game.vn.game.tienlendemla.object.TienLenPlayer;
import game.vn.game.tienlendemla.utils.BoardMoneyUtil;
import game.vn.game.tienlendemla.utils.TienLenCardUtils;
import game.vn.game.tienlendemla.utils.TienLenDeck;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import org.slf4j.Logger;

/**
 *
 * @author hoanghh
 */
public class TienLenDemLaController extends GameController {

    public static final int DEFAULT_NUMBER_TIENLEN_CARD = 13;
    /**
     * bộ bài cho bàn này.
     */
    private final transient CardSet cardSet;
    //bài đánh của lượt gần nhất
    private Card[] cardMove;
    /**
     * danh sách nhóm bài của thằng đánh đầu tiên , để check đền bài
     */
    private final List<List<Card>> firstCardMove;
    /**
     * List cua nhung con heo duoc danh trong 1 van.
     */
    private final List<Card> eatHeo;
    /**
     * loại bài được đánh
     */
    private int typeMove;
    //về nhất, nhì, ba, bét
    private int win;
    // tiền thưởng, tiền phạt
    private BigDecimal penalty = BigDecimal.ZERO;
    private BigDecimal penaltyTotal = BigDecimal.ZERO;
    private String penaltyDes_VI, penaltyDes_EN, penaltyDes_ZH, penaltyTotalDes_VI, penaltyTotalDes_EN, penaltyTotalDes_ZH ;
    private User winner = null;
    private int typeBai;
    TienLenPlayer[] players;
    BotCards[] botCards;
    /**
     * người chơi hiện tại
     */
    private User playerMove, playerBeginNewRound, playerBiChat, moveFirst;
    private final Logger log;
     /**
     * Xử lý tiền phạt
     */
    private final BoardMoneyUtil boardMoney;
    private static Random random = new Random();
    private final MoneyManagement moneyManagement;
    
    public TienLenDemLaController(Room room, GameExtension gameEx) {
        super(room, gameEx);
        players = new TienLenPlayer[4];
        botCards = new BotCards[4];
        for (int i = 0; i< players.length;i++){
            players[i] = new TienLenPlayer();
            botCards[i] = new BotCards();
        }
        eatHeo = new ArrayList<>();
        firstCardMove = new ArrayList();
        cardSet = new CardSet();
        log = game.getLogger();
        loadTimeConfig();
        boardMoney= new BoardMoneyUtil();
        moneyManagement = new MoneyManagement();
    }

    private void loadTimeConfig(){
        try {
            RoomVariable rv = new SFSRoomVariable("turnTime", getPlayingTime()/1000);
            this.room.setVariable(rv);
        } catch (SFSVariableException ex) {
            log.error("set turnTime error", ex);
        }
    }

    @Override
    public synchronized void leave(User player) { 
        int seatNum = getSeatNumber(player);
        this.log.debug("LEAVE " + player.getName());
        try {
            boolean isInTurn = isInturn(player);
            int nInTurn = countInturnPlayer();
            User nextPlayer = nextPlayer(player);
            super.leave(player);

            if (seatNum != -1 && isPlaying() && isInTurn) {
                // neu nguoi thoat la nguoi di cuoi, thi chuyen nguoi di cuoi
                if (Utils.isEqual(player, playerBeginNewRound)) {
                    playerBeginNewRound = nextPlayer;
                }

                // neu nguoi thoat dang giu luot choi thi chuyen luot
                if (Utils.isEqual(player, getCurrentPlayer())) {
                    skip(player,seatNum);
                }
                if (nInTurn <= 1) {
                    stopGame();
                } else {

                    penaltyDes_EN = "";
                    penaltyDes_VI = "";
                    penaltyDes_ZH = "";
                    xetThuiBai(players[seatNum].getCards());
                    // phat 1 van nhat
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.LEAVE_ROOM, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.LEAVE_ROOM, GlobalsUtil.ENGLISH_LOCALE));
                    addPenaltyDesPharseZh(TienLenLanguage.getMessage(TienLenLanguage.LEAVE_ROOM, GlobalsUtil.CHINESE_LOCALE));
                    if(players[seatNum].getCards().size() == DEFAULT_NUMBER_TIENLEN_CARD){
                          penalty = Utils.add(penalty, boardMoney.getCongMoney());
                    }else{
                        BigDecimal moneyCards = Utils.multiply(new BigDecimal(String.valueOf(players[seatNum].getCards().size())), getMoney());
                        penalty = Utils.add(penalty, moneyCards);
                    }
                    penalty =  getMoneyFromUser(player).min(penalty);
                    if (penalty.signum() > 0) {
                        //chỉ lưu thêm thuế vào db thôi vì tiền phạt này ko cộng cho user
                        updateMoney2WithLocale(player, penalty.negate(), penaltyDes_VI, penaltyDes_EN, penaltyDes_ZH, CommonMoneyReasonUtils.BO_CUOC,BigDecimal.ZERO, players[seatNum].cardsToList());
                        boardMoney.addMoneyPot(penalty);
                        penalty= BigDecimal.ZERO;
                    }
                    
                    if (nInTurn == 2) {
                        playerFinishWhenLeaveGame(nextPlayer);
                        playerMove = nextPlayer;
                        setCurrentPlayer(player);
                        stopGame();
                    }
                }
            }

        } catch (Exception e) {
            log.error("Tienlen leave game error:", e);
        }finally{
            forceLogoutUser(player);
        }
        //nếu chỉ còn 1 người chơi thì bò quyền đánh đầu tiên của winner
        if (Utils.isEqual(moveFirst, player) || getPlayersList().size() <= 1) {
            moveFirst = null;
        }
    }
    
    private void playerFinishWhenLeaveGame(User player) {
        try {
            int seat = getSeatNumber(player);
            if (player == null || seat <= -1) {
                return;
            }

            BigDecimal bonusMoney[];
            bonusMoney = setMoneyMinusTax(boardMoney.getMoneyPot(), getTax());
            updateMoney2WithLocale(player, bonusMoney[MONEY], "", "", "", CommonMoneyReasonUtils.TOI_1, bonusMoney[TAX] ,null);
            boardMoney.setMoneyPot(BigDecimal.ZERO);

            players[seat].setWin(win);
            players[seat].addPenalty(bonusMoney[MONEY]);
            winner = player;
            SFSObject m = MessageFactory.getINSTANCE().createMessageFinishGame(getIdDBOfUser(player), (byte) win);
            sendAllUserMessage(m);
            win++;
            sendRankingData(player, bonusMoney[TAX].doubleValue(), 1);
            updateAchievement(player, CommonMoneyReasonUtils.THANG);
        } catch (Exception e) {
            log.error("Tienlen finishWhenLeaveGame error:", e);
        }
    }

    @Override
    public synchronized boolean join(User user, String pwd) {
        try {
            if (!super.join(user, pwd)) {
                return false;
            }
            int seatNum = getSeatNumber(user);
            if (seatNum != -1) {
                //add player into game list player
                players[seatNum] = new TienLenPlayer();
                players[seatNum].resetCards();
            }
            this.log.debug("players size =" + getPlayersList());
            if (isPlaying()) {
                sendMessagePlaying(user);
            }
            processCountDownStartGame();
            return true;
        } catch (Exception e) {
            log.error("error join tien len ", e);
        }
        return false;
    }
    
    @Override
    public synchronized boolean joinShuffle(User user) {
        try {
            if (!super.joinShuffle(user)) {
                return false;
            }
            int seatNum = getSeatNumber(user);
            if (seatNum != -1) {
                //add player into game list player
                players[seatNum] = new TienLenPlayer();
                players[seatNum].resetCards();
            }

            if (isPlaying()) {
                sendMessagePlaying(user);
            }
            processCountDownStartGame();
            user.removeProperty(UserInforPropertiesKey.ON_SHUFFLE);
            return true;
        } catch (Exception e) {
            log.error("joinShuffle", e);
        }
        return false;
    }

    @Override
    public void onReturnGame(User user) {
        try {
            super.onReturnGame(user);
            // nếu chưa start ván thì gui ve thoi gian countdown
            if (!isPlaying()) {
                return;
            }
            if (Utils.isEqual(user, playerMove)) {
                playerMove = user;
            }
            int seatNum = getSeatNumber(user);
            if (seatNum != -1) {
                SFSObject returnGame = MessageFactory.getINSTANCE().createReturnMessage(this,getIdDBOfUser(playerMove),
                        cardMove, getIdDBOfUser(getCurrentPlayer()), players[seatNum].getCards(), 
                        (int)getTimeRemain(), Utils.isEqual(user, playerBeginNewRound), players, getPlayingTime()/1000);

                sendUserMessage(returnGame, user);
            }
        } catch (Exception e) {
            log.error( "onReturnGame error:", e);
        }
    }

    @Override
    public void processMessage(User player, ISFSObject sfsObj) {
        super.processMessage(player, sfsObj);
        try {
            int actionCode = sfsObj.getInt(SFSKey.ACTION_INGAME);
            int sNum = getSeatNumber(player);
            if (sNum == -1) {
                return; // khong con o trong ban
            }
            if (!Utils.isEqual(player, getCurrentPlayer()) || !isPlaying()) {
                return;
            }
            BigDecimal moneyOfUser = getMoneyFromUser(player);
            switch (actionCode) {
                case SFSAction.MOVE:
                    List<Short> cardIds = new ArrayList(sfsObj.getShortArray("cards"));
                    if (cardIds != null && cardIds.size() > 0) {
                        Card[] cards = new Card[cardIds.size()];

                        for (int i = 0; i < cardIds.size(); i++) {
                            cards[i] = CardSet.getCard(cardIds.get(i).byteValue());
                            if (!players[sNum].getCards().contains(cards[i])) {// check exist
                                return;
                            }
                        }
                        move(player, cards);
                        addBoardDetail(player, CommonMoneyReasonUtils.MOVE, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0, 0, cardIds);
                    }
                    break;
                case SFSAction.SKIP:
                    skip(player, getSeatNumber(player));
                    addBoardDetail(player, CommonMoneyReasonUtils.SKIP, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0, 0, null);
                    break;
            }
        } catch (Exception e) {
            log.error( "processMessage() error:", e);
        }
    }
    
    /**
     * Chức năng tới nhất ăn hết
     */
    private void processWinAll(int winnerSeatNum, boolean isChatHeo) {
        try {
            User p = getUser(winnerSeatNum);
            String idDBP= getIdDBOfUser(p);
            //xet cóng 3 nhà
            if (players[winnerSeatNum].getCards().isEmpty() && isCongAll()) {
                xetCong3User(p);
                SFSObject m =  MessageFactory.getINSTANCE().createMessageMove(idDBP, getCardsMoveList(),getIdDBOfUser(getCurrentPlayer()),isChatHeo, (byte) players[winnerSeatNum].getCards().size());
                sendAllUserMessage(m);
                stopGame();
                return;
            }
            nextTurn(getSeatNumber(getCurrentPlayer()));
            SFSObject m = MessageFactory.getINSTANCE().createMessageMove(idDBP, getCardsMoveList(), getIdDBOfUser(getCurrentPlayer()), isChatHeo, (byte) players[winnerSeatNum].getCards().size());
            sendAllUserMessage(m);
            // sua lai de cho client khong bi chan danh.
            if (typeMove == -1) {
                for (int i = 0; i < 4; i++) {
                    players[i].setSkipstatus(false);
                }
                SFSObject skipMessage = MessageFactory.getINSTANCE().createMessageSkip(idDBP, getIdDBOfUser(getCurrentPlayer()), true);
                sendUserMessage(skipMessage, p);
            }
            if (players[winnerSeatNum].getCards().isEmpty()) {
               finishWinAll(p); 
            }
        } catch (Exception e) {
            log.error("Tienlen processWinAll error:", e);
        }
    }

    /**
     * User đánh ra lá bài, nhóm bài, kiểm tra có hợp lệ không
     * @param player
     * @param cards : bài đánh ra
     */
    private void move(User player, Card[] cards){
        try {
            int sNum = getSeatNumber(player);
            if (sNum == -1) {
                return; // khong con o trong ban
            }

            Arrays.sort(cards);
            typeBai = TienLenCardUtils.getType(cards);
            if (typeBai == -1) {
                sendToastMessage(TienLenLanguage.getMessage(TienLenLanguage.INVALID_CARD, getLocaleOfUser(player)), player, 2);
                return;
            }
            if (players[sNum].isSkipstatus() && typeBai != TienLenCardUtils.FOUR_PAIR_CONT) {
                sendToastMessage(TienLenLanguage.getMessage(TienLenLanguage.INVALID_CARD, getLocaleOfUser(player)), player, 2);
                return;
            }
            
            if (moveFirst == null) {
                moveFirst = player;
            }
            
            if (checkMove(cards, typeBai)) {
                boolean isChatHeo=false;
                if (penalty.signum() > 0 && playerMove!=null) {
                    playerBiChat = playerMove;
                    penaltyTotal = Utils.add(penaltyTotal, penalty);
                    penaltyTotalDes_VI += penaltyDes_VI;
                    penaltyTotalDes_EN += penaltyDes_EN;
                    penaltyTotalDes_ZH += penaltyDes_ZH;
                    isChatHeo=true;
                }
                // lưu lại bài đánh đầu tiên để check đền bài nếu thằng đánh đầu tới trắng
                if (Utils.isEqual(player, moveFirst)) {
                    firstCardMove.add(Arrays.asList(cards));
                }

                playerMove = player;
                playerBeginNewRound = player;
                players[sNum].setSkipstatus(false);
                cardMove = cards;
                typeMove = typeBai;
                for (int i = 0; i < cards.length; i++) {
                    players[sNum].removeCards(cards[i]);
                }
                processWinAll(sNum, isChatHeo);

            } else {
                sendToastMessage(TienLenLanguage.getMessage(TienLenLanguage.INVALID_CARD, getLocaleOfUser(player)), player, 2);
            }
        } catch (Exception e) {
            log.error( "Tienlen move error:" , e);
        }
    }
    
    private void skip(User player, int nSeat) {
        try {
            if (Utils.isEqual(playerBeginNewRound, player) && Utils.isEqual(playerBeginNewRound, getCurrentPlayer())
                    && playerMove != null && typeMove == TienLenCardUtils.NOTYPE) {

                typeMove = -1;
                if (penaltyTotal.signum() > 0) {
                    chat();
                }
                for (int i = 0; i < 4; i++) {
                    players[i].setSkipstatus(false);
                }
                autoPlay(player);
            } else {
                if (nSeat == -1) {
                    return;
                }
                if (typeMove != -1) {
                    players[nSeat].setSkipstatus(true);
                }
                nextTurn(nSeat);
                if (getCurrentPlayer() == null) {
                    return;
                }
                if (isSkipAll()) {
                    setCurrentPlayer(playerBeginNewRound);
                }
                int seatPlayerBeginNewRound=getSeatNumber(playerBeginNewRound);
                if (players[seatPlayerBeginNewRound].getCards().isEmpty()) {
                    playerBeginNewRound = getCurrentPlayer();
                }
                if (Utils.isEqual(getCurrentPlayer(), playerBeginNewRound)) { // begin new circle
                    typeMove = -1;
                    if (penaltyTotal.signum() > 0) {
                        chat();
                    }
                    for (int i = 0; i < 4; i++) {
                        players[i].setSkipstatus(false);
                    }
                }
                SFSObject m = MessageFactory.getINSTANCE().createMessageSkip(getIdDBOfUser(player),getIdDBOfUser(this.getCurrentPlayer()), typeMove == -1);
                sendAllUserMessage(m);
                
            }
        } catch (Exception e) {
            log.error("Tienlen skip error" , e);
        }
    }
    /**
     * tự động đánh ra nhóm bài khi timeout
     *
     * @param player
     */
    public void autoPlay(User player) {
        try {
            int seat = getSeatNumber(player);
            if (seat < 0) {
                return;
            }
            if (players[seat].getCards().size() > 0) {
                Card[] cards = (Card[]) players[seat].getAutoCards();
                if (cards != null && cards.length > 0) {
                    move(player, cards);
                }
            } else {
                // Sua lai vi co truong hop cong tien lien tuc do func PlayerFinish() duoc goi tu func update cong tien lien tuc
                setInturn(player, false);
                nextTurn(getSeatNumber(getCurrentPlayer()));
                if (getCurrentPlayer() == null) {
                    stopGame();
                } else {
                    playerBeginNewRound = getCurrentPlayer();
                    playerBiChat = null;
                    playerMove = null;
                }
            }
        } catch (Exception ex) {
            log.error( "error when auto play:" , ex);
        }
    }
    
    /**
     * Tìm user đánh đầu tiên random
     */
    private void setMoverFirst() {
         // tim nguoi danh dau tien
        User firstUser = null;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < getPlayersSize(); i++) {
            User p = getUser(i);
            if (p != null) {
              users.add(p);
            }
        }
        firstUser = users.get(random.nextInt(users.size()));
        setCurrentPlayer(firstUser);
        setCurrentMoveTime();
    }
    /**
     * TODO: sửa lại trường hợp hưởng soái, thường bắt người cuối cùng bỏ lượt
     * mới được hưởng soái
     * @param sNum
     * @return 
     */
    public User nextTurn(int sNum) {
        try {
            int currentSeat = sNum;
            setCurrentPlayer(null);
            for (int i = 0; i < getPlayersSize(); i++) {
                sNum = (sNum + 1) % getPlayersSize();
                User p = getUser(sNum);
                if (p != null && isInturn(p)) {
                    this.log.debug("next snum = " + sNum + " u=" + p.getName());
                    if (!players[sNum].isSkipstatus()) {
                        setCurrentPlayer(p);
                        if (sNum == currentSeat) {
                            typeMove = -1;
                        }
                        break;
                    } else {
                        // chuyen cho nguoi huong soai neu ko con ai
                        if (Utils.isEqual(p, playerBeginNewRound)) {
                            setCurrentPlayer(p);
                            if (sNum == currentSeat) {
                                typeMove = -1;
                            }
                        }
                        if (isSkipAll()) {
                            playerMove = p;
                        }
                        // Truong hop 4 doi thong.
                        if (typeMove == TienLenCardUtils.ONE_CARD && cardMove[0].getCardNumber() == 12
                                || typeMove == TienLenCardUtils.PAIR && cardMove[0].getCardNumber() == 12
                                || typeMove == TienLenCardUtils.THREE_PAIR_CONT || typeMove == TienLenCardUtils.FOUR_OF_A_KIND || typeMove == TienLenCardUtils.FOUR_PAIR_CONT) {
                            if (CardUtil.demDoiThong(players[sNum].getCards()) >= 4) {
                                //fix: trường hợp người bàn chơi có hơn 3 người, và người cầm 4 đôi thông ko bỏ lượt dc
                                if (isSkipAll()) {
                                    typeMove = -1;
                                    continue;
                                } else {
                                    setCurrentPlayer(p);
                                }
                                //end fix
                                break;
                            }
                        }
                    }
                }
            }
            setStateGame(getWaittingGameState());      
            setCurrentMoveTime();
        } catch (Exception e) {
            log.error("Tienlen nextTurn error:" , e);
        }
        return getCurrentPlayer();
    }
    @Override
    public void update() {
        try {
            super.update();
             if (isCanStart()) {
                startGame();
                return;
            }
            /**
             * User sẽ auto skip khi hết thời gian countDown hoặc disconnect khi ván playing
             */
            if (isPlaying() && getCurrentPlayer() != null && isTimeout()) {
                this.log.debug("autoSkip " + getCurrentPlayer().getName());
                checkNoActionNotBetGame(getCurrentPlayer());
                skip(getCurrentPlayer(),getSeatNumber(getCurrentPlayer()));
            }
        } catch (Exception e) {
            log.error("Tienlen update error:", e);
        }
    }
    /**
     * trả về true nếu tất cả người chơi trong phòng đều bỏ lượt hoặc ko có lượt
     *
     * @return
     */
    private boolean isSkipAll() {
        for (int i = 0; i < getPlayersSize(); i++) {
            User u = getUser(i);
            if (isInturn(u) && !players[i].isSkipstatus()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void startGame() {
        this.log.debug("Startgame " + room.getDump());
        checkPlayerEnoughMoney();
        super.startGame(); 
        reset();
        // chon nguoi di truoc
        setMoverFirst();

        playerBeginNewRound = getCurrentPlayer();
        playerMove = getCurrentPlayer();
        if (isPlaying()) {
            // send start message
            for (int i = 0; i < getPlayersSize(); i++) {
                User p = getUser(i);
                if (p != null) {
                    SFSObject m = MessageFactory.getINSTANCE().createMessageStartGame(getPlayingTime()/1000, players[i].getCards(),getIdDBOfUser(getCurrentPlayer()));
                    sendUserMessage(m, p);
                }
            }
        }
        sendStartGameViewerMessge();
        eventForUser();
        // kiem tra xem co ai toi trang khong
        this.processForceFinish();
        if (isPlaying()) {
            setCurrentMoveTime();
            setStateGame(getWaittingGameState());
        }
        this.log.debug("start game: user begin = " + playerBeginNewRound.getName());
    }
    
        /**
     * Chạy event
     */
    private void eventForUser() {
        if (!isEnableEvent()) {
            return;
        }
        for (int i=0; i<players.length; i++) {
            try {
                User u = getUser(i);
                if(u == null){
                    continue;
                }
                addEventForUser(players[i].getCards(), u);
            } catch (Exception e) {
                log.error("TienLenGame.eventForUser() error", e);
            }
        }
    }
    
    /**
     * Xử lý event cho user
     *
     * @param player
     */
    private void addEventForUser(List<Card> cards, User u) {
        try {
            //is sảnh rồng
            if (CardUtil.isStraightDragon(cards)) {
                addUserGetEvent(u, EventManager.TL_IS_STRAIGHT_DRAGON, new ArrayList<>());
                return;
            }
            //is 5 đôi thông
            if (CardUtil.demDoiThong(cards) >= 5) {
                addUserGetEvent(u, EventManager.TL_5_DOI_THONG, new ArrayList<>());
                return;
            }
            //is 6 doi
            if (CardUtil.isSixPairsTLMN(cards)) {
                addUserGetEvent(u, EventManager.TL_6_PAIR, new ArrayList<>());
                return;
            }

            //tứ quý heo
            if (CardUtil.countHeo(cards) == 4) {
                addUserGetEvent(u, EventManager.TL_4_HEO, new ArrayList<>());
                return;
            }
            //4 doi thong
            if (CardUtil.demDoiThong(cards) == 4) {
                addUserGetEvent(u, EventManager.TL_4_DOI_THONG, new ArrayList<>());
                return;
            }

        } catch (Exception e) {
            log.error("Tien Len addEventForUser error:", e);
        }
    }

    /**
     * reset trạng thái của bàn game
     */
    private void reset() {
        loadTimeConfig();
        winner = null;
        cardSet.xaoBai();
        chiabai();
        penalty = BigDecimal.ZERO;
        penaltyDes_EN = "";
        penaltyDes_VI = "";
        penaltyDes_ZH = "";
        penaltyTotal = BigDecimal.ZERO;
        penaltyTotalDes_EN = "";
        penaltyTotalDes_VI = "";
        penaltyTotalDes_ZH = "";
        win = 0;
        setCurrentPlayer(null);
        playerMove = null;
        playerBeginNewRound= null;
        typeMove = -1;
        moneyManagement.reset();
        for (int i = 0; i < 4; i++) {
            players[i].reset();
            User u = getUser(i);
            if(u == null){
                continue;
            }
            moneyManagement.bettingMoney(getIdDBOfUser(u), getMoneyFromUser(u));
        }
        eatHeo.clear();
        firstCardMove.clear();
        cardMove = null;
        boardMoney.setMoney(getMoney());
        boardMoney.setMoneyPot(BigDecimal.ZERO);
    }
    /**
     * chia bai cho tung player trong game.
     */
    private void chiabai() {
        boolean haveBot = false;
        for (int i = 0; i < players.length; i++) {
            if (isBot(getUser(i))) {
                haveBot = true;
            }
            players[i].resetCards();
        }
        
        if (TienLenDemLaConfig.getInstance().isTest() && TienLenDemLaConfig.getInstance().getTestCase() > 0) {
            TienLenDeck deck = new TienLenDeck();
            deck.reset();
            List<Card> mcards = deck.getTestCase(TienLenDemLaConfig.getInstance().getTestCase());
            deck.addFullCard(mcards);
            players[0].getCards().addAll(mcards);
            for (int i = 1; i < players.length; i++) {
                deck.addFullCard(players[i].getCards());
            }
        }else {            
             if (haveBot  && isOpenBotGame()) {
                processBotGame();
            } else {
                dealNormalCards();
            }
        }
    }
    
        private void dealNormalCards() {
        for (int i = 0; i < cardSet.length(); i++) {
            players[i % 4].receivedCard(cardSet.dealCard());
        }
    }
    
    /**
     *  xử lý bài cho bot
     */
    private void processBotGame(){
        int percent = random.nextInt(100);
        boolean bigger = false;
        if (percent < getAdvRatio()) {
            bigger = true;
        }
        if (bigger) {//chia bài lợi thế
            int potIndex = 0;
            for (int i = 0; i < this.players.length; i++) {
                botCards[i].reset();
                User p = getUser(i);
                if (isBot(p)) {
                    potIndex = i;
                }
            }
            for (int i = 0; i < cardSet.length(); i++) {
                botCards[i % 4].addCard(cardSet.dealCard());
            }
            
            int  maxCardsIndex = -1;
            for (int i = 0; i < botCards.length; i++) {
                int moneyType = TienLenCardUtils.getTypeForceFinish(botCards[i].getCards());
                if (moneyType != TienLenCardUtils.NOTYPE) {
                    maxCardsIndex =i;
                    break;
                }
            }
            
            if (maxCardsIndex < 0) {
                maxCardsIndex = TienLenCardUtils.findGreatestCards(botCards);//index có bài lớn nhất
            }
            //hoán đổi
            BotCards tempCards = botCards[potIndex];
            botCards[potIndex] = botCards[maxCardsIndex];
            botCards[maxCardsIndex] = tempCards;

            for (int i = 0; i < this.players.length; i++) {
                this.players[i].getCards().addAll(botCards[i].getCards());
            }
        }else{
            dealNormalCards();
        }
    }
    
    @Override
    public synchronized void stopGame() {
        this.log.debug("-----------------STOPGAME-------------");
        if (!isPlaying()) {
            return;
        }
        try {
            SFSObject m = MessageFactory.getINSTANCE().createMessageStopGame(getIdDBOfUser(getCurrentPlayer()),players, this);
            sendAllUserMessage(m);

        } catch (Exception e) {
            log.error("Stop game tien len:", e);
        } finally {
            super.stopGame();
            //phải kiem tra sau stop game de tranh user leave trước khi stop
            for (User user: getPlayersList()) {
                kickNoActionUser(user);
            }
            if (isShuffleRoom() && getPlayersList().size() > 1) {
                doShuffle();
            } else {
                processCountDownStartGame();
            }
        }
    }
    
    /**
     * kick những user nhà con không đủ tiền không đủ tiền
     *
     * @param bp
     */
    private void checkPlayerEnoughMoney() {
        BigDecimal moneyToPlay = Utils.multiply(getMoney(), new BigDecimal(String.valueOf(TienLenDemLaConfig.getInstance().getMoneyToContinuePlaying())));
        // kick những thằng ko đủ win
        for (User p : getPlayersList()) {
            if (getMoney().signum() > 0 && getMoneyFromUser(p).compareTo(moneyToPlay) < 0) {
                String mess=TienLenLanguage.getMessage(TienLenLanguage.NOT_ENOUGH_WIN, getLocaleOfUser(p));
                mess = String.format(mess,getCurrency(getLocaleOfUser(p)), TienLenDemLaConfig.getInstance().getMoneyToContinuePlaying());
                addToWaitingUserList(p,mess); 
            }
        }
    }

    /**
     * Trường hợp choi 4 nhà có 3 nhà bị xét cóng
     * @param user
     * @param userCong 
     */
    private void xetCong3User(User user){
          try {
            String lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE) +" "+  TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
            String lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE) +" "+  TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE);
            String bonusVi = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE) +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_WIN, GlobalsUtil.VIETNAMESE_LOCALE);
            String bonusEn = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE) +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_WIN, GlobalsUtil.ENGLISH_LOCALE);
            String bonusZh = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.CHINESE_LOCALE) +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_WIN, GlobalsUtil.CHINESE_LOCALE);
            BigDecimal total = BigDecimal.ZERO; 
            User userLoseAll = findUserDen(user,lostVi,lostEn);
              //Có user đền
              if (userLoseAll != null) {
                  //Tổng tiền user đền thua
                  BigDecimal moneyLose = BigDecimal.ZERO;
                  for (int i = 0; i < 4; i++) {                
                      User p = userLoseAll;
                      User p1= getUser(i);
                      List<Card> cards = players[i].getCards();
                      if (cards.size() == DEFAULT_NUMBER_TIENLEN_CARD && isInturn(p1)) {
                          penaltyDes_EN = "";
                          penaltyDes_VI = "";
                          penaltyDes_ZH = "";
                          penalty= BigDecimal.ZERO;
                          xetThuiBai(cards);
                          // phat 2 lan tien cuoc
                          penalty = Utils.add(penalty, boardMoney.getCongMoney());
                          penalty =  getMoneyFromUser(p).min(penalty);
                          penalty = getCanWinOrLoseMoney(p, penalty);
                          penalty = getCanWinOrLoseMoney(user, penalty);
                          if (penalty.signum() > 0) {
                              moneyLose = Utils.add(moneyLose, penalty);
                              updateMoney2WithLocale(p, penalty.negate(), penaltyDes_VI, penaltyDes_EN, penaltyDes_ZH, CommonMoneyReasonUtils.DEN, BigDecimal.ZERO, players[i].cardsToList());
                          }
                          setInturn(p1, false);
                          //trường hợp user được đến sẽ tính hòa
                          if(!Utils.isEqual(p1, p)){
                              updateAchievement(p1, CommonMoneyReasonUtils.HOA);
                          }
                      }
                  }
                  total = moneyLose;
                  int seatLoseAll=getSeatNumber(userLoseAll);
                  if(seatLoseAll>-1){
                      players[seatLoseAll].addPenalty(moneyLose.negate());
                  }
              } else {
                  //trường hợp không có user nào đền
                  for (int i = 0; i < 4; i++) {
                      User p = getUser(i);
                      if(p==null){
                          continue;
                      }
                      List<Card> cards = players[i].getCards();
                      if (cards.size() == DEFAULT_NUMBER_TIENLEN_CARD && isInturn(p)) {
                          penaltyDes_EN = "";
                          penaltyDes_VI = "";
                          penaltyDes_ZH = "";
                          penalty= BigDecimal.ZERO;
                          xetThuiBai(cards);
                          // phat 2 lan tien cuoc
                          penalty = Utils.add(penalty, boardMoney.getCongMoney());
                          penalty =  getMoneyFromUser(p).min(penalty);
                          penalty = getCanWinOrLoseMoney(p, penalty);
                          penalty = getCanWinOrLoseMoney(user, penalty);
                          if (penalty.signum() > 0) {
                              String textVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE)
                                      +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE) + getTextUpdated(": ", penaltyDes_VI);
                              String textEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE)
                                      +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE) + getTextUpdated(": ", penaltyDes_EN);
                              String textZh = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.CHINESE_LOCALE)
                                      +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.CHINESE_LOCALE) + getTextUpdated(": ", penaltyDes_ZH);
                              
                              total = Utils.add(total, penalty);
                              
                              updateMoney2WithLocale(p, penalty.negate(), textVi, textEn, textZh, CommonMoneyReasonUtils.TIEN_PHAT_CONG, BigDecimal.ZERO,players[i].cardsToList());
                              players[i].addPenalty(penalty.negate());
                          }
                          setInturn(p, false);
                      }
                  }

              }
            
            if (total.signum() > 0) {
                // tính thuế số tiền nhận được từ phạt cóng
                BigDecimal []arrResultMoney = setMoneyMinusTax(total, getTax());
                updateMoney2WithLocale(user, arrResultMoney[MONEY], bonusVi, bonusEn, bonusZh,CommonMoneyReasonUtils.THANG_CONG ,arrResultMoney[TAX],null);
                int seatWiner=getSeatNumber(user);
                if(seatWiner>-1){
                    players[seatWiner].addPenalty(arrResultMoney[MONEY]);
                }
                winner=user;
                moveFirst = user;
                sendRankingData(user, arrResultMoney[TAX].doubleValue(), 1);
                updateAchievement(user, CommonMoneyReasonUtils.THANG);
            }
        } catch (Exception e) {
            log.error("Tienlen xetCong3User error:", e);
        }
    }
    
    /**
     * Tìm ra user đền bài
     * @param sNumWiner
     * @param lostVi
     * @param lostEn
     * @return 
     */
    private User findUserDen(User winner, String lostVi, String lostEn) {
        User userLoseAll = null;
        int sNumWiner= getSeatNumber(winner);
        // nếu cóng hết , kiếm thằng đền bài
        for (int j = 0; j < firstCardMove.size() - 1; j++) {
            List<Card> cardMoved = firstCardMove.get(j);
            for (int i = 0; i < getPlayersSize(); i++) {
                sNumWiner = (sNumWiner + 1) % getPlayersSize();
                User p = getUser(sNumWiner);
                if(p==null){
                    continue;
                }
                List<Card> cards = players[sNumWiner].getCards();
                List<Card> returnCards = TienLenCardUtils.findHigherInUserCards(cards, cardMoved);
                if (!returnCards.isEmpty()) {
                    userLoseAll = p;
                    break;
                }
            }
            if (userLoseAll != null) {
                lostVi = TienLenLanguage.getMessage(TienLenLanguage.COMPENSATE, GlobalsUtil.VIETNAMESE_LOCALE) + TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
                lostEn = TienLenLanguage.getMessage(TienLenLanguage.COMPENSATE, GlobalsUtil.ENGLISH_LOCALE) + TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE);
                break;
            }
        }
        return userLoseAll;
    }
    
    private int [] xetThuiBai(List<Card> bai1) {
        int []result = new int[5];
        try {
            penalty = BigDecimal.ZERO;
            List<Card> tempCards = new ArrayList<>();
            tempCards.addAll(bai1);
            int nHeoDo = 0, nHeoDen = 0, n3dt = 0, nTuQuy = 0, n4dt = 0;
            int i, j;

            // heo
            ListIterator<Card> iter = tempCards.listIterator(tempCards.size());
            while (iter.hasPrevious()) {
                Card c = iter.previous();
                // quân bài lớn nhất không phải heo thì khỏi tìm tiếp
                if (!c.isHeo()) {
                    break;
                }
                if (c.isTypeBlack()) {
                    nHeoDen++;
                } else {
                    nHeoDo++;
                }
                iter.remove();
            }

            // tu quy
            if (tempCards.size() >= 4) {

                for (i = 0; i < tempCards.size() - 3; i++) {
                    for (j = 1; j < 4; j++) {
                        if (tempCards.get(i).getCardNumber() != tempCards.get(i + j).getCardNumber()) {
                            break;
                        }
                    }
                    if (j == 4) {
                        nTuQuy++;
                        // bỏ tứ quý ra khỏi bài
                        ListIterator<Card> iter4OfAkind = tempCards.listIterator(i);
                        Card begin4OfAKind = tempCards.get(i);
                        while (iter4OfAkind.hasNext()) {
                            Card c = iter4OfAkind.next();
                            if (c.getCardNumber() == begin4OfAKind.getCardNumber()) {
                                iter4OfAkind.remove();
                            } else {
                                break;
                            }
                        }
                        i--;
                    }
                }
            }

            // doi thong
            if (tempCards.size() >= 6) {
                int count = CardUtil.demDoiThong(tempCards);
                if (count == 4) {
                    n4dt += 1;
                } else if (count == 3) {
                    n3dt += 1;
                }
            }

            BigDecimal n3dtMoney = Utils.multiply(boardMoney.getThui3DoiThong(), new BigDecimal(String.valueOf(n3dt)));
            BigDecimal n4dtMoney = Utils.multiply(boardMoney.getChatBonDoiThongMoney(), new BigDecimal(String.valueOf(n4dt)));
            
            penalty = Utils.add(boardMoney.getThuiHeoDen(nHeoDen), boardMoney.getThuiHeoDo(nHeoDo));
            penalty = Utils.add(penalty,boardMoney.getThuiTuQuyMoney(nTuQuy));
            penalty = Utils.add(penalty,n3dtMoney);
            penalty = Utils.add(penalty,n4dtMoney);

            if (penalty.signum() > 0) {
                penaltyDes_VI = "";
                penaltyDes_EN = "";
                penaltyDes_ZH = "";
                if (nHeoDen > 0) {
                    addPenaltyDesPharseVi(nHeoDen+" "+TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(nHeoDen+" "+TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.ENGLISH_LOCALE));
                    addPenaltyDesPharseZh(nHeoDen+" "+TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.CHINESE_LOCALE));
                }
                if (nHeoDo > 0) {
                    addPenaltyDesPharseVi(nHeoDo+" "+TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(nHeoDo+" "+TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.ENGLISH_LOCALE));
                    addPenaltyDesPharseZh(nHeoDo+" "+TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.CHINESE_LOCALE));
                }
                if (n3dt > 0) {
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.ENGLISH_LOCALE));
                    addPenaltyDesPharseZh(TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.CHINESE_LOCALE));
                }

                if (nTuQuy > 0) {
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.ENGLISH_LOCALE));
                    addPenaltyDesPharseZh(TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.CHINESE_LOCALE));
                }
                if (n4dt > 0) {
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.ENGLISH_LOCALE));
                    addPenaltyDesPharseZh(TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.CHINESE_LOCALE));   
                }
                penaltyDes_VI= TienLenLanguage.getMessage(TienLenLanguage.UNUSED_UPPERCASE, GlobalsUtil.VIETNAMESE_LOCALE)+penaltyDes_VI;
                penaltyDes_EN= TienLenLanguage.getMessage(TienLenLanguage.UNUSED_UPPERCASE, GlobalsUtil.ENGLISH_LOCALE)+penaltyDes_EN;
                penaltyDes_ZH= TienLenLanguage.getMessage(TienLenLanguage.UNUSED_UPPERCASE, GlobalsUtil.CHINESE_LOCALE)+penaltyDes_ZH;
                result[0] += nHeoDo;
                result[1] += nHeoDen;
                result[2] += n3dt;
                result[3] += nTuQuy;
                result[4] += n4dt;
            }

        } catch (Exception e) {
            log.error( "Tienlen xetThuiBai error:" , e);
        }
        
        return result;
    }
    

    /**
     * Kiểm tra có chặt bài (chặt heo, tứ quý, 3 đôi thông, 4 đôi thông)
     */
    private void chat() {
        try {
            penaltyTotal = penaltyTotal.min(getMoneyFromUser(playerBiChat));
            if (penaltyTotal.signum() > 0) {
                if (playerBiChat != null) {
                    String defeatVi=TienLenLanguage.getMessage(TienLenLanguage.DEFEATED, GlobalsUtil.VIETNAMESE_LOCALE);
                    String defeatEn=TienLenLanguage.getMessage(TienLenLanguage.DEFEATED, GlobalsUtil.ENGLISH_LOCALE);
                    String defeatZh=TienLenLanguage.getMessage(TienLenLanguage.DEFEATED, GlobalsUtil.CHINESE_LOCALE);
           
                    updateMoney2WithLocale(playerBiChat, penaltyTotal.negate(), defeatVi+penaltyTotalDes_VI, defeatEn+penaltyTotalDes_EN
                            , defeatZh+penaltyTotalDes_ZH, CommonMoneyReasonUtils.BI_CHAT,BigDecimal.ZERO,null);
                    
                    defeatVi=TienLenLanguage.getMessage(TienLenLanguage.DEFEAT, GlobalsUtil.VIETNAMESE_LOCALE);
                    defeatEn=TienLenLanguage.getMessage(TienLenLanguage.DEFEAT, GlobalsUtil.ENGLISH_LOCALE);
                    defeatZh=TienLenLanguage.getMessage(TienLenLanguage.DEFEAT, GlobalsUtil.CHINESE_LOCALE);
                    BigDecimal []arrResultMoney = setMoneyMinusTax(penaltyTotal,getTax());                   
                    updateMoney2WithLocale(playerMove, arrResultMoney[MONEY], defeatVi+penaltyTotalDes_VI, defeatEn+penaltyTotalDes_EN, 
                            defeatZh+penaltyTotalDes_ZH,CommonMoneyReasonUtils.THANG_CHAT,arrResultMoney[TAX],null);
                }
            }
            penaltyTotal = BigDecimal.ZERO;
            penaltyTotalDes_VI = "";
            penaltyTotalDes_EN = "";
            penaltyTotalDes_ZH = "";
        } catch (Exception e) {
            log.error( "TienLen Chat error", e);
        }
    }
    
    /**
     * TODO: Kiểm tra bài người chơi đánh có hợp lệ không
     *
     * @param bai bai cua luot danh hien tai.
     * @param type type cua luot danh hien tai.
     * @return true neu co the danh, nguoc lai return false.
     */
    private boolean checkMove(Card[] bai, int type) {
        try {
            penalty = BigDecimal.ZERO;
            penaltyDes_VI = "";
            penaltyDes_EN = "";
            penaltyDes_ZH = "";
            // kiem tra dua theo loai bai cua lan danh truoc.
            switch (typeMove) {
                case TienLenCardUtils.NOTYPE:
                    if (type != TienLenCardUtils.NOTYPE) {
                        // reset lại danh sách heo khi người chơi đánh heo ở vòng mới
                        if (bai[bai.length - 1].isHeo()) {
                            eatHeo.clear();
                            eatHeo.addAll(Arrays.asList(bai));
                        }
                        return true;
                    }
                case TienLenCardUtils.ONE_CARD:
                    // truong hop la 1 con
                    if (type == TienLenCardUtils.ONE_CARD && Card.isHigher(bai[0], cardMove[0])) {
                        // luu lai heo de phong truong hop chat chong.
                        if (bai[0].isHeo()) {
                            if (!cardMove[0].isHeo()) {
                                eatHeo.clear();
                            }
                            eatHeo.add(bai[0]);
                        }
                        return true;
                    }
                    // chat heo
                    if (cardMove[0].isHeo()) {
                        if (type == TienLenCardUtils.THREE_PAIR_CONT || type == TienLenCardUtils.FOUR_PAIR_CONT || type == TienLenCardUtils.FOUR_OF_A_KIND) {
                            if (eatHeo.size() == 1) {
                                penaltyDes_VI += 1;
                                penaltyDes_EN += 1;
                                penaltyDes_ZH += 1;
                                if (cardMove[0].isTypeBlack()) {
                                    penalty = boardMoney.getChatHeoDen(1);
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                } else {
                                    penalty =  boardMoney.getChatHeoDo(1);
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                }
                                //truong hop chat chong
                            } else {
                                int black = 0;
                                int red = 0;
                                for (int i = 0; i < eatHeo.size(); i++) {
                                    if (eatHeo.get(i).isTypeBlack()) {
                                        black++;
                                    } else {
                                        red++;
                                    }
                                }
                                if (black == 0 && red == 2) {
                                    penalty = boardMoney.getChatHeoDo(2);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    penaltyDes_ZH += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 1 && red == 1) {
                                    penalty = Utils.add(boardMoney.getChatHeoDen(1), boardMoney.getChatHeoDo(1));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    penaltyDes_ZH += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    penaltyDes_ZH += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 2 && red == 0) {
                                    penalty = boardMoney.getChatHeoDen(2);
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    penaltyDes_ZH += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                } else if (black == 2 && red == 1) {
                                    penalty = Utils.add(boardMoney.getChatHeoDo(1),boardMoney.getChatHeoDen(2));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    penaltyDes_ZH += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    penaltyDes_ZH += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 1 && red == 2) {
                                    penalty = Utils.add(boardMoney.getChatHeoDen(1), boardMoney.getChatHeoDo(2));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    penaltyDes_ZH += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    penaltyDes_ZH += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 2 && red == 2) {
                                    penalty = Utils.add(boardMoney.getChatHeoDen(2), boardMoney.getChatHeoDo(2));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    penaltyDes_ZH += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    penaltyDes_ZH += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                }
                            }
                            return true;
                        }
                    }
                    break;
                case TienLenCardUtils.PAIR:
                    if (type == TienLenCardUtils.PAIR && Card.isHigher(bai[1], cardMove[1])) {
                        if (!cardMove[1].isHeo()) {
                            eatHeo.clear();
                        }
                        eatHeo.add(bai[0]);
                        eatHeo.add(bai[1]);
                        return true;
                    }
                    // chat doi heo
                    if (cardMove[0].getCardNumber() == 12) {
                        if (type == TienLenCardUtils.FOUR_OF_A_KIND || type == TienLenCardUtils.FOUR_PAIR_CONT) {
                            if (eatHeo.size() > 2) {
                                // chat 4 heo
                                penalty = Utils.add(boardMoney.getChatHeoDen(2),boardMoney.getChatHeoDo(2));
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                penaltyDes_ZH += 2;
                                addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                penaltyDes_ZH += 2;
                                addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                addPenaltyDesEn(TienLenLanguage.RED_TWO);
                            } else if (cardMove[1].isTypeBlack()) {
                                penalty = boardMoney.getChatHeoDen(2);
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                penaltyDes_ZH += 2;
                                addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                            } else if (!cardMove[0].isTypeBlack()) {
                                penalty = boardMoney.getChatHeoDo(2);
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                penaltyDes_ZH += 2;
                                addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                addPenaltyDesEn(TienLenLanguage.RED_TWO);
                            } else {
                                penalty = Utils.add(boardMoney.getChatHeoDen(1),boardMoney.getChatHeoDo(1));
                                penaltyDes_VI += 1;
                                penaltyDes_EN += 1;
                                penaltyDes_ZH += 1;
                                addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                penaltyDes_VI += 1;
                                penaltyDes_EN += 1;
                                penaltyDes_ZH += 1;
                                addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                addPenaltyDesEn(TienLenLanguage.RED_TWO);
                            }
                            return true;
                        }
                    }
                    break;
                case TienLenCardUtils.TRIPLE:
                    if (type == TienLenCardUtils.TRIPLE && bai[2].getId() > cardMove[2].getId()) {
                        return true;
                    }
                    break;
                case TienLenCardUtils.STRAIGHT:
                    if (type == TienLenCardUtils.STRAIGHT && bai.length == cardMove.length && bai[bai.length - 1].getId() > cardMove[cardMove.length - 1].getId()) {
                        return true;
                    }
                    break;
                case TienLenCardUtils.THREE_PAIR_CONT:
                    if (type == TienLenCardUtils.THREE_PAIR_CONT && Card.isHigher(bai[5], cardMove[5])
                            || type == TienLenCardUtils.FOUR_OF_A_KIND || type == TienLenCardUtils.FOUR_PAIR_CONT) {
                        penalty = boardMoney.getChat3DoiThong();
                        addPenaltyDesVi(TienLenLanguage.THREE_PAIRS_SEQ);
                        addPenaltyDesEn(TienLenLanguage.THREE_PAIRS_SEQ);
                        return true;
                    }
                    break;

                case TienLenCardUtils.FOUR_OF_A_KIND:
                    if (type == TienLenCardUtils.FOUR_OF_A_KIND && Card.isHigher(bai[3], cardMove[3])
                            || type == TienLenCardUtils.FOUR_PAIR_CONT) {
                        penalty = boardMoney.getChatTuQuyMoney(1);
                        addPenaltyDesVi(TienLenLanguage.FOUR_OF_A_KIND);
                        addPenaltyDesEn(TienLenLanguage.FOUR_OF_A_KIND);
                        return true;
                    }
                    break;

                case TienLenCardUtils.FOUR_PAIR_CONT:
                    if (type == TienLenCardUtils.FOUR_PAIR_CONT && Card.isHigher(bai[7], cardMove[7])) {
                        penalty = boardMoney.getChatBonDoiThongMoney();
                        addPenaltyDesVi(TienLenLanguage.FOUR_PAIRS_SEQ);
                        addPenaltyDesEn(TienLenLanguage.FOUR_PAIRS_SEQ);
                        return true;
                    }
                    break;
            }

        } catch (Exception e) {
            log.error("Tienlen checkMove error " , e);
        }
        return false;
    }
     /**
     * thắng ăn hết
     * @param p 
     */
    private void finishWinAll(User p){
        try{
            //check xem có bị chặt không
            if (penaltyTotal.signum() > 0) {
                chat();
            }

            // xet tien thuong phat
            String lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE);
            String lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE);
            String lostZh = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.CHINESE_LOCALE);
             
            String bonusVi = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE);
            String bonusEn = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE);
            String bonusZh = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.CHINESE_LOCALE);
            
            BigDecimal bonusTotal = boardMoney.getMoneyPot();
            boardMoney.setMoneyPot(BigDecimal.ZERO);
            String textViWiner="";
            String textEnWiner="";
            String textZhWiner="";

            int countWinCards =0;//đếm tổng số card thắng
            int []resultSum = new int[5];
            for (int i = 0; i < getPlayersSize(); i++) {
                User p1 = getUser(i);
                if (p1 != null && !p1.equals(p) && isInturn(i)) {
                    String textVi = "";
                    String textEn = "";
                    String textZh = "";
                    
                    penaltyDes_VI="";
                    penaltyDes_EN="";
                    penaltyDes_ZH="";
                    lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE);
                    lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE);
                    lostZh = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.CHINESE_LOCALE);
                    
                    penalty = BigDecimal.ZERO;
                    int []result = xetThuiBai(players[i].getCards()); // xet thui bai
                    
                    for(int j =0 ;j <result.length ; j++){
                        resultSum[j]+=result[j];
                    }
                    int reasonId = CommonMoneyReasonUtils.THUA;
                    if (players[i].getCards().size() == DEFAULT_NUMBER_TIENLEN_CARD) {
                        penalty = Utils.add(penalty, boardMoney.getCongMoney());
                        //thua cóng phạt thêm 1 lần tiền cược
                        lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE)
                                +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
                        lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE)
                                +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE);
                        lostZh = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.CHINESE_LOCALE)
                                +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.CHINESE_LOCALE);
                        reasonId = CommonMoneyReasonUtils.TIEN_PHAT_CONG;
                    }else{
                        penalty= Utils.add(penalty,Utils.multiply(boardMoney.getMoney(), new BigDecimal(String.valueOf(players[i].getCards().size()))));
                    }
                    penalty = getMoneyFromUser(p1).min(penalty);
                    penalty= getCanWinOrLoseMoney(p1, penalty);
                    if (penalty.signum() > 0) {
                        textVi = players[i].getCards().size()+" lá. ";
                        textVi += (penaltyDes_VI.isEmpty()?"":penaltyDes_VI);
                        
                        textEn = players[i].getCards().size()+" card. ";
                        textEn += (penaltyDes_EN.isEmpty()?"":penaltyDes_EN);
                        
                        textZh = players[i].getCards().size()+" card. ";
                        textZh += (penaltyDes_ZH.isEmpty()?"":penaltyDes_ZH);
                        
                        countWinCards +=  players[i].getCards().size();
                        
                        penalty= getCanWinOrLoseMoney(p, penalty);
                        
                        updateMoney2WithLocale(p1, penalty.negate(), lostVi + ": " +textVi, lostEn + ": " +textEn, lostZh + ": " +textZh, reasonId, BigDecimal.ZERO, players[i].cardsToList());
                        players[i].addPenalty(penalty.negate());
                        bonusTotal = Utils.add(bonusTotal, penalty);
                    }else {
                        //ghi thêm lich sử cho trungong hợp user không đủ tiền
                        updateLogGameForUser(p1, reasonId, players[i].cardsToList());
                    }
                }
            }

            if (resultSum[0] > 0) {
                textViWiner += (textViWiner.isEmpty()?"":", ") + resultSum[0] + " " + TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.VIETNAMESE_LOCALE);
                textEnWiner += (textEnWiner.isEmpty()?"":", ")+ resultSum[0] + " " + TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.ENGLISH_LOCALE);
                textZhWiner += (textZhWiner.isEmpty()?"":", ")+ resultSum[0] + " " + TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.CHINESE_LOCALE);
            }
            if (resultSum[1] > 0) {
                textViWiner += (textViWiner.isEmpty()?"":", ") + resultSum[1] + " " + TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.VIETNAMESE_LOCALE);
                textEnWiner += (textEnWiner.isEmpty()?"":", ") + resultSum[1] + " " + TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.ENGLISH_LOCALE);
                textZhWiner += (textZhWiner.isEmpty()?"":", ") + resultSum[1] + " " + TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.CHINESE_LOCALE);
            }
            if (resultSum[2] > 0) {
                textViWiner += (textViWiner.isEmpty()?"":", ") + resultSum[2] + " " + TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.VIETNAMESE_LOCALE);
                textEnWiner += (textEnWiner.isEmpty()?"":", ") + resultSum[2] + " " + TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.ENGLISH_LOCALE);
                textZhWiner += (textZhWiner.isEmpty()?"":", ") + resultSum[2] + " " + TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.CHINESE_LOCALE);
            }
            if (resultSum[3] > 0) {
                textViWiner += (textViWiner.isEmpty()?"":", ") + resultSum[3] + " " + TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.VIETNAMESE_LOCALE);
                textEnWiner += (textEnWiner.isEmpty()?"":", ") + resultSum[3] + " " + TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.ENGLISH_LOCALE);
                textZhWiner += (textZhWiner.isEmpty()?"":", ") + resultSum[3] + " " + TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.CHINESE_LOCALE);
            }
            if (resultSum[4] > 0) {
                textViWiner += (textViWiner.isEmpty()?"":", ") + resultSum[4] + " " + TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.VIETNAMESE_LOCALE);
                textEnWiner += (textEnWiner.isEmpty()?"":", ") + resultSum[4] + " " + TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.ENGLISH_LOCALE);
                textZhWiner += (textZhWiner.isEmpty()?"":", ") + resultSum[4] + " " + TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.CHINESE_LOCALE);
            }
            
            textViWiner = textViWiner.isEmpty()?"": (TienLenLanguage.getMessage(TienLenLanguage.UNUSED_UPPERCASE, GlobalsUtil.VIETNAMESE_LOCALE)+ ": "+textViWiner);
            textEnWiner =  textEnWiner.isEmpty()?"": (TienLenLanguage.getMessage(TienLenLanguage.UNUSED_UPPERCASE, GlobalsUtil.ENGLISH_LOCALE)+ ": "+textEnWiner);
            textZhWiner =  textZhWiner.isEmpty()?"": (TienLenLanguage.getMessage(TienLenLanguage.UNUSED_UPPERCASE, GlobalsUtil.CHINESE_LOCALE)+ ": "+textZhWiner);
            if (bonusTotal.signum() > 0) {
                String textVi = "";
                String textEn = "";
                String textZh = "";
                // tính thuế số tiền winner nhận được
                BigDecimal [] arrResultMoney = setMoneyMinusTax(bonusTotal, getTax());
                textVi = bonusVi + ": " + TienLenLanguage.getMessage(TienLenLanguage.WIN, GlobalsUtil.VIETNAMESE_LOCALE) + " 1, "+countWinCards +" lá. "+ textViWiner;
                textEn = bonusEn + ": " + TienLenLanguage.getMessage(TienLenLanguage.WIN, GlobalsUtil.ENGLISH_LOCALE) + " 1," +countWinCards +" cards. "+  textEnWiner;
                textZh = bonusZh + ": " + TienLenLanguage.getMessage(TienLenLanguage.WIN, GlobalsUtil.ENGLISH_LOCALE) + " 1," +countWinCards +" cards. "+  textZhWiner;
                updateMoney2WithLocale(p, arrResultMoney[MONEY], textVi, textEn, textZh, CommonMoneyReasonUtils.TOI_1, arrResultMoney[TAX],getCardsMoveList());
                int seatWiner=getSeatNumber(p);
                if(seatWiner>-1){
                    players[seatWiner].addPenalty(arrResultMoney[MONEY]);
                }
                sendRankingData(p, arrResultMoney[TAX].doubleValue(), 1);
                updateAchievement(p, CommonMoneyReasonUtils.THANG);
            }
            SFSObject m= MessageFactory.getINSTANCE().createMessageFinishGame(getIdDBOfUser(p), (byte) win);
            sendAllUserMessage(m);
            moveFirst = p;
            winner = p;
            setCurrentPlayer(p);
        } catch (Exception e) {
            log.error("Tienlen finishWinAll error:" , e);
        } finally {
            stopGame();
        }
    }
    
    private void forceFinish(int i) {
        try {
            int k;
            User p = getUser(i);
            if(p==null){
                return;
            }
            
            List<Card> cards = players[i].getCards();
            SFSObject sfso = MessageFactory.getINSTANCE().createMessageForceFinish(getIdDBOfUser(p), cards, TienLenCardUtils.getTypeForceFinish(cards));
            sendAllUserMessage(sfso);
            
            // xet tien thuong phat
            String lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE);
            String lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE);
            String lostZh = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.CHINESE_LOCALE);
             
            String bonusVi = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE);
            String bonusEn = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE);
            String bonusZh = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.CHINESE_LOCALE);
            BigDecimal bonusTotal = BigDecimal.ZERO;
            for (k = 0; k < getPlayersSize(); k++) {
                User p1 = getUser(k);
                if (p1 != null && !Utils.isEqual(p1, p) && isInturn(p1)) {
                    penalty = BigDecimal.ZERO;

                    // phat 1 van nhat
                    penalty = getMoneyFromUser(p1).min(boardMoney.getForceFinishMoney(13));
                    penalty = getCanWinOrLoseMoney(p1, penalty);
                    penalty = getCanWinOrLoseMoney(p, penalty);
                    if (penalty.signum() > 0) {
                        String textVi;
                        String textEn;
                        String textZh;
                        textVi = lostVi + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.VIETNAMESE_LOCALE) + " " + penaltyDes_VI;
                        textEn = lostEn + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.ENGLISH_LOCALE) + " " + penaltyDes_EN;
                        textZh = lostZh + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.CHINESE_LOCALE) + " " + penaltyDes_ZH;
                        updateMoney2WithLocale(p1, penalty.negate(), textVi, textEn, textZh, CommonMoneyReasonUtils.PHAT_TOI_TRANG, BigDecimal.ZERO, players[k].cardsToList());
                        players[k].addPenalty(penalty.negate());
                    }
                    bonusTotal = Utils.add(bonusTotal, penalty);
                }   
            }
            if (bonusTotal.signum() > 0) {
                String textVi;
                String textEn;
                String textZh;
                // tính thuế số tiền winner nhận được
                BigDecimal[] arrResultMoney = setMoneyMinusTax(bonusTotal, getTax());
                textVi = bonusVi + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.VIETNAMESE_LOCALE) + " " + penaltyDes_VI;
                textEn = bonusEn + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.ENGLISH_LOCALE) + " " + penaltyDes_EN;
                textZh = bonusZh + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.CHINESE_LOCALE) + " " + penaltyDes_ZH;
                updateMoney2WithLocale(p, arrResultMoney[MONEY], textVi, textEn, textZh, CommonMoneyReasonUtils.THUONG_TOI_TRANG, arrResultMoney[TAX], players[i].cardsToList());
                sendRankingData(p, arrResultMoney[TAX].doubleValue(), 1);
                int seatWinner=getSeatNumber(p);
                if(seatWinner > -1){
                    players[seatWinner].addPenalty(arrResultMoney[MONEY]);
                }  
                sendRankingData(p, arrResultMoney[TAX].doubleValue(), 1);
                updateAchievement(p, CommonMoneyReasonUtils.THANG);
            }

            moveFirst = p;
            winner = p;
            setCurrentPlayer(p);
            moveFirst = null;
        } catch (Exception e) {
            log.error("Tienlen forceFinish error:" , e);
        }finally{
            stopGame();
        }
    }
    
    private void processForceFinish() {
        if (isPlaying() == false && winner != null) {//ván có người thắng rồi thì ko tới trắng nữa
            return;
        }

        int sNum = getSeatNumber(getCurrentPlayer());
        for (int i = 0; i < getPlayersSize(); i++) {
            User p = getUser(sNum);
            if (p != null && isInturn(p)) {
                if (checkForceFinish(players[sNum])) {
                    forceFinish(sNum);
                    break;
                }
            }

            sNum = (sNum + 1) % getPlayersSize();
        }
    }
    
    private boolean checkForceFinish(TienLenPlayer tienlenPlayer) {
        try {
            List<Card> bai = tienlenPlayer.getCards();
            if (bai.size() != DEFAULT_NUMBER_TIENLEN_CARD) {
                return false;
            }
            int typeForce = TienLenCardUtils.getTypeForceFinish(bai);
            if (TienLenCardUtils.NOTYPE != typeForce) {
                penaltyDes_VI = TienLenCardUtils.getTypeForceDescription(typeForce, GlobalsUtil.VIETNAMESE_LOCALE);
                penaltyDes_EN = TienLenCardUtils.getTypeForceDescription(typeForce, GlobalsUtil.ENGLISH_LOCALE);
                penaltyDes_ZH = TienLenCardUtils.getTypeForceDescription(typeForce, GlobalsUtil.CHINESE_LOCALE);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Tienlen checkForceFinish error: " , e);
            return false;
        }
    }
    /**
     * thêm mô tả về các khoản thưởng phạt theo cụm từ ngăn cách bởi dấu ","
     *
     * @param key : khóa trong TienLenSoloLanguage
     */
    private void addPenaltyDesPharseVi(String pharse) {
        penaltyDes_VI=(penaltyDes_VI.isEmpty()?"":penaltyDes_VI+", ") ;
        penaltyDes_VI+= pharse;
    }

    private void addPenaltyDesPharseEn(String pharse) {
        penaltyDes_EN=penaltyDes_EN.isEmpty()?"":penaltyDes_EN+", ";
        penaltyDes_EN += pharse;
    }
    
    private void addPenaltyDesPharseZh(String pharse) {
        penaltyDes_ZH=penaltyDes_ZH.isEmpty()?"":penaltyDes_ZH+", ";
        penaltyDes_ZH += pharse;
    }
    
    /**
     * thêm mô tả về các khoản thưởng phạt
     *
     * @param key : khóa trong TienLenSoloLanguage
     */
    private void addPenaltyDesVi(String key) {
        penaltyDes_VI+= TienLenLanguage.getMessage(key, new Locale(GlobalsUtil.VIETNAMESE));
    }

    private void addPenaltyDesEn(String key) {
        penaltyDes_EN += TienLenLanguage.getMessage(key, new Locale(GlobalsUtil.ENGLISH));
        penaltyDes_ZH += TienLenLanguage.getMessage(key, new Locale(GlobalsUtil.CHINESE));
    }

    /**
     * Đếm số user bị xét cóng trong game
     * @return 
     */
    private int countUserCong() {
        int countCong = 0;
        for (int i = 0; i < getPlayersSize(); i++) {
            User p = getUser(i);
            List<Card> cards = players[i].getCards();
            if (cards.size() == DEFAULT_NUMBER_TIENLEN_CARD && p != null && isInturn(p)) {
                countCong++;
            }
        }
        return countCong;
    }
    
    /**
     * Kiem tra nều text rỗng thì không add space vào
     * @param space
     * @param text
     * @return 
     */
    private String getTextUpdated(String space,String text){
        return (text.isEmpty()?"":space + text);
    }

    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(TienLenDemLaConfig.getInstance().getMaxViewer());
    }
    
    public List<Short> getCardsMoveList(){
        List<Short> arr = new ArrayList<>();
        if(this.cardMove==null){
            return arr;
        }
        for (int i = 0; i < this.cardMove.length; i++) {
            if (this.cardMove[i] != null){
                arr.add((short)this.cardMove[i].getId());
            }
        }
        return arr;
    }
    
    private void sendMessagePlaying(User user) {
        try {
            SFSObject m = MessageFactory.getINSTANCE().createMessagePlaying(getIdDBOfUser(playerMove),
                    getCardsMoveList(),getIdDBOfUser(getCurrentPlayer()), (int) getTimeRemain(), getPlayingTime()/1000);
            sendUserMessage(m, user);
        } catch (Exception e) {
            log.error("sendMessagePlaying() error: ", e);
        }
    }

    @Override
    public User getUser(int seat) {
        return super.getUser(seat);
    }

    @Override
    protected byte getServiceId() {
        return Service.TIEN_LEN_DEM_LA;
    }
    
    /**
     * Số tiền win/lose tối đa
     * - Số win/lose của user bị thua nhiều hơn số win
     * của user tới (có khi start ván) thì user tới chỉ ăn bằng với số
     * win mình có (xét từng user có trong bàn). 
     * - Số win của user bị thua ít
     * hơn số win của user tới  (có khi start ván) thì user tới  ăn bằng
     * bằng đúng số win đó (xét từng user có trong bàn).
     *
     * @param user
     * @param money
     * @return
     */
    private BigDecimal getCanWinOrLoseMoney(User userWin, BigDecimal winLoseMoney) {
        try {
            int seat = getSeatNumber(userWin);
            if (seat < 0) {
                return BigDecimal.ZERO;
            }
           return moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(userWin), winLoseMoney);
        } catch (Exception e) {
            log.error("TLMN.getCanWinMoney() error:", e);
        }
        return getMoneyFromUser(userWin);
    }
   
    /**
     * Kiểm tra có xử cóng tất cả không, để tìm user đến bài
     *
     * @return
     */
    private boolean isCongAll() {
        try {
            int countCong = 0;
            int countInturn = 0;
            for (int i = 0; i < getPlayersSize(); i++) {
                User p = getUser(i);
                List<Card> cards = players[i].getCards();
                if (p == null) {
                    continue;
                }
                if (!isInturn(i)) {
                    continue;
                }
                countInturn++;
                if (cards.size() == DEFAULT_NUMBER_TIENLEN_CARD) {
                    countCong++;
                }
            }

            if (countInturn == 4 && countCong == 3) {
                return true;
            }

            if (countInturn == 3 && countCong == 2) {
                return true;
            }

        } catch (Exception e) {
            log.error("Tienlen isCongAll error:", e);
        }
        return false;
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user);
    }
 
}
