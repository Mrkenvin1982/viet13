/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.PhomCommand;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.object.BoardLogInGame;
import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.lang.GameLanguage;
import game.vn.common.object.MoneyManagement;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.game.phom.lang.PhomLanguage;
import game.vn.game.phom.message.MessageFactory;
import game.vn.game.phom.object.CardPhom;
import game.vn.game.phom.object.CardSetPhom;
import game.vn.game.phom.object.DeckTest;
import game.vn.game.phom.object.Phom;
import game.vn.game.phom.object.PhomPlayer;
import game.vn.game.phom.utils.PhomUtils;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

/**
 *
 * @author tuanp
 */
public class PhomController extends GameController{

    /**
     * so lan tien cuoc khi ăn chốt
     */
    private static final BigDecimal AN_CHOT = new BigDecimal("4.00");
    //xử lý tiền trong ván chơi
    /**
     * so lan tien cuoc khi u
     */
    private static final BigDecimal MONEY_U = new BigDecimal("5.00");
    /**
     * so lan tien cuoc khi an ga lan thu 1
     */
    private static final BigDecimal MONEY_1 = new BigDecimal("1.00");
    /**
     * so lan tien cuoc khi an ga lan thu 2
     */
    private static final BigDecimal MONEY_2 = new BigDecimal("2.00");
    /**
     * so lan tien cuoc khi an ga lan thu 3
     */
    private static final BigDecimal MONEY_3 = new BigDecimal("3.00");
    //xác định loại ù
    private static final byte U0 = 0; //Ù do chia bài
    private static final byte U1 = 1; //Ù khan
    private static final byte U2 = 2; //Ù đền
    /**
     * số lần tiền cược bị phạt khi bị cháy
     */
    private static final int MONEY_PENALTY_CHAY = 4;
    private final Logger log;
    // ghi lại quá trình từng ván bài để bắt lỗi
    private final BoardLogInGame boardLog;
    /**
     * Người thắng ván chơi
     */
    private User winner = null;
    /**
     * người hạ bài trước
     */
    private User firstHa = null;
    /**
     * Leaver is firstHa
     */
    private User leavefirstHa = null;
    /**
     * người bắt đầu vòng mới
     */
    private User userLastMove = null;
    /**
     * bộ bài cho bàn này.
     */
    private final transient CardSetPhom cardSet = new CardSetPhom(52);
    /**
     * đếm số người chơi rời bàn
     */
    private int countLeave = 0;
    /**
     * Số bài nọc giới hạn trong ván chơi, phụ thuộc vào số lượng người chơi
     */
//    private int limitCard = 0;
    /**
     * Số người chơi khi bắt đầu ván
     */
    private int numPlayerStart = 0;
    /**
     * kiểm tra ván là ù
     */
    private boolean isUWin = false;
    /**
     * tiền do người chơi thoát ra
     */
    private BigDecimal leaveMoney = BigDecimal.ZERO;
//    private transient final Object lock = new Object();
    private User lastMoveUser;
    private int currentCardId;
    // luu lai danh sach nhung con bai co the gui qua phomPlayer khac
    private Map<Byte,PhomPlayer> mapCardsCanAdd;
    //danh sách user trong game phom
    private PhomPlayer[] players ;
    //message trong game phom
    MessageFactory messageFactory;
    DeckTest deckTest;
    
    private final MoneyManagement moneyManagement;
    
    public PhomController(Room room, PhomGame gameEx) {
        super(room, gameEx);
        this.players= new PhomPlayer[this.room.getMaxUsers()];
        for(int i=0;i< this.players.length;i++){
            this.players[i]= new PhomPlayer();
        }
        this.log =this.game.getLogger();
        boardLog = new BoardLogInGame();
        messageFactory= new MessageFactory(this);
        moneyManagement = new MoneyManagement();
    }
    public PhomPlayer[] getPlayers(){
        return players;
    }

    @Override
    public User getUser(int seat) {
        return super.getUser(seat); 
    }
    
    /**
     * Chia bài cho người chơi
     */
    private void chiaBai() {
        try {
            if (PhomConfig.getInstance().isTest() && PhomConfig.getInstance().getTestCase() > 0) {
                deckTest = new DeckTest();
                deckTest.reset();
                List<CardPhom> mcards = deckTest.getTestCase(3);

                for (CardPhom c : mcards) {
                    players[0].addCardDeal(c);
                }

                mcards = deckTest.getTestCase(4);
                for (CardPhom c : mcards) {
                    players[1].addCardDeal(c);
                }

                for (int i = 0; i < players.length; i++) {
                    User user = this.getUser(i);
                    if (user == null) {
                        continue;
                    }
                    deckTest.addFullCard(players[i].getCardDeal());
                    players[i].sortCards();
                    players[i].setCurrCards(new ArrayList<>(players[i].getCardDeal()));
                    players[i].setSeat(i);
                    boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "start", 0, "Cards:" + players[i].getStringCards());
                }
                CardPhom c = deckTest.dealCard();
                getPhomPlayer(firstHa).addCardDeal(c);
                getPhomPlayer(firstHa).sortCards();
                getPhomPlayer(firstHa).addCurrCard(c);
                getPhomPlayer(firstHa).sortCurrCards();
            } else {
                //xào bài
                cardSet.xaoBai();
                for (int i = 0; i < players.length; i++) {
                    User user = this.getUser(i);
                    if (user == null) {
                        continue;
                    }
                    //chia mỗi thằng 9 lá
                    List<Card> listCard = new ArrayList<>();
                    for (int j = 0; j < 9; j++) {
                        CardPhom c = cardSet.dealCard();
                        players[i].addCardDeal(c);
                        listCard.add(CardSet.getCard(c.getId()));
                    }

                    players[i].sortCards();
                    players[i].setCurrCards(new ArrayList<>(players[i].getCardDeal()));
                    players[i].setSeat(i);
                    boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "start", 0, "Cards:" + players[i].getStringCards());
                }
                //chia thêm 1 lá cho thằng đánh đầu tiên
                CardPhom c = cardSet.dealCard();
                getPhomPlayer(firstHa).addCardDeal(c);
                getPhomPlayer(firstHa).sortCards();
                getPhomPlayer(firstHa).addCurrCard(c);
                getPhomPlayer(firstHa).sortCurrCards();
            }
        } catch (Exception e) {
            log.error("NewPhom chiaBai: " + boardLog.getLog(), e);
        }
    }

    /**
     * kiểm tra và xử khi có ù khan
     */
    private void proceesU2Khan() {
        try {
            PhomPlayer playerU = null;
            for (int i = 0; i < players.length; i++) {
                User user = this.getUserPlaying(i);
                if (user == null) {
                    continue;
                }
                if (!isInturn(user)) {
                    continue;
                }
                if (checkU2Khan(players[i].getCardDeal())) {
                    players[i].setIsU(true);
                    playerU = players[i];
                    winner = user;
                    players[i].isIsWinner();
                }
            }

            if (playerU != null) {
                sendU2Message(playerU, U1);
                processU2Win(playerU, U1);
                log.info("Stop Game!");
            }
        } catch (Exception e) {
            log.error("NewPhom processU2Khan: " + boardLog.getLog(), e);
        }
    }

    /**
     * Kiểm tra sau khi chia bài có ù khan hay không
     *
     * @param listCards
     * @return
     */
    private boolean checkU2Khan(List<CardPhom> listCards) {

        if (listCards.isEmpty()) {
            return false;
        }

        List<Integer> temp = new ArrayList<>();

        for (CardPhom card : listCards) {
            if (!temp.contains(card.getCardNumber())) {
                temp.add(card.getCardNumber());
            }
        }
        int countPair = 0;
        for (Integer i : temp) {
            //đếm số lần i lặp lại trong card deal
            int count = 0;
            for (CardPhom card : listCards) {
                if (i == card.getCardNumber()) {
                    count++;
                }
                //được 1 đôi
                if (count == 2) {
                    countPair++;
                    count = 0;
                }
            }
        }

        temp.clear();
        if (countPair > 0) {
            return false;
        }

        //bài không đôi
        //xét tiếp phải là không phỏm sảnh
        int tempCardNumber = -2;
        int tempCardType = -2;
        int tempNum1 = -1;
        int tempType1 = -1;

        for (int i = 0; i < listCards.size(); i++) {
            CardPhom card = listCards.get(i);
            if (tempCardType == card.getCardType()
                    && ((tempCardNumber == card.getCardNumber() - 1)
                    || (tempCardNumber == card.getCardNumber() - 2))) {

                return false;
            } else if (tempType1 == card.getCardType() && (tempNum1 == card.getCardNumber() - 2)) {

                return false;
            }

            tempNum1 = tempCardNumber;
            tempType1 = tempCardType;
            tempCardNumber = card.getCardNumber();
            tempCardType = card.getCardType();
        }

        return true;
    }

    /**
     * gởi message kết thúc ván chơi
     */
    private void sendFinishMessage() {
        try {
            //gửi kết quả thằng thắng trước
            if (winner != null) {
                PhomPlayer np = getPhomPlayer(winner);
                if (np != null && !np.isIsLeaved()) {
                    SFSObject ob= this.messageFactory.getStopMesssage(np,getIdDBOfUser(winner));
                    sendAllUserMessage(ob);
                }
            }
            // gửi kết quả những thằng thua
            for (int i = 0; i < players.length; i++) {
                User u = getUser(i);
                if (u == null) {
                    continue;
                }
                if (!isInturn(u)) {
                    continue;
                }
                if (Utils.isEqual(u, winner)) {
                    continue;
                }
                PhomPlayer np = getPhomPlayer(u);
                if (np != null && !np.isIsLeaved()) {
                    SFSObject ob = this.messageFactory.getStopMesssage(np,getIdDBOfUser(u));
                    sendAllUserMessage(ob);
                    boardLog.addLog(u.getName(), getMoneyFromUser(u).doubleValue(), "finish", 0, "Cards:" + np.getStringCards());
                }
            }
        } catch (Exception e) {
            log.error("sendFinishMessage: " + boardLog.getLog(), e);
        }
    }
    /**
     * gửi message bắt đầu ván chơi
     */
    private void sendStartMessage() {
        try {
            // send start message
            for (int i = 0; i < players.length; i++) {
                User u = getUser(i);
                if (u != null) {
                    log.debug("--------id game phom: "+ u.getName()+" , IDHA: "+firstHa.getName());
                    PhomPlayer p = getPhomPlayer(u);
                    SFSObject ob= this.messageFactory.getStartGameMessage(p,getIdDBOfUser(u),getIdDBOfUser(firstHa),
                            getPlayingTime()/1000);
                    sendUserMessage(ob, u);
                    numPlayerStart++;
                }
            }
        } catch (Exception e) {
            log.error("sentStartMessage: " + boardLog.getLog(), e);
        }
    }
    private boolean addCardPhom(byte cardId, Phom phom) {
        List<Byte> listToAdd = new ArrayList<>(phom.getCloneCardIds());
        listToAdd.add(cardId);
        return CardUtil.isPhomList(listToAdd);
    }

    /**
     * gửi 1 lá vô phỏm
     *
     * @param playerToAdd
     * @param currentPlayer
     * @param listToRemove
     * @param cardId
     * @return
     */
    private boolean addCardPhom(PhomPlayer playerToAdd, PhomPlayer currentPlayer, byte cardId) {
        boolean bResult = false;
        try {
            List<Byte> listToAdd = new ArrayList<>();
            listToAdd.add(cardId);
            //lọc qua phỏm của nó
            for (Phom phom : playerToAdd.getCardPhom()) {
                if (listToAdd.isEmpty()) {
                    break;
                }
            //ghép bài
                //lưu cái này lại nếu ghép với id hiện tại ra phỏm thì remove cái này đi
                Set<Byte> temp = phom.getCloneCardIds();
                listToAdd.addAll(temp);
                //chưa add dc bài
                if (listToAdd.size() <= 3) {
                    break;
                }
                //kiểm tra phỏm
                if (CardUtil.isPhomList(listToAdd)) {
                    log.debug("create phom success:" + PhomUtils.listCardToString(listToAdd));
                    //thêm vô phỏm thành công
                    if (currentPlayer.addCardPhoms(cardId, phom)) {
                        String idFromUser=getIdDBOfUser(getUser(currentPlayer.getSeat()));
                        String idUser=getIdDBOfUser(getUser(playerToAdd.getSeat()));
                        sendAddCardMessage(idFromUser,idUser, cardId, listToAdd);
                        bResult = true;
                        //set lại phỏm này đã hạ để tái phỏm
                        phom.setIsHa(true);
                        //add xong rồi thì bay ra, tránh trường hợp add một quân vô 2 phỏm
                        break;
                    }
                }
                //gỡ phỏm ra
                listToAdd.removeAll(temp);
            }
        } catch (Exception e) {
            this.log.error("addCardPhom() erro:", e);
        }
        return bResult;
    }

    /**
     * lấy danh sách bài có thể gửi
     *
     * @param cardsId
     * @param user
     * @param currentPlayer
     * @return
     */
    private List<Byte> getListCardsCanAdd(List<Byte> listCardIds) {
        List<Byte> listCardsCanAdd = new ArrayList<>();
        mapCardsCanAdd = new HashMap<>();
        //lọc qua list thằng đang chơi
        for (int i=0;i<players.length;i++) {
            User user= getUserPlaying(i);
            if(user==null){
                continue;
            }
            //chỉ xét thằng hạ phỏm và chưa thoát
            if ((players[i].isIsHa() || players[i].isIsTaiHa()) && !players[i].isIsLeaved()) {
                //check gửi Phỏm đc ko
                for (Phom phom : players[i].getCardPhom()) {
                    if (!phom.isIsHa()) {
                        continue;
                    }
                    Phom tempPhom = new Phom(new ArrayList<>(phom.getCloneCardIds()));
                    for (int j = 0; j < listCardIds.size(); j++) {
                        byte b = listCardIds.get(j);
                        if (addCardPhom(b, tempPhom)) {
                            mapCardsCanAdd.put(b, players[i]);
                            tempPhom.addCard(b);
                            listCardsCanAdd.add(b);
                            listCardIds.remove(j);
                            /* duyệt lại mảng bài có thể gửi, tránh gửi thiếu bài 
                             * ví dụ trường hợp p có phỏm 789 cơ, danh sách bài gửi 
                             * có 5 cơ, 6 cơ, phải gửi được 6 cơ trước rồi mới gửi 5 cơ
                             */
                            j = -1;
                            log.debug("add success!List card now = " + PhomUtils.listCardToString(listCardIds) + " user=" + user.getName());
                        }
                    }//end for
                }
            }
        }//end for
        return listCardsCanAdd;
    }

    /**
     * gửi bài vào phỏm
     *
     * @param cardsId
     */
    private void addCards(List<Byte> cardsId, User user, PhomPlayer currentPlayer, boolean isAdded) {
        log.debug("List addcards:" + PhomUtils.listCardToString(cardsId));
        try {
            // bị cháy, không cho gửi bài
            if (!currentPlayer.isIsChay()) {
                if (!mapCardsCanAdd.isEmpty()) {
                    for (int j = 0; j < cardsId.size(); j++) {
                        byte b = cardsId.get(j);
                        if (mapCardsCanAdd.containsKey(b)) {
                            PhomPlayer p = mapCardsCanAdd.get(b);
                            if (addCardPhom(p, currentPlayer, b)) {
                                currentPlayer.removeCurrCard(b);
                                cardsId.remove(j);
                                isAdded = true;
                                // duyet lai danh sach bai gui, tranh gui sot bai
                                j = -1;
                                User userAdded=getUser(p.getSeat()) ;
                                if(userAdded!= null) {
                                    List<Short> bList= new ArrayList<>();
                                    bList.add((short)b);
                                    addBoardDetail(userAdded, CommonMoneyReasonUtils.ADD_CARD, getMoneyFromUser(userAdded).doubleValue(), getMoneyFromUser(userAdded).doubleValue(), 0,0, bList);
                                }
                                boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "add card", b, "Cards:" + currentPlayer.getStringCards());
                            }
                        }
                    }
                } else {
                    List<Byte> listTemp = new ArrayList<>(cardsId);
                    //lọc qua list thằng đang chơi
                    for (int i = 0; i < this.players.length; i++) {
                        User u = getUserPlaying(i);
                        if (u == null) {
                            continue;
                        }
                        PhomPlayer p = this.players[i];
                        //chỉ xét thằng hạ phỏm và chưa thoát
                        if ((p.isIsHa() || p.isIsTaiHa()) && !p.isIsLeaved()) {
                            //duyệt qua bài muốn gửi
                            for (int j = 0; j < listTemp.size(); j++) {
                                byte b = listTemp.get(j);
                                //trong khi còn gửi được thì cứ tiếp tục gửi
                                while (addCardPhom(p, currentPlayer, b)) {
                                    currentPlayer.removeCurrCard(b);
                                    cardsId.remove(j);
                                    isAdded = true;
                                    //nếu gửi thành công thì gửi lại từ đầu phòng trường hợp gửi sót bài
                                    addCards(cardsId, user, currentPlayer, true);
                                    User userAdded = getUser(p.getSeat());
                                    if (userAdded != null) {
                                        List<Short> bList = new ArrayList<>();
                                        bList.add((short) b);
                                        addBoardDetail(userAdded, CommonMoneyReasonUtils.ADD_CARD, getMoneyFromUser(userAdded).doubleValue(), getMoneyFromUser(userAdded).doubleValue(), 0,0, bList);
                                    }
                                    boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "add card", b, "Cards:" + currentPlayer.getStringCards());
                                    return;
                                }
                            }//end for
                        }
                    }//end for
                }//end else
            }
            if (!isAdded) {
                String strInfor = PhomLanguage.getMessage(PhomLanguage.CANNOT_SEND_CARD, getLocaleOfUser(user));
                sendToastMessage(strInfor, user, 3);
            }
        } catch (Exception e) {
            this.log.error("addCards() error: ", e);
        }
    }

    /**
     * reset các biến private trong class
     */
    private void reset() {
        this.winner = null;
        this.numPlayerStart = 0;
        this.userLastMove = null;
        setCurrentPlayer(firstHa);
        this.countLeave = 0;
        this.isUWin = false;
        this.leaveMoney =  BigDecimal.ZERO;
        lastMoveUser = null;
        currentCardId = -1;
        boardLog.clear();
        leavefirstHa=null;
        moneyManagement.reset();
        for(int i=0;i<this.room.getMaxUsers();i++){
            players[i].reset();
            User u = getUser(i);
            if(u == null){
                continue;
            }
             moneyManagement.bettingMoney(getIdDBOfUser(u), getMoneyFromUser(u));
        }
    }

    /**
     * kiểm tra quân bài hợp lệ để đánh ra không
     *
     * @param cardId
     * @return
     */
    private boolean isValidMove(byte cardId, PhomPlayer player) {
        boolean valid = true;
        if (player == null) {
            return false;
        }
        //ko phải là bài user đã ăn hoặc không phải quân rác đã đi trước đó.
        if (player.isEatenCard(cardId) || player.getCardMove().contains(cardId)) {
            return false;
        }
        return valid;
    }

    /**
     * Đánh ra một quân bài
     *
     * @param player
     * @param cardId
     */
    private synchronized void move(PhomPlayer player, byte cardId, User user) {
        this.game.trace("PHOM MOVE game move: "+user.getName() +" "+ cardId);
        try {
            if (isValidMove(cardId, player)) {
                //chuyển lượt
                nextTurn();
                lastMoveUser = user;
                currentCardId = cardId;
                //gửi message move
                sendMoveMessage(cardId, player);
                //thêm bài đã đánh
                player.addCardMove(cardId);
                //xóa con đã đánh
                player.removeCurrCard(cardId);
                //xóa con bài khỏi phỏm
                player.removePhomByCardId(cardId);
                //
                player.incrCountTurn();
                //reset trạng thái đã bốc bài
                player.resetIsGotCard();
                //hết bài, ù
                if (player.getCurrCards().isEmpty()) {
                    processU(player, null);
                }
                //nếu là lượt hạ cuối cùng
                if (numPlayerStart - (getNumHaPhom() + countLeave) <= 0) {
                    processNormalWin();
                }
            } else {
                sendToastMessage(PhomLanguage.getMessage(PhomLanguage.INVALID_CARDS,getLocaleOfUser(user)), user, AN_CHOT.intValue());
                //chuyển lượt
                nextTurn();
            }

            userLastMove = user;
        } catch (Exception e) {
            log.error("new phom game move: " + boardLog.getLog(), e);
            //chuyển lượt
            nextTurn();
        }
    }

    /**
     * gửi message move cho toàn bàn
     *
     * @param cardId
     * @param userId
     */
    private void sendMoveMessage(byte cardId, PhomPlayer player) {
        try {
            SFSObject m = this.messageFactory.getMoveMessage(getIdDBOfUser(getUser(player.getSeat())),getIdDBOfUser(getCurrentPlayer()),cardId, player.numCardMove());
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("sendMoveMessage: " + boardLog.getLog(), e);
        }
    }

    /**
     * Gui message Ha phom
     *
     */
    private void sendHaPhomMessage(PhomPlayer player) {
        try {
            SFSObject m = this.messageFactory.getHaPhomMesssage(player);
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("sendHaPhomMessage: " + boardLog.getLog(), e);
        }
    }

    /**
     * user rút một quân bài
     *
     * @param user
     */
    private void getCard(User user, boolean discardError) {
        try {
            PhomPlayer player = getPhomPlayer(user);
            if (!player.isIsLeaved()) {
                //người đánh đầu tiên + lượt đầu tiên ko dc phép rút bài
                if ((player.getCountTurn() == 0 && user == this.firstHa && player.getCardDeal().size() == 10) || player.isIsGotCard()) {
                    if (discardError == false) {
                        sendToastMessage(PhomLanguage.getMessage(PhomLanguage.CANNOT_GET_CARD,getLocaleOfUser(user)), user, 3);
                    }
                } else {//nếu chưa rút thì cho rút
                    if (player.isIsGotCard() == false && this.cardSet.hasCard()) {
                        CardPhom card = this.cardSet.dealCard();
                        if (card != null) {
                            log.debug(user.getName() + " get card " + PhomUtils.getCardName(card.getId()));
                            player.addCardGet(card);
                            sendGetCardMessage(user, card.getId());
                            player.setIsGotCard(true);
                            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "getCard", card.getId(), "Cards:" + player.getStringCards());
                            List<Short> bList = new ArrayList<>();
                                        bList.add((short) card.getId());
                            addBoardDetail(user, CommonMoneyReasonUtils.GET_CARD
                                    , getMoneyFromUser(user).doubleValue(), getMoneyFromUser(user).doubleValue(), 0, 0, bList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("new phom getCard: " + boardLog.getLog(), e);
        }
    }

    /**
     * gửi message get card cho 1 user
     *
     * @param user
     * @param cardId
     */
    private void sendGetCardMessage(User user, byte cardId) {
        try {
            SFSObject m = this.messageFactory.getCardMessage(getIdDBOfUser(user), cardId);
            sendUserMessage(m, user);
        } catch (Exception e) {
            log.error("sendGetCardMessage: " + boardLog.getLog(), e);
        }
    }

    private void sendAddCardMessage(String idFromUser,String userId, byte card, List<Byte> cardset) {
        try {
            SFSObject ob= this.messageFactory.getAddCardMessage(idFromUser,userId, card,PhomUtils.list2List(cardset));
            sendAllUserMessage(ob);
        } catch (Exception e) {
            log.error("sendAddCardMessage: " + boardLog.getLog(), e);
        }
    }

    /**
     * gửi message ù cho cả bàn
     *
     * @param p
     * @param u
     */
    private void sendU2Message(PhomPlayer p, byte u) {
        try {
            User user = getUser(p.getSeat());
            SFSObject m = this.messageFactory.getUMessage(p, getIdDBOfUser(user), u);
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("sendU2Message: " + boardLog.getLog(), e);
        }
    }

    /**
     * Trường hợp 2 người thoát ra một người
     *
     * @param bonusMoney
     */
    private void onceWin(User user) {

        if (!isPlaying() || isUWin) {
            return;
        }

        try {
            BigDecimal[] arrResultMoney = setMoneyMinusTax(this.leaveMoney, getTax());
            for (int i = 0; i < this.players.length; i++) {
                User u = getUserPlaying(i);
                if (u == null) {
                    continue;
                }
                if (!isInturn(u)) {
                    continue;
                }
                if (Utils.isEqual(u, user)) {
                    continue;
                }
                PhomPlayer player = getPhomPlayer(u);
                if (player != null) {
                    player.setBonusMoney(arrResultMoney[MONEY]);
                    player.setIsWinner(true);
                    winner = u;
                    updateMoney(u, arrResultMoney[MONEY],CommonMoneyReasonUtils.THANG,
                            GameLanguage.getMessage(GameLanguage.WIN, GlobalsUtil.ENGLISH_LOCALE),
                            GameLanguage.getMessage(GameLanguage.WIN, GlobalsUtil.VIETNAMESE_LOCALE),
                            GameLanguage.getMessage(GameLanguage.WIN, GlobalsUtil.CHINESE_LOCALE),
                            arrResultMoney[TAX], null);
                    sendRankingData(u, arrResultMoney[TAX].doubleValue(), 1);
                    updateAchievement(u, CommonMoneyReasonUtils.THANG);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("onceWin error ", e);
        }
        stopGame();
    }
    /**
     * Sử dung updateMoney3 để đảm bảo không bị âm win
     * Đảm bảo phải - win cho user thua trước
     * Sau đó mới thực hiện + win thắng
     * điều
     * @param u
     * @param value
     * @param reasonId
     * @param desc
     * @param messEn
     * @param messVi 
     */
    private void updateMoney(User u, BigDecimal value, int reasonId, String messEn, String messVi, String messZh, BigDecimal tax, List<Short> cardIds) {
        updateMoney2WithLocale(u, value, messVi, messEn, messZh, reasonId, tax, cardIds);
    }

    @Override
    public synchronized void leave(User user) {
        log.debug(user.getName() + " bỏ cuộc");
        try {
            //xử lý khi có người thoát khỏi bàn
            int seatNum = getSeatNumber(user);
            //kiểm tra player có đang trong game
            boolean isInturn = isInturn(user);
            User nextUser = this.nextPlayer(user);
            super.leave(user);
            if (isPlaying() && isInturn && seatNum != -1) {
                PhomPlayer p = players[seatNum];
                if (p != null) {
                    BigDecimal penalize =  Utils.multiply(getMoney(), new BigDecimal(String.valueOf(MONEY_PENALTY_CHAY)));
                    BigDecimal money = getMoneyFromUser(user).min(penalize);
                    //cập nhật tiền
                    this.leaveMoney = Utils.add(this.leaveMoney, money);
                    p.setBonusMoney(money.negate());
                    String enLog=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.ENGLISH_LOCALE);
                    String viLog=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.VIETNAMESE_LOCALE);
                    String ZhLog=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.CHINESE_LOCALE);
                    updateMoney(user, money.negate(), CommonMoneyReasonUtils.BO_CUOC ,enLog, viLog, ZhLog, BigDecimal.ZERO,p.getCurrCardsId2Array());
                    sendMessageUpdateMoneyUserLeave(user, -money.doubleValue(), enLog, viLog, ZhLog);
                    p.setIsLeaved(true);
                    countLeave++;
                    log.debug("leave " + p + "");
                }

                //nếu thằng đang còn lượt và thoát ra
                if (Utils.isEqual(getCurrentPlayer(), user)) {
                    setCurrentPlayer(nextUser);
                    setCurrentMoveTime();
                    setStateGame(this.getWaittingGameState());
                    //nếu thằng hiện là first hạ thì chuyển thành thằng tiếp theo
                    if (Utils.isEqual(firstHa, user)) {
                        firstHa = getCurrentPlayer();
                    }
                    if (getCurrentPlayer() != null) {
                        sendSkipMessage();
                    }
                } else if (Utils.isEqual(firstHa, user)) {
                    /**
                     * luu LeavefirstHa = firstHa vi tranh truong hợp firstHa kế tiếp
                     * là nextUser ăn gà LeavefirstHa và chuyển lượt cho user tiếp theo thì rules game sẽ sai
                     */
                    leavefirstHa=firstHa;
                    firstHa = nextUser;
                    if (getCurrentPlayer() != null) {
                        sendSkipMessage();
                    }
                }

            }
            if(seatNum != -1){
              players[seatNum].reset();  
            }
            //thoát khi bàn còn 2 người
            if (isPlaying()) {
                if (numPlayerStart - countLeave == 1) {
                    onceWin(user);
                }
            }
        } catch (Exception e) {
            log.error("NewPhom leave error" + boardLog.getLog(), e);
        }finally{
            forceLogoutUser(user);
        }
       
        boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "leave", 0);
        //không còn ai trong bàn thì reset winner
        if (this.getPlayersList().isEmpty() || Utils.isEqual(winner, user)) {
            winner = null;
        }
    }

    
    private void sendSkipMessage() {
        try {
            SFSObject m= this.messageFactory.getSkipMessage(getIdDBOfUser(getCurrentPlayer()),getIdDBOfUser(firstHa));
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("Phom error when skip Phom" + boardLog.getLog(), e);
        }
    }

    /**
     * kiểm tra xem có thể ăn bài hay không
     *
     * @param user
     * @param cardEat
     * @param card1
     * @param card2
     * @return
     */
    private boolean canChangePhom(PhomPlayer player, List<Byte> listCardSend) {
        if (player == null || player.isIsGotCard()) {
            return false;
        }
        //kiểm tra bài con gửi lên có phải là con đã ăn từ trước hay ko
        for (byte b : listCardSend) {
            if (player.isEatenCard(b)) {
                return false;
            }
        }

        //kiểm tra ăn 2 gà tạo phỏm 
        Collections.sort(listCardSend);
        for (Phom phom : player.getCardPhom()) {
            for (byte b : listCardSend) {
                if (phom.getCardIds().contains(b)) {
                    List<Byte> cardsPhom = new ArrayList<>();
                    byte cardEat = -1;
                    Iterator<Byte> iterator = phom.getCardIds().iterator();
                    while (iterator.hasNext()) {
                        Byte cardId = iterator.next();
                        if (player.getCardEat().contains(cardId)) {
                            cardEat = cardId;
                        }
                        if (cardId == b) {
                            continue;
                        }
                        cardsPhom.add(cardId);
                    }

                    if (cardEat > -1) {
                        Collections.sort(cardsPhom);
                        // check lại tách phỏm dài > 4 con, các con còn lại tạo được phỏm thì cho nó tách
                        if (cardsPhom.size() > 3) {
                            for (int i = 0; i < cardsPhom.size() - 2; i++) {
                                // lấy bộ 3 tạo phỏm, nếu có phỏm thì cho phép ăn bài
                                List<Byte> createPhom = cardsPhom.subList(i, i + 3);
                                if (CardUtil.isPhomList(createPhom) && createPhom.contains(cardEat)) {
                                    return true;
                                }
                            }
                        }
                        if (!CardUtil.isPhomList(cardsPhom)) {
                            return false;
                        }
                    }
                }
            }
        }
        return CardUtil.isPhomList(listCardSend);
    }

    /**
     * Gui message an bai
     */
    private void sendEatCardMessage() {
        try {
            SFSObject m= this.messageFactory.getEatCardMessage(getIdDBOfUser(firstHa));
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("sendEatCardMessage: " + boardLog.getLog(), e);
        }
    }
    /**
     * lay rac cua thang first ha chuyen cho thang bi an
     * và chuyển cho thằng kế tiếp
     */
    private void xoayRac() {
        try {
            PhomPlayer player = getPhomPlayer(firstHa);
            //lay rac cua thang first ha chuyen cho thang bi an
            if (player != null && player.numCardMove() != 0
                    && this.firstHa != null && this.userLastMove != null) {
                if (this.firstHa.getId() != this.userLastMove.getId()) {
                    byte cardId = player.getLastCardIdMove();
                    //            log.info("xoay rac: " + cardId);
                    //chuyển rác cho thằng bị ăn userLastMove
                    PhomPlayer playerLastMove = getPhomPlayer(userLastMove);
                    if (playerLastMove != null) {
                        playerLastMove.addCardMove(cardId);
                    }
                    player.removeCardMove(cardId);
                }

                //nếu thằng này hạ rồi, thì cho nó hạ tiếp
                if (player.isIsHa()) {
                    player.setIsHa(false);
                    player.setIsTaiHa(true);
                }
            }
            /**
             * Không xét user first Hạ lần nữa vì
             * User A la firstHa, thoát bàn -> chuyển firstHa ==user B(currentPlayer)
             * Nên nếu user B hiện tại ăn gà của A thì không chuyển firstHa nua
             */
            if(!isInturn(userLastMove) && Utils.isEqual(userLastMove, leavefirstHa)){
               return; 
            }            
            //xác định thằng hạ phỏm tức là thằng bắt đầu vòng mới
            int seat = getSeatNumber(firstHa);
            this.firstHa=getNewFirstHa(seat); 
        } catch (Exception e) {
            log.error("NewPhomGame xoay rac loi: " + boardLog.getLog(), e);
        }
    }
    /**
     * Tim thằng first ha ke tiep la thằng vi trí chổ ngồi next
     * @param seat
     * @return 
     */
    private User getNewFirstHa(int seat) {
        User newFirstHa = null;
        for (int i = seat+1; i < players.length; i++) {
            if (getUser(i) != null && isInturn(i)) {
                newFirstHa = getUser(i);
                break;
            }
        }
        if (newFirstHa == null) {
            for (int i = 0; i < seat; i++) {
                if (getUser(i) != null && isInturn(i)) {
                    newFirstHa = getUser(i);
                    break;
                }
            }
        }
        return newFirstHa;
    }

    /**
     * Lấy newphomplayer theo userName
     *
     * @param userId
     * @return
     */
    private PhomPlayer getPhomPlayer(User user) {
         int seat=getSeatNumber(user);
         if(seat!=-1){
             return players[seat];
         }
        return null;
    }

    /**
     * Xử lý hạ phỏm của player
     *
     * @param p
     * @param arrCards
     */
    private void haPhom(PhomPlayer p, List<Byte> arrCards, User user) {
        log.debug("haPhom: " + "user " + user.getName() + " cards=" + PhomUtils.listCardToString(arrCards));

        //hạ rồi hạ nữa là tái hạ
        if (p.isIsHa()) {
            p.setIsTaiHa(true);
            log.debug("tai ha Phom ");
        }
        //set đã hạ phỏm
        p.setIsHa(true);
        //kiểm tra xem bài có nằm trong bộ phỏm hoặc bộ cầm trên tay ko
        ArrayList<Byte> temp = new ArrayList<>(p.getCurrCardsId());
        temp.addAll(p.getCardsIdInPhom());
        for (Byte b : arrCards) {
            if (!temp.contains(b) && b != -1) {
                sendToastMessage(PhomLanguage.getMessage(PhomLanguage.LAY_DOWN_NOT_SUCCESS, getLocaleOfUser(user)), user, 3);
                log.debug("Ha phom khong thanh cong: " + "user " + user.getName() + " " + PhomUtils.listCardToString(arrCards));
                return;
            }
        }
        temp.clear();
        // end kiem tra bai        
        for (Byte b : arrCards) {
            if (b == -1) {
                Set<Byte> setPhom = new HashSet<>(temp);
                if (CardUtil.isPhomList(temp) && setPhom.size() == temp.size()) {
                    // check các phỏm đang có, đúng phỏm client gửi lên thì set nó hạ
                    for (Phom phom : p.getCardPhom()) {
                        if (!phom.isIsHa() && phom.getCardIds().containsAll(setPhom)) {
                            phom.setIsHa(true);
                            // check lại danh sách bài đang cầm, có bài trùng với phỏm hạ thì bỏ đi
                            if (p.getCurrCardsId().containsAll(phom.getCardIds())) {
                                for (byte b1 : setPhom) {
                                    p.getCurrCards().remove(new CardPhom(b1));
                                }
                            }
                        }
                    }
                }
                temp.clear();
            } else {
                temp.add(b);
            }
        }

        log.debug("check cháy:");
        // check cháy bài
        if (p.getCardPhom().isEmpty()) {
            p.setIsChay(true);
        } else {
            //trường hợp có phỏm nhưng không hạ -> cháy
            p.setIsChay(true);
            for (Phom phom : p.getCardPhom()) {
                // đảm bảo các phỏm có chứa quân bài ăn phải được hạ
                if (!phom.isIsHa() && phom.isContainOneOfCard(p.getCardEat())) {
                    phom.setIsHa(true);
                }
                log.debug("xet phỏm:" + PhomUtils.listCardToString(new ArrayList<>(phom.getCardIds())) + " hạ:" + phom.isIsHa());
                if (phom.isIsHa()) {
                    p.setIsChay(false);
                }
            }
        }
        sendHaPhomMessage(p);
        if (p.isIsChay()) {
            log.debug(getUser(p.getSeat()).getName() + " cháy " + " -cards:" + PhomUtils.listCardToString(p.getCurrCardsId()));
        }
        log.debug("end check cháy!!!!!!!!!!!!!!!!!!");
        // add lại bài trong phỏm chưa hạ vào bài cầm trên tay để tính điểm
        p.getCurrCards().addAll(p.getCardPhomChuaHa());
        // danh sách bài có thể gửi bài cho client
        if (!p.isIsChay()) {
            sendListAddCards(p);
        }
    }

    /**
     * gửi danh sách bài có thể add
     *
     * @param p
     */
    private void sendListAddCards(PhomPlayer p) {
        try {
            List<Byte> cardsCanAdd = getListCardsCanAdd(p.getCurrCardsId());
//            log.debug("list can add =" + NewPhomUtils.listCardToString(cardsCanAdd));
            SFSObject m =this.messageFactory.getListAddCardsMessage(PhomUtils.list2List(cardsCanAdd));
            sendUserMessage(m, getUser(p.getSeat()));
        } catch (Exception ex) {
            log.error("sendListAddCards error", ex);
        }
    }

    /**
     * đếm số thằng cháy trong bàn
     *
     * @return
     */
    private int countChay() {
        int count = 0;
        for (int i=0;i< this.players.length;i++) {
            User user= getUser(i);
            if(user==null){
                continue;
            }
            if (this.players[i].isIsChay() || this.players[i].isIsLeaved()) {
                count++;
            }
        }
        return count;
    }

    /**
     * tính tổng tiền ăn của tổng người chơi bị cháy sử dụng khi trong ván tất
     * cả đều cháy
     *
     * @return
     */
    private BigDecimal calMoneyChay(boolean isTotalChay, User playerWin) {
        BigDecimal count = BigDecimal.ZERO;
        log.debug("thắng >>>> " + playerWin);
        
        String winnerIdDB = getIdDBOfUser(winner);
        for (int i=0; i< this.players.length;i++) {
            User user= this.getUser(i);
            if(user==null){
                continue;
            }
            if (!isInturn(user)) {
                continue;
            }
            PhomPlayer player=this.players[i];
            if (player.isIsChay()) {
                BigDecimal moneyChay = BigDecimal.ZERO;
                //chưa thoát khỏi bàn thì trừ tiền
                if (!player.isIsLeaved()) {
                    //tất cả đều cháy
                    if (isTotalChay) {
                        //thằng thắng thì ko trừ tiền
                        if (!Utils.isEqual(user, playerWin)) {
                            log.debug(player + "");
                            BigDecimal penaltyChay = Utils.multiply(getMoney(), new BigDecimal(String.valueOf(MONEY_PENALTY_CHAY)));
                            moneyChay = getMoneyFromUser(user).min(penaltyChay);
                            moneyChay = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(user), moneyChay);
                            
                            moneyChay = moneyManagement.getCanWinOrLoseMoney(winnerIdDB, moneyChay);
                            player.setBonusMoney(moneyChay);
                            updateMoney(user, moneyChay.negate(), CommonMoneyReasonUtils.CHAY,
                                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.ENGLISH_LOCALE),
                                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.VIETNAMESE_LOCALE),
                                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.CHINESE_LOCALE),
                                    BigDecimal.ZERO,player.getCurrCardsId2Array());
                        }
                    }
                }
                count = Utils.add(count, moneyChay);
            }
        }
        return count;
    }

    private BigDecimal processChayOrWin(PhomPlayer p) {
        if (p.isIsLeaved()) {
            //do tiền bonus của thằng thoát là âm nên trả về -
            return p.getBonusMoney().negate();
        }
        if (p.isIsChay()) {
            User loser = getUser(p.getSeat());
            BigDecimal penaltyChay = Utils.multiply(getMoney(), new BigDecimal(String.valueOf(MONEY_PENALTY_CHAY)));
            BigDecimal money = getMoneyFromUser(loser).min(penaltyChay);
            money = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(loser), money);
            p.setBonusMoney(money.negate());
            updateMoney(loser, money.negate(), CommonMoneyReasonUtils.CHAY ,
                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.ENGLISH_LOCALE),
                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.VIETNAMESE_LOCALE), 
                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.CHINESE_LOCALE),
                    BigDecimal.ZERO,p.getCurrCardsId2Array());
            return money;
        } else {
            p.setIsWinner(true);
            winner = getUser(p.getSeat());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal processChayOrLose(PhomPlayer p, BigDecimal rate) {
        BigDecimal money = BigDecimal.ZERO;
        User user=getUser(p.getSeat());
        if (p.isIsLeaved()) {
            //do tiền bonus của thằng thoát là âm nên trả về -
            return p.getBonusMoney().negate();
        }
        if (p.isIsChay()) {
            BigDecimal penaltyChay = Utils.multiply(getMoney(), new BigDecimal(String.valueOf(MONEY_PENALTY_CHAY)));
            money = getMoneyFromUser(user).min(penaltyChay);
            money = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(user), money);
            p.setBonusMoney(money.negate());
            
            updateMoney(user, money.negate(), CommonMoneyReasonUtils.CHAY,
                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.ENGLISH_LOCALE),
                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.VIETNAMESE_LOCALE),
                    PhomLanguage.getMessage(PhomLanguage.EMPTY_PHOM,GlobalsUtil.CHINESE_LOCALE),
                    BigDecimal.ZERO,p.getCurrCardsId2Array());
            return money;
        } else {
            BigDecimal moneyRate = Utils.multiply(getMoney(), rate);
            money = getMoneyFromUser(user).min(moneyRate);
            money = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(user), money);
            
            p.setBonusMoney(money.negate());
            updateMoney(user, money.negate(), CommonMoneyReasonUtils.THUA,
                    GameLanguage.getMessage(GameLanguage.LOSE,GlobalsUtil.ENGLISH_LOCALE),
                    GameLanguage.getMessage(GameLanguage.LOSE,GlobalsUtil.VIETNAMESE_LOCALE),
                    GameLanguage.getMessage(GameLanguage.LOSE,GlobalsUtil.CHINESE_LOCALE),
                    BigDecimal.ZERO,p.getCurrCardsId2Array());
            return money;
        }
    }

    /**
     * lấy player thắng ván chơi
     *
     * @return
     */
    private PhomPlayer getPlayerWinner() {
        for (int i=0;i<this.players.length;i++) {
            User user= this.getUser(i);
            if(user==null){
                continue;
            }
            if (!players[i].isIsLeaved()) {
                if (players[i].isIsWinner() || players[i].isIsU()) {
                    return players[i];
                }
            }
        }
        return null;
    }

    private void processNormalWin() {
        //xử ù rồi, ko tính thắng thua nữa
        if (!isPlaying() || isUWin) {
            return;
        }

        try {
            //Tiền thằng thắng
            BigDecimal winBonusMoney = BigDecimal.ZERO;
            PhomPlayer previousPlayer = null;

            //nếu số thằng cháy = số người chơi thì thằng hạ phỏm đầu tiên thắng
            if (numPlayerStart == countChay()) {
                winner = firstHa;
                //ăn hết
                PhomPlayer playerWin = getPhomPlayer(winner);
                BigDecimal moneyChay = calMoneyChay(true, winner);
                //tiền ăn sau thuế
                BigDecimal []moneyChayAfterTax = setMoneyMinusTax(moneyChay, getTax());
                playerWin.setIsWinner(true);
                
                updateMoney(winner, moneyChayAfterTax[MONEY], CommonMoneyReasonUtils.THANG,
                        GameLanguage.getMessage(GameLanguage.WIN,GlobalsUtil.ENGLISH_LOCALE),
                        GameLanguage.getMessage(GameLanguage.WIN,GlobalsUtil.VIETNAMESE_LOCALE),
                        GameLanguage.getMessage(GameLanguage.WIN,GlobalsUtil.CHINESE_LOCALE),
                        moneyChayAfterTax[TAX],playerWin.getCurrCardsId2Array());
                playerWin.setBonusMoney(moneyChayAfterTax[MONEY]);
                sendRankingData(winner, moneyChayAfterTax[TAX].doubleValue(), 1);
            } else {
                List<PhomPlayer> listPlayer = getListPlayerSorted();
                //tinh tien
                for (int i = 0; i < listPlayer.size(); i++) {
                    PhomPlayer p = listPlayer.get(i);
                    /*
                     * 2 người thì ăn người thua 1 lần tiền cược
                     */
                    switch (numPlayerStart) {
                        case 2:
                            if (i == 0) {//tới 1
                                winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                previousPlayer = p;
                            } else {
                                if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                } else {
                                    winBonusMoney = Utils.add(winBonusMoney,processChayOrLose(p, MONEY_1));
                                }
                            }   
                            break;
                        case 3:
                            /**
                             * 3 người chơi, ăn người nhì 1 lần, người ba 2 lần
                             */
                            if (i == 0) {//tới 1 hoặc cháy
                                winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                previousPlayer = p;
                            } else if (i == 1) {
                                if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                } else {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrLose(p, MONEY_1));
                                }
                                previousPlayer = p;
                            } else {
                                if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                } else {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrLose(p, MONEY_2));
                                }
                            }
                            break;
                        case 4:
                            /**
                             * 4 người chơi, ăn người nhì 1 lần, người ba 2 lần,
                             * người bét 3 lần
                             */
                            if (i == 0) {
                                winBonusMoney = Utils.add(winBonusMoney,  processChayOrWin(p));
                                previousPlayer = p;
                            } else if (i == 1) {
                                if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                } else {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrLose(p, MONEY_1));
                                }
                                previousPlayer = p;
                            } else if (i == 2) {
                                if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                } else {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrLose(p, MONEY_2));
                                }
                                previousPlayer = p;
                            } else {
                                if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrWin(p));
                                } else {
                                    winBonusMoney = Utils.add(winBonusMoney, processChayOrLose(p, MONEY_3));
                                }
                            }   
                            break;
                        default:
                            break;
                    }
                }//end for tinh tien
                PhomPlayer winnerPhom=getPlayerWinner();
                if (winnerPhom != null) {
                    User userWin= getUser(winnerPhom.getSeat());
                    BigDecimal []arraResultMoney=setMoneyMinusTax(winBonusMoney, getTax());
                    winBonusMoney = arraResultMoney[MONEY];
                    winnerPhom.setBonusMoney(winBonusMoney);
                    updateMoney(userWin, winBonusMoney, CommonMoneyReasonUtils.THANG ,
                            GameLanguage.getMessage(GameLanguage.WIN,GlobalsUtil.ENGLISH_LOCALE),
                            GameLanguage.getMessage(GameLanguage.WIN,GlobalsUtil.VIETNAMESE_LOCALE),
                            GameLanguage.getMessage(GameLanguage.WIN,GlobalsUtil.CHINESE_LOCALE),
                            arraResultMoney[TAX],winnerPhom.getCurrCardsId2Array());
                    sendRankingData(userWin, arraResultMoney[TAX].doubleValue(), 1);
                    updateAchievement(userWin, CommonMoneyReasonUtils.THANG);
                }
                log.debug("Stop Game!");
            }

        } catch (Exception e) {
            log.error("New Phom processNormalWin error" + boardLog.getLog(), e);
        }finally{
            stopGame();
        }  
    }
    /**
     * Lấy log cho user thua trong trường hợp tới bình thường
     *
     * @return
     */
    private String getLogWinNormal( List<PhomPlayer> listPlayer ) {
        PhomPlayer previousPlayer = null;
        StringBuilder infoLog = new StringBuilder();
        try {
            //tinh tien
            for (int i = 0; i < listPlayer.size(); i++) {
                PhomPlayer p = listPlayer.get(i);
                /*
                 * 2 người thì ăn người thua 1 lần tiền cược
                 */
                if (numPlayerStart == 2) {
                    if (i == 0) {//tới 1
                        addLogChayOrWin(p, infoLog);
                        previousPlayer = p;
                    } else {
                        if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                            addLogChayOrWin(p, infoLog);
                        } else {
                            addLogChayOrLose(p, MONEY_1, infoLog);
                        }
                    }
                } else if (numPlayerStart == 3) {
                    /**
                     * 3 người chơi, ăn người nhì 1 lần, người ba 2 lần
                     */
                    if (i == 0) {//tới 1 hoặc cháy
                        addLogChayOrWin(p, infoLog);
                        previousPlayer = p;
                    } else if (i == 1) {
                        if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                            addLogChayOrWin(p, infoLog);
                        } else {
                            addLogChayOrLose(p, MONEY_1, infoLog);
                        }
                        previousPlayer = p;
                    } else {
                        if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                            addLogChayOrWin(p, infoLog);
                        } else {
                            addLogChayOrLose(p, MONEY_2, infoLog);
                        }
                    }
                } else if (numPlayerStart == 4) {
                    /**
                     * 4 người chơi, ăn người nhì 1 lần, người ba 2 lần, người
                     * bét 3 lần
                     */
                    if (i == 0) {
                        addLogChayOrWin(p, infoLog);
                        previousPlayer = p;
                    } else if (i == 1) {
                        if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                            addLogChayOrWin(p, infoLog);
                        } else {
                            addLogChayOrLose(p, MONEY_1, infoLog);
                        }
                        previousPlayer = p;
                    } else if (i == 2) {
                        if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                            addLogChayOrWin(p, infoLog);
                        } else {
                            addLogChayOrLose(p, MONEY_2, infoLog);
                        }
                        previousPlayer = p;
                    } else {
                        if (previousPlayer != null && (previousPlayer.isIsChay() || previousPlayer.isIsLeaved())) {
                            addLogChayOrWin(p, infoLog);
                        } else {
                            addLogChayOrLose(p, MONEY_3, infoLog);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("getLogWinNormal() erro: ", e);
        }
        return infoLog.toString();
    }
    
    private void addLogChayOrWin(PhomPlayer p,StringBuilder logWinner){
//        User user=this.getUser(p.getSeat());
//         if (p.isIsLeaved()) {
//            //do tiền bonus của thằng thoát là âm nên trả về -
//            logWinner.append(String.format("- %s: %s (Rời bàn) .\n",user.getName(),getMoneyText(-p.getBonusMoney().doubleValue()))) ;
//        }
//        if (p.isIsChay()) {
//            double money = Math.min(getMoneyFromUser(user).doubleValue(), getMoney().doubleValue() * MONEY_PENALTY_CHAY);
//            logWinner.append(String.format("- %s: %s (Cháy).\n",user.getName(),getMoneyText(-money))) ;
//        } else{
//            logWinner.append(String.format("- %s: Thắng.\n",user.getName())) ;
//        }
    }

    private void addLogChayOrLose(PhomPlayer p, BigDecimal rate, StringBuilder logWinner) {
//        double money;
//        User user=this.getUser(p.getSeat());
//        if (p.isIsLeaved()) {
//            //do tiền bonus của thằng thoát là âm nên trả về -
//            logWinner.append(String.format("- %s: %s (Rời bàn) .\n",user.getName(), getMoneyText(-p.getBonusMoney().doubleValue())));
//        }
//
//        if (p.isIsChay()) {
//            money = Math.min(getMoneyFromUser(user).doubleValue(), getMoney().doubleValue() * MONEY_PENALTY_CHAY);
//            logWinner.append(String.format("- %s: %s (Cháy).\n",user.getName(), getMoneyText(-money)));
//
//        } else {
//            money = Math.min(getMoneyFromUser(user).doubleValue(), getMoney().doubleValue() * rate.doubleValue());
//            logWinner.append(String.format("- %s: %s %s .\n", user.getName(), getMoneyText(-money), p.getInforCards()));
//        }
    }

    /**
     * xử lý reset phỏm hoặc tạo mới phỏm do client request
     *
     * @param listPhom
     * @param player
     * @param user
     */
    public void resetPhom(List<Byte> listPhom, PhomPlayer player, User user) {
        if (!isPlaying() ) {
            return;
        }
        log.debug("Đổi phỏm:" + PhomUtils.listCardToString(listPhom));
        //kiểm tra xem bài có nằm trong bộ phỏm hoặc bộ cầm trên tay ko
        List<Byte> temp = new ArrayList<>(player.getCurrCardsId());
        temp.addAll(player.getCardsIdInPhom());
        log.debug("temp =" + PhomUtils.listCardToString(temp));
        for (Byte b : listPhom) {
            if (!temp.contains(b) && b != -1) {
                sendToastMessage(PhomLanguage.getMessage(PhomLanguage.LAY_OFF_NOT_SUCCESS, this.getLocaleOfUser(user)), user, 3);
                log.debug("Doi phom khong thanh cong: " + user.getName() + " " + PhomUtils.listCardToString(listPhom) + " temp =" + PhomUtils.listCardToString(temp));
                return;
            }
        }
        temp.clear();
        ArrayList<Byte> list = new ArrayList<>();
        player.resetPhom();
        for (Byte b : listPhom) {
            if (b != -1) {
                list.add(b);
            } else {
                if (list.size() >= 3 && CardUtil.isPhomList(list)
                        && (new HashSet<>(list)).size() == list.size()) {
                    player.addCardPhoms(list);
                }
                list.clear();
            }
        }
        processU(player, null);
    }

    /**
     * Lấy số người đã hạ phỏm
     *
     * @return
     */
    public int getNumHaPhom() {
        int count = 0;
        for (int i=0;i< this.players.length;i++) {
            User user= getUser(i);
            if(user==null){
                continue;
            }
            if (players[i].isIsHa() && !players[i].isIsLeaved()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void startGame() {
        try {
            //trường hợp user ko đủ tiền start bàn
            for (int i = 0; i < this.players.length; i++) {
                User user = getUser(i);
                BigDecimal minJoin = Utils.multiply(getMoney(),  new BigDecimal(String.valueOf(getMinJoinGame())));
                if (user != null && getMoneyFromUser(user).compareTo(minJoin) < 0) {
                    sendCanNotJoinMessage(user,getMinJoinGame());
                    return;
                }
            }
            //chọn người đi đầu tiên
            if (winner != null) {
                firstHa = winner;
            } else {
                firstHa = this.getOwner();
            }
            
            setCurrentPlayer(firstHa);
            log.debug("=======start game =============");
            log.debug("start game getOwner(): " + this.getOwner());
            log.debug("start game winner: " + winner);
            log.debug("start game firstHa: " + firstHa);
            log.debug("=======end start game =============");

            if (firstHa == null) {
                return;
            }
            reset();
            //chia bài
            chiaBai();
            //bắt đầu ván mới
            super.startGame();
            //gửi message start
            sendStartMessage();
            
            proceesU2Khan();
            if (isPlaying()) {
                setStateGame(this.getWaittingGameState());
                setCurrentMoveTime();
            }
            sendStartGameViewerMessge();
        } catch (Exception e) {
            log.error("NewPhom error: " + boardLog.getLog(), e);
        }
    }

    public User getWinner() {
        return winner;
    }

    /**
     * Kiểm tra bài của player có ù hay ko
     *
     * @param p
     * @return
     */
    private void processU(PhomPlayer p, PhomPlayer lastMovePlayer) {
        try {
            if (!isPlaying() || isUWin) {
                return;
            }
            if (isDenBai(p, lastMovePlayer)) {
                p.setIsU(true);
                winner = getUser(p.getSeat());
                sendU2Message(p, U2);
                processU2Den(p, lastMovePlayer);
            } else if (p.getCardPhom().size() == 3 || p.getCurrCards().isEmpty()) {
                p.setIsU(true);
                winner = getUser(p.getSeat());
                sendU2Message(p, U0);
                processU2Win(p, U0);
            }
        } catch (Exception e) {
            log.error("processU:" + boardLog.getLog(), e);
        }
    }

    private int getNumPlayerToU() {
        return numPlayerStart - countLeave - 1;
    }

    /**
     * Xử lý tiền khi có thằng ù
     */
    private void processU2Win(PhomPlayer playerU, byte typeU) {
        try {
            if (!isPlaying() || isUWin) {
                return;
            }
            
            String winnerIdDB = getIdDBOfUser(winner);
            BigDecimal money = BigDecimal.ZERO;
            for (int i=0;i< this.players.length;i++) {
                User user= this.getUser(i);
                if(user==null){
                    continue;
                }
                if(!isInturn(user)){
                    continue;
                }
                //thoát rồi thì lấy số tiền âm cộng cho thằng ù
                if (this.players[i].isIsLeaved()) {
                    money = Utils.add(money, this.players[i].getBonusMoney().negate());
                    continue;
                }
                if (!this.players[i].isIsU() && !this.players[i].isIsThuaU()) {
                    BigDecimal moneyPhatU = Utils.multiply(getMoney(), MONEY_U);
                               moneyPhatU = moneyManagement.getCanWinOrLoseMoney(winnerIdDB, moneyPhatU);
                               
                               moneyPhatU = getMoneyFromUser(user).min(moneyPhatU);
                               moneyPhatU = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(user), moneyPhatU);
                    money = Utils.add(money, moneyPhatU);
                    //Trừ tiền mấy thằng bị ù
                    this.players[i].setBonusMoney(moneyPhatU.negate());
                    
                    updateMoney(user, moneyPhatU.negate(), CommonMoneyReasonUtils.THUA_U,
                            PhomLanguage.getMessage((typeU == U1) ? PhomLanguage.LOSE_RUMMY_KHAN : PhomLanguage.LOSE_RUMMY, GlobalsUtil.ENGLISH_LOCALE),
                            PhomLanguage.getMessage((typeU == U1) ? PhomLanguage.LOSE_RUMMY_KHAN : PhomLanguage.LOSE_RUMMY, GlobalsUtil.VIETNAMESE_LOCALE),
                            PhomLanguage.getMessage((typeU == U1) ? PhomLanguage.LOSE_RUMMY_KHAN : PhomLanguage.LOSE_RUMMY, GlobalsUtil.CHINESE_LOCALE),
                            BigDecimal.ZERO,this.players[i].getCurrCardsId2Array());
                    this.players[i].setIsThuaU(true);
                }
            }

            BigDecimal []arrResultMoney = setMoneyMinusTax(money, getTax());
            playerU.setBonusMoney(arrResultMoney[MONEY]);
            
            PhomPlayer phomWin=getPhomPlayer(winner);
            List<Short> arrayCardIDS = phomWin==null? new ArrayList<>() : getPhomPlayer(winner).getCurrCardsId2Array();
            updateMoney(winner, arrResultMoney[MONEY], CommonMoneyReasonUtils.U ,
                   PhomLanguage.getMessage((typeU == U1) ? PhomLanguage.RUMMY_KHAN : PhomLanguage.RUMMY, GlobalsUtil.ENGLISH_LOCALE),
                   PhomLanguage.getMessage((typeU == U1) ? PhomLanguage.RUMMY_KHAN : PhomLanguage.RUMMY, GlobalsUtil.VIETNAMESE_LOCALE),
                   PhomLanguage.getMessage((typeU == U1) ? PhomLanguage.RUMMY_KHAN : PhomLanguage.RUMMY, GlobalsUtil.CHINESE_LOCALE),
                   arrResultMoney[TAX],arrayCardIDS);

            this.isUWin = true;
            sendRankingData(winner, arrResultMoney[TAX].doubleValue(), 1);
        } catch (Exception e) {
            log.error("processU2Win: " + boardLog.getLog(), e);
        }
        stopGame();
    }

    /**
     * xử lý thằng ù đền
     *
     * @param p
     * @param lastMovePlayer
     */
    private void processU2Den(PhomPlayer p, PhomPlayer lastMovePlayer) {
        if (!isPlaying() || isUWin) {
            return;
        }
        try {
            String winnerIdDB = getIdDBOfUser(winner);
            User userLastMove=this.getUser(lastMovePlayer.getSeat());
            BigDecimal moneyU = Utils.multiply(getMoney(), MONEY_U);
                       moneyU = moneyManagement.getCanWinOrLoseMoney(winnerIdDB, moneyU);
                       
            BigDecimal money = Utils.multiply(new BigDecimal(String.valueOf(getNumPlayerToU())), moneyU);
            money = getMoneyFromUser(userLastMove).min(money);
            money = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(userLastMove), money);
             
            BigDecimal []arrResultMoney = setMoneyMinusTax(money, getTax());
            
            //xử thằng thua
            lastMovePlayer.setBonusMoney(money.negate());
            updateMoney(userLastMove, money.negate(), CommonMoneyReasonUtils.DEN,
                    PhomLanguage.getMessage(PhomLanguage.BEING_HIT_CONTINUOUSLY, GlobalsUtil.ENGLISH_LOCALE),
                    PhomLanguage.getMessage(PhomLanguage.BEING_HIT_CONTINUOUSLY, GlobalsUtil.VIETNAMESE_LOCALE), 
                    PhomLanguage.getMessage(PhomLanguage.BEING_HIT_CONTINUOUSLY, GlobalsUtil.CHINESE_LOCALE), 
                    BigDecimal.ZERO,lastMovePlayer.getCurrCardsId2Array());
            
            p.setBonusMoney(arrResultMoney[MONEY]);
            
            PhomPlayer phomWin=getPhomPlayer(winner);
            List<Short> arrayCardIDS= phomWin==null? new ArrayList<>() : getPhomPlayer(winner).getCurrCardsId2Array();
            updateMoney(winner, arrResultMoney[MONEY], 
                    CommonMoneyReasonUtils.THANG_DEN_BAI, 
                    PhomLanguage.getMessage(PhomLanguage.HIT_COTINUOUSLY,GlobalsUtil.ENGLISH_LOCALE), 
                    PhomLanguage.getMessage(PhomLanguage.HIT_COTINUOUSLY, GlobalsUtil.VIETNAMESE_LOCALE), 
                    PhomLanguage.getMessage(PhomLanguage.HIT_COTINUOUSLY, GlobalsUtil.CHINESE_LOCALE), 
                    arrResultMoney[TAX],arrayCardIDS);
            sendRankingData(winner, arrResultMoney[TAX].doubleValue(), 1);
            updateAchievement(winner, CommonMoneyReasonUtils.THANG);
            //set mấy thằng còn lại bị ù hết
            for (int i=0;i< this.players.length;i++) {
                User user =this.getUserPlaying(i);
                if (user == null) {
                    continue;
                }
                //Trừ tiền mấy thằng bị ù
                if (!this.players[i].isIsU()) {
                    this.players[i].setIsThuaU(true);
                }
                if(!Utils.isEqual(user, winner) && !this.players[i].equals(lastMovePlayer)){
                    updateLogGameForUser(user,CommonMoneyReasonUtils.THUA, this.players[i].getCurrCardsId2Array());
                }
            }
            this.isUWin = true;
        } catch (Exception e) {
            log.error("processU2Den error " + p, e);
        }
        stopGame();
    }

    @Override
    public synchronized void stopGame() {
        if (!isPlaying()) {
            return;
        }
        try {
            sendFinishMessage();
        } catch (Exception e) {
            this.log.error("PHOM stopgame() error: ", e);
        }finally{
            super.stopGame();
            for (User user: getPlayersList()) {
                kickNoActionUser(user);
            }
        }
        if (isShuffleRoom() && getPlayersList().size() > 1) {
            doShuffle();
        } else {
            processCountDownStartGame();
        }
    }
 
    /**
     * kiem tra den bai
     *
     * @param p
     * @param playerLast
     * @return
     */
    private boolean isDenBai(PhomPlayer p,PhomPlayer playerLast) {
        if (playerLast == null) {
            return false;
        }
        //phải 3 người trở lên mới xét đền
        if(countInturnPlayer()<3){
            return false;
        }
        //nếu bài bị ăn giống bài dc ăn
        if (playerLast.getCardBeEated().size() == 3 && p.getNumCardsEat() == 3) {
            if (playerLast.getCardBeEated().containsAll(p.getCardEat())) {
                return true;
            }
        }
        return false;
    }

    /**
     * so tien tra ve khi an ga
     *
     * @param cardeat: bai an
     * @param soLuot
     * @return
     */
    private BigDecimal eatCardMoney(int soLuot) {
        BigDecimal eatCardMoney = BigDecimal.ZERO;
        switch (soLuot) {
            case 1:
                eatCardMoney = Utils.multiply(getMoney(), MONEY_1);
                break;
            case 2:
                eatCardMoney = Utils.multiply(getMoney(), MONEY_2) ;
                break;
            case 3:
                eatCardMoney = Utils.multiply(getMoney(), MONEY_3);
                break;
            case 4:
                eatCardMoney = Utils.multiply(getMoney(), AN_CHOT);
                break;
        }
        
        eatCardMoney = eatCardMoney.min(getMoneyFromUser(userLastMove));
        eatCardMoney = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(userLastMove), eatCardMoney);
        
        return eatCardMoney;
    }
    
    /**
     * Lấy ra reason id khi ăn gà
     * @param soLuot
     * @return 
     */
    private int[] getEatCardReasonId(int soLuot) {
        int[] reasonId = new int[]{-1,-1};
        switch (soLuot) {
            case 1:
                reasonId[0] = CommonMoneyReasonUtils.AN_GA_1;
                reasonId[1] = CommonMoneyReasonUtils.BI_AN_GA_1;
                break;
            case 2:
                reasonId[0] = CommonMoneyReasonUtils.AN_GA_2;
                reasonId[1] = CommonMoneyReasonUtils.BI_AN_GA_2;
                break;
            case 3:
                reasonId[0] = CommonMoneyReasonUtils.AN_GA_3;
                reasonId[1] = CommonMoneyReasonUtils.BI_AN_GA_3;
                break;
            case 4:
                reasonId[0] = CommonMoneyReasonUtils.AN_CHOT_HA;
                reasonId[1] = CommonMoneyReasonUtils.BI_AN_CHOT_HA;
                break;
        }
        return reasonId;
    }

    /**
     * tra ve chuoi mo ta sau khi an bai
     *
     * @param cardeat
     * @param soLuot
     * @param user
     * @param locale
     * @return
     */
    public String desc(int cardeat, int soLuot, User user, Locale locale) {
        String resultAn = "";
        String resultBiAn = "";
        switch (soLuot) {
            case 1:
                resultAn += PhomLanguage.getMessage(PhomLanguage.LAY_OFF_RND_1, locale);
                resultBiAn += PhomLanguage.getMessage(PhomLanguage.LAID_OFF_RND_1, locale);
                break;
            case 2:
                resultAn += PhomLanguage.getMessage(PhomLanguage.LAY_OFF_RND_2, locale);
                resultBiAn += PhomLanguage.getMessage(PhomLanguage.LAID_OFF_RND_2, locale);
                break;
            case 3:
                resultAn += PhomLanguage.getMessage(PhomLanguage.LAY_OFF_RND_3, locale);
                resultBiAn += PhomLanguage.getMessage(PhomLanguage.LAID_OFF_RND_3, locale);
                break;
            case 4:
                resultAn += PhomLanguage.getMessage(PhomLanguage.HITTING_A_CARD, locale);
                resultBiAn += PhomLanguage.getMessage(PhomLanguage.BEING_HIT, locale);
                break;
        }

        if (user == getCurrentPlayer()) {
            return resultAn;
        } else {
            return resultBiAn;
        }
    }

    /**
     * tự động đánh ra con bài lớn nhất
     *
     * @param p
     */
    private void autoMove(PhomPlayer p, User user) {
        move(p, p.getBiggestCurrentCard().getId(), user);
    }

    /**
     * tự động lấy bài
     *
     * @param user
     */
    private void autoGetCard(User user) {
        getCard(user, true);
    }

    /**
     * Tự động hạ phỏm
     *
     * @param player
     */
    private void autoHaPhom(PhomPlayer player) {
        this.game.trace("PHOM HẠ PHỎM : "+this.getUser(player.getSeat()).getName());
        try {
            if (!player.isIsHa()) {
                if (player.getCardPhom().isEmpty()) {//client gửi lên ko có phỏm
                    player.setIsChay(true);
                } else {
                    for (Phom phom : player.getCardPhom()) {
                        phom.setIsHa(true);
                    }
                }
                sendHaPhomMessage(player);
                sendListAddCards(player);
                //set đã hạ phỏm
                player.setIsHa(true);
            }
        } catch (Exception e) {
            log.debug("autoHaPhom: " + boardLog.getLog(), e);
        }
    }

    @Override
    public void update() {
        try {
//            this.game.trace("PHOM UPDATE-----------------");
            super.update();
            if (isCanStart()) {
                startGame();
                return;
            }
            if (isPlaying() && getCurrentPlayer() != null && isTimeout()) {
                checkNoActionNotBetGame(getCurrentPlayer());
                User currentUser = getCurrentPlayer();
                PhomPlayer currentPlayer = getPhomPlayer(currentUser);
                if (!currentPlayer.isIsLeaved()) {
                    if (!currentPlayer.isIsGotCard()) {
                        autoGetCard(currentUser);
                    }
                    //nếu là vòng hạ phỏm thì hạ phỏm
                    if (currentPlayer.numCardMove() == 3) {
                        autoHaPhom(currentPlayer);
                    }
                    //đánh con chốt
                    autoMove(currentPlayer, getCurrentPlayer());
                }
            }
        } catch (Exception e) {
            log.error("NewPhomGame update error" + boardLog.getLog(), e);
        }
    }

    @Override
    public synchronized boolean join(User player, String pwd) {
        try {
            if (player == null) {
                return false;
            }
            if (!super.join(player, pwd)) {
                return false;
            }
            if (isPlaying() && getSeatNumber(player) != -1) {
                SFSObject ob= this.messageFactory.getPlayingMessage((getPlayingTime()/1000), getIdDBOfUser(getCurrentPlayer()), getIdDBOfUser(firstHa));
                sendUserMessage(ob, player);
            }
            processCountDownStartGame();
            return true;
        } catch (Exception e) {
            log.error("New Phom join error" + boardLog.getLog(), e);
        }
        return false;
    }
    
    @Override
    public synchronized boolean joinShuffle(User player) {
        try {
            if (player == null) {
                return false;
            }
            if (!super.joinShuffle(player)) {
                return false;
            }
            if (isPlaying() && getSeatNumber(player) != -1) {
                SFSObject obj = this.messageFactory.getPlayingMessage((getPlayingTime()/1000), getIdDBOfUser(getCurrentPlayer()), getIdDBOfUser(firstHa));
                sendUserMessage(obj , player);
            }
            processCountDownStartGame();
            player.removeProperty(UserInforPropertiesKey.ON_SHUFFLE);
            return true;
        } catch (Exception e) {
            log.error("joinShuffle error" + boardLog.getLog(), e);
        }
        return false;
    }
    
    /**
     * Khi người có vòng đánh cuối đánh lá thứ 3 là bắt đầu tính "ăn chốt".
     *
     * @param playerLastMove
     * @return
     */
    private byte getSizePlayerLastMove(PhomPlayer playerLastMove) {
        if (playerLastMove.getCardMove().size() == 3) {
            int count = 0;
            for (int i=0;i<this.players.length;i++) {
                User user= this.getUser(i);
                if(user==null){
                    continue;
                }
                if (this.players[i].equals(playerLastMove)) {
                    continue;
                }
                if (this.players[i].getCardMove().size() == 3) {
                    count++;
                }
            }
            //ăn chốt khi người có vòng đánh cuối đánh lá thứ 3(tất cả đều 3 lá)
            if (count == getListPlayerSorted().size() - 1) {
                return 4;
            }
        }
        return (byte) playerLastMove.getCardMove().size();
    }

    /**
     * Send message để update tiền cho user trong trường hợp đang chơi user
     * thoát bàn
     *
     * @param user
     * @param money
     * @param descEn
     * @param descVi
     */
    private void sendMessageUpdateMoneyUserLeave(User user, double money, String descEn, String descVi, String descZh) {
        SFSObject mess;
        String idDB = getIdDBOfUser(user);
        //bo cuoc phai gui lai up message update money vi sendToAllWithLocale ko co player da leave
        if (getLocaleOfUser(user).equals(GlobalsUtil.VIETNAMESE_LOCALE)) {
            mess = getBonusMoney(idDB, money, descVi);
            sendUserMessage(mess, user);
        } else if (getLocaleOfUser(user).equals(GlobalsUtil.ENGLISH_LOCALE)) {
            mess = getBonusMoney(idDB, money, descEn);
            sendUserMessage(mess, user);
        }else{
            mess = getBonusMoney(idDB, money, descZh);
            sendUserMessage(mess, user);
        }
    }
     /**
     * Kiểm tra :
     * nếu số money >0 thêm vào dấu "+" trước money
     * nếu số money <0 thêm vào dấu "-" trước money
     * @return 
     */
    private String getMoneyText(double money){
        return (money>0?"+"+money:money)+"" ;
    }

    /**
     * sort kết quả danh sách player theo score
     * @return 
     */
    private List<PhomPlayer> getListPlayerSorted(){
        List<PhomPlayer> listPlayer= new ArrayList<>();
        for(int i=0;i<this.players.length;i++){
            User user= this.getUser(i);
            if(user==null || !isInturn(user)){
                continue;
            }
            listPlayer.add(this.players[i]);
        }
        Collections.sort(listPlayer);
        return listPlayer;
    }

    @Override
    protected void nextTurn() {
        super.nextTurn();
        setStateGame(this.getWaittingGameState());
    }

    @Override
    public void processMessage(User player, ISFSObject sfsObj) {
        super.processMessage(player, sfsObj);
        try {
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            int sNum = getSeatNumber(player);
            if (!isInturn(player)) {
                return;
            }
            if (sNum < 0) {
                return;
            }
            if (idAction != PhomCommand.RESET_PHOM_EAT) {
                resetNoActionTime(player);
            }
            PhomPlayer currentPlayer = getPhomPlayer(player);
            BigDecimal moneyOfUser=getMoneyFromUser(player);
            switch (idAction) {
                case SFSAction.MOVE:
                    try {
                        if (!isPlaying() || !Utils.isEqual(player, getCurrentPlayer())) {
                            break;
                        }
                        byte idCard = sfsObj.getByte(SFSKey.INFOR_CARD);
                        move(currentPlayer, idCard, player);
                        List<Short> bList = new ArrayList<>();
                        bList.add((short) idCard);
                        addBoardDetail(player, CommonMoneyReasonUtils.MOVE, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0,0, bList);
                        boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "move", idCard, "Cards:" + currentPlayer.getStringCards());
                    } catch (Exception e) {
                       log.error("PHOM move erro:",e);
                    }
                    break;
                case PhomCommand.EAT_CARD:
                    try {
                        if (!isPlaying() || !Utils.isEqual(player, getCurrentPlayer())) {
                            break;
                        }
                        List <Short> arrayCards = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
                        byte cardEat = arrayCards.get(0).byteValue();
                        List<Byte> listCards = new ArrayList<>();
                        for (int i = 0; i < arrayCards.size(); i++) {
                            listCards.add(arrayCards.get(i).byteValue());
                        }
                        //log.debug("Process an bai:" + PhomUtils.getCardName(cardEat) + " to " + PhomUtils.listCardToString(listCards));
                        Collections.sort(listCards);

                        PhomPlayer playerLastMove = getPhomPlayer(userLastMove);
                        //kiểm tra bài ăn hợp lệ
                        Set<Byte> setCards = new HashSet<>(listCards);
                        if (!canChangePhom(currentPlayer, listCards)
                                || setCards.size() < listCards.size()) {
                            sendToastMessage(PhomLanguage.getMessage(PhomLanguage.INVALID_CARDS,getLocaleOfUser(player)), player, 3);
                            break;
                        }

                        //xử lý bài bị ăn
                        playerLastMove.addCardBeEated(cardEat);
                        currentPlayer.addCardEat(cardEat);
//                     log.debug("Bai danh ra: " + getPhomPlayer(userLastMove.getUserId()).numCardMove() + ", bai bi an: " + getPhomPlayer(userLastMove.getUserId()).numCardBeEated());
                        //nếu 2 card1 card2 đã có trong phỏm thì remove khỏi phỏm
                        for (byte b : listCards) {
                            currentPlayer.removePhomByCardId(b);
                            //xóa bài phỏm khỏi bài hiện tại
                            currentPlayer.removeCurrCard(b);
                        }
                        //bỏ phỏm mới vô list phỏm của player
                        currentPlayer.addCardPhoms(listCards);

                        //xu ly tien sau khi an va gui message bonus money
                        /**
                         * cách tính : ăn lá bài thứ 1 tính ăn gà lượt 1, ăn lá
                         * bài thứ 2 tính ăn gà lượt 2, ko tính theo vòng đánh
                         * nữa. TH đặc biệt ăn chốt: ăn lá bài ở vòng 4, ko tính
                         * là ăn con thứ mấy
                         */
                        int eatTurn = getSizePlayerLastMove(playerLastMove) < 4 ? currentPlayer.getCardEat().size() :AN_CHOT.intValue();
                        BigDecimal money = eatCardMoney(eatTurn);
                        int []reasonId = getEatCardReasonId(eatTurn);
                        ///////thông báo thằng bị ăn
                        String resultBiAnVi = desc(cardEat, eatTurn, userLastMove, GlobalsUtil.VIETNAMESE_LOCALE);
                        String resultBiAnEn = desc(cardEat, eatTurn, userLastMove, GlobalsUtil.ENGLISH_LOCALE);
                        String resultBiAnZh = desc(cardEat, eatTurn, userLastMove, GlobalsUtil.CHINESE_LOCALE);
                        List<Short> cardEats = new ArrayList<>();
                        cardEats.add((short)cardEat);
                        
                        money = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(player), money);
                        money = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(userLastMove), money);
                        
                        updateMoney(userLastMove, money.negate(), reasonId[1], resultBiAnEn, resultBiAnVi, resultBiAnZh, BigDecimal.ZERO, cardEats);
                        //thông báo thằng dc ăn
                        String resultAnEn = desc(cardEat, eatTurn, player, GlobalsUtil.ENGLISH_LOCALE);
                        String resultAnVi = desc(cardEat, eatTurn, player, GlobalsUtil.VIETNAMESE_LOCALE);
                        String resultAnZh = desc(cardEat, eatTurn, player, GlobalsUtil.CHINESE_LOCALE);
                        BigDecimal []arrResultMoney=setMoneyMinusTax(money, getTax());
                        money = arrResultMoney[MONEY];
                        //cap nhat tien cho newphomplayer
                        updateMoney(player, money, reasonId[0], resultAnEn, resultAnVi, resultAnZh, arrResultMoney[TAX], cardEats);

                        //xoa con bai bi an
                        playerLastMove.removeCardMove(cardEat);
                        ///xoay rác
                        xoayRac();
                        //ăn bài coi như bốc bài rồi
                        currentPlayer.setIsGotCard(true);
                        sendEatCardMessage();
                        processU(currentPlayer, playerLastMove);
                        boardLog.addLog(player.getName(),getMoneyFromUser(player).doubleValue(), "eat", money.doubleValue(), "Card eat:" + cardEat);
                    } catch (Exception e) {
                        log.error("PHOM eat card erro: ", e);
                    }

                    break;
                case PhomCommand.GET_CARD:
                    try {
                        if (!isPlaying() || !Utils.isEqual(player, getCurrentPlayer())) {
                            break;
                        }
                        this.game.trace("PHOM GET_CARD-----------------");
                        getCard(player, false);
                        processU(currentPlayer, null);
                    } catch (Exception e) {
                       log.error("PHOM get cards erro:",e);
                    }
                    break;
                case PhomCommand.HA_PHOM:
                    try {
                        List <Short> list = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
                        List<Byte> listCards =new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            byte idCard = list.get(i).byteValue();
                            listCards.add(idCard);
                        }
                        
                        haPhom(currentPlayer, listCards, player);
                        processU(currentPlayer, getPhomPlayer(userLastMove));
                        addBoardDetail(player, CommonMoneyReasonUtils.HA_PHOM, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(),0,0,list);
                        boardLog.addLog(player.getName(),getMoneyFromUser(player).doubleValue(), "ha phom", 0, "Cards:" + currentPlayer.getStringCards());
                    } catch (Exception e) {
                       log.error("PHOM ha phom erro:",e);
                    }
                    break;
                case PhomCommand.ADD_CARD:
                     try {
                        PhomPlayer phomPlayer = getPhomPlayer(player);
                        List <Short> arrayCards = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
                        List<Byte> addCards =new ArrayList<>();
                        for (int i = 0; i < arrayCards.size(); i++) {
                            addCards.add(arrayCards.get(i).byteValue());
                        }
                        addCards(addCards, player, phomPlayer, false);
                        addBoardDetail(player, CommonMoneyReasonUtils.SEND_CARD, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0,0,arrayCards);
                        processU(currentPlayer, null);
                    } catch (Exception e) {
                        log.error("PHOM add card erro:",e);
                    }
                    break;
                case PhomCommand.RESET_PHOM_EAT:
                    try {
                        List <Short> list = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
                        List<Byte> listCardsPhom = new ArrayList<>();
                        for (short idCArd : list) {
                            byte idCard = (byte)idCArd;
                            listCardsPhom.add(idCard);
                        }
                        if (listCardsPhom.size() > 0) {
                            resetPhom(listCardsPhom, currentPlayer, player);
                        } else {
                            boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "reset phom eat", 0, "Cards:" + currentPlayer.getStringCards());
                        }
                    } catch (Exception e) {
                        log.error("RESET_PHOM_EAT erro:", e);
                    }

                    boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "reset phom eat", 0, "Cards:" + currentPlayer.getStringCards());
                    break;

            }
        } catch (Exception e) {
            this.game.trace("PHOM processMessage() erro: ",e);
        }
    }
    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(PhomConfig.getInstance().getMaxViewer());
    }

    @Override
    public void onReturnGame(User user) {
        super.onReturnGame(user);
        try {
            PhomPlayer p = getPhomPlayer(user);
            if (p == null) {
                return;
            }
            if (p.isIsLeaved()) {
                return;
            }
            if (Utils.isEqual(user, firstHa)) {
                firstHa = user;
            }
            if (Utils.isEqual(user, lastMoveUser)) {
                lastMoveUser = user;
            }
            if (isPlaying()) {
                SFSObject ob = messageFactory.getOnreturnGameMessage(getTimeLimit() / 1000, getIdDBOfUser(getCurrentPlayer()),
                        getIdDBOfUser(lastMoveUser),getIdDBOfUser(firstHa), currentCardId, 
                        (numPlayerStart - countLeave), players, getTimeRemain(), p);
                sendUserMessage(ob, user);
            }
        } catch (Exception e) {
            this.log.error("Phom.onReturnGame() error: ", e);
        }
    }

    @Override
    protected byte getServiceId() {
        return Service.PHOM;
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user); 
    }
}
