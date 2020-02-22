/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import game.vn.util.watchservice.PropertyConfigurator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class ServerConfig extends PropertyConfigurator {

    private static final String IP = "server.ip";
    private static final String IP_WS = "server.ip.ws";
    private static final String SERVER_ID = "server.id";
    private static final String GAME_POOL_SIZE = "server.gamePollSize";
    private static final String MAX_WAITING_BOARD = "server.maxWaitingBoard";
    private static final String CONNECTION_ID = "server.connectionId";
    private static final String MERCHANT_ID = "server.merchant.id";

    private static final String IS_CLOSE_REAL_MONEY = "server.isCloseRealMoney";
    private static final String CLIENT_LITES = "game.clientLites";
    private static final String ENABLE_VIP = "game.enableVip";
    private static final String ENABLE_RANKING = "game.enableRanking";
    private static final String IS_OPEN_BOT = "game.isOpenBot";
    private static final String IS_SEND_HIST_TTKT = "game.isSendHistToTTKT";
    private static final String IS_SEND_HIST_GAME = "game.isSendHistToGame";
    private static final String IS_ADVANTAGE_RATIO_BY_GROUP_ID= "game.isAdvantageRatioByGroupID";

    private List<String> clientLites;
    private List<Integer> shuffleGames;

    private final static ServerConfig INSTANCE = new ServerConfig("conf/", "server.properties");

    public static ServerConfig getInstance() {
        return INSTANCE;
    }

    public ServerConfig(String path, String nameFile) {
        super(path, nameFile);
        clientLites = null;
    }

    public String getIP() {
        return this.getStringAttribute(IP, "localhost");
    }

    public String getIPWS() {
        return this.getStringAttribute(IP_WS, "localhost");
    }

    public int getServerId() {
        return this.getIntAttribute(SERVER_ID, 0);
    }

    /**
     * số thread tối đa trong game 1 thread tương ứng 1 board
     *
     * @return
     */
    public int getGameControllerPollSize() {
        return getIntAttribute(GAME_POOL_SIZE, 1000);
    }

    /**
     * số thread chạy event
     *
     * @return
     */
    public int getEventPoolSize() {
        return getIntAttribute("EVENT_POOL_SIZE", 300);
    }

    /**
     * thời gian idle của thread, đơn vị giây
     *
     * @return
     */
    public int getAliveTimeThread() {
        return getIntAttribute("ALIVE_TIME_THREAD", 300);
    }

    /**
     * Số bàn user không chơi ngồi giữ chổ quá số bàn này sẽ chuyen thành viewer
     *
     * @return
     */
    public int getMaxWaitingBoard() {
        return this.getIntAttribute(MAX_WAITING_BOARD, 2);
    }

    /**
     * link nạp tiền thật gửi cho client
     *
     * @return
     */
    public String getPaymentUrl() {
        return getStringAttribute("server.payment.url", "");
    }

    /**
     * id game Z88
     *
     * @return
     */
    public String getHistoryConnection() {
        return getStringAttribute("history.connection", "5acb0e45d1b32f565f8b4567");
    }

    public String getHistoryKey() {
        return getStringAttribute("history.key", "2GVWJ72TFVQQ830I7GJ6YHTRJG");
    }

    public String getMerchantAuthenticateKey(String merchantId) {
        return getStringAttribute("auth.key." + merchantId, "");
    }

    public String getRandomAuthenticateKey(String merchantId) {
        return getStringAttribute(merchantId + ".auth.key", "4d77f9785b");
    }

    public String getVn88AuthenticateKey() {
        return getStringAttribute("vn88.auth.key", "e87ea9ef6c");
    }

    public int getConnectionId() {
        return this.getIntAttribute(CONNECTION_ID, 1);
    }

    public String getMerchantId() {
        return getStringAttribute(MERCHANT_ID, "");
    }

    /**
     * lấy thời gian nhật điểm free, mặc định 14400s (4h)
     *
     * @return
     */
    public int getTimeReceivePointFree() {
        return getIntAttribute("point.time.free", 14400);
    }

    /**
     * lấy thời gian nhật điểm video, mặc định 10800s (3h)
     *
     * @return
     */
    public int getTimeReceivePointVideo() {
        return getIntAttribute("point.time.video", 10800);
    }

    /**
     * số điểm free user nhận được sau 4h
     *
     * @return
     */
    public int getPointFree() {
        return getIntAttribute("point.free", 5000);
    }

    /**
     * số lần free user nhận được trong 24 giờ
     *
     * @return
     */
    public int getCountFree() {
        return getIntAttribute("point.count.free", 10);
    }

    /**
     * số điểm nhận được khi xem video
     *
     * @return
     */
    public int getPointVideo() {
        return getIntAttribute("point.video", 2000);
    }

    /**
     * số lần nhận được diểm khi xem video trong 24 giờ
     *
     * @return
     */
    public int getCountVideo() {
        return getIntAttribute("point.count.video", 20);
    }

    /**
     * bật/tắt điểm free
     *
     * @return
     */
    public boolean isPointFreeEnable() {
        return getBooleanAttribute("point.free.enable", true);
    }

    /**
     * bật/tắt điểm video
     *
     * @return
     */
    public boolean isPointVideoEnable() {
        return getBooleanAttribute("point.video.enable", true);
    }

    /**
     * bật/tắt nạp thẻ
     *
     * @return
     */
    public boolean isChargeEnable() {
        return getBooleanAttribute("charge.enable");
    }

    /**
     * số dòng hiện lịch sử
     *
     * @return
     */
    public int historyLimit() {
        return getIntAttribute("history.limit", 7);
    }

    public int apiTimeoutRequest() {
        return getIntAttribute("api.timeout.request", 60000);
    }

    public int apiTimeoutCharge() {
        return getIntAttribute("api.timeout.charge", 60000);
    }

    public int apiTimeoutFacebook() {
        return getIntAttribute("api.timeout.facebook", 60000);
    }

    public int apiTimeoutGetGameListMoon() {
        return getIntAttribute("api.timeout.get.game.list.moon", 60000);
    }

    public int apiTimeoutGetGameListHandicap() {
        return getIntAttribute("api.timeout.get.game.list.handicap", 60000);
    }

    public int apiTimeoutPassportConfig() {
        return getIntAttribute("api.timeout.passport.config", 60000);
    }

    public int apiTimeoutPassportWithdraw() {
        return getIntAttribute("api.timeout.passport.withdraw", 60000);
    }

    public int apiTimeoutPassportBankingWithdraw() {
        return getIntAttribute("api.timeout.passport.banking.withdraw", 60000);
    }

    /**
     * Đóng hoặc mở tiền thât
     *
     * @return
     */
    public boolean isCloseRealMoney() {
        return this.getBooleanAttribute(IS_CLOSE_REAL_MONEY);
    }

    public boolean enableVip() {
        return getBooleanAttribute(ENABLE_VIP);
    }

    public boolean enableRanking() {
        return getBooleanAttribute(ENABLE_RANKING);
    }

    public List<Integer> getListShuffleGame() {
        if (shuffleGames == null) {
            shuffleGames = new ArrayList<>();
            String s = getStringAttribute("game.mode.shuffle.id");
            if (s != null && !s.isEmpty()) {
                for (String id : s.split(",")) {
                    shuffleGames.add(Integer.parseInt(id));
                }
            }
        }
        return shuffleGames;
    }

    /**
     * Lấy ra danh sách game tournamnet
     *
     * @return
     */
    public List<String> getClientLites() {
        if (clientLites == null) {
            clientLites = new ArrayList<>();
            String stringValue = getStringAttribute(CLIENT_LITES, "");
            String[] arrayvalue = stringValue.split(",");
            clientLites.addAll(Arrays.asList(arrayvalue));
        }
        return clientLites;
    }

    /**
     * %phí chuyển tiền
     *
     * @return
     */
    public double getTranserRate() {
        return getDoubleAttribute("transfer.fee.rate", 0.00);
    }

    /**
     * số tiền chuyển tối thiểu
     *
     * @return
     */
    public double getTransferMinMoney() {
        return getDoubleAttribute("transfer.money.min", 0.02);
    }

    public boolean enableTransferMoney() {
        return getBooleanAttribute("transfer.enable", true);
    }

    @Override
    protected void doChanged() {
        super.doChanged();
        clientLites = null;
        shuffleGames = null;
    }

    public int getVerifyAccessTokenSuccessCode() {
        return getIntAttribute("code.verify.success", 15000);
    }

    public int getBoardUserCountTime() {
        return getIntAttribute("time.board.user.count", 0);
    }

    /**
     * Thời gian ping để giữ connect trong con login
     *
     * @return
     */
    public int getPingTime() {
        return getIntAttribute("pingTime", 30);
    }

    public int eventScreen() {
        return getIntAttribute("event.screen", 7);
    }

    public int eventScreenSub() {
        return getIntAttribute("event.screen.sub", 59);
    }

    /**
     * check game co chay bot hay khong
     *
     * @return
     */
    public boolean isPOpenBot() {
        return getBooleanAttribute(IS_OPEN_BOT);
    }

    public int getProviderIdMoon() {
        return getIntAttribute("moon.provider", 1);
    }

    public String getMerchantIdMoon() {
        return getStringAttribute("moon.merchant", "metro");
    }

    public String getCurrencyMoon() {
        return getStringAttribute("moon.currency", "VND");
    }

    public int getProviderIdHandicap() {
        return getIntAttribute("handicap.provider", 2);
    }

    public String getMerchantIdHandicap() {
        return getStringAttribute("handicap.merchant", "metro");
    }

    public String getCurrencyHandicap() {
        return getStringAttribute("handicap.currency", "VND");
    }
    
    /**
     * Gửi thông tin hist đến TTKT
     *
     * @return
     */
    public boolean isSendHistToTTKT() {
        return getBooleanAttribute(IS_SEND_HIST_TTKT, true);
    }

    /**
     * game hiện tại tự ghi log
     *
     * @return
     */
    public boolean isSendHistToGame() {
        return getBooleanAttribute(IS_SEND_HIST_GAME);
    }

    /* lấy số Win tặng cho event tạo tk    *
     * @    return
     */
    public int getBonusWinFree() {
        return getIntAttribute("win.bonus", 66666);
    }
    
    public boolean isAdvantageRatioByGroupID() {
        return getBooleanAttribute(IS_ADVANTAGE_RATIO_BY_GROUP_ID);
    }
    
    public boolean isChatEnable() {
        return getBooleanAttribute("chat.enable", true);
    }

    public int getChatInterval() {
        return getIntAttribute("chat.interval", 10000);
    }
    
    public String getFBAppId() {
        return getStringAttribute("fb.appid", "737217973457819");
    }

    public String getFBAppSecret() {
        return getStringAttribute("fb.appsecret", "d0d0205aa139a14f21d323f2f3fdd372");
    }
}
