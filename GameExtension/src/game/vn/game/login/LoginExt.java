package game.vn.game.login;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;
import game.command.SFSCommand;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.ExtensionConstant;
import game.vn.common.filter.CustomFilterSetup;
import game.vn.common.queue.QueueHistory;
import game.vn.common.queue.QueueNotify;
import game.vn.common.queue.QueueServiceApi;
import game.vn.common.queue.QueueServiceEvent;
import game.vn.common.queue.QueueServiceVip;
import game.vn.common.queue.QueueUserManager;
import game.vn.common.queue.updateconfig.QueueUpdateConfig;
import game.vn.common.service.ShuffleService;
import game.vn.common.tournament.TournamentManager;
import game.vn.game.login.handler.JoinZoneEventHandler;
import game.vn.game.login.handler.LoginHandler;
import game.vn.game.login.handler.LogoutHandler;
import game.vn.game.login.handler.ReconnectHandler;
import game.vn.game.login.handler.RemoveRoomHandler;
import game.vn.game.login.handler.DisConnectHandler;
import game.vn.game.login.handler.request.CommonClientRequest;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author minhhnb
 */
public class LoginExt extends SFSExtension{

    @Override
    public void init() {
        trace("LoginGameExt init...");
        Utils.init(getApi());
        addEventHandler();
        CustomFilterSetup.initialize(getParentZone());
        trace("addEventHandler init...");
        Database.instance.init(getParentZone().getDBManager());
        trace("Database init...");
        initQueue();
        trace("initQueue init...");
        SFSConfig.init(getParentZone());
        // remove tất cả các board cũ của con server này trên hazelcast
        HazelcastUtil.removeAllBoardFromIP(ServerConfig.getInstance().getIP());
        trace("LoginGameExt init done.");

        HazelcastUtil.addServerInfo(ExtensionConstant.SERVER_TYPE_GAME);
        //loa từ file config
        TournamentManager.getInstance().initAllTournaments();
        //init tournament
        TournamentManager.getInstance().initTourToHazelcast();

        if (!ServerConfig.getInstance().getListShuffleGame().isEmpty()) {
            getApi().getSystemScheduler().scheduleAtFixedRate(new ShuffleService(getApi()), 0, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void destroy() {
        trace("LoginGameExt destroy...");
        super.destroy();  
    }

    /**
     * init những event extension sẽ bắt và xử lý khi client gửi len
     */
    private void addEventHandler(){
        addEventHandler(SFSEventType.USER_LOGIN, LoginHandler.class);
        addEventHandler(SFSEventType.USER_LOGOUT, LogoutHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT, DisConnectHandler.class);
        addEventHandler(SFSEventType.USER_RECONNECTION_SUCCESS, ReconnectHandler.class);   
        addEventHandler(SFSEventType.USER_JOIN_ZONE, JoinZoneEventHandler.class);
        addEventHandler(SFSEventType.ROOM_REMOVED, RemoveRoomHandler.class);
        
        addRequestHandler(SFSCommand.CLIENT_REQUEST, CommonClientRequest.class);
    }

    /**
     * init queue
     */
    private void initQueue() {
        if (ServerConfig.getInstance().enableVip()) {
            QueueServiceVip.getInstance().init();
        }
        QueueServiceApi.getInstance().init();
        QueueUpdateConfig.instance().init();
        QueueUpdateConfig.instance().consume();
        QueueUserManager.instance().init(getApi(), getParentZone());
        QueueUserManager.instance().consume();
        QueueNotify.getInstance().init();
        QueueServiceEvent.getInstance().init();
        QueueHistory.instance().init();
    }
}
