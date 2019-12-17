/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.SFSAction;
import game.command.SamCommand;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.GameExtension;
import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.common.constant.Service;
import game.vn.common.lang.GameLanguage;
import game.vn.common.object.BoardLogInGame;
import game.vn.common.object.MoneyManagement;
import game.vn.game.sam.lang.SamLanguage;
import game.vn.game.sam.message.SamGameMessage;
import game.vn.game.sam.object.SamPlayer;
import game.vn.game.sam.object.TestDeck;
import game.vn.game.sam.utils.BoardMoneyUtil;
import game.vn.game.sam.utils.SamCardUtils;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;

/**
 *
 * @author tuanp
 */
public class SamController extends GameController{

    /**
     * Bộ bài trong game Xâm gồm 10 lá
     */
    public static final int DEFAULT_NUMBER_XAM_CARD = 10;
    /**
     * Người chiến thắng của màn chơi
     */
    private User winner;
    /**
     * Loại bài vừa được đánh ra
     */
    private int typeCard;
    /**
     * Loai bài đã đánh ra trước đó
     */
    private int typeMoved;
    /**
     * Bộ bài cho bàn
     */
    private transient CardSet cardSet;
    /**
     * Bài của lượt đánh gần nhất
     */
    private List<Card> cardMove;
    /**
     * Tiền thưởng, tiền phạt
     */
    private BigDecimal penalty = BigDecimal.ZERO;
    /**
     * Description của những khoản tiền bị phạt
     */
    private String penaltyDesVi, penaltyDesEn, penaltyDesZh;
    /**
     * Reason Id dựa theo thông tin của tiền phạt
     */
    private int reasonId = -1;
    /**
     * Số con heo đã được đánh trong 1 vòng
     */
    private int heoCount;
    /**
     * Số tứ quý đã được đánh trong 1 vòng
     */
    private int fourOfKindCount;
    /**
     * Danh sách người chơi
     */
    private final Map<String, SamPlayer> listPlayer = new HashMap<>();
    /**
     * Người chơi vừa đánh trước đó
     */
    private User playerMoved;
    /**
     * Người bắt đầu vòng mới
     */
    private User playerBeginNewRound;
    /**
     * Người bị chặt
     */
    private User playerBiChat;
    /**
     * Người đi đầu tiên của bàn
     */
    private User moveFirst;
    /**
     * User báo Xâm
     */
    private User xamUser;
    /**
     * Count số player đã báo hủy xâm
     */
    private int skipXamCount;
    /**
     * Xử lý tiền phạt
     */
    private BoardMoneyUtil boardMoney;
    /**
     * Log4j
     */
    private Logger log;
    /**
     * Log trong bàn
     */
    private BoardLogInGame boardLog;
    /**
     * Sử dụng để lưu số player rời bàn xét cho trường hợp báo xâm có thằng rời
     * bàn
     */
    private byte countPlayerLeave = 0;
    /**
     * Game Message factory
     */
    private final SamGameMessage gameMessage;
    //kiem tra đang trong thoi gian báo sâm
    private long callXamTime;
    private BigDecimal xamSuccessMoney = BigDecimal.ZERO;
    private User denBaiUser;
    //Dùng để ghi log cho game
    private String logText="";
    /**
     * danh sách nhóm bài của thằng đánh đầu tiên , để check đền bài
     */
    private final List<List<Card>> firstCardMove;
    private static Random random = new Random();
    private final MoneyManagement moneyManagement;
    
    public SamController(Room room, GameExtension gameEx) {
        super(room, gameEx);
        cardSet = new CardSet();
        boardLog = new BoardLogInGame();
        gameMessage = new SamGameMessage(this);
        firstCardMove = new ArrayList();
        cardMove=new ArrayList<>();
        log=this.game.getLogger();
        boardMoney = new BoardMoneyUtil(getMoney());
        moneyManagement = new MoneyManagement();
    }
 
    public Map<String, SamPlayer> getListPlayer() {
        return listPlayer;
    }

    public User getPlayerMoved() {
        return playerMoved;
    }

    public void setPlayerMoved(User playerMove) {
        this.playerMoved = playerMove;
    }

    public User getPlayerBeginNewRound() {
        return playerBeginNewRound;
    }

    public void setPlayerBeginNewRound(User playerBeginNewRound) {
        this.playerBeginNewRound = playerBeginNewRound;
    }

    public User getPlayerBiChat() {
        return playerBiChat;
    }

    public void setPlayerBiChat(User playerBiChat) {
        this.playerBiChat = playerBiChat;
    }

    public User getMoveFirst() {
        return moveFirst;
    }

    public void setMoveFirst(User moveFirst) {
        this.moveFirst = moveFirst;
    }

    public User getXamUser() {
        return xamUser;
    }

    public void setXamUser(User xamUser) {
        this.xamUser = xamUser;
    }

    public List<Card> getCardMove() {
        return cardMove;
    }
    public List<Short> getCardsMoveToList(){
        List<Short> arr = new ArrayList();
        for(int i=0;i<this.cardMove.size();i++){
            arr.add((short)this.cardMove.get(i).getId());
        }
        return arr;
    }

    public void setCardMove(List<Card> cardMove) {
        this.cardMove = cardMove;
    }

    public BigDecimal getPenalty() {
        return penalty;
    }

    public void setPenalty(BigDecimal penalty) {
        this.penalty = penalty;
    }

    public BoardMoneyUtil getBoardMoney() {
        return boardMoney;
    }

    public void setBoardMoney(BoardMoneyUtil boardMoney) {
        this.boardMoney = boardMoney;
    }

    /**
     * Cộng tiền phạt
     *
     * @param penalty
     */
    public void addPenalty(BigDecimal penalty) {
        this.penalty = Utils.add(this.penalty, penalty);
    }

    public String getPenaltyDesVi() {
        return penaltyDesVi;
    }

    public void setPenaltyDesVi(String penaltyDes) {
        this.penaltyDesVi = penaltyDes;
    }

    public void addPenaltyDesVi(String penaltyDes) {
        this.penaltyDesVi=this.penaltyDesVi.isEmpty()?(""):(this.penaltyDesVi+",");
        this.penaltyDesVi = this.penaltyDesVi + penaltyDes;
    }
    
    public String getPenaltyDesZh() {
        return penaltyDesZh;
    }

    public void setPenaltyDesZh(String penaltyDes) {
        this.penaltyDesZh = penaltyDes;
    }

    public void addPenaltyDesZh(String penaltyDes) {
        this.penaltyDesZh=this.penaltyDesZh.isEmpty()?(""):(this.penaltyDesZh+",");
        this.penaltyDesZh = this.penaltyDesZh + penaltyDes;
    }

    public String getPenaltyDesEn() {
        return penaltyDesEn;
    }

    public void setPenaltyDesEn(String penaltyDesEn) {
        this.penaltyDesEn = penaltyDesEn;
    }

    public void addPenaltyDesEn(String penaltyDes) {
        this.penaltyDesEn=this.penaltyDesEn.isEmpty()?(""):(this.penaltyDesEn+",");
        this.penaltyDesEn = this.penaltyDesEn + penaltyDes;
    }

    public int getTypeCard() {
        return typeCard;
    }

    public void setTypeCard(int typeCard) {
        this.typeCard = typeCard;
    }

    public int getTypeMoved() {
        return typeMoved;
    }

    public void setTypeMoved(int typeMoved) {
        this.typeMoved = typeMoved;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
    }

    public int getHeoCount() {
        return heoCount;
    }

    public void setHeoCount(int heoCount) {
        this.heoCount = heoCount;
    }

    public void increaseHeoCount() {
        this.heoCount++;
    }

    public int getFourOfKindCount() {
        return fourOfKindCount;
    }

    public void setFourOfKindCount(int fourOfKindCount) {
        this.fourOfKindCount = fourOfKindCount;
    }

    public void increaseFourOfKindCount() {
        this.fourOfKindCount++;
    }

    public SamGameMessage getGameMessage() {
        return gameMessage;
    }
    @Override
    public User getCurrentPlayer() {
        return super.getCurrentPlayer();
    }
    @Override
    public int getSeatNumber(User player) {
        return super.getSeatNumber(player);
    }

    @Override
    public int getSeatNumber(String userID) {
        return super.getSeatNumber(userID);
    }
    public double getMinBet() {
        return SamConfig.getInstance().getMinBetMoney();
    }

    /**
     * Kiểm soát việc join vào một bàn
     *
     * @param user
     * @param pwd
     * @return 
     */
    @Override
    public synchronized boolean join(User user, String pwd) {
        try {
            if (user == null) {
                return false;
            }
            if (!super.join(user, pwd)) {
                return false;
            }
            int sNum = getSeatNumber(user);
            if (sNum != -1) {
                addNewSamPlayer(user);
                return true;
            }
        } catch (Exception e) {
            log.error("XamGame.join error " + boardLog.getLog(), e);
        }
        return false;
    }

    /**
     * kiểm tra trong những người đang chơi, người nào còn 1 lá báo cho viewer
     * hoặc người reconnect
     */
    private void checkPlayerHaveOneCard() {
        for (User p : getPlayingPlayers()) {
            SamPlayer xp = getXamPlayer(p);
            if (xp.getCards().size() == 1) {
                sendNotifyOneCardMessage(getIdDBOfUser(p));
            }
        }
    }

    /**
     * Chia bài cho người chơi
     */
    private void chiaBai() {
        //Xào bài
        cardSet.xaoBai();
        if (SamConfig.getInstance().isTest() && SamConfig.getInstance().getTestCase() > 0) {
            TestDeck deck = new TestDeck();
            deck.reset();
            for (SamPlayer player : getListPlayer().values()) {
                List<Card> list1 = deck.getTest(SamConfig.getInstance().getTestCase());
                player.getCards().addAll(list1);
                deck.addFullCard(player.getCards());
                break;
            }
            
             for (SamPlayer player : getListPlayer().values()) {
                 deck.addFullCard(player.getCards());
             }
        }else{
            //Chia bài
            //Chia mỗi player 10 lá
            for (int j = 0; j < DEFAULT_NUMBER_XAM_CARD; j++) {
                for (SamPlayer player : getListPlayer().values()) {
                    player.receiveCard(cardSet.dealCard());
                    player.sortCards();
                }
            }
        }
    }

    @Override
    public void startGame() {
        try {
            log.info("-------- START GAME---------------");
            reset();
            chiaBai();

            //nếu là ván đầu tiên thì chọn user có card nhỏ nhất là người đi trước
            setMoveFirst(getFirstUser());
            setCurrentPlayer(moveFirst);
            setPlayerBeginNewRound(moveFirst);
            setPlayerMoved(moveFirst);

            super.startGame();
            sendStartMessage();
            //Check tới trắng
            processForceFinish();
            //Send message bắt đầu thời gian báo Xâm
            if (isPlaying()) {
                sendXamTimeInforMessage();
                //Bắt đầu tính thời gian lượt đi đầu tiên
                setStateGame(this.getWaittingGameState());
                setCurrentMoveTime();
            }
            sendStartGameViewerMessge();
        } catch (Exception e) {
            log.error("XamGame.startGame rror: " + boardLog.getLog(), e);
        }

    }

    /**
     * Send start message
     */
    private void sendStartMessage() {
        try {
            for (SamPlayer player : getListPlayer().values()) {
                if(isPlayerState(player.getUser())){
                   player.sendStartMessage(getPlayingTime()/1000); 
                }  
            }
        } catch (Exception e) {
            log.error("XamGame.sendStartMessage error: " + boardLog.getLog(), e);
        }
    }

    /**
     * Xử lý tới trắng
     */
    private void processForceFinish() {
        //ván có người thắng rồi thì ko tới trắng nữa
        if (!isPlaying() && winner != null) {
            return;
        }
        int biggestForce = SamCardUtils.NOTYPE;
        User userForce = null;
        int sNum = getSeatNumber(getCurrentPlayer());
        // duyệt theo vòng bắt đầu từ thằng đánh đầu tiên
        for (int i = 0; i < this.getPlayersSize(); i++) {
            sNum = (sNum + i) % this.getPlayersSize();
            User user = getUser(sNum);
            if (user != null && isInturn(sNum)) {
                SamPlayer xamPlayer = getXamPlayer(user);
                int typeForce = checkForceFinish(xamPlayer);
                if (!xamPlayer.isLeaveState() && typeForce > biggestForce) {
                    biggestForce = typeForce;
                    userForce = user;
                }
            }
        }
        if (SamCardUtils.NOTYPE != biggestForce && userForce != null) {
            setPenaltyDesVi(SamCardUtils.getTypeForceDescription(biggestForce, GlobalsUtil.VIETNAMESE_LOCALE));
            setPenaltyDesEn(SamCardUtils.getTypeForceDescription(biggestForce, GlobalsUtil.ENGLISH_LOCALE));
            setPenaltyDesZh(SamCardUtils.getTypeForceDescription(biggestForce, GlobalsUtil.CHINESE_LOCALE));
            forceFinish(userForce,biggestForce);
        }
    }

    /**
     * Kiểm tra tới trắng
     *
     * @param xamPlayer
     * @param isNewGame
     * @return
     */
    private int checkForceFinish(SamPlayer xamPlayer) {
        try {
            List<Card> cards = xamPlayer.getCards();
            if (cards.size() != DEFAULT_NUMBER_XAM_CARD) {
                return SamCardUtils.NOTYPE;
            }
            //Kiểm tra tới trắng theo kiểu nào: sảnh rồng, 5 đôi,......
            return SamCardUtils.getTypeForceFinish(cards);
        } catch (Exception e) {
            log.error("XamGame.checkForceFinish error: " + boardLog.getLog(), e);
            return SamCardUtils.NOTYPE;
        }
    }

    /**
     * Xử lý tới trắng
     *
     * @param userWinner
     * @param typeForce
     */
    public void forceFinish(User userWinner,int typeForce) {
        try {
            boardLog.addLog(userWinner.getName(),getMoneyFromUser(userWinner).doubleValue(), "forceFinish", 0);
            winner = userWinner;
            setMoveFirst(userWinner);
            setCurrentPlayer(userWinner);

            //Send message FORCE_FINISH cho toàn bộ người chơi trong bàn
            SFSObject m = getGameMessage().getForceFinishMessage(typeForce);
            sendAllUserMessage(m);

            //Xét tiền thưởng tiền phạt
            BigDecimal bonusTotal = BigDecimal.ZERO;
            for (int i = 0; i < getPlayersSize(); i++) {
                User user = getUser(i);
                if (user != null && !Utils.isEqual(user, userWinner)) {
                    //tới trắng phạt 10 lần tiền cược
                    setPenalty(boardMoney.getForceFinishMoney());
                    BigDecimal value = getCanWinOrLoseMoney(userWinner, getPenalty());
                               value = getCanWinOrLoseMoney(user, value);
                    
                    setPenalty(getMoneyFromUser(user).min(value));
                    if (getPenalty().signum() > 0) {
                        SamPlayer xamPlayer = getXamPlayer(user);
                        String textVi=SamLanguage.getMessage(SamLanguage.FORCE_FINISH_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
                        String textEn=SamLanguage.getMessage(SamLanguage.FORCE_FINISH_LOSE, GlobalsUtil.ENGLISH_LOCALE);
                        String textZh=SamLanguage.getMessage(SamLanguage.FORCE_FINISH_LOSE, GlobalsUtil.CHINESE_LOCALE);
                        if (updateMoney(user, getPenalty().negate(),textVi ,textEn, textZh, CommonMoneyReasonUtils.PHAT_TOI_TRANG, BigDecimal.ZERO,xamPlayer.getCardsToList())) {
                            bonusTotal= Utils.add(bonusTotal, getPenalty());
                            //Set penalty đe tra ve khi ket thuc game
                            xamPlayer.setPenalty(getPenalty().negate());
                        }
                    }
                }
            }

            if (bonusTotal.signum() > 0) {
                BigDecimal []arrResultMoney = setMoneyMinusTax(bonusTotal, getTax());

                //Set tiền thưởng để trả về khi kết thúc game
                SamPlayer winnerPlayer = getXamPlayer(userWinner);
                winnerPlayer.setPenalty(arrResultMoney[MONEY]);
                String textVi=SamLanguage.getMessage(SamLanguage.FORCE_FINISH, GlobalsUtil.VIETNAMESE_LOCALE) + getPenaltyDesVi();
                String textEn=SamLanguage.getMessage(SamLanguage.FORCE_FINISH, GlobalsUtil.ENGLISH_LOCALE) + getPenaltyDesEn();
                String textZh=SamLanguage.getMessage(SamLanguage.FORCE_FINISH, GlobalsUtil.CHINESE_LOCALE) + getPenaltyDesZh();
                updateMoney(userWinner, arrResultMoney[MONEY],textVi ,textEn, textZh, CommonMoneyReasonUtils.THUONG_TOI_TRANG,arrResultMoney[TAX],winnerPlayer.getCardsToList());
                sendRankingData(userWinner, arrResultMoney[TAX].doubleValue(), 1);
                updateAchievement(userWinner, CommonMoneyReasonUtils.THANG);
            }

        } catch (Exception e) {
            log.error("XamGame.forceFinish error:" + boardLog.getLog(), e);
        }
        //Stop game
        stopGame();
    }

    /**
     * Reset game
     */
    private void reset() {
        winner = null;
        cardSet.xaoBai();
        typeMoved = -1;
        typeCard = -1;
        penalty = BigDecimal.ZERO;
        penaltyDesVi = "";
        penaltyDesEn = "";
        penaltyDesZh = "";
        logText="";
        setCurrentPlayer(null);
        playerMoved = null;
        playerBeginNewRound = null;
        xamUser = null;
        heoCount = 0;
        fourOfKindCount = 0;
        skipXamCount = 0;
        cardMove = new ArrayList<>();
        xamSuccessMoney = BigDecimal.ZERO;
        denBaiUser = null;
        countPlayerLeave = 0;
        firstCardMove.clear();
        boardMoney.setMoney(getMoney());
        boardMoney.setMoneyPot(BigDecimal.ZERO);
        boardLog.clear();
        moneyManagement.reset();
        for(SamPlayer player : getListPlayer().values()) {
            player.reset();
            // xét tiền của game trước khi start game
            moneyManagement.bettingMoney(getIdDBOfUser(player.getUser()), getMoneyFromUser(player.getUser()));
        }
    }

    /**
     * Stop game
     */
    @Override
    public synchronized void stopGame() {
        if (!isPlaying()) {
            return;
        }
        log.info("----------SAM STOP GAME----------------");
        try {
            SFSObject m = gameMessage.getStopMessage();
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("XamGame.stopGame error:" + boardLog.getLog(), e);
        } finally {
            super.stopGame();
            for (User user: getPlayersList()) {
                kickNoActionUser(user);
            }
            boardLog.clear();
            /**
             * xet=null để khi start game se chọn user có bài nhỏ nhất
             * nếu có user báo xâm thì moveFirst = user báo xâm
             */
            if(SamConfig.getInstance().isMoveFirstSmallCard()){
                moveFirst=null;
            } 
            processCountDownStartGame();
        }
    }
    @Override
    public void onReturnGame(User user) {
        try {
            super.onReturnGame(user);
            if (Utils.isEqual(user, xamUser)) {
                setXamUser(user);
            }
            if (Utils.isEqual(playerMoved, user)) {
                setPlayerMoved(user);
            }
            if (isPlaying()) {
                SamPlayer xamPlayer = getXamPlayer(getIdDBOfUser(user));
                if (xamPlayer != null) {
                    xamPlayer.setUser(user);
                    SFSObject ob = xamPlayer.getOnReturnMessage();
                    sendUserMessage(ob, user);
                }
            }
        } catch (Exception e) {
            this.log.error("Sam.onReturnGame() error: ", e);
        }
    }
    
    @Override
    public void processMessage(User player, ISFSObject sfsObj) {
        try {
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            int sNum = getSeatNumber(getIdDBOfUser(player));
            if(sNum<0){
                return;
            }
           String idDBPlayer=getIdDBOfUser(player);
           String idDBCurrent=getIdDBOfUser(getCurrentPlayer());
           BigDecimal moneyOfUser=getMoneyFromUser(player);
           super.processMessage(player, sfsObj); 
           switch(idAction){
               case SamCommand.XAM:
                   if (isPlaying()) {
                       //Nếu có người báo Xâm rồi thì không báo Xâm nữa
                       if (xamUser != null) {
                           break;
                       }
                       setXamUser(player);
                       //Set người đi đầu tiên là người báo Xâm
                       setMoveFirst(xamUser);
                       setPlayerBeginNewRound(xamUser);
                       setCurrentPlayer(player);

                       SamPlayer xamPlayer = getXamPlayer(getIdDBOfUser(xamUser));
                       xamPlayer.setState(xamPlayer.getXamState());
                       for (User p : getPlayingPlayers()) {
                           SamPlayer playerItem = getXamPlayer(getIdDBOfUser(p));
                           if (!playerItem.isLeaveState() && !playerItem.isXamState()) {
                               playerItem.setState(playerItem.getMovingState());
                           }
                       }
                       sendXamMessage();
                       setCurrentMoveTime();
                       setStateGame(this.getWaittingGameState());
                       addBoardDetail(player, CommonMoneyReasonUtils.XAM, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0,0, null);
                       boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "Xam", 0);
                       xamSuccessMoney = getXamSuccessMoney();
                   }
                   break;
               case SamCommand.SKIP_XAM:
                   boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "Skip xam", 0);
                   if (isPlaying()) {
                       boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "Skip xam", 0);
                       SamPlayer xamPlayer = getXamPlayer(idDBPlayer);
                       //Nếu player đó chưa hủy báo Xâm
                       if(xamPlayer != null && xamPlayer.isReadyState() && !isCallXamTime()) {
                           skipXamCount++;
                           xamPlayer.setState(xamPlayer.getSkipXamState());
                           //Nếu toàn bộ player hủy báo Xâm sẽ gửi message cho toàn bàn
                           if (skipXamCount >= getPlayingPlayers().size() && !checkHavePlayerDontSkipXam()) {
                               for (User p : getPlayingPlayers()) {
                                   SamPlayer playerItem = getXamPlayer(getIdDBOfUser(p));
                                   if (!playerItem.isLeaveState() && !playerItem.isXamState()) {
                                       playerItem.setState(playerItem.getMovingState());
                                   }
                               }
                               sendSkipXamMessage();
                               setCurrentMoveTime();
                               setStateGame(this.getWaittingGameState());
                           }
                       }
                        boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "Skip xam", 0);
                        addBoardDetail(player, CommonMoneyReasonUtils.SKIP_XAM, moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0,0, null);
                   }
                   break;
               case SFSAction.MOVE:
                   if (!idDBPlayer.equals(idDBCurrent) || !isPlaying()) {
                       break;
                   }

                   /**
                    * số thằng báo hủy báo xâm ít hơn số người chơi và không có
                    * ai báo xâm ,ko cho move chặn chỗ này vì có user có thể gửi
                    * command MOVE trong lúc những người khác đang trong thời
                    * gian chờ báo xâm
                    */
                   if (checkHavePlayerDontSkipXam() && !isCallXamTime() && xamUser == null) {
                       break;
                   }

                   SamPlayer movePlayer = getXamPlayer(idDBPlayer);
                   
                   //danh sach bai danh ra
                   List<Card> cards = new ArrayList<>();
                   List<Short> cardIds = new ArrayList(sfsObj.getShortArray("cards"));
                   if (cardIds != null && cardIds.size() > 0) {
                       for (int i = 0; i < cardIds.size(); i++) {
                           Card c = CardSet.getCard(cardIds.get(i).byteValue());
                           // check exist
                           if (!movePlayer.getCards().contains(c)) {
                               return;
                           }
                           cards.add(c);
                       }
                   }
                   addBoardDetail(player, CommonMoneyReasonUtils.MOVE , moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0, 0, cardIds);
                   //Đánh bài
                   movePlayer.move(cards);                  
                   boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "Move:" + SamCardUtils.getStringCards(cards), 0,
                           "Current Cards:" + SamCardUtils.getStringCards(movePlayer.getCards()));
                   break;
               case SFSAction.SKIP:
                   if (!idDBPlayer.equals(idDBCurrent) || !isPlaying()) {
                       break;
                   }
                   /**
                    * số thằng báo hủy báo xâm ít hơn số người chơi và không có
                    * ai báo xâm ,ko cho move chặn chỗ này vì có user có thể gửi
                    * command MOVE trong lúc những người khác đang trong thời
                    * gian chờ báo xâm
                    */
                   if (checkHavePlayerDontSkipXam() && xamUser == null) {
                       break;
                   }

                   SamPlayer skipPlayer = getXamPlayer(idDBPlayer);
                   if (skipPlayer == null) {
                       boardLog.addLog(player.getName(),getMoneyFromUser(player).doubleValue(), player.getName() + " Skip XamPlayer=null ", 0);
                   }
                   if (!skipPlayer.isXamState() && !skipPlayer.isReadyState()) {
                       skipPlayer.setState(skipPlayer.getSkippedState());
                   }
                   skipPlayer.skip();
                   addBoardDetail(player, CommonMoneyReasonUtils.SKIP , moneyOfUser.doubleValue(), moneyOfUser.doubleValue(), 0, 0, null);
                   boardLog.addLog(player.getName(),getMoneyFromUser(player).doubleValue(), "Skip", 0);
                   break;  
            }
        } catch (Exception e) {
            log.error("SAM processMessage() error: ", e);
        }
        
    }

    private boolean isCallXamTime() {
        return isPlaying()   && (System.currentTimeMillis() - callXamTime) >= SamConfig.getInstance().getXamTimeLimit();
    }

    @Override
    public void update() {
        try {
            super.update();
            if (isCanStart()) {
                startGame();
                return;
            }

            //Nếu hết thời gian báo hoặc hủy xâm thì gửi message cho client
            if (isCallXamTime()) {
                //Xài iterator để tránh lỗi concurrent
                Iterator<SamPlayer> iterator = getListPlayer().values().iterator();
                while (iterator.hasNext()) {
                    SamPlayer player = iterator.next();
                    if (player.isReadyState() && isInturn(player.getUser())) {
                        boardLog.addLog(player.getUser().getName(),getMoneyFromUser(player.getUser()).doubleValue(), "Skip xam", 0);
                        player.setState(player.getSkipXamState());
                        skipXamCount++;
                        if (skipXamCount >= getPlayingPlayers().size()) {
                            for(User p : getPlayingPlayers()) {
                                SamPlayer xamPlayer = getXamPlayer(p);
                                if (!xamPlayer.isLeaveState() && !xamPlayer.isXamState()) {
                                    xamPlayer.setState(xamPlayer.getMovingState());
                                }
                            }
                            sendSkipXamMessage();
                            setCurrentMoveTime();
                            setStateGame(this.getWaittingGameState());
                            break;
                        }
                    }
                }
            }

            //Hết thời gian 1 lượt đánh
            if (isPlaying()&& isTimeout()) {
                User user = getCurrentPlayer();
                if (user == null) {
                    log.error("XamGame.update: current user is null");
                    log.error("\nBoardLog:" + boardLog.getLog());
                    stopGame();
                    return;
                }

                SamPlayer player = getXamPlayer(user);
                if (player == null) {
                    log.error("XamGame.update: XamPlayer is null "+user.getName());
                    log.error("\nBoardLog:" + boardLog.getLog());
                    stopGame();
                    return;
                }
                checkNoActionNotBetGame(getCurrentPlayer());
                //Nếu hết giờ thi tự động bỏ lượt
                if (player.isMovingState()) {
                    player.setState(player.getSkippedState());
                    player.skip();
                }

                if (player.isXamState()) {
                    player.skip();
                }

                if (isCanStop()) {
                    stopGame();
                }
            }

        } catch (Exception e) {
            log.error("XamGame.upate error: " + boardLog.getLog(), e);
        }
    }

    @Override
    public synchronized void leave(User playerLeave) {
        String idDBLeaver=getIdDBOfUser(playerLeave);
        try {
            int seatNum = getSeatNumber(playerLeave);
            boolean isInTurn=isInturn(seatNum);
            int countInTurn = countInTurnPlayer();
            super.leave(playerLeave);
//            super.leave(playerLeave);
            //Nếu người chiến thắng ván chơi trước rời bàn thì set winner = null
            if (Utils.isEqual(winner, playerLeave)) {
                //nếu người rời bàn là người thắng thì xét winner là thằng kế tiếp để bắt đầu ván
                if (countInTurn <= 1) {
                    winner = null;
                } else {
                    winner = nextPlayer(playerLeave);
                }
            }

            boardLog.addLog(playerLeave.getName(),getMoneyFromUser(playerLeave).doubleValue(), "Leave", 0);
            
            SamPlayer xamPlayer = getXamPlayer(idDBLeaver);
            if (seatNum != -1 && isPlaying() && isInTurn) {
                //Nếu người rời bàn là người bắt đầu vòng mới thì chuyển lượt
                if (Utils.isEqual(getPlayerBeginNewRound(), playerLeave)) {
                    setPlayerBeginNewRound(nextPlayer(playerLeave));
                }
                //nếu chưa hết thời gian báo xâm coi như hủy báo xâm
                if (!isCallXamTime() && xamPlayer.isReadyState()) {
                    //Nếu toàn bộ player hủy báo Xâm sẽ gửi message cho toàn bàn
                    if (skipXamCount >= getPlayingPlayers().size()) {
                        for (User p : getPlayingPlayers()) {
                            SamPlayer playerItem = getXamPlayer(p);
                            if (!playerItem.isLeaveState() && !playerItem.isXamState()) {
                                playerItem.setState(playerItem.getMovingState());
                            }
                        }
                        if (countInTurn != 2){
                             //nếu có 2 thằng, có 1 thằng roi bàn thì không báo sâm
                            sendSkipXamMessage();
                        }
                        setCurrentMoveTime();
                        setStateGame(this.getWaittingGameState());
                    }
                }
                //Nếu đang trong luot đánh thì bỏ lượt
                if (Utils.isEqual(playerLeave, getCurrentPlayer())) {
                    xamPlayer.setState(xamPlayer.getSkippedState());
                    if (countInTurn != 2){
                        //nếu có 2 thằng, có 1 thằng roi bàn thì khong gui skip
                         xamPlayer.skip();
                    } 
                }

                //count số player rời khỏi bàn
                countPlayerLeave++;
                xamPlayer.setState(xamPlayer.getLeaveState());
                //Nếu chỉ còn 1 hoặc 0 người chơi thì dừng bàn chơi
                if (countInTurn <= 1) {
                    stopGame();
                } else {
                    //nếu thằng rời bàn là thằng báo Xâm thì phạt như Xâm thất bại
                    if (Utils.isEqual(playerLeave,getXamUser())) {
                        // setInturn false để không đếm thằng rời bàn vào danh sách đang chơi nữa 
                        setInturn(playerLeave, false);
                        phatXamPlayerLeave(xamPlayer);
                        stopGame();
                    } else {
                        //Xét heo
                        xetHeo(xamPlayer);
                        //xét tứ quý
                        xetTuQuy(xamPlayer);
                        //Nêu có người báo xâm thì phạt thêm 20 lần tiền phạt
                        if (xamUser != null || !isCallXamTime()) {
                            addPenalty(boardMoney.getXamSuccessMoney());
                        } else {
                            if (xamPlayer.getCards().size() < DEFAULT_NUMBER_XAM_CARD) {
                                addPenalty(boardMoney.getNormalMoney(xamPlayer.getCards().size()));
                            } else {
                                addPenalty(boardMoney.getCongMoney());
                            }
                        }
                        if (getPenalty().signum() > 0) {
                            BigDecimal value = getPenalty().min(getMoneyFromUser(playerLeave));
                                       value = moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(playerLeave), value);
                            setPenalty(value);
                            
                            String textVi= GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.VIETNAMESE_LOCALE);
                            String textEn=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.ENGLISH_LOCALE);
                            String textZh=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.CHINESE_LOCALE);
                            logText+="- "+playerLeave.getName()+": Bỏ cuộc.\n";
                            xamPlayer.setPenalty(getPenalty().negate());
                            if (updateMoney(playerLeave, getPenalty().negate(), textVi,textEn ,textZh, CommonMoneyReasonUtils.BO_CUOC, BigDecimal.ZERO,xamPlayer.getCardsToList())) {
                                boardMoney.addMoneyPot(getPenalty());
                            }
                        }

                        //Nếu chỉ còn 2 người chơi thì cho người chơi còn lại thắng
                        if (countInTurn == 2) {
                            nextTurn();
                            winner = getCurrentPlayer();
                            SamPlayer xamWinnerPlayer = getXamPlayer(winner);
                            BigDecimal maxMoneyWin = Utils.multiply(moneyManagement.getBettingMoney(getIdDBOfUser(winner)), new BigDecimal(String.valueOf(countPlayerLeave))) ;
                            BigDecimal total = boardMoney.getMoneyPot().min(maxMoneyWin);
                            boardMoney.setMoneyPot(BigDecimal.ZERO);
                            BigDecimal []arrResultMoney = setMoneyMinusTax(total, getTax());
                            
                            xamWinnerPlayer.setPenalty(arrResultMoney[MONEY]);
                            updateMoney(winner, arrResultMoney[MONEY],"","","", CommonMoneyReasonUtils.THANG,arrResultMoney[TAX],xamWinnerPlayer.getCardsToList());
                            sendRankingData(winner, arrResultMoney[TAX].doubleValue(), 1);
                            updateAchievement(winner, CommonMoneyReasonUtils.THANG);
                            stopGame();
                        }
                        setPenalty(BigDecimal.ZERO);                  
                    }
                }
            }
        } catch (Exception e) {
            log.error("XamGame.leave error " + boardLog.getLog(), e);
        } finally {
            forceLogoutUser(playerLeave);
            //remove khỏi list player
            getListPlayer().remove(idDBLeaver);
            //nếu chỉ còn 1 người chơi thì bò quyền đánh đầu tiên của winner
            if (Utils.isEqual(moveFirst, playerLeave) || getPlayersList().size() == 1) {
                moveFirst = null;
            }
        }
    }

    /**
     * Đếm số người chơi còn trong lượt chơi
     *
     * @return
     */
    private int countInTurnPlayer() {
        int count = 0;
        try {
            for (int i = 0; i <getPlayersSize(); i++) {
                User p = getUser(i);
                if (isInturn(i) && p != null) {
                    count++;
                }
            }
        } catch (Exception e) {
            log.error("XamGame.countInTurnPlayer error", e);
        }
        return count;
    }

    /**
     * Update tiền trong bàn chơi
     *
     * @param player
     * @param value
     * @param text
     * @param reasonId
     * @return
     */
    private boolean updateMoney(User player, BigDecimal value, String textVi, String textEn, String textZh, int reasonId,BigDecimal tax, List<Short> arrayCardIDs) {
        if (value.signum() == 0) {
            return false;
        }
        
        if (updateMoney2WithLocale(player, value, textVi, textEn, textZh, reasonId, tax, arrayCardIDs)) {
            log.debug("updateMoney " + player.getName() + ", win =" + value + " reason =" + textVi);
            return true;
        }
        log.error("XamGame.updateMoney error: user: " + player.getName() + " value: " + value + " logText: " + textVi + "\nboardLog: " + boardLog.getLog());
        return false;
    }

    /**
     * Get Xam Player
     *
     * @param user
     * @return
     */
    public SamPlayer getXamPlayer(User user) {
        String idDB=getIdDBOfUser(user);
        if(idDB==null){
            return null;
        }
        return getListPlayer().get(idDB);
    }
    
    /**
     * Get Xam Player
     *
     * @param idDBUser
     * @return
     */
    public SamPlayer getXamPlayer(String idDBUser) {
        if(!getListPlayer().containsKey(idDBUser)){
            return null;
        }
        return getListPlayer().get(idDBUser);
    }

    /**
     * Send Xâm message
     */
    private void sendXamMessage() {
        try {
            SFSObject m = gameMessage.getSamMessage();
            sendAllUserMessage(m);
        } catch (Exception e) {
           log.error("XamGame.sendXamMessage error: " + boardLog.getLog(), e);
        }
    }

    /**
     * Send message Xâm thất bại
     */
    private void sendXamFailMessage() {
        try {
            SFSObject m = gameMessage.getChanXamMessage();
            sendAllUserMessage(m);;
        } catch (Exception e) {
            log.error("XamGame.sendXamFailMessage error: " + boardLog.getLog(), e);
        }
    }

    /**
     * Send Skip Message
     *
     * @param player
     * @param isClearCard
     */
    public void sendSkipMessage(User player, boolean isClearCard) {
        try {
            SFSObject m = gameMessage.getSkipMessage(getIdDBOfUser(player),getIdDBOfUser(getCurrentPlayer()), isClearCard);
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("XamGame.sendSkipMessage error:" + boardLog.getLog(), e);
        }
    }

    /**
     * Tính tiền bị chặt
     */
    public void updateMoneyChat() {
        try {
            BigDecimal value = getCanWinOrLoseMoney(playerBiChat, getPenalty());
            setPenalty(value.min(getMoneyFromUser(playerBiChat)));
            if (getPenalty().signum() > 0) {
                if (playerBiChat != null) {
                    log.debug("tiền chặt  >> " + getPenalty());
                    setPenalty(getCanWinOrLoseMoney(playerMoved, getPenalty()));
                    updateMoney(playerBiChat, getPenalty().negate(), getPenaltyDesVi(), getPenaltyDesEn(), getPenaltyDesZh(), getReasonId(), BigDecimal.ZERO,null);
                    
                    BigDecimal []arrResultMoney=setMoneyMinusTax(getPenalty(), getTax());
                    setPenalty(arrResultMoney[MONEY]);
                    updateMoney(playerMoved, getPenalty(), getPenaltyDesVi(), getPenaltyDesEn(), getPenaltyDesZh(), CommonMoneyReasonUtils.THANG_CHAT,arrResultMoney[TAX], null);
                }
            }
            setPenalty(BigDecimal.ZERO);
            setPenaltyDesVi("");
            setPenaltyDesEn("");
            setPenaltyDesZh("");
        } catch (Exception e) {
            log.error("XamGame.updateMoneyChat error: " + boardLog.getLog(), e);
        }
    }

    /**
     * Hệ thống tự đi bài nếu hết thời gian
     *
     * @param player
     */
    public void autoPlay(User player) {
        try {
            int sNum = getSeatNumber(player);
            if (sNum == -1) {
                return;
            }
            SamPlayer xamPlayer = getXamPlayer(player);
            //Lây nhóm bài nhỏ nhất trong bộ bài và đánh ra
            if (xamPlayer.getCards().size() > 0) {
                List<Card> cards = new ArrayList<>();
                try {
                    cards.addAll(xamPlayer.getSmallestCards());
                } catch (Exception e) {
                    log.error("autoPlay error", e);
                    cards.add(xamPlayer.getCards().get(0));
                }
                if (!xamPlayer.isXamState() && !xamPlayer.isReadyState()) {
                    xamPlayer.setState(xamPlayer.getMovingState());
                }
                xamPlayer.move(cards);
            }
        } catch (Exception e) {
            log.error("XamGame.autoplay error:" + boardLog.getLog(), e);
        }
    }

    private void checkDenBaoUser(List<Card> cardMoveds, SamPlayer player) {
        if (getPlayingPlayers().size() <= 2) {
            return;
        }
        if (player.getCards().isEmpty()) {
            return;
        }
        if (cardMoveds == null) {
            return;
        }
        log.debug("checkDenBaoUser " + player.getUser().getName() + " cardMoved =" + SamCardUtils.getStringCards(cardMoveds));

        User nextUser = getNextUser(true);
        if (nextUser == null) {
            return;
        }
        SamPlayer nextXamPlayer = getXamPlayer(nextUser);
        // thằng kế tiếp còn 1 lá hoặc skip thì check bài
        if (nextXamPlayer.getCards().size() > 1 || nextXamPlayer.isSkippedState()) {
            return;
        }
        // thằng kế tiếp chỉ còn 1 lá và nó là thằng đánh cuối cùng lượt vừa rồi 
        if (!Utils.isEqual(nextUser, getPlayerMoved()) && cardMoveds.size() > 1) {
            return;
        }

        if (SamCardUtils.getType(cardMoveds) == SamCardUtils.ONE_CARD) {
            // thằng đánh tiếp theo báo 1, check con bài đánh ra có phải con lớn nhất trong bài, sai -> đền        
            if (cardMoveds.get(0).getCardNumber() < player.getCards().get(player.getCards().size() - 1).getCardNumber()
                    && cardMoveds.get(0).getCardNumber() < nextXamPlayer.getCards().get(0).getCardNumber()) {
                log.debug("user den bao =" + getCurrentPlayer().getName());
                denBaiUser = player.getUser();
            } else {
                denBaiUser = null;
            }
        } else {
            List<Card> listbaiden = SamCardUtils.findHigherInUserCards(player.getCards(), cardMoveds);
            if (listbaiden.isEmpty()) {
                denBaiUser = null;
                return;
            }
            player.getCards().removeAll(listbaiden);
            // chỉ còn heo nếu đánh bài có thể bắt -> ko đền
            if (checkHaveOnlyHeo(player.getCards())) {
                denBaiUser = null;
                player.getCards().addAll(listbaiden);
                return;
            }
            player.getCards().addAll(listbaiden);
            log.debug("user den bao =" + getCurrentPlayer().getName() + " bai den=" + SamCardUtils.getStringCards(listbaiden));
            denBaiUser = player.getUser();
        }
    }

    /**
     * lấy user tiếp theo của user hiện tại
     *
     * @return
     */
    private User getNextUser(boolean checkDen) {
        int sNum = getSeatNumber(getCurrentPlayer());
        User nextUser = null;
       
        for (int i = 0; i < getPlayersSize(); i++) {
            sNum = (sNum + 1) % getPlayersSize();
            nextUser = getUser(sNum);
            if (nextUser != null && isInturn(sNum)) {
                SamPlayer xamPlayer = getXamPlayer(nextUser);
                if (checkDen) {//sử dụng cho trường hợp đền báo 1
                    if (xamPlayer != null) {
                        break;
                    }
                } else {//Kiểm tra người tiếp theo có trong lượt đi không
                    if (xamPlayer != null && !xamPlayer.isSkippedState()) {
                        break;
                    }
                }
            }
        }
        return nextUser;
    }

    /**
     * Chuyển lượt đánh cho người tiếp theo
     */
    @Override
    public void nextTurn() {
        try {
            int currentSeatNumb = getSeatNumber(getCurrentPlayer());
            User nextUser = getNextUser(false);
            setCurrentPlayer(nextUser);
            if (getSeatNumber(nextUser) == currentSeatNumb) {
                setTypeMoved(-1);
            }
            setCurrentMoveTime();
            setStateGame(this.getWaittingGameState());
        } catch (Exception e) {
            log.error("XamGame.nextTurn error:" + boardLog.getLog(), e);
        }
    }

    /**
     * Send Hủy Xâm message cho toàn bàn
     */
    public void sendSkipXamMessage() {
        try {
            SFSObject m = gameMessage.getSkipXamMessage();
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("XamGame.sendSkipXamMessage error:" + boardLog.getLog(), e);
        }
    }

    /**
     * Đánh bài
     *
     * @param player
     * @param cards
     */
    public void move(User player, List<Card> cards) throws Exception {
        int sNum = getSeatNumber(player);
        if (sNum == -1) {
            return;
        }
        Locale localePlayer=getLocaleOfUser(player);
        SamPlayer xamPlayer = getXamPlayer(player);

        Collections.sort(cards);

        //Lấy kiểu bài đánh ra
        setTypeCard(SamCardUtils.getType(cards));
        //Kiểm tra loại bài đánh ra có hợp lệ không
        if (getTypeCard() == SamCardUtils.NOTYPE) {
            String infor=GameLanguage.getMessage(GameLanguage.INVALID_CARD, localePlayer);
            xamPlayer.sendMoveErrorMessage(infor);
            return;
        }

        //Kiểm tra bài đánh có hợp lệ không
        if (!checkMove(xamPlayer, cards, getTypeCard())) {
            String infor=GameLanguage.getMessage(GameLanguage.INVALID_CARD, localePlayer);
            xamPlayer.sendMoveErrorMessage(infor);
            return;
        }
        boolean isBiChat=getPenalty().signum() >0;
        //Xóa bài vừa đánh ra danh sách bài trên tay
        xamPlayer.getCards().removeAll(cards);
        // check chỉ còn heo trên tay, không cho đánh bài/ nhóm bài vừa rồi
        if (checkHaveOnlyHeo(xamPlayer.getCards())) {
            xamPlayer.getCards().addAll(cards);
            xamPlayer.sendMoveErrorMessage(SamLanguage.getMessage(SamLanguage.NOT_ALLOW_HEO, localePlayer));
            return;
        }

        // kiểm tra có đền báo không
        if (getTypeCard() == SamCardUtils.ONE_CARD) {
            checkDenBaoUser(cards, xamPlayer);
        }
        //Nếu thằng đánh ở lượt đánh trước đó có báo Xâm- thằng current chặn dc ---> Xâm Fail
        if (getPlayerMoved() != null && !Utils.isEqual(getCurrentPlayer(), getPlayerMoved())) {
            SamPlayer movePlayer = getXamPlayer(getPlayerMoved());
            //Xam that bại
            if (movePlayer != null && movePlayer.isXamState()) {
                winner = player;
                setMoveFirst(winner);
                setPlayerBiChat(getPlayerMoved());
                setCardMove(cards);
                setTypeMoved(getTypeCard());
                setPlayerMoved(player);
                sendMoveMessage(isBiChat);
                sendXamFailMessage();
                phatXamFail();
                stopGame();
                return;
            }
        }
        if (getPenalty().signum() > 0 && getPlayerMoved() != null) {
            setPlayerBiChat(getPlayerMoved());
        }

        setPlayerMoved(player);
        setPlayerBeginNewRound(player);
        setCardMove(cards);
        setTypeMoved(getTypeCard());
        // lưu lại bài đánh đầu tiên để check đền bài nếu thằng đánh đầu tới trắng
        if (Utils.isEqual(player, moveFirst)) {
            firstCardMove.add(cards);
        }
        //Chuyển lượt
        nextTurn();

        sendMoveMessage(isBiChat);

        //nếu thằng vừa đánh là thằng bắt đầu vòng mới thì reset lại
        if (getTypeMoved() == SamCardUtils.NOTYPE) {
            for (SamPlayer playerItem : listPlayer.values()) {
                if (!playerItem.isLeaveState() && !playerItem.isXamState()) {
                    playerItem.setState(playerItem.getSkippedState());
                }
            }
            //set lại bằng 0 cho các trường hợp chặt chồng ở vòng mới
            setHeoCount(0);
            setFourOfKindCount(0);
        }
        // báo chỉ còn 1 lá bài
        if (xamPlayer.getCards().size() == 1) {
            sendNotifyOneCardMessage(getIdDBOfUser(xamPlayer.getUser()));
        }
        //Nếu người chơi hết bài thì thắng, nếu không thì chuyển lượt tiếp theo cho người tiếp theo
        if (xamPlayer.getCards().isEmpty()) {
            if (getPenalty().signum() > 0) {
                updateMoneyChat();
            }

            winner = player;
            setMoveFirst(player);
            if (xamPlayer.isXamState()) {
                xamSuccess();
            } else {
                //Xét thua cóng
                xetCong(player);
                //Xét bài các người chơi còn lại
                xetBai();
            }
            stopGame();
        }
    }

    /**
     * Send move message
     * @param isBiChat
     */
    public void sendMoveMessage(boolean isBiChat) {
        try {
            SFSObject m = gameMessage.getMoveMessage(isBiChat);
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("XamGame.sendMoveMessage error" + boardLog.getLog(), e);
        }
    }

    /**
     * Check bài đánh ra có hợp lệ không
     *
     * @param xamPlayer
     * @param cards
     * @param typeCard
     * @return
     */
    public boolean checkMove(SamPlayer xamPlayer, List<Card> cards, int typeCard) {
        try {
            setPenalty(BigDecimal.ZERO);
            setPenaltyDesVi("");
            setPenaltyDesEn("");
            setPenaltyDesZh("");
            setReasonId(-1);

            //Kiểm tra dựa theo loại bài của lần đánh trước
            switch (getTypeMoved()) {
                case SamCardUtils.NOTYPE:
                    if (typeCard != -1) {
                        // reset lại heo khi người chơi đánh heo ở vòng mới
                        if (cards.get(cards.size() - 1).isHeo()) {
                            setHeoCount(cards.size());
                        }
                        if (typeCard == SamCardUtils.FOUR_OF_A_KIND) {
                            increaseFourOfKindCount();
                        }
                        //đôi tứ quý chỉ được dùng để chặt heo và chặt tứ quý
                        if (typeCard == SamCardUtils.DOUBLE_FOUR_OF_A_KIND) {
                            return false;
                        }
                        return true;
                    }
                    break;
                case SamCardUtils.ONE_CARD:
                    //Trường hợp đánh 1 lá
                    if (typeCard == SamCardUtils.ONE_CARD
                            && SamCardUtils.isHigher(cards.get(cards.size() - 1), getCardMove().get(getCardMove().size() - 1))) {

                        //Nếu con cuối trên tay là con heo thì không được đánh
                        if (cards.get(cards.size() - 1).isHeo() && xamPlayer.getCards().size() == cards.size()
                                && xamPlayer.getCards().get(cards.size() - 1).isHeo()) {
                            return false;
                        }
                        // luu lai heo de phong truong hop chat chong.
                        if (cards.get(cards.size() - 1).isHeo()) {
                            setHeoCount(cards.size());
                        }
                        return true;
                    }
                    //Nếu bài đi trước là heo thì xét trường hợp bị chặt
                    if (getCardMove().get(getCardMove().size() - 1).isHeo()) {
                        // tứ quý mới chặt được heo
                        if (typeCard == SamCardUtils.FOUR_OF_A_KIND) {
                            setPenaltyDesVi(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.VIETNAMESE_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.VIETNAMESE_LOCALE));
                            setPenaltyDesEn(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.ENGLISH_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.ENGLISH_LOCALE));
                            setPenaltyDesZh(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.CHINESE_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.CHINESE_LOCALE));
                            setReasonId(CommonMoneyReasonUtils.BI_CHAT);
                            addPenalty();
                            increaseFourOfKindCount();
                            return true;
                        }
                    }
                    break;
                case SamCardUtils.PAIR:
                    //Trường hợp đánh ra 1 đôi
                    if (typeCard == SamCardUtils.PAIR
                            && SamCardUtils.isHigher(cards.get(cards.size() - 1), getCardMove().get(getCardMove().size() - 1))) {
                        //Nếu con cuối trên tay là đôi heo thì không được đánh
                        if (cards.get(cards.size() - 1).isHeo() && xamPlayer.getCards().size() == cards.size()
                                && xamPlayer.getCards().get(cards.size() - 1).isHeo()) {
                            return false;
                        }
                        if (cards.get(cards.size() - 1).isHeo()) {
                            setHeoCount(cards.size());
                        }
                        return true;
                    }
                    //Nếu bài đi trước đó là đôi heo thì xét bị chặt
                    if (getCardMove().get(getCardMove().size() - 1).isHeo()) {
                        //4 đôi thông hoặc 2 tứ quý mới chặt được đôi heo
                        if (typeCard == SamCardUtils.DOUBLE_FOUR_OF_A_KIND) {
                            //Bị chặt 2 heo, mỗi heo mất 15 lần tiền cược
                            setPenaltyDesVi(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.VIETNAMESE_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.PAIR, GlobalsUtil.VIETNAMESE_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.VIETNAMESE_LOCALE));
                            setPenaltyDesEn(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.ENGLISH_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.PAIR, GlobalsUtil.ENGLISH_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.ENGLISH_LOCALE));
                            setPenaltyDesZh(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.CHINESE_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.PAIR, GlobalsUtil.CHINESE_LOCALE)
                                    + SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.CHINESE_LOCALE));
                            setReasonId(CommonMoneyReasonUtils.BI_CHAT);
                            addPenalty();
                            increaseFourOfKindCount();
                            increaseFourOfKindCount();
                            return true;
                        }
                    }
                    break;
                case SamCardUtils.TRIPLE:
                    //Trường hợp đánh ra 1 bộ 3 lá
                    if (typeCard == SamCardUtils.TRIPLE
                            && SamCardUtils.isHigher(cards.get(cards.size() - 1), getCardMove().get(getCardMove().size() - 1))) {
                        //Nếu con cuối trên tay là 3 con heo thì không được đánh
                        if (cards.get(cards.size() - 1).isHeo() && xamPlayer.getCards().size() == cards.size()
                                && xamPlayer.getCards().get(cards.size() - 1).isHeo()) {
                            return false;
                        }
                        return true;
                    }
                    break;
                case SamCardUtils.STRAIGHT:
                    //Trường hợp đánh ra 1 sảnh
                    if (typeCard == SamCardUtils.STRAIGHT
                            && cards.size() == getCardMove().size()) {
                        List<Card> tempCard1 = new ArrayList<>(cards);
                        List<Card> tempCard2 = new ArrayList<>(getCardMove());
                        // sảnh có heo
                        if (tempCard1.get(tempCard1.size() - 1).isHeo()) {
                            tempCard1.remove(tempCard1.size() - 1);
                            // sảnh có A 2..., bỏ A,2 ra
                            if (tempCard1.get(tempCard1.size() - 1).getCardNumber() == 11) {
                                tempCard1.remove(tempCard1.size() - 1);
                            }
                        }
                        if (tempCard2.get(tempCard2.size() - 1).isHeo()) {
                            tempCard2.remove(tempCard2.size() - 1);
                            // sảnh có A 2..., bỏ A,2 ra
                            if (tempCard2.get(tempCard2.size() - 1).getCardNumber() == 11) {
                                tempCard2.remove(tempCard2.size() - 1);
                            }
                        }
                        if (SamCardUtils.isHigher(tempCard1.get(tempCard1.size() - 1), tempCard2.get(tempCard2.size() - 1))) {
                            return true;
                        }
                    }

                    break;

                case SamCardUtils.FOUR_OF_A_KIND:
                    //Chỉ tứ quý lớn hơn hoặc 4 đôi thông mới chặt được tứ quý
                    if ((typeCard == SamCardUtils.FOUR_OF_A_KIND && SamCardUtils.isHigher(cards.get(cards.size() - 1),
                            getCardMove().get(getCardMove().size() - 1)))) {
                        //Nếu con cuối trên tay là tứ quý heo thì không được đánh
                        if (cards.get(cards.size() - 1).isHeo() && xamPlayer.getCards().size() == cards.size()
                                && xamPlayer.getCards().get(cards.size() - 1).isHeo()) {
                            return false;
                        }
                        setPenaltyDesVi(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.VIETNAMESE_LOCALE)
                                + SamLanguage.getMessage(SamLanguage.FOUR_OF_A_KIND, GlobalsUtil.VIETNAMESE_LOCALE));
                        setPenaltyDesEn(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.ENGLISH_LOCALE)
                                + SamLanguage.getMessage(SamLanguage.FOUR_OF_A_KIND, GlobalsUtil.ENGLISH_LOCALE));
                        setPenaltyDesZh(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.CHINESE_LOCALE)
                                + SamLanguage.getMessage(SamLanguage.FOUR_OF_A_KIND, GlobalsUtil.CHINESE_LOCALE));
                        setReasonId(CommonMoneyReasonUtils.CHAT_TU_QUY);
                        addPenalty();
                        increaseFourOfKindCount();
                        return true;
                    }
                    break;
                case SamCardUtils.DOUBLE_FOUR_OF_A_KIND:
                    //Chỉ tứ quý lớn hơn hoặc 4 đôi thông mới chặt được tứ quý
                    if ((typeCard == SamCardUtils.DOUBLE_FOUR_OF_A_KIND && SamCardUtils.isHigher(cards.get(cards.size() - 1),
                            getCardMove().get(getCardMove().size() - 1)))) {
                        //Nếu con cuối trên tay là tứ quý heo thì không được đánh
                        if (cards.get(cards.size() - 1).isHeo() && xamPlayer.getCards().size() == cards.size()
                                && xamPlayer.getCards().get(cards.size() - 1).isHeo()) {
                            return false;
                        }
                        setPenaltyDesVi(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.VIETNAMESE_LOCALE)
                                + SamLanguage.getMessage(SamLanguage.DOUBLE_FOUR_OF_A_KIND, GlobalsUtil.VIETNAMESE_LOCALE));
                        setPenaltyDesEn(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.ENGLISH_LOCALE)
                                + SamLanguage.getMessage(SamLanguage.DOUBLE_FOUR_OF_A_KIND, GlobalsUtil.ENGLISH_LOCALE));
                        setPenaltyDesZh(SamLanguage.getMessage(SamLanguage.DEFEAT, GlobalsUtil.CHINESE_LOCALE)
                                + SamLanguage.getMessage(SamLanguage.DOUBLE_FOUR_OF_A_KIND, GlobalsUtil.CHINESE_LOCALE));
                        setReasonId(CommonMoneyReasonUtils.CHAT_TU_QUY);
                        addPenalty();
                        increaseFourOfKindCount();
                        increaseFourOfKindCount();
                        return true;
                    }
                    break;

            }
            return false;
        } catch (Exception e) {
            log.error("XamGame.checkMove error" + boardLog.getLog(), e);
            return false;
        }
    }

    /**
     * Cộng tiền phạt nếu có chặt chồng
     */
    public void addPenalty() {
        setPenalty(BigDecimal.ZERO);
        if (getHeoCount() > 0) {
            addPenalty(getBoardMoney().getChatHeoMoney(getHeoCount()));
        }

        if (getFourOfKindCount() > 0) {
            addPenalty(getBoardMoney().getChatTuQuyMoney(getFourOfKindCount()));
        }
    }

    /**
     * Bỏ lượt
     *
     * @param player
     */
    public void skip(SamPlayer player) {
        try {
            if (Utils.isEqual(getPlayerBeginNewRound(), player.getUser())&& Utils.isEqual(getPlayerBeginNewRound(), getCurrentPlayer())
                    && getPlayerMoved() != null && getTypeMoved() == SamCardUtils.NOTYPE) {
                setTypeMoved(-1);
                if (getPenalty().signum() > 0) {
                    updateMoneyChat();
                }

                for (User p : getPlayingPlayers()) {
                    SamPlayer xamPlayer = getXamPlayer(p);
                    if (!xamPlayer.isLeaveState() && !xamPlayer.isXamState() && !xamPlayer.isReadyState()) {
                        xamPlayer.setState(xamPlayer.getMovingState());
                    }
                }

                //set lại bằng 0 cho các trường hợp chặt chồng ở vòng mới
                setHeoCount(0);
                setFourOfKindCount(0);
                //Nếu player là người bắt đầu vòng mới mà skip thì hệ thống tự động đi bài của user đó
                autoPlay(player.getUser());
            } else {
                if (getTypeMoved() != -1) {
                    player.setState(player.getSkippedState());
                }

                checkDenBaoUser(cardMove, player);
                nextTurn();

                if (getCurrentPlayer() == null) {
                    return;
                }

                //nếu tất cả đề skip thì set lại current player là thằng bắt đầu vòng mới
                if (isSkipAll()) {
                    setCurrentPlayer(getPlayerBeginNewRound());
                }
                //Cho biết có clear bài hiện tại trên bàn hay ko
                boolean isClearCard = false;
                //Bắt đầu vòng mới
                if (Utils.isEqual(getCurrentPlayer(), getPlayerBeginNewRound())) {
                    isClearCard = true;
                    setTypeMoved(-1);
                    if (getPenalty().signum() > 0) {
                        updateMoneyChat();
                    }
                    for (User p : getPlayingPlayers()) {
                        SamPlayer xamPlayer = getXamPlayer(p);
                        if (!xamPlayer.isLeaveState() && !xamPlayer.isXamState() && !xamPlayer.isReadyState()) {
                            xamPlayer.setState(xamPlayer.getMovingState());
                        }
                    }
                    //set lại bằng 0 cho các trường hợp chặt chồng ở vòng mới
                    setHeoCount(0);
                    setFourOfKindCount(0);
                }
                sendSkipMessage(player.getUser(), isClearCard);
            }
        } catch (Exception e) {
           log.error("XamGame.skip error" + boardLog.getLog(), e);
        }
    }

    /**
     * Kiểm tra xem toàn bộ người chơi bỏ lượt không
     *
     * @return
     */
    public boolean isSkipAll() {
        for (User p : getPlayingPlayers()) {
            SamPlayer player = getXamPlayer(p);
            if (!player.isSkippedState()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Xét heo
     *
     * @param xamPlayer
     */
    public void xetHeo(SamPlayer xamPlayer) {
        int heo = SamCardUtils.countHeo(xamPlayer.getCards());
        if (heo > 0) {
            addPenalty(getBoardMoney().getUngHeoMoney(heo));
            addPenaltyDesVi(SamLanguage.getMessage(SamLanguage.UNUSED, GlobalsUtil.VIETNAMESE_LOCALE)
                    + heo +" "+ SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.VIETNAMESE_LOCALE));
            addPenaltyDesEn(SamLanguage.getMessage(SamLanguage.UNUSED, GlobalsUtil.ENGLISH_LOCALE)
                    + heo +" "+ SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.ENGLISH_LOCALE));
            addPenaltyDesZh(SamLanguage.getMessage(SamLanguage.UNUSED, GlobalsUtil.CHINESE_LOCALE)
                    + heo +" "+ SamLanguage.getMessage(SamLanguage.TWO, GlobalsUtil.CHINESE_LOCALE));
        }
    }

    private User findUserDenBai() {
        // 2 thằng chơi khỏi kiếm thằng đền bài
        if (getPlayingPlayers().size() <= 2) {
            return null;
        }
        int countCong = 0;
        int sNum = getSeatNumber(winner);
        try {
            for (User p : getPlayingPlayers()) {
                SamPlayer xamPlayer = getXamPlayer(p);
                //Nếu player còn trong lượt chơi và bài trên tay còn 10 lá thì thua cóng
                if (!Utils.isEqual(winner, p) && !xamPlayer.isLeaveState()
                        && xamPlayer.getCards().size() == DEFAULT_NUMBER_XAM_CARD) {
                    countCong++;
                }
            }

            if (countCong == (getPlayingPlayers().size() - 1)) {
                for (int j = 0; j < firstCardMove.size() - 1; j++) {
                    List<Card> cardMoved = firstCardMove.get(j);
                    log.debug(" nhóm bài " + j + " =" + SamCardUtils.getStringCards(cardMoved));
                    for (int i = 0; i < getPlayersSize(); i++) {
                        sNum = (sNum + 1) % getPlayersSize();
                        User p = getUser(sNum);
                        if (p == null) {
                            continue;
                        }
                        List<Card> cards = SamCardUtils.findHigherInUserCards(getXamPlayer(p).getCards(), cardMoved);
                        if (!cards.isEmpty()) {
                            log.debug("user den :" + p.getName() + " bai den " + SamCardUtils.getStringCards(cards));
                            SFSObject m = gameMessage.getDenBaiMessage(getIdDBOfUser(p),Utils.convertListCardIds2Array(cards));
                            sendAllUserMessage(m);
                            return p;
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("findUserDenBai error", e);
        }
        log.debug("End findUserDenBai >>>>");
        return null;
    }

    /**
     * Xét thua cóng khi có player win
     *
     * @param player
     */
    public void xetCong(User player) {
        try {
            String lostVi = "";
            String lostEn = "";
            String lostZh = "";
            //Tổng tiền thua cóng
            BigDecimal total = BigDecimal.ZERO;
            User userLoseAll = findUserDenBai();
            if (userLoseAll == null && denBaiUser != null) {
                userLoseAll = denBaiUser;
            }
            
            //ghi log cho thằng thắng
            for (User p : getPlayingPlayers()) {
                SamPlayer xamPlayer = getXamPlayer(p);
                //Nếu player còn trong lượt chơi và bài trên tay còn 10 lá thì thua cóng
                if (!Utils.isEqual(winner, p) && !xamPlayer.isLeaveState()
                        && xamPlayer.getCards().size() == DEFAULT_NUMBER_XAM_CARD) {
                    setPenaltyDesVi("");
                    setPenaltyDesEn("");
                    setPenaltyDesZh("");
                    lostVi = SamLanguage.getMessage(SamLanguage.INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
                    lostEn = SamLanguage.getMessage(SamLanguage.INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE);
                    lostZh = SamLanguage.getMessage(SamLanguage.INSTANT_LOSE, GlobalsUtil.CHINESE_LOCALE);
                    //Xét heo
                    xetHeo(xamPlayer);
                    //xét tứ quý
                    xetTuQuy(xamPlayer);
                    //Thua cóng phạt 20 lần tiền cược
                    addPenalty(getBoardMoney().getCongMoney());
                    User userLostMoney = userLoseAll == null ? xamPlayer.getUser() : userLoseAll;
                    BigDecimal value = getCanWinOrLoseMoney(userLostMoney, getPenalty());
                               value = getMoneyFromUser(userLostMoney).min(value);
                               value = getCanWinOrLoseMoney(player, value);
                    setPenalty(value);
                    int reason=CommonMoneyReasonUtils.TIEN_PHAT_CONG;
                    //Set penalty cho player để trả về khi ket thuc game
                    if (userLoseAll == null) {
                        xamPlayer.setPenalty(getPenalty().negate());
                    } else {  
                        SamPlayer xamPlayerLoseAll = getXamPlayer(userLoseAll);
                        xamPlayerLoseAll.addPenalty(getPenalty().negate()); 
                        if(!Utils.isEqual(userLoseAll,p)){
                            reason=CommonMoneyReasonUtils.DEN;
                        }
                        
                    }
                    SamPlayer samLost=getXamPlayer(userLostMoney);
                    if (updateMoney(userLostMoney, getPenalty().negate(), lostVi + getPenaltyDesVi(), lostEn + getPenaltyDesEn(),
                            lostZh + getPenaltyDesZh(), reason,BigDecimal.ZERO,samLost==null ? null : xamPlayer.getCardsToList())) {
                        total = Utils.add(total, getPenalty());
                    }
                    setPenalty(BigDecimal.ZERO);
                }
            }

            if (total.signum() > 0) {
                BigDecimal []arrResultMoney = setMoneyMinusTax(total, getTax());
                //Set penalty cho player để trả về khi ket thuc game
                SamPlayer winnerPlayer = getXamPlayer(getIdDBOfUser(player));
                winnerPlayer.addPenalty(arrResultMoney[MONEY]);
                String textVi=SamLanguage.getMessage(SamLanguage.BONUS_INSTANT_LOSE, GlobalsUtil.VIETNAMESE_LOCALE);
                String textEn=SamLanguage.getMessage(SamLanguage.BONUS_INSTANT_LOSE, GlobalsUtil.ENGLISH_LOCALE);
                String textZh=SamLanguage.getMessage(SamLanguage.BONUS_INSTANT_LOSE, GlobalsUtil.CHINESE_LOCALE);
                
                updateMoney(player, arrResultMoney[MONEY],textVi ,textEn, textZh, CommonMoneyReasonUtils.TONG_TIEN_CONG,arrResultMoney[TAX],winnerPlayer.getCardsToList());
                sendRankingData(player, arrResultMoney[TAX].doubleValue(), 1);
                updateAchievement(player, CommonMoneyReasonUtils.THANG);
            }
        } catch (Exception e) {
            log.error("XamGame.xetCong error:" + boardLog.getLog(), e);
        }
    }

    /**
     * Xét bài: đếm số bài trên tay + xét úng heo+ úng tứ quy
     */
    public void xetBai() {
        try {
            BigDecimal total = BigDecimal.ZERO;
            String lostVi;
            String lostEn;
            String lostZh;
            // có thằng đền bài
            for (User p : getPlayingPlayers()) {
                SamPlayer xamPlayer = getXamPlayer(p);
                if (!Utils.isEqual(winner, p) && !xamPlayer.isLeaveState()
                        && xamPlayer.getCards().size() < DEFAULT_NUMBER_XAM_CARD) {
                    if (denBaiUser != null && !Utils.isEqual(p, denBaiUser)) {
                        lostVi = SamLanguage.getMessage(SamLanguage.COMPENSATE, GlobalsUtil.VIETNAMESE_LOCALE);
                        lostEn = SamLanguage.getMessage(SamLanguage.COMPENSATE, GlobalsUtil.ENGLISH_LOCALE);
                        lostZh = SamLanguage.getMessage(SamLanguage.COMPENSATE, GlobalsUtil.CHINESE_LOCALE);
                    } else {
                        lostVi =SamLanguage.getMessage(SamLanguage.LOSE, GlobalsUtil.VIETNAMESE_LOCALE)+"";
                        lostEn = SamLanguage.getMessage(SamLanguage.LOSE, GlobalsUtil.ENGLISH_LOCALE)+"";
                        lostZh = SamLanguage.getMessage(SamLanguage.LOSE, GlobalsUtil.CHINESE_LOCALE)+"";
                    }
                    setPenaltyDesVi("");
                    setPenaltyDesEn("");
                    setPenaltyDesZh("");
                    //Xét heo
                    xetHeo(xamPlayer);
                    //xét tứ quý
                    xetTuQuy(xamPlayer);
                    //Thua phạt số bài trên tay x tiền cược
                    addPenalty(getBoardMoney().getNormalMoney(xamPlayer.getCards().size()));
                    User userLostMoney = denBaiUser == null ? xamPlayer.getUser() : denBaiUser;
                    BigDecimal value = getCanWinOrLoseMoney(userLostMoney, getPenalty());
                               value = value.min(getMoneyFromUser(userLostMoney));
                               value = getCanWinOrLoseMoney(winner, value);
                    
                    setPenalty(value);
                    
                    int reason=CommonMoneyReasonUtils.THUA;
                    //Set penalty cho player để trả về khi ket thuc game
                    if (denBaiUser == null) {
                        xamPlayer.setPenalty(getPenalty().negate());
                    } else {
                        SamPlayer xamPlayerLoseAll = getXamPlayer(denBaiUser);
                        if (xamPlayerLoseAll != null) {
                            xamPlayerLoseAll.addPenalty(getPenalty().negate());
                            if (!Utils.isEqual(p, denBaiUser)) {
                                reason=CommonMoneyReasonUtils.DEN;
                            }
                        }
                    }
                    lostVi = lostVi + (getPenaltyDesVi().isEmpty()?"":": "+getPenaltyDesVi());
                    lostEn = lostEn + (getPenaltyDesEn().isEmpty()?"":": "+getPenaltyDesEn());
                    lostZh = lostZh + (getPenaltyDesZh().isEmpty()?"":": "+getPenaltyDesZh());
                    SamPlayer samLoster=getXamPlayer(userLostMoney);
                    if (updateMoney(userLostMoney, getPenalty().negate(), lostVi, lostEn, lostZh,reason ,BigDecimal.ZERO,samLoster==null? null:xamPlayer.getCardsToList())) {
                        total = Utils.add(total, getPenalty());
                    }
                    setPenalty(BigDecimal.ZERO);
                }
            }
            //tiền phạt rời bàn
            if (boardMoney.getMoneyPot().signum() > 0) {
                total = Utils.add(total, boardMoney.getMoneyPot());
                total = total.min(updateMaxMoneyCanRecive(winner));
                boardMoney.setMoneyPot(BigDecimal.ZERO);
            }

            if (total.signum() > 0) {
                BigDecimal []arrResulMoney = setMoneyMinusTax(total, getTax());
                //Set penalty cho player để trả về khi ket thuc game
                SamPlayer winnerPlayer = getXamPlayer(winner);
                winnerPlayer.addPenalty(arrResulMoney[MONEY]);
                updateMoney(winner, arrResulMoney[MONEY], "", "", "", CommonMoneyReasonUtils.THANG,arrResulMoney[TAX],winnerPlayer.getCardsToList());
                sendRankingData(winner, arrResulMoney[TAX].doubleValue(), 1);
                updateAchievement(winner, CommonMoneyReasonUtils.THANG);
            }
        } catch (Exception e) {
            log.error("XamGame.xetBai error" + boardLog.getLog(), e);
        }
    }

    /**
     * Lấy số win thắng được nếu báo xâm thành công
     *
     * @return
     */
    private BigDecimal getXamSuccessMoney() {
        BigDecimal total = BigDecimal.ZERO;
        for (User p : getPlayingPlayers()) {
            SamPlayer xPlayer = getXamPlayer(p);
            if (!Utils.isEqual(p, xamUser) && !xPlayer.isLeaveState()) {
                //số tiền ăn của mỗi user khi báo xâm
                BigDecimal xamMoneysuccess = BigDecimal.ZERO;
                //Xét heo
                int countHeo = SamCardUtils.countHeo(xPlayer.getCards());
                if (countHeo > 0) {
                    xamMoneysuccess = Utils.add(xamMoneysuccess, getBoardMoney().getUngHeoMoney(countHeo));
                }
                //xét tứ quý
                int countFourOfAKind = SamCardUtils.countFourOfAKind(xPlayer.getCards());
                if (countFourOfAKind > 0) {
                    xamMoneysuccess = Utils.add(xamMoneysuccess, getBoardMoney().getUngTuQuyMoney(countHeo));
                }
                if (countHeo > 0 || countFourOfAKind > 0) {
                    logText += "- " + xPlayer.getUser().getName()+ ": 10 lá, ";
                    logText += countHeo > 0 ? (countHeo + " heo") : "";
                    if (countHeo <= 0) {
                        logText += getTextUpdated(" ", countFourOfAKind > 0 ? (countFourOfAKind + " tứ quý") : "");
                    } else {
                        logText += getTextUpdated(", ", countFourOfAKind > 0 ? (countFourOfAKind + " tứ quý") : "");
                    }
                    logText += ".\n";
                }
                xamMoneysuccess =  Utils.add(xamMoneysuccess,getBoardMoney().getXamSuccessMoney());
                total = Utils.add(total,getCanWinOrLoseMoney(xamUser, xamMoneysuccess));
            }
        }
        return total;
    }
    /**
     * Xâm thành công: ăn 20 lần tiền cược mỗi nhà + xét heo+ xét tứ quý
     */
    public void xamSuccess() {
        try {
            String lostVi = "";
            String lostEn = "";
            String lostZh = "";
            //Tổng tiền thua cóng
            BigDecimal total = BigDecimal.ZERO;
            User userLoseAll = findUserDenBai();

            for (User p : getPlayingPlayers()) {
                SamPlayer xamPlayer = getXamPlayer(p);
                if (!Utils.isEqual(p, winner) && !xamPlayer.isLeaveState()) {
                    lostVi="";
                    lostEn="";
                    lostZh = "";
                    setPenaltyDesVi("");
                    setPenaltyDesEn("");
                    setPenaltyDesZh("");
                    //Xét heo
                    xetHeo(xamPlayer);
                    //xét tứ quý
                    xetTuQuy(xamPlayer);
                    addPenalty(getBoardMoney().getXamSuccessMoney());
                    User userLostMoney = userLoseAll == null ? xamPlayer.getUser() : userLoseAll;
                    BigDecimal value = getCanWinOrLoseMoney(userLostMoney, getPenalty());
                               value = getMoneyFromUser(userLostMoney).min(value);
                               value = getCanWinOrLoseMoney(winner, value);
                            
                    setPenalty(value);
                    
                    int reason=CommonMoneyReasonUtils.THUA;
                    //Set penalty cho player để trả về khi ket thuc game
                    if (userLoseAll == null) {
                        xamPlayer.setPenalty(getPenalty().negate());
                    } else {
                        // có thằng đền bài
                        SamPlayer xamPlayerLoseAll = getXamPlayer(userLoseAll);
                        xamPlayerLoseAll.addPenalty(getPenalty().negate());
                        if (!Utils.isEqual(p, userLoseAll)) {
                            reason=CommonMoneyReasonUtils.DEN;
                        }
                    }
                    String textVi=lostVi +" "+getPenaltyDesVi()+".";
                    String textEn=lostEn + getPenaltyDesEn();
                    String textZh=lostZh + getPenaltyDesZh();
                    
                    SamPlayer samLoster=getXamPlayer(userLostMoney);
                    if (updateMoney(userLostMoney, getPenalty().negate(),textVi ,textEn, textZh,reason,BigDecimal.ZERO,samLoster==null? null:xamPlayer.getCardsToList())) {
                        total = Utils.add(total, getPenalty());
                    }
                    setPenalty(BigDecimal.ZERO);
                }
            }

            //nếu có user thoát bàn
            if (boardMoney.getMoneyPot().signum() > 0) {
                total = Utils.add(total, boardMoney.getMoneyPot());
                total = total.min(updateMaxMoneyCanRecive(winner));
                boardMoney.setMoneyPot(BigDecimal.ZERO);
            }

            if (total.signum() > 0) {   
                BigDecimal []arrResultMoney=setMoneyMinusTax(total, getTax());
                //Set penalty cho player để trả về khi ket thuc game
                SamPlayer winnerPlayer = getXamPlayer(winner);
                winnerPlayer.setPenalty(arrResultMoney[MONEY]);
                String textVi=SamLanguage.getMessage(SamLanguage.XAM_SUCCESS, GlobalsUtil.VIETNAMESE_LOCALE);
                String textEn=SamLanguage.getMessage(SamLanguage.XAM_SUCCESS, GlobalsUtil.ENGLISH_LOCALE);
                String textZh=SamLanguage.getMessage(SamLanguage.XAM_SUCCESS, GlobalsUtil.CHINESE_LOCALE);
                updateMoney(winner, arrResultMoney[MONEY],textVi ,textEn, textZh, CommonMoneyReasonUtils.THANG,arrResultMoney[TAX],winnerPlayer.getCardsToList());
                updateAchievement(winner, CommonMoneyReasonUtils.THANG);
                sendRankingData(winner, arrResultMoney[TAX].doubleValue(), 1);
            }
        } catch (Exception e) {
            log.error("XamGame.xetBai error" + boardLog.getLog(), e);
        }
    }

    /**
     * Phat tien neu xam that bại
     */
    public void phatXamFail() {
        try {
            if (xamSuccessMoney.signum() > 0) {
                xamSuccessMoney = xamSuccessMoney.min(updateMaxMoneyCanRecive(getCurrentPlayer()));

                xamSuccessMoney = getMoneyFromUser(getPlayerBiChat()).min(xamSuccessMoney);
                //Set penalty cho player để trả về khi ket thuc game
                SamPlayer xamFailPlayer = getXamPlayer(getPlayerBiChat());
                xamFailPlayer.setPenalty(xamSuccessMoney.negate());
                String textVi=SamLanguage.getMessage(SamLanguage.XAM_FAIL, GlobalsUtil.VIETNAMESE_LOCALE);
                String textEn=SamLanguage.getMessage(SamLanguage.XAM_FAIL, GlobalsUtil.ENGLISH_LOCALE);
                String textZh=SamLanguage.getMessage(SamLanguage.XAM_FAIL, GlobalsUtil.CHINESE_LOCALE);
                updateMoney(getPlayerBiChat(), xamSuccessMoney.negate(),textVi ,textEn, textZh, CommonMoneyReasonUtils.THUA,BigDecimal.ZERO,xamFailPlayer.getCardsToList());
            }
            //Update tiền thưởng cho thằng chặn Xâm 
            SamPlayer chanXamPlayer = getXamPlayer(getCurrentPlayer());
            if (boardMoney.getMoneyPot().signum() > 0) {
                xamSuccessMoney = (Utils.add(xamSuccessMoney, boardMoney.getMoneyPot()));
                xamSuccessMoney = xamSuccessMoney.min(updateMaxMoneyCanRecive(getCurrentPlayer()));
                boardMoney.setMoneyPot(BigDecimal.ZERO);
            }
            
            BigDecimal []arrResultMoney=setMoneyMinusTax(xamSuccessMoney, getTax());
            xamSuccessMoney=arrResultMoney[MONEY];
            chanXamPlayer.setPenalty(xamSuccessMoney);
            String textVi=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE) + SamLanguage.getMessage(SamLanguage.DEFEAT_XAM, GlobalsUtil.VIETNAMESE_LOCALE);
            String textEn=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE) + SamLanguage.getMessage(SamLanguage.DEFEAT_XAM, GlobalsUtil.ENGLISH_LOCALE);
            String textZh=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.CHINESE_LOCALE) + SamLanguage.getMessage(SamLanguage.DEFEAT_XAM, GlobalsUtil.CHINESE_LOCALE);
            updateMoney(chanXamPlayer.getUser(), xamSuccessMoney, textVi, textEn, textZh, CommonMoneyReasonUtils.THANG_DEN_BAI,arrResultMoney[TAX],chanXamPlayer.getCardsToList());
            sendRankingData(chanXamPlayer.getUser(), arrResultMoney[TAX].doubleValue(), 1);
            updateAchievement(chanXamPlayer.getUser(), CommonMoneyReasonUtils.THANG);
        } catch (Exception e) {
            log.error("XamGame.phatXamFail error " + boardLog.getLog(), e);
        }
    }
    /**
     * Phạt khi thằng báo Xâm rời bàn
     *
     * @param xamPlayerLeave
     */
    public void phatXamPlayerLeave(SamPlayer xamPlayerLeave) {
        try {
            //tổng số tiền user được đền
            BigDecimal total = BigDecimal.ZERO;
            //số tiền từng user được đền
            Map<String, BigDecimal> moneyDen = new HashMap<>();
            
            for (User p : getPlayingPlayers()) {
                String idDBPlayer=getIdDBOfUser(p);
                SamPlayer xamPlayer = getXamPlayer(idDBPlayer);
                if (!xamPlayer.isLeaveState() && !Utils.isEqual(p, xamPlayerLeave.getUser())) {
                    setPenaltyDesVi("");
                    setPenaltyDesEn("");
                    setPenaltyDesZh("");
                    xetHeo(xamPlayer);
                    //xét tứ quý
                    xetTuQuy(xamPlayer);
                    BigDecimal value = Utils.add(getBoardMoney().getXamFailMoney(1), getPenalty());
                               value = getCanWinOrLoseMoney(p, value);
                    moneyDen.put(idDBPlayer, value);
                    total = Utils.add(total, value);
                    if (getPenalty().signum() > 0) {
                        setPenalty(BigDecimal.ZERO);
                    }
                }
            }
            
            BigDecimal moneyBonus = BigDecimal.ZERO;
            if(moneyDen.size() >0){
                moneyBonus = Utils.divide(boardMoney.getMoneyPot(), new BigDecimal(String.valueOf(moneyDen.size())));
                boardMoney.setMoneyPot(BigDecimal.ZERO);
            }

            /**
             * - Nếu trường hợp số tiền đền nhiều hơn số tiền thằng rời bàn có
             * thì lấy tiền của nó đang có chia đều cho những thằng khác - Ngược
             * lại thì thằng nào được đền có heo nhiều hơn thì ăn đúng số heo nó
             * có
             */
            BigDecimal moneyOfLeaver=getMoneyFromUser(xamPlayerLeave.getUser());
                       moneyOfLeaver =  getCanWinOrLoseMoney(xamPlayerLeave.getUser(), moneyOfLeaver);
            if (moneyOfLeaver.compareTo(total) < 0) {
                total = moneyOfLeaver;
                //Set penalty cho player để trả về khi ket thuc game
                xamPlayerLeave.setPenalty(total.negate());
                String textVi = GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.VIETNAMESE_LOCALE) + getPenaltyDesVi();
                String textEn = GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.VIETNAMESE_LOCALE) + getPenaltyDesEn();
                String textZh = GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.VIETNAMESE_LOCALE) + getPenaltyDesZh();
                updateMoney(xamPlayerLeave.getUser(),total.negate(), textVi, textEn, textZh, CommonMoneyReasonUtils.BO_CUOC_KHI_BAO_SAM,BigDecimal.ZERO,null);

                BigDecimal money = Utils.divide(total, new BigDecimal(String.valueOf(getPlayingPlayers().size())));
                money = Utils.add(money, moneyBonus);
                BigDecimal []arrResultMoney = setMoneyMinusTax(money, getTax());
                money = arrResultMoney[MONEY];
                //Số tiền còn lại chia đều cho những thằng khác
                for (User p : getPlayingPlayers()) {
                    String idDBPlayer=getIdDBOfUser(p);
                    SamPlayer xamPlayer = getXamPlayer(idDBPlayer);
                    if (!Utils.isEqual(p, xamPlayerLeave.getUser()) && !xamPlayer.isLeaveState()) {
                        String textwinnerVi=SamLanguage.getMessage(SamLanguage.BONUS,GlobalsUtil.VIETNAMESE_LOCALE);
                        String textwinnerEn=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE);
                        String textwinnerZh=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.CHINESE_LOCALE);
                        updateMoney(xamPlayer.getUser(), money, textwinnerVi , textwinnerEn, textwinnerZh, CommonMoneyReasonUtils.THANG_DEN_BAI,arrResultMoney[TAX],null);
                        updateAchievement(xamPlayer.getUser(), CommonMoneyReasonUtils.THANG);
                        //Set penalty cho player để trả về khi ket thuc game
                        xamPlayer.setPenalty(money);
                    }
                }
            } else {
                //Set penalty cho player để trả về khi ket thuc game
                xamPlayerLeave.setPenalty(total.negate());
                String textVi=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.VIETNAMESE_LOCALE) + getPenaltyDesVi();
                String textEn=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.ENGLISH_LOCALE) + getPenaltyDesEn();
                String textZh=GameLanguage.getMessage(GameLanguage.GIVE_UP,GlobalsUtil.CHINESE_LOCALE) + getPenaltyDesZh();
                updateMoney(xamPlayerLeave.getUser(),total.negate(), textVi , textEn, textZh, CommonMoneyReasonUtils.THUA, BigDecimal.ZERO,null);

                for (User p : getPlayingPlayers()) {
                    String idDBPlayer=getIdDBOfUser(p);
                    SamPlayer xamPlayer = getXamPlayer(idDBPlayer);
                    if (!Utils.isEqual(p, xamPlayerLeave.getUser())  && !xamPlayer.isLeaveState()) {
                        BigDecimal money = Utils.add(moneyBonus, moneyDen.get(idDBPlayer));
                        BigDecimal[] arrResultMoney = setMoneyMinusTax(money, getTax());
                        
                        String textWinnerVi=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.VIETNAMESE_LOCALE);
                        String textWinnerEn=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.ENGLISH_LOCALE);
                        String textWinnerZh=SamLanguage.getMessage(SamLanguage.BONUS, GlobalsUtil.CHINESE_LOCALE);
                        updateMoney(xamPlayer.getUser(), arrResultMoney[MONEY], textWinnerVi, textWinnerEn, textWinnerZh, CommonMoneyReasonUtils.THANG_DEN_BAI,arrResultMoney[TAX] ,null);
                        //Set penalty cho player để trả về khi ket thuc game
                        xamPlayer.setPenalty(arrResultMoney[MONEY]);
                        updateAchievement(xamPlayer.getUser(), CommonMoneyReasonUtils.THANG);
                    }
                }
            }
        } catch (Exception e) {
            log.error("XamGame.phatXamPlayerLeave error " + boardLog.getLog(), e);
        }
    }

    /**
     * Xử lý các trường hợp dẫn tới stop game
     *
     * @return
     */
    public boolean isCanStop() {
        if (!isPlaying()) {
            return false;
        }

        return getInTurnPlayers().size() <= 1;
    }


    /**
     * Gửi thông tin thời gian còn lại để báo Xâm
     */
    public void sendXamTimeInforMessage() {
        try {
            callXamTime=System.currentTimeMillis();
            SFSObject m = gameMessage.getXamTimeInfoMessage();
            sendAllUserMessage(m);
        } catch (Exception e) {
            log.error("", e);
        }
    }
    /**
     * Check min money
     *
     * @param userMoney
     * @param boardMoney
     * @return
     */
    public boolean checkMoney(double userMoney, double boardMoney) {
        double monUpdated =Utils.multiply(boardMoney, getMinJoinGame());
        return userMoney >= monUpdated;
    }

    private void sendNotifyOneCardMessage(String userId) {
        try {
            SFSObject m = gameMessage.getNotifyOneCardMessage(userId);
            sendAllUserMessage(m);
        } catch (Exception ex) {
            log.error(SamGame.class.getName() + " sendNotifyOneCardMessage error", ex);
        }
    }

    /**
     * kiểm tra bài người chơi chỉ còn heo ?
     *
     * @param cards
     * @return
     */
    private boolean checkHaveOnlyHeo(List<Card> cards) {
        if (cards.isEmpty()) {
            return false;
        }
        for (Card c : cards) {
            if (!c.isHeo()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkHavePlayerDontSkipXam() {
        for (User p : getPlayingPlayers()) {
            SamPlayer xp = getXamPlayer(p);
            if (xp.isReadyState()) {
                return true;
            }
        }
        return false;
    }

    /**
     * User ít win hơn tới xâm nếu: - Số win của user bị thua nhiều hơn số win
     * của user tới xâm (có khi start ván) thì user tới xâm chỉ ăn bằng với số
     * win mình có (xét từng user có trong bàn). - Số win của user bị thua ít
     * hơn số win của user tới xâm (có khi start ván) thì user tới xâm ăn bằng
     * bằng đúng số win đó (xét từng user có trong bàn).
     *
     * @param user
     * @param money
     * @return
     */
    private BigDecimal getCanWinOrLoseMoney(User userWin, BigDecimal moneyWin) {
        SamPlayer player = getXamPlayer(getIdDBOfUser(userWin));
        if (player == null) {
            return BigDecimal.ZERO;
        }
        return moneyManagement.getCanWinOrLoseMoney(getIdDBOfUser(userWin), moneyWin);
    }

    /**
     * Kiểm tra xem trong ván player đã báo xâm hết chưa
     *
     * @return
     */
    public boolean isSkipXamFull() {
        return (skipXamCount >= getPlayingPlayers().size());
    }

    /**
     * Lấy ra những user đã tham gia khi start ván hiện tại: user đang chơi và
     * user thoát ngang
     *
     * @return
     */
    private byte getPlayersXamStarted() {
        return (byte) (getPlayingPlayers().size() + countPlayerLeave);
    }

    /**
     * Sử dụng để kiểm tra số tiền tối đa user có thể ăn được
     *
     * @param winner
     * @return
     */
    private BigDecimal updateMaxMoneyCanRecive(User winner) {
        SamPlayer xamWinnerPlayer = getXamPlayer(winner);
        if (xamWinnerPlayer == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = Utils.multiply(moneyManagement.getBettingMoney(getIdDBOfUser(winner)) , new BigDecimal(String.valueOf(getPlayersXamStarted() - 1)));
        //kiểm tra winner phải ăn tối đa tiền phạt user rời bàn =số tiền winner đem vào bàn * số user choi voi nó
        return value;
    }
    /**
     * Xét tứ quý
     *
     * @param xamPlayer
     */
    public void xetTuQuy(SamPlayer xamPlayer) {
        int countTuQuy = SamCardUtils.countFourOfAKind(xamPlayer.getCards());
        if (countTuQuy > 0) {
            addPenalty(getBoardMoney().getUngTuQuyMoney(countTuQuy));
            addPenaltyDesVi(SamLanguage.getMessage(SamLanguage.UNUSED, GlobalsUtil.VIETNAMESE_LOCALE)
                    + countTuQuy +" "+ SamLanguage.getMessage(SamLanguage.FOUR_OF_A_KIND, GlobalsUtil.VIETNAMESE_LOCALE));
            addPenaltyDesEn(SamLanguage.getMessage(SamLanguage.UNUSED, GlobalsUtil.ENGLISH_LOCALE)
                    + countTuQuy +" "+ SamLanguage.getMessage(SamLanguage.FOUR_OF_A_KIND, GlobalsUtil.ENGLISH_LOCALE));
            addPenaltyDesZh(SamLanguage.getMessage(SamLanguage.UNUSED, GlobalsUtil.CHINESE_LOCALE)
                    + countTuQuy +" "+ SamLanguage.getMessage(SamLanguage.FOUR_OF_A_KIND, GlobalsUtil.CHINESE_LOCALE));
        }
    }
    /**
     * Tìm user đánh đầu tiên khi vừa tạo bạn
     * sẽ chọn user có con bài nhỏ nhất
     * @return 
     */
    private User getFirstUser() {
        // tim nguoi danh dau tien
        User firstUser = null;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < getPlayersSize(); i++) {
            User p = getUser(i);
            String idDB=getIdDBOfUser(p);
            if (p != null && getListPlayer().containsKey(idDB)) {
              users.add(p);
            }
        }
        firstUser = users.get(random.nextInt(users.size()));
        setCurrentPlayer(firstUser);
        setCurrentMoveTime();
        
        return firstUser;
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
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user);
    }
    /**
     * Những player đang ngồi trong bàn chơi, ko tính viewer
     *
     * @return List<Player>
     */
    public List<User> getPlayingPlayers() {
        List<User> list = this.getPlayersList();
        Iterator<User> iter = list.iterator();
        while (iter.hasNext()) {
            User player = iter.next();
            if (!isInturn(player)) {
                iter.remove();
            }
        }
        return list;
    }
    /**
     * Lấy ra user thắng trong ván
     * @return 
     */
    public User getWinner() {
        return winner;
    }
    /**
     * Get thời gian còn lại để báo sâm
     * @return 
     */
    public byte getTimeToXam() {
        if (isPlaying()) {
            byte time = (byte) ((System.currentTimeMillis() - getCurrentMoveTime()));
            if (time == 0) {
                return (byte) (SamConfig.getInstance().getXamTimeLimit() / 1000);
            }
            if (time < SamConfig.getInstance().getXamTimeLimit()) {
                return (byte) ((SamConfig.getInstance().getXamTimeLimit() - time) / 1000);
            }
        }
        return 0;
    }

    @Override
    public Locale getLocaleOfUser(User user) {
        return super.getLocaleOfUser(user);
    }

    @Override
    public void sendUserMessage(ISFSObject params, User user) {
        super.sendUserMessage(params, user);
    }
    /**
     * Gửi meessage thông tin bàn khi co user
     * join bàn lúc ván đang playing
     * @param user 
     */
    private void sendMessagePlaying(User user) {
        try {
            SFSObject ob = gameMessage.getPlayingMessage(getPlayingTime()/ 1000, (int) (this.getTimeRemain()), 
                    getIdDBOfUser(getPlayerMoved()), getIdDBOfUser(getCurrentPlayer()), getCardsMoveToList());
            sendUserMessage(ob, user);
        } catch (Exception e) {
            this.log.error("sendMessagePlaying() error: ", e);
        }
    }

    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(SamConfig.getInstance().getMaxViewer());
    }

    @Override
    protected void waiterBuyStack(User user) {
        super.waiterBuyStack(user); 
        addNewSamPlayer(user);
    }
    /**
     * add user đến danh sách game sâm
     *
     * @param user
     */
    private void addNewSamPlayer(User user) {
        listPlayer.put(getIdDBOfUser(user), new SamPlayer(user, this));
        if (isPlaying()) {
            setInturn(user, false);
            SamPlayer xamPlayer = getXamPlayer(user);
            if (isCallXamTime()) {
                xamPlayer.setState(xamPlayer.getSkippedState());
            }
            checkPlayerHaveOneCard();
            sendMessagePlaying(user);
        }
        processCountDownStartGame();
        log.debug("User " + user.getName() + " join board - ListPlayer: " + listPlayer.size());
    }

    @Override
    public int getTimeLimit() {
        return super.getTimeLimit();
    }

    @Override
    protected byte getServiceId() {
        return Service.SAM;
    }

    @Override
    public int getTimeRemain() {
        if(isCallXamTime()){
            return getTimeToXam();
        }
        return super.getTimeRemain();
    }
     
    
}
