/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import com.smartfoxserver.v2.protocol.serialization.SerializableSFSType;
import java.util.List;

/**
 *
 * @author hanv
 */
public class ClientInfo implements SerializableSFSType {
    private static final byte AUTHORIZE_TYPE_LOGIN = 1;
    private static final byte AUTHORIZE_TYPE_VERIFY = 2;

    /**
     * Là key, được tạo ra từ các thành phần bên dưới
     */
    private String device_id;
    /**
     * Tên thiết bị
     */
    private String name;
    /**
     * thông số mạng
     */
    private String subscriber_Id;
    /**
     * thông số sim
     */
    private String sim_serial;
    /**
     * Chỉ dùng cho máy Android
     */
    private String android_id;
    /**
     * 
     */
    private String mac_address;
    /**
     * platform : android, ios, web, windowsphone8
     */
    private String platform;
    /**
     * true/false . Máy iOS là dùng cho jailbreak
     */
    private boolean rooted;
    /**
     * 
     */
    private String finger_print;
    /**
     * ios,windows phone, android, windows, mac
     */
    private String os;
    private String os_version;
    /**
     * số tự sinh
     */
    private String udid;
    /**
     * 
     */
    private String bluetooth_address;
    /**
     * ID quảng cáo
     */
    private String advertising_id;
    /**
     * Là check sum tất cả các thông tin user gửi lên.
     */
    private String checksum;
    /**
     * IP login lần đầu
     */
    private String first_ip;
    /**
     * IP login lần cuối
     */
    private String last_ip;
    
    private String channel;
    /**
     * ngôn ngữ đang sử dụng
     */
    private String lang;
    /**
     * version của user
     */
    private String app_version;

    private int width;
    private int height;
    private String carrier;
    private String bundle_id;
    private String location;
    private String network;
    private String email = "";
    private String sessionId = String.valueOf(System.currentTimeMillis());
    private int authorizeType = AUTHORIZE_TYPE_LOGIN;

    private List<Integer> shuffleGames;

    /**
     * Tổng số lần thiết bị này kết nối hệ thống
     */
    private String count;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubscriber_Id() {
        return subscriber_Id;
    }

    public void setSubscriber_Id(String subscriber_Id) {
        this.subscriber_Id = subscriber_Id;
    }

    public String getSim_serial() {
        return sim_serial;
    }

    public void setSim_serial(String sim_serial) {
        this.sim_serial = sim_serial;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isRooted() {
        return rooted;
    }

    public void setRooted(boolean rooted) {
        this.rooted = rooted;
    }

    public String getFinger_print() {
        return finger_print;
    }

    public void setFinger_print(String finger_print) {
        this.finger_print = finger_print;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOs_version() {
        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getBluetooth_address() {
        return bluetooth_address;
    }

    public void setBluetooth_address(String bluetooth_address) {
        this.bluetooth_address = bluetooth_address;
    }

    public String getAdvertising_id() {
        return advertising_id;
    }

    public void setAdvertising_id(String advertising_id) {
        this.advertising_id = advertising_id;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFirst_ip() {
        return first_ip;
    }

    public void setFirst_ip(String first_ip) {
        this.first_ip = first_ip;
    }

    public String getLast_ip() {
        return last_ip;
    }

    public void setLast_ip(String last_ip) {
        this.last_ip = last_ip;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getBundle_id() {
        return bundle_id;
    }

    public void setBundle_id(String bundle_id) {
        this.bundle_id = bundle_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getAuthorizeType() {
        return authorizeType;
    }

    public void setAuthorizeType(int authorizeType) {
        this.authorizeType = authorizeType;
    }

    public List<Integer> getShuffleGames() {
        return shuffleGames;
    }

    @Override
    public String toString() {
        return "ClientInfo{" + "device_id=" + device_id + ", name=" + name + ", subscriber_Id=" + subscriber_Id + ", sim_serial=" + sim_serial + ", android_id=" + android_id + ", mac_address=" + mac_address + ", platform=" + platform + ", rooted=" + rooted + ", finger_print=" + finger_print + ", os=" + os + ", os_version=" + os_version + ", udid=" + udid + ", bluetooth_address=" + bluetooth_address + ", advertising_id=" + advertising_id + ", checksum=" + checksum + ", first_ip=" + first_ip + ", last_ip=" + last_ip + ", channel=" + channel + ", lang=" + lang + ", app_version=" + app_version + ", width=" + width + ", height=" + height + ", carrier=" + carrier + ", bundle_id=" + bundle_id + ", location=" + location + ", network=" + network + ", count=" + count + '}';
    }
}
