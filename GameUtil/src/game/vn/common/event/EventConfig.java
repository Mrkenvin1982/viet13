/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.event;

import game.vn.util.Utils;
import game.vn.util.watchservice.PropertyConfigurator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author
 */
public class EventConfig extends PropertyConfigurator{
    
    private final static EventConfig INSTANCE = new EventConfig("conf/","EventConfig.properties");
    
    public static final String CONDITION_LIST = "condition_list";
    public static final String SERVICE_ID_LIST = "serviceId_list";
    public static final String MONEY_GET_EVENT = "money_get_event";
    public static final String IWIN_GAME_EVENT_ENABLE = "game.event.enable";
    
    // danh sách điều kiện trúng thưởng item
    private List<String> listCondition;
    //danh sách game mở event
    private List<Byte> listServiceId;

    public EventConfig(String path, String nameFile) {
        super(path, nameFile);
        init();
    }
    
    public static EventConfig getInstance() {
        return INSTANCE;
    }
    
    private void init() {
        String strListCondition = getStringAttribute(CONDITION_LIST);
        String[] arrCondition = strListCondition.split(",");
        listCondition = new ArrayList<>(Arrays.asList(arrCondition));

        String strListServiceId = getStringAttribute(SERVICE_ID_LIST);
        String[] arrServiceId = strListServiceId.split(",");
        listServiceId = new ArrayList<>();
        for (int i = 0; i < arrServiceId.length; i++) {
            int serviceId = Utils.convertToInteger(arrServiceId[i]);
            if (serviceId > 0) {
                listServiceId.add((byte) serviceId);
            }
        }
    }

    @Override
    protected void doChanged() {
        super.doChanged();
        init();
    }

    public List<String> getListCondition() {
        return listCondition;
    }

    public void setListCondition(List<String> listCondition) {
        this.listCondition = listCondition;
    }
    
    /**
     * số tiền nhỏ nhất để người chơi có thể nhận được event
     * @return 
     */
    public double getMoneyCanGetEvent() {
        return getDoubleAttribute(MONEY_GET_EVENT, 0);
    }
    /**
     * Tắt mở event chổ này
     * @return 
     */
    public boolean isEnableEvent(){
        return getBooleanAttribute(IWIN_GAME_EVENT_ENABLE);
    }
    
    public boolean checkHaveEventToServiceId(byte serviceId) {
        return listServiceId.contains(serviceId);
    }
}
