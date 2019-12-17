/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.MauBinhCommand;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.event.EventManager;
import game.vn.common.lang.GameLanguage;
import game.vn.common.object.MoneyManagement;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.game.maubinh.utils.AutoArrangement;
import game.vn.game.maubinh.utils.GameChecker;
import game.vn.game.maubinh.lang.MauBinhLanguage;
import game.vn.game.maubinh.message.Message;
import game.vn.game.maubinh.message.MessageFactory;
import game.vn.game.maubinh.object.Cards;
import game.vn.game.maubinh.object.DeckTest;
import game.vn.game.maubinh.object.MauBinhCardSet;
import game.vn.game.maubinh.object.MauBinhType;
import game.vn.game.maubinh.object.Player;
import game.vn.game.maubinh.object.Result;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.slf4j.Logger;

/**
 *
 * @author luannv
 */
public class MauBinhGameController extends GameController {

    public static final int MONEY_MB =MONEY;
    public static final int TAX_MB=TAX;
    
    private MoneyManager moneyManager = null;
    private final transient MauBinhCardSet cardSet;
    private final Player[] players;
    private long startTime;
    private int compareTime;
    private final Logger log;
    private final Random random;
    
    private final MoneyManagement moneyManagement;

    public MauBinhGameController(Room room, MauBinhGame game) {
        super(room, game);
        this.log=this.game.getLogger();
        this.cardSet = new MauBinhCardSet();
        this.players = new Player[MauBinhConfig.DEFAULT_NUMBER_PLAYER];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player();
        }
        this.moneyManager = new MoneyManager(this.getMoney(),this);
        random = new Random();
        moneyManagement = new MoneyManagement();
    }

    @Override
    public void onReturnGame(User user) {
        if (user == null) {
            return;
        }
        super.onReturnGame(user);
        if (!isPlaying()) {
            // nếu chưa start ván thì gui ve thoi gian countdown
            if (this.getPlayerNumber() > 1) {
                sendBoardInforMessage();
            }
        }
        Player player = this.getMauBinhPlayerByUser(user);
        if (player == null) {
            return;
        }
        player.setUser(user);
        byte restTime = 0;
        if (this.isPlaying()) {
            restTime = (byte) ((getPlayingTime() - (System.currentTimeMillis() - this.startTime)) / MauBinhConfig.ONE_SECOND);
                if (restTime < 0) {
                    restTime = 0;
                }
        }

        if (this.isPlaying() == false || player.getUser() == null) {
            Message m = MessageFactory.makeInGameInforMessageForViewer(
                    (byte) (this.getTimeLimit() / MauBinhConfig.ONE_SECOND), restTime);
            super.sendUserMessage(m, user);
            return;
        }

        List<Card> cardList = player.isFinish() ? player.getCards().getArrangeCards() : player.getCards().getCards();
        // trường hợp finish nhưng chưa đủ 3 chi
        if (cardList == null || cardList.isEmpty()) {
            if (player.isFinish()) {
                cardList = AutoArrangement.getSolution(player.getCards().getCards());
                Cards cards = player.getCards();
                cards.clearArrangement();
                // cập nhật lại 3 chi, và gửi cardList mới về cho user
                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_SMALL_SET; i++) {
                    cards.receivedCardTo1stSet(cardList.get(i));
                }
                int beginset2 = MauBinhConfig.NUMBER_CARD_SMALL_SET;
                for (int i = beginset2; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset2; i++) {
                    cards.receivedCardTo2ndSet(cardList.get(i));
                }
                int beginset3 = MauBinhConfig.NUMBER_CARD_SMALL_SET + MauBinhConfig.NUMBER_CARD_BIG_SET;
                for (int i = beginset3; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset3; i++) {
                    cards.receivedCardTo3rdSet(cardList.get(i));
                }
                if (cards.isFinishArrangement()) {
                    cards.setMauBinhTypeAfterArrangement();
                }
            }
        }
        Message m = MessageFactory.makeInGameInforMessage(player.isFinish(),
                cardList, (byte) (this.getTimeLimit() / MauBinhConfig.ONE_SECOND),
                restTime, (byte) player.getCards().getMauBinhType(),players);
        super.sendUserMessage(m, user);
    }

    @Override
    public void processMessage(User player, ISFSObject message) {
        try {
            super.processMessage(player, message);

            // khong con o trong ban
            if (this.getMauBinhPlayerByUser(player) == null) {
                return;
            }

            int maubinhCommand = message.getInt(SFSKey.ACTION_INGAME);
            switch (maubinhCommand) {
                case MauBinhCommand.FINISH:
                    if (this.isPlaying()) {
                         List <Short> cards = new ArrayList(message.getShortArray(SFSKey.ARRAY_INFOR_CARD));
                         if(cards.size() > MauBinhConfig.DEFAULT_NUMBER_CARD){
                             return;
                         }
                        List<Card> listCard = new ArrayList<>();
                        for (int i = 0; i < cards.size(); i++) {
                            Card c = CardSet.getCard(cards.get(i).byteValue());
                            listCard.add(c);
                        }
                        this.processFinishCommand(player, listCard);
                    }

                    break;

                case MauBinhCommand.AUTO_ARRANGE:
                    // vẫn đang dùng vì client và server đang auto binh khác nhau
                    if (this.isPlaying()) {
                        this.processAutoArrangeCommand(player);
                    }

                    break;

                case MauBinhCommand.SORT_BY_ORDER:
                    if (this.isPlaying()) {
                        this.processSortByOrderCommand(player);
                    }

                    break;

                case MauBinhCommand.SORT_BY_TYPE:
                    if (this.isPlaying()) {
                        this.processSortByTypeCommand(player);
                    }

                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            log.error("MauBinh processMessage error: ", ex);
        }
    }
    @Override
    public void update() {
        try {
            super.update();
            if (isCanStart()) {
                startGame();
                return;
            }
            // reset lại thời gian so bài 
            if (!isPlaying() && compareTime > 0 && (System.currentTimeMillis() - getCurrentMoveTime()) > compareTime) {
                compareTime = 0;
                /**
                 * Xử những thằng ko đủ win
                 */
                for (User player : this.getPlayers()) {
                    if (!moneyManager.enoughMoneyToStart(player, getMinJoinGame())) {    
                        String errorMessage = GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_MONEY, Utils.getUserLocale(player));
                        addToWaitingUserList(player, errorMessage);
                    }
                }
                setCurrentMoveTime();
            }
            // End of playing time.
            if (this.isPlaying()) {
                if (this.isTimeout()) {
                    this.log.debug("MauBinhGameController.update() - End of playing process");
                    // hết giờ tự động binh cho user chưa finish
                    boolean havePlayerAutoFinish = false;
                    for (Player player : players) {
                        if (player.getUser() != null && isPlaying() && !player.isFinish()) {
                            player.setIsTimeOut(true);
                            List<Card> listAutoArrangeCards = processAutoArrangeCommand(player.getUser());
                            processFinishCommand(player.getUser(), listAutoArrangeCards);
                            havePlayerAutoFinish = true;
                        }
                    }
                    if (!havePlayerAutoFinish) {
                        this.processFinish();
                    }
                }
            }
        } catch (Exception e) {
            log.error("MauBinh update error: ", e);
        }
    }

    @Override
    public void startGame() {
        try {
            if (this.moneyManager != null && this.getOwner() != null
                    && this.moneyManager.enoughMoneyToStart(this.getOwner(), getMinJoinGame()) == false) {
                 String errorMessage = GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_MONEY, Utils.getUserLocale(this.getOwner()));
                errorMessage = Utils.formatedString(errorMessage, super.getCurrency(getLocaleOfUser(this.getOwner())));
                Message m = MessageFactory.makeErrorMessage(errorMessage);
                super.sendUserMessage(m, this.getOwner());

                // Set ready to false.
                for (int i = 0; i < getPlayersSize(); i++) {
                    if (players[i].getUser() != null && !Utils.isEqual(players[i].getUser(), this.getOwner())) {
                        addToWaitingUserList(players[i].getUser(), errorMessage);

                    }
                }
                return;
            }

            
            super.startGame();
            reset();

            if (isPlaying()) {
                setCurrentMoveTime();
                setStateGame(getWaittingGameState());
                sendStartGameViewerMessge();
            }
        } catch (Exception e) {
            log.error("MauBinh startGame error: ", e);
        }
    }

    private void reset() {
        // Reset limit time.
        compareTime = 0;
        this.startTime = System.currentTimeMillis();
        moneyManagement.reset();

        this.cardSet.xaoBai();
        // Delivery cards to players with START command.
        this.deliveryCard();
        // Match user-i with player-i.
        for (int i = 0; i < this.getPlayersSize(); i++) {
            User u = getUser(i);
            if (u == null) {
                continue;
            }
            moneyManagement.bettingMoney(getIdDBOfUser(u), getMoneyFromUser(u));
        }
    }
    
    @Override
    public synchronized void stopGame() {
        if (!isPlaying()) {
            return;
        }
        try {
            Message m = MessageFactory.makeStopMessage();
            super.sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("MauBinh stopGame error: ", e);
        } finally {
            super.stopGame();
            for(User user: getPlayersList()) {
                checkNoActionBetGame(user);
            }
            removeDisconnectedUser();
            setCurrentMoveTime();
            //reset lại countDown start ván
            if (this.getPlayerNumber() > 1) {
                // set lại thời gian so bài để chờ client
                compareTime = MauBinhConfig.getInstance().getCompareTime();
                if (isShuffleRoom()) {
                    doShuffle();
                } else {
                    this.processCountDownStartGame();
                }
            }
        }
    }
    
    private int countPlayerInTurn() {
        int count = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i].getUser() == null) {
                continue;
            }
            if (isInturn(players[i].getUser())) {
                count++;
            }
        }
        return count;
    }

    // Update money if user leaves.
    @Override
    public synchronized void leave(User player) {
        try {
            int seatNumber = this.getSeatNumber(player);
            int countInTurn = this.countPlayerInTurn();
            boolean isIturn = this.isInturn(player);
            super.leave(player);
            if (isPlaying() && seatNumber != -1) {
                BigDecimal penalizeLeaver = Utils.multiply(new BigDecimal(String.valueOf(MauBinhConfig.getInstance().getChiLeaveBonus())), this.getMoney());
                this.moneyManager.updateMoneyForLeave(this, seatNumber, countInTurn, this.players,penalizeLeaver);

                if (isIturn && this.canBeFinish()) {
                    for (int i = 0; i < this.players.length; i++) {
                        if (this.players[i].getUser() != null) {
                            this.players[i].setFinishFlag(true);
                        }
                    }

                    if (GameChecker.isFinishAll(this.players)) {
                        this.processFinish();
                    }
                }
            }
            //reset lại thời gian so bàn
            if (this.getPlayerNumber() < 2) {
                compareTime = 0;
            }
        } catch (Exception e) {
            log.error("MauBinh leave error: r b", e);
        } finally{
            forceLogoutUser(player);
        }
    }
    
    @Override
    public synchronized boolean join(User user, String pwd) {
        try {
            boolean isReconnect= isReconnect(user);
            if (!super.join(user, pwd)) {
                addToWaitingUserList(user, "");
                return false;
            }
            
            if(isReconnect){
                return true;
            }
            if (this.moneyManager == null || this.moneyManager.getGameMoney() != this.getMoney()) {
                this.moneyManager = new MoneyManager(this.getMoney(), this);
            }
            if (getOwner() == null) {
                return false; //fix trường hợp user join room nhanh, hoặc đang bảo trì server
            }
            byte restTime = 0;
            if (this.isPlaying()) {
                restTime = (byte) ((this.getPlayingTime()- (System.currentTimeMillis() - this.startTime)) / MauBinhConfig.ONE_SECOND);
                if (restTime < 0) {
                    restTime = 0;
                }
            }

            Message m = MessageFactory.makeTableInfoMessage(
                    (byte) (this.getPlayingTime() / MauBinhConfig.ONE_SECOND), restTime);
            super.sendUserMessage(m, user);
            this.processCountDownStartGame();
            return true;
        } catch (Exception e) {
            log.error("MAU BINH join() error: ", e);
        }
        return false;
    }
    
    @Override
    public synchronized boolean joinShuffle(User user) {
        try {
            if (!super.joinShuffle(user)) {
                addToWaitingUserList(user, "");
                return false;
            }
            
            if (this.moneyManager == null || this.moneyManager.getGameMoney() != this.getMoney()) {
                this.moneyManager = new MoneyManager(this.getMoney(), this);
            }

            if (getOwner() == null) {
                return false; //fix trường hợp user join room nhanh, hoặc đang bảo trì server
            }

            byte restTime = 0;
            if (this.isPlaying()) {
                restTime = (byte) ((this.getPlayingTime()- (System.currentTimeMillis() - this.startTime)) / MauBinhConfig.ONE_SECOND);
                if (restTime < 0) {
                    restTime = 0;
                }
            }

            Message m = MessageFactory.makeTableInfoMessage((byte) (this.getPlayingTime() / MauBinhConfig.ONE_SECOND), restTime);
            super.sendUserMessage(m, user);
            this.processCountDownStartGame();
            user.removeProperty(UserInforPropertiesKey.ON_SHUFFLE);
            return true;
        } catch (Exception e) {
            log.error("MAU BINH joinShuffle() error: ", e);
        }
        return false;
    }

    public void updateMoney(User u, BigDecimal value, List<Short> arrayIDs,BigDecimal tax) {
        int reasonId = CommonMoneyReasonUtils.HOA;
        if (value.signum()> 0) {
            reasonId = CommonMoneyReasonUtils.THANG;
            updateAchievement(u, reasonId);
        } else if (value.signum() < 0) {
            reasonId = CommonMoneyReasonUtils.THUA;
        } else {
            // Don't update money with value 0.
            updateAchievement(u, reasonId);
            this.updateLogGameForUser(u, reasonId, arrayIDs);
            return;
        }
        this.updateMoney(u, value, reasonId, tax,arrayIDs);
        
    }
    
     public void updateMoney(User u, BigDecimal value, List<Short> arrayIDs,BigDecimal tax, int reasonId) {
        this.updateMoney(u, value, reasonId, tax,arrayIDs);
    }

    public boolean updateMoneyforLeaver(User u, BigDecimal value, int reasonId, List<Short> arrayCardIds) {
        return super.updateMoney(u, value, reasonId, BigDecimal.ZERO, arrayCardIds); 
    }
    

    public void sendBonusMoneyMessage(User user, BigDecimal money, String desc) {
        SFSObject m = getBonusMoney(getIdDBOfUser(user), money.doubleValue(), desc);
        super.sendUserMessage(m, user);
    }

    public void updateMoneyForService(User u, BigDecimal value) {
        this.updateMoney(u, value,CommonMoneyReasonUtils.AUTO_BINH,BigDecimal.ZERO,null);
    }

    public BigDecimal[] getMoneyAfterTax(BigDecimal money) {
        return this.setMoneyMinusTax(money, getTax());
    }

    public int getAutoArrangementPrice() {
        return MauBinhConfig.getInstance().getAutoArrangementBinhDanPrice();
    }

    // Count number of playing user (Not finish player).
    private boolean canBeFinish() {
        if (this.players == null || this.players.length == 0) {
            return false;
        }

        int playingNo = 0;
        int notFinishNo = 0;
        for (int i = 0; i < this.players.length; i++) {
            if (this.players[i].getUser() != null) {
                if (this.players[i].isFinish() == false) {
                    notFinishNo++;
                }

                playingNo++;
            }
        }

        return (notFinishNo == 0) || (playingNo == 1);
    }

    /**
     * Delivery cards to players.
     */
    private void deliveryCard() {
        List<Integer> advantageSeats = new ArrayList<>();
        for (int i = 0; i < this.players.length; i++) {
            this.players[i].reset();
            this.players[i].setUser(getUser(i));
            User u = getUser(i);
            if (isBot(u)) {
                if (isAdvantageRatioByGroupID()) {
                    //kiem tra radom tập bot trong group vào lợi thế mới chia bài
                    int percent = random.nextInt(100);
                    if (percent < getAdvantageRatio(u)) {
                        advantageSeats.add(i);
                    }
                } else {
                    advantageSeats.add(i);
                }
            }
        }
        
        if (MauBinhConfig.getInstance().isTest() && MauBinhConfig.getInstance().getTestCase() > 0) {
            DeckTest desk= new DeckTest();
            desk.reset();
            List<Card> cards=desk.getTestCase(MauBinhConfig.getInstance().getTestCase());
            players[0].getCards().getCards().addAll(cards);
            for(int i= 0;i<players.length;i++){
                desk.addFullCard(players[i].getCards().getCards());
                this.players[i].getCards().sort();
                this.players[i].getCards().setMauBinhType();
            }
        } else {
            //mở bot và phải có bot mới chia lỡi thế
            if (!advantageSeats.isEmpty() && isOpenBotGame() ) {
                if(isAdvantageRatioByGroupID()){
                    processBotGameByGroup(advantageSeats);
                }else{
                    processBotGameNotByGroup();
                }  
            } else {
                for (int i = 0; i < this.cardSet.length(); i++) {
                    this.players[i % MauBinhConfig.DEFAULT_NUMBER_PLAYER].getCards().receivedCard(this.cardSet.dealCard());
                }
            }

        }

        // Send START command with card list.
        // Match user-i with player-i.
        for (int i = 0; i < this.getPlayersSize(); i++) {
            User p = getUser(i);
            if (p != null) {
                Message m = MessageFactory.makeStartMessage(
                        (byte) (this.getTimeLimit() / MauBinhConfig.ONE_SECOND),
                        this.players[i].getCards().getCards(),
                        (byte) this.players[i].getCards().getMauBinhType());
                super.sendUserMessage(m, p);
            }
        }

        // Check MauBinh type. If MauBinh then consider as FINISH.
        for (int i = 0; i < this.players.length; i++) {
            if (this.players[i].getCards().isMauBinh()) {
                this.players[i].setFinishFlag(true);
            }
        }

        eventForUser();
                
        if (GameChecker.isFinishAll(this.players)) {
            this.processFinish();
        }
    }
    
    /**
     * Xử lý chia bai lợi thế cho bot
     */
    private void processBotGameByGroup(List<Integer> advantageSeats) {
        Cards[] cards = new Cards[4];
        for (int i = 0; i < this.players.length; i++) {
            if (this.players[i].getUser() != null) {
                cards[i] = new Cards();
            }
        }
        for (int i = 0; i < this.cardSet.length(); i++) {
            int index = i % MauBinhConfig.DEFAULT_NUMBER_PLAYER;
            if (cards[index] == null) {
                continue;
            }
            cards[index].receivedCard(this.cardSet.dealCard());

        }

        List<Cards> sortedCards = sortedCards(cards);//index có bài lớn nhất

        //chia bài cho bot
        for (int i : advantageSeats) {
            int index = sortedCards.size() - 1;
            this.players[i].getCards().getCards().addAll(sortedCards.remove(index).getCards());
            this.players[i].getCards().sort();
            this.players[i].getCards().setMauBinhType();
        }

        //chia bài random cho user thường và bot không nằm trong lợi thế 
        for (int i = 0; i < this.players.length; i++) {
            if (this.players[i].getUser() == null) {
                continue;
            }
            if (this.players[i].getCards().getCards().isEmpty()) {
                int index = random.nextInt(sortedCards.size());
                this.players[i].getCards().getCards().addAll(sortedCards.remove(index).getCards());
                this.players[i].getCards().sort();
                this.players[i].getCards().setMauBinhType();
            }
        }
    }
    
        /**
     * Xử lý chia bai lợi thế cho bot
     */
    private void processBotGameNotByGroup() {
        int percent = random.nextInt(100);
        boolean bigger = false;
        if (percent < getAdvRatio()) {
            bigger = true;
        }
        if (bigger) {//chia bài lợi thế
            Cards[] cards = new Cards[4];
            for (int i = 0; i < this.players.length; i++) {
                if (this.players[i].getUser() != null) {
                    cards[i] = new Cards();
                }
            }
            for (int i = 0; i < this.cardSet.length(); i++) {
                int index = i % MauBinhConfig.DEFAULT_NUMBER_PLAYER;
                if (cards[index] == null) {
                    continue;
                }
                cards[index].receivedCard(this.cardSet.dealCard());

            }

            List<Cards> sortedCards = sortedCards(cards);//index có bài lớn nhất
            
            //chia bài cho bot
            for (int i = 0; i < this.players.length; i++) {
                if (this.players[i].getUser() == null) {
                    continue;
                }
                if (isBot(this.players[i].getUser())) {
                    int index = sortedCards.size() - 1;
                    this.players[i].getCards().getCards().addAll(sortedCards.remove(index).getCards());
                    this.players[i].getCards().sort();
                    this.players[i].getCards().setMauBinhType();
                }
            }
            
            //chia bài cho user thường random
            for (int i = 0; i < this.players.length; i++) {
                if (this.players[i].getUser() == null) {
                    continue;
                }
                if (!isBot(this.players[i].getUser())) {
                    int index = random.nextInt(sortedCards.size());
                    this.players[i].getCards().getCards().addAll(sortedCards.remove(index).getCards());
                    this.players[i].getCards().sort();
                    this.players[i].getCards().setMauBinhType();
                }
            }
            
        } else {//chia bài bình thường
            for (int i = 0; i < this.cardSet.length(); i++) {
                this.players[i % MauBinhConfig.DEFAULT_NUMBER_PLAYER].getCards().receivedCard(this.cardSet.dealCard());
            }
        }
    }
    
    /**
     * Tạo event cho user
     */
    private void eventForUser() {
        if (!isEnableEvent()) {
            return;
        }
        try {
            for (Player player : this.players) {
                if (player.getUser() != null) {
                    addEvent(player);
                }
            }
        } catch (Exception e) {
            log.error("Event error:", e);
        }
    }
    
    /**
     * add event cho user
     * @param player 
     */
    private void addEvent(Player player) {  
        List<Card> resultList = AutoArrangement.getSolution(player.getCards().getCards());
        if(resultList==null || resultList.isEmpty()){
            return;
        }
        Cards cards = new Cards();
        cards.setCards(resultList);
        // 3 first for 1st set.
        for (int i = 0; i < MauBinhConfig.NUMBER_CARD_SMALL_SET; i++) {
            cards.receivedCardTo1stSet(resultList.get(i));
        }
        int beginset2 = MauBinhConfig.NUMBER_CARD_SMALL_SET;
        // 5 next for 2nd set.
        for (int i = beginset2; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset2; i++) {
            cards.receivedCardTo2ndSet(resultList.get(i));
        }
        int beginset3 = MauBinhConfig.NUMBER_CARD_SMALL_SET + MauBinhConfig.NUMBER_CARD_BIG_SET;
        // 5 last for 3rd set.
        for (int i = beginset3; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset3; i++) {
            cards.receivedCardTo3rdSet(resultList.get(i));
        }
        
        player.getCards().clearArrangement();

        //thùng phá sảnh
        if (cards.haveStraightFlush()) {
            addUserGetEvent(player.getUser(), EventManager.MAUBINH_STRAIGHT_FLUSH, new ArrayList<>());
        }
       
        //có tứ quý trong list cards
        if (CardUtil.haveTuQuyInListCard(resultList)) {
            addUserGetEvent(player.getUser(), EventManager.MAUBINH_4_OF_A_KIND, new ArrayList<>());
        }  
        
        cards.sort();
        cards.setMauBinhType();
        if (cards.isMauBinh()) {
            addUserGetEvent(player.getUser(), EventManager.IS_MAUBINH, new ArrayList<>());
            if(cards.getMauBinhType() == MauBinhType.SAME_COLOR_13){
                 addUserGetEvent(player.getUser(), EventManager.MAUBINH_SAME_COLOR_13, new ArrayList<>());
            }
            if(cards.getMauBinhType() == MauBinhType.SIX_PAIR_WITH_THREE){
                 addUserGetEvent(player.getUser(), EventManager.MAUBINH_SIX_PAIR_WITH_THREE, new ArrayList<>());
            }
            if(cards.getMauBinhType() == MauBinhType.FOUR_OF_THREE){
                 addUserGetEvent(player.getUser(), EventManager.MAUBINH_4_XAM, new ArrayList<>());
            }
            if(cards.getMauBinhType() == MauBinhType.STRAIGHT_13){
                 addUserGetEvent(player.getUser(), EventManager.MAUBINH_IS_STRAIGHT_DRAGON, new ArrayList<>());
            }
        }
        
    }


    /**
     * Process FINISH command.
     *
     * @param player a User object.
     * @param dis transfer data.
     */
    private void processFinishCommand(User user, List<Card> listCards) {
        Player player = this.getMauBinhPlayerByUser(user);
        if (player == null || listCards == null) {
            return;
        }
        
        if(player.getCards().getMauBinhType()== MauBinhType.SIX_PAIR 
                || player.getCards().getMauBinhType()== MauBinhType.SAME_COLOR_13
                || player.getCards().getMauBinhType()== MauBinhType.STRAIGHT_13
                || player.getCards().getMauBinhType()== MauBinhType.SIX_PAIR_WITH_THREE
                || player.getCards().getMauBinhType()== MauBinhType.FOUR_OF_THREE){
            
            return;
            
        }

        // Get 13 cards:
        try {
            Cards cards = player.getCards();
            // Clear arrangement.
            cards.clearArrangement();

            // 3 first for 1st set.
            for (int i = 0; i < MauBinhConfig.NUMBER_CARD_SMALL_SET; i++) {
                cards.receivedCardTo1stSet(listCards.get(i));
            }
            int beginset2 = MauBinhConfig.NUMBER_CARD_SMALL_SET;
            // 5 next for 2nd set.
            for (int i = beginset2; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset2; i++) {
                cards.receivedCardTo2ndSet(listCards.get(i));
            }
            int beginset3 = MauBinhConfig.NUMBER_CARD_SMALL_SET + MauBinhConfig.NUMBER_CARD_BIG_SET;
            // 5 last for 3rd set.
            for (int i = beginset3; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset3; i++) {
                cards.receivedCardTo3rdSet(listCards.get(i));
            }

            // Send error to client if there are duplicated cards.
            if (cards.isFinishArrangement() == false) {
                String errorMessage =  MauBinhLanguage.getMessage(MauBinhLanguage.DUPLICATE_CARD, getLocaleOfUser(user)); 
                Message m = MessageFactory.makeInterfaceErrorMessage(
                        (byte) MauBinhCommand.FINISH, errorMessage);
                super.sendUserMessage(m, user);
            } else {
                cards.setMauBinhTypeAfterArrangement();
                if (!player.isTimeOut()) {
                    Message m = MessageFactory.makeFinishMessage(getIdDBOfUser(user));
                    for (int i = 0; i < this.getPlayersSize(); i++) {
                        User p = getUser(i);
                        if(p == null){
                            continue;
                        }
                        if(!isInturn(p)){
                            continue;
                        }
                        sendUserMessage(m, p);
                    }
                    
                } else {
                    // ghi thêm bài của người chơi fix lỗi tự động binh ở sv nhưng không show đc bài
                    List<Card> cardList = player.getCards().getArrangeCards();
                    if (cardList == null) {
                        cardList = player.getCards().getCards();
                    }
                    int type = player.getCards().IsFailedArrangement()
                            ? MauBinhConfig.FAILED_ARRANGEMENT : player.getCards().getMauBinhType();
                    Message m = MessageFactory.makeSendCardMessage(getIdDBOfUser(user), type, cardList);
                    super.sendUserMessage(m, user);
                }
                /**
                 * Chưa kết thúc và là 3 sảnh 3 thùng(3 sảnh, 3 thùng client gửi lên mới tính là MB)
                 */
                if(!player.isFinish() && player.getCards().isMauBinh()){
                  addUserGetEvent(user, EventManager.IS_MAUBINH, new ArrayList<>());  
                }
                player.setFinishFlag(true);
                
                if (GameChecker.isFinishAll(this.players)) {
                    this.processFinish();
                }
            }
        } catch (Exception ex) {
            log.error("MauBinh processFinishCommand error: ", ex);
        }
    }

    /**
     * Process AUTO_ARRANGE command.
     *
     * @param player a User object.
     */
    private List<Card> processAutoArrangeCommand(User user) {
        Player player = this.getMauBinhPlayerByUser(user);
        if (player == null) {
            return null;
        }
        List<Card> resultList = new ArrayList<>();
        // Get 13 cards:
        try {
            Cards cards = player.getCards();
            resultList = AutoArrangement.getSolution(cards.getCards());
            
            // Send result to client.
            // không timeout mới gửi message cho client
            if (!player.isTimeOut()) {
                Message m = MessageFactory.makeAutoArrangeResultMessage(resultList);
                super.sendUserMessage(m, user);
            }
            if (!isBot(user)) {
                // Set flag to charge money.
                player.setAutoArrangementFlag(true);
            }
        } catch (Exception ex) {
            log.error("MauBinh processAutoArrangeCommand error: ", ex);
        }
        return resultList;
    }

    /**
     * Process SORT_BY_ORDER command.
     *
     * @param player a User object.
     */
    private void processSortByOrderCommand(User user) {
        Player player = this.getMauBinhPlayerByUser(user);
        if (player == null) {
            return;
        }

        // Get 13 cards:
        try {
            Cards cards = player.getCards();
            List<Card> resultList = AutoArrangement.sortCardByOrder(cards.getCards());

            // Send error to client.
            Message m = MessageFactory.makeSortByOrderMessage(resultList);
            super.sendUserMessage(m, user);
        } catch (Exception ex) {
            log.error("MauBinh processSortByOrderCommand error: ", ex);
        }
    }

    /**
     * Process SORT_BY_TYPE command.
     *
     * @param player a User object.
     */
    private void processSortByTypeCommand(User user) {
        Player player = this.getMauBinhPlayerByUser(user);
        if (player == null) {
            return;
        }

        // Get 13 cards:
        try {
            Cards cards = player.getCards();
            List<Card> resultList = AutoArrangement.sortCardByType(cards.getCards());

            // Send error to client.
            Message m = MessageFactory.makeSortByTypeMessage(resultList);
            super.sendUserMessage(m, user);
        } catch (Exception ex) {
            log.error("MauBinh processSortByTypeCommand error: ", ex);
        }
    }
    
    /**
     * Compare cards of players. Calculate win or lose money.
     */
    private synchronized void processFinish() {
        if (this.players == null) {
            return;
        }
        if (!isPlaying()) {
            return;
        }

        try {
            // Compare player with player.
            Result[][] result = GameChecker.comparePlayers(this.players);

            // Calculate win chi.
            int[] winChi = GameChecker.getWinChi(this, players, result);

            double[] winMoney;

            winMoney = this.moneyManager.caculateMoneyNewFromMoneyOfUser(getMoneyUser(players), winChi);

            // Update money to DB.
            winMoney = this.moneyManager.updateMoney(this, players, winMoney, winChi, result);

            // Add bonus money from bonus chi if there is someone, who leave the game.
            this.moneyManager.addBonusMoney(this, players);

            // Update for auto arrangement using.
            winMoney = this.moneyManager.updateMoneyForAutoArrangementUsing(this, players, winMoney);

            for (int i = 0; i < this.players.length; i++) {
                if (players[i].getUser() == null) {
                    continue;
                }

                Message m = MessageFactory.makeResultMessage(i, players, winMoney, winChi, result);
                if (m == null) {
                    continue;
                }

                super.sendUserMessage(m, players[i].getUser());

                //gửi thông tin sập hầm
                Message mSapHam = MessageFactory.decriptionSapHamMessage(players[i]);
                if (mSapHam == null) {
                    continue;
                }

                super.sendUserMessage(mSapHam, players[i].getUser());
                // gửi thông tin win của user sau khi update
                m = MessageFactory.makeUserMoneyInfoMessage(getIdDBOfUser(players[i].getUser()), getMoneyFromUser((players[i].getUser())).doubleValue());
                super.sendAllUserMessage(m);
            }
        } catch (Exception e) {
            log.debug("processFinish() erro: ", e);
        } finally {
            this.stopGame();
        }
    }

    private Player getMauBinhPlayerByUser(User user) {
        int sNum = getSeatNumber(user);
        // khong con o trong ban
        if (sNum == -1 || sNum >= this.players.length) {
            return null;
        }

        return players[sNum];
    }
    /**
     * Gửi thông tin thời gian còn lại trước khi bắt đầu ván mới cho cả bàn
     *
     */
    public void sendBoardInforMessage() {
        try {
            Message m = MessageFactory.getBoardInfoMessage(getTimeToStart());
            super.sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("sendBoardInforMessage", e);
        }
    }

    /**
     * Thời gian còn lại trước khi bắt đầu ván
     *
     * @return
     */
    @Override
    public byte getTimeToStart() {
        if (!isPlaying()) {
            long time = ((System.currentTimeMillis() - getCurrentMoveTime())/1000);
            if (time == 0) {
                return (byte) (getTimeAutoStartDefault() + (compareTime/1000));
            }
            if (time < getTimeAutoStartDefault() + (compareTime/1000)) {
                return (byte) ((getTimeAutoStartDefault() + (compareTime/1000) - time));
            }
        }
        return 0;
    }

    private void removeDisconnectedUser() {
        for (User player : this.getPlayers()) {
            //trường hợp nó offline mà vẫn còn trong bàn
            if (!player.getSession().isConnected()) {
                leave(player);
            }
        }
    }

    /**
     * Trả về danh sách tiền hiện tại của user
     *
     * @param players
     * @return
     */
    private double[] getMoneyUser(Player[] players) {
        double[] moneys = new double[players.length];
        for (int i = 0; i < players.length; i++) {
            User u = players[i].getUser();
            if ( u == null) {
                continue;
            }
            BigDecimal moneyOfUser = moneyManagement.getBettingMoney(getIdDBOfUser(u)).min(getMoneyFromUser(u));
            moneys[i] = Utils.getRoundBigDecimal(moneyOfUser).doubleValue();
            
        }
        return moneys;
    }

    @Override
    public Locale getLocaleOfUser(User user) {
        return super.getLocaleOfUser(user);
    }

    @Override
    public BigDecimal getMoneyFromUser(User user) {
        return super.getMoneyFromUser(user);
    }
    private List<User> getPlayers() {
        return this.getPlayersList();
    }
    
    private int getPlayerNumber() {
        return this.getPlayers().size();
    }
    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(MauBinhConfig.getInstance().getMaxViewer());
    }

    @Override
    protected byte getServiceId() {
        return Service.MAUBINH;
    }

    @Override
    public void sendRankingData(User user, double tax, int point) {
        super.sendRankingData(user, tax, point);
    } 
    
    
    /**
     * Auto sắp xep
     * @param cards
     * @return 
     */
    private Cards[] arrangeCards(Cards cards[]) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) {
                continue;
            }
            cards[i].sort();
            cards[i].setMauBinhType();
            List<Card> resultList = AutoArrangement.getSolution(cards[i].getCards());
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            // 3 first for 1st set.
            for (int j = 0; j < MauBinhConfig.NUMBER_CARD_SMALL_SET; j++) {
                cards[i].receivedCardTo1stSet(resultList.get(j));
            }
            int beginset2 = MauBinhConfig.NUMBER_CARD_SMALL_SET;
            // 5 next for 2nd set.
            for (int j = beginset2; j < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset2; j++) {
                cards[i].receivedCardTo2ndSet(resultList.get(j));
            }

            int beginset3 = MauBinhConfig.NUMBER_CARD_SMALL_SET + MauBinhConfig.NUMBER_CARD_BIG_SET;
            // 5 last for 3rd set.
            for (int j = beginset3; j < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset3; j++) {
                cards[i].receivedCardTo3rdSet(resultList.get(j));
            }
        }

        return cards;
    }
    
    /**
     * sort bài từ nhỏ đến lớn
     * @param cards
     * @return 
     */
    private List<Cards> sortedCards(Cards cards[]){
        List<Cards>  sortedCards = new ArrayList<>();
        cards = arrangeCards(cards);
        Result[][] result = GameChecker.comparePlayers(cards);
        // Calculate win chi.
        int[] winChi = GameChecker.getWinChi(this, cards, result);
        for(int i=0; i< winChi.length ;i++){
            if(cards[i] == null){
                continue;
            }
            cards[i].setWinchi(winChi[i]);
            sortedCards.add(cards[i]);
        }
        
        Collections.sort(sortedCards);
        return sortedCards;

    }
    
    /**
     * Kiểm tra auto-range có lỗi không
     * @param resultList
     * @return 
     */
    private boolean isFailedArrangement(List<Card> resultList){
        Cards cards = new Cards();
        cards.clearArrangement();
        cards.setCards(resultList);
        // 3 first for 1st set.
        for (int i = 0; i < MauBinhConfig.NUMBER_CARD_SMALL_SET; i++) {
            cards.receivedCardTo1stSet(resultList.get(i));
        }
        int beginset2 = MauBinhConfig.NUMBER_CARD_SMALL_SET;
        // 5 next for 2nd set.
        for (int i = beginset2; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset2; i++) {
            cards.receivedCardTo2ndSet(resultList.get(i));
        }
        int beginset3 = MauBinhConfig.NUMBER_CARD_SMALL_SET + MauBinhConfig.NUMBER_CARD_BIG_SET;
        // 5 last for 3rd set.
        for (int i = beginset3; i < MauBinhConfig.NUMBER_CARD_BIG_SET + beginset3; i++) {
            cards.receivedCardTo3rdSet(resultList.get(i));
        }
        
        return cards.IsFailedArrangement();
    }
    
}
