package game.vn.game.login.constant;

/**
 * cac static var lien quan toi extension
 *
 * @author minhhnb
 */
public class ExtensionConstant {

    public static final byte NORMAL_LOGIN = 0;
    public static final byte FB_LOGIN = 1;
    public static final byte GG_LOGIN = 2;
    public static final byte TOKEN_LOGIN = 3;

    public static final byte CODE_SUCCESS = 1;
    public static final byte CODE_FAIL = 0;

    //format avatar mới sẽ có dạng avatarName@version
    //example : messi@1.png, cr7@2.png
    public static final String AVATAR_NAME_FORMAT = "%s@%d.png";
    //avatar defaul neu user chua set avatar nao ca
    public static final String DEFAULT_AVATAR_NAME = "default@1.png";
}
