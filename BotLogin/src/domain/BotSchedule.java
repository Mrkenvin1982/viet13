/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.util.List;

/**
 *
 * @author hanv
 */
public class BotSchedule {
    public static final byte REPEAT_CURRENT_DAY = 0;
    public static final byte REPEAT_DAILY = 1;
    public static final byte REPEAT_CUSTOM = 2;

    private String userId;
    private byte repeat;
    private List<BotScheduleTime> time;
    private String startDate;
    private String endDate;
    private String weekDays;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public byte getRepeat() {
        return repeat;
    }

    public void setRepeat(byte repeat) {
        this.repeat = repeat;
    }

    public List<BotScheduleTime> getTime() {
        return time;
    }

    public void setTime(List<BotScheduleTime> time) {
        this.time = time;
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

    public String getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(String weekDays) {
        this.weekDays = weekDays;
    }
}
