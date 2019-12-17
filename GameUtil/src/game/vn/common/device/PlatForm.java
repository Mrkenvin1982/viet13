/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.device;

import game.vn.common.object.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanP
 */
public class PlatForm  {

    public static final byte ID_ALL=-1;
    public static final byte ID_UNKNOW=0;
    public static final byte ID_ANDROID=1;
    public static final byte ID_IOS=2;
    public static final byte ID_WINDOWS_DESKTOP=3;
    
    public static final PlatForm ANDROID = new PlatForm(ID_ANDROID);
    public static final PlatForm IOS = new PlatForm(ID_IOS);
    public static final PlatForm WINDOWS_DESKTOP = new PlatForm(ID_WINDOWS_DESKTOP);
    public static final PlatForm ALL_PLATFORM = new PlatForm(ID_ALL);
    public static final PlatForm UNKNOW = new PlatForm(ID_UNKNOW);
    
    
    public static final String IOS_NAME = "ios";
    public static final String ANDROID_NAME = "android";
    public static final String DESKTOP_NAME = "desktop";
    public static final String UNKNOW_NAME = "unknow";
    public static final String ALL_PLATFORM_NAME = "all";
    
    static Logger logger = LoggerFactory.getLogger(PlatForm.class);
    private final int id;
    private final String name;

    public PlatForm(int id) {
        super();
        this.id = id;
        this.name = getName();
    }

    public boolean isAll() {
        return this.id==ID_ALL;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        String name = "";
        switch (getId()) {
            case ID_ANDROID:
                name = ANDROID_NAME;
                break;
            case ID_IOS:
                name = IOS_NAME;
                break;
            case ID_WINDOWS_DESKTOP:
                name = DESKTOP_NAME;
                break;
            case ID_UNKNOW:
            default:
                name = UNKNOW_NAME;
                break;
        }
        return name;
    }

    /**
     * lấy tên platform theo quy định bên inside
     *
     * @return
     */
    public String getInsideName() {
        switch (getId()) {
            case ID_ANDROID:
                return ANDROID_NAME;
            case ID_IOS:
                return IOS_NAME;
            case ID_WINDOWS_DESKTOP:
                return DESKTOP_NAME;
        }
        return UNKNOW_NAME;
    }
    
    public static PlatForm getPlatForm(ClientInfo clientInfor) {
       PlatForm returnPlatForm = UNKNOW;

        if (DESKTOP_NAME.equalsIgnoreCase(clientInfor.getPlatform())) {
             returnPlatForm = WINDOWS_DESKTOP;
        }else if (IOS_NAME.equalsIgnoreCase(clientInfor.getPlatform())) {
            returnPlatForm = IOS;
        }else if (ANDROID_NAME.equalsIgnoreCase(clientInfor.getPlatform())) {
            returnPlatForm = ANDROID;
        } 
        logger.debug(""+returnPlatForm);
        return returnPlatForm;
    }
    public static PlatForm getPlatForm(String namePlatform) {
       PlatForm returnPlatForm = UNKNOW;
        if (DESKTOP_NAME.equalsIgnoreCase(namePlatform)) {
             returnPlatForm = WINDOWS_DESKTOP;
        }else if (IOS_NAME.equalsIgnoreCase(namePlatform)) {
            returnPlatForm = IOS;
        }else if (ANDROID_NAME.equalsIgnoreCase(namePlatform)) {
            returnPlatForm = ANDROID;
        }else if (ALL_PLATFORM_NAME.equalsIgnoreCase(namePlatform)) {
            returnPlatForm = ALL_PLATFORM;
        } 
        logger.debug(""+returnPlatForm);
        return returnPlatForm;
    }

    public Version getCurrentVersion() {
//        Version currentVersion = ServerInfo.getInstance().getCurrentClientVersion();
//        switch (this.getId()) {
//            case ID_ANDROID:
//                currentVersion = ServerInfo.getInstance().getClientAndroidVersion();
//                break;
//            case ID_IOS:
//                currentVersion = ServerInfo.getInstance().getClientIOSVersion();
//                break;
//            case ID_WINDOWS_DESKTOP:
//            case ID_UNKNOW:
//            default:
//                currentVersion = ServerInfo.getInstance().getCurrentClientVersion();
//                break;
//        }
//        return currentVersion;
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlatForm other = (PlatForm) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
