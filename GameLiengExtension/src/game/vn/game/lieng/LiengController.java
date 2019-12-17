/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.command.LiengCommand;
import game.key.SFSKey;
import game.vn.common.GameController;
import game.vn.common.GameExtension;
import game.vn.common.card.object.Card;
import game.vn.common.constant.Service;
import game.vn.common.object.BoardLogInGame;
import game.vn.game.lieng.card.LiengCard;
import game.vn.game.lieng.card.LiengDesk;
import game.vn.game.lieng.message.LiengGameMessageFactory;
import game.vn.game.lieng.card.DeckTest;
import game.vn.game.lieng.object.LiengPlayer;
import game.vn.game.lieng.object.LiengPlayerScoreComparator;
import game.vn.game.lieng.object.Round;
import game.vn.game.lieng.utils.BettingUtil;
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
import org.slf4j.Logger;

/**
 * Xử lý logic của game Liêng
 * @author tuanp
 */
public class LiengController extends GameController{

    private Logger log;
    private final static int MAX_ROUND = 4;
    public final static byte MIN_PLAYER = 2;
    public final static byte MAX_PLAYER = 6;
    private User winner;
    //key: userId, value LiengPlayer
    private final Map<String, LiengPlayer> liengPlayers;
    private final List<Round> rounds;
    private LiengGameMessageFactory gameMessage;
    private LiengDesk liengDesk;
    //tien cuoc sau cung
    private BigDecimal lastBet =  BigDecimal.ZERO;
    //số người chơi khi bắt đầu ván
    private int countStartGamePlayers;
    //số người chơi đã úp bỏ, dùng để tính trường hợp stop game khi tất cả úp bỏ
    private int countFold;
    //số người đã thoát khỏi ván: khi leave hoặc ngồi xem chuyển thành viewer
    //chỉ set trường hợp ván đang chơi, bình thường leave ko cần tính
    private int countLeave;
    // ghi lại quá trình từng ván bài để bắt lỗi
    private final BoardLogInGame boardLog;
    // danh sách LiengPlayer chơi từ đầu để xét khi kết thúc game
    private final List<LiengPlayer> moneyPlayers;
    private String logTextLever="";
    public LiengController(Room room, GameExtension gameEx) {
        super(room, gameEx);
        this.gameMessage = new LiengGameMessageFactory(this);
        this.liengPlayers = new HashMap<>();
        this.rounds = new ArrayList<>();
        this.moneyPlayers = new ArrayList<>();
        boardLog = new BoardLogInGame();
        reset();
    }

    /**
     * Sử dụng khi khởi tạo bàn chơi và khi kết thúc ván chơi
     */
    private void reset() {
        moneyPlayers.clear();
        rounds.clear();
        this.countFold = 0;
        this.countLeave = 0;
        this.countStartGamePlayers = 0;
        liengDesk = new LiengDesk();
        gameMessage = new LiengGameMessageFactory(this);
        lastBet = BigDecimal.ZERO;
        for (LiengPlayer player : liengPlayers.values()) {
            player.reset();
        }
        boardLog.clear();
        log=this.game.getLogger();
    }
    
    /**
     * Tổng tiền đã bet của ván
     *
     * @return
     */
    public BigDecimal getTotalBetMoney() {
        BigDecimal total = BigDecimal.ZERO;
        for (Round round : rounds) {
            total = Utils.add(total, round.getTotalBetMoney());
        }
        return total;
    }

    /**
     * Tất cả người chơi đã bet số win bằng nhau
     *
     * @return
     */
    private boolean isAllBettedEqual() {
        //ko tính round đầu tiên
        if (isCanCheck()) {
            return false;
        }

        List<User> list = getPlayingPlayers();
        for (User p : list) {
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(p));
            if(liengPlayer==null){
                continue;
            }
            if (liengPlayer.isAllIn()) {
                continue;
            }
            // có thằng chưa làm hành động j hết
            if (liengPlayer.getLastAction() == LiengPlayer.NOTHING) {
                return false;
            }

            if (liengPlayer.getBetMoney().compareTo(getLastBet()) !=0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lấy số money có thể dùng để bet
     *
     * @param player
     * @return
     */
    public BigDecimal getRemainMoneyToBet(LiengPlayer player) {
        //temp là số tiền cần có để theo
        BigDecimal temp =Utils.subtract(getLastBet(), player.getBetMoney());
        //temp2 là số tiền còn lại để mua phỉnh sau khi theo
        BigDecimal temp2 = Utils.subtract(getMoneyFromUser(player.getUser()),temp);
        return temp2;
    }

    /**
     * Kiểm tra player có thể tố hay ko, lỗi trả về false
     *
     * @param player
     * @return
     */
    public boolean isCanBet(LiengPlayer player) {
        BigDecimal temp2 = getRemainMoneyToBet(player);
        if(temp2.signum() <= 0){
            return false;
        }
        
        List<BigDecimal> list = BettingUtil.getListChips(temp2, getMoney());
        //ko đủ win để tố
        if (list.isEmpty()) {
            return false;
        }
        //vòng cuối và quay về người bắt đầu thì ko đc tố
        if (rounds.size() == MAX_ROUND - 1 && isFinishRound()) {
            return false;
        }
        //vòng cuối ko dc tố
        if (rounds.size() == MAX_ROUND) {
            return false;
        }
        return true;
    }

    public BigDecimal getLastBet() {
        return lastBet;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setLastBet(BigDecimal lastBet) {
        this.lastBet = lastBet;
    }

    public LiengDesk getLiengDesk() {
        return liengDesk;
    }

    public LiengGameMessageFactory getGameMessage() {
        return gameMessage;
    }

    @Override
    public synchronized void leave(User user) {
        int nSeat = getSeatNumber(user);
        User nextUser= nextPlayer(user);
        String idDBLeaver=getIdDBOfUser(user);
        boolean isPlayer = isPlayerState(user);
        super.leave(user);
        try {
            if (nSeat > -1 && isPlayer) {
                if (Utils.isEqual(user, winner)) {
                    winner = null;
                }
                LiengPlayer liengLeaver=getLiengPlayer(idDBLeaver);
                checkAndFoldForLeaver(user, nextUser);
                incrCountLeave(liengLeaver);
                List<Short> arrayCardIDs=liengLeaver.getCardsToList();
                
                if (isPlaying()) {
                    logTextLever += logTextLever.isEmpty() ? user.getName() : ", " + user.getName();
                    updateLogGameForUser(user,CommonMoneyReasonUtils.BO_CUOC,arrayCardIDs);
                }
                checkAndStopGame();  
            }
        } catch (Exception e) {
            log.error("leave error: " + boardLog.getLog(), e);
        } finally {
            this.liengPlayers.remove(idDBLeaver);
            forceLogoutUser(user);
            boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "leave", 0);
        }
    }
    /**
     * Lấy vòng hiện tại của ván chơi
     *
     * @return
     */
    public Round getCurrentRound() {
        if (rounds.isEmpty()) {
            return null;
        }
        return rounds.get(rounds.size() - 1);
    }

    /**
     * lưu sô tiền liengPlayer đã chơi
     *
     * @param liengPlayer
     */
    public void addMoneyPlayers(LiengPlayer liengPlayer) {
        if (!moneyPlayers.contains(liengPlayer)) {
            moneyPlayers.add(liengPlayer);
        }
    }

    /**
     * lấy người thắng trong danh sách những người chơi từ đầu, nếu người có bài
     * cao nhất tố hết win thì lấy người thắng trong những người còn lại dùng
     * trong processStopGame
     *
     * @param listLiengPlayers
     * @return
     * @throws Exception
     */
    private LiengPlayer findWinner() throws Exception {
        if (moneyPlayers.size() == 1) {
            return moneyPlayers.get(0);
        }

        List<LiengPlayer> list = new ArrayList<>();
        for (LiengPlayer liengPlayer : moneyPlayers) {
            // bỏ những thằng đã thoát hoặc ngồi xem hoặc fold
            if (liengPlayer.isLeave() || liengPlayer.isFolded()) {
                continue;
            }
            list.add(liengPlayer);
        }
        if (list.isEmpty()) {
            return null;
        }
        Collections.sort(list, new LiengPlayerScoreComparator());
        return list.get(0);
    }
    public User getWinner() {
        return winner;
    }

    @Override
    public void onReturnGame(User user) {
        try {
            super.onReturnGame(user);
            if (isPlaying()) {
                LiengPlayer player = getLiengPlayer(Utils.getIdDBOfUser(user));
                if (Utils.isEqual(player.getUser(), user)) {
                    player.setUser(user);
                }
                sendUserMessage(player.getReturnMessage(),user);
            }
        } catch (Exception e) {
            log.error("onReturnGame error:" + boardLog.getLog(), e);
        }
    }

    /**
     *
     * @param u
     * @param value
     * @param reasonId
     * @param displayText
     * @param tax
     * @param arrayCardIDS
     * @return
     */
    public boolean updateMoney(User u, BigDecimal value, int reasonId, String displayText,BigDecimal tax,List<Short> arrayCardIDS) {
        if (value.signum() == 0) {
            return false;
        }
        if (super.updateMoney(u, value, reasonId,tax,arrayCardIDS)) {
            //cập nhật số tiền đã tố của người chơi
            if (value.signum() < 0) {
                LiengPlayer liengPlayer=getLiengPlayer(getIdDBOfUser(u));
                if(liengPlayer!=null){
                    liengPlayer.addBetMoney(value.negate());
                }
            }
            sendBonusMoneyMessage(u, value.doubleValue(), displayText);
            log.debug("updateMoney success: user: " + u.getName() + " value: " + value  + "\nboardLog: " + boardLog.getLog());
            return true;
        }
        log.error("updateMoney error: user: " + u.getName() + " value: " + value + "\nboardLog: " + boardLog.getLog());
        return false;
    }

    /**
     * Gửi message bonus money cho cả bàn
     *
     * @param u
     * @param value
     * @param displayText
     */
    public void sendBonusMoneyMessage(User u, double value, String displayText) {
        SFSObject m = getBonusMoney(getIdDBOfUser(u), value, displayText);
        sendAllUserMessage(m);
    }

    @Override
    public synchronized boolean join(User user, String pwd) {
        try {
            if(!super.join(user, pwd)){
                return false;
            }
            addNewLiengPlayers(user);
            processCountDownStartGame();
            boardLog.addLog(user.getName(), getMoneyFromUser(user).doubleValue(), "Viewer Join", 0);
            if (isPlaying()) {
                sendViewerMessage(user);
            }
            return true;
        } catch (Exception e) {
            log.error("processViewerJoinCommand: ", e);
        }
        return false;
    }

    /**
     * Nếu chưa đến lượt player mà player đó leave hoặc standup thì tăng số
     * lượng leave. Trường hợp player vào bàn đang chơi và leave ngay thì không
     * tăng Trường hợp đến lượt player mà player đó leave hoặc standup thì không
     * tăng
     *
     * @param player
     */
    public void incrCountLeave(LiengPlayer player) {
        if (player != null && isPlaying()) {
            // nếu trạng thái hiện tại của player là betting nghĩa là player đó đã tham gia chơi mà chưa fold
            if (player.isBetting()) {
                this.countLeave++;
            }
            player.setState(player.getLeaveState());
        }
    }

    /**
     *
     * @param round
     */
    public void addRound(Round round) {
        rounds.add(round);
    }

    /**
     * Set người thằng tới lượt
     *
     * @param player
     */
    public void setCurrentLiengPlayer(LiengPlayer player) {
        log.debug("LiengGame.setCurrentLiengPlayer: " + player.getUser().getName());
        super.setCurrentPlayer(player.getUser());
    }

    /**
     * Gửi thông tin tiền cược của ván chơi mỗi khi có thay đổi
     */
    public void sendBoardBetMessage() {
        try {
            SFSObject m = gameMessage.getBoardBetMessage();
            sendAllUserMessage(m);
        } catch (Exception e) {
            this.log.error("lieng sendBoardBetMessage() error", e);
        }
    }

    /**
     * Gửi message kết quả của ván
     */
    public void sendResultMessage() {
        try {
            SFSObject mEn = gameMessage.getResultMessage(GlobalsUtil.ENGLISH_LOCALE);
            SFSObject mVi = gameMessage.getResultMessage(GlobalsUtil.VIETNAMESE_LOCALE);
            SFSObject mZh = gameMessage.getResultMessage(GlobalsUtil.CHINESE_LOCALE);
            sendToAllWithLocale(mEn, mVi, mZh);
        } catch (Exception e) {
            this.log.error("Lieng sendResultMessage() error", e);
        }
    }
    @Override
    public void update() {
        super.update();
        try {
            if (isCanStart()) {
                startGame();
                return;
            }
            if (isPlaying() && isTimeout()) {
                User user = getCurrentPlayer();
                if (user == null) {
                    log.error("LiengGame.update: current user is null");
                    log.error("\nBoardLog:" + boardLog.getLog());
                    stopGame();
                    return;
                }
                LiengPlayer player = getLiengPlayer(getIdDBOfUser(user));
                if (player == null) {
                    log.error("LiengGame.update: current player is null");
                    log.error("\nBoardLog:" + boardLog.getLog());
                    stopGame();
                    return;
                }
                log.info("-----------LIENG UPDATE ----------");
                checkAndFold(user);
                checkAndStopGame();
            }
        } catch (Exception e) {
            log.error("update error: " + boardLog.getLog(), e);
        }
    }

    @Override
    public void startGame() {
        try {
            super.startGame();
            processStartGame();
            logTextLever="";
            log.info("-----------LIENG START ----------");
        } catch (Exception e) {
            log.error("startgame error:" + boardLog.getLog(), e);
        }
    }

    /**
     * Gửi message khi bắt đầu ván chơi
     *
     * @throws Exception
     */
    private void sendStartMessage() {
        try {
            SFSObject startMessage = this.gameMessage.getStartGameMessage();
            sendAllUserMessage(startMessage);
        } catch (Exception e) {
            this.log.error("sendStartMessage() error: ", e);
        }
    }


    /**
     * Lấy những người chơi trong bàn, danh sách này có trường hợp user đã
     * disconnect
     *
     * @return
     */
    public List<User> getPlayers() {
        return this.getPlayersList();
    }

    /**
     * Những player đang ngồi trong bàn chơi, ko tính viewer và úp bỏ
     *
     * @return 
     */
    public List<User> getPlayingPlayers() {  
        List<User> list = new ArrayList<>();
        //loại thằng in turn = false và ko all in, tức là những thằng mới vào khi bàn đang chơi
        for (LiengPlayer player : liengPlayers.values()) {
            if (isInturn(player.getUser())) {
                list.add(player.getUser());
            } else {
                if (player.isAllIn()) {
                    list.add(player.getUser());
                }
            }
        }
        return list;
    }

    /**
     * Xử lý các step cần thiết khi bắt đầu game
     *
     * @throws Exception
     */
    private void processStartGame() {
        try {
            reset();
            sendStartMessage();
            printPlayerList();
            addRound(new Round(this));
            for (User player : getPlayers()) {
                this.countStartGamePlayers++;
                LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));
                if(liengPlayer==null){
                    continue;
                }
                if (updateMoney(liengPlayer.getUser(), getMoney().negate(),
                        CommonMoneyReasonUtils.DAT_CUOC, "", BigDecimal.ZERO, null)) {
                    boardLog.addLog(player.getName(), getMoneyFromUser(player).doubleValue(), "Đặt cược", getMoney().doubleValue(), liengPlayer.getStringCards());
                    liengPlayer.setState(liengPlayer.getBettingState());
                    addMoneyPlayers(liengPlayer);
                    getCurrentRound().addBetMoney(getIdDBOfUser(player), getMoney());
                }

                if (getMoneyFromUser(player).signum() == 0) {
                    setInturn(player, false);
                    liengPlayer.setState(liengPlayer.getAllInState());
                }
            }
            setLastBet(getMoney());
            dealCard();
            //send message viewer
            sendViewerMessage(true);
            sendBoardBetMessage();
            //sau khi bắt đầu, chỉ 1 người còn đủ win chơi tiếp thì stop game
            checkAndStopGame();
            if (!isPlaying()) {
                return;
            }
            //set cho no la current player
            User beginner = getBeginner();
            setCurrentPlayer(beginner);
            log.debug("beginner: " + beginner.getName());
            LiengPlayer liengBeginner = getLiengPlayer(getIdDBOfUser(getBeginner()));
            liengBeginner.setDefaultAction();
            //gui message next-turn cho ca ban
            liengBeginner.sendNextTurnMessage();
            //change game state
            setStateGame(getWaittingGameState());
            setCurrentMoveTime();
            sendStartGameViewerMessge();
            winner = null;
        } catch (Exception e) {
            this.log.error("processStartGame() error: ", e);
        }
    }

    /**
     * Người tố đầu tiên, là người thắng của ván trước, hoặc là chủ bàn
     *
     * @return
     */
    private User getBeginner() {
        if (this.winner == null) {
            return this.getOwner();
        }
        return winner;
    }

    /**
     * Lấy liêng player tương ứng với userId
     *
     * @param userId
     * @return
     */
    public LiengPlayer getLiengPlayer(String userId) {
        return this.liengPlayers.get(userId);
    }

    /**
     * Chia bài khi bắt đầu ván chơi
     *
     * @throws Exception
     */
    private void dealCard() throws Exception {
        if(LiengConfig.getInstance().isTest() && LiengConfig.getInstance().getTestCase() > 0) {
            DeckTest deck = new DeckTest();
            deck.reset();
            List<Card> mcards = deck.getTestCase(LiengConfig.getInstance().getTestCase());
            
            int i=0;
            for (User player : getPlayingPlayers()) {
                LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));
                if (liengPlayer == null) {
                    continue;
                }
                if (i == 0) {
                    for (Card c : mcards) {
                        liengPlayer.addCard((LiengCard) c);
                    }
                } else {
                    deck.addFullCard(liengPlayer.getCards());
                }
                
                sendUserMessage(liengPlayer.sendDealCardMessage(), liengPlayer.getUser());
                i++;
            }
        }else {
            for (User player : getPlayingPlayers()) {
                LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));
                if (liengPlayer == null) {
                    continue;
                }
                this.liengDesk.dealCard(liengPlayer);
                sendUserMessage(liengPlayer.sendDealCardMessage(), liengPlayer.getUser());
            }
        }
    }

    /**
     * Gửi message sit với nội dung user ko đủ win
     * @param user
     */
    public void sendNotEnoughWinMessage(User user){
        try {
           SFSObject m = this.gameMessage.getNotEnoughWinMessage(user);
        sendUserMessage(m, user); 
        } catch (Exception e) {
            this.log.error("sendNotEnoughWinMessage() error:", logTextLever);
        }  
    }

    private void printPlayerList() {
        log.debug("print player list");
        for (User player : getPlayers()) {
            log.debug("username: " + player.getName()
                    + " seat: " + getSeatNumber(player));
        }
    }

    /**
     * Kiểm tra nếu còn một người chơi thì stop game, dùng khi leave và stand up
     *
     * @param user
     */
    private void checkAndStopGame() {
        //chỉ còn một người chơi
        if (isPlaying() && isCanStop()) {
            stopGame();
        }
    }

    /**
     * Kiểm tra nếu tới lược user thì úp bỏ, dùng khi leave và stand up
     *
     * @param user
     * @throws Exception
     */
    private void checkAndFold(User user) {
        try {
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(user));
            if(liengPlayer==null){
                return;
            }
            if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                if (!liengPlayer.isBetting()) {
                    log.error(user.getName() + " error State:" + liengPlayer.getState() + " boardlog:" + boardLog.getLog());
                    liengPlayer.setState(liengPlayer.getBettingState());
                }
                liengPlayer.fold();
            }
        } catch (Exception e) {
            log.error("checkAndFold:" + user.getName() + " " + boardLog.getLog(), e);
        }
    }

    @Override
    public  synchronized void stopGame() {
        if (!isPlaying()) {
            return;
        }
        log.debug("-----------LIENG STOP ----------");
        try {
            processStopGame();
        } catch (Exception e) {
            log.error("processStopGame error:" + boardLog.getLog(), e);
        } finally {
            super.stopGame();
            for(User user: getPlayersList()) {
                checkNoActionBetGame(user);
            }
            log.debug("Stop Game!");
        }
        boardLog.clear();
        processCountDownStartGame();
    }

    /**
     * Tìm người tháng ván chơi
     *
     * @throws Exception
     */
    private LiengPlayer determineWinner() throws Exception {
        List<LiengPlayer> list = new ArrayList<>();
        for (User player : getPlayingPlayers()) {
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));
            //null trong trường hợp user đã thoát ra khỏi ván chơi
            if (liengPlayer == null) {
                continue;
            }
            if (!isInturn(liengPlayer.getUser()) && getMoneyFromUser(player).signum() != 0) {
                continue;
            }
            list.add(liengPlayer);
        }
        Collections.sort(list, new LiengPlayerScoreComparator());
        if (list.size() > 0) {
            LiengPlayer liengWinner = list.get(0);
            this.winner = liengWinner.getUser();
            return liengWinner;
        }
        return null;
    }

    /**
     * xử lý trước khi gọi hàm super.stopGame
     *
     * @throws Exception
     */
    private void processStopGame() {
        try {
            LiengPlayer liengWinner = determineWinner();
            if(liengWinner==null){
                return;
            }
            log.debug("GameStopState stopping: winner: " + liengWinner.getUser().getName());
            // số thuế của người thắng
            BigDecimal taxWinner = BigDecimal.ZERO;
            // tổng tiền các player đã bet
            BigDecimal totalBet = getTotalBetMoney();
            // điều kiện để kêt thúc while: moneyPlayers = 1 hoặc thằng thắng chưa hết win
            boolean isFinishCheckMoney = false;
            while (!isFinishCheckMoney) {
                // lấy thằng thắng cao nhất trong danh sách người chơi
                LiengPlayer roundWinner = findWinner();
                //không tìm được người thắng
                if (roundWinner == null) {
                    break;
                }
                // bỏ thằng thắng khỏi danh sách người chơi
                moneyPlayers.remove(roundWinner);

                totalBet=Utils.subtract(totalBet, roundWinner.getBetMoney());
                BigDecimal winMoney = BigDecimal.ZERO;
                BigDecimal moneyRoundWinner = getMoneyFromUser(roundWinner.getUser());
                // nếu thằng thắng trong quá trình chơi hết win
                if (moneyRoundWinner.signum() == 0) {
                    Iterator<LiengPlayer> iter = moneyPlayers.iterator();
                    while (iter.hasNext()) {
                        LiengPlayer player = iter.next();
                        BigDecimal exchange = Utils.subtract(player.getBetMoney(), roundWinner.getBetMoney());
                        if (exchange.signum() < 0) {
                        // thằng theo tố hết toàn bộ số tiền nó có nhưng ít hơn số tiền thằng thắng đã cược
                            //thì thằng thắng chỉ lấy được số tiền tố của thằng theo
                            winMoney = Utils.add(winMoney, player.getBetMoney());
                            // remove player khỏi danh sách moneyPlayers
                            iter.remove();
                        } else {
                            winMoney = Utils.add(winMoney, roundWinner.getBetMoney());
                            // trừ số tiền đã thua cho thằng roundwiner
                            player.addBetMoney(roundWinner.getBetMoney().negate());
                        }
                    }//end while iter.hasNext
                } // end if roundwiner money == 0
                else {
                    isFinishCheckMoney = true;
                    winMoney = totalBet;
                }
                // chỉ còn 1 người trong danh sách players
                if (moneyPlayers.size() == 1) {
                    isFinishCheckMoney = true;
                }
                // tính lại tổng số tiền còn lại trong pot
                totalBet =Utils.subtract(totalBet, winMoney);
                BigDecimal[] arrResultMoney = setMoneyMinusTax(winMoney, getTax());
                winMoney =Utils.add(arrResultMoney[MONEY], roundWinner.getBetMoney());
                roundWinner.addWinMoney(winMoney);
                if (!Utils.isEqual(roundWinner.getUser(), liengWinner.getUser())) {
                    //người thắng vòng không phải la người thắng
                    updateMoney(roundWinner.getUser(), winMoney, CommonMoneyReasonUtils.THANG, "", arrResultMoney[TAX],roundWinner.getCardsToList());
                    updateAchievement(roundWinner.getUser(), CommonMoneyReasonUtils.THANG);
                }else{
                    taxWinner=Utils.add(taxWinner, arrResultMoney[TAX]);
                }
                log.debug("roundWinner: " + roundWinner.getUser().getName() + " winMoney: " + winMoney);
            }// end while

            // trả tiền thừa cho người thua chót
            if (totalBet.signum() > 0) {
                updateMoney(moneyPlayers.get(0).getUser(), totalBet, CommonMoneyReasonUtils.TRA_TIEN, "", BigDecimal.ZERO,moneyPlayers.get(0).getCardsToList());
            }
            updateMoney(liengWinner.getUser(), liengWinner.getWinMoney(),
                    CommonMoneyReasonUtils.THANG, "", taxWinner,liengWinner.getCardsToList());
            addLogTextForLoser(liengWinner);
            sendResultMessage();
            updateAchievement(liengWinner.getUser(), CommonMoneyReasonUtils.THANG);
        } catch (Exception e) {
            this.log.error("processStopGame() error: ", e);
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

    /**
     * tăng số người đã úp bỏ
     */
    public void incrCountFold() {
        this.countFold++;
    }

    @Override
    public int getSeatNumber(String userID) {
        return super.getSeatNumber(userID);
    }

    @Override
    public int getSeatNumber(User user) {
        return super.getSeatNumber(user); 
    }
    
    @Override
    public void nextTurn() {
        super.nextTurn();
    }

    @Override
    public User getCurrentPlayer() {
        return super.getCurrentPlayer();
    }

    /**
     * Kiểm tra đã kết thúc vòng thì thêm vòng mới Dùng khi tố, tố hết và bỏ
     * lượt tố
     *
     * @throws Exception
     */
    private void checkFinishRoundAndAddRound() {
        try {
           if (isPlaying() && isFinishRound() && rounds.size() < MAX_ROUND) {
            addRound(new Round(this));
        }
        } catch (Exception e) {
            this.log.error("checkFinishRoundAndAddRound() error: ", e);
        }
        
    }

    /**
     * Nếu là vòng đầu tiên thì thêm vòng mới vào <br />
     * Dùng khi tố, tố hết và bỏ lượt tố
     *
     */
    private void checkAndAddNewRound() {
        //nếu là vòng đầu tiên thì thêm vòng mới vào
        if (rounds.size() == 1) {
            addRound(new Round(this));
        }
    }

    /**
     * dk 1:Nếu lượt hiện tại đã tố thì thêm vòng mới. Vì một người chỉ tố một
     * lần trong vòng đk 2: số lượng người chơi trong vòng ko tính người đã
     * thoát ra hoặc úp bỏ phải bằng số lượng người in turn và all in
     *
     * @return
     */
    public boolean isFinishRound() {
        if (isAllPlayerBettingAndCheckInground()
                && getCurrentRound().isContain(getIdDBOfUser(getCurrentPlayer()))) {
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra user đã tham gia bet,check,call trong vòng hiện tại hết chưa
     *
     * @return
     */
    public boolean isAllPlayerBettingAndCheckInground() {
        List<User> list = getPlayers();
        Iterator<User> iter = list.iterator();
        //số player đã tham gia trong vòng hiện tại
        int countPlayerBettedAndChecked = 0;
        //loại thằng in turn = false và ko all in
        while (iter.hasNext()) {
            User player = iter.next();
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));

            if (liengPlayer == null) {
                iter.remove();
                continue;
            }
            if (!liengPlayer.isAllIn() && !isInturn(player)) {
                continue;
            }
            if (getCurrentRound().isContain(getIdDBOfUser(player))) {
                countPlayerBettedAndChecked++;
            }
        }

        //all player đã tham gia check,bet,call trong vòng hiện tại
        if (getCurrentRound().getPlayersInGround() == countPlayerBettedAndChecked) {
            return true;
        }
        return false;
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
        //trường hợp tất cả tố đều nhau
        if (isAllBettedEqual()) {
            return true;
        }

        return getPlayingPlayers().size() <= 1;
    }

    /**
     * kiểm tra các điều kiện stop game
     *
     */
    private boolean isCompleteAndStopGame() {
        if (!isPlaying()) {
            return false;
        }
        //nếu tất cả allin
        if (isAllAllIn()) {
            return true;
        }
        //trường hợp tất cả tố đều nhau
        if (isAllBettedEqual()) {
            return true;
        }
        //trường hợp trong ván chỉ còn 1 người
        if (getPlayerInturnAndAllIn().size() <= 1) {
            return true;
        }
        //tất cả allin và còn 1 người chơi trong lượt không phải allin
        if (getPlayingPlayers().size() <= 1 && isFinishRound()) {
            return true;
        }
        return false;
    }

    /**
     * Tất cả đều check thì stop game
     */
    private void checkIsAllCheckedAndStopGame() {
        if (isAllChecked()) {
            stopGame();
        }
    }

    /**
     * Kiểm tra nếu là vòng tố đầu tiên, ko tính vòng mặc định, và chưa ai tố
     * trong vòng
     *
     * @return
     */
    public boolean isCanCheck() {
        if (this.rounds.size() == 1) {
            return true;
        }

        if (this.rounds.size() == 2 && getCurrentRound().getTotalBetMoney().compareTo(BigDecimal.ZERO) == 0
                && !isFinishRound()) {
            return true;
        }

        return false;
    }

    /**
     * Gửi message này mới vào bàn chơi
     * @param user
     */
    public void sendViewerMessage(User user) {
        try {
            SFSObject m = this.gameMessage.getViewerMessage(false);
            sendUserMessage(m, user);
        } catch (Exception e) {
            log.error("sendViewerMessage error: ", e);
        }
    }

    /**
     * Gửi message này khi ván chơi bắt đầu
     *
     * @param isStart
     * @throws Exception
     */
    public void sendViewerMessage(boolean isStart) throws Exception {
        SFSObject m = this.gameMessage.getViewerMessage(isStart);
        sendAllViewer(m);
         for(User user: getAllWaiter()){
             sendUserMessage(m, user);
         }
    }

    /**
     * Trường hợp tất cả bỏ lượt tố <br />
     * Dùng khi Check
     *
     * @return
     */
    public boolean isAllChecked() {
        return isFinishRound() && getCurrentRound().getTotalBetMoney().compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Gửi về danh sách những user inTurn và allin,fold
     *
     * @return
     */
    public List<User> getPlayerInturnAndAllinFold() {

        List<User> list = getPlayers();
        Iterator<User> iter = list.iterator();
        //loại thằng in turn = false và ko all in, tức là những thằng mới vào khi bàn đang chơi
        while (iter.hasNext()) {
            User player = iter.next();
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));

            if (liengPlayer == null) {
                iter.remove();
                continue;
            }
            if (!liengPlayer.isAllIn() && !liengPlayer.isFolded() && !isInturn(player)) {
                iter.remove();
            }
        }
        return list;
    }

    /**
     * Gửi về danh sách những user inTurn và allin
     *
     * @return
     */
    public List<User> getPlayerInturnAndAllIn() {
        List<User> list = getPlayers();
        Iterator<User> iter = list.iterator();
        //loại thằng in turn = false và ko all in
        while (iter.hasNext()) {
            User player = iter.next();
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(player));

            if (liengPlayer == null) {
                iter.remove();
                continue;
            }
            if (!liengPlayer.isAllIn() && !isInturn(player)) {
                iter.remove();
            }
        }
        return list;
    }

    /**
     * Tất cả all in
     *
     * @param args
     */
    private boolean isAllAllIn() {
        List<User> list = getPlayers();
        //loại thằng in turn = false và ko all in
        for (User p : list) {
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(p));
            if (liengPlayer == null) {
                continue;
            }
            if (!liengPlayer.isAllIn()) {
                return false;
            }
        }
        return true;
    }

      /**
     * add log for loser
     * @param liengWinner
     * @param logText 
     */
    private void addLogTextForLoser( LiengPlayer liengWinner){
        try {
            List<User> list = getPlayingPlayers();
            for (User p : list) {
                if(Utils.isEqual(p,liengWinner.getUser())){
                    continue;
                }
                LiengPlayer liengPlayer= getLiengPlayer(getIdDBOfUser(p));
                if(liengPlayer!=null){
                    updateLogGameForUser(p,CommonMoneyReasonUtils.THUA, liengPlayer.getCardsToList());
                }  
            }
        } catch (Exception e) {
            log.error("addLogTextForLoser erro ", e);
        }
        
    }

    @Override
    public void setCurrentPlayer(User currentPlayer) {
        super.setCurrentPlayer(currentPlayer); 
    }
    /**
     * User sẽ fold va chuyển lượt cho user khác
     * khi rời bàn
     * @param user
     * @param nextUser 
     */
    private void checkAndFoldForLeaver(User user, User nextUser) {
        try {
            LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(user));
            if(liengPlayer==null){
                return;
            }
            if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                if (!liengPlayer.isBetting()) {
                    log.error(liengPlayer.getUser().getName() + " error State:" + liengPlayer.getState() + " boardlog:" + boardLog.getLog());
                    liengPlayer.setState(liengPlayer.getBettingState());
                }
                liengPlayer.foldForLeaver(nextUser);
            }
        } catch (Exception e) {
            log.error("checkAndFold:" + user.getName() + " " + boardLog.getLog(), e);
        }
    }

    @Override
    public String getIdDBOfUser(User user) {
        return super.getIdDBOfUser(user); 
    }

    @Override
    public Locale getLocaleOfUser(User user) {
        return super.getLocaleOfUser(user); 
    }

    @Override
    public String getCurrency(Locale lo) {
        return super.getCurrency(lo); 
    }

    
    @Override
    public BigDecimal getMoney() {
        return super.getMoney(); 
    }

    @Override
    public void sendAllUserMessage(ISFSObject params) {
        super.sendAllUserMessage(params);
    }

    @Override
    public void sendUserMessage(ISFSObject params, User user) {
        super.sendUserMessage(params, user);
    }

    @Override
    public synchronized void sendToAllWithLocale(SFSObject engMessage, SFSObject viMessage, SFSObject zhMessage) {
        super.sendToAllWithLocale(engMessage, viMessage, zhMessage);
    }

    @Override
    public void sendToAllWithLocale(User u, double value, String messEn, String messVi) {
        super.sendToAllWithLocale(u, value, messEn, messVi);
    }

    @Override
    public synchronized BigDecimal getMoneyFromUser(User user) {
        return super.getMoneyFromUser(user); 
    }

    @Override
    public void sendToastMessage(String errString, User user, int i) {
        super.sendToastMessage(errString, user, i); 
    }

    @Override
    protected void sendCanNotJoinMessage(User user,int minJoin) {
         sendNotEnoughWinMessage(user);
    }
    

    @Override
    public void processMessage(User player, ISFSObject sfsObj) {
        super.processMessage(player, sfsObj);
        try {
           
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            switch(idAction){
                case LiengCommand.SIT:
                    join(player, "");
                    break;
                case LiengCommand.BET:
                    processBet(player,sfsObj);
                    break;
                case LiengCommand.CALL:
                    processCall(player);
                    break;
                case LiengCommand.CHECK:
                    processCheck(player);
                    break;
                case LiengCommand.FOLD:
                    processFold(player);
                    break;
                
            }
        } catch (Exception e) {
            this.log.error("processMessage error: ", e);
        }

    }
    /**
     * User xử lý action fold
     */
    private void processFold(User user) {
        try {
            if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                checkAndFold(user);
                checkAndAddNewRound();
                boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "fold", 0);
                if (isAllFolded() || isAllChecked()) {
                    stopGame();
                }
                if (!isAllBettedEqual()) {
                    checkFinishRoundAndAddRound();
                }
                if (isCompleteAndStopGame()) {
                    stopGame();
                }
            }
        } catch (Exception e) {
            this.log.debug("processFold error:", e);
        }
    }
    /**
     * Xử lý action user
     * @param user 
     */
    private void processCheck(User user) {
        try {
            if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                if (!isCanCheck()) {
                    return;
                }
                checkAndAddNewRound();
                LiengPlayer liengPlayer=getLiengPlayer(getIdDBOfUser(user));
                if (liengPlayer != null) {
                    liengPlayer.check();
                }
                boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "check", 0);
                checkIsAllCheckedAndStopGame();
            }
        } catch (Exception e) {
            this.log.error("processCheck() error: ", e);
        }
    }
    /**
     * Xử lý action user bet
     * @param user
     * @param sfsObj 
     */
    private void processBet(User user, ISFSObject sfsObj) {
        try {
            if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                BigDecimal betMoney = new BigDecimal(String.valueOf(sfsObj.getDouble(LiengCommand.BET_MONEY)));
                betMoney = Utils.getRoundBigDecimal(betMoney);
                LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(user));
                if (liengPlayer.isWaitingToNextTurn()) {
                    log.error("user: " + user.getName() + " isWaitingToNextTurn" + "\nboardLog:" + boardLog.getLog());
                    return;
                }
                checkAndAddNewRound();
                liengPlayer.bet(betMoney);
                sendBoardBetMessage();
                boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "bet", liengPlayer.getBetMoney().doubleValue());
                log.debug("user: " + user.getName() + " total bet: " + liengPlayer.getBetMoney());
                //trường hợp tất cả tố đều nhau
                if (!isAllBettedEqual()) {
                    checkFinishRoundAndAddRound();
                }
                if (isCompleteAndStopGame()) {
                    stopGame();
                }
            }
        } catch (Exception e) {
            this.log.debug("processBet error:", e);
        }
    }
    /**
     * Xử lý action user call
     * @param user
     */
    private void processCall(User user) {
        try {
            if (isPlaying() && Utils.isEqual(user, getCurrentPlayer())) {
                LiengPlayer liengPlayer = getLiengPlayer(getIdDBOfUser(user));
                if (!liengPlayer.getActions().contains(LiengPlayer.CALL)) {
                    log.error("Wrong action-" + "user: " + user.getName() + " call" + "\nboardLog:" + boardLog.getLog());
                    return;
                }
                checkAndAddNewRound();
                liengPlayer.call();
                sendBoardBetMessage();
                boardLog.addLog(user.getName(),getMoneyFromUser(user).doubleValue(), "call", liengPlayer.getBetMoney().doubleValue());
                log.debug("user: " + user.getName() + " total bet: " + liengPlayer.getBetMoney());
                //trường hợp tất cả tố đều nhau
                if (!isAllBettedEqual()) {
                    checkFinishRoundAndAddRound();
                }
                if (isCompleteAndStopGame()) {
                    stopGame();
                }
            }
        } catch (Exception e) {
            this.log.error("processCall() error: ", e);
        }
    }
    @Override
    public void initMaxUserAndViewer() {
        this.room.setMaxSpectators(LiengConfig.getInstance().getMaxViewer());
    }

    @Override
    public void updateLogGameForUser(User u, int reason, List<Short> arrayCardIds) {
        super.updateLogGameForUser(u, reason, arrayCardIds); 
    }

    @Override
    protected void waiterBuyStack(User user) {
        super.waiterBuyStack(user);
        addNewLiengPlayers(user);
    }
    /**
     * Add user mới vòa danh sách danh sách user game Lieng
     */
    private void addNewLiengPlayers(User user) {
        //đã chuyển từ viewer sang player => thêm vào danh sách Lieng player
        LiengPlayer player = new LiengPlayer(user, this);
        player.incrCountJoinBoard();
        this.liengPlayers.put(getIdDBOfUser(user), player);
    }

    @Override
    public int getMinJoinGame() {
        return super.getMinJoinGame(); 
    }

    @Override
    public int getPlayingTime() {
        return super.getPlayingTime();
    }

    @Override
    protected byte getServiceId() {
        return Service.LIENG;
    }

    @Override
    public String getCurrencyName(Locale lo) {
        return super.getCurrencyName(lo);
    }

    @Override
    public void setInturn(User u, boolean isInturn) {
        super.setInturn(u, isInturn);
    }

    @Override
    public byte getTimeToStart() {
        return super.getTimeToStart(); 
    }
    
    @Override
    protected boolean isCanBuyIn(User player, double buyStackMoney) {
        if (isInturn(player)) {
            return false;
        }
        return super.isCanBuyIn(player, buyStackMoney);
    }
}
