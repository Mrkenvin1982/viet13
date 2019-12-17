/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.service;

import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.SFSConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lib.contants.PlayMode;
import game.vn.common.object.UserShuffle;
import game.vn.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hanv
 */
public class ShuffleService implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ShuffleService.class);

    private static final ConcurrentHashMap<String, ArrayList> SHUFFLE_USERS = new ConcurrentHashMap<>();

    private final ISFSApi sfsApi;
    
    public ShuffleService(ISFSApi sfsApi) {
        this.sfsApi = sfsApi;
    }
    
    public static synchronized void addShuffleWaitingUser(String userId, byte serviceId, double betMoney, String lastBoardName) {
        synchronized (SHUFFLE_USERS) {
            ArrayList users = SHUFFLE_USERS.get(serviceId + "_" + betMoney);
            if (users == null) {
                users = new ArrayList();
            }
            users.add(new UserShuffle(userId, lastBoardName));
            SHUFFLE_USERS.put(serviceId + "_" + betMoney, users);
        }
    }

    @Override
    public void run() {
        try {
            synchronized (SHUFFLE_USERS) {
                if (SHUFFLE_USERS.isEmpty()) {
                    return;
                }

                LOGGER.info("************* shuffle: " + SHUFFLE_USERS.size());

                for (Map.Entry<String, ArrayList> entry : SHUFFLE_USERS.entrySet()) {
                    String[] key = entry.getKey().split("_");
                    byte serviceId = Byte.parseByte(key[0]);
                    double betMoney = Double.parseDouble(key[1]);
                    String lobby = Utils.getLobbyName(serviceId, MoneyContants.MONEY);
                    int maxRoomUser = RoomConfig.getInstance().getNoPlayer(lobby);
                    ArrayList<UserShuffle> users = entry.getValue();
                    Collections.shuffle(users);

                    LOGGER.debug(serviceId + "_" + betMoney + ": " + users.size());

                    for (int i=0; i<users.size(); i+=maxRoomUser) {
                        Room room = Utils.createBoardGame(betMoney, serviceId, SFSConfig.getZone(), MoneyContants.MONEY, PlayMode.SHUFFLE, false);
                        for (int j = i; j < i + maxRoomUser && j < users.size(); j++) {
                            UserShuffle userShuffle = users.get(j);
                            User user = sfsApi.getUserByName(userShuffle.getUserId());
                            if (user != null) {
                                sfsApi.joinRoom(user, room);
                            }
                        }
                    }
                }

                SHUFFLE_USERS.clear();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

}