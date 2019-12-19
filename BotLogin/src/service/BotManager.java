/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.google.gson.JsonObject;
import constant.Constant;
import db.Database;
import domain.BotConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sfs.SFSBot;
import util.Configs;
import util.GsonUtil;
import util.Utils;

/**
 *
 * @author hanv
 */
public class BotManager {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final BotManager INSTANCE = new BotManager();
    
    public static BotManager getInstance() {
        return INSTANCE;
    }

    private static final List<SFSBot> BOT = new ArrayList<>();

    private String getEmail(String email, int i) {
        String id = String.valueOf(i);
        if (i < 10) {
            id = "00" + id;
        } else if (i < 100) {
            id = "0" + id;
        }
        return String.format(email, id);
    }

    public synchronized void callBot(String roomName, double betMoney, byte moneyType, int serviceId) {
        int type = 0;
        int countBotInRoom = 0;
        switch (serviceId) {
            case Service.BAI_CAO:
                for (SFSBot bot : BOT) {
                    if (bot.getRoomName() != null && roomName.equals(bot.getRoomName())) {
                        countBotInRoom++;
                    }
                }
                if(moneyType == Constant.POINT){
                    type = Constant.POINT_TYPE_BOT_BC_CON;
                }else {
                    type = Constant.USER_TYPE_BOT_BC_CON;
                }
                break;
            case Service.BLACKJACK:
                for (SFSBot bot : BOT) {
                    if (bot.getRoomName() != null && roomName.equals(bot.getRoomName())) {
                        countBotInRoom++;
                    }
                }
                if(moneyType == Constant.POINT){
                    type = Constant.POINT_TYPE_BOT_XD_CON;
                }else {
                    type = Constant.USER_TYPE_BOT_XD_CON;
                }
                break;
            case Service.TIENLEN:
                for (SFSBot bot : BOT) {
                    if (bot.getRoomName() != null && roomName.equals(bot.getRoomName())) {
                        return; // tiến lên chỉ cho 1 bot vào bàn
                    }
                }
                if(moneyType == Constant.POINT){
                    type = Constant.POINT_TYPE_BOT_TL_CON;
                } else {
                    type = Constant.USER_TYPE_BOT_TL_CON;
                }
                break;
            case Service.LIENG:
                break;
            case Service.MAUBINH:
                if(moneyType == Constant.POINT){
                    type = Constant.POINT_TYPE_BOT_MB_CON;
                }else {
                    type = Constant.USER_TYPE_BOT_MB_CON;
                }
                break;
            default:
                return;
        }

        BotConfig config = Database.INSTANCE.getBotConfig(serviceId, moneyType);
        int maxBotCount = config.getBotInBoard();
        int count = 1;
        if (serviceId != Service.TIENLEN && serviceId != Service.MAUBINH) {
            count = Utils.nextInt(maxBotCount) + 1;
            if (count + countBotInRoom > maxBotCount) {
                count = maxBotCount - countBotInRoom;
                if (count == 0) {
                    return;
                }
            }
        }

        int time = config.getTimeJoinBoard() * 1000 / count;
        Collections.shuffle(BOT);
        for (SFSBot bot : BOT) {
            try {
                if (bot.isRunning() && !bot.isInGame() && bot.getUserType() == type && bot.getRoomName() == null && bot.canBuyStack(betMoney)) {
                    LOGGER.info("bot found " + bot.getEmail() + " " + roomName + " " + betMoney);
                    Utils.sleepRandom(time);
                    bot.setRoomName(roomName);
                    bot.buyStack(betMoney);
                    if (--count == 0) {
                        return;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }

    }
    
    public synchronized void recallBot(String roomName, double betMoney, byte moneyType, int serviceId) {
        LOGGER.info("recall bot for room " + roomName);

        int type = 0;
        int countBotInRoom = 0;
        
        switch (serviceId) {
            case Service.BAI_CAO:
                if (moneyType == Constant.POINT) {
                    type = Constant.POINT_TYPE_BOT_BC_CON;
                } else {
                    type = Constant.USER_TYPE_BOT_BC_CON;
                }
                break;
            case Service.BLACKJACK:
                if (moneyType == Constant.POINT) {
                    type = Constant.POINT_TYPE_BOT_XD_CON;
                } else {
                    type = Constant.USER_TYPE_BOT_XD_CON;
                }
                break;
            case Service.TIENLEN:
                type = Constant.USER_TYPE_BOT_TL_CON;
                break;
            case Service.LIENG:
                break;
            case Service.MAUBINH:
                if (moneyType == Constant.POINT) {
                    type = Constant.POINT_TYPE_BOT_MB_CON;
                } else {
                    type = Constant.USER_TYPE_BOT_MB_CON;
                }
                break;
            default:
                return;
        }

        BotConfig config = Database.INSTANCE.getBotConfig(serviceId, moneyType);
        int maxBotCount = config.getBotInBoard();
        int count = 1;
        if (serviceId != Service.TIENLEN && serviceId != Service.MAUBINH) {
            count = Utils.nextInt(maxBotCount) + 1;
            if (count + countBotInRoom > maxBotCount) {
                count = maxBotCount - countBotInRoom;
                if (count == 0) {
                    return;
                }
            }
        }

        int time = 10000 / count;
        Collections.shuffle(BOT);
        for (SFSBot bot : BOT) {
            try {
                if (bot.isRunning() && bot.getUserType() == type && bot.getRoomName() == null && bot.canBuyStack(betMoney)) {
                    LOGGER.info("recall bot " + bot.getEmail() + " " + roomName + " " + betMoney);
                    Utils.sleepRandom(time);
                    bot.setRoomName(roomName);
                    bot.buyStack(betMoney);
                    if (--count == 0) {
                        return;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }

    }

    public void start() throws IOException {
        String s = Configs.getInstance().getListBot();
        JsonObject json = GsonUtil.parse(s).getAsJsonObject();

        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();
        int amount;
        List<String> emails = null;
        if (email.isEmpty()) {
            emails = Files.readAllLines(Paths.get("conf/", "email.txt"));
            amount = emails.size();
        } else {
            amount = json.get("amount").getAsInt();
        }
        
        for (int i = 0; i < amount; i++) {
            String botEmail = emails != null ? emails.get(i) : getEmail(email, i + 1);
            SFSBot bot = new SFSBot(botEmail, password);
            if (bot.start()) {
                BOT.add(bot);
            }
        }
        startMonitor();
    }

    public boolean haveUser(List<String> userIds, byte serviceId) {
        if (BOT == null) {
            return false;
        }

        for (String userId : userIds) {
            boolean isUser = true;
            for (SFSBot bot : BOT) {
                if (bot.getUserId().equals(userId)) {
                    isUser = false;
                    break;
                }
            }
            if (isUser) {
                return true;
            }
        }
        return false;
    }
    
    private void startMonitor() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Configs.getInstance().updateLogEnable();
                    LOGGER.info("****************** MONITOR ******************");
                    monitor();
                    LOGGER.info("*********************************************");

                } catch (Exception e) {
                    LOGGER.info("", e);
                }
            }
        }, 2 * 60 * 1000, 3 * 60 * 1000);
    }

    private void monitor() {
        if (BOT == null || BOT.isEmpty()) {
            return;
        }

        int countInGame = 0;
        int countBotConnected = 0;
        for (SFSBot bot : BOT) {
            if (bot.isInGame()) {
                countInGame++;
            }
            if (bot.isConnected()){
                countBotConnected++;
            }
        }
        
        if (countBotConnected == 0){
            LOGGER.info("all bot disconnect");
            System.exit(0);
        }
        LOGGER.info("monitor game: " + countInGame + " in game, " + (BOT.size() - countInGame) + " free");
    }

}
