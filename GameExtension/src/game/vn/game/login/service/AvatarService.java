/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.login.service;

import com.smartfoxserver.v2.entities.data.SFSArray;
import game.vn.game.login.constant.ExtensionConstant;
import game.vn.game.login.domain.AvatarInfo;
import game.vn.util.db.Database;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author anlh
 */
public class AvatarService {

    private static final Logger LOG = LoggerFactory.getLogger(AvatarService.class);

    public static final AvatarService instance = new AvatarService();
    public static SFSArray listAvatar = new SFSArray();
    public static List<AvatarInfo> listRawAvatar = new ArrayList<>();
    
    /**
     * get avatar filename user khi login thành công, để set vào userVariable cho client lấy
     * @param userId
     * @return 
     */
    public String getUserAvatar(String userId) {
        String avatarFileName = ExtensionConstant.DEFAULT_AVATAR_NAME;
        try {
            int userAvatarId = Database.instance.getUserAvatarId(userId);
            
            if (userAvatarId > 0) {
                for (AvatarInfo avatarinfo: listRawAvatar) {
                    if(avatarinfo.getAvatarId() == userAvatarId) {
                        avatarFileName = String.format(ExtensionConstant.AVATAR_NAME_FORMAT, avatarinfo.getAvatarName(), avatarinfo.getVersion());
                        break;                        
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("getUserAvatar error: ", e);
        }
        return avatarFileName;
    }

}
