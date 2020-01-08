/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.config.QueueConfig;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.SFSConfig;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.event.BestCardsEventTask;
import game.vn.common.event.EventConfig;
import game.vn.common.event.EventManager;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.api.BotAdvantage;
import game.vn.common.lib.api.PointConvertConfig;
import game.vn.common.lib.contants.PlayMode;
import game.vn.common.lib.contants.UserType;
import game.vn.common.lib.event.UserCardsObj;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.lib.log.BoardDetail;
import game.vn.common.lib.log.InvoiceDetail;
import game.vn.common.lib.log.InvoiceInfo;
import game.vn.common.lib.log.Invoices;
import game.vn.common.lib.log.PlayersDetail;
import game.vn.common.lib.payment.UserBalanceUpdate;
import game.vn.common.lib.ranking.GameDataObj;
import game.vn.common.lib.vip.ListUserSendTax;
import game.vn.common.lib.vip.UserTaxData;
import game.vn.common.log.InsertUserMoneyLog;
import game.vn.common.log.SendVipDataTask;
import game.vn.common.message.MessageController;
import game.vn.common.object.boardhistory.UserMoneyLog;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.queue.QueueServiceApi;
import game.vn.common.queue.QueueServiceVip;
import game.vn.common.service.ShuffleService;
import game.vn.common.state.CountDownStartState;
import game.vn.common.state.IGameState;
import game.vn.common.state.PlayingGameState;
import game.vn.common.state.StoppingGameState;
import game.vn.common.state.WaittingState;
import game.vn.common.thread.ThreadPoolGame;
import game.vn.common.user.state.MagagerUserState;
import game.vn.util.db.Database;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.HazelcastUtil;
import game.vn.util.GlobalsUtil;
import game.vn.util.Utils;
import game.vn.util.db.UpdateMoneyResult;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class này xử lý logic chung cho tất cả các game
 *
 * @author 
 */
public abstract class GameController implements Runnable {

    protected static final int MONEY = 0;
    protected static final int TAX = 1;
    private static final int SEAT_UNKNOW=-1;
    private final Object LOCK = new Object();

    //danh sách user bao gòm: playerState, waiterState
    private final Map<Integer, User> players;
    
    protected final GameExtension game;
    protected final Room room;
    protected final MessageController messageController;
    protected long startTime;

    /**
     * Dùng để quản lý chổ ngồi cho user key: chổ ngồi, value:idDB
     */
    private final String[] playerSeats;
    /**
     * Tổng số tiền đã trừ trong bàn, nếu số tiền muốn cộng cho người chơi lớn
     * hơn số này thì ko cho cộng
     */
    private BigDecimal minusMoney = BigDecimal.ZERO;
    private BigDecimal penalize = BigDecimal.ZERO;
    private BigDecimal sumTax = BigDecimal.ZERO;
    private BigDecimal sumBet = BigDecimal.ZERO;        // tổng tiền user bị trừ (đặt cược, bị chặt heo, thúi heo ...)
    private BigDecimal sumReceive = BigDecimal.ZERO;    // tổng tiền user nhận được (thưởng cuối ván, chặt heo, ...)
    /**
     * message queue dung de xu ly trong game.
     */
    private final BlockingQueue<Object> messages;
    // sử dụng để stop thread
    private final AtomicBoolean isStop;
    /**
     * dung de log luc khoi tao game.
     */
    private final Object initializeSessionLock = new Object();
    /**
     * status de biet ban nay da khoi tao hay chua.
     */
    private AtomicBoolean initialization = new AtomicBoolean(false);
    private AtomicBoolean isEnoughPlayersToStart = new AtomicBoolean(false);

    /**
     * thoi gian danh cua lan truoc.
     */
    private long currentMoveTime;
    private int timeLimit = 20000;

    //các trạng thái của game
    private IGameState gameState;
    private final IGameState playingGameState;
    private final IGameState stoppingGameState;
    private final IGameState waittingState;
    private final IGameState countDownState;

    private User currentPlayer;
    protected Executor executorLog;

    //ghi log và lịch sử game
    protected Invoices invoices;
    protected ListUserSendTax listUserSendTax;

    //thông tin buy stack
    private List<InvoiceDetail> invoiceBuyStackDetails = new ArrayList<>();
    private List<BoardDetail> boardBuyStackDetails = new ArrayList<>();

    private String nameLobby;
    private BigDecimal money = BigDecimal.ZERO;
    
    //event trong game
    private final EventManager eventManager;
    private  int moneyType;
    
    //lợi thế chia bài cho bot
    private BotAdvantage botAdv ;
    private final Random random;
    
    public GameController(Room room, GameExtension gameEx) {
        this.room = room;
        this.game = gameEx;
        
        initMaxUserAndViewer();
        moneyType = this.room.getVariable(RoomInforPropertiesKey.MONEY_TYPE).getIntValue();
        nameLobby = Utils.getLobbyName(getServiceId(), getMoneyType());
        this.room.setMaxUsers(getMaxPlayers());
        
        this.messageController = new MessageController(this);
        this.playerSeats = new String[getPlayersSize()];
        try {
            money =  new BigDecimal(String.valueOf(this.room.getVariable(RoomInforPropertiesKey.BET_BOARD).getDoubleValue()));
            money = Utils.getRoundBigDecimal(money);
            
            BigDecimal maxBetMoney = Utils.multiply(getMoney(), new BigDecimal(String.valueOf(getMaxBet())));
            RoomVariable ver = new SFSRoomVariable(RoomInforPropertiesKey.MAX_BET_BOARD, maxBetMoney.doubleValue(),true, false, false);
            this.room.setVariable(ver);

            initStartGame();
        } catch (SFSVariableException ex) {
            this.game.getLogger().error("set Variable for room error: ", ex);
        }

        //state in game
        this.playingGameState = new PlayingGameState();
        this.stoppingGameState = new StoppingGameState();
        this.waittingState = new WaittingState();
        this.countDownState = new CountDownStartState();
        this.gameState = this.stoppingGameState;
        this.executorLog = Executors.newFixedThreadPool(1);
        messages = new LinkedBlockingQueue<>();
        players = new ConcurrentHashMap<>();
        isStop = new AtomicBoolean(true);
        setTimeLimit(getPlayingTime());
        invoices = new Invoices(ServerConfig.getInstance().getConnectionId(), getServiceId(), ServerConfig.getInstance().getServerId(), ServerConfig.getInstance().getMerchantId());
        eventManager = new EventManager();
        random= new Random();
        botAdv = new BotAdvantage();
        if (ServerConfig.getInstance().isPOpenBot()) {
            botAdv = Database.instance.getBotAdvance(getServiceId(), (byte) getMoneyType());
        }
    }
    
    private void initStartGame() {
        penalize = new BigDecimal(String.valueOf(RoomConfig.getInstance().getPennalizeFactor(getNameLobby())));
        penalize = Utils.multiply(penalize, getMoney());
        sumTax = BigDecimal.ZERO;
        sumBet = BigDecimal.ZERO;
        sumReceive = BigDecimal.ZERO;
        this.invoiceBuyStackDetails.clear();
        this.boardBuyStackDetails.clear();
    }

    /**
     * Tat ca cac game deu phai implement function nay cho chuc nang tro lai
     * game cua user. chỉ được trở lại game khi ván isPlayer và ván đang chơi
     *
     * @param user
     */
    public void onReturnGame(User user) {
        try {
            int seat = getSeatNumber(user);
            User oldUser = getUser(seat);
            if (oldUser == null) {
                kickUser(user, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user)));
                return;
            }
            List<UserVariable> vars = oldUser.getVariables();
            if (Utils.isEqual(user, getCurrentPlayer())) {
                setCurrentPlayer(user);
            }
            user.setVariables(vars);
            user.setProperty(UserInforPropertiesKey.IN_TURN, oldUser.getProperty(UserInforPropertiesKey.IN_TURN));
            user.setProperty(UserInforPropertiesKey.USER_STATE, oldUser.getProperty(UserInforPropertiesKey.USER_STATE));
            user.setProperty(UserInforPropertiesKey.COUNT_NO_ACTION_TIME, oldUser.getProperty(UserInforPropertiesKey.COUNT_NO_ACTION_TIME));
            user.setProperty(UserInforPropertiesKey.USER_TYPE, oldUser.getProperty(UserInforPropertiesKey.USER_TYPE));
            user.setProperty(UserInforPropertiesKey.MONEY_STACK, oldUser.getProperty(UserInforPropertiesKey.MONEY_STACK));
            user.setProperty(UserInforPropertiesKey.IS_WAITER, oldUser.getProperty(UserInforPropertiesKey.IS_WAITER));
            user.setProperty(UserInforPropertiesKey.SEAT_USER, oldUser.getProperty(UserInforPropertiesKey.SEAT_USER));
            players.put(seat, user);
            //set lai onwer
            if (Utils.isEqual(user, getOwner())) {
                setOwner(user);
            }
            sendJoinBoardMessage(user);
            sendAddPlayerMessage(user);
        } catch (Exception e) {
            this.game.getLogger().error("GameController.onReturnGame() error: ", e);
        }
    }

    public IGameState getPlayingGameState() {
        return playingGameState;
    }

    public IGameState getStoppingGameState() {
        return stoppingGameState;
    }

    public IGameState getWaittingGameState() {
        return waittingState;
    }

    public IGameState getCountDownStartGameState() {
        return countDownState;
    }

    public void setStateGame(IGameState state) {
        boolean checkPlaying = isPlaying();
        gameState = state;
        //nêu thai đổi trạng thái thì update lại
        if (isPlaying() != checkPlaying) {
            //update thông tin board
            updateBoardInforToHazelcast();
        }
    }

    public IGameState getGameState() {
        return gameState;
    }

    public void startGame() {
        try {
            synchronized (LOCK) {
            this.game.getLogger().info("start game: "+this.room.getName());
            this.minusMoney = BigDecimal.ZERO;
            isEnoughPlayersToStart.set(false);
            boolean haveBot = false;
                setStateGame(this.getPlayingGameState());
            /**
             * set user in turn tăng số ván chờ của user ngồi giữ bàn
             */
            for (User u : this.getAllPlayers()) {
                if(isBot(u)){
                    haveBot = true;
                }
                //user không đủ tiền chơi
                if (!isPlayerState(u)) {
                    setQuickPlayOfUser(u, true);
                    setInturn(u, false);
                    //tăng số bàn chờ
                    updateCountWaitingBoard(u, getManagerUsersState(u).getStateCurrent().getWaittingBoard() + 1);
                    continue;
                }
                getManagerUsersState(u).setPlayerState();
                setInturn(u, true);
                setQuickPlayOfUser(u, false);
                //update trang thái bàn user đang chơi
                updateBoardPlayingToHazelcast(u, true);
                if (getMoneyFromUser(u).compareTo(getMoneyToContinue()) < 0){
                    addToWaitingUserList(u, "");
                }
            }
                
            setTimeLimit(getPlayingTime());
            initBoardLog();
            startTime = System.currentTimeMillis();
            if(haveBot && ServerConfig.getInstance().isPOpenBot()){
                botAdv = Database.instance.getBotAdvance(getServiceId(), (byte) getMoneyType());
            }else{
                botAdv.setEnable(false);
                botAdv.setAdvRatio(0);
                botAdv.setMinPoint(0);
            }
            }

        } catch (Exception e) {
            this.game.getLogger().error("GameController.startGame() error: ", e);
        }
    }
    
    /**
     * Send message start game cho những user là viewer
     */
    protected void sendStartGameViewerMessge() {
        try {
            SFSObject ob = messageController.geStartGameViewerMessage(getPlayersList(),getPlayingTime()/1000, getIdDBOfUser(getCurrentPlayer()));
            for(User user: getAllWaiter()){
                sendUserMessage(ob,user);
            }
            sendAllViewer(ob);
        } catch (Exception e) {
            this.game.getLogger().error("GameController.sendStartGameMessge() error: ", e);
        }
    }

    /**
     * Khởi tạo log ván game khi start ván
     */
    private void initBoardLog() {
        try {
            invoices.reset();

            invoices.setCreatedAt(System.currentTimeMillis() / 1000);
            invoices.setRequestId(Utils.md5String(this.room.getName() + String.valueOf(System.currentTimeMillis())));

            List<PlayersDetail> playerInforList = new ArrayList<>();
            List<String> idDBUsers = new ArrayList<>();
            for (User u : this.getAllPlayers()) {
                if (isPlayerState(u)) {
                    PlayersDetail playerInfor = new PlayersDetail();
                    playerInfor.setPlayerId(Utils.getIdDBOfUser(u));
                    playerInfor.setPlayerName(getUserName(u));
                    playerInfor.setBetUnit(getMoneyType());
                    playerInfor.setBetCredit(getMoney().doubleValue());
                    playerInfor.setCreditAfter(getMoneyFromUser(u).doubleValue());
                    playerInfor.setCreditBefore(getMoneyFromUser(u).doubleValue());
                    playerInfor.setResultId(CommonMoneyReasonUtils.THUA);

                    playerInforList.add(playerInfor);
                    idDBUsers.add(getIdDBOfUser(u));
                }
            }
            invoices.setPlayerIds(idDBUsers);
            invoices.setPlayersDetail(playerInforList);
            invoices.setInvoiceId(Utils.md5String(this.room.getName() + this.room.getId() +String.valueOf(System.currentTimeMillis())));

            InvoiceInfo invoiceInfo = new InvoiceInfo();
            invoiceInfo.setCredit(getMoney().doubleValue());
            invoiceInfo.setTax(0);
            invoices.setInvoiceInfo(invoiceInfo);

            listUserSendTax = new ListUserSendTax();

        } catch (Exception e) {
            this.game.getLogger().error("GameController.initLogBoard() error: ", e);
        }
    }

    /**
     * Add bàn đang chơi của user đến hazelcast
     *
     * @param user
     */
    private void addBoardPlayingToHazelCast(User user) {
        String idUser = getIdDBOfUser(user);
        try {
            Board board = new Board();
            board.setBetMoney(getMoney().doubleValue());
            board.setIp(ServerConfig.getInstance().getIP());
            board.setIpWS(ServerConfig.getInstance().getIPWS());
            board.setIsPlaying(false);
            board.setMoneyType(getMoneyType());
            board.setName(this.room.getName());
            board.setPort(SFSConfig.getPort());
            board.setPortWS(SFSConfig.getWsPort());
            board.setServiceId(this.getServiceId());
            board.setZone(this.game.getParentZone().getName());
            board.setServerId((byte)ServerConfig.getInstance().getServerId());

            PlayingBoardManager playingBoard = new PlayingBoardManager();
            playingBoard.setUserId(idUser);
            playingBoard.setBoardPlaying(board);
            playingBoard.setNameLobby(getNameLobby());
            HazelcastUtil.addPlayingBoard(playingBoard);

        } catch (Exception e) {
            this.game.getLogger().error("GameController.addBoardPlayingToHazelCast() error: ", e);
        }
    }

    /**
     * Update trang thái bàn playing
     *
     * @param user
     * @param isPlaying
     */
    private void updateBoardPlayingToHazelcast(User user, boolean isPlaying) {
        try {
            String idUser = getIdDBOfUser(user);
            //update board playing của user
            HazelcastUtil.lockPlayingBoard(idUser);
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(idUser);
            if (playingBoard == null) {
                return;
            }
            Board boardInfor = playingBoard.getBoardPlaying();
            if (boardInfor != null) {
                boardInfor.setIsPlaying(isPlaying);
                HazelcastUtil.updatePlayingBoard(playingBoard);
            }
            HazelcastUtil.unlockPlayingboard(idUser);
        } catch (Exception e) {
            this.game.getLogger().error("GameController.updateBoardPlayingToHazelcast() error: ", e);
        }
    }

    /**
     * remove bàn dang choi khi user thoát bàn 
     *
     * @param user
     */
    protected void removeBoardPlayingToHazelcast(User user) {
        String idUser = getIdDBOfUser(user);
        try {
            //update board playing của user
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(idUser);
            if (playingBoard != null) {
                HazelcastUtil.removePlayingBoard(idUser);
            }
        } catch (Exception e) {
            this.game.getLogger().error("GameController.removeBoardPlayingToHazelcast() error: ", e);
        }
    }

    public void stopGame() {
        try {
            this.game.getLogger().info("stop game: "+this.room.getName());
            processCheckWaittingBoardUser();
            onEventProcess() ;
            checkSendAndAutoBuyStack();
            
            setStateGame(this.getStoppingGameState());
            for (User u : this.getAllPlayers()) {
                //update trang thái bàn user đang chơi
                updateBoardPlayingToHazelcast(u, false);
                setInturn(u, false);
            }
            checkUserLostConnect();
            isEnoughPlayersToStart.set(checkPlayersToStartAndCountDown());
            sendLogGame();
            initStartGame();
            if(getPlayersList().size() <2){
                checkUserAutoLeave();
            }
        } catch (Exception e) {
            this.game.getLogger().error("GameController.stopGame() error: ", e);
        }
    }
    
    /**
     * Gửi log game quan ttkt
     */
    private void sendLogGame() {
        try {
            invoices.setFinishedAt(System.currentTimeMillis() / 1000);
            invoices.getInvoiceInfo().setTax(sumTax.doubleValue());
            invoices.getInvoiceInfo().setBet(sumBet.doubleValue());
            invoices.getInvoiceInfo().setReceive(sumReceive.doubleValue());
            invoices.getInvoiceInfo().setBetUnit(getMoneyType());

            double totalPenalty = 0;
            double playMoney = 0;
            for (PlayersDetail playerDetail : invoices.getPlayersDetail()) {
                String userId = playerDetail.getPlayerId();
                double winAmount = 0;
                double rake = 0;
                double penalty = 0;
                double turnOver = 0;
                for (InvoiceDetail invoiceDetail : invoices.getInvoiceDetail()) {
                    if (invoiceDetail.getReasonId() != CommonMoneyReasonUtils.BUY_STACK && invoiceDetail.getPlayerId().equals(userId)) {
                        if (invoiceDetail.getReasonId() == CommonMoneyReasonUtils.PENALTY_SYTEM) {
                            penalty = invoiceDetail.getValue();
                            totalPenalty = Utils.add(totalPenalty, penalty);
                        } else {
                            winAmount = Utils.add(winAmount, invoiceDetail.getValue());
                            rake = Utils.add(rake, invoiceDetail.getRake());
                            if (invoiceDetail.getRake() > 0) {
                                // hanv: khi có thuế là có tiền thắng, dùng thuế để tính ngược lại tiền thắng trước thuế, tránh trường hợp tiền thắng tính thêm tiền trả cược
                                turnOver = Utils.add(turnOver, Utils.multiply(Utils.divide(invoiceDetail.getRake(), getTax()), 100));
                            }
                        }
                    }
                }
                if (winAmount < 0) {
                    playMoney = Utils.add(playMoney, -winAmount);
                } else {
                    playMoney = Utils.add(playMoney, winAmount);
                }            
                playerDetail.setRake(rake);
                playerDetail.setWinAmount(winAmount);
                playerDetail.setPenalty(penalty);
                playerDetail.setTotalTurnover(turnOver);
                playerDetail.setRoundId(invoices.getInvoiceId());
                playerDetail.setBetId(Utils.md5String(invoices.getInvoiceId() + userId));
                playerDetail.setUserChannel(Database.instance.getUserChannel(userId));
                for (User u : this.getAllPlayers()) {
                    if (userId.equals(Utils.getIdDBOfUser(u))) {
                        playerDetail.setIsBot(Utils.isBot(u));
                        break;
                    }
                }
            }
            invoices.getInvoiceInfo().setPlayMoney(playMoney);
            invoices.getInvoiceInfo().setPenalty(totalPenalty);

            for(InvoiceDetail invoice: invoiceBuyStackDetails){
                invoices.addInvoiceDetail(invoice);
            }
            for(BoardDetail boardDetail: boardBuyStackDetails){
                invoices.addBoardDetail(boardDetail);
            }


            //gửi lên queue để ghi log xuống DB
            executorLog.execute(new InsertUserMoneyLog(invoices));
            updateWithdrawInfo(invoices.clone());
        } catch (Exception e) {
            this.game.getLogger().error("GameController.sendLogGame() error: ", e);
        }
    }

    protected void update() {
        checkMaintainServer();
        if (this.getGameState().equals(this.getCountDownStartGameState()) && getTimeToStart() == RoomConfig.getInstance().getLeaveRoomDelayTime()) {
            checkUserAutoLeave();
    }
    }
    
    private void checkUserAutoLeave() {
        for (User user : getAllPlayers()) {
            MagagerUserState userStateManager = getManagerUsersState(user);
            if (userStateManager.isIsLeaveGame()) {
                leave(user);
            }
        }
    }
    
    protected void checkMaintainServer(){
        //kiểm tra game có đang bảo trì
        if ((RoomConfig.getInstance().isMaintainGame(getServiceId()) || RoomConfig.getInstance().isMaintainAllGame()) && !isPlaying()) {
            for (User user : this.room.getUserList()) {
                kickUser(user,RoomConfig.getInstance().getMaintainInfor(Utils.getUserLocale(user)));
            }
        }
    }

    /**
     * xử lý khi user rời bàn
     *
     * @param playerLeave 
     */
    public synchronized void leave(User playerLeave) {
        if (playerLeave == null) {
            return;
        }
        int seat = getSeatNumber(playerLeave);
        if (seat == -1) {
            return;
        }
        try {
                this.game.getLogger().info("leave: " + getIdDBOfUser(playerLeave));
            //update tong tin bỏ cuộc của user
            if (isPlaying() && isInturn(playerLeave)) {
                updateAchievement(playerLeave, CommonMoneyReasonUtils.BO_CUOC);
            }
            penalizeLeaver(playerLeave);
            processLeaveGame(playerLeave);
            
                if (!isPlaying() && getPlayersList().size() < 2) {
                    checkUserAutoLeave();
                }
        } catch (Exception e) {
            this.game.getLogger().error("GameController.leave() error: ", e);
        }
    }

    /**
     * server đẩy user khỏi bàn khi leaveShuffle
     * @param user 
     * @param boardName 
     */
    public void leaveShuffle(User user, String boardName) {
        if (user == null || getSeatNumber(user) == -1) {
            return;
        }

        try {
            user.setProperty(UserInforPropertiesKey.ON_SHUFFLE, true);
            String userId = Utils.getIdDBOfUser(user);
            game.getLogger().debug("leave shuffle: " + userId);
            processLeaveGame(user);
            HazelcastUtil.removePlayingBoard(userId);
            ShuffleService.addShuffleWaitingUser(userId, getServiceId(), money.doubleValue(), boardName);
        } catch (Exception e) {
            game.getLogger().error("GameController.leaveShuffle error: ", e);
        }
    }

    /**
     * tiền phạt khi rời bàn, hệ thống sẽ lấy tiền này
     * @param playerLeave 
     */
    protected void penalizeLeaver(User playerLeave) {
        try {
            if (isPlaying() && isInturn(playerLeave)) {
                BigDecimal penalizeMoney = getMoneyFromUser(playerLeave).min(getPenalizeLeaver());
                updateMoney(playerLeave, penalizeMoney.negate(), CommonMoneyReasonUtils.PENALTY_SYTEM, BigDecimal.ZERO, null);
                SFSObject engMessage = this.getBonusMoney(getIdDBOfUser(playerLeave), penalizeMoney.negate().doubleValue(), "");
                sendUserMessage(engMessage, playerLeave);
            }
        } catch (Exception e) {
            this.game.getLogger().error("GameController.penalizeLeaver() error: ", e);
        }
    }
    
    /**
     * -Force user log out khỏi server game 
     * -Trả tiền lại cho user
     *
     * @param playerLeave
     */
    protected void forceLogoutUser(User playerLeave) {
        try {
            String idUser = Utils.getIdDBOfUser(playerLeave);
            if (HazelcastUtil.getPlayingBoard(idUser) != null) {
                //trả tiền lại cho user
                repayMoneyStask(playerLeave);
                HazelcastUtil.removePlayingBoard(idUser);
            }
        } catch (Exception e) {
            this.game.getLogger().error("GameController.forceLogoutUser() error: ", e);
        }
        sendLeaveRoomMessage(playerLeave);
        //khi user disable mạng, tắt wifi thì sẽ bị delay 5s
        this.game.getApi().kickUser(playerLeave, null, " ", 0);
    }

    /**
     * Gửi message leave room
     * @param user 
     */
    protected void sendLeaveRoomMessage(User user) {
        try {
            SFSObject ob = messageController.getLeaveRoomMessage(getIdDBOfUser(user));
            sendAllUserMessage(ob);
        } catch (Exception e) {
            this.game.getLogger().error("sendMessageLeaveRoom error: ", e);
        }
    }
    
    protected void sendShuffleMessage() {
        sendAllUserMessage(messageController.getShuffleMessage());
    }

    /**
     * Trả tiền còn lại của user khi user rời bàn
     * @param user 
     */
    protected void repayMoneyStask(User user){
       
        String userId = Utils.getIdDBOfUser(user);
       
        try {
            UserState userState = HazelcastUtil.getUserState(userId);
             this.game.getLogger().info(getMoneyFromUser(user)+ "repayMoneyStask:"+userId);
            if (userState != null) {
                BigDecimal moneyOfUser = getMoneyFromUser(user);
                if (!userState.isIsLogoutGame() && moneyOfUser.signum() > 0) {
                    UpdateMoneyResult result;
                    if (getMoneyType() == MoneyContants.MONEY) {
                        result = Database.instance.callCashoutMoneyStackProcedure(Utils.getIdDBOfUser(user));
                    } else {
                        result = Database.instance.callCashoutPointProcedure(Utils.getIdDBOfUser(user));
                    }
                    updateMoneyStackOfUser(user, BigDecimal.ZERO);

//                    if (result.after.compareTo(result.before) != 0) {
//                        UserBalanceUpdate ubu = new UserBalanceUpdate();
//                        ubu.setPlayerId(userId);
//                        ubu.setServiceId(getServiceId());
//                        ubu.setEmail(userState.getEmail());
//                        ubu.setSessionId(userState.getSessionId());
//                        ubu.setRequestId(Utils.md5String(userId + money.doubleValue() + System.currentTimeMillis()));
//                        ubu.setLogId(ubu.getRequestId());
//                        ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
//                        ubu.setCreatedAt(System.currentTimeMillis() / 1000);
//                        ubu.setBalance(result.after);
//                        ubu.setChange(result.after.subtract(result.before));
//                        ubu.setLastBalance(result.after);
//                        ubu.setDescription("Refund stack");
//                        ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_PLAY_GAME);
//                        ubu.setChannel(Database.instance.getUserChannel(userId));
//                        if (moneyType == MoneyContants.MONEY) {
//                            ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
//                            ubu.setUnit("real");
//                        } else {
//                            ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_POINT, Locale.ENGLISH));
//                            ubu.setUnit("point");
//                        }
//                        QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
//                    }

                    this.game.getLogger().info(userId+".repayMoneyStask: money ="+moneyOfUser +", sumMoneyBefore="+result.before.doubleValue()+", sumMoneyAfter="+result.after.doubleValue());
                }
                userState.setIsLogoutGame(true);
                userState.setMoneyStack(0);
                userState.setPointStack(0);
                userState.setBetBoard(0);
                userState.setMoneyType(getMoneyType());
            }
            HazelcastUtil.updateUserState(userState);
        } catch (Exception e) {
           this.game.getLogger().error("Utils.repayMoneyTask", e);
        }
    }

    private boolean processLeaveGame(User playerLeave) {
        if (playerLeave == null) {
            return false;
        }
        try {
            /**
             * Owner leave bàn thì set lai owner chổ này
             */
            if (Utils.isEqual(playerLeave, this.getOwner())) {
                User nextUser = findOwner(playerLeave);
                setOwner(nextUser);
            }
            
            //xet lại user không ở trong bàn
            setInturn(playerLeave, false);
            //reset lại chức năng chơi nhanh của user
            setQuickPlayOfUser(playerLeave, false);
            //remove user khỏi danh sách player
            removePlayer(playerLeave);

            //xét lại chổ ngồi user
            setSeatWhenUserLeave(getIdDBOfUser(playerLeave));
            updateSeatUser(playerLeave, SEAT_UNKNOW);
            isEnoughPlayersToStart.set(checkPlayersToStartAndCountDown());
            if (!isEnoughPlayersToStart.get()) {
                if (!isPlaying()) {
                    /**
                     * nếu chua start thì xét lại trang thái, để khi user khac
                     * join vao thì chạy lại trạng thái countDown
                     */
                    setStateGame(this.getStoppingGameState());
                }
                //nếu còn 1 người trong ván thì reset trạng thái quick play
                resetAllPlayerQuickPlayInGame();
            }
            updateBoardPlayingToHazelcast(playerLeave,false);
            return true;
        } catch (Exception e) {
            this.game.getLogger().error("GameController.processLeaveGame() error: ", e);
        }
        return false;
    }

    /**
     * Tất cả những phát sinh tiền trong bàn chơi sẽ gọi qua hàm này
     *
     * @param u
     * @param value
     * @param reasonId
     * @param tax
     * @param arrayCardIds
     * @return
     */
    protected synchronized boolean updateMoney(User u, BigDecimal value, int reasonId, BigDecimal tax, List<Short> arrayCardIds) {
        if (value.signum() == 0 || u == null) {
            return false;
        }
        try {
            if (!isPlayerState(u)) {
                return false;
            }
            String idDBUser = getIdDBOfUser(u);
            BigDecimal moneyOfUserBefore = getMoneyFromUser(u);

            if (value.signum() < 0) {
                //nếu trừ tiền, thì -value phải nhỏ hơn hoặc bằng tiền hiện có của user
                if (moneyOfUserBefore.compareTo(value.negate()) <0 ) {
                    return false;
                }
            }

            //kiểm tra xem số tiền đã deposit có lớn hơn số tiền muốn lấy ra chưa
            if (minusMoney.compareTo(value) < 0) {
                this.game.getLogger().error("GameController.updateMoney error: user: " + u.getName()
                        + " minusMoney: " + minusMoney + " value: " + value);
            }

            //tiền deposit lớn hơn value -> gán value = tiền deposit
            value = minusMoney.min(value);
            if (value.signum() == 0) {
                return false;
            }
            
            //trường hợp ko lỗi
            if (minusMoney.compareTo(value) >= 0) {
                UpdateMoneyResult rs;
                if (getMoneyType() == MoneyContants.MONEY) {
                    rs = Database.instance.callUpdateMoneyStackProcedure(idDBUser, value);
                } else {
                    rs = Database.instance.callUpdatePointStackProcedure(idDBUser, value);
                }

                if (moneyOfUserBefore.compareTo(rs.before) != 0) {
                    this.game.getLogger().info(idDBUser+".Update money moneyOfUserBefore difference rs.before: value= "+value.doubleValue()+"moneyOfUserBefore="+moneyOfUserBefore+", before="+rs.before+", after="+rs.after);
                    sendUpdateMoneyLog(u, value, reasonId, tax, rs.before, rs.after, arrayCardIds);
                    //trường hợp lỗi cache và db khác nhau -> đồng bộ lại data và 
                    if (value.signum() < 0) {
                        /**
                         * Mục dích: đảm bảo trừ đúng số tiền value, nếu user
                         * không đủ tiền thì còn bao nhiều trừ bấy nhiêu
                         */
                        BigDecimal checkMoney =rs.before.min(value.negate()) ;
                        //lấy đúng số win có thể trừ để cộng vào minusMoney
                        minusMoney = Utils.add(minusMoney, checkMoney);
                        updateMoneyStackOfUser(u, rs.after);
                        return true;
                    }
                    minusMoney = Utils.add(minusMoney, value.negate());
                    updateMoneyStackOfUser(u, rs.after);
                    return true;
                }
                minusMoney = Utils.add(minusMoney, value.negate());
                updateMoneyStackOfUser(u, rs.after);
            }

            BigDecimal moneyOfUser = getMoneyFromUser(u);
            this.game.getLogger().info(idDBUser+".Update money: value= "+value.doubleValue()+" before="+moneyOfUserBefore+", after="+moneyOfUser);
            sendUpdateMoneyLog(u, value, reasonId, tax, moneyOfUserBefore, moneyOfUser, arrayCardIds);
            
            //ghi log thue cho VIP
            if (ServerConfig.getInstance().enableVip() && tax.signum()> 0 && getMoneyType() == MoneyContants.MONEY) {
                UserTaxData userTaxData = new UserTaxData();
                userTaxData.setGameId(reasonId + "");
                userTaxData.setTax(tax.doubleValue());
                userTaxData.setTime(new Date());
                userTaxData.setUserId(getIdDBOfUser(u));
                userTaxData.setServerId(ServerConfig.getInstance().getServerId() + "");

                //gửi lên queue để vip chạy
                executorLog.execute(new SendVipDataTask(userTaxData));
                this.game.getLogger().info("send Vip data : " + userTaxData.getUserId() + " - money : " + value + " - tax : " + userTaxData.getTax());
            }
            
            if (value.signum() > 0) {
                sumReceive = Utils.add(sumReceive, value);
            } else {
                sumBet = Utils.add(sumBet, value.abs());
            }

            for (PlayersDetail playerInfor : invoices.getPlayersDetail()) {
                if (playerInfor.getPlayerId().equals(getIdDBOfUser(u))) {
                    playerInfor.setCreditAfter(getMoneyFromUser(u).doubleValue());
                    break;
                }
            }

//            sendLogBalanceUpdate(u, idDBUser, value, reasonId, moneyOfUserBefore, moneyOfUser);

            return true;
        } catch (Exception e) {
            this.game.getLogger().error("GameController.updateMoney() error:", e);
        }
        return false;
    }
    
    private void sendUpdateMoneyLog(User u, BigDecimal value, int reasonId, BigDecimal tax, BigDecimal before, BigDecimal after, List<Short> arrayCardIds) {
        //add thông tin chi tiết bàn tới log history
        addBoardDetail(u, reasonId, before.doubleValue(), after.doubleValue(), value.doubleValue(), tax.doubleValue(), arrayCardIds);
        //ghi log khi có phát sinh tiền trong game
        addInvoiceDetail(u, reasonId, before.doubleValue(), after.doubleValue(), value.doubleValue(), tax.doubleValue(), arrayCardIds);
        /**
         * Gửi về cho client thông tin phát sinh tiền để hiển thị lịch sử trong
         * chat
         */
        if (reasonId != CommonMoneyReasonUtils.DAT_CUOC) {
            sendHistoryBoard(u, value.doubleValue(), reasonId, arrayCardIds);
        }
    }
     
    protected void sendLogBalanceUpdate(User user, String userId, BigDecimal value, int reasonId, BigDecimal before, BigDecimal after) throws NoSuchAlgorithmException {
        String requestId = Utils.md5String(userId + value.doubleValue() + System.currentTimeMillis());
        executorLog.execute(() -> {
            UserState userState = HazelcastUtil.getUserState(userId);
            
            UserBalanceUpdate ubu = new UserBalanceUpdate();
            BigDecimal userMoney = getMaxStack(user);
            ubu.setPlayerId(userId);
            ubu.setEmail(userState.getEmail());
            ubu.setSessionId(userState.getSessionId());
            ubu.setServiceId(getServiceId());
            ubu.setRequestId(requestId);
            ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
            ubu.setLogId(invoices.getInvoiceId());
            ubu.setCreatedAt(System.currentTimeMillis() / 1000);
            ubu.setBalance(userMoney.add(after));
            ubu.setChange(value);
            ubu.setLastBalance(userMoney.add(before));
            ubu.setDescription(CommonMoneyReasonUtils.getReasonDescription(reasonId));
            ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_PLAY_GAME);
            ubu.setChannel(Database.instance.getUserChannel(userId));
            if (moneyType == MoneyContants.MONEY) {
                ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
                ubu.setUnit("real");
            } else {
                ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_POINT, Locale.ENGLISH));
                ubu.setUnit("point");
            }
            QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
        });
    }

    /**
     * Ham nay chi dung cho tournament de dam bao khong win qua so tien quy dinh
     * @param minus 
     */
    protected void setMinusMoney(BigDecimal minus){
        minusMoney = minus;
    }

    /**
     * Ghi log cho user thua
     *
     * @param u
     * @param reason
     * @param arrayCardIds
     */
    protected void updateLogGameForUser(User u, int reason, List<Short> arrayCardIds) {
        try {
            double moneyOfUser =  getMoneyFromUser(u).doubleValue();
            //add thông tin chi tiết ván chơi
            addBoardDetail(u, reason, moneyOfUser, moneyOfUser, 0, 0, arrayCardIds);
            addInvoiceDetail(u, reason, moneyOfUser, moneyOfUser, 0, 0, arrayCardIds);
            //gửi thông tin về cho client
            sendHistoryBoard(u, 0, reason, arrayCardIds);
        } catch (Exception e) {
            this.game.getLogger().error("GameController.updateLogGameForUser() error:", e);
        }
    }

    /**
     * Gửi về thông tin lịch sử ván chơi cho client
     *
     * @param u
     * @param value
     * @param reasonId
     * @param arrayCardIds
     */
    protected void sendHistoryBoard(User u, double value, int reasonId, List<Short> arrayCardIds) {
        try {
            UserMoneyLog userMoneyLog = new UserMoneyLog();
            userMoneyLog.setBoardId(this.room.getId());
            userMoneyLog.setUserId(getIdDBOfUser(u));
            userMoneyLog.setUserName(getUserName(u));
            userMoneyLog.setServerId(ServerConfig.getInstance().getServerId());
            userMoneyLog.setServiceId(getServiceId());
            userMoneyLog.setValue(value);
            userMoneyLog.setMoney(getMoneyFromUser(u).doubleValue());
            userMoneyLog.setMoneyType(getMoneyType());
            userMoneyLog.setReasonId(reasonId);
            userMoneyLog.setOptionalArrayData(arrayCardIds==null? new ArrayList<>():arrayCardIds);
            userMoneyLog.setBoardLogDate(Utils.getCurrentDateString("yyyyMMddhhmm"));
            SFSObject ob = messageController.getHistoryMessage(userMoneyLog);
            sendAllPlayersMessage(ob);
        } catch (Exception e) {
            this.game.getLogger().error("GameController.sendHistoryBoard() error: ", e);
        }
    }

    protected BigDecimal getMoney() {
        return money;
    }

    /**
     * Tính tiền thuế thu được khi user update money
     *
     * @param money
     * @param taxPercent
     * @return
     */
    private BigDecimal getTax(BigDecimal money, int taxPercent) {
        double tax = (money.doubleValue() * taxPercent) / 100;
        //mức tiền thắng tối thiểu để tính thuế được config
        double taxThreshold = RoomConfig.getInstance().getTaxThreshold();
        if (money.doubleValue() < taxThreshold) {
            tax = 0;
        }
        BigDecimal value= new BigDecimal(String.valueOf(tax));
        value = Utils.getRoundBigDecimal(value);
        return value;
    }
    
    private double getTax(double money, int taxPercent) {
        double tax = (money * taxPercent) / 100;
        //mức tiền thắng tối thiểu để tính thuế được config
        double taxThreshold = RoomConfig.getInstance().getTaxThreshold();
        if (money < taxThreshold) {
            tax = 0;
        }
        return tax;
    }

    public void processMessage(User player, ISFSObject sfsObj) {
        try {
            int idAction = sfsObj.getInt(SFSKey.ACTION_INGAME);
            if (getServiceId() != Service.PHOM  && getServiceId()!=Service.XI_TO) {
                resetNoActionTime(player);
            }
            switch (idAction){
                case SFSAction.QUICK_PLAY:
                    //xử lý chức năng chơi nhanh cho tất cả các game
                    if (isPlaying() || isQuickPlayOfUser(player)) {
                        break;
                    }
                    setQuickPlayOfUser(player, true);
                    break;
                case SFSAction.BUY_STACK_IN_GAME:
                    double buyStackMoney = sfsObj.getDouble(SFSKey.MONEY_STACK);
                    if(isCanBuyIn(player, buyStackMoney)){
                        processBuyStack(player, buyStackMoney);
                    }
                    break;
                case SFSAction.CONTINUE_GAME:
                    resetNoActionTime(player);
                    break;
                case SFSAction.GET_REMAIN_TIME:
                    sendUserMessage(messageController.getRemainTimeInBoard(getTimeRemain()),player);
                    break;    
                case SFSAction.AUTO_BUY_IN:
                    boolean isAutoBuyIn = sfsObj.getBool("isAutoBuyIn");
                    setAutoBuyIn(player, isAutoBuyIn);
                    break;
                case SFSAction.LEAVE_GAME:
                    synchronized (LOCK) {
                    if (!this.isCanLeave(player)) {
                        String message = GameLanguage.getMessage(GameLanguage.CANT_LEAVE_GAME_NOW, Utils.getUserLocale(player));
                        sendToastMessage(message, player, 3);
                        return;
                    }
                    leave(player);
                    }
                    break;
                case SFSAction.AUTO_LEAVE_GAME:
                    boolean isAutoLeave = sfsObj.getBool("isAutoLeave");
                    setAutoLeave(player, isAutoLeave);
                    break;
            }
        } catch (Exception e) {
            this.game.getLogger().error("processMessage() error: ", e);
        }
    }
    
    /**
     * check xem user có đủ điều kiện buy-in
     * @param player
     * @param buyStackMoney
     * @return 
     */
    protected boolean isCanBuyIn(User player, double buyStackMoney) {
        double maxStack = this.getMaxStack(player).doubleValue();
        if(maxStack <= 0){
            sendToastMessage(GameLanguage.getMessage(GameLanguage.NOT_ENOUGH_SUM_MONEY, Utils.getUserLocale(player)), player, 3);
            return false;
        }
        if (buyStackMoney <= 0) {
            checkSendBuyStackMessage(player, maxStack);
            return false;
        }
        
        return true;
    }

    private void checkSendBuyStackMessage(User player, double maxStack){
        double minstack = this.getMinStackReBuyIn(player).doubleValue();
        if(maxStack < minstack){
            minstack = maxStack;
        }
        sendUserMessage(messageController.getBuyStackMessage(minstack, maxStack), player);
    }

    /**
     * send message to user
     *
     * @param params
     * @param user
     */
    protected void sendUserMessage(ISFSObject params, User user) {
        try {
            this.game.send(SFSCommand.CLIENT_REQUEST_INGAME, params, user);
        } catch (Exception e) {
            this.game.getLogger().error("sendUserMessage() error: ", e);
        }
        
    }

    /**
     * Send message to all user in room
     *
     * @param params
     */
    protected void sendAllUserMessage(ISFSObject params) {
        try {
            this.game.send(SFSCommand.CLIENT_REQUEST_INGAME, params, this.room.getUserList());
        } catch (Exception e) {
            this.game.getLogger().error("sendAllUserMessage() error: ", e);
        }
    }
    
     /**
     * Send message to all user in room
     *
     * @param user
     * @param params
     */
    protected void sendAllUserOutOfMeMessage(User user,ISFSObject params) {
        try {
            for(User u: this.room.getUserList()){
                if(Utils.isEqual(u, user)){
                    continue;
                }
                sendUserMessage(params, u);
            }
        } catch (Exception e) {
            this.game.getLogger().error("sendAllUserOutOfMeMessage() error: ", e);
        }
    }

    /**
     * send message to all viewer in room
     *
     * @param params
     */
    protected void sendAllViewer(ISFSObject params) {
        try {
            this.game.send(SFSCommand.CLIENT_REQUEST_INGAME, params, this.room.getSpectatorsList());
        } catch (Exception e) {
            this.game.getLogger().error("sendAllViewer() error: ", e);
        }
    }

    /**
     * Gửi message tới tất cả player chơi trong ván
     *
     * @param params
     */
    protected void sendAllPlayersMessage(ISFSObject params) {
        try {
            this.game.send(SFSCommand.CLIENT_REQUEST_INGAME, params, this.getAllPlayers());
        } catch (Exception e) {
            this.game.getLogger().error("sendAllPlayersMessage() error: ", e);
        }
    }

    /**
     * gửi hết mọi player trong board.
     *
     * @param u : user nhận message
     * @param value : số tiền được cập nhật
     * @param messEn : mess tiếng Anh
     * @param messVi : mess tiếng Việt
     */
    protected void sendToAllWithLocale(User u, double value, String messEn, String messVi) {
        String idDB = getIdDBOfUser(u);
        SFSObject engMessage = this.getBonusMoney(idDB, value, messEn);
        SFSObject viMessage = this.getBonusMoney(idDB, value, messVi);
        for (User userGame : getPlayersList()) {
            if (getLocaleOfUser(userGame).equals(GlobalsUtil.ENGLISH_LOCALE)) {
                sendUserMessage(engMessage, userGame);
            } else if (getLocaleOfUser(userGame).equals(GlobalsUtil.VIETNAMESE_LOCALE)) {
                sendUserMessage(viMessage, userGame);
            } 
        }
    }

    /**
     * Update tiền và gui message về cho user chổ này
     *
     * @param user
     * @param value
     * @param messVi
     * @param messEn
     * @param messZh
     * @param reasonId
     * @param tax
     * @param arrayCardIds
     * @return
     */
    protected boolean updateMoney2WithLocale(User user, BigDecimal value, String messVi, String messEn, String messZh, int reasonId, BigDecimal tax, List<Short> arrayCardIds) {
        try {
            if (value.signum() == 0) {
                return true;
            }
            if (updateMoney(user, value, reasonId, tax, arrayCardIds)) {
                String idDB = getIdDBOfUser(user);
                //bo cuoc phai gui lai message update money vi sendToAllWithLocale ko co player da leave
                 if (getLocaleOfUser(user).equals(GlobalsUtil.VIETNAMESE_LOCALE)) {
                    SFSObject viMessage = this.getBonusMoney(idDB, value.doubleValue(), messVi);
                    sendUserMessage(viMessage, user);
                } else if (getLocaleOfUser(user).equals(GlobalsUtil.ENGLISH_LOCALE)) {
                    SFSObject engMessage = this.getBonusMoney(idDB, value.doubleValue(), messEn);
                    sendUserMessage(engMessage, user);
                } else {
                    SFSObject zhMessage = this.getBonusMoney(idDB, value.doubleValue(), messZh);
                    sendUserMessage(zhMessage, user);
                }
                return true;
            }
        } catch (Exception e) {
            this.game.getLogger().error("updateMoney2WithLocale error ", e);
        }
        return false;
    }

    /**
     * Get locale của user
     *
     * @param user
     * @return
     */
    protected Locale getLocaleOfUser(User user) {
        if (user == null) {
            return GlobalsUtil.DEFAULT_LOCALE;
        }
        return (Locale) user.getProperty(UserInforPropertiesKey.LOCALE_USER);
    }

    /**
     * get money đang có của user vì gọi lien tục khi start ván, kết thúc vàn va
     * check tiền nen syn lại chổ này
     *
     * @param user
     * @return
     */
    public BigDecimal getMoneyFromUser(User user) {
        if (user == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal moneyOfUser = BigDecimal.ZERO;
        try {
            moneyOfUser = new BigDecimal(String.valueOf(user.getProperty(UserInforPropertiesKey.MONEY_STACK)));
            moneyOfUser = Utils.getRoundBigDecimal(moneyOfUser);
        } catch (Exception e) {
            this.game.getLogger().error("GameController.getMoneyFromUser() erro:", e);
        }

        return moneyOfUser;
    }

    /**
     * update tiền trực tiếp vào user
     *
     * @param u
     * @param moneyOfUser
     */
    protected void updateMoneyStackOfUser(User u, BigDecimal moneyOfUser) {
        if (u == null) {
            return;
        }
        try {
            UserVariable ver = new SFSUserVariable(UserInforPropertiesKey.MONEY_STACK, moneyOfUser.doubleValue());
            this.game.getApi().setUserVariables(u, Arrays.asList(ver));
            u.setProperty(UserInforPropertiesKey.MONEY_STACK, moneyOfUser);
            sendUpdateVariableUserMessage(u, UserInforPropertiesKey.MONEY_STACK);
        } catch (Exception e) {
            this.game.getLogger().error("updateMoneyOfUser() error:", e);
        }
    }
    
    /**
     * Update tiền tổng của user đến hazelcast
     * @param user
     * @param sumStack 
     */
    private void updateMoneyStackToHazelcast(User user, double sumStack){
        String idDBUser = getIdDBOfUser(user);
        try {
            UserState userState = HazelcastUtil.getUserState(idDBUser);
            if (userState != null) {
                if (getMoneyType() == MoneyContants.MONEY) {
                    userState.setMoneyStack(sumStack);
                } else {
                    userState.setPointStack(sumStack);
                }
                HazelcastUtil.updateUserState(userState);
            }
        } catch (Exception e) {
              this.game.getLogger().error("updateSumMoneyToHazelcast() error:", e);
        }
    }

    /**
     * - Add những user không đủ tiền đến danh sách user chờ - Không sử dụng hàm
     * kick user khỏi phòng khi hết tiền nữa - Hệ thống sẽ giữ chổ user 2 ván,
     * nếu không mua tẩy thì sẽ bị kick
     *
     * @param userRemoved
     * @param reason
     */
    protected void addToWaitingUserList(User userRemoved, String reason) {
        try {
            //lấy chổ ngồi để add lại cho user
            int seat = getSeatNumber(userRemoved);
            if (seat == -1) {
                return;
            }

            /**
             * Owner leave bàn thì set lai owner chổ này
             */
            if (Utils.isEqual(userRemoved, this.getOwner())) {
                User nextUser = findOwner(userRemoved);
                setOwner(nextUser);
            }

            //xet lại user không ở trong bàn
            setInturn(userRemoved, false);
            //reset lại chức năng chơi nhanh của user
            setQuickPlayOfUser(userRemoved, false);
            
            updateBoardPlayingToHazelcast(userRemoved,false);
            getManagerUsersState(userRemoved).setWaiterState();
            updateStatusWaiterUser(userRemoved, true);
            isEnoughPlayersToStart.set(checkPlayersToStartAndCountDown());
            if (!isEnoughPlayersToStart.get()) {
                if (!isPlaying()) {
                    /**
                     * nếu chua start thì xét lại trang thái, để khi user khac
                     * join vao thì chạy lại trạng thái countDown
                     */
                    setStateGame(this.getStoppingGameState());
                }
                //nếu còn 1 người trong ván thì reset trạng thái quick play
                resetAllPlayerQuickPlayInGame();
            }
            
            if (!reason.isEmpty()) {
                sendToastMessage(reason, userRemoved, 3);
            }
        } catch (Exception e) {
            this.game.getLogger().error("addToWaitingUserList() error:", e);
            kickUser(userRemoved, "");
        }
    }

    /**
     * Gửi message thông báo cho user
     *
     * @param errString
     * @param user
     * @param i
     */
    public void sendToastMessage(String errString, User user, int i) {
        try {
            if(user==null){
                return;
            }
            SFSObject obMsg = MessageController.getToastMessage(errString, i);
            this.game.send(SFSCommand.CLIENT_REQUEST, obMsg, user);
        } catch (Exception e) {
            this.game.getLogger().error("sendToastMessage error:", e);
        }
    }

    public boolean isPlaying() {
        return this.gameState.equals(waittingState) || this.gameState.equals(playingGameState);
    }

    private boolean isCountDownState() {
        return gameState.equals(countDownState);
    }

    /**
     * Lấy ra vị tri chổ ngồi của user trong ván
     *
     * @param idDBUser: id get từ DB của user
     * @return
     */
    protected int getSeatNumber(String idDBUser) {
        for (int i = 0; i < playerSeats.length; i++) {
            if (playerSeats[i] == null) {
                continue;
            }
            if (playerSeats[i].equals(idDBUser)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Xét lại chổ ngồi của leaver ==null
     *
     * @param userName
     */
    private void setSeatWhenUserLeave(String idDBUser) {
        for (int i = 0; i < playerSeats.length; i++) {
            if (playerSeats[i] == null) {
                continue;
            }
            if (playerSeats[i].equals(idDBUser)) {
                playerSeats[i] = null;
            }
        }
    }

    /**
     * Xet chổ ngồi cho user khi user join bàn
     *
     * @param userName
     */
    private int setSeatWhenUserJoin(String idDBUser){
        for(int i=0;i<playerSeats.length;i++){
            if(playerSeats[i]==null){
                playerSeats[i]=idDBUser;
                return i;
            }
        }
        return -1;
    }

    /**
     * Kiểm tra điều kiện user join bàn Tùy game mà có điều kiện khác nhau
     *
     * @param user
     * @return
     */
    private boolean isUserCanJoin(User user) {
        if (user == null) {
            return false;
        }
        BigDecimal minMoney = Utils.multiply(getMoney(),  new BigDecimal(String.valueOf(getMinJoinGame())));
        if (getMoneyFromUser(user).compareTo(minMoney) < 0) {
            sendCanNotJoinMessage(user, getMinJoinGame());
            return false;
        }
 
        //check tiền của nhà cái cho những game đặt cược
        if (getOwner() == null) {
            BigDecimal minMoneyOwner = Utils.multiply(getMoney(), new BigDecimal(String.valueOf(getMinJoinOwner())));
            if (getMoneyFromUser(user).compareTo(minMoneyOwner) < 0) {
                sendCanNotJoinMessage(user, getMinJoinOwner());
                return false;
            }
        }
        return true;
    }

    /**
     * gửi cmd thông báo user khong thể join game
     *
     * @param user
     * @param minJoin
     */
    protected void sendCanNotJoinMessage(User user, int minJoin) {
        String info = String.format(GameLanguage.getMessage(GameLanguage.NO_MONEY_USER, getLocaleOfUser(user)), minJoin);
        sendToastMessage(info, user, 3);
    }

    /**
     * Gửi thông báo chung khi join room không thành cong
     *
     * @param user
     */
    private void sendJoinRoomError(User user) {
        String info = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, getLocaleOfUser(user));
        sendToastMessage(info, user, 3);
    }

    /**
     * Lấy ra số lần tiền toi thieu de join game
     *
     * @return
     */
    protected int getMinJoinGame() {
        return RoomConfig.getInstance().getMinJoinGame(getNameLobby());
    }

    /**
     * Lấy ra số lần tiền toi thieu owner de join game
     *
     * @return
     */
    protected int getMinJoinOwner() {
        return RoomConfig.getInstance().getMinJoinOwner(getNameLobby());
    }

    /**
     * loại tiền tệ trong game
     *
     * @param lo
     * @return
     */
    protected String getCurrency(Locale lo) {
        if (getMoneyType() == MoneyContants.MONEY) {
            return GameLanguage.getMessage(GameLanguage.TYPE_MONEY, lo);
        }
        return GameLanguage.getMessage(GameLanguage.TYPE_POINT, lo);
    }
    
    /**
     * Tên tiền tệ trong game
     *
     * @param lo
     * @return
     */
    protected String getCurrencyName(Locale lo) {
        if (getMoneyType() == MoneyContants.MONEY) {
            return GameLanguage.getMessage(GameLanguage.NAME_MONEY, lo);
        }
        return GameLanguage.getMessage(GameLanguage.NAME_POINT, lo);
    }

    /**
     * lấy số tiền nhận được sau thuế
     *
     * @param money : tiền trước thuế
     * @param taxPercent : thuế
     * @return tiền sau thuế
     */
    protected BigDecimal[] setMoneyMinusTax(BigDecimal money, int taxPercent) {
        //mang co 2 data
        //data index 1: tax thu ve, data index 0 : so tien sau khi bi tru thue cua user
        BigDecimal[] result = new BigDecimal[2];
        result[TAX] = BigDecimal.ZERO;
        result[MONEY] = BigDecimal.ZERO;
        if(ServerConfig.getInstance().isSatoshiGame()){
            result = calculatorMinusTaxRound(money.doubleValue(), taxPercent);
        }else{
            result = calculatorMinusTax(money, taxPercent);
        }
        sumTax = Utils.add(sumTax, result[TAX]);
        return result;
    }
    
    /**
     * Tính thuế theo kiểu làm tròn xuống ( kiểu Long)
     * @param moneyInput
     * @param taxPercent
     * @return 
     */
    private BigDecimal[] calculatorMinusTaxRound(double moneyInput, int taxPercent) {
        BigDecimal[] result = new BigDecimal[2];
        result[TAX] = BigDecimal.ZERO;
        result[MONEY] = BigDecimal.ZERO;
        
        long money = (long) moneyInput;
        long tax = (money * taxPercent) / 100;
        money = money - tax;

        result[MONEY] = new BigDecimal(String.valueOf(money));
        result[TAX] =  new BigDecimal(String.valueOf(tax));
        return result;
    }
    
    private double[] calculatorMinusTax(double money, int taxPercent) {
        //mang co 2 data
        //data index 1: tax thu ve, data index 0 : so tien sau khi bi tru thue cua user
        double[] result = new double[2];
        //lay lam tron len 2 chu so X 100
        long moneyBefore = Math.round(money * 100);

        //tinh thue
        double tax = getTax(money, taxPercent);
        long taxAfterRound = Math.round(tax * 100);

        //sau khi tinh thue lay ra so làm tròn x100
        long moneyAfter = Math.round((money - tax) * 100);

        if (moneyBefore - (moneyAfter + taxAfterRound) < 0) {
            //hieu nay >= 0 => thue lam tron len, user lam tron xuong >> he thong lời >> bo qua
            //hieu nay < 0 => thue lam tron len, user lam tron len>> he thong lo >> trừ phần này cho user
            moneyAfter += (moneyBefore - (moneyAfter + taxAfterRound));
        }

        //gan ket qua tra  ve
        result[MONEY] = (double) moneyAfter / 100;
        result[TAX] = (double) taxAfterRound / 100;
        return result;
    }
    
      private BigDecimal[] calculatorMinusTax(BigDecimal money, int taxPercent) {
        //mang co 2 data
        //data index 1: tax thu ve, data index 0 : so tien sau khi bi tru thue cua user
        BigDecimal[] result = new BigDecimal[2];
        result[TAX] = BigDecimal.ZERO;
        result[MONEY] = BigDecimal.ZERO;
        //lay lam tron len 2 chu so X 100
        BigDecimal moneyBefore = Utils.getRoundBigDecimal(money);

        //tinh thue
        BigDecimal tax = getTax(money, taxPercent);

        //sau khi tinh thue lay ra so làm tròn x100
        BigDecimal moneyAfter = Utils.subtract(moneyBefore, tax);
        moneyAfter = Utils.getRoundBigDecimal(moneyAfter);

        //gan ket qua tra  ve
        result[MONEY] = moneyAfter;
        result[TAX] = tax;
        return result;
    }

    public Room getRoom() {
        return room;
    }

    /**
     * Mức cược tối đa user có thể đặt Hiển thị trong thanh kéo
     *
     * @param user
     * @return
     */
    protected BigDecimal getMaxBoardValue(User user) {
        return getMoney();
    }

    /**
     * Mức cược tối thiểu user có thể đặt Hiển thị trong thanh kéo
     *
     * @param user
     * @return
     */
    protected BigDecimal getMinBoardValue(User user) {
        return getMoney();
    }

    /**
     * xét lượt hiện tại trong board cho user
     *
     * @param currentPlayerInput
     */
    public void setCurrentPlayer(User currentPlayerInput) {
        this.currentPlayer = currentPlayerInput;
    }

    protected void setInturn(User u, boolean isInturn) {
        u.setProperty(UserInforPropertiesKey.IN_TURN, isInturn);
    }
    
    /**
     * Kiểm tra user đang ngồi trong bàn có phải đang chơi game
     *
     * @param u
     * @return
     */
    public boolean isInturn(User u) {
        if (u == null) {
            return false;
        }
        return (boolean) u.getProperty(UserInforPropertiesKey.IN_TURN);
    }
    
    public boolean isBot(User u) {
        if (u == null) {
            return false;
        }
        return Utils.getUserType(u) > UserType.NORMAL;
    }

    protected boolean isInturn(int seat) {
        if (seat == -1 || seat > this.playerSeats.length || this.playerSeats[seat] == null) {
            return false;
        }
        User finder = null;
        for (User user : getAllPlayers()) {
            if (isPlayerState(user) && this.playerSeats[seat].equals(getIdDBOfUser(user))) {
                finder = user;
                break;
            }
        }
        return isInturn(finder);
    }

    /**
     * chuyển lượt user
     */
    protected void nextTurn() {
        setCurrentPlayer(nextPlayer(getCurrentPlayer()));
        setCurrentMoveTime();
    }

    /**
     *
     * @param player
     * @return
     */
    protected User nextPlayer(User player) {
        int sNum = getSeatNumber(getIdDBOfUser(player));
        int length = this.playerSeats.length;
        for (int i = 0; i < length; i++) {
            sNum = (sNum + 1) % length;
            User u = getUser(sNum);
            if (u != null && isInturn(u)) {
                return u;
            }
        }
        return null;
    }

    /**
     * get user trong ván dựa vào chổ ngồi
     *
     * @param seat
     * @return
     */
    protected User getUser(int seat) {
        if (seat <= -1 || seat >= this.playerSeats.length) {
            return null;
        }
        if (playerSeats[seat] == null) {
            return null;
        }
        for (User user : this.getAllPlayers()) {
            if (isPlayerState(user) && playerSeats[seat].equals(getIdDBOfUser(user))) {
                return user;
            }
        }
        return null;
    }

    /**
     * lấy ra user đang chơi theo chổ ngồi
     *
     * @param seat
     * @return
     */
    protected User getUserPlaying(int seat) {
        if (seat <= -1 || seat > playerSeats.length) {
            return null;
        }
        if (playerSeats[seat] == null) {
            return null;
        }
        for (User user : getAllPlayers()) {
            if (!isPlayerState(user) || !isInturn(user)) {
                continue;
            }
            if (playerSeats[seat].equals(getIdDBOfUser(user))) {
                return user;
            }
        }
        return null;
    }

    public User getCurrentPlayer() {
        return this.currentPlayer;
    }

    protected void setOwner(User user) {
        try {
            this.room.setOwner(user);
            String idUser = getIdDBOfUser(user);
            RoomVariable ver = new SFSRoomVariable(RoomInforPropertiesKey.ID_OWNER, idUser, true, false, false);
            this.game.getApi().setRoomVariables(user, this.room, Arrays.asList(ver));
        } catch (Exception e) {
            this.game.getLogger().error("set owner error:", e);
        }
    }

    protected SFSObject getBonusMoney(String userID, double value, String text) {
        try {
            return this.messageController.getBonusMoney(userID, value, text);
        } catch (Exception e) {
            this.game.getLogger().error("getBonusMoney() error:", e);
        }
        return null;
    }

    /**
     * gửi hết mọi player trong board.
     *
     * @param engMessage
     * @param viMessage
     * @param zhMessage
     */
    public synchronized void sendToAllWithLocale(SFSObject engMessage, SFSObject viMessage, SFSObject zhMessage) {
        try {
            for (User user : getAllPlayers()) {
                if (user != null) {
                    if (getLocaleOfUser(user).equals(GlobalsUtil.ENGLISH_LOCALE)) {
                        sendUserMessage(engMessage, user);
                    } else if (getLocaleOfUser(user).equals(GlobalsUtil.VIETNAMESE_LOCALE)) {
                        sendUserMessage(viMessage, user);
                    }else{
                        sendUserMessage(zhMessage, user);
                    }
                }
            }
        } catch (Exception e) {
            this.game.getLogger().error("sendToAllWithLocale() error:", e);
        }
    }

    /**
     * đếm số lượng người còn đang chơi trong phòng
     *
     * @return
     */
    protected int countInturnPlayer() {
        int count = 0;
        for (User u : getPlayersList()) {
            if (isInturn(u)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Thời gian còn lại trước khi bắt đầu ván
     *
     * @return
     */
    protected byte getTimeToStart() {
        if (!isPlaying()) {
            long time = ((System.currentTimeMillis() - getCurrentMoveTime())) / 1000;
            if (time == 0) {
                return (byte) (getTimeAutoStartDefault());
            }
            if (time < getTimeAutoStartDefault()) {
                return (byte) ((getTimeAutoStartDefault() - time));
            }
        }
        return 0;
    }

    /**
     * kiem tra user có chọn chơi nhanh chưa
     *
     * @param user
     * @return
     */
    public boolean isQuickPlayOfUser(User user) {
        if (user == null) {
            return false;
        }
        if (user.containsProperty(UserInforPropertiesKey.QUICK_PLAY)) {
            return user.getVariable(UserInforPropertiesKey.QUICK_PLAY).getBoolValue();
        }
        return false;
    }

    /**
     * Xét chức năng chơi nhanh cho user
     *
     * @param user
     * @param isQuickPlay
     */
    private void setQuickPlayOfUser(User user, boolean isQuickPlay) {
        try {
            UserVariable ver = new SFSUserVariable(UserInforPropertiesKey.QUICK_PLAY, isQuickPlay);
            this.game.getApi().setUserVariables(user, Arrays.asList(ver));
            sendUpdateVariableUserMessage(user, UserInforPropertiesKey.QUICK_PLAY);
        } catch (Exception e) {
            this.game.getLogger().error("setQuickPlayOfUser() error:", e);
        }
    }

    /**
     * kiểm tra tất cả người chơi đã sẵn sàng chưa? dùng để bắt đầu ván khi tất
     * cả đã sẵn sàng
     *
     * @return
     */
    private boolean isAllPlayerQuickPlayInGame() {
        List<User> players = getPlayersList();
        for (User user : players) {
            if (!isQuickPlayOfUser(user)) {
                return false;
            }
        }
        return players.size() > 1;
    }

    /**
     * Gui thong tin autostart van
     *
     * @param autoStartTime
     * @return
     */
    protected SFSObject getBoardInfoMessage(int autoStartTime) {
        try {
            return this.messageController.getBoardInfoMessage(autoStartTime);
        } catch (Exception e) {
            this.game.getLogger().error("getBoardInfoMessage() error:", e);
        }
        return null;
    }

    protected void resetAllPlayerQuickPlayInGame() {
        for (User user : getPlayersList()) {
            setQuickPlayOfUser(user, false);
        }
    }

    /**
     * Kiểm tra điều kiện để start ván
     *
     * @return
     */
    protected boolean isCanStart() {
        return (getTimeToStart() == 0 || isAllPlayerQuickPlayInGame())
                && !isPlaying() && isEnoughPlayersToStart.get();
    }

    /**
     * Danh sách player in turn trong core
     *
     * @return
     */
    protected List<User> getInTurnPlayers() {
        List<User> list = new ArrayList<>();
        for (User user : getAllPlayers()) {
            if (isInturn(user) && isPlayerState(user)) {
                list.add(user);
            }
        }
        return list;
    }

    /**
     * Xử lý user join vào game thành player
     *
     * @param user
     * @return
     */
    private boolean processJoin(User user) {
        if (user == null) {
            sendJoinRoomError(user);
            return false;
        }
        try {
            if (!isUserCanJoin(user)) {
                this.game.trace(" log isUserCanJoin error: >>>>>>>>>>>>> ");
                repayMoneyStask(user);
                return false;
            }

            int seatUser = setSeatWhenUserJoin(getIdDBOfUser(user));
            if (seatUser == -1) {
                sendJoinRoomError(user);
                this.game.trace(" log seat error: >>>>>>>>>>>>> ");
                repayMoneyStask(user);
                return false;
            }
            processJoinWhenHaveBot(user);
           
            updateSeatUser(user,seatUser);
            addPlayer(user);
            /**
             * Run thread trong gamecontroller khi có user join
             */
            if (!this.getPlayersList().isEmpty()) {
                initialize();
            }
            
            if(isAdvantageRatioByGroupID() && Utils.isBot(user)){
                String idDBUser = Utils.getIdDBOfUser(user);
                BotAdvantage ad = Database.instance.getBotAdvance(idDBUser);
                updateAdvantageRatioForBot(user, ad.getAdvRatio());
                updateAdFirstRatioForBot(user, ad.getFirstRatio());
                updateGroupIDForBot(user, Database.instance.getBotAdvanceGroupId(idDBUser));
            }
            updateStatusWaiterUser(user, false);
             //update hazecast danh sach ban user dang playing
            addBoardPlayingToHazelCast(user);
            sendJoinBoardMessage(user);
            sendAddPlayerMessage(user);
            return true;
        } catch (Exception e) {
            this.game.getLogger().error("GameConttroller.join() error: ", e);
        }
        sendJoinRoomError(user);
        return false;
    }
    
    public boolean isReconnect(User user) {
        return getSeatNumber(getIdDBOfUser(user)) != -1;

    }
    
    /**
     * Xử lý khi user từ ngoài lobby mua tẩy vào game
     * @param user
     * @param pwd
     * @return 
     */
    public boolean join(User user, String pwd) {
        UserState userState = HazelcastUtil.getUserState(getIdDBOfUser(user));
        if (userState == null) {
            return false;
        }
        //trừ tiền của user khi mua tẩy  ngoài lobby
        if(!updateMoneyBuyStackAtLobby(user)){
            return false;
        }
        return processJoin(user);
    }

    public boolean joinShuffle(User user) {
        UserState userState = HazelcastUtil.getUserState(getIdDBOfUser(user));
        if (userState == null) {
            return false;
        }
        return processJoin(user);
    }

    /**
     * Trừ tiền user mua tẩy của user ngoài lobby khi join game
     * @param user
     * @return 
     */
    private boolean updateMoneyBuyStackAtLobby(User user) {
        try {
            UserState userState = HazelcastUtil.getUserState(getIdDBOfUser(user));
            String inforText = getIdDBOfUser(user)+", moneycache="+userState.getMoneyStack()+", pointcache="+userState.getPointStack()
                        + " update tiền database khi mua tay, moneyTypeOfUser="
                        + userState.getMoneyType()+", Room money type="+getMoneyType();
             this.game.getLogger().info(inforText);
            //xét tiền cho user chổ này
            BigDecimal moneyStack = BigDecimal.ZERO;
            if (getMoneyType() == MoneyContants.MONEY) {
                moneyStack = new BigDecimal(String.valueOf(userState.getMoneyStack()));
            } else {
                moneyStack = new BigDecimal(String.valueOf(userState.getPointStack()));
            }
            if (moneyStack.signum() == 0) {
                kickUser(user, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user)));
                return false;
            }
            userState.setMoneyStack(0);
            userState.setPointStack(0);
            userState.setBetBoard(0);
            String userId = getIdDBOfUser(user);
            UpdateMoneyResult result;
            //chổ này check lại type money để update tiền thiệt va tiền ảo
            if (getMoneyType() == MoneyContants.MONEY) {
                result = Database.instance.callBuyStackMoneyProcedure(userId, moneyStack);
            } else {
                result = Database.instance.callBuyStackPointProcedure(userId, moneyStack);
            }
            
            userState.setIsUpdateMoneySum(true);
            HazelcastUtil.updateUserState(userState);
            //update tiền DB không thành công
            if (result.before.compareTo(result.after)== 0) {
               kickUser(user, GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user)));
               return false;
            }

            updateMoneyStackOfUser(user, result.stack);
            
//            UserBalanceUpdate ubu = new UserBalanceUpdate();
//            ubu.setPlayerId(userId);
//            ubu.setEmail(userState.getEmail());
//            ubu.setSessionId(userState.getSessionId());
//            ubu.setServiceId(getServiceId());
//            ubu.setRequestId(Utils.md5String(userId + moneyStack.doubleValue() + System.currentTimeMillis()));
//            ubu.setLogId(ubu.getRequestId());
//            ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
//            ubu.setCreatedAt(System.currentTimeMillis() / 1000);
//            ubu.setBalance(result.after);
//            ubu.setChange(moneyStack);
//            ubu.setLastBalance(result.before);
//            ubu.setDescription("Buy stack in lobby");
//            ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_PLAY_GAME);
//            ubu.setChannel(Database.instance.getUserChannel(userId));
//            if (moneyType == MoneyContants.MONEY) {
//                ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
//                ubu.setUnit("real");
//            } else {
//                ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_POINT, Locale.ENGLISH));
//                ubu.setUnit("point");
//            }
//            QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
            
            this.game.getLogger().info("buy stack in lobby success: money buy = "+moneyStack.doubleValue() +", sumMoneyBefore ="+result.before.doubleValue() +", sumMoneyafter ="+result.after.doubleValue());
            return true;
        } catch (Exception e) {
            this.game.getLogger().error("updateMoneyBuyStack() error:", e);
        }
        return false;
    }

    /**
     * Get id database của user của user
     *
     * @param user
     * @return
     */
    protected String getIdDBOfUser(User user) {
        return Utils.getIdDBOfUser(user);
        }

    /**
     * Lấy ra vị tri chổ ngồi của user trong ván
     *
     * @param user
     * @return
     */
    protected int getSeatNumber(User user) {
        String idDBUser = this.getIdDBOfUser(user);
        if (idDBUser == null || idDBUser.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < playerSeats.length; i++) {
            if (playerSeats[i] == null) {
                continue;
            }
            if (playerSeats[i].equals(idDBUser)) {
                return i;
            }
        }
        return -1;

    }

    /**
     * default khi thằng chủ bàn thoát sẽ chuyển cho thằng ngồi kế bên làm chủ
     * bàn
     *
     * @param player
     * @return
     */
    protected User findOwner(User player) {
        String idDBPlayer = getIdDBOfUser(player);
        int sNum = getSeatNumber(idDBPlayer);
        int length = this.playerSeats.length;
        for (int i = 0; i < length; i++) {
            sNum = (sNum + 1) % length;
            User u = getUser(sNum);
            if (u != null && !getIdDBOfUser(u).equals(idDBPlayer)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Gửi thông tin start ván
     * @param timeStart
     */
    protected void sendBoardInforMessage(int timeStart) {
        try {
            sendAllUserMessage(this.getBoardInfoMessage(timeStart));
        } catch (Exception e) {
            this.game.getLogger().error("sendBoardInforMessage() erro: ", e);
        }
    }

    /**
     * Xử lý chạy countDownstartGame
     */
    protected void processCountDownStartGame() {
        try {
            List<User> players = this.getPlayersList();
           
            if (!isPlaying() && players.size() > 1) {
                 boolean canStart = checkPlayersToStartAndCountDown();
                /*
                 - Nếu có 2 user thì reset countDown,ngược lại khi có 3 user trở lên
                 thì không reset countDown
                 - Trạng thái đang countDown thì không xét nữa
                 */
                if (!this.getGameState().equals(this.getCountDownStartGameState()) && canStart) {
                    setStateGame(this.getCountDownStartGameState());
                    setCurrentMoveTime();
                    if(checkRandomCountDownWhenHaveBot()){
                        /**
                         * Nếu bàn có 2 bot thì khi user vào sẽ random trong 2 trường hợp:
                         * - User vào và random countdown từ 10s -1s.
                         * - Start ván cho 2 bot trước rồi mới cho user vào bàn.
                         */
                        addCurrentMoveTime(-random.nextInt(8000));
                    }
                    
                }
                sendBoardInforMessage(getTimeToStart());
                isEnoughPlayersToStart.set(canStart);
            }
            
        } catch (Exception e) {
            this.game.getLogger().error("processCountDownStartGame() error:", e);
        }
    }
    
    /**
     * Add user vào danh sách user chơi trong bàn không phải là viewer
     *
     * @param user
     */
    private void addPlayer(User user) {
        int seat = getSeatNumber(user);
        if (seat > -1) {
            MagagerUserState managerUserState = new MagagerUserState();
            managerUserState.setAutoBuyInStack(getMoneyFromUser(user));
            setManagerUserState(user, managerUserState);
            
            initActionOfUser(user);
            setInturn(user, false);
            getManagerUsersState(user).setPlayerState();
            players.put(seat, user);
            /**
             * Là user đầu tiền join ván thành công xét là owner
             */
            if (this.getPlayersList().size() == 1) {
                setOwner(user);
            }
        }
        Board board = HazelcastUtil.getBoardInfor(this.room.getName());
        if (board != null) {
            board.setFreeSeat(getFreeSeat());
            board.setIsPlaying(isPlaying());
            board.getIps().put(getIdDBOfUser(user), user.getSession().getAddress());
            HazelcastUtil.addBoardWaitingInfor(board);
        }
    }
    
    /**
     * update trạng thái bàn lên hazelcast
     */
    protected  void updateBoardPlayingToHazelcast(){
        Board board = HazelcastUtil.getBoardInfor(this.room.getName());
        if (board != null) {
            board.setFreeSeat(getFreeSeat());
            board.setIsPlaying(isPlaying());
            HazelcastUtil.addBoardWaitingInfor(board);
        }
    }
    /**
     * Remove khỏi danh sách khi chuyển thành viewer và leave khỏi board
     *
     * @param seat
     */
    private void removePlayer(User user) {
        int seat = getSeatNumber(user);
        if (players.containsKey(seat)) {
            players.remove(seat);
        }
        Board board = HazelcastUtil.getBoardInfor(this.room.getName());
        if (board != null) {
            board.setFreeSeat(getFreeSeat());
            board.setIsPlaying(isPlaying());
            String idIB=getIdDBOfUser(user);
            if(board.getIps().containsKey(idIB)){
                board.getIps().remove(idIB);
            }
            HazelcastUtil.addBoardWaitingInfor(board);
        }
    }

    /**
     * lấy ra danh sách player
     *
     * @return
     */
    protected List<User> getPlayersList() {
        List<User> listUser = new ArrayList<>();
        for (User user : this.getAllPlayers()) {
            if (isPlayerState(user)) {
                listUser.add(user);
            }
        }
        return listUser;
    }
    
    /**
     * Danh sách id player
     * @return 
     */
    protected List<String> getPlayerInturnIds() {
        List<String> ids = new ArrayList<>();
        for (User user : this.getAllPlayers()) {
            if (isInturn(user) && isPlayerState(user)) {
                ids.add(getIdDBOfUser(user));
            }
        }
        return ids;
    }
    
    /**
     * Lấy ra danh sách user đợi
     * @return 
     */
    protected List<User> getAllWaiter(){
         List<User> listUser = new ArrayList<>();
        for (User user : this.getAllPlayers()) {
            if (!isPlayerState(user)) {
                listUser.add(user);
            }
        }
        return listUser;
    }
    protected List<User> getAllPlayers() {
        return new ArrayList<>(this.players.values());
    }

    /**
     * danh sách player max trong game(khong bao gòm viewer)
     *
     * @return
     */
    protected int getPlayersSize() {
        return this.room.getMaxUsers();
    }

    /**
     * số chổ ngồi còn trống trong game
     *
     * @return
     */
    protected int getFreeSeat() {
        return getPlayersSize() - players.size();
    }

    /**
     * update thông tin board và board waiting đến hazelcast
     */
    private void updateBoardInforToHazelcast() {
        //update thông tin chung của board
        Board board = HazelcastUtil.getBoardInfor(this.room.getName());
        if (board != null) {
            board.setFreeSeat(getFreeSeat());
            board.setIsPlaying(isPlaying());
        }
        HazelcastUtil.addBoardWaitingInfor(board);
    }

    public abstract void initMaxUserAndViewer();

    protected User getOwner() {
        return this.room.getOwner();
    }

    public void addMessage(Object m) {
        if (m != null) {
            messages.add(m);
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.room.getGroupId() + "-" + this.room.getId());
        ISFSObject m;
        while (true) {
            try {
                if (isStop.get()) {
                    break;
                }
                Object o = messages.poll(1, TimeUnit.SECONDS);
                if (o instanceof ISFSObject) {
                    m = (ISFSObject) o;
                    String idDBUser = m.getUtfString(SFSKey.ID_DB_USER);
                    User user = this.findUserToProcessMesssage(idDBUser);
                    if (user == null) {
                        continue;
                    }
                    processMessage(user, m);
                }
                update();
            } catch (Exception e) {
                this.game.getLogger().error("run thread GameController error:", e);
            } finally {
                if (this.room == null || isCanRemoveRoom()) {
                    this.game.getLogger().info("ready remove room ");
                    isStop.set(true);
                    initialization.set(false);
                    if (this.room != null) {
                        //kiểm tra nếu ván không còn lại user isPlayer nào thì remove ván đi
                        if (this.game.getParentZone().getRoomByName(this.room.getName()) != null) {
                            kickAllUser();
                            //remove room khoi hazelcast
                            Board board = HazelcastUtil.getBoardInfor(room.getName());
                            HazelcastUtil.removeBoardInfor(room.getName());
                            HazelcastUtil.removeBoardWaitingInfor(board);
                            this.game.getLogger().info("room is removed"+room.getName());
                            this.game.getApi().removeRoom(room);
                        }else{
                            this.game.getLogger().info("find room to remove error : "+room.getName());
                        }
                    }
                }
            }
        }
    }
    
    public boolean isCanRemoveRoom(){
        return this.getPlayersList().isEmpty();
    }

    /**
     * tìm theo viewer va player vì viewer có thể gửi lên mua tẩy
     *
     * @param idDBUser
     * @return
     */
    private User findUserToProcessMesssage(String idDBUser) {
        if (idDBUser == null) {
            return null;
        }

        for (User user : this.room.getUserList()) {
            if (idDBUser.equals(getIdDBOfUser(user))) {
                return user;
            }
        }
        return null;
    }

    /**
     * Khởi tạo chạy 1 thread trong gameController
     */
    protected final void initialize() {
        if (!initialization.get()) {
            synchronized (initializeSessionLock) {
                if (!initialization.get()) {
                    initialization.set(true);
                    isStop.set(false);
                    ThreadPoolGame.getPool().executeGameController(this);
                    this.game.getLogger().info(ThreadPoolGame.getPool().toString());
                }
            }
        }
    }

    /**
     * get the last move time of this board.
     *
     * @return current move time in this board
     */
    protected long getCurrentMoveTime() {
        return currentMoveTime;
    }

    /**
     * set current move time in this board to sytem current time.
     */
    protected void setCurrentMoveTime() {
        currentMoveTime = System.currentTimeMillis();
    }
    
    /**
     * Cộng thêm thời gian move
     * @param time 
     */
    protected void addCurrentMoveTime(int time){
        currentMoveTime+=time;
    }

    /**
     * Lấy ra thời gian auto start của game
     *
     * @return
     */
    protected int getTimeAutoStartDefault() {
        return RoomConfig.getInstance().getWaitingTime(getNameLobby());
    }

    /**
     * get current time limit for a turn in this board.
     *
     * @return time limit by second.
     */
    public int getTimeLimit() {
        return timeLimit;
    }

    /**
     * set new time limit for a turn.
     *
     * @param timeLimit time limit by second.
     */
    protected void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * Hết một lượt đánh
     *
     * @return
     */
    protected boolean isTimeout() {
        return System.currentTimeMillis() - getCurrentMoveTime() > getTimeLimit();
    }

    /**
     * Thời gian còn lại của một lượt second
     *
     * @return
     */
    public int getTimeRemain() {
        return (int) ((getTimeLimit() - (System.currentTimeMillis() - getCurrentMoveTime())) / 1000);
    }

    /**
     * room: tiền thiệt(type=2) hay tiền ảo (type=1)
     *
     * @return
     */
    protected final int getMoneyType() {
        return moneyType;
    }

    /**
     * Số tiền tối thiểu để user mua tẩy
     *
     * @return
     */
    protected BigDecimal getMinStack() {
        //default là 10 lần tiền cược
        BigDecimal minStack = getMoney().multiply(BigDecimal.TEN);
        try {
            minStack = Utils.multiply(getMoney(),  new BigDecimal(String.valueOf(getMinJoinGame())));
        } catch (Exception e) {
            this.game.getLogger().error("GameController.getMinStack() erro:", e);
        }
        minStack = Utils.getRoundBigDecimal(minStack);
        return minStack;
    }
    
    /**
     * Số tiền tối thiểu user có thể rebuy-in vao tiếp
     * @return 
     */
    private BigDecimal getMinStackReBuyIn(User user) {
        BigDecimal minStack = getMoney();

        BigDecimal moneyOfUser = this.getMoneyFromUser(user);
        if (moneyOfUser.compareTo(getMinStack()) < 0) {
            minStack = Utils.subtract(getMinStack(), moneyOfUser);
        }
        minStack = Utils.getRoundBigDecimal(minStack);
        return minStack;
    }

    /**
     * Số tiền tẩy tối đa user có thể mua vào
     *
     * @param user
     * @return
     */
    protected BigDecimal getMaxStack(User user) {
        String idDBUser = getIdDBOfUser(user);
        //số tiền hiện có của user
        BigDecimal moneyOfUser = BigDecimal.ZERO;
        try {
            if (getMoneyType() == MoneyContants.MONEY) {
                moneyOfUser = new BigDecimal(Database.instance.getUserMoney(idDBUser));
            } else {
                moneyOfUser = new BigDecimal(Database.instance.getUserPoint(idDBUser));
            }
        } catch (Exception e) {
            this.game.getLogger().error("GameController.getMaxStack()error: ", e);
        }
        
        moneyOfUser = Utils.getRoundBigDecimal(moneyOfUser);
        return moneyOfUser;
    }

    /**
     * Kiểm tra board đã full chưa
     * @return 
     */
    private boolean isFullBoard(){
        return getFreeSeat()==0;
    }
    
    /**
     * user mua thêm tẩy
     *
     * @param user
     * @param moneyStackInput
     */
    protected void processBuyStack(User user, double moneyStackInput) {
        try {
            BigDecimal moneyStack = new BigDecimal(String.valueOf(moneyStackInput));
            moneyStack = Utils.getRoundBigDecimal(moneyStack);
            BigDecimal beforeMoneyUser = getMoneyFromUser(user);
            
            if (user.isSpectator() && isFullBoard()) {
                sendToastMessage(GameLanguage.getMessage(GameLanguage.IS_FULL_BOARD, getLocaleOfUser(user)), user, 3);
                return;
            }
             
             BigDecimal sumMoney = Utils.add(moneyStack, getMoneyFromUser(user));
            //tiền mua vào + tiền tẩy không đủ min stack
            if (sumMoney.compareTo(this.getMinStack()) < 0) {
                String infor = String.format(GameLanguage.getMessage(GameLanguage.NO_SUM_MONEY, getLocaleOfUser(user)), getCurrency(getLocaleOfUser(user)));
                sendToastMessage(infor, user, 3);
                return;
            }
            
            BigDecimal maxStack = this.getMaxStack(user);
            //tiền tẩy +  tiền tổng không đủ
            BigDecimal moneyOfUserSum =Utils.add( getMoneyFromUser(user), maxStack);
            if (moneyOfUserSum.compareTo(this.getMinStack()) < 0) {
                sendNoMoneyMessage(user);
                addToWaitingUserList(user, "");
                return;
            }

            if (!updateMoneyToDB(user, moneyStack)) {
                checkSendBuyStackMessage(user, maxStack.doubleValue());
                return;
            }
            
            if (!isPlayerState(user)) {
                waiterBuyStack(user); 
            }
            //luu tien buy stack de su dung khi auto-buy in
            MagagerUserState userStateManager = getManagerUsersState(user);
            userStateManager.setAutoBuyInStack(moneyStack);

            addBuyStackBoardDetail(user, beforeMoneyUser.doubleValue(), getMoneyFromUser(user).doubleValue(), moneyStack.doubleValue());
            addBuyStackInvoiceDetail(user, beforeMoneyUser.doubleValue(), getMoneyFromUser(user).doubleValue(), moneyStack.doubleValue());
            sendPopupMessage(user, String.format(GameLanguage.getMessage(GameLanguage.INFOR_BUY_STACK, getLocaleOfUser(user)),Utils.getStringStack(moneyStack.doubleValue(),getMoneyType()),getCurrency(getLocaleOfUser(user))));
        } catch (Exception e) {
            this.game.getLogger().error("GameController.buyStack()error: ", e);
        }
    }
    
    /**
     * update tiền đến db
     * @param user
     * @param money 
     */
    private boolean updateMoneyToDB(User user, BigDecimal money) {
        String userId = getIdDBOfUser(user);
        try {
            UserState userState = HazelcastUtil.getUserState(userId);

            UpdateMoneyResult result;
            //chổ này check lại type money để update tiền thiệt va tiền ảo
            if (getMoneyType() == MoneyContants.MONEY) {
                result = Database.instance.callBuyStackMoneyProcedure(userId, money);
            } else {
                result = Database.instance.callBuyStackPointProcedure(userId, money);
            }
            //update tiền DB không thành công
            if (result.before.compareTo(result.after) == 0) {
                return false;
            }
            userState.setIsUpdateMoneySum(true);
            HazelcastUtil.updateUserState(userState);

//            UserBalanceUpdate ubu = new UserBalanceUpdate();
//            ubu.setPlayerId(userId);
//            ubu.setServiceId(getServiceId());
//            ubu.setEmail(userState.getEmail());
//            ubu.setSessionId(userState.getSessionId());
//            ubu.setRequestId(Utils.md5String(userId + money.doubleValue() + System.currentTimeMillis()));
//            ubu.setLogId(ubu.getRequestId());
//            ubu.setConnectionId(ServerConfig.getInstance().getConnectionId());
//            ubu.setCreatedAt(System.currentTimeMillis() / 1000);
//            ubu.setBalance(result.before);
//            ubu.setChange(money.negate());
//            ubu.setLastBalance(result.before);
//            ubu.setDescription("Buy stack in game");
//            ubu.setPaymentFlow(UserBalanceUpdate.PAYMENT_FLOW_PLAY_GAME);
//            ubu.setChannel(Database.instance.getUserChannel(userId));
//            if (moneyType == MoneyContants.MONEY) {
//                ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_MONEY, Locale.ENGLISH));
//                ubu.setUnit("real");
//            } else {
//                ubu.setCurrency(GameLanguage.getMessage(GameLanguage.NAME_POINT, Locale.ENGLISH));
//                ubu.setUnit("point");
//            }
//            QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyBalance(), true, ubu);
            //update lại tẩy của user
            updateMoneyStackOfUser(user, result.stack);
            this.game.getLogger().info(userId+" buy stack success: buy ="+money.doubleValue()+", sumMoneyBeforer="+result.before.doubleValue()+", sumMoneyAfter="+result.after.doubleValue());
            return true;
        } catch (Exception e) {
            this.game.getLogger().error("GameController.buyStack()error: ", e);
        }
        return false;
    }

    /**
     * Khi Waiter mua tẩy
     *
     * @param user
     */
    protected void waiterBuyStack(User user) {
        getManagerUsersState(user).setPlayerState();
        /**
         * Là user đầu tiền join ván thành công xét là owner
         */
        if (this.getPlayersList().size() == 1) {
            setOwner(user);
        }
        updateStatusWaiterUser(user, false);
        processCountDownStartGame();
    }

    /**
     * Kiểm tra tiền của user, nếu hết tiền thì chuyển thành viewer và gửi
     * message user mua tẩy
     */
    protected void checkSendAndAutoBuyStack() {
        //kick ra những user không đủ tiền
        for (User user : this.getAllPlayers()) {
            if (getMoneyFromUser(user).compareTo(getMoneyToContinue()) < 0) {
                BigDecimal maxStack = this.getMaxStack(user);
                //nếu tiền tẩy + tiền tổng -> không đủ thì kick lun
                BigDecimal moneyOfUserSum = Utils.add(getMoneyFromUser(user),maxStack) ;
                if (moneyOfUserSum.compareTo(this.getMoneyToContinue()) < 0) {
                    sendNoMoneyMessage(user);
                    addToWaitingUserList(user, "");
                    continue;
                }
                if (isPlayerState(user)) {
                    MagagerUserState userStateManager = getManagerUsersState(user);
                    if (userStateManager.isIsAutoBuyIn()) {
                        BigDecimal moneyToBuy = Utils.subtract(this.getMoneyToContinue(), getMoneyFromUser(user));
                        moneyToBuy = moneyToBuy.max(userStateManager.getAutoBuyInStack());//phai dam bao mua vao đủ minjoin
                        
                        BigDecimal buyStack = maxStack.min(moneyToBuy);
                        processBuyStack(user, buyStack.doubleValue());
                    } else {
                        addToWaitingUserList(user, "");
                        checkSendBuyStackMessage(user, maxStack.doubleValue());
                    }
                } 
            }
        }
    }

    /**
     * Kiểm tra số ván user ngồi giữ ván(SITOUT) quá 2 ván sẽ bi kick ra khoi
     * ban
     */
    private void processCheckWaittingBoardUser() {
        for (User user : this.getAllPlayers()) {
            if (getManagerUsersState(user).getStateCurrent().getWaittingBoard() >= ServerConfig.getInstance().getMaxWaitingBoard()) {
                String infor = GameLanguage.getMessage(GameLanguage.NO_BUY_STACK, getLocaleOfUser(user));
                kickUser(user, infor);
            }
        }
    }

    /**
     * Update số ván user đã chờ
     */
    private void updateCountWaitingBoard(User u, int count) {
        try {
            getManagerUsersState(u).setWaitingBoard(count);
        } catch (Exception ex) {
            this.game.getLogger().error("setCountWaitingBoard() error: ", ex);
        }
    }

    /**
     * Xet trạng thái của user
     *
     * @param u
     * @param userState
     */
    private void setManagerUserState(User u, MagagerUserState userState) {
        u.setProperty(UserInforPropertiesKey.USER_STATE, userState);
    }

    /**
     * Lấy ra trạng thái của user
     *
     * @param u
     * @return
     */
    private MagagerUserState getManagerUsersState(User u) {
        try {
            return (MagagerUserState) u.getProperty(UserInforPropertiesKey.USER_STATE);
        } catch (Exception e) {
            this.game.getLogger().error("getUsersState() error: ", e);
        }
        MagagerUserState managerUserState = new MagagerUserState();
        setManagerUserState(u, managerUserState);
        return managerUserState;
    }

    public boolean isPlayerState(User u) {
        return getManagerUsersState(u).isPlayer();
    }

    /**
     * Update lại chổ ngồi của user
     *
     * @param seat
     * @param user
     */
    private void updateSeatUser(User user, int seat) {
        UserVariable ver = new SFSUserVariable(UserInforPropertiesKey.SEAT_USER, seat);
        this.game.getApi().setUserVariables(user, Arrays.asList(ver));
        user.setProperty(UserInforPropertiesKey.SEAT_USER, seat);
    }

    public int getSeatUser(User user) {
        if(user == null){
            return -1;
        }
        return (int)user.getProperty(UserInforPropertiesKey.SEAT_USER);
    }
    
    private void updateAdvantageRatioForBot(User user, int ratio){
        user.setProperty(UserInforPropertiesKey.ADVANTAGE_RATIO, ratio);
    }
    
    protected int getAdvantageRatio(User user){
        return (int)user.getProperty(UserInforPropertiesKey.ADVANTAGE_RATIO);
    }
    
    private void updateAdFirstRatioForBot(User user, int ratio){
        user.setProperty(UserInforPropertiesKey.FIRST_RATIO, ratio);
    }
    
    protected int getAdFirstRatio(User user){
        return (int)user.getProperty(UserInforPropertiesKey.FIRST_RATIO);
    }
    
    private void updateGroupIDForBot(User user, int ratio){
        user.setProperty(UserInforPropertiesKey.GROUP_ID, ratio);
    }
    
    private int getGroupId(User user){
        return (int)user.getProperty(UserInforPropertiesKey.GROUP_ID);
    }
    
    public String getUserAvatar(User user) {
        if(user == null){
            return null;
        }
        return user.getVariable(UserInforPropertiesKey.AVATAR).getStringValue();
    }

    protected abstract byte getServiceId();

    /**
     * Lưu log chi tiết ván chơi
     *
     * @param user
     * @param actionId
     * @param moneyBefore
     * @param moneyAfter
     * @param value
     * @param tax
     * @param optionalArrayData
     */
    protected void addBoardDetail(User user, int actionId, double moneyBefore, double moneyAfter, double value,double tax, List<Short> optionalArrayData) {
        if (user == null) {
            return;
        }
        try {
            byte[] arrayCards;
            if (optionalArrayData == null) {
                arrayCards = new byte[0];
            } else {
                arrayCards = new byte[optionalArrayData.size()];
                for (int i = 0; i < optionalArrayData.size(); i++) {
                    arrayCards[i] = optionalArrayData.get(i).byteValue();
                }
            }
            
            BoardDetail boardDetail = new BoardDetail();
            boardDetail.setPlayerId(Utils.getIdDBOfUser(user));
            boardDetail.setPlayerName(getUserName(user));
            boardDetail.setActionId(actionId);
            boardDetail.setCreditBefore(moneyBefore);
            boardDetail.setCreditAfter(moneyAfter);
            boardDetail.setLogDate(Utils.getCurrentDateString("yyyyMMddhhmm"));
            boardDetail.setOptionalArrayData(arrayCards);
            boardDetail.setValue(value);

            invoices.getBoardDetail().add(boardDetail);
        } catch (Exception e) {
            this.game.getLogger().error("addBoardDetail() error: ", e);
        }
    }

    /**
     * Log thông tin ván chơi khi có phát sinh tiền gửi vể cho client
     *
     * @param user
     * @param reasonId
     * @param moneyBefore
     * @param moneyAfter
     * @param value
     * @param tax
     * @param optionalArrayData
     */
    protected void addInvoiceDetail(User user, int reasonId, double moneyBefore, double moneyAfter, double value, double tax,List<Short> optionalArrayData) {
        if (user == null) {
            return;
        }
        try {
            byte[] arrayCards;
            if (optionalArrayData == null) {
                arrayCards = new byte[0];
            } else {
                arrayCards = new byte[optionalArrayData.size()];
                for (int i = 0; i < optionalArrayData.size(); i++) {
                    arrayCards[i] = optionalArrayData.get(i).byteValue();
                }
            }
            //ghi log khi có phát sinh tiền trong game
            String userId = Utils.getIdDBOfUser(user);
            String logId = Utils.md5String(userId + value + tax + reasonId + System.currentTimeMillis());
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setLogId(logId);
            invoiceDetail.setPlayerId(userId);
            invoiceDetail.setPlayerName(getUserName(user));
            invoiceDetail.setCreditBefore(moneyBefore);
            invoiceDetail.setCreditAfter(moneyAfter);
            invoiceDetail.setOptionalArrayData(arrayCards);
            invoiceDetail.setReasonId(reasonId);
            invoiceDetail.setValue(value);
            invoiceDetail.setRake(tax);
            invoiceDetail.setLogDate(Utils.getCurrentDateString("yyyyMMddhhmm"));
            invoices.addInvoiceDetail(invoiceDetail);
        } catch (Exception e) {
            this.game.getLogger().error("addBoardDetail() error: ", e);
        }
    }

    /**
     * Kiểm tra idle time cua user
     *
     * @return
     */
    private boolean isOutOfIdleTime(User user) {
        if (user == null) {
            return false;
        }
        long idleTime = (System.currentTimeMillis() - user.getLastRequestTime()) / 1000;
        return idleTime > RoomConfig.getInstance().getIdleTimeLimit();
    }

    /**
     * Lấy ra số lần user không action khi tới lượt
     *
     * @param user
     * @return
     */
    private int getCountNoActionTime(User user) {
        if (user == null) {
            return 0;
        }
        return (int) user.getProperty(UserInforPropertiesKey.COUNT_NO_ACTION_TIME);
    }

    /**
     * Tăng dần số lần không action
     *
     * @param user
     */
    private void increaseNoActionTime(User user) {
        if (user == null ) {
            return;
        }
        int count = getCountNoActionTime(user) + 1;
        user.setProperty(UserInforPropertiesKey.COUNT_NO_ACTION_TIME, count);
    }

    protected void resetNoActionTime(User user) {
        if (user == null) {
            return;
        }
        user.setProperty(UserInforPropertiesKey.COUNT_NO_ACTION_TIME, 0);
    }
    
    private void initActionOfUser(User user){
         user.setProperty(UserInforPropertiesKey.COUNT_NO_ACTION_TIME, 0);
    }

    /**
     * Kick user khỏi server game
     *
     * @param user
     * @param infor
     */
    public void kickUser(User user, String infor) {
        try {
            this.game.getLogger().info("kickUser() :"+ getIdDBOfUser(user));
            //gui ve thông báo nguyên nhân kick
            this.game.getApi().sendModeratorMessage(null, infor, null, Arrays.asList(user.getSession()));
        } catch (Exception e) {
            this.game.getLogger().error("sendModeratorMessage() erro:", e);
        }
        //khi kick phải gọi leave() để đảm bảo user thoat game đúng process
        leave(user);
    }

    /**
     * Game đặt cược(bai cao, xi dach,...) Sau 2 vòng không đặt cược sẽ hiện
     * bảng thông báo AFK: Nếu user nhấn vào nút OK trên bảng thông báo khi màn
     * hình hiện ra sẽ cho phép user tiếp tục chơi. Nếu user không có hành động
     * khi màn hình hiện ra và tiếp tục để ván thứ 3 trôi qua sẽ cho user thoát
     * bàn.
     *
     * @param user
     */
    protected void checkNoActionBetGame(User user) {
        //tăng số lần không action theo ván chơi
        increaseNoActionTime(user);
        if (getCountNoActionTime(user) == (RoomConfig.getInstance().getMaxNoActionTime() - 1)) {
            String strInfor = GameLanguage.getMessage(GameLanguage.NO_ACTION_CONFIRM, Utils.getUserLocale(user));
            //gửi thông báo continue
            SFSObject ob = messageController.getContinueMessage(strInfor);
            sendUserMessage(ob, user);
            return;
        }

        if (getCountNoActionTime(user) >= RoomConfig.getInstance().getMaxNoActionTime()) {
            String strInfor = GameLanguage.getMessage(GameLanguage.NO_ACTION_IN_GAME, Utils.getUserLocale(user));
            kickUser(user, strInfor);
        }
    }

    /**
     * Game theo lượt (tlmn,...) Sau 2 vòng không đặt cược sẽ hiện bảng thông
     * báo AFK: Nếu user nhấn vào nút OK trên bảng thông báo khi màn hình hiện
     * ra sẽ cho phép user tiếp tục chơi. Nếu user không có hành động khi màn
     * hình hiện ra và tiếp tục để ván thứ 3 trôi qua sẽ cho user thoát bàn.
     *
     * @param user
     */
    protected void checkNoActionNotBetGame(User user) {
        increaseNoActionTime(user);
        if (getCountNoActionTime(user) >= (RoomConfig.getInstance().getMaxNoActionTime() - 1)) {
            String strInfor = GameLanguage.getMessage(GameLanguage.NO_ACTION_CONFIRM, Utils.getUserLocale(user));
            //gửi thông báo continue
            SFSObject ob = messageController.getContinueMessage(strInfor);
            sendUserMessage(ob, user);
        }
        
        updateMoneyStackOfUser(user, getMoneyFromUser(user));
    }

    /**
     * Kich những user không aciton trong game
     *
     * @param user
     */
    protected void kickNoActionUser(User user) {
        if (getCountNoActionTime(user) >= RoomConfig.getInstance().getMaxNoActionTime()) {
            String strInfor = GameLanguage.getMessage(GameLanguage.NO_ACTION_IN_GAME, Utils.getUserLocale(user));
            kickUser(user, strInfor);
        }
    }

    /**
     * Lấy ra tên game của lobby hiện tai de lấy thông tin từ file config
     *
     * @return
     */
    protected final String getNameLobby() {
        return nameLobby;
    }

    /**
     * Số tiền phạt khi rời bàn đang chơi
     *
     * @return
     */
    protected BigDecimal getPenalizeLeaver() {
        return penalize;
    }

    /**
     * Lấy ra số lần tiền đặt cược tối đa cho game đặt cược
     *
     * @return
     */
    protected int getMaxBet() {
        return RoomConfig.getInstance().getMaxBetGame(getNameLobby());
    }

    protected int getMaxPlayers() {
        return RoomConfig.getInstance().getNoPlayer(getNameLobby());
    }

    /**
     * Gọi de remove nhung user còn kẹt lại trong bàn khi remove room
     */
    private void kickAllUser() {
        try {
            //kick tat ca user he thong da giu
            for (User u : this.getAllWaiter()) {
                int seat = getSeatNumber(u);
                if (seat == -1) {
                    continue;
                }
                leave(u);
            }
            //kick tat ca user la viewer
            for (User u : this.room.getSpectatorsList()) {
                leave(u);
            }
        } catch (Exception e) {
             this.game.getLogger().error("GameController.kickAllUser()() erro:", e);
        }
    }
    
    public String getUserName(User user) {
        if (user == null) {
            return "";
        }
        return user.getVariable(UserInforPropertiesKey.DISPLAY_NAME).getStringValue();
    }

    /**
     * Thời gian 1 lượt chơi
     *
     * @return
     */
    protected int getPlayingTime() {
        return RoomConfig.getInstance().getPlayingTime(getNameLobby()) * 1000;
    }

    /**
     * Update trang thái user sit out
     *
     * @param seat
     * @param user
     */
    private void updateStatusWaiterUser(User user, boolean isWaiter) {
        UserVariable ver = new SFSUserVariable(UserInforPropertiesKey.IS_WAITER, isWaiter);
        this.game.getApi().setUserVariables(user, Arrays.asList(ver));
        user.setProperty(UserInforPropertiesKey.IS_WAITER, isWaiter);
        sendUpdateVariableUserMessage(user, UserInforPropertiesKey.IS_WAITER);
    }

    public boolean getStatusWaiter(User user) {
        return (boolean)user.getProperty(UserInforPropertiesKey.IS_WAITER);
    }

    /**
     * Update trạng thái của user trong game
     *
     * @param user
     * @param key
     */
    private void sendUpdateVariableUserMessage(User user, String key) {
        try {
            SFSObject ob = messageController.getVariableUserMessage(user, key);
            sendAllUserMessage(ob);
        } catch (Exception e) {
            this.game.getLogger().error("sendUpdateVariableUserMessage() error: ", e);
        }
    }

    /**
     * Gửi thông tin người chơi trong bàn cho user vua join
     *
     * @param user
     */
    private void sendJoinBoardMessage(User user) {
        try {
            //gửi thông tin tất cả user trong bàn cho user join
            SFSObject ob = messageController.getJoinRoomMessage(getAllPlayers());
            sendUserMessage(ob, user);
        } catch (Exception e) {
            this.game.getLogger().error("sendJoinBoardMessage() error: ", e);
        }
    }
    /**
     * Gửi thong tin user moi join board cho user trong ban
     * @param user 
     */
    private void sendAddPlayerMessage(User user) {
        //gửi thong tin user join cho tất cả user trong bàn
        SFSObject onUserJoin = messageController.getAddPlayerMessage(user);
        sendAllUserOutOfMeMessage(user, onUserJoin);
    }
    
    
    
    /**
     * Thuế của từng game
     * @return 
     */
    protected int getTax(){
        return RoomConfig.getInstance().getTax(getNameLobby());
    }
    
    /**
     * gửi cho client để điều hướng qua  màn hình nap khi user hết tiền
     * @param user 
     */
    private void sendNoMoneyMessage(User user){
        try {
            //gửi thông tin tất cả user trong bàn cho user join
            SFSObject ob = messageController.getNoMoneyMessage();
            sendUserMessage(ob, user);
        } catch (Exception e) {
             this.game.getLogger().error("sendNoMoneyMessage() error: ", e);
        }
    }
    
    /**
     * Gưi ve cho client dạng pop-up chat
     */
    private void sendPopupMessage(User user,String infor){
         try {
            //gửi thông tin tất cả user trong bàn cho user join
            SFSObject ob = messageController.getMessageInBoard(infor,getIdDBOfUser(user));
            sendUserMessage(ob, user);
        } catch (Exception e) {
             this.game.getLogger().error("sendPopupMessage() error: ", e);
        }
    }
    
    /**
     * Remove user khi user không còn trong room - chọn tự động rời bàn khi kết thúc ván
     */
    private void checkUserLostConnect(){
        for(User user: getAllPlayers()){
            if(!this.room.containsUser(user)){
                leave(user);
            }
        }
    }
    
    /**
     * 
     * @param user
     * @param tax 
     * @param point 
     */
    protected void sendRankingData(User user, double tax, int point) {
        if (ServerConfig.getInstance().enableRanking() && getMoneyType() == MoneyContants.MONEY) {
            executorLog.execute(new Runnable() {
                @Override
                public void run() {
                    GameDataObj obj = new GameDataObj();
                    obj.setBetmoney(getMoney().doubleValue());
                    obj.setBonusPoint(point);
                    obj.setService_id(getServiceId());
                    obj.setTax(tax);
                    obj.setWinUserid(getIdDBOfUser(user));
                    obj.setWinUsername(getUserName(user));
                    obj.setStartTime(startTime);
                    QueueServiceVip.getInstance().sendRankingData(obj);
                }
            });
        }
    }

    /**
     * Gửi trạng thái thắng thua của user trong ván
     * @param user 
     * @param type 
     * @return  
     */
    protected SFSObject getStatusLoseOrWinUserMessage(User user, int type){
        SFSObject ob =null;
        try {
             //gửi thông tin tất cả user trong bàn cho user join
            ob = messageController.getStatusUser(getIdDBOfUser(user), (byte) type);
        } catch (Exception e) {
            this.game.getLogger().error("sendStatusLoseOrWinUser() error: ", e);
        }
        return ob;
    }
    
    /**
     * Số tiền tối thiểu để user tiếp tục chơi ván tiếp theo
     * @return 
     */
    protected BigDecimal getMoneyToContinue(){
        return getMinStack();
    }
    
    /**
     * update win-lose-draw-give up
     * @param u
     * @param reasonId 
     */
    protected void updateAchievement(User u, int reasonId) {
        for (PlayersDetail playerInfor : invoices.getPlayersDetail()) {
            if (playerInfor.getPlayerId().equals(getIdDBOfUser(u))) {
                playerInfor.setResultId(reasonId);
                break;
            }
        }
    }

    protected boolean isShuffleRoom() {
        return room.getVariable(RoomInforPropertiesKey.MODE).getIntValue() == PlayMode.SHUFFLE;
    }

    /**
     * Is auto buy in
     * @param isAutoBuyIn 
     */
    private void setAutoBuyIn(User u, boolean isAutoBuyIn){
        MagagerUserState managerUserState = getManagerUsersState(u);
        managerUserState.setIsAutoBuyIn(isAutoBuyIn);
        sendAutoBuyInHandMessage(u, isAutoBuyIn);
    }
    
    /**
     * isAutoLeave
     * @param isAutoLeave 
     */
    private void setAutoLeave(User u, boolean isAutoLeave){
        MagagerUserState managerUserState = getManagerUsersState(u);
        managerUserState.setIsLeaveGame(isAutoLeave);
        sendAutoLeaveMessage(u, isAutoLeave);
    }
    
    /**
     * Gửi thong tin auto muck hand to user
     * @param user 
     */
    private void sendAutoBuyInHandMessage(User user, boolean isAutoBuyIn){
        try {
            SFSObject ob = messageController.getAutoBuyInMessage(isAutoBuyIn);
            sendUserMessage(ob, user);
        } catch (Exception e) {
            this.game.getLogger().error("sendLastHandMessage() error: ", e);
        }
    }

     /**
     * Gửi thong tin auto muck hand to user
     * @param user 
     */
    private void sendAutoLeaveMessage(User user, boolean isAutoLeave){
        try {
            SFSObject ob = messageController.getAutoLeaveMessage(isAutoLeave);
            sendUserMessage(ob, user);
        } catch (Exception e) {
            this.game.getLogger().error("sendAutoLeaveMessage() error: ", e);
        }
    }

    protected void doShuffle() {
        // kick afk user
        for (User user : getAllWaiter()) {
            String infor = GameLanguage.getMessage(GameLanguage.NO_ACTION_IN_GAME, Utils.getUserLocale(user));
            kickUser(user, infor);
        }

        sendShuffleMessage();
        
        // leaveShuffle
        for (User user : getPlayersList()) {
            game.getApi().leaveRoom(user, room);
        }
    }
    
    private void updateWithdrawInfo(Invoices invoices) {
        executorLog.execute(() -> {
            for (PlayersDetail pDetal : invoices.getPlayersDetail()) {
                if (pDetal.getWinAmount() > 0) {
                    if (getMoneyType() == MoneyContants.MONEY) {
                        BigDecimal quota = new BigDecimal(pDetal.getWinAmount());
                        Database.instance.addUserAvailableWithdrawMoney(pDetal.getPlayerId(), quota);
                    } else {
                        PointConvertConfig wcc = Database.instance.getPointConvertConfig();
                        String userId = pDetal.getPlayerId();
                        int quota = (int) (pDetal.getWinAmount() / wcc.getTurnOver());
                        game.getLogger().debug("update quota: " + userId + " " + quota);
                        Database.instance.addPointConvertQuota(userId, new BigDecimal(quota));
                    }
                }
            }
        });

    }

    /**
     * Xử lý event trong bàn
     */
    private void onEventProcess() {
        try {
            if (!isEnableEvent()) {
                return;
            }

            Map<String,Double> turnOvers = new HashMap<>();
            for (PlayersDetail playerDetail : invoices.getPlayersDetail()) {
                String userId = playerDetail.getPlayerId();
                double turnOver = 0;
                for (InvoiceDetail invoiceDetail : invoices.getInvoiceDetail()) {
                    if (invoiceDetail.getReasonId() != CommonMoneyReasonUtils.BUY_STACK && invoiceDetail.getPlayerId().equals(userId)) {
                        if (invoiceDetail.getRake() > 0) {
                            // hanv: khi có thuế là có tiền thắng, dùng thuế để tính ngược lại tiền thắng trước thuế, tránh trường hợp tiền thắng tính thêm tiền trả cược
                            turnOver = Utils.add(turnOver, Utils.multiply(Utils.divide(invoiceDetail.getRake(), getTax()), 100));
                        }
                    }
                }
                turnOvers.put(userId, turnOver);
            }

            /**
             * Add event user tham gia trong bàn
             */
            for (int i = 0; i < getPlayersSize(); i++) {
                User user = getUser(i);
                if (user != null && isInturn(i)) {
                    double turnOver =0;
                    String idDBUser = getIdDBOfUser(user);
                    if(turnOvers.containsKey(idDBUser)){
                        turnOver = turnOvers.get(idDBUser);
                }
                    addPlayingUserGetEvent(user, turnOver, EventManager.PLAYING_USER, new ArrayList<>());
            }
            }
            
            List<UserCardsObj> listUserEvent = eventManager.getListUserGetEvent();
            for (UserCardsObj userCardsObj : listUserEvent) {
                /**
                 * Phải nằm trong danh sách event trong config và ván bài phải
                 * phát sinh thuế thì mới gửi event lên 
                 */
                if (!EventConfig.getInstance().getListCondition().contains(userCardsObj.getCondition())) {
                    continue;
                }

                int seat = getSeatNumber(userCardsObj.getUserId());
                if (seat == -1) {
                    continue;
                }
                
                User u = getUser(seat);
                if (u == null) {
                    continue;
                }
                userCardsObj.setInvoiceId(invoices.getInvoiceId());
                BestCardsEventTask task = new BestCardsEventTask(userCardsObj);
                eventManager.runEventTask(task);
            }
            eventManager.clearListUserGetEvent();
        } catch (Exception e) {
            eventManager.clearListUserGetEvent();
            this.game.getLogger().error("onEventProcess() error: ", e);
        }
    }
    
    protected void addUserGetEvent(User u, String condition, List<Integer> cardIds){
        // chưa mở event
        if (!EventConfig.getInstance().isEnableEvent()) {
            return;
        }

        if (u == null) {
            return;
        }

        if (!EventConfig.getInstance().getListCondition().contains(condition)) {
            return;
        }
        
        UserCardsObj userCardsObj = new UserCardsObj();
        userCardsObj.setCondition(condition);
        userCardsObj.setGameId(String.valueOf(getServiceId()));
        userCardsObj.setListCardIds(cardIds);
        userCardsObj.setMoneyBoard(getMoney().doubleValue());
        userCardsObj.setMoneyType(getMoneyType());
        userCardsObj.setNumOfPlayer(getPlayersList().size());
        userCardsObj.setRoomName(this.room.getName());
        userCardsObj.setServerId(ServerConfig.getInstance().getServerId());
        userCardsObj.setUserId(getIdDBOfUser(u));
        userCardsObj.setTimeCreate(System.currentTimeMillis() / 1000);
        eventManager.addUserGetEvent(userCardsObj);
    }

     private void addPlayingUserGetEvent(User u, double turnOver, String condition, List<Integer> cardIds){
        // chưa mở event
        if (!EventConfig.getInstance().isEnableEvent()) {
            return;
        }

        if (u == null) {
            return;
        }

        if (!EventConfig.getInstance().getListCondition().contains(condition)) {
            return;
        }
        
        UserCardsObj userCardsObj = new UserCardsObj();
        userCardsObj.setCondition(condition);
        userCardsObj.setGameId(String.valueOf(getServiceId()));
        userCardsObj.setListCardIds(cardIds);
        userCardsObj.setMoneyBoard(getMoney().doubleValue());
        userCardsObj.setMoneyType(getMoneyType());
        userCardsObj.setNumOfPlayer(getPlayersList().size());
        userCardsObj.setRoomName(this.room.getName());
        userCardsObj.setServerId(ServerConfig.getInstance().getServerId());
        userCardsObj.setUserId(getIdDBOfUser(u));
        userCardsObj.setTurnOver(turnOver);
        userCardsObj.setTimeCreate(System.currentTimeMillis() / 1000);
        eventManager.addUserGetEvent(userCardsObj);
    }

    /**
     * Kiểm tra có mở event cho game không
     * @return 
     */
    protected  boolean isEnableEvent(){
        return EventConfig.getInstance().isEnableEvent() && 
                EventConfig.getInstance().checkHaveEventToServiceId(getServiceId());
    }
    
    /**
     * config mở pot và phải là money game
     *
     * @return
     */
    protected boolean isOpenBotGame() {
        return botAdv.isEnable();
    }
    
    protected int getAdvRatio() {
        return botAdv.getAdvRatio();
    }
    
    protected int getAdvMinPoint() {
        return botAdv.getMinPoint();
    }
    
    /**
     * Kiểm tra số lượng player và bot để start game:
     * - nếu bàn toàn user thường, ko có bot -> xử lý như bình thường
     * - nếu bàn có 1 bot, còn lại là user -> xử lý bình thường
     * - nếu bàn toàn là bot, ko có user -> ko start ván
     * @return 
     */
    protected  boolean checkPlayersToStartAndCountDown(){
        List<User> users = this.getAllPlayers();
        if(users.size() < 2){
            return false;
        }
        
        if(!isOpenBotGame()){
            return this.getPlayersList().size() >= 2;
        }
        
        if(isAdvantageRatioByGroupID()){
            return isCountDownByGroup(users);
        }
        
        return isCountDownNotByGroup(users);
    }
    
    private boolean isCountDownByGroup(List<User> users){
        List<Integer> botGroupIds = new ArrayList<>();
        int countBot = 0;
        int countPlayer =0;
        for(User u: users){
            if(!isPlayerState(u)){
                continue;
            }
            countPlayer++;
            if(isBot(u)){
                countBot++;
                botGroupIds.add(getGroupId(u));
            }
        }
        
        if(botGroupIds.size() >1){
            int groupId = botGroupIds.get(0);
            for(int id: botGroupIds){
                if(groupId != id){
                    return true;
                }
            }
        }
        if(countPlayer < 2){
            return false;
        }
        
        if(countBot !=users.size()){
            return true;
        }
        
        return false;
    }
    
    private boolean isCountDownNotByGroup(List<User> users) {
        int countBot = 0;
        int countPlayer = 0;
        for (User u : users) {
            if (!isPlayerState(u)) {
                continue;
            }
            countPlayer++;
            if (isBot(u)) {
                countBot++;
            }
        }
        if (countPlayer < 2) {
            return false;
        }

        if (countBot != users.size()) {
            return true;
        }
        return false;
    }
    
        /**
     * nếu bàn có 2 bot đang chờ, user vào là user thường: 
     * random 50% start ván trước rồi đẩy user vào hoặc đẩy user vào rồi count down như bình thường
     */
    private void processJoinWhenHaveBot(User joiner){
        if( isPlaying() || !isOpenBotGame()){
            return ;
        }
        
        int percent = random.nextInt(100);
        if(percent < 50){
            return;
        }
        
        if(!checkAllUserIsBot() ){
            return ;
        }
        if(isBot(joiner)){
            return;
        }
        startGame();
        
    }
    
    /**
     * Kiểm tra tất cả user trong bàn có phải là bot, so luong bot >=2
     * @return 
     */
    private boolean checkAllUserIsBot() { 
        List<User> users = getAllPlayers();
        if(users.size() <2){
            return false;
        }
        int countBot = 0;
        for (User u : users) {
            if (!isPlayerState(u)) {
                continue;
            }
            if (isBot(u)) {
                countBot++;
            }
        }

        if (countBot < 2) {
            return false;
        }

        if (countBot != users.size()) {
            return false;
        }
        return true;
    }
    
    /**
     * Request: bot >=2 && 1 Player
     * @return 
     */
    private boolean checkRandomCountDownWhenHaveBot(){
        if(!isOpenBotGame()){
            return false;
        }
        List<User> users = getAllPlayers();
        if(users.size() <3){
            return false;
        }
        
        int countBot = 0;
        int countPlayerNotBot =0;
        for (User u : users) {
            if (!isPlayerState(u) && isBot(u)) {
                return false;
            }
            if (isBot(u)) {
                countBot++;
            }else{
                countPlayerNotBot++;
            }
        }
       
        if (countBot < 2 || countPlayerNotBot!=1) {
            return false;
        }
         
        return (countBot+countPlayerNotBot) == users.size();
    }

    /**
     * isPlaying thì không thể thoát khỏi bàn
     * @param u
     * @return 
     */
    public boolean isCanLeave(User u){
        if(isUserCanLeave(u)){
            return true;
            }
            return !isPlaying();
        }

    protected boolean isUserCanLeave(User u){
        return !isInturn(u);
    }
    
        
    private void addBuyStackInvoiceDetail(User user, double moneyBefore, double moneyAfter, double value) {
        try {
            //ghi log khi có phát sinh tiền trong game
            String userId = Utils.getIdDBOfUser(user);
            String logId = Utils.md5String(userId + value + 0 + CommonMoneyReasonUtils.BUY_STACK + System.currentTimeMillis());
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setLogId(logId);
            invoiceDetail.setPlayerId(userId);
            invoiceDetail.setPlayerName(getUserName(user));
            invoiceDetail.setCreditBefore(moneyBefore);
            invoiceDetail.setCreditAfter(moneyAfter);
            invoiceDetail.setOptionalArrayData(new byte[0]);
            invoiceDetail.setReasonId(CommonMoneyReasonUtils.BUY_STACK);
            invoiceDetail.setValue(value);
            invoiceDetail.setRake(0);
            invoiceDetail.setLogDate(Utils.getCurrentDateString("yyyyMMddhhmm"));
            invoiceBuyStackDetails.add(invoiceDetail);
        } catch (Exception e) {
            this.game.getLogger().error("addBuyStackInvoiceDetail() error: ", e);
        }
    }
    
    private void addBuyStackBoardDetail(User user, double moneyBefore, double moneyAfter, double value) {
        if (user == null) {
            return;
        }
        try {
            BoardDetail boardDetail = new BoardDetail();
            boardDetail.setPlayerId(Utils.getIdDBOfUser(user));
            boardDetail.setPlayerName(getUserName(user));
            boardDetail.setActionId(CommonMoneyReasonUtils.BUY_STACK);
            boardDetail.setCreditBefore(moneyBefore);
            boardDetail.setCreditAfter(moneyAfter);
            boardDetail.setLogDate(Utils.getCurrentDateString("yyyyMMddhhmm"));
            boardDetail.setOptionalArrayData(new byte[0]);
            boardDetail.setValue(value);

            boardBuyStackDetails.add(boardDetail);
        } catch (Exception e) {
            this.game.getLogger().error("addBoardDetail() error: ", e);
        }
    }
    
    protected boolean isAdvantageRatioByGroupID(){
        return ServerConfig.getInstance().isAdvantageRatioByGroupID();
    }
    
}
