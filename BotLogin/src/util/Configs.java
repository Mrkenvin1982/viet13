/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import util.watchservice.PropertyConfigurator;

/**
 *
 * @author hanv
 */
public class Configs extends PropertyConfigurator {

    private static final Configs INSTANCE = new Configs("conf/", "config.properties");
    private boolean isLogEnable;

    public Configs(String path, String nameFile) {
        super(path, nameFile);
    }

    public static Configs getInstance() {
        return INSTANCE;
    }
    
    public String queueHost() {
        return getStringAttribute("queue.host");
    }

    public int queuePort() {
        return getIntAttribute("queue.port");
    }

    public String queueUsername() {
        return getStringAttribute("queue.username");
    }

    public String queuePassword() {
        return getStringAttribute("queue.password");
    }

    public int queuePoolSize() {
        return getIntAttribute("queue.poolSize", 3);
    }
    
    public String queueSuffix() {
        return getStringAttribute("queue.suffix", "");
    }

    public boolean queueEnable() {
        return getBooleanAttribute("queue.enable");
    }

    public String getListBot() {
        return getStringAttribute("bots");
    }

    public List<String> getListName() {
        try {
            return Files.readAllLines(Paths.get("conf/name.txt"));
        } catch (IOException e) {
        }
        return null;
    }

    public String getVerifyUrl() {
        return getStringAttribute("url.verify", "https://account.devuid.club/");
    }

    public String getLoginHost() {
        return getStringAttribute("login.host", "vietthirteen.xyz");
    }

    public int getLoginPort() {
        return getIntAttribute("login.port", 9933);
    }

    public String getLoginZone() {
        return getStringAttribute("login.zone", "Z88Zone");
    }
    
    public String getGameHost() {
        return getStringAttribute("game.host");
    }

    public boolean isLogEnable() {
        return isLogEnable;
    }

    public void updateLogEnable() {
        isLogEnable = getBooleanAttribute("log.enable");
    }

    public boolean isLoginByToken() {
        return getStringAttribute("login.method", "token").equals("token");
    }

    public int getTaiXiuBetDelayFrom() {
        return getIntAttribute("taixiu.bet.delay.from", 0);
    }

    public int getTaiXiuBetDelayTo() {
        return getIntAttribute("taixiu.bet.delay.to", 15);
    }

    public boolean isStop() {
        return getBooleanAttribute("stop");
    }
}