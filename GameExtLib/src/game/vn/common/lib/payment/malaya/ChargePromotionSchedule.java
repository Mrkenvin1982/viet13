/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment.malaya;

import java.util.List;

/**
 *
 * @author hanv
 */
public class ChargePromotionSchedule {
    public static final byte TYPE_MBANK = 0;
    public static final byte TYPE_EEZIEPAY = 1;
    public static final byte TYPE_CARD = 2;

    public static final byte REPEAT_CURRENT_DAY = 0;
    public static final byte REPEAT_DAILY = 1;
    public static final byte REPEAT_OTHER = 2;
    
    private int id;
    private byte type;
    private byte repeat;
    private String startDate;
    private String endDate;
    private List<ChargePromotionTime> time;
    private boolean enable;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getRepeat() {
        return repeat;
    }

    public void setRepeat(byte repeat) {
        this.repeat = repeat;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<ChargePromotionTime> getTime() {
        return time;
    }

    public void setTime(List<ChargePromotionTime> time) {
        this.time = time;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

}
