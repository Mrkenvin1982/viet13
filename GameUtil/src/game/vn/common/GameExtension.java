/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common;

import com.smartfoxserver.v2.controllers.SystemRequest;
import com.smartfoxserver.v2.controllers.filter.ISystemFilterChain;
import com.smartfoxserver.v2.controllers.filter.SysControllerFilterChain;
import game.vn.common.handler.request.CommonClientRequest;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;
import game.command.SFSCommand;
import game.vn.common.filter.ChatInGameFilter;
import game.vn.common.filter.JoinRoomFilter;
import game.vn.common.filter.LeaveRoomFilter;
import game.vn.common.handler.JoinRoomHandler;
import game.vn.common.handler.LeaveRoomHandler;
import game.vn.common.handler.UserDisConnectHandler;
import game.vn.common.handler.request.ClientGameRequest;
import game.vn.util.db.Database;

/**
 *
 * @author binhnt
 */
public abstract class GameExtension extends SFSExtension {

    private String groupName;
    private String extensionId;
    private String extensionMainClass;
    public GameController gameController;

    public GameExtension(String groupName, String extensionId, String extensionMainClass) {
        this.groupName = groupName;
        this.extensionId = extensionId;
        this.extensionMainClass = extensionMainClass;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    public String getExtensionMainClass() {
        return extensionMainClass;
    }

    public void setExtensionMainClass(String extensionMainClass) {
        this.extensionMainClass = extensionMainClass;
    }

    @Override
    public void init() {
//        trace("init Game...");
        this.addCommonEventHandler();
        Database.instance.init(getParentZone().getDBManager());
        //nếu là game thì mới khỏi tạo gameController(xử lý logic game)
        if(this.getParentRoom().isGame()){
            initGameController();
        }
//        trace("init Game... DONE");
    }

    /**
     * init những event extension sẽ bắt và xử lý khi client gửi len
     */
    private void addCommonEventHandler() {
        addRequestHandler(SFSCommand.CLIENT_REQUEST, CommonClientRequest.class);
        addRequestHandler(SFSCommand.CLIENT_REQUEST_INGAME, ClientGameRequest.class);
        addEventHandler(SFSEventType.USER_JOIN_ROOM, JoinRoomHandler.class);
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, LeaveRoomHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT, UserDisConnectHandler.class);
    }

    private void addFilterChain() {
      // Reset filter chain to clean previous filters
        getParentZone().resetSystemFilterChain();

        // add filter join room
        ISystemFilterChain filterChain = new SysControllerFilterChain();
        filterChain.addFilter("JoinRoomFilter", new JoinRoomFilter());
        // Plug the filter chain
        getParentZone().setFilterChain(SystemRequest.JoinRoom, filterChain);

        ISystemFilterChain leaveRoomFilterChain = new SysControllerFilterChain();
        leaveRoomFilterChain.addFilter("LeaveRoomFilter", new LeaveRoomFilter());
        getParentZone().setFilterChain(SystemRequest.LeaveRoom, leaveRoomFilterChain);

        ISystemFilterChain chatFilterChain = new SysControllerFilterChain();
        chatFilterChain.addFilter("ChatFilter", new ChatInGameFilter());
        getParentZone().setFilterChain(SystemRequest.PublicMessage, chatFilterChain);
    }

    protected abstract void initGameController();
}