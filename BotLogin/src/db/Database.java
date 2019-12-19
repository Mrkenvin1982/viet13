/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import constant.Constant;
import domain.BotConfig;
import domain.BotSchedule;
import domain.BotScheduleTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DateUtil;

/**
 *
 * @author hanv
 */
public class Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    public static final Database INSTANCE = new Database();
    
    public BotConfig getBotConfig(int serviceId, byte moneyType) {
        BotConfig botConfig = new BotConfig();
        try (Connection conn = DBPoolManager.getInstance().getApiConnection()) {
            String field = moneyType == Constant.MONEY ? "CONFIG_VALUE" : "CONFIG_VALUE_POINT";
            String sql = String.format("SELECT `CONFIG_KEY`, `%s` FROM `SERVER_CONFIG_BOT` WHERE SERVICE_ID = ?", field);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, serviceId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        switch (rs.getString("CONFIG_KEY")) {
                            case "MIN_BUY_IN":
                                botConfig.setMinBuyIn(rs.getInt(field));
                                break;
                            case "MAX_BUY_IN":
                                botConfig.setMaxBuyIn(rs.getInt(field));
                                break;
                            case "TIME_JOIN_BOARD":
                                botConfig.setTimeJoinBoard(rs.getInt(field));
                                break;
                            case "TIME_CHANGE_BOARD_FROM":
                                botConfig.setTimeChangeBoardFrom(rs.getInt(field));
                                break;
                            case "TIME_CHANGE_BOARD_TO":
                                botConfig.setTimeChangeBoardTo(rs.getInt(field));
                                break;
                            case "BOT_IN_BOARD":
                                botConfig.setBotInBoard(rs.getInt(field));
                                break;
                            case "BOT_EACH_TURN":
                                botConfig.setBotEachTurn(rs.getInt(field));
                                break;
                            case "BOT_EACH_TURN_TO":
                                botConfig.setBotEachTurnTo(rs.getInt(field));
                                break;
                            case "BOT_EACH_TURN_FROM":
                                botConfig.setBotEachTurnFrom(rs.getInt(field));
                                break;
                            case "ENABLE":
                                botConfig.setEnable(rs.getBoolean(field));
                                break;
                            case "LIST_BET_MONEY":
                                List<Double> listBet = new ArrayList<>();
                                String s = rs.getString(field).trim();
                                if (s != null && !s.isEmpty()) {
                                    for (String bet : s.split(",")) {
                                        listBet.add(Double.parseDouble(bet));
                                    }
                                }
                                rs.getString(field);

                                botConfig.setListBet(listBet);
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getBotConfig", e);
        }

        return botConfig;
    }
    
    public BotSchedule getBotSchedule(String userId) {
        try (Connection conn = DBPoolManager.getInstance().getApiConnection()) {
            String sql = "SELECT * FROM bot_schedule WHERE user_id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BotSchedule schedule = new BotSchedule();
                        schedule.setUserId(rs.getString("user_id"));
                        schedule.setRepeat(rs.getByte("repeat"));

                        List<BotScheduleTime> scheduleTimes = new ArrayList<>();
                        for (String time : rs.getString("time").split(";")) {
                            BotScheduleTime scheduleTime = new BotScheduleTime();
                            scheduleTime.setStartTime(time.split("-")[0]);
                            scheduleTime.setEndTime(time.split("-")[1]);
                            scheduleTimes.add(scheduleTime);
                        }

                        schedule.setTime(scheduleTimes);
                        if (rs.getDate("start_date") != null) {
                            schedule.setStartDate(DateUtil.getDateString(rs.getDate("start_date"), "yyyy-MM-dd"));
                        }
                        if (rs.getDate("end_date") != null) {
                            schedule.setEndDate(DateUtil.getDateString(rs.getDate("end_date"), "yyyy-MM-dd"));
                        }
                        schedule.setWeekDays(rs.getString("week_days"));
                        return schedule;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getBotSchedule", e);
        }
        return null;
    }
    
}
