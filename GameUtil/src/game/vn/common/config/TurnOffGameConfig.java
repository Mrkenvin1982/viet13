/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.lib.updateconfig.TurnOffGameDetail;
import game.vn.common.lib.updateconfig.TurnOffGameList;
import game.vn.util.Utils;
import game.vn.util.watchservice.PropertyConfigurator;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xử lý turn off game trong class này
 * @author tuanp
 */
public class TurnOffGameConfig extends PropertyConfigurator{
     public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == src.longValue()) {
                return new JsonPrimitive(src.longValue());
            }
            if (src == src.intValue()) {
                return new JsonPrimitive(src.intValue());
            }
            if (src == src.floatValue()) {
                return new JsonPrimitive(src.floatValue());
            }
            return new JsonPrimitive(src);
        }
    }).create();
    
    private final static Logger LOGGER = LoggerFactory.getLogger(TurnOffGameConfig.class);
    private final static TurnOffGameConfig INSTANCE = new TurnOffGameConfig("conf/", "gameConfig.json");
    private TurnOffGameList listConfig;
    private String fileConfig = "conf/gameConfig.json";
    public static String ALL = "all";
    
    public TurnOffGameConfig(String path, String nameFile) {
        super(path, nameFile);
        fileConfig = path + nameFile;
    }
    
    public static TurnOffGameConfig getInstance(){
        return INSTANCE;
    }
    
    /**
     * init dữ liệu review đọc từ file review.json
     */
    public void init() {
        listConfig = GSON.fromJson(getText(), TurnOffGameList.class);
    }
     /**
     * Kiểm tra platform, bundle có đang review version hay không. có check ip
     * nước ngoài không.
     *
     * @param bundle
     * @param version
     * @param platform
     * @param nameGame
     * @param moneyType
     * @return
     */
    public boolean isTurnOffGame(String nameGame, String platform,String version, String bundle, int moneyType) {
        if(listConfig == null){
            return false;
        }
        List<TurnOffGameDetail> listGameDetail;
        if(moneyType==MoneyContants.MONEY){
            listGameDetail = listConfig.getMoneyGameList();
        }else {
            listGameDetail = listConfig.getPointGameList();
        }
        
        if(listGameDetail.isEmpty()){
            return false;
        }
        TurnOffGameDetail reviewDetail = getTurnOffGameDetail(listGameDetail, nameGame,platform,version,bundle);
        if(reviewDetail==null){
            return false;
        }
        return true;
    }
    
    public boolean isTurnOffTaiXiu(String platform, String version, String bundle, int moneyType) {
        if (listConfig == null) {
            return false;
        }
        List<TurnOffGameDetail> listGameDetail;
        if (moneyType == MoneyContants.MONEY) {
            listGameDetail = listConfig.getMoneyGameList();
        } else {
            listGameDetail = listConfig.getPointGameList();
        }
        if (listGameDetail.isEmpty()) {
            return false;
        }
        
        for (TurnOffGameDetail gameDetail : listGameDetail) {
            if (gameDetail.getServiceId() != Service.TAI_XIU) {
                continue;
            }
            if (gameDetail.getPlatform().equals(ALL) || gameDetail.getPlatform().equals(platform)) {
                if (gameDetail.getVersion().equals(ALL) || gameDetail.getVersion().equals(version)) {
                    if (gameDetail.getBundle().equals(ALL) || gameDetail.getBundle().equals(bundle)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Kiem tra có tồn tại trong config
     * @param reviewDetail
     * @param platform
     * @param version
     * @param bundle
     * @return 
     */
    private boolean isExit(TurnOffGameDetail reviewDetail, String platform, String version, String bundle) {
        //chặn tất cả các version
        if (reviewDetail.getPlatform().equals(ALL) || reviewDetail.getPlatform().equals(platform)) {
            if (reviewDetail.getVersion().equals(ALL) || reviewDetail.getVersion().equals(version)) {
                if (reviewDetail.getBundle().equals(ALL) || reviewDetail.getBundle().equals(bundle)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Lấy ra detail game chặn theo version
     * @param platform
     * @param version
     * @param bundle
     * @return 
     */
    private TurnOffGameDetail getTurnOffGameDetail(List<TurnOffGameDetail> listGameDetail , String nameGame, String platform, String version, String bundle) {
        try {
            for (TurnOffGameDetail game : listGameDetail) {
                String nameGameDetail = Utils.getLobbyName(game.getServiceId(), game.getMoneyType());
                if (!nameGameDetail.equals(nameGame)) {
                    continue;
                }
                if (isExit(game, platform, version, bundle)) {
                    return game;
                }
            }
        } catch (Exception e) {
            LOGGER.error("getTurnOffGameDetail error: ", e);
        }

        return null;
    }

    /**
     * cập nhật lại file review
     *
     * @param data
     */
    public void updateFileGameConfig(String data) throws Exception {
        LOGGER.info("data: " + data);
        byte[] mdata = data.getBytes("UTF-8");
        try (FileOutputStream out = new FileOutputStream(fileConfig)) {
            out.write(mdata);
        }
    }

    @Override
    protected void doChanged() {
        super.doChanged(); 
        init();
    }
    public TurnOffGameList getListConfig() {
        return listConfig;
    }
    
    /**
     * Lấy ra thông tin chi tiết theo moneyType
     * @param moneyType
     * @return 
     */
    public List<TurnOffGameDetail> getTurnOffGameDetails(int moneyType){
        if(moneyType == MoneyContants.MONEY){
            return listConfig.getMoneyGameList();
        }
        return listConfig.getPointGameList();
    }
    
}
