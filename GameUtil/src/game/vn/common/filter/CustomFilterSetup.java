/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.filter;

import com.smartfoxserver.v2.controllers.SystemRequest;
import com.smartfoxserver.v2.controllers.filter.ISystemFilterChain;
import com.smartfoxserver.v2.controllers.filter.SysControllerFilterChain;
import com.smartfoxserver.v2.entities.Zone;

/**
 * 
 * @author
 */
public class CustomFilterSetup {
    public static void initialize(Zone targetZone) {
        // Reset filter chain to clean previous filters
        targetZone.resetSystemFilterChain();

        // add filter join room
        ISystemFilterChain filterChain = new SysControllerFilterChain();
        filterChain.addFilter("JoinRoomFilter", new JoinRoomFilter());
        // Plug the filter chain
        targetZone.setFilterChain(SystemRequest.JoinRoom, filterChain);

        ISystemFilterChain leaveRoomFilterChain = new SysControllerFilterChain();
        leaveRoomFilterChain.addFilter("LeaveRoomFilter", new LeaveRoomFilter());
        targetZone.setFilterChain(SystemRequest.LeaveRoom, leaveRoomFilterChain);

        ISystemFilterChain chatFilterChain = new SysControllerFilterChain();
        chatFilterChain.addFilter("ChatFilter", new ChatInGameFilter());
        targetZone.setFilterChain(SystemRequest.PublicMessage, chatFilterChain);
    }
}
