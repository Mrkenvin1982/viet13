/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.game.CreateSFSGameSettings;
import game.vn.common.card.object.Card;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.device.Device;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.contants.PlayMode;
import game.vn.common.lib.contants.UserType;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.common.properties.UserInforPropertiesKey;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author binhnt
 */
public class Utils {
    private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int DECIMAL = 2;
    public static final String UNKNOW ="";
    /**
     * An error code.
     */
    public static final int ERROR_CODE = -1;
    private static ISFSApi api;
    
    public static void init(ISFSApi sfsApi) {
        api = sfsApi;
    }

    public static String formatedString(String s, Object... args) {
        try {
            return String.format(s, args);
        } catch (Exception e) {
            LOGGER.error("formatedString error: " + s, e);
            return s;
        }
    }
    
    /**
     * format number by GlobalsUtil.DEFAULT_LOCALE
     *
     * @param number 10000
     * @return 10.000
     */
    public static String formatNumber(Object number) {
        return NumberFormat.getInstance(GlobalsUtil.DEFAULT_LOCALE).format(number);
    }

    /**
     * 
     * @param user
     * @return 
     */
    public static Locale getUserLocale(User user) {
        if(user == null){
            return GlobalsUtil.DEFAULT_LOCALE;
        }
        try {
            return (Locale)user.getProperty(UserInforPropertiesKey.LOCALE_USER);
        } catch (Exception e) {
            LOGGER.error("getUserLocale", e);
        }
        return GlobalsUtil.DEFAULT_LOCALE;
    }
    
    /**
     * Lấy ngày hiện tại theo format
     *
     * @param pattern
     * @return
     */
    public static String getCurrentDateString(String pattern) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        // formatting
        return format.format(date);
    }

    /**
     * lấy service id theo tên lobby, dùng khi user vào room lobby
     * @param lobbyName
     * @return 
     */
    public static byte getServiceId(String lobbyName) {
        switch (lobbyName) {
            case ExtensionConstant.BAI_CAO_LOBBY_NAME:
            case ExtensionConstant.BAI_CAO_LOBBY_NAME_REAL:
                return Service.BAI_CAO;
                
            case ExtensionConstant.BLACK_JACK_LOBBY_NAME:
            case ExtensionConstant.BLACK_JACK_LOBBY_NAME_REAL:
                return Service.BLACKJACK;
                
            case ExtensionConstant.LIENG_LOBBY_NAME:
            case ExtensionConstant.LIENG_LOBBY_NAME_REAL:
                return Service.LIENG;
                
            case ExtensionConstant.MAUBINH_LOBBY_NAME:
            case ExtensionConstant.MAUBINH_LOBBY_NAME_REAL:
                return Service.MAUBINH;
                
            case ExtensionConstant.PHOM_LOBBY_NAME:
            case ExtensionConstant.PHOM_LOBBY_NAME_REAL:
                return Service.PHOM;
                
            case ExtensionConstant.SAM_LOBBY_NAME:
            case ExtensionConstant.SAM_LOBBY_NAME_REAL:
                return Service.SAM;
                
            case ExtensionConstant.TLMN_LOBBY_NAME:
            case ExtensionConstant.TLMN_LOBBY_NAME_REAL:
                return Service.TIENLEN;
                
            case ExtensionConstant.TLDL_LOBBY_NAME:
            case ExtensionConstant.TLDL_LOBBY_NAME_REAL:
                return Service.TIEN_LEN_DEM_LA;
                 
            case ExtensionConstant.XITO_LOBBY_NAME:
            case ExtensionConstant.XITO_LOBBY_NAME_REAL:
                return Service.XI_TO;
                
            case ExtensionConstant.TL_TOUR_LOBBY_NAME_REAL:
                return Service.TIEN_LEN_TOUR;
                
            case ExtensionConstant.TLDL_SOLO_LOBBY_NAME:
            case ExtensionConstant.TLDL_SOLO_LOBBY_NAME_REAL:
                return Service.TLDL_SOLO;
        }
        return Service.SYSTEM;
    }
    
    /**
     * get service id theo group id của game
     * @param groupId
     * @return 
     */
    public static byte getServiceIdBoardGame(String groupId){
        switch(groupId){
            case ExtensionConstant.BAICAO_GROUP_NAME:
                return Service.BAI_CAO;
            case ExtensionConstant.BLACKJACK_GROUP_NAME:
                return Service.BLACKJACK;
            case ExtensionConstant.LIENG_GROUP_NAME:
                return Service.LIENG;
            case ExtensionConstant.MAUBINH_GROUP_NAME:
                return Service.MAUBINH;
            case ExtensionConstant.PHOM_GROUP_NAME:
                return Service.PHOM;
            case ExtensionConstant.SAM_GROUP_NAME:
                return Service.SAM;
            case ExtensionConstant.TLMN_GROUP_NAME:
                return Service.TIENLEN;
            case ExtensionConstant.TLDL_GROUP_NAME:
                return Service.TIEN_LEN_DEM_LA;
             case ExtensionConstant.XITO_GROUP_NAME:
                return Service.XI_TO;
            case ExtensionConstant.TL_TOUR_GROUP_NAME:
                return Service.TIEN_LEN_TOUR;
            case ExtensionConstant.TLDL_SL_GROUP_NAME:
                return Service.TLDL_SOLO;
        }
        return Service.SYSTEM;
    }

    /**
     * 
     * @param userId
     * @return 
     */
    public static User findUser(String userId) {
        return api.getUserByName(userId);
    }

    /**
     * Chuyển đổi list card sang array idCards
     * @param cardset
     * @return 
     */
    public static List<Short> convertListCardIds2Array(List<Card> cardset) {
        List<Short> cards = new ArrayList<>();
        for (int i = 0; i < cardset.size(); i++) {
            cards.add((short)cardset.get(i).getId());
        }
        return cards;
    }
    
     /**
     *  lay group Name theo ten lobby
     * @param serviceId
     * @return 
     */
    public static String getGroupIdFromLobby(int serviceId) {
        switch (serviceId) {
            case Service.BAI_CAO:
                return ExtensionConstant.BAICAO_GROUP_NAME; 
            case Service.BLACKJACK:
                return ExtensionConstant.BLACKJACK_GROUP_NAME;
            case Service.LIENG:
                return ExtensionConstant.LIENG_GROUP_NAME; 
            case Service.MAUBINH:
                return ExtensionConstant.MAUBINH_GROUP_NAME; 
            case Service.PHOM:
                return ExtensionConstant.PHOM_GROUP_NAME;
            case Service.SAM:
                return ExtensionConstant.SAM_GROUP_NAME;
            case Service.TIENLEN:
                return ExtensionConstant.TLMN_GROUP_NAME;
            case Service.TIEN_LEN_DEM_LA:
                return ExtensionConstant.TLDL_GROUP_NAME;
            case Service.XI_TO:
                return ExtensionConstant.XITO_GROUP_NAME;
            case Service.TIEN_LEN_TOUR:
                return ExtensionConstant.TL_TOUR_GROUP_NAME;
            case Service.TLDL_SOLO:
                return ExtensionConstant.TLDL_SL_GROUP_NAME;
        }
        return null;
    }
    
    /**
     * lay ext ID theo ServiceId
     *
     * @param serviceId
     * @return
     */
    public static String getExtIDFromServiceId(int serviceId) {
        switch (serviceId) {
            case Service.BAI_CAO:
                return ExtensionConstant.BAICAO_EXT_ID;
            case Service.BLACKJACK:
                return ExtensionConstant.BLACKJACK_EXT_ID;
            case Service.LIENG:
                return ExtensionConstant.LIENG_EXT_ID;
            case Service.MAUBINH:
                return ExtensionConstant.MAUBINH_EXT_ID;
            case Service.PHOM:
                return ExtensionConstant.PHOM_EXT_ID;
            case Service.SAM:
                return ExtensionConstant.SAM_EXT_ID;
            case Service.TIENLEN:
                return ExtensionConstant.TLMN_EXT_ID; 
            case Service.XI_TO:
                return ExtensionConstant.XITO_EXT_ID;
            case Service.TIEN_LEN_DEM_LA:
                return ExtensionConstant.TLDL_EXT_ID; 
            case Service.TIEN_LEN_TOUR:
                return ExtensionConstant.TL_TOUR_EXT_ID; 
            case Service.TLDL_SOLO:
                return ExtensionConstant.TLDL_SL_EXT_ID; 
        }
        return null;
    }
    
     /**
     * lay ext class theo ServiceId
     *
     * @param serviceId
     * @return
     */
    public static String getExtClassFromServiceId(int serviceId) {
        switch (serviceId) {
            case Service.BAI_CAO:
                return ExtensionConstant.BAICAO_EXT_CLASS;
            case Service.BLACKJACK:
                return ExtensionConstant.BLACKJACK_EXT_CLASS;
            case Service.LIENG:
                return ExtensionConstant.LIENG_EXT_CLASS;
            case Service.MAUBINH:
                return ExtensionConstant.MAUBINH_EXT_CLASS;
            case Service.PHOM:
                return ExtensionConstant.PHOM_EXT_CLASS;
            case Service.SAM:
                return ExtensionConstant.SAM_EXT_CLASS;
            case Service.TIENLEN:
                return ExtensionConstant.TLMN_EXT_CLASS;
            case Service.TIEN_LEN_DEM_LA:
                return ExtensionConstant.TLDL_EXT_CLASS;
            case Service.XI_TO:
                return ExtensionConstant.XITO_EXT_CLASS;
            case Service.TIEN_LEN_TOUR:
                return ExtensionConstant.TL_TOUR_EXT_CLASS;
            case Service.TLDL_SOLO:
                return ExtensionConstant.TLDL_SL_EXT_CLASS;
        }
        return null;
    }
    
    public static String getLobbyName(int serviceId,int moneyType){
         if(moneyType==MoneyContants.MONEY){
            return getNameLobbyMoneyByService(serviceId);
        }
        
        return getNameLobbyPointByService(serviceId);
    }
    
    private static String getNameLobbyPointByService(int serviceId){
        switch (serviceId) {
            case Service.BAI_CAO:
                return ExtensionConstant.BAI_CAO_LOBBY_NAME;
            case Service.BLACKJACK:
                return ExtensionConstant.BLACK_JACK_LOBBY_NAME;
            case Service.LIENG:
                return ExtensionConstant.LIENG_LOBBY_NAME;
            case Service.MAUBINH:
                return ExtensionConstant.MAUBINH_LOBBY_NAME;
            case Service.PHOM:
                return ExtensionConstant.PHOM_LOBBY_NAME;
            case Service.SAM:
                return ExtensionConstant.SAM_LOBBY_NAME;
            case Service.TIENLEN:
                return ExtensionConstant.TLMN_LOBBY_NAME;
            case Service.TIEN_LEN_DEM_LA:
                return ExtensionConstant.TLDL_LOBBY_NAME;
            case Service.XI_TO:
                return ExtensionConstant.XITO_LOBBY_NAME;
            case Service.TLDL_SOLO:
                return ExtensionConstant.TLDL_SOLO_LOBBY_NAME;
        }
        return "";
    }
    
    private static String getNameLobbyMoneyByService(int serviceId){
        switch (serviceId) {
            case Service.BAI_CAO:
                return ExtensionConstant.BAI_CAO_LOBBY_NAME_REAL;
            case Service.BLACKJACK:
                return ExtensionConstant.BLACK_JACK_LOBBY_NAME_REAL;
            case Service.LIENG:
                return ExtensionConstant.LIENG_LOBBY_NAME_REAL;
            case Service.MAUBINH:
                return ExtensionConstant.MAUBINH_LOBBY_NAME_REAL;
            case Service.PHOM:
                return ExtensionConstant.PHOM_LOBBY_NAME_REAL;
            case Service.SAM:
                return ExtensionConstant.SAM_LOBBY_NAME_REAL;
            case Service.TIENLEN:
                return ExtensionConstant.TLMN_LOBBY_NAME_REAL;
            case Service.TIEN_LEN_DEM_LA:
                return ExtensionConstant.TLDL_LOBBY_NAME_REAL;
            case Service.XI_TO:
                return ExtensionConstant.XITO_LOBBY_NAME_REAL;
            case Service.TIEN_LEN_TOUR:
                return ExtensionConstant.TL_TOUR_LOBBY_NAME_REAL;
            case Service.TLDL_SOLO:
                return ExtensionConstant.TLDL_SOLO_LOBBY_NAME_REAL;
        }
        return "";
    }
    
     /**
     * Tạo phòng cố định cho từng game
     * @param bet
     * @param serviceId
     * @param zone
     * @param moneyType
     * @param mode
     * @param isTournament
     * @return 
     */
    public static Room createBoardGame(double bet,int serviceId, Zone zone, int moneyType, byte mode, boolean isTournament){
        Room room = null;
        try {
            CreateSFSGameSettings setting = new CreateSFSGameSettings();
            setting.setGroupId(Utils.getGroupIdFromLobby(serviceId));
            setting.setGame(true);
            setting.setName(NameUtil.GetBoardNameLength10(String.valueOf(ServerConfig.getInstance().getServerId())));
            setting.setAutoRemoveMode(SFSRoomRemoveMode.NEVER_REMOVE);
            setting.setDynamic(true);
            setting.setMaxVariablesAllowed(20); // lỗi sfs ko thể lấy từ tool admin nên phải set tay

            List<RoomVariable> vers= new ArrayList<>();
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.BET_BOARD, bet, true, false, false));
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.MONEY_TYPE, moneyType, true, false, false));
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.IS_TOURNAMENT, isTournament, true, false, false));
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.MODE, mode, true, false, false));
            vers.add(new SFSRoomVariable(RoomInforPropertiesKey.SERVICE_ID, serviceId, true, false, false));

            setting.setRoomVariables(vers);
            CreateRoomSettings.RoomExtensionSettings roomExtSetting = new CreateRoomSettings.RoomExtensionSettings(Utils.getExtIDFromServiceId(serviceId),Utils.getExtClassFromServiceId(serviceId));
            setting.setExtension(roomExtSetting);
            room= zone.createRoom(setting);
            
            //add thông tin room infor đến hazelcast
            Board board = new Board();
            board.setBetMoney(bet);
            board.setFreeSeat(room.getMaxUsers());
            board.setIp(ServerConfig.getInstance().getIP());
            board.setIpWS(ServerConfig.getInstance().getIPWS());
            board.setIsPlaying(false);
            board.setName(room.getName());
            board.setPort(SFSConfig.getPort());
            board.setPortWS(SFSConfig.getWsPort());
            board.setServiceId((byte) serviceId);
            board.setZone(SFSConfig.getZoneName());
            board.setMoneyType(moneyType);
            board.setMode(mode);
            board.setServerId((byte)ServerConfig.getInstance().getServerId());

            HazelcastUtil.addBoardWaitingInfor(board);

        } catch (Exception e) {
              LOGGER.error("createBoardGame", e);
        }
        return room;
    }
    
    /**
     * Sử dung de compare 2 doi tuong user
     * @param user1
     * @param user2
     * @return 
     */
    public static boolean isEqual(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return false;
        }
        try {
            String idDBUser1 = Utils.getIdDBOfUser(user1);
            String idDBUser2 = Utils.getIdDBOfUser(user2);
            return idDBUser1.equals(idDBUser2);
        } catch (Exception e) {
            LOGGER.error("isEqual() error: ", e);
        }
        return false;
    }
    
    /**
     * Get id database của user của user
     * @param user
     * @return 
     */
    public static String getIdDBOfUser(User user) {
        if (user == null) {
            return UNKNOW;
        }
        
        return user.getProperty(UserInforPropertiesKey.ID_DB_USER).toString();
    }
    
    /**
     * Update id database của user
     * @param u 
     * @param idDBUser 
     */
    public static void updateIdDBOfUser(User u, String idDBUser) {
        if(u==null){
            return;
        }
        try {
            u.setProperty(UserInforPropertiesKey.ID_DB_USER, idDBUser);
            UserVariable ver = new SFSUserVariable(UserInforPropertiesKey.ID_DB_USER, idDBUser);
            u.setVariable(ver);
        } catch (Exception e) {
            LOGGER.error("updateIdDBOfUser() error:",e);
        }
    }
    
    /**
     * convert tu string to double
     * @param listValue
     * @return 
     */
    public static List<Double> convertToListDouble(String listValue) {
        List<Double> bets = new ArrayList<>();
        try {
            String[] betArray = listValue.split(",");
            for (String value : betArray) {
                bets.add(Double.valueOf(value));
            }
        } catch (Exception e) {
             LOGGER.error("Utils.convertToListDouble() erro:", e);
        }
        return  bets;
    }
    
    /**
     * Lấy ra loại tiền của user dang chọn
     * @param user
     * @return 
     */
    public static byte getMoneyTypeOfUser(User user) {
        if(user==null){
            return MoneyContants.POINT;
        }
        byte type = MoneyContants.POINT;
        try {
            int value=user.getVariable(UserInforPropertiesKey.MONEY_TYPE).getIntValue();
            type = (byte)value;
        } catch (Exception e) {
            LOGGER.error("Utils.getMoneyTypeOfUser() erro:", e);
        }
        return type;
    }

    /**
     * 
     * @param user
     * @param serviceId
     * @return 
     */
    public static byte getUserPlayMode(User user, int serviceId) {
        try {
            ISFSArray ids = user.getVariable(UserInforPropertiesKey.SHUFFLE_GAMES).getSFSArrayValue();
            if (ids.contains(serviceId)) {
                return PlayMode.SHUFFLE;
            }
        } catch (Exception e) {
            LOGGER.error("getUserPlayMode", e);
        }
        return PlayMode.NORMAL;
    }

    /**
     * Lấy tiền trong bàn của user
     * @param user
     * @return 
     */
    public static double getMoneyStackFromUser(User user) {
        if (user == null) {
            return 0;
        }
        double moneyOfUser = 0;
        try {
            moneyOfUser = (double)user.getProperty(UserInforPropertiesKey.MONEY_STACK);
        } catch (Exception e) {
            LOGGER.error("Utils.getMoneyFromUser() erro:", e);
        }
        return moneyOfUser;
    }
    
    /**
     * Làm tròn xuống 2 chữ số thập phân
     * 10.019 =10.01; 10.011=10.01
     * @param value
     * @return 
     */
    private static double roundDown2Decimal(double value){
        //chuyển sang kiểu long x100 để làm tròn xuống
        long valueRound=(long)(value*100);
        //tính lại kiểu double
        return (double)valueRound/100;
    }
    
    public static double getMoneyOfUser(User user) {
        if (user == null) {
            return 0;
        }
        return  (double) user.getProperty(UserInforPropertiesKey.MONEY_USER);
        }

    /**
     * Lấy ra điểm của user
     * @param user
     * @return 
     */
    public static double getPointOfUser(User user) {
        if (user == null) {
            return 0;
        }
        return  (double) user.getProperty(UserInforPropertiesKey.POINT_USER);
        }

    /**
     * loại tiền tệ trong game
     * @param moneyType
     * @param lo
     * @return 
     */
    public static String getCurrency(int moneyType, Locale lo) {
        if (moneyType == MoneyContants.MONEY) {
            return GameLanguage.getMessage(GameLanguage.TYPE_MONEY, lo);
        }
        return GameLanguage.getMessage(GameLanguage.TYPE_POINT, lo);
    }
    
     /**
     * Tên tiền tệ trong game
     * @param moneyType
     * @param lo
     * @return 
     */
    public static String getCurrencyName(int moneyType, Locale lo) {
        if (moneyType == MoneyContants.MONEY) {
            return GameLanguage.getMessage(GameLanguage.NAME_MONEY, lo);
        }
        return GameLanguage.getMessage(GameLanguage.NAME_POINT, lo);
    }

    public static Date getDateFromString(String date, String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date dateTobonus = new Date();
        try {
            dateTobonus = df.parse(date);
        } catch (ParseException ex) {
            LOGGER.error("getDateFromString", ex);
        }
        return dateTobonus;
    }
    /**
     * Update tiền thật của user
     * @param u
     * @param moneyOfUser 
     */
    public static void updateMoneyOfUser(User u, double moneyOfUser) {
        if(u==null){
            return;
        }
        try {
            u.setProperty(UserInforPropertiesKey.MONEY_USER, moneyOfUser);
            UserVariable var = new SFSUserVariable(UserInforPropertiesKey.MONEY_USER, moneyOfUser);
            api.setUserVariables(u, Arrays.asList(var));
        } catch (Exception e) {
            LOGGER.error("updateMoneyOfUser() error:",e);
        }
    }
    
    /**
     * update point của user
     * @param u
     * @param pointOfUser
     */
     public static void updatePointOfUser(User u, double pointOfUser) {
        if(u==null){
            return;
        }
        try {
            u.setProperty(UserInforPropertiesKey.POINT_USER, pointOfUser);
            UserVariable var = new SFSUserVariable(UserInforPropertiesKey.POINT_USER, pointOfUser);
            api.setUserVariables(u, Arrays.asList(var));
        } catch (Exception e) {
            LOGGER.error("updatePointOfUser() error:",e);
        }
    }
     
    public static String md5String(String sourceString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(sourceString.getBytes());
        byte[] encryptDatas = md.digest();
        String returnData = new BigInteger(1, encryptDatas).toString(16);
        while (returnData.length() < 32) {
            returnData = "0" + returnData;
        }
        return returnData;
    }
    
    public static String convertListDoubleToString(List<Double> listValue) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listValue.size(); i++) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(listValue.get(i));
        }
        return sb.toString();
    }

    /**
     * Lấy version của user
     * @param user
     * @return 
     */
    public static Device getDevice(User user){
         try {
           return (Device)user.getProperty(UserInforPropertiesKey.DEVICE);
         }catch (Exception e) {
             LOGGER.error("Utils.getDevice() ", e);
         }
         return new Device();
    }
    
     /**
     * Lấy thông tin point va money
     * @param stack
     * @param moneyType
     * @return 
     */
    public static String getStringStack(double stack, int moneyType){
        if(moneyType == MoneyContants.MONEY){
            return FormatUtil.formatNumber(stack);
        }
        
        return FormatUtil.formatNumber((long)stack);
    }
    
    private static double getRound(double value){
        long valueRound=(long)(value);
        //tính lại kiểu double
        return (double)valueRound;
    }
    
    public static BigDecimal getRoundBigDecimal(BigDecimal value){
        return value.setScale(DECIMAL, RoundingMode.HALF_UP);
    }
    
    /**
     * Các method add(), subtract(), divide(), multiply()
     * sử dụng cho các giá trị double vì sẽ bị lỗi thất thoát giá trị khi thuc hien cac phep tinh
     * (Retain precision with double in Java):
     * vd:  double total= 5.6 +5.8 = 11.399999999999
     * @param value1
     * @param value2
     * @return 
     */
    public static double add(double value1, double value2) {
        long money1 = Math.round(value1 * 100);
        long money2 = Math.round(value2 * 100);

        double result = (double) (money1 + money2) / 100;
        return result;
    }
    
    public static double subtract(double value1, double value2) {
        long money1 = Math.round(value1 * 100);
        long money2 = Math.round(value2 * 100);

        double result = (double) (money1 - money2) / 100;
        return result;
    }
    
    public static double divide(double value1, double value2) {
        double result = value1 / value2;
        result = roundDown2Decimal(result);
        return result;
    }
    
    public static double multiply(double value1, double value2) {
        double result = value1 * value2;
        result = roundDown2Decimal(result);
        return result;
    }
    
     public static BigDecimal multiply(BigDecimal value1, BigDecimal value2) {
        BigDecimal result = value1.multiply(value2);
        result = getRoundBigDecimal(result);
        return result;
    }
    
    public static BigDecimal divide(BigDecimal value1, BigDecimal value2) {
        BigDecimal result = value1.divide(value2, DECIMAL, RoundingMode.HALF_UP);
        return result;
    }
    
    public static BigDecimal subtract(BigDecimal value1, BigDecimal value2) {
        BigDecimal result = value1.subtract(value2);
        result = getRoundBigDecimal(result);
        return result;
    }
    
    public static BigDecimal add(BigDecimal value1, BigDecimal value2) {
        BigDecimal result = value1.add(value2);
        result = getRoundBigDecimal(result);
        return result;
    }

    public static boolean onShuffle(User user) {
        Object obj = user.getProperty(UserInforPropertiesKey.ON_SHUFFLE);
        if (obj != null) {
            return (boolean)obj;
        }
        return false;
    }

    public static int getUserType(User user) {
        if (user.containsProperty(UserInforPropertiesKey.USER_TYPE)) {
            return (int)user.getProperty(UserInforPropertiesKey.USER_TYPE);
        }
        return UserType.NORMAL;
    }

     /**
     * Convert a string to integer number.
     *
     * @param stringNumber String number.
     * @return -1 if unsuccess otherwise return an integer number.
     */
    public static int convertToInteger(String stringNumber) {
        if (stringNumber == null || stringNumber.compareTo(UNKNOW) == 0) {
            return ERROR_CODE;
        }

        try {
            return Integer.parseInt(stringNumber);
        } catch (NumberFormatException ex) {
            LOGGER.error("Convert to integer got exception :", ex);
        }

        return ERROR_CODE;
    }
    
    public static boolean isBot(User u) {
        if (u == null) {
            return false;
        }
        return Utils.getUserType(u) > UserType.NORMAL;
    }
    
    public static long getTimeCurrentToZeroHour(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() - System.currentTimeMillis(); 
    }

    public static void updateUserType(User user, int userType) {
        if (user == null) {
            return;
        }

        try {
            user.setProperty(UserInforPropertiesKey.USER_TYPE, userType);
            UserVariable var = new SFSUserVariable(UserInforPropertiesKey.USER_TYPE, userType);
            api.setUserVariables(user, Arrays.asList(var));
        } catch (Exception e) {
            LOGGER.error("updateUserType",e);
        }
    }

}
