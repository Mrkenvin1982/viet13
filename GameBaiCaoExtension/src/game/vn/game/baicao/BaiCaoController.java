/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.card.CardUtil;
import game.vn.common.card.object.Card;
import game.vn.common.object.MoneyManagement;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.event.EventManager;
import game.vn.common.lang.GameLanguage;
import game.vn.game.baicao.lang.BaiCaoLanguage;
import game.vn.game.baicao.message.MessageFactory;
import game.vn.game.baicao.object.BaiCaoPlayer;
import game.vn.game.baicao.object.DeckTest;
import game.vn.game.baicao.object.VisualPlayer;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;

/**
 * Xử lý tất cả logic trong game
 *
 * @author tuanp
 */
public class BaiCaoController extends GameController{

    private static final int DEFAULT_CARD_NUMBER = 3;
    private final transient CardSet cardSet = new CardSet();
    private final BaiCaoPlayer[] players;
    
    private User winerUser = null;
    private final MoneyManagement managerMoney;
    private final Logger log;

    private final MessageFactory messageFactory;
    private final Random random;
    
    public BaiCaoController(Room room, BaiCaoGame game) {
        super(room, game);
        this.messageFactory = new MessageFactory(this);
        this.managerMoney = new MoneyManagement();
        players= new BaiCaoPlayer[this.room.getMaxUsers()];
        for(int i=0;i<this.players.length;i++){
           players[i]= new BaiCaoPlayer(); 
        }
        random = new Random();
        this.log=this.game.getLogger();
        
    }
    /**
     * Xử lý start game
     */
    @Override
    public void startGame() {
        try {
             //kiểm tra lại tiền của nhà cái khi start ván, và check lai tiền của nhà con
            if (!checkOwnerMoney()) {
                for (User u: getPlayersList()) {
                    checkPlayerEnoughMoney(u);
                }
                return;
            }
            this.log.debug("-------------START GAME---------------");
            super.startGame();
            resetGame();
            dealCard();
            if (bettingWhenStartGame()) {
                setCurrentMoveTime();
                processStartGame();
                setStateGame(getPlayingGameState());
            }
            updateBoardPlayingToHazelcast();
        } catch (Exception e) {
            this.log.error("startGame() erro: ", e);
        }
    }
    /**
     * Xử lý stop game
     */
    @Override
    public synchronized void stopGame() {
        this.log.debug("-------------STOP GAME---------------");
        if (!isPlaying()) {
            return;
        }
        //sử dũng để kiểm tra có đổi nhà cái hay không
        User owner = this.getOwner();
        try {
          
            for (int i=0;i<this.players.length;i++) {
                if (!this.players[i].isPlaying()) {
                    continue;
                }
                User user = this.getUser(i);
                if (user != null) {
                    sendResult(user, null);
                }          
            }
            payCashWhenStopGame();
        } catch (Exception ex) {
            this.log.error("bai cao stopGame() erro: ", ex);
        } finally {
            super.stopGame();
            for (User user : getPlayersList()) {
                checkNoActionBetGame(user);
            }
            // set lại tiền cược của user
            for (User user : this.getPlayersList()) {
                if (user != null) {
                    String idDBUser = getIdDBOfUser(user);
                    BigDecimal betMoney = getMoneyFromUser(user).min(getBettingMoney(idDBUser));
                    if (betMoney.compareTo(getBettingMoney(idDBUser)) != 0) {
                        sendBetMoneyMessage(user, betMoney.doubleValue());
                    }
                    setBettingMoney(user, betMoney);
                }
            }
            
            BigDecimal countPlayers = new BigDecimal((String.valueOf(this.getPlayersList().size() - 1)));
            BigDecimal ownerBetting = Utils.multiply(getMoney(), countPlayers);
            if (this.getOwner() != null && getMoneyFromUser(this.getOwner()).compareTo(ownerBetting) < 0) {
                processChoseNewOwner();
            }
            
            /*Kiểm tra user có đủ tiền cho ván tiếp theo
             nếu không đủ thì reset tiền cược nhà con về mức cược tối thiểu
             */
            if (!checkOwnerMoney()) {
                resetMoneyBoard();
            }
        }
        //gửi thông báo chọn nhà cái mới
        if (!Utils.isEqual(owner, this.getOwner())) {
            sendDialogInforNewOwner();
        }
        //chay countDown start ván mới
        processCountDownStartGame();
    }

    /**
     * Xử lý user leave game chổ này
     *
     * @param userLeave
     */
    @Override
    public synchronized void leave(User userLeave) {
        this.log.debug("-------------LEAVE GAME "+userLeave.getName()+"---------------");
        boolean isChangeOwner = false ;
        //lấy chủ phòng rời bài để xử lý trừ tiền
        User leaveOwner = this.getOwner();
        int seat=getSeatNumber(userLeave);
        int ownerSeat=getSeatNumber(this.getOwner()); 
        if(this.getOwner()==null || Utils.isEqual(userLeave, this.getOwner())){
            isChangeOwner=true;
            leaveOwner=userLeave;
        }
        /**
         * Trong trường hợp userLeave là nhà cái, nếu thoát bàn
         * thì this.getOwner()=null
         */
        super.leave(userLeave);
        try {
            this.log.debug(userLeave.getName() + " leave");

            if (isPlaying() && seat!=-1 && this.players[seat].isPlaying()) {
                if (isChangeOwner) {//nếu chủ bàn thoát ngang thì kết thúc ván + đền làng
                    payCashWhenOwnerLeave(leaveOwner);
                } else {//nếu dân thường thoát ngang thì nhà cái ăn hết tiền
                    this.players[seat].setPlaying(false);
                    if (countPlayingPlayer() < 2) {
                        this.players[ownerSeat].setIsWin(true);
                        // win còn dư cộng lại cho thằng chủ bàn
                        if (managerMoney.getInGameMoney().signum() > 0) {
                            caculatorForOwner(this.players[ownerSeat], leaveOwner);
                        }
                        this.players[ownerSeat].setPlaying(false);
                        stopGame();
                    }
                    //nếu số user playing = số user đã lật thì stop game
                    if (countPlayingPlayer() == numOfPlayers()) {
                        stopGame();
                    }
                }
                this.players[seat].setPlaying(false);
                
            }
            //reset lại tiền cược khi rời bàn
            managerMoney.remove(getIdDBOfUser(userLeave));
        } catch (Exception e) {
            this.log.error("Bai Cao leave error", e);
        }finally {
            forceLogoutUser(userLeave);
            //reset countDown khi chủ bàn rời khỏi bàn
            if (this.getPlayersList().size() > 1 && isChangeOwner) {
                //nếu người rời bàn là chủ bàn thì chọn lại chủ bàn
                processCountDownStartGame();
                processChoseNewOwner();
                sendDialogInforNewOwner();
            }
        }
    }
    /**
     * check va kiêm tra khi user join phòng
     *
     * @param user
     * @param pwd
     * @return 
     */
    @Override
    public synchronized boolean join(User user, String pwd) {
        boolean status = false;
        try {
            if (!super.join(user, pwd)) {
                return false;
            }
            if (processJoinBoardSuccess(user)) {
                status = true;
                updateBoardPlayingToHazelcast();
            }
        } catch (Exception e) {
            this.log.error("Bai cao join got exception: r", e);
        }

        return status;
    }
    /**
     * Xử lý tất cả các message trong board
     *
     * @param user
     * @param sfsObj
     */
    @Override
    public void processMessage(User user, ISFSObject sfsObj) {
        super.processMessage(user, sfsObj);
        try {
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            BaiCaoPlayer baicaoPlayer = this.getBaiCaoPlayerFromUser(user);
            if (baicaoPlayer == null) {
                return;
            }
            switch (idAction) {
                case SFSAction.MOVE:
                    if (isPlaying()) {//neu khong phai dang trong van khong xu ly.
                        //mac dinh luon luon nhan 3 con bai
                        List <Short> cards = new ArrayList(sfsObj.getShortArray(SFSKey.ARRAY_INFOR_CARD));
                        if(cards.size() > DEFAULT_CARD_NUMBER){
                            return;
                        }
                        if (baicaoPlayer.isHolding()) {
                            cards = getShowCards(cards, baicaoPlayer);
                            for (short id : cards) {
                                if (id != -1) {
                                    baicaoPlayer.addListShow(id);
                                } 
                            }
                            processLatBai(user, cards);
                            sendResult(user, cards);
                            
                            //xet đã lật bài hết chưa
                            if(baicaoPlayer.isShowCardsAll()){
                                baicaoPlayer.setHolding(false);
                            } 
                        }
                        
                        if (isFinish()) {
                            stopGame();
                        }
                    }
                    break;
                case SFSAction.BET:
                    //Thời gian countDown tối thiểu để đặt cược tối thiểu là 5s
                    if (this.getTimeToStart() < BaiCaoConfig.getInstance().getMinTimeSetMoney()) {
                        sendToastMessage(BaiCaoLanguage.getMessage(BaiCaoLanguage.OVER_TIME_SET_MONEY, getLocaleOfUser(user)), user, 3);
                        break;
                    }
                    // Xu ly viec dat tien cua user khong phai la cai.
                    if (Utils.isEqual(user, this.getOwner()) || isPlaying()) {
                        break;
                    } else {
                        if (baicaoPlayer.isBetted()) {
                            sendToastMessage(BaiCaoLanguage.getMessage(BaiCaoLanguage.OVER_TIME_SET_MONEY, getLocaleOfUser(user)), user, 3);
                            break;
                        }
                        BigDecimal money = new BigDecimal(String.valueOf(sfsObj.getDouble(SFSKey.MONEY_BET)));
                        money = Utils.getRoundBigDecimal(money);
                        money = checkUserSetMoney(user, money);
                        if (processSetMoney(user, money, 0)) {
                            baicaoPlayer.setBetted(true);
                        }
                        break;
                    }
            }
        } catch (Exception e) {
            this.log.error("PHOM processMessage() ERROR:", e);
        }
    }

    /**
     * Gửi kết quả bài của user về nếu user đó đã lật hết bài
     *
     * @param user
     * @param arr
     */
    private void sendResult(User user, List<Short> arr) {
        try {
            BaiCaoPlayer baicaoPlayer = getBaiCaoPlayerFromUser(user);
            if (baicaoPlayer != null && baicaoPlayer.isPlaying()) {
                int result = baicaoPlayer.getResult();
                if (arr != null) {
                    for (short id: arr) {
                        if (id == -1) {
                            return;
                        } else {
                            baicaoPlayer.addListShow(id);
                        }
                    }
                }
                SFSObject fob = this.messageFactory.getResultMesssage(result, getIdDBOfUser(user));
                sendAllUserMessage(fob);
            }
        } catch (Exception ex) {
            this.log.error("sendResult() erro: ", ex);
        }
    }

    /**
     * Lấy ra BaicaoPlayer từ user
     *
     * @param user
     * @return
     */
    private BaiCaoPlayer getBaiCaoPlayerFromUser(User user) {
        if(user==null){
            return null;
        }
        int seat= this.getSeatNumber(user);
        if(seat==-1){
            return null;
        }
        return players[seat];
    }

    /**
     * Kiểm tra card client gửi lên để show bài
     *
     * @param cardRequests
     * @param player
     * @return
     */
    private List<Short> getShowCards(List<Short> cardRequests, BaiCaoPlayer player) {
        List<Short> cards = new ArrayList<>();
        for (int i = 0; i < cardRequests.size(); i++) {
            if (cardRequests.get(i) != -1) {
                cards.add((short)player.getCard().get(i).getId());
            } else {
                cards.add((short)-1);
            }
        }
        return cards;
    }

    /**
     * Xu ly trong truong hop user lat bai gui len server.
     *
     * @param player nguoi gui.
     * @param cards nhung con bai gui len. Mac dinh luon la 3 con. <br> Neu con
     * nao chua lat thi co gia tri -1.
     */
    private void processLatBai(User user, List<Short> cards) {
        try {
            SFSObject fob = this.messageFactory.getOpenCardsMessage(getIdDBOfUser(user), cards);
            sendAllUserMessage(fob);
        } catch (Exception e) {
            this.log.error("processLatBai() erro: ", e);
        }
    }
    private void dealCard() {
        if (BaiCaoConfig.getInstance().isTest() && BaiCaoConfig.getInstance().getTestCase() > 0) {
            DeckTest deck = new DeckTest();
            deck.reset();
            List<Card> mcards = deck.getTestCase(BaiCaoConfig.getInstance().getTestCase());
            for (Card c : mcards) {
                players[0].receivedCard(c);
            }
            for (int i = 1; i < players.length; i++) {
                deck.addFullCard(players[i].getCard());
            }
        } else {
            boolean haveBot = false;
            for (int i = 0; i < players.length; i++) {
                if(isBot(getUser(i))){
                    haveBot = true;
                }
            }
            if (haveBot && isOpenBotGame() ) {
                int percent = random.nextInt(100);
                boolean bigger = false;
                if (percent < getAdvRatio()) {
                    bigger = true;
                }
                if (bigger) {//chia bài lợi thế
                    if (isBot(getOwner())) {
                        processBotIsOwner();
                    } else {
                        processBotIsNotOwner();
                    }
                } else {//chia bài bình thường
                    dealNormalCard();
                }

            } else {
                dealNormalCard();
            }
        }
        addEvent();
    }
    
    private void dealNormalCard() {
        for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
            for (int j = 0; j < this.players.length; j++) {
                this.players[j].receivedCard(cardSet.dealCard());
            }
        }
    }
    
    private void addEvent() {
        try {
            if (isEnableEvent()) {
                // check xem có user nào đủ điều kiện nhận thưởng sự kiện event Trung thu:
                for (int i = 0; i < players.length; i++) {

                    //3 cây giống nhau
                    int countSamco = CardUtil.demSamCoMB(players[i].getCard());
                    if (countSamco > 0) {
                        User u = getUser(i);
                        if (u != null) {
                            addUserGetEvent(u, EventManager.BAICAO_3_SAME_CARD, players[i].getListCardIds());
                        }
                        continue;
                    }
                    //3 tiên
                    int result = players[i].getResult();
                    if (result == 10) {
                        User u = getUser(i);
                        if (u != null) {
                            addUserGetEvent(u, EventManager.BAICAO_3_TIEN, players[i].getListCardIds());
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("Event error ", e);
        }
    }
    
    private void processBotGame() {
        List<Card> dealCards = new ArrayList<>(cardSet.getCards());
        List<Integer> botSeats = new ArrayList<>();
        for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
            for (int j = 0; j < this.players.length; j++) {
                User u = this.getUser(j);
                if(u == null || !isInturn(u)){
                    continue;
                }
                if (isBot(u)) {
                    if (!botSeats.contains(j)) {
                        botSeats.add(j);
                    }
                    continue;
                }
                this.players[j].receivedCard(dealCards.get(0));
                dealCards.remove(dealCards.get(0));
            }
        }
        for (int botSeat : botSeats) {
            int percent = random.nextInt(100);
            boolean bigger = false;
            if (percent < getAdvRatio()) {
                bigger = true;
            }
            
            List<Card> cards = getCardsForBot(bigger, dealCards, getAdvMinPoint());
            if (cards.isEmpty()) {
                for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
                    this.players[botSeat].receivedCard(dealCards.get(0));
                    dealCards.remove(dealCards.get(0));
                }
            } else {
                for (Card card : cards) {
                    this.players[botSeat].receivedCard(card);
                }
            }
        }
    }
    
    /**
     * Xử lý lợi thế chia bài trong trường hợp bot is owner
     * 
     */
    private void processBotIsOwner() {
        List<Card> dealCards = new ArrayList<>(cardSet.getCards());
        //danh sách user không phải bot
        List<Integer> seats = new ArrayList<>();
        BigDecimal balancerMoney = BigDecimal.ZERO;
        int ownerSeat =-1;
        
        //chia bài cho bot con
        for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
            for (int j = 0; j < this.players.length; j++) {
                User u = this.getUser(j);
                if (u == null || !isInturn(u)) {
                    continue;
                }

                if(Utils.isEqual(u, getOwner())){
                    ownerSeat = j;
                    continue;
                }
                
                if (isBot(u)) {//chia bài cho bot con
                    this.players[j].receivedCard(dealCards.get(0));
                    dealCards.remove(dealCards.get(0));
                    continue;
                }
                if (!seats.contains(j)) {
                    seats.add(j);
                    balancerMoney = Utils.add(balancerMoney, getBettingMoney(getIdDBOfUser(u)));
                }
            }
        }
        
        /**
         * không có user
         */
        if (seats.isEmpty()) {
            for (int j = 0; j < DEFAULT_CARD_NUMBER; j++) {
                this.players[ownerSeat].receivedCard(dealCards.get(0));
                dealCards.remove(dealCards.get(0));
            }
            return;
        }
        
        //số tiền nhà cái có thể thắng để hòa
        balancerMoney = Utils.divide(balancerMoney, new BigDecimal("2.0"));

        /**
         * Lấy danh sách user thua
         */
        List<Integer> loserSeats = new ArrayList<>();
        //trộn danh sách user không phải bot
        Collections.shuffle(seats);
        BigDecimal sumMoney = BigDecimal.ZERO;
        for(Integer seat : seats){
             User u = this.getUser(seat);
             sumMoney = Utils.add(sumMoney, getBettingMoney(getIdDBOfUser(u)));
             loserSeats.add(seat);
             if(sumMoney.compareTo(balancerMoney) >=0){
                 break;
             }  
        }
        
        /**
         * danh sách các tụ bài tạo sẵn de map vao cho user và bot cái
         */
        List<VisualPlayer> visualPlayers = new ArrayList<>();
        //j <= seats.size() : cộng thê bot cái
        for (int i = 0; i <= seats.size(); i++) {
            VisualPlayer p = new VisualPlayer();
             for (int j = 0; j < DEFAULT_CARD_NUMBER; j++) {
                 p.addCards(dealCards.get(0));
                 dealCards.remove(dealCards.get(0));
             }
             p.setResult(getResult(p.getCards()));
             visualPlayers.add(p);
        }
        
        //sort bai theo thứ tự tăng dần từ nhỏ tới lớn
        Collections.sort(visualPlayers);
        
        int loserMaxResult =0;
        //chia cho user thua
         for(Integer seat : loserSeats){
             VisualPlayer p = visualPlayers.get(0);
             players[seat].getCard().addAll(p.getCards());
             visualPlayers.remove(p);
             seats.remove(seat);
             if(p.getResult() > loserMaxResult){
                 loserMaxResult = p.getResult();
             }
         }
         
         boolean dealed =false;
         //chia bài loi thế cho owner đảm bảo lớn hơn loser, trộn bài
         Collections.shuffle(visualPlayers);
         for(VisualPlayer p: visualPlayers){
             if(p.getResult() > loserMaxResult){
                 players[ownerSeat].getCard().addAll(p.getCards());
                 visualPlayers.remove(p);
                 dealed = true;
                 break;
             }
         }
         
         
         //add owner seat vào danh sách còn lại nếu chưa chia bài
        if (!dealed) {
            seats.add(ownerSeat);
        }

         //chia cho user thua
         for(Integer seat : seats){
             players[seat].getCard().addAll(visualPlayers.get(0).getCards());
             visualPlayers.remove(visualPlayers.get(0));
         }
        
    }
    
    /**
     * Xử lý lợi thế chia bài trong  trường hợp owner không phải là bot
     */
    private void processBotIsNotOwner(){
        List<Card> dealCards = new ArrayList<>(cardSet.getCards());
        //danh sách bot con
        List<Integer> seats = new ArrayList<>();
        BigDecimal balancerMoney = BigDecimal.ZERO;
        int ownerSeat =-1;
        
        //chia bài cho nhà con khong phải bot trong bàn
        for (int i = 0; i < DEFAULT_CARD_NUMBER; i++) {
            for (int j = 0; j < this.players.length; j++) {
                User u = this.getUser(j);
                if (u == null || !isInturn(u)) {
                    continue;
                }

                if(Utils.isEqual(u, getOwner())){
                    ownerSeat = j;
                    continue;
                }
                
                if (!isBot(u)) {//chia bài cho bot con
                    this.players[j].receivedCard(dealCards.get(0));
                    dealCards.remove(dealCards.get(0));
                    continue;
                }
                if (!seats.contains(j)) {
                    seats.add(j);
                    balancerMoney = Utils.add(balancerMoney, getBettingMoney(getIdDBOfUser(u)));
                }
            }
        }

        
         /**
         * không có bot con
         */
        if (seats.isEmpty()) {
            for (int j = 0; j < DEFAULT_CARD_NUMBER; j++) {
                this.players[ownerSeat].receivedCard(dealCards.get(0));
                dealCards.remove(dealCards.get(0));
            }
            return;
        }
        
        //số tiền bot con có thể thắng để hòa
        balancerMoney = Utils.divide(balancerMoney, new BigDecimal("2.0"));
        
         /**
         * Lấy danh sách bot thắng
         */
        List<Integer> winnerSeats = new ArrayList<>();
        //trộn danh sách bot con
        Collections.shuffle(seats);
        BigDecimal sumMoney = BigDecimal.ZERO;
        for(Integer seat : seats){
             User u = this.getUser(seat);
             sumMoney = Utils.add(sumMoney, getBettingMoney(getIdDBOfUser(u)));
             winnerSeats.add(seat);
             if(sumMoney.compareTo(balancerMoney) >=0){
                 break;
             }  
        }
        
        /**
         * danh sách các tụ bài tạo sẵn de map vao cho user và bot cái
         */
        List<VisualPlayer> visualPlayers = new ArrayList<>();
        //j <= seats.size() : cộng thê bot cái
        for (int i = 0; i <= seats.size(); i++) {
            VisualPlayer p = new VisualPlayer();
             for (int j = 0; j < DEFAULT_CARD_NUMBER; j++) {
                 p.addCards(dealCards.get(0));
                 dealCards.remove(dealCards.get(0));
             }
             p.setResult(getResult(p.getCards()));
             visualPlayers.add(p);
        }
        
        //sort bai theo thứ tự tăng dần từ nhỏ tới lớn
        Collections.sort(visualPlayers);
        
        int winnerMinResult = visualPlayers.size() >0 ? visualPlayers.get(visualPlayers.size() -1).getResult() : 0 ;
        //chia cho user thắng
         for(Integer seat : winnerSeats){
             int index = visualPlayers.size() -1;
             VisualPlayer p = visualPlayers.get(index);
             players[seat].getCard().addAll(p.getCards());
             visualPlayers.remove(p);
             seats.remove(seat);
             if(p.getResult() < winnerMinResult){
                 winnerMinResult = p.getResult();
             }
         }
         
         
         boolean dealed =false;
         //chia bài cho owner đảm bảo nhỏ hơn winners, trộn bài
         Collections.shuffle(visualPlayers);
         for(VisualPlayer p: visualPlayers){
             if(p.getResult() < winnerMinResult){
                 players[ownerSeat].getCard().addAll(p.getCards());
                 visualPlayers.remove(p);
                 dealed = true;
                 break;
             }
         }
         
         //add owner seat vào danh sách còn lại nếu chưa chia bài
        if (!dealed) {
            seats.add(ownerSeat);
        }
         
         //chia cho user thua
         for(Integer seat : seats){
             players[seat].getCard().addAll(visualPlayers.get(0).getCards());
             visualPlayers.remove(visualPlayers.get(0));
         }
        
    } 
    
    /**
     * Tìm ra 3 card có điểm lớn hơn point limit config
     * @return 
     */
    private List<Card> getCardsForBot(boolean bigger, List<Card> dealCards, int minPoint) {
        List<Card> mcards = new ArrayList<>();
        int sizeCard = dealCards.size();//card con lai chua dung
        if (!bigger) {
            for (int i = 0; i < sizeCard; i++) {
                for (int j = i + 1; j < sizeCard; j++) {
                    for (int k = j + 1; k < sizeCard; k++) {
                        mcards.clear();
                        Card card1 = dealCards.get(i);
                        Card card2 = dealCards.get(j);
                        Card card3 = dealCards.get(k);
                        
                        mcards.add(card1);
                        mcards.add(card2);
                        mcards.add(card3);

                        if (getResult(mcards) < minPoint) {
                            dealCards.remove(card1);
                            dealCards.remove(card2);
                            dealCards.remove(card3);
                            return mcards;
                        }
                       
                    }
                }
            }
        } else {
            for (int i = 0; i < sizeCard; i++) {
                for (int j = i + 1; j < sizeCard; j++) {
                    for (int k = j + 1; k < sizeCard; k++) {
                        mcards.clear();
                        Card card1 = dealCards.get(i);
                        Card card2 = dealCards.get(j);
                        Card card3 = dealCards.get(k);
                        
                        mcards.add(card1);
                        mcards.add(card2);
                        mcards.add(card3);

                        if (getResult(mcards) >= minPoint) {
//                            log.info(getStringCardList(mcards));
                            dealCards.remove(card1);
                            dealCards.remove(card2);
                            dealCards.remove(card3);
                            return mcards;
                        }
                    }
                }
            }
        }

        return mcards;
    }
    
    public String getStringCardList(List<Card> cards){
        String result = "";
        for (Card c: cards){
            result+= getStringCard(c) + " ";
        }
        return result;
    }
    
    /**
     * lấy quân bài ở dạng text để ghi log
     *
     * @param card
     * @return
     */
    public String getStringCard(Card card) {
        String strCard = "";
        if (card == null){
            return strCard;
        }
        
        switch (card.getCardNumber()) {
            case 0:
                strCard = "A";
                break;
            case 1:
                strCard = "2";
                break;
            case 2:
                strCard = "3";
                break;
            case 3:
                strCard = "4";
                break;
            case 4:
                strCard = "5";
                break;
            case 5:
                strCard = "6";
                break;
            case 6:
                strCard = "7";
                break;
            case 7:
                strCard = "8";
                break;
            case 8:
                strCard = "9";
                break;
            case 9:
                strCard = "10";
                break;
            case 10:
                strCard = "J";
                break;
            case 11:
                strCard = "Q";
                break;
            case 12:
                strCard = "K";
                break;
        }

        switch (card.getCardType()) {
            case 0:
                strCard += " bích";
                break;
            case 1:
                strCard += " chuồn";
                break;
            case 2:
                strCard += " rô";
                break;
            case 3:
                strCard += " cơ";
                break;
        }
        return strCard;
    }
    
     /**
     * Tinh diem 3 la bai.
     *
     * @param cards
     * @return diem so cua 3 la bai. bu la 0 <br> 3 cao la 10 diem.
     */
    public int getResult(List<Card> cards) {
        int result = 0;
        int nCount = 0;
        for (int i = 0; i < cards.size(); i++) {
            int number = cards.get(i).getCardNumber() + 1;
            if (number >= 11) {//11= con tien
                nCount++;
            }
        }

        if (nCount == 3) {  //ba tien
            return 10;
        } else {
            for (int i = 0; i < cards.size(); i++) {
                int number = cards.get(i).getCardNumber() + 1;
                if (number < 11) {//11= con tien
                    result += number;
                }
            }
            if (result >= 10) {
                result = result%10;
            }
        }
        return result;
    }

    private void processStartGame() {
        try {
            for (int i=0;i< players.length;i++) {
                User user = this.getUser(i);
                if (isInturn(user)) {
                    players[i].setPlaying(true);
                    players[i].setHolding(true);
                    SFSObject fObject = this.messageFactory.getStartGameMessage(getPlayingTime()/1000, players[i].card2List(),getPlayerInturnIds());
                    sendUserMessage(fObject, user);
                }
            }
            sendStartGameViewerMessge();
        } catch (Exception e) {
            this.log.error("processStartGame() erro: ", e);
        }
    }

    /**
     * Xu ly trong truong hop ket thuc 1 van choi.<br> So bai thang cai voi tung
     * thang dan.
     */
    private void processStopGame() {
        try {
            SFSObject fObject = this.messageFactory.getStopGameMessage(players);
            sendAllUserMessage(fObject);
        } catch (Exception ex) {
            this.log.error("processStopGame() erro: ", ex);
        }
    }

    /**
     * Xu ly trong truong hop join board thanh cong. <br> Neu dang choi hien
     * thong tin nhung nguoi dang cho va bai cua no.
     *
     * @param user nguoi moi vua join.
     */
    private boolean processJoinBoardSuccess(User user) {
        try {
            int nSeat = getSeatNumber(user);
            if (nSeat > -1 && nSeat < this.room.getMaxUsers()) {
                players[nSeat].resetGame();
            } else {
                return false;
            }
            if (isPlaying()) {
                //goi thong tin cua no cho tat ca
                SFSObject fObject = this.messageFactory.getPlayingMessage(players,user);
                this.sendUserMessage(fObject, user);
            }

            // set tien mac dinh cho no.
            if (!processSetMoney(user, getMoney(), 1)) {
                addToWaitingUserList(user, String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.OWNER_NOT_ENOUGH_WIN, getLocaleOfUser(user)), getCurrency(getLocaleOfUser(user))));
                return false;
            }
            processCountDownStartGame();
            return true;
        } catch (Exception e) {
            this.log.error("Cao process join board error", e);
        }
        return false;
    }
    @Override
    public Room getRoom() {
        return room;
    }

    private void resetGame() {
        try {
            winerUser = null;
            cardSet.xaoBai();
            for (BaiCaoPlayer baiCaoPlayer: players) {
                baiCaoPlayer.resetGame();
            }
            managerMoney.resetMoneyIngame();
        } catch (Exception e) {
            this.log.error("bai cao reset error", e);
        }
    }

    private void payCashWhenOwnerLeave(User owner) {
        // cộng win cho nhà con
        for(User user : this.getPlayersList()) {
            if (user != null) {
                BaiCaoPlayer baicaoPlayer = getBaiCaoPlayerFromUser(user);
                if (baicaoPlayer != null && !Utils.isEqual(user, owner)&& baicaoPlayer.isPlaying()) {
                    //nhà con ăn tiền nhà cái
                    BigDecimal nMoney = getUpdateMoney(user, -1, 0);
                    BigDecimal []arrResultMoney = getMoneyMinusTax(user, nMoney);
                    
                    BigDecimal moneyCheck = Utils.subtract(managerMoney.getInGameMoney(), nMoney);
                    if (moneyCheck.signum() < 0) {
                        this.log.debug("congTien error username " + user.getName() + " " + " inGameMoney " + managerMoney.getInGameMoney() + " " + user.getName() + " " + nMoney);
                        nMoney = BigDecimal.ZERO;
                    }

                    updateMoney2WithLocale(user, arrResultMoney[MONEY], "", "", "", CommonMoneyReasonUtils.THANG, arrResultMoney[TAX],baicaoPlayer.card2List());
                    baicaoPlayer.setPlaying(false);
                    managerMoney.addInGameMoney(nMoney.negate());
                    baicaoPlayer.setIsWin(true);
                    updateAchievement(user, CommonMoneyReasonUtils.THANG);
                }
            }
        }
        BaiCaoPlayer playerOwner = getBaiCaoPlayerFromUser(owner);
        if (playerOwner != null) {
            // win còn dư cộng lại cho thằng chủ bàn
            if (managerMoney.getInGameMoney().signum() > 0) {
                caculatorForOwner(getBaiCaoPlayerFromUser(owner), owner);
            }
            playerOwner.setPlaying(false);
        }
        stopGame();
    }

    /**
     * Tiền user đặt cược trước khi start ván
     *
     * @param user
     * @param nMoney
     */
    private void setBettingMoney(User user, BigDecimal nMoney) {
        if (user != null) {
            BaiCaoPlayer baicaoPlayer = getBaiCaoPlayerFromUser(user);
            //kiểm tra nếu là dân thì chỉ được xét cược trước ván
            if (baicaoPlayer != null && !baicaoPlayer.isPlaying()) {
                managerMoney.bettingMoney(getIdDBOfUser(user), nMoney);
            }
        }
    }

    public BigDecimal getBettingMoney(String idDBUser) {
        return managerMoney.getBettingMoney(idDBUser);
    }

    /**
     * Đếm số user còn lại trong game(không tính user rời bàn)
     *
     * @param userLeave
     * @return
     */
    private int countPlayingPlayer() {
        int countUserPlaying = 0;
        for (int i=0;i<players.length;i++) {
            User user = getUser(i);
            if (user != null && players[i].isPlaying()) {
                countUserPlaying++;
            }
        }
        return countUserPlaying;
    }

    /**
     * tính tiền thắng-thua-hoa cho owner
     *
     * @param str
     * @param playerOwner
     * @param owner
     */
    private void caculatorForOwner(BaiCaoPlayer playerOwner, User owner) {
        if (playerOwner == null) {
            return;
        }
        //tính tiền thắng thua của nhà cái
        if (playerOwner.isPlaying()) {
            BigDecimal money = managerMoney.getInGameMoney();
            String idDBOwner = getIdDBOfUser(owner);
            //tiền nhận được phải nhỏ hơn tổng số tiền tối đa ăn được
             BigDecimal doubleBetting = Utils.add(getBettingMoney(idDBOwner), getBettingMoney(idDBOwner));
            if (money.compareTo(doubleBetting) > 0) {
                money = BigDecimal.ZERO;
            }
            BigDecimal moneyBetOwner = getBettingMoney(idDBOwner);
            int reason = CommonMoneyReasonUtils.HOA;
            if (money.compareTo(moneyBetOwner) > 0) {//thắng
                BigDecimal []arrResultMoney = getMoneyMinusTax(owner, money);
                updateMoneyBaiCao2WithLocale(owner, arrResultMoney[MONEY], CommonMoneyReasonUtils.THANG,arrResultMoney[TAX],playerOwner.card2List());
                playerOwner.setIsWin(true);
                playerOwner.setMoneyWinLose(Utils.subtract(arrResultMoney[MONEY], moneyBetOwner));
                reason = CommonMoneyReasonUtils.THANG;
            } else if (money.compareTo(moneyBetOwner) < 0) {//thua
                if (money.signum() == 0) {
                    updateLogGameForUser(owner,CommonMoneyReasonUtils.THUA, playerOwner.card2List());
                }
                updateMoneyBaiCao2WithLocale(owner, money, CommonMoneyReasonUtils.THUA, BigDecimal.ZERO,playerOwner.card2List());
                playerOwner.setMoneyWinLose(Utils.subtract(money, moneyBetOwner));
                reason = CommonMoneyReasonUtils.THUA;
            } else {//hòa
                updateMoneyBaiCao2WithLocale(owner, money, CommonMoneyReasonUtils.HOA, BigDecimal.ZERO,playerOwner.card2List());
            }
            managerMoney.addInGameMoney(money.negate());
            updateAchievement(owner, reason);
        }
    }

    /**
     * Tính tiền thắng thua cho nhà con khi kết thúc ván
     *
     * @param user
     * @param str
     * @param nOwnerResult
     */
    private void caculatorMoneyForUser(int nOwnerResult) {
        //cộng tiền cho nhà con
        for (User user : this.getPlayersList()) {
            BaiCaoPlayer baicaoPlayer = getBaiCaoPlayerFromUser(user);
            if (baicaoPlayer != null && !Utils.isEqual(user, this.getOwner()) && user != null && baicaoPlayer.isPlaying()) {

                int nResult = baicaoPlayer.getResult();
                BigDecimal nMoney = getUpdateMoney(user, nOwnerResult, nResult);

                if (managerMoney.getInGameMoney().compareTo(nMoney) < 0) {
                    this.log.debug("congTien error nameUser " + user.getName() + " " + " inGameMoney " + managerMoney.getInGameMoney() + " " + user.getName() + " " + nMoney);
                    nMoney = BigDecimal.ZERO;
                }
                int reason = 0;
                //update lại tiền từ db
                BigDecimal []arrResultMoney = getMoneyMinusTax(user, nMoney);
                if (nResult < nOwnerResult) {     //thua nhà cái
                    this.log.debug(getTextLogUser(user));
                    updateLogGameForUser(user,CommonMoneyReasonUtils.THUA, baicaoPlayer.card2List());
                    baicaoPlayer.setMoneyWinLose(getBettingMoney(getIdDBOfUser(user)).negate());
                    reason= CommonMoneyReasonUtils.THUA;
                } else if (nResult > nOwnerResult) {    //thắng nhà cái
                    updateMoneyBaiCao2WithLocale(user, arrResultMoney[MONEY], CommonMoneyReasonUtils.THANG, arrResultMoney[TAX],baicaoPlayer.card2List());
                    managerMoney.addInGameMoney(nMoney.negate());
                    baicaoPlayer.setIsWin(true);
                    baicaoPlayer.setMoneyWinLose(Utils.subtract(arrResultMoney[MONEY], getBettingMoney(getIdDBOfUser(user))));
                    reason= CommonMoneyReasonUtils.THANG;
                } else if (nResult == nOwnerResult) {//hoà
                    updateMoneyBaiCao2WithLocale(user, arrResultMoney[MONEY], CommonMoneyReasonUtils.HOA, BigDecimal.ZERO,baicaoPlayer.card2List());
                    managerMoney.addInGameMoney(nMoney.negate());
                    reason= CommonMoneyReasonUtils.HOA;
                }
                
                sendResultWinOrLose(user, reason);
                updateAchievement(user, reason);
            }
        }
    }

    private void sendResultWinOrLose(User user, int reasonOfUser){
        try {
            int seat = getSeatNumber(user);
            if(seat == -1){
                return;
            }
            //gửi kết quả thắng thua về cho user
            int reasonIdOwner = CommonMoneyReasonUtils.HOA;
            if (reasonOfUser == CommonMoneyReasonUtils.THANG) {
                reasonIdOwner = CommonMoneyReasonUtils.THUA;
            }
            if (reasonOfUser == CommonMoneyReasonUtils.THUA) {
                reasonIdOwner = CommonMoneyReasonUtils.THANG;
            }
           
            SFSObject msg = getStatusLoseOrWinUserMessage(user, reasonOfUser);
            sendUserMessage(msg, user);
            sendUserMessage(msg, getOwner());
            
            msg = getStatusLoseOrWinUserMessage(getOwner(), reasonIdOwner);
            sendUserMessage(msg, user);
        } catch (Exception e) {
            this.log.error("BJ sendResultWinOrLose() erro:", e);
        }
        
    }
    
    /**
     * Số user đã lật bài
     *
     * @return
     */
    private int numOfPlayers() {
        int iReturn = 0;
        for (User user : this.getPlayersList()) {
            BaiCaoPlayer baicaoPlayer = this.getBaiCaoPlayerFromUser(user);
            if (baicaoPlayer == null) {
                continue;
            }
            /**
             * HoangHH: phải check thêm isInturn để tránh count những thằng vào
             * bàn khi ván đã bắt đầu
             */
            if (!baicaoPlayer.isHolding()) {
                iReturn++;
            }
        }
        return iReturn;
    }

    /**
     * Chọn chủ bàn là người có số tiền cao nhất và reset lại mức cược của nhà
     * con về mức cược của bàn
     */
    private void processChoseNewOwner() {
        User userOwner = null;
        BigDecimal maxMoney = getMoney();
        //chọn user có số tiền lớn nhất
        for (User user : this.getPlayersList()) {
            if (user == null) {
                continue;
            }

            if (getMoneyFromUser(user).compareTo(maxMoney) > 0) {
                userOwner = user;
                maxMoney = getMoneyFromUser(user);
            }
        }
        BigDecimal countPlayers = new BigDecimal((String.valueOf(this.getPlayersList().size() - 1)));
        BigDecimal moneyCheck = Utils.multiply(getMoney(), countPlayers);
        if (userOwner == null || (getMoneyFromUser(userOwner).compareTo(moneyCheck) < 0)) {
            //kick hết tất cả user ra khỏi bàn
            for (User user : this.getPlayersList()) {
                kickUser(user, String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.OWNER_NOT_ENOUGH_WIN,
                        getLocaleOfUser(user)), getCurrency(getLocaleOfUser(user))));
            }
            return;
        }
        //xet lại owner
        setOwner(userOwner);
        resetMoneyBoard();
        resetAllPlayerBet();
    }

    private BigDecimal[] getMoneyMinusTax(User user, BigDecimal nMoney) {
        BigDecimal []resultMoney = new BigDecimal[2];
        resultMoney[TAX]= BigDecimal.ZERO;
        BigDecimal moneyCanWin =Utils.subtract(nMoney, getBettingMoney(getIdDBOfUser(user)));
        //user thắng thì log tax va tính tax dựa trên tiền thực tế user ăn được
        if (moneyCanWin.signum() > 0) {
            resultMoney = setMoneyMinusTax(moneyCanWin,getTax());
            resultMoney[MONEY]= Utils.add(resultMoney[MONEY], getBettingMoney(getIdDBOfUser(user)));
        } else {
            //user hòa hoặc thua thì không log va tính thuế
            resultMoney[MONEY] = nMoney;
        }
        return resultMoney;
    }

    /**
     * Gọi hàm này khi tính tiền khi thắng,thua, hòa của nhà con
     *
     * @param user
     * @param nOwnerResult: kết quả bài nhà cái
     * @param userResult:kết quả bài nhà con
     * @return
     */
    public BigDecimal getUpdateMoney(User user, int nOwnerResult, int userResult) {
        BigDecimal money = BigDecimal.ZERO;
        BaiCaoPlayer player = getBaiCaoPlayerFromUser(user);

        if (player == null) {
            return money;
        }

        if (nOwnerResult == userResult) {
            money = managerMoney.updateMoney(getBettingMoney(getIdDBOfUser(user)));
        } else if (nOwnerResult < userResult){
            money = Utils.multiply(getBettingMoney(getIdDBOfUser(user)), new BigDecimal("2.00"));
            money = managerMoney.updateMoney(money);
        }
        return money;
    }
    /**
     * Xử lý đặt tiền của user trước khi start ván
     */
    private boolean bettingWhenStartGame() {
        BigDecimal ownerMoney = BigDecimal.ZERO;
        try {
            /**
             * Trừ tiền của nhà con đặt cược
             */
            for (User user: this.getPlayersList()) {
                if (isInturn(user) && !Utils.isEqual(user, this.getOwner())) {
                    BigDecimal betMoney = getBettingMoney(getIdDBOfUser(user));
                    if (!updateMoneyBaiCao2WithLocale(user, betMoney.negate(), CommonMoneyReasonUtils.DAT_CUOC, BigDecimal.ZERO, null)) {
                        String info = String.format(GameLanguage.getMessage(GameLanguage.NO_MONEY_USER, getLocaleOfUser(user)), getMinJoinGame());
                        addToWaitingUserList(user, info);
                        continue;
                    }
                    ownerMoney = Utils.add(ownerMoney, betMoney);
                    managerMoney.addInGameMoney(betMoney);
                }
            }
            /**
             * nếu kick hết user chỉ còn thằng cái thì return false de ket thuc
             * game
             */
            if (this.getPlayersList().size() <= 1) {
                return false;
            }
            /**
             * Trừ tiền nhà cái
             */
            if (!updateMoneyBaiCao2WithLocale(this.getOwner(), ownerMoney.negate(), CommonMoneyReasonUtils.CAI_DAT_CUOC, BigDecimal.ZERO,new ArrayList<>())) {
                // trả tiền lại cho nhà con 
                for (User user: this.getPlayersList()) {
                    if(managerMoney.getInGameMoney().signum() <=0){
                        break;
                    }
                    if (isInturn(user)  && !Utils.isEqual(user, this.getOwner())) {
                        BigDecimal betMoney = getBettingMoney(getIdDBOfUser(user));
                        updateMoneyBaiCao2WithLocale(user, betMoney, CommonMoneyReasonUtils.TRA_TIEN, BigDecimal.ZERO,new ArrayList<>());
                        managerMoney.addInGameMoney(betMoney.negate());
                    }
                }
                
                String info = String.format(GameLanguage.getMessage(GameLanguage.NO_MONEY_USER, getLocaleOfUser(this.getOwner())), getMinJoinOwner());
                addToWaitingUserList(this.getOwner(),  info);
                return false;
            }
            setBettingMoney(this.getOwner(), ownerMoney);
            this.log.debug("ownerMoney " + ownerMoney);
            managerMoney.addInGameMoney(ownerMoney);
            return true;
        } catch (Exception e) {
            this.log.error("bettingWhenStartGame error:", e);
        }
        return false;
    }
    private boolean checkOwnerMoney() {
        try {
            BigDecimal money = getMoneyFromUser(this.getOwner());
            BigDecimal totalUserMoney = BigDecimal.ZERO;
            if(getMoney().signum() == 0){
                return true;
            }
            if (money.compareTo(getMoney()) < 0 || (money.signum() == 0)) {
                return false;
            }
            //lấy hết tiền đặt cược của người chơi
            for(User u : this.getPlayersList()) {
                if (u != null && !Utils.isEqual(u, this.getOwner())) {
                    totalUserMoney = Utils.add(totalUserMoney, getBettingMoney(getIdDBOfUser(u)));
                }
            }
            //nếu tiền chủ bàn < tổng cược --> thông báo lỗi
            return money.compareTo(totalUserMoney) >= 0;
        } catch (Exception e) {
            this.log.error("bai cao checkOwnerMoney", e);
            return false;
        }
    }

    private void checkPlayerEnoughMoney(User bp) {
        try {
            if (getMoneyFromUser(bp).compareTo(getMoney()) < 0) {
                String info =   GameLanguage.getMessage(GameLanguage.NO_MONEY_USER, getLocaleOfUser(bp));
                info = String.format(info,getMinJoinGame());
                addToWaitingUserList(bp, info);
                return;
            }
            // set tien mac dinh cho no.
            if (!Utils.isEqual(bp, this.getOwner()) && !processSetMoney(bp, getBettingMoney(getIdDBOfUser(bp)), 1)) {
                addToWaitingUserList(bp, String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.OWNER_NOT_ENOUGH_WIN,
                        getLocaleOfUser(bp)), getCurrency(getLocaleOfUser(bp))));
            }
        } catch (Exception ex) {
            this.log.error("checkPlayerEnoughMoney error", ex);
        }
    }

    /**
     * truong hop user dan lang gui tien dat len server.
     *
     * @param user nguoi gui.
     * @param money so tien dat.
     * @param isJoining: 0: user đặt cược, 1: hệ thống xét tiền4444
     */
    private boolean processSetMoney(User user, BigDecimal money, int isJoining) {
        if (user == null) {
            return false;
        }
        boolean status = false;
        try {
            // đặt nhỏ hơn tiền phòng thì lấy tiền phòng
            if (money.compareTo(getMoney()) < 0) {
                money = getMoney();
            }

            // win đặt lớn hơn số win user đang có
            if (getMoneyFromUser(user).compareTo(money) < 0) {
                String errString = GameLanguage.getMessage(GameLanguage.SET_MONEY_7, getLocaleOfUser(user));
                errString = String.format(errString, Utils.getStringStack(getMoneyFromUser(user).doubleValue(), getMoneyType()), getCurrency(getLocaleOfUser(user)));
                sendToastMessage(errString, user, 3);
                if (getMoneyFromUser(user).compareTo(getMoney()) >= 0) {
                    money = getMoneyFromUser(user);
                }
            }
            if (money.compareTo(getMaxBoardValue(user)) > 0) {
                String err = String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.BET_MONEY,getLocaleOfUser(user)),
                       getMaxBet());
                sendToastMessage(err, user, 3);
                money = getMaxBoardValue(user);
            }
            money = money.min(getMoneyFromUser(user));
            // tinh tien thang chu ban coi co du tien chung khong
            BigDecimal tienDat = checkTienCai(user);
            BigDecimal tienCon =Utils.subtract(getMoneyFromUser(this.getOwner()), tienDat); //tiền của nhà cái sau khi trừ hết tiền cược
            if (tienCon.signum() < 0 || tienCon.compareTo(getMoney()) < 0) {
                // Cai khong du tien chung cho con
                // Reset tien cua con
                if (Utils.isEqual(user, this.getOwner())) {
                    String errString =  GameLanguage.getMessage(GameLanguage.SET_MONEY_8, getLocaleOfUser(user));
                    errString = String.format(errString, getCurrency(getLocaleOfUser(user)));
                    sendToastMessage(errString, user, 3);
                }
                money = BigDecimal.ZERO;
                return false;
            }
            if (tienCon.compareTo(money) < 0 && tienCon.compareTo(getMoney()) >= 0) {
                // Cai khong du tien dat cho thang nay.
                // user chi co the dat so tien con lai cua cai.
                String errString =  GameLanguage.getMessage(GameLanguage.SET_MONEY_9, getLocaleOfUser(user));
                errString = String.format(errString,Utils.getStringStack(tienCon.doubleValue(), getMoneyType()), getCurrency(getLocaleOfUser(user)));
                sendToastMessage(errString, user, 3);
                money = tienCon;
            }
            //cái thì tiền cược là 0
            if (Utils.isEqual(user, this.getOwner())) {     
                money = BigDecimal.ZERO;
            }
            setBettingMoney(user, money);
            if (isJoining == 0) {
                sendBetMoneyMessage(user, money.doubleValue());
            } else if (isJoining == 1) {
                for (User userGame : this.getPlayersList()) {
                    sendBetMoneyMessage(userGame, getBettingMoney(getIdDBOfUser(userGame)).doubleValue());
                }
            }
            status = true;
        } catch (Exception e) {
            this.log.error("Cao process set money error", e);
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
        BigDecimal totalMoney =  BigDecimal.ZERO;
        try {
            for (User danUser:this.getPlayersList()) {
                if (danUser != null && !Utils.isEqual(danUser, this.getOwner()) && !Utils.isEqual(danUser,user)) {
                    // cong tien nhung thang khac da dat.
                    totalMoney = Utils.add(totalMoney, getBettingMoney(getIdDBOfUser(danUser)));
                }
            }
        } catch (Exception e) {
            this.log.error("Cao check tien cai error", e);
        }
        return totalMoney;
    }

    /**
     * Cộng tiền thắng-thua-hòa cho user khi kết thúc ván
     */
    private void payCashWhenStopGame() {
        StringBuilder str = new StringBuilder();
        BaiCaoPlayer playerOwner = getBaiCaoPlayerFromUser(this.getOwner());
        winerUser = this.getOwner();
        try {
            if (playerOwner == null) {
                return;
            }
            int nOwnerResult = playerOwner.getResult();
            //cộng tiền thắng-thua-hoa cho nhà con
            caculatorMoneyForUser(nOwnerResult);
            //cộng tiền thắng-thua-hoa cho nhà cái
            caculatorForOwner(playerOwner, this.getOwner());
            // kiếm thằng winner: là thằng thắng nhiều win nhất trong ván, chỉ so sánh những thằng còn chơi
            // tiền cái ăn được của thằng rời bàn không cộng dồn vô tiền cái ăn khi kết thúc ván
            BigDecimal moneyWin = BigDecimal.ZERO;
            User userWin = null;
            for (User user : this.getPlayersList()) {
                BaiCaoPlayer baicaoPlayer = this.getBaiCaoPlayerFromUser(user);
                if (baicaoPlayer == null || !baicaoPlayer.isWin()) {
                    continue;
                }
                // nhiều user cùng thắng số tiền bằng nhau thì ko có thằng thắng nhất
                if (getBettingMoney(getIdDBOfUser(user)).compareTo(moneyWin) == 0) {
                    continue;
                }
                if (getBettingMoney(getIdDBOfUser(user)).compareTo(moneyWin) > 0) {
                    userWin = user;
                    moneyWin = getBettingMoney(getIdDBOfUser(user));
                }
            }
            winerUser = userWin;
            this.log.debug(str.toString());
        } catch (Exception ex) {
            this.log.error("Cao payCash got exception", ex);
        } finally {
            /**
             * gui message stop game về sau update money vì client gui lên
             * request min-max mức cược của user sau khi stop game nếu gửi về
             * trước thì tiền của user ván này chưa được update
             */
            processStopGame();
        }

        //reset lai playing cho player
        for (BaiCaoPlayer player : players) {
            player.setPlaying(false);
        }
    }

    /**
     * Thông tin điểm bài cào dẽ ghi log
     *
     * @param result
     * @return
     */
    private String getTextResult(int result) {
        if (result >= 10) {
            return "3 tiên";
        }
        return result + " điểm";
    }

    /**
     * Ghi log cho nhà con
     *
     * @param user
     * @return
     */
    private String getTextLogUser(User user) {
        if (user == null) {
            return "";
        }
        BaiCaoPlayer playerOwner = this.getBaiCaoPlayerFromUser(this.getOwner());
        if(playerOwner==null){
            return"";
        }
        
        BaiCaoPlayer baicaoPlayer = this.getBaiCaoPlayerFromUser(user);
        if(baicaoPlayer==null){
            return"";
        }

        String logText = "";
        if (Utils.isEqual(user, this.getOwner()) && baicaoPlayer.isPlaying()) {
            logText = " - " + this.getOwner().getName() + "(Cái): " + getTextResult(playerOwner.getResult()) + " (" + playerOwner.getStringCardList() + ") \n";
            logText += " - " + user.getName() + ": " + getTextResult(baicaoPlayer.getResult()) + " (" + baicaoPlayer.getStringCardList() + ")";
        }
        return logText;
    }

    private void sendBetMoneyMessage(User u, double money) {
        try {
            sendAllUserMessage(messageFactory.getBetMoneyMessage(u, money));
        } catch (Exception e) {
            this.log.error("sendBetMoneyMessage erro", e);
        }
    }

    /**
     * Reset tiền cược của nhà con về mức cược tối thiểu của bàn
     */
    private void resetMoneyBoard() {
        for (User user : this.getPlayersList()) {
            // set lại tiền cược của user trở về mức thấp nhất
            if (!Utils.isEqual(user, this.getOwner())) {
                setBettingMoney(user, getMoney());
                sendBetMoneyMessage(user, getMoney().doubleValue());
            }
        }
    }

    /**
     * gửi thông tin xét chủ bàn mới trong trường hợp chủ bàn trước đó không đủ
     * tiền trả cho nhà con
     */
    public void sendDialogInforNewOwner() {
        try {
            for (User user : this.getPlayersList()) {
                sendToastMessage(String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.SET_OWNER, getLocaleOfUser(user)),getUserName(getOwner())), user, 3);
            }
        } catch (Exception e) {
            this.log.error("sendDialogInforNewOwner", e);
        }
    }

    /**
     * Reset trạng thái quickplay=false cho tất cả user
     */
    private void resetAllPlayerBet() {
        for (User user : this.getPlayersList()) {
            BaiCaoPlayer baicaoPlayer = this.getBaiCaoPlayerFromUser(user);
            if (baicaoPlayer == null) {
                continue;
            }
            baicaoPlayer.setBetted(false);
        }
    }
    /**
     * Khi tạo bàn set mức cược tối thiểu nào thì mức cược tối đa cho nhà con là
     * gấp 10 lần mức tối thiểu đó VD : Tạo bàn 500k thì mức cược từ 500k-5M
     *
     * @param user
     */
    private BigDecimal checkUserSetMoney(User user, BigDecimal money) {
        if (Utils.isEqual(user, this.getOwner())) {
            return money;
        }

        BigDecimal maxVip =getMaxBoardValue(user);
        if (money.compareTo(maxVip) > 0) {
            money =  maxVip;
            String err = String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.BET_MONEY, getLocaleOfUser(user)),
                    getMaxBet());
            sendToastMessage(err, user, 3);
        }
        return money;
    }

    /**
     * Gửi thông tin ván chơi khi user reconnect tới game
     * @param user 
     */
    public void sendMessageReturn(User user) {
        try {
            SFSObject fObject = this.messageFactory.getMessageReturnMessage( players,getLocaleOfUser(user));
            sendUserMessage(fObject, user);
        } catch (Exception e) {
            this.log.error("sendMessageReturn() erro: ",e);
        }
    }
    /**
     * Mức cược tối da user có thể đặt trong board
     * dùng de kiem tra lúc user đặt cược
     * @param user
     * @return 
     */
    @Override
    protected BigDecimal getMaxBoardValue(User user) {
        BigDecimal max = Utils.multiply(new BigDecimal(String.valueOf(getMaxBet())), getMoney());
        return  max.min(getMoneyFromUser(user));
    }

    @Override
    public User getUser(int seat) {
        return super.getUser(seat); 
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user); 
    }
    
    /**
     *
     * @return
     */
    private boolean isFinish() {
        boolean isFinish = false;
        for (int i = 0; i < players.length; i++) {
            if (players[i].isHolding() && getUser(i) != null) {
                isFinish = false;
                break;
            }
            isFinish = true;
        }
        return isFinish;
    }
    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(BaiCaoConfig.getInstance().getMaxViewer());
    }
    
    @Override
    public void update() {
        try {
            super.update();
            //tự động start game
            if (isCanStart()) {
                startGame();
            }
            if (isPlaying() && isTimeout()) {
                stopGame();
            }
        } catch (Exception e) {
            log.error("Bai cao update error", e);
        }
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
            addToWaitingUserList(user, String.format(BaiCaoLanguage.getMessage(BaiCaoLanguage.OWNER_NOT_ENOUGH_WIN, getLocaleOfUser(user)), getCurrency(getLocaleOfUser(user))));
        }
    }

    @Override
    public void onReturnGame(User user) {
        try {
            super.onReturnGame(user);
            if (isPlaying()) {
                sendMessageReturn(user);
            }
        } catch (Exception e) {
            this.log.error("BaiCao.onReturnGame() error: ", e);
        }
    }

    @Override
    public User getCurrentPlayer() {
        return super.getCurrentPlayer();
    }

    @Override
    protected byte getServiceId() {
        return Service.BAI_CAO;
    }

    @Override
    protected int getFreeSeat() {
        BigDecimal tienDat = checkTienCai(getOwner());
         //tiền của nhà cái sau khi trừ hết tiền cược
        BigDecimal tienCon = Utils.subtract(getMoneyFromUser(this.getOwner()), tienDat);
        if (tienCon.signum() < 0 || tienCon.compareTo(getMoney()) < 0) {
            return 0;
        }
        return super.getFreeSeat();
    }

    protected boolean updateMoneyBaiCao2WithLocale(User user, BigDecimal value, int reasonId, BigDecimal tax, List<Short> arrayCardIds) {
        if (value.signum() == 0) {
            return true;
        }
        if (updateMoney(user, value, reasonId, tax, arrayCardIds)) {
            String idDB = getIdDBOfUser(user);
            SFSObject viMessage = this.getBonusMoney(idDB, value.doubleValue(), "");
            sendUserMessage(viMessage, user);
            return true;
        }
        return false;
    }

}
