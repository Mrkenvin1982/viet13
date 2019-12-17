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
 * @author 
 */
public class Device {

    /**
     * default platform for every device.
     */
    private static final PlatForm DEFAULT_PLATFORM = PlatForm.UNKNOW;
    transient Logger logger = LoggerFactory.getLogger(Device.class);
    private PlatForm platForm = DEFAULT_PLATFORM;
    private ClientInfo clientInfor;
    private String clientPhone;
    private String refcode;
    private String phoneCode;
    private Version version;
    private String clientIp;
    private String bundleId="";
    /**
     * thông tin về device user đã đăng nhập
     */
    /**
     * device đã được tín nhiệm chưa? 1: trusted, 0: not trust
     */
    private int trusted = -1;
    public Device() {
        this(DEFAULT_PLATFORM);
    }

    public Device(PlatForm platForm) {
        this.platForm = platForm;
        version = Version.UNKNOWN_VERSION;
    }

    public PlatForm getPlatForm() {
        return platForm;
    }

    public void setPlatForm(PlatForm platForm) {
        this.platForm = platForm;
    }

    public ClientInfo getClientInfor() {
        return clientInfor;
    }

    public void setClientInfor(ClientInfo clientInfor) {
        this.clientInfor = clientInfor;
        setPlatForm(PlatForm.getPlatForm(clientInfor));
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public String getRefcode() {
        return refcode;
    }

    public void setRefcode(String refcode) {
        this.refcode = refcode;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public boolean isHigherCurrentVersion() {
        return isHigherVersion(getPlatForm().getCurrentVersion());
    }

    public boolean isHigherVersion(Version version) {
        return this.getVersion().compareTo(version) >= 0;
    }


    public int getTrusted() {
        return trusted;
    }

    public void setTrusted(int trusted) {
        this.trusted = trusted;
    }

    public boolean isTrusted(){
        return trusted == 1;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Device{platForm=");
        sb.append(platForm)
                .append(", clientInfor=").append(clientInfor)
                .append(", clientPhone=").append(clientPhone)
                .append(", refcode=").append(refcode)
                .append(", phoneCode=").append(phoneCode)
                .append(", BundleId=").append(bundleId);
        return sb.toString();
    }
}
