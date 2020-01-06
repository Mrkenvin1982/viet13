/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import constant.Constant;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.lib.contants.MoneyContants;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.UUID;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.Service;
import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;
import sfs2x.client.util.PasswordUtil;
import util.HTTPUtil;
import util.Utils;

/**
 *
 * @author hanv
 */
public final class SFSClient implements IEventListener {
    
    private final String email = "nvha81@gmail.com";
    private final String BALANCER_URL = "https://balancer.devcas.club/get-member";
    private static final String VERIFY_URL = "https://account.devuid.club";
    private final long TIME_RECONNECT = 10000;
    private final JsonParser parser = new JsonParser();

    private String token;
    private String clientId;
    private String userId;
    private final String password = "t12345";
    private final String host = "127.0.0.1";
    private final int port = 9933;
    private final String zone = "Z88Zone";
    private String name;
    private JsonArray listServer;
    private int serverId = 0;
    private Room room;
    
    private final Logger log = LoggerFactory.getLogger(SFSClient.class);
    private final Timer timer = new Timer();
    
    private final SmartFox sfs;
    private final ConfigData cfg;
    
    public SFSClient() {
        cfg = new ConfigData();
        sfs = new SmartFox();
        sfs.addEventListener(SFSEvent.CONNECTION, this);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this);
        sfs.addEventListener(SFSEvent.LOGIN, this);
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, this);
        sfs.addEventListener(SFSEvent.LOGOUT, this);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, new IEventListener() {
            @Override
            public void dispatch(BaseEvent e) throws SFSException {
                String cmd = e.getArguments().get("cmd").toString();
                SFSObject sfsObj = (SFSObject) e.getArguments().get("params");
                log.info("cmd: " + cmd);
                log.info(sfsObj.getDump());
                int action = sfsObj.containsKey(SFSKey.ACTION_INCORE) ? sfsObj.getInt(SFSKey.ACTION_INCORE) : sfsObj.getInt(SFSKey.ACTION_INGAME);
                switch (action) {
                    case SFSAction.JOIN_ZONE_SUCCESS:
                        requestInfoAllGame();
                        break;
                    case SFSAction.PLAY_TAIXIU:
                        break;
                    case SFSAction.REQUEST_INFOR_ALL_GAME:
                        joinLobby();
                        break;
                    case SFSAction.LOBBY_LIST_COUNTER:
                        buyStack(1000.0);
                        break;
                }
            }
        });

        start();
    }

    @Override
    public void dispatch(BaseEvent evt) throws SFSException {
        switch (evt.getType()) {
            case SFSEvent.CONNECTION:
                boolean success = (Boolean) evt.getArguments().get("success");
                if (success) {
                    login();
                } else {
                    log.info("connect fail");
                    sendMail(String.format("fail connect to server %s", name));
                    reconnect();
                }
                break;

            case SFSEvent.CONNECTION_LOST:
                log.info("Connection was closed");
                break;

            case SFSEvent.LOGIN:
                log.info("Login success");
                break;
                
            case SFSEvent.LOGIN_ERROR:
                String error = evt.getArguments().get("errorMessage").toString();
                log.info("Login error:  " + error);
                sendMail(String.format("fail login server %s %s", name, error));
                sfs.disconnect();
                break;

            case SFSEvent.LOGOUT:
                sfs.disconnect();
                break;
                
            case SFSEvent.ROOM_JOIN:
                room = (Room) evt.getArguments().get("room");
                log.info(email + " join room: " + room.getName());
                getListBetMoney();
                break;
        }
    }

    private void connect() {
        cfg.setHost(host);
        cfg.setPort(port);
        cfg.setZone(zone);
        sfs.connect(cfg);
    }
    
    private void reconnect() {
        serverId++;
        if (serverId < listServer.size()) {
            connect();
        } else {
            log.info("refresh list server");
            serverId = 0;
            listServer = getListServer();
            if (listServer != null && listServer.size() > 0) {
                connect();
            } else {
                
            }
        }
    }
    
    private void login() {
        JsonObject json = new JsonObject();
        json.addProperty("platform", "web");
        json.addProperty("channel", "bot-channel");
        json.addProperty("bundle_id", "");
        json.addProperty("app_version", "1.0.0");
        json.addProperty("udid", "bot-udid");
        json.addProperty("sessionId", String.valueOf(System.currentTimeMillis()));
        json.addProperty("email", email);
        json.addProperty("authorizeType", 1);

        SFSObject params = new SFSObject();
        params.putByte("login_type", (byte)2);
        params.putUtfString("login_token", "788305308286");
        params.putUtfString("client_info", json.toString());
        LoginRequest rq = new LoginRequest("", "", zone, params);
        sfs.send(rq);
    }
    
    private JsonArray getListServer() {
        try {
            String s = HTTPUtil.request(BALANCER_URL);
            JsonArray arr = parser.parse(s).getAsJsonObject().get("listServerLogin").getAsJsonArray();
            log.info("list server:" + arr.toString());
            return arr;
        } catch (Exception ex) {
            log.error("getListServer", ex);
        }
        return null;
    }

    private void sendMail(String text) {
        Properties props = new Properties();
        props.put("mail.smtp.ssl.trust", "webmail.mecorp.vn");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "webmail.mecorp.vn");
        props.put("mail.smtp.port", "587");
        
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("iwin-server@mecorp.vn", "LW=kCPI@");
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");
            message.setFrom(new InternetAddress("iwin-server@mecorp.vn"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Bot Login Alert");
            message.setText(text);

            Transport.send(message);

            log.info("Send mail done");

        } catch (MessagingException e) {
            log.error("", e);
        }
    }
    
    private void getTaiXiuInfo() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, (byte)1);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void getUserHistory() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, (byte)4);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void getServerHistory() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, (byte)5);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void getMatchHistory() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, (byte)5);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void getTop() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, (byte)7);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void sendTaiXiuBet() {
        JsonObject json = new JsonObject();
        json.addProperty("betMoney", 10000);
        json.addProperty("betChoice", 1);
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.PLAY_TAIXIU);
        sfsObj.putByte(SFSKey.COMMAND, (byte)3);
        sfsObj.putUtfString(SFSKey.DATA, json.toString());
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private String registerClient() throws IOException {
        String idFa = UUID.randomUUID().toString();
        JsonObject json = new JsonObject();
        json.addProperty("userAgent", "GT7690");
        json.addProperty("platform", "android");
        json.addProperty("deviceId", "Samsung galaxy S8+");
        json.addProperty("lang", "vn");
        json.addProperty("version", "1.0");
        json.addProperty("channel", "68|ref39");
        json.addProperty("idFa", idFa);
        json.addProperty("gaId", PasswordUtil.MD5Password(idFa));
        String response = HTTPUtil.request(VERIFY_URL + "/RegisterClient", json.toString());
        return response;
    }
    
    private String authenticate() throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", PasswordUtil.MD5Password(password));
        json.addProperty("clientId", clientId);
        String response = HTTPUtil.request(VERIFY_URL + "/Authorize", json.toString());
        return response;
    }
    
    public void start() {
        try {
            connect();
        } catch (Exception e) {
            log.error("error starting bot " + email, e);
        }
    }

    public static void main(String[] args) throws Exception {
        new SFSClient();
    }

    private void requestInfoAllGame() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.REQUEST_INFOR_ALL_GAME);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj));
    }
    
    private void joinLobby() {
        sfs.send(new JoinRoomRequest(Service.getLobbyName(Service.TIENLEN, MoneyContants.MONEY)));
    }
    
    public void getListBetMoney() {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.LOBBY_LIST_COUNTER);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj, room));
    }
    
    public void buyStack(double betMoney) {
        SFSObject sfsObj = new SFSObject();
        sfsObj.putInt(SFSKey.ACTION_INCORE, SFSAction.BUY_STACK_IN_LOBBY);
        sfsObj.putDouble(SFSKey.BET_BOARD, betMoney);
        sfsObj.putDouble(SFSKey.MONEY_STACK, betMoney * 10);
        sfsObj.putBool(SFSKey.IS_OWNER, true);
        sfs.send(new ExtensionRequest(SFSCommand.CLIENT_REQUEST, sfsObj, room));
    }
}
