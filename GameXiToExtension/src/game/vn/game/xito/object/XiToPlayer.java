/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.vn.game.xito.XiToController;
import game.vn.game.xito.message.PlayerMessageFactory;
import game.vn.game.xito.playerstate.IPlayerState;
import game.vn.game.xito.playerstate.PlayerAllInState;
import game.vn.game.xito.playerstate.PlayerLeaveState;
import game.vn.game.xito.playerstate.PlayerTurnState;
import game.vn.game.xito.playerstate.PlayerWaitingToNextTurnState;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Player game xì tố
 * @author tuanp
 */
public class XiToPlayer {

    public final static byte BET = 1;
    public final static byte RAISE = 2;
    public final static byte CALL = 3;
    public final static byte CHECK = 4;
    public final static byte ALLIN = 6;
    public final static byte FOLD = 7;
    //thời gian chờ tối đa sau khi kết thúc ván mà ko tiếp tục, đơn vị giây
    public final static byte MAX_SLEEP = 1;
    //bài của user, vòng preflop là 2 lá
    private final Map<Byte, XiToCard> holdCards = new TreeMap<>();
    //la bai nguoi choi chon lat
    private XiToCard showedCard;
    //la bai nguoi choi chon up
    private XiToCard hideCard;
    //lá bài sau mới dc nhận
    private XiToCard lastHoldCard;
    //toan bo message lien quan toi nguoi choi
    private PlayerMessageFactory playerMessageFactory;
    //đang chơi
    private final IPlayerState turnState;
    //đã tố hết
    private final IPlayerState allInState;
    //chờ đến lượt
    private final IPlayerState waitToNextTurnState;
    //leave or standup
    private final IPlayerState leaveState;
    //State initialization
    private IPlayerState state;
    //lien quan toi tien
    //so tien thang, hoac la dc tra lai trong truong hop nguoi thang bet tien it hon nguoi thua
    private BigDecimal winStack = BigDecimal.ZERO;
    private BigDecimal betStack = BigDecimal.ZERO;
    //user tuong ung voi player
    private User user;
    //xi to game controller
    private final XiToController xiToGame;
    //action cua user tuy trang trang thai
    private final Set<Byte> actions;
    //mô tả hành động sau cùng của user
    private String lastActionDesc = "";

    private static final Logger log = LoggerFactory.getLogger(XiToPlayer.class.getName());

    public XiToPlayer(User user, XiToController xiToGame) {
//        log.debug("xitoplayer name :" + user.getUsername());
        this.user = user;
        this.xiToGame = xiToGame;
        this.actions = new HashSet<>();
        playerMessageFactory = new PlayerMessageFactory(this);
        this.allInState = new PlayerAllInState(this);
        this.turnState = new PlayerTurnState(this);
        this.waitToNextTurnState = new PlayerWaitingToNextTurnState(this);
        this.leaveState = new PlayerLeaveState(this);
        //State initialization
        this.state = new PlayerTurnState(this);
    }

    public void reset() {
        this.lastHoldCard = null;
        this.winStack = BigDecimal.ZERO;
        this.betStack = BigDecimal.ZERO;
        this.showedCard = null;
        this.hideCard = null;
        this.actions.clear();
        this.holdCards.clear();
        this.lastActionDesc = "";
    }

    //**************************************************************************
    //xu ly lien quan toi set-get    
    public XiToCard getLastHoldCard() {
        return lastHoldCard;
    }

    public BigDecimal getWinStack() {
        return winStack;
    }

    public void setWinStack(BigDecimal winStack) {
        this.winStack = winStack;
    }

    public IPlayerState getAllInState() {
        return allInState;
    }

    public Set<Byte> getActions() {
        return actions;
    }

    public List<Short> getActionsToList() {
        List<Short> cardBs = new ArrayList<>();
        for (byte ac : this.actions) {
            cardBs.add((short)ac);
        }
        return cardBs;
    }

    public void addAction(Byte action) {
        this.actions.add(action);
    }

    public Map<Byte, XiToCard> getHoldCards() {
        return holdCards;
    }

    public Collection<Byte> getHoldCardsId() {
        return this.holdCards.keySet();
    }

    /**
     * Lấy danh sách card không bao gồm hide card va card null
     *
     * @return
     */
    public List<Short> getHoldCardsCheckedToList() {
        List<Short> cards = new ArrayList<>();
        for (Byte cardId : this.holdCards.keySet()) {
            if (this.holdCards.get(cardId).equals(this.getHideCard()) || this.getHideCard() == null) {
                cards.add((short)-1);
            } else {
                cards.add((short)cardId);
            }
        }
        return cards;
    }

    public List<Short> getHoldCardsIdToList() {
        List<Short> cardBs = new ArrayList<>();
        for (Byte cardId : this.holdCards.keySet()) {
            cardBs.add((short)cardId);
        }
        return cardBs;
    }
    
    public List<Short> getHidePreflogCardsIdToList() {
        List<Short> cardBs = new ArrayList<>();
        cardBs.add((short) -1);
        cardBs.add((short) -1);
        return cardBs;
    }

    public void addCard(XiToCard card) {
        this.holdCards.put(card.getId(), card);
        this.lastHoldCard = card;
    }

    public PlayerMessageFactory getPlayerMessageFactory() {
        return playerMessageFactory;
    }

    public void setPlayerMessageFactory(PlayerMessageFactory playerMessageFactory) {
        this.playerMessageFactory = playerMessageFactory;
    }

    public void setState(IPlayerState state) {
        this.state = state;
    }

    public BigDecimal getStack() {
        return xiToGame.getMoneyFromUser(user);
    }

    /**
     * trừ stack của player: <br />
     * nếu stack trừ lớn hơn stack còn lại thì set stack còn lại = 0 <br />
     * add stack vào round hiện tại và ghi log
     *
     * @param stack
     * @return 
     * @throws Exception
     */
    public boolean minusStack(BigDecimal stack) throws Exception {
        if (this.getStack().compareTo(stack) < 0) {
            return false;
        }
        xiToGame.getCurrentRound().addStack(xiToGame.getIdDBOfUser(this.getUser()), stack);
        xiToGame.setLastBetStack(stack);
        betStack = Utils.add(betStack, stack);
        return true;
    }
    public User getUser() {
        return user;
    }

    public XiToCard getShowedCard() {
        return showedCard;
    }

    public XiToCard getHideCard() {
        return hideCard;
    }

    public void setHideCard(XiToCard hideCard) {
        this.hideCard = hideCard;
    }

    public void setShowedCard(XiToCard showedCard) {
        this.showedCard = showedCard;
    }

    public IPlayerState getTurnState() {
        return turnState;
    }

    public IPlayerState getWaitToNextTurnState() {
        return waitToNextTurnState;
    }

    public IPlayerState getLeaveState() {
        return leaveState;
    }

    public XiToCard getCard(byte cardId) {
        return this.holdCards.get(cardId);
    }

    //**************************************************************************
    //end set-get
    public XiToController getGameController() {
        return this.xiToGame;
    }

    public void bet(BigDecimal stack) throws Exception {
        this.state.betting(stack);
    }

    public void raise(BigDecimal stack) throws Exception {
        this.state.raising(stack);
    }

    public void call() throws Exception {
        this.state.calling();
    }

    public void allIn() throws Exception {
        this.state.allIn();
    }

    public void check() throws Exception {
        this.state.checking();
    }

    /**
     * úp bỏ bài
     *
     * @throws Exception
     */
    public void fold() throws Exception {
        this.state.folding();
    }

    /**
     * Tra ve nhung quan bai da show
     *
     * @return
     * @throws Exception
     */
    public List<XiToCard> getShowedCards() throws Exception {
        List<XiToCard> list = new ArrayList<>();
        for (XiToCard card : holdCards.values()) {
            if (!card.equals(hideCard)) {
                list.add(card);
            }
        }
        return list;
    }

    /**
     * tra ket qua cua bai da show
     *
     * @return
     * @throws Exception
     */
    public ResultCard getResultShowedCards() throws Exception {
        return XiToCardUtil.evalCards(getShowedCards());
    }

    /**
     * Tra ve ket qua cua bai dang giu
     *
     * @return
     * @throws Exception
     */
    public ResultCard getResultCards() throws Exception {
        return XiToCardUtil.evalCards(holdCards.values());
    }

    /**
     * Trả về chuỗi bài đang giữ. Dùng để ghi log
     *
     * @return
     */
    public String getCards() {
        String str = "";
        int i = 0;
        for (XiToCard card : holdCards.values()) {
            if (i == 0) {
                str += card;
            } else {
                str += " " + card;
            }
            i++;
        }
        return str;
    }

    /**
     * Trạng thái người chơi hết stack
     *
     * @return
     */
    public boolean isAllIn() {
        return this.state.equals(this.allInState);
    }

    /**
     * Đang tham gia ván chơi
     *
     * @return
     */
    public boolean isTurn() {
        return this.state.equals(this.turnState);
    }

    /**
     * đang chờ tới lượt
     *
     * @return
     */
    public boolean isWaitingToNextTurn() {
        return this.state.equals(waitToNextTurnState);
    }

    public boolean isLeave() {
        return this.state.equals(leaveState);
    }

    public void sendNextTurnMessage() throws Exception {
        SFSObject nextTurnMessage = this.playerMessageFactory.getNextTurnMessage();
        this.xiToGame.sendAllUserMessage(nextTurnMessage);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.user);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XiToPlayer other = (XiToPlayer) obj;
        return Utils.isEqual(user, other.user);
    }

    /**
     * set những action mặc định của người tố đầu tiên trong ván
     *
     */
    public void setDefaultAction() {
        this.actions.clear();
        this.actions.add(XiToPlayer.CHECK);
        this.actions.add(XiToPlayer.BET);
        this.actions.add(XiToPlayer.ALLIN);
    }

    /**
     * print card to debug
     */
    public void printShowedCards() {
        log.debug(this.user.getName());
        try {
            log.debug("hide card: " + this.hideCard);
            log.debug("show cards: ");
            for (XiToCard card : getShowedCards()) {
                log.debug(card.toString());
            }
        } catch (Exception e) {
            log.error("printShowedCards - user:" + user.getName(), e);
        }
    }

    @Override
    public String toString() {
        return "player: " + user.getId() + "-" + user.getName();
    }

    public String getIdBDUSer(){
        return this.xiToGame.getIdDBOfUser(user);
    }
    public BigDecimal getBetStack(){
        return betStack;
    }
    
    public SFSObject getReturnMessage() throws Exception{
        return playerMessageFactory.getReturnGameMessage();
    }
   
    public void setUser(User userInput) {
        this.user = userInput;
        playerMessageFactory.setCurrentPlayer(this);
    }
}
