/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.filter;

import com.smartfoxserver.v2.controllers.filter.SysControllerFilter;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.filter.FilterAction;
import game.vn.common.config.SFSConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;

/**
 *
 * @author hanv
 */
public class JoinRoomFilter extends SysControllerFilter {

    @Override
    public FilterAction handleClientRequest(User user, ISFSObject isfso) throws SFSException {
        try {
            String roomName = isfso.getUtfString("n");
            Room room = SFSConfig.getZone().getRoomByName(roomName);
            /**
             * Trường hợp remove room in sfs mà không remove trên hazelcast thì
             * sẽ join room error, thì remove o đây để tránh bị dính bàn không
             * tồn tại trên hazelcast
             */
            if (room == null) {
                //remove room khoi hazelcast
                Board board = HazelcastUtil.getBoardInfor(roomName);
                if (board != null) {
                    if (board.getServerId() == ServerConfig.getInstance().getServerId()) {
                        HazelcastUtil.removeBoardInfor(roomName);
                        HazelcastUtil.removeBoardWaitingInfor(board);
                    }
                }
                String userId = Utils.getIdDBOfUser(user);
                //remove bàn reconnect
                PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(userId);
                if (playingBoard != null && playingBoard.getBoardPlaying() != null) {
                    Board boardPlaying = playingBoard.getBoardPlaying();
                    //không thể trùng name
                    if (boardPlaying.getName().equals(roomName) && boardPlaying.getServerId() == ServerConfig.getInstance().getServerId()) {
                        HazelcastUtil.removePlayingBoard(userId);
                    }
                }
                return FilterAction.HALT;
            }
        } catch (Exception e) {
            this.trace("JoinRoomFilter error: ", e);
        }
        return FilterAction.CONTINUE;
    }

}
