/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlentour;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.GameExtension;
import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.tournament.TournamentController;
import game.vn.game.tienlentour.language.TienLenLanguage;
import game.vn.game.tienlentour.message.MessageFactory;
import game.vn.game.tienlentour.object.TienLenPlayer;
import game.vn.game.tienlentour.utils.BoardMoneyUtil;
import game.vn.game.tienlentour.utils.TienLenCardUtils;
import game.vn.game.tienlentour.utils.TienLenDeck;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import org.slf4j.Logger;

/**
 *
 * @author TuanP
 */
public class TienLenTourController extends TournamentController {

    public static final int DEFAULT_NUMBER_TIENLEN_CARD = 13;
    /**
     * bộ bài cho bàn này.
     */
    private final transient CardSet cardSet;
    //bài đánh của lượt gần nhất
    private Card[] cardMove;
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
    private String penaltyDes_VI, penaltyDes_EN, penaltyTotalDes_VI, penaltyTotalDes_EN ;
    private User winner = null;
    private int typeBai;
    TienLenPlayer[] players;
    /**
     * người chơi hiện tại
     */
    private User playerMove, playerBeginNewRound, playerBiChat;
    private final Logger log;
    private boolean isRotated,isMoveFirst;
     /**
     * Xử lý tiền phạt
     */
    private final BoardMoneyUtil boardMoney;
    
    public TienLenTourController(Room room, GameExtension gameEx) {
        super(room, gameEx);
        players = new TienLenPlayer[4];
        for (int i = 0; i< players.length;i++){
            players[i] = new TienLenPlayer();
        }
        eatHeo = new ArrayList<>();
        cardSet = new CardSet();
        log = game.getLogger();
        loadTimeConfig();
        boardMoney= new BoardMoneyUtil();
        isRotated=false;
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
            int countPlayers = getPlayersList().size();
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
                if (countPlayers <= 1) {
                    stopGame();
                } else {

                    penaltyDes_EN = "";
                    penaltyDes_VI = "";
                    // phat 1 van nhat
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.LEAVE_ROOM, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.LEAVE_ROOM, GlobalsUtil.ENGLISH_LOCALE));

                    updateInforWithLocale(player, penaltyDes_VI, penaltyDes_EN);
                    
                    if (countPlayers == 2) {
                        playerFinishWhenLeaveGame(nextPlayer);
                        playerMove = nextPlayer;
                        setCurrentPlayer(player);
                        if (isStarted()) {
                            processFinishTournament(nextPlayer);
                        }
                        stopGame();
                    }
                }
            }
            
            //nếu chỉ còn 1 người chơi thì bò quyền đánh đầu tiên của winner
            if (getPlayersList().size() <= 1) {
                isMoveFirst = false;
            }

        } catch (Exception e) {
            log.error("Tienlen leave game error:", e);
        }finally{
            forceLogoutUser(player);
        }
    }

    private void playerFinishWhenLeaveGame(User player) {
        try {
            int seat = getSeatNumber(player);
            if (player == null || seat <= -1) {
                return;
            }

            winner = player;
            SFSObject m = MessageFactory.getINSTANCE().createMessageFinishGame(getIdDBOfUser(player), (byte) win, getWinsOfUser(player));
            sendAllUserMessage(m);
            win++;
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
            processTournamentBonus();
            return true;
        } catch (Exception e) {
            log.error("error join tien len ", e);
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
                        cardMove,getIdDBOfUser(getCurrentPlayer()), players[seatNum].getCards(), 
                        (int)getTimeRemain(), Utils.isEqual(user, playerBeginNewRound), players, getPlayingTime()/1000, getBonusMoney().doubleValue(),getCountWinMax());

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
            if (!isRoateOverTime()) {
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
            log.error("processMessage error:", e);
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
                SFSObject m =  MessageFactory.getINSTANCE().createMessageMove(idDBP, getCardsMoveList(), getIdDBOfUser(getCurrentPlayer()),isChatHeo, (byte) players[winnerSeatNum].getCards().size());
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
            TienLenPlayer tienlenPlayer = players[sNum];
            if (Utils.isEqual(player, playerBeginNewRound)&& tienlenPlayer.getCards().size() == 13 && isMoveFirst) {
                if (!cards[0].equals(tienlenPlayer.getSmallestCard())) {
                    sendToastMessage(TienLenLanguage.getMessage(TienLenLanguage.MOVE_ERROR_MESSAGE, getLocaleOfUser(player)),player, 3);
                    return;
                }
            }

            if(isMoveFirst){
                isMoveFirst = false;
            }
            if (checkMove(cards, typeBai)) {
                boolean isChatHeo=false;
                if (penalty.signum() > 0 && playerMove!=null) {
                    playerBiChat = playerMove;
                    penaltyTotal = Utils.add(penaltyTotal, penalty);
                    penaltyTotalDes_VI += penaltyDes_VI;
                    penaltyTotalDes_EN += penaltyDes_EN;
                    isChatHeo=true;
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
            log.error( "error when auto play:" , ex.fillInStackTrace());
        }
    }
    
    /**
     * Tìm user đánh đầu tiên của ván là user có bài nhỏ nhất
     */
    private void setMoverFirst() {
        // tim nguoi danh dau tien
        Card smallestCard = null;
        User firstUser = null;
        for (int i = 0; i < getPlayersSize(); i++) {
            User p = getUser(i);
            if (p != null) {
                Card card = players[i].getSmallestCard();
                if (smallestCard == null) {
                    smallestCard = card;
                    firstUser = p;
                } else if (card.compareTo(smallestCard) < 0) {
                    smallestCard = card;
                    firstUser = p;
                }
            }
        }
        setCurrentPlayer(firstUser);
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
           
            if (!isRoateOverTime()) {
                return;
            } else {
                checkStopRotate();
            }
            /**
             * User sẽ auto skip khi hết thời gian countDown hoặc disconnect khi ván playing
             */
            if (isPlaying() && getCurrentPlayer() != null && isTimeout()) {
                this.log.debug("autoSkip " + getCurrentPlayer().getName());
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
        this.log.debug("Startgame ");
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
                    SFSObject m = MessageFactory.getINSTANCE().createMessageStartGame(getPlayingTime()/1000, players[i].getCards(), getIdDBOfUser(getCurrentPlayer()));
                    sendUserMessage(m, p);
                }
            }
        }
        sendStartGameViewerMessge();
        // kiem tra xem co ai toi trang khong
        this.processForceFinish();
        if (isPlaying()) {
            setCurrentMoveTime();
            setStateGame(getWaittingGameState());
        }
        this.log.debug("start game: user begin = " + playerBeginNewRound.getName());
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
        penaltyTotal = BigDecimal.ZERO;
        penaltyTotalDes_EN = "";
        penaltyTotalDes_VI = "";
        win = 0;
        setCurrentPlayer(null);
        playerMove = null;
        playerBeginNewRound= null;
        typeMove = -1;
        for (int i = 0; i < 4; i++) {
            players[i].reset();
            User user= getUser(i);
            players[i].setMoneyBeforeStartGame(user==null ? BigDecimal.ZERO : getMoneyFromUser(user));
        }
        eatHeo.clear();
        cardMove = null;
        boardMoney.setMoney(getMoney());
        isMoveFirst = true;
    }
    
    /**
     * chia bai cho tung player trong game.
     */
    private void chiabai() {
        for (int i = 0; i < players.length; i++) {
            players[i].resetCards();
        }
        if (TienLenTourConfig.getInstance().isTest() && TienLenTourConfig.getInstance().getTestCase() > 0) {
            TienLenDeck deck = new TienLenDeck();
            deck.reset();
            List<Card> mcards = deck.getTestCase(TienLenTourConfig.getInstance().getTestCase());
            deck.addFullCard(mcards);
            players[0].getCards().addAll(mcards);
            for (int i = 1; i < players.length; i++) {
                deck.addFullCard(players[i].getCards());
            }
        }else {            
            for (int i = 0; i < cardSet.length(); i++) {
                players[i % 4].receivedCard(cardSet.dealCard());
            }
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
            processCountDownStartGame();
        }
    }

    /**
     * Trường hợp choi 4 nhà có 3 nhà bị xét cóng
     * @param user
     * @param userCong 
     */
    private void xetCong3User(User user){
          try {
            String bonusVi = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE)+" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_WIN, GlobalsUtil.VIETNAMESE_LOCALE);
            String bonusEn = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE) +" "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_WIN, GlobalsUtil.ENGLISH_LOCALE);
              //trường hợp không có user nào đền
              for (int i = 0; i < 4; i++) {
                  User p = getUser(i);
                  if (p == null) {
                      continue;
                  }
                  String lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE);
                  String lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE);
                  List<Card> cards = players[i].getCards();
                  if (cards.size() == DEFAULT_NUMBER_TIENLEN_CARD && p != null && isInturn(p)) {
                      penalty = BigDecimal.ZERO;
                      xetThuiBai(cards);
                      updateInforWithLocale(p, lostVi, lostEn);
                      setInturn(p, false);
                  }
              }

              updateInforWithLocale(user, bonusVi, bonusEn);
              updateInforWinner(user);
              SFSObject m = MessageFactory.getINSTANCE().createMessageFinishGame(getIdDBOfUser(user), (byte) win, getWinsOfUser(user));
              sendAllUserMessage(m);
              winner = user;
              updateLogGameForUser(user,CommonMoneyReasonUtils.THANG , new ArrayList<>());
        } catch (Exception e) {
            log.error("Tienlen xetCong3User error:", e);
        }
    }
    
    private void xetThuiBai(List<Card> bai1) {
        try {
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
                if (nHeoDen > 0) {
                    addPenaltyDesPharseVi(nHeoDen+" "+TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(nHeoDen+" "+TienLenLanguage.getMessage(TienLenLanguage.BLACK_TWO, GlobalsUtil.ENGLISH_LOCALE));
                }
                if (nHeoDo > 0) {
                    addPenaltyDesPharseVi(nHeoDo+" "+TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(nHeoDo+" "+TienLenLanguage.getMessage(TienLenLanguage.RED_TWO, GlobalsUtil.ENGLISH_LOCALE));
                }
                if (n3dt > 0) {
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_SEQ, GlobalsUtil.ENGLISH_LOCALE));
                }

                if (nTuQuy > 0) {
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.FOUR_OF_A_KIND, GlobalsUtil.ENGLISH_LOCALE));
                }
                if (n4dt > 0) {
                    addPenaltyDesPharseVi(TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.VIETNAMESE_LOCALE));
                    addPenaltyDesPharseEn(TienLenLanguage.getMessage(TienLenLanguage.FOUR_PAIRS_SEQ, GlobalsUtil.ENGLISH_LOCALE));
                    
                }
                penaltyDes_VI= TienLenLanguage.getMessage(TienLenLanguage.UNUSED, GlobalsUtil.VIETNAMESE_LOCALE)+penaltyDes_VI;
                penaltyDes_EN= TienLenLanguage.getMessage(TienLenLanguage.UNUSED, GlobalsUtil.ENGLISH_LOCALE)+penaltyDes_EN;
            }
            
        } catch (Exception e) {
            log.error( "Tienlen xetThuiBai error:" , e);
        }
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
                    updateInforWithLocale(playerBiChat, defeatVi + penaltyDes_VI, defeatEn + penaltyDes_EN);  
                    
                    defeatVi=TienLenLanguage.getMessage(TienLenLanguage.DEFEAT, GlobalsUtil.VIETNAMESE_LOCALE);
                    defeatEn=TienLenLanguage.getMessage(TienLenLanguage.DEFEAT, GlobalsUtil.ENGLISH_LOCALE);
                    updateInforWithLocale(playerMove, defeatVi+penaltyTotalDes_VI, defeatEn +  penaltyTotalDes_EN);    
                }
            }
            penaltyTotal = BigDecimal.ZERO;
            penaltyTotalDes_VI = "";
            penaltyTotalDes_EN = "";
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
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 1 && red == 1) {
                                    penalty = Utils.add(boardMoney.getChatHeoDen(1),boardMoney.getChatHeoDo(1));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 2 && red == 0) {
                                     penalty = boardMoney.getChatHeoDen(2);
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                } else if (black == 2 && red == 1) {
                                    penalty = Utils.add(boardMoney.getChatHeoDo(1),boardMoney.getChatHeoDen(2));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 1 && red == 2) {
                                    penalty = Utils.add(boardMoney.getChatHeoDen(1), boardMoney.getChatHeoDo(2));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
                                    addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                    addPenaltyDesEn(TienLenLanguage.RED_TWO);
                                } else if (black == 2 && red == 2) {
                                    penalty = Utils.add(boardMoney.getChatHeoDen(2), boardMoney.getChatHeoDo(2));
                                    penaltyDes_VI += black;
                                    penaltyDes_EN += black;
                                    addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                    addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                    penaltyDes_VI += red;
                                    penaltyDes_EN += red;
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
                                addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                addPenaltyDesEn(TienLenLanguage.RED_TWO);
                            } else if (cardMove[1].isTypeBlack()) {
                                penalty = boardMoney.getChatHeoDen(2);
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                            } else if (!cardMove[0].isTypeBlack()) {
                                penalty = boardMoney.getChatHeoDo(2);
                                penaltyDes_VI += 2;
                                penaltyDes_EN += 2;
                                addPenaltyDesVi(TienLenLanguage.RED_TWO);
                                addPenaltyDesEn(TienLenLanguage.RED_TWO);
                            } else {
                                penalty = Utils.add(boardMoney.getChatHeoDen(1),boardMoney.getChatHeoDo(1));
                                penaltyDes_VI += 1;
                                penaltyDes_EN += 1;
                                addPenaltyDesVi(TienLenLanguage.BLACK_TWO);
                                addPenaltyDesEn(TienLenLanguage.BLACK_TWO);
                                penaltyDes_VI += 1;
                                penaltyDes_EN += 1;
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
            String lostVi = "";
            String lostEn = "";
            String bonusVi = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE);
            String bonusEn = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE);
            for (int i = 0; i < getPlayersSize(); i++) {
                User p1 = getUser(i);
                if (p1 != null && !p1.equals(p) && isInturn(i)) {
                    lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE);
                    lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE);

                    xetThuiBai(players[i].getCards()); // xet thui bai
                    if (players[i].getCards().size() == DEFAULT_NUMBER_TIENLEN_CARD) {
                        //thua cóng phạt thêm 1 lần tiền cược
                        lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE)
                                +": "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
                        lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE)
                                +": "+ TienLenLanguage.getMessage(TienLenLanguage.INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE);
                    }

                    updateInforWithLocale(p1, lostVi , lostEn);
                }
            }
            
            // tính thuế số tiền winner nhận được
            String textVi = bonusVi + ": " + TienLenLanguage.getMessage(TienLenLanguage.WIN, GlobalsUtil.VIETNAMESE_LOCALE) ;
            String textEn = bonusEn + ": " + TienLenLanguage.getMessage(TienLenLanguage.WIN, GlobalsUtil.ENGLISH_LOCALE);
            updateInforWithLocale(p, textVi, textEn);
            updateInforWinner(p);
            
            SFSObject m= MessageFactory.getINSTANCE().createMessageFinishGame(getIdDBOfUser(p), (byte) win, getWinsOfUser(p));
            sendAllUserMessage(m);
            winner = p;
            setCurrentPlayer(p);
            updateLogGameForUser(p,CommonMoneyReasonUtils.THANG , new ArrayList<>());
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
            // xet tien thuong phat
            String lostVi = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.VIETNAMESE_LOCALE);
            String lostEn = TienLenLanguage.getMessage(TienLenLanguage.LOST, GlobalsUtil.ENGLISH_LOCALE);
            String bonusVi = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE);
            String bonusEn = TienLenLanguage.getMessage(TienLenLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE);
            for (k = 0; k < getPlayersSize(); k++) {
                User p1 = getUser(k);
                if (p1 != null && !Utils.isEqual(p1, p) && isInturn(p1)) {
                    String textVi;
                    String textEn;
                    textVi = lostVi + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.VIETNAMESE_LOCALE) + " " + penaltyDes_VI;
                    textEn = lostEn + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.ENGLISH_LOCALE) + " " + penaltyDes_EN;
                    updateInforWithLocale(p1, textVi, textEn);
                }
            }
            String textVi;
            String textEn;
            textVi = bonusVi + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.VIETNAMESE_LOCALE) + " " + penaltyDes_VI;
            textEn = bonusEn + ": " + TienLenLanguage.getMessage(TienLenLanguage.FORCE_FINISH, GlobalsUtil.ENGLISH_LOCALE) + " " + penaltyDes_EN;
            updateInforWithLocale(p, textVi, textEn);
            winner = p;
            setCurrentPlayer(p);
            checkStopRotate();
        } catch (Exception e) {
            log.error("Tienlen forceFinish error:" , e);
        }finally{
            stopGame();
        }
    }
    
    private void checkStopRotate() {
        if (!isRotated) {
            setCurrentMoveTime();
            SFSObject m = messageController.getRotateTimeInfoMessage((byte) 0);
            sendAllUserMessage(m);
            isRotated = true;
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
                if (checkForceFinish(players[sNum],true)) {
                    forceFinish(sNum);
                    break;
                }
            }

            sNum = (sNum + 1) % getPlayersSize();
        }
    }
    
    private boolean checkForceFinish(TienLenPlayer tienlenPlayer, boolean isNewGame) {
        try {
            List<Card> bai = tienlenPlayer.getCards();
            if (bai.size() != DEFAULT_NUMBER_TIENLEN_CARD) {
                return false;
            }
            // trường hợp ván đầu tiên và có tứ quý 3
            if (isNewGame) {
                if (TienLenCardUtils.isFourBa(bai)) {
                    penaltyDes_VI = TienLenLanguage.getMessage(TienLenLanguage.FOUR_THREECARDS, GlobalsUtil.VIETNAMESE_LOCALE);
                    penaltyDes_EN = TienLenLanguage.getMessage(TienLenLanguage.FOUR_THREECARDS, GlobalsUtil.ENGLISH_LOCALE);
                    return true;
                }
                if (TienLenCardUtils.is3PairsContAtBegin(bai)) {
                    penaltyDes_VI = TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_CONT_WITH_3SPADE, GlobalsUtil.VIETNAMESE_LOCALE);
                    penaltyDes_EN = TienLenLanguage.getMessage(TienLenLanguage.THREE_PAIRS_CONT_WITH_3SPADE, GlobalsUtil.ENGLISH_LOCALE);
                    return true;
                }
            }

            int typeForce = TienLenCardUtils.getTypeForceFinish(bai,isNewGame);
            if (TienLenCardUtils.NOTYPE != typeForce) {
                penaltyDes_VI = TienLenCardUtils.getTypeForceDescription(typeForce, GlobalsUtil.VIETNAMESE_LOCALE);
                penaltyDes_EN = TienLenCardUtils.getTypeForceDescription(typeForce, GlobalsUtil.ENGLISH_LOCALE);
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
    /**
     * thêm mô tả về các khoản thưởng phạt
     *
     * @param key : khóa trong TienLenSoloLanguage
     */
    private void addPenaltyDesVi(String key) {
        penaltyDes_VI+= TienLenLanguage.getMessage(key, new Locale("vi"));
    }

    private void addPenaltyDesEn(String key) {
        penaltyDes_EN += TienLenLanguage.getMessage(key, new Locale("en"));
    }
    

    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(TienLenTourConfig.getInstance().getMaxViewer());
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
        return Service.TIEN_LEN_TOUR;
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
    public byte getWinsOfUser(User user) {
        return super.getWinsOfUser(user); 
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user); 
    }
    
}
