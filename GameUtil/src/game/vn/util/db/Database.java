/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smartfoxserver.v2.db.IDBManager;
import game.vn.common.constant.MoneyContants;
import game.vn.common.lib.api.BotAdvantage;
import game.vn.common.lib.api.ConvertMoneyResult;
import game.vn.common.lib.api.P2PTransferConfig;
import game.vn.common.lib.api.PointConvertConfig;
import game.vn.common.lib.api.Transaction;
import game.vn.common.lib.api.TransactionHistory;
import game.vn.common.lib.api.UserReceiveMoneyOffline;
import game.vn.common.lib.iap.IAPItem;
import game.vn.common.lib.news.News;
import game.vn.common.lib.news.NewsButton;
import game.vn.common.lib.payment.malaya.ChargeBankingInfo;
import game.vn.common.lib.payment.malaya.ChargeCardInfo;
import game.vn.common.lib.payment.malaya.ChargePromotionSchedule;
import game.vn.common.lib.payment.malaya.ChargePromotionTime;
import game.vn.common.lib.poker.spinandgo.Reward;
import game.vn.common.lib.poker.spinandgo.RewardMulti;
import game.vn.common.object.ClientVersionUpdate;
import game.vn.common.object.PointReceiveInfo;
import game.vn.common.object.UserInfo;
import game.vn.common.tournament.SumMoneyByBetBoard;
import game.vn.util.DateUtil;
import game.vn.util.Utils;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hanv
 */
public class Database {
    private final static Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public static final Database instance = new Database();

    private IDBManager dbManager;

    public void init(IDBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * cập nhật điểm của user
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callUpdatePointProcedure(String userId, double value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_update_point(?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setDouble(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callUpdatePointProcedure " + userId + " - " + value, e);
        }
        return umr;
    }

    /**
     * cập nhật điểm của user
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callUpdatePointProcedure(String userId, BigDecimal value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_update_point(?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callUpdatePointProcedure " + userId + " - " + value.toString(), e);
        }
        return umr;
    }

    /**
     * cập nhật tiền của user
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callUpdateMoneyProcedure(String userId, BigDecimal value) {
        return callUpdateUSDProcedure(userId, value);
    }

    /**
     * cập nhật tiền của user trong bàn
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callUpdateMoneyStackProcedure(String userId, BigDecimal value) {
        return callUpdateUSDStackProcedure(userId, value);
    }

    /**
     * cập nhật tiền của user
     * @param userId
     * @param value
     * @return
     */
    private UpdateMoneyResult callUpdateUSDProcedure(String userId, BigDecimal value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_update_money(?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callUpdateMoneyProcedure " + userId + " - " + value.toString(), e);
        }
        return umr;
    }

    public TransferMoneyResult callTransferMoneyProcedure(String fromUserId, String toUserId, BigDecimal fromValue, BigDecimal toValue) {
        return callTransferUSDProcedure(fromUserId, toUserId, fromValue, toValue);
    }

    private TransferMoneyResult callTransferUSDProcedure(String fromUserId, String toUserId, BigDecimal fromValue, BigDecimal toValue) {
        TransferMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_transfer_money(?, ?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, fromUserId);
                call.setString(2, toUserId);
                call.setBigDecimal(3, fromValue);
                call.setBigDecimal(4, toValue);
                call.registerOutParameter(5, Types.DECIMAL);
                call.registerOutParameter(6, Types.DECIMAL);
                call.registerOutParameter(7, Types.DECIMAL);
                call.registerOutParameter(8, Types.DECIMAL);
                call.executeUpdate();
                umr = new TransferMoneyResult();
                umr.fromMoneyBefore = call.getBigDecimal(5);
                umr.fromMoneyAfter = call.getBigDecimal(6);
                umr.toMoneyBefore = call.getBigDecimal(7);
                umr.toMoneyAfter = call.getBigDecimal(8);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callTransferUSDProcedure " + fromUserId + " " + fromValue.toString(), e);
        }
        return umr;
    }

    /**
     * Get điểm của user
     * @param userId
     * @return
     */
    public double getUserPoint(String userId) {
        double point = -1;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `point` FROM `sfs_user_point` WHERE `user_id` = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        point = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserPoint", e);
        }
        return point;
    }

    /**
     *
     * @param userId
     * @return
     */
    public boolean checkUserIdExist(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `id` FROM `sfs_user` WHERE `id`=? LIMIT 1")) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.checkUserIdExist", e);
        }
        return false;
    }

    public String getUserIdByTransferData(String data) {
        String userId = null;
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `id` FROM `sfs_user` WHERE `id`=? OR `email`=? LIMIT 1")) {
                ps.setString(1, data);
                ps.setString(2, data);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getString(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserIdByTransferData", e);
        }
        return userId;
    }

    /**
     * get tiền của user
     * @param userId
     * @return
     */
    public double getUserMoney(String userId) {
        return getUserUSD(userId);
    }

    /**
     * get tiền của user
     * @param userId
     * @return
     */
    private double getUserUSD(String userId) {
        double money = -1;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `money` FROM `sfs_user_money` WHERE `user_id` = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        money = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserUSD error:", e);
        }
        return money;
    }

    
    public void updateLastLogin(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sfs_user SET last_login=NOW() WHERE id=?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateLastLogin", e);
        }
    }

    public void updateLastLoginFB(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sfs_facebook_user SET last_login=NOW() WHERE user_id=?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateLastLoginFB", e);
        }
    }

    public void updateLastLoginGG(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `sfs_google_user` SET `last_login`=NOW() WHERE `user_id`=?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateLastLoginGG", e);
        }
    }

    public String getUserIdBySocialId(String socialId) {
        String userId = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `id` FROM `sfs_user` where `social_id` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, socialId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserIdBySocialId error", e);
        }
        return userId;
    }

    public String getUserIdByFacebookBusinessToken(String businessToken) {
        String userId = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `user_id` FROM `sfs_facebook_user` where `business_token` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, businessToken);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserIdByFacebookBusinessToken error", e);
        }
        return userId;
    }

    public String getUserIdByGoogleId(String googleId) {
        String userId = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `user_id` FROM `sfs_google_user` where `gg_id` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, googleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserIdByGoogleId error", e);
        }
        return userId;
    }

    public String getUserEmail(String userId) {
        String email = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `email` FROM `sfs_user` where `id` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        email = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserEmail", e);
        }
        return email;
    }

    public double insertNewUser(String userId, String socialId, String displayName, String avatar, String email, String platform, byte loginType) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "CALL `sfs_create_user`(?, ?, ?, ?, ?, ?, ?, ?)";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setString(2, socialId);
                call.setString(3, displayName);
                call.setString(4, avatar);
                call.setString(5, email);
                call.setString(6, platform);
                call.setByte(7, loginType);
                call.registerOutParameter(8, Types.DECIMAL);
                call.executeUpdate();
                return call.getBigDecimal(8).doubleValue();
            }
        } catch (Exception e) {
            LOGGER.error("Database.insertNewUser", e);
        }
        return -1;
    }

    /**
     *
     * @param userId
     * @param displayName
     * @param merchantId
     */
    public void createGuest(String userId, String displayName, String merchantId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "CALL `sfs_create_guest`(?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, displayName);
                ps.setString(3, merchantId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.createGuest", e);
        }
    }

    /**
     * - Kiem tra display name của user
     * - Check xem user có bị ban không
     * @param userId
     * @return
     */
    public int[] checkExitDisplayName(String userId) {
        String displayName = null;
        int[] result = new int[2];
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `display_name`,ban FROM `sfs_user` WHERE `id`=? LIMIT 1")) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        displayName = rs.getString(1);
                        int ban = rs.getInt("ban");
                        if (ban == 1) {
                            result[1] = -1;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getDisplayName", e);
        }
        if (displayName == null || displayName.isEmpty()) {
            result[0] = -1;
        }
        return result;
    }

    public boolean banUser(String userId, int type) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `sfs_user` SET `ban`=? WHERE `id`=? LIMIT 1")) {
                ps.setInt(1, type);
                ps.setString(2, userId);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.banUser", e);
        }

        return false;
    }

    public String getDisplayName(String userId) {
        String displayName = null;
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `display_name` FROM `sfs_user` WHERE `id`=? LIMIT 1")) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        displayName = rs.getString(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getDisplayName", e);
        }
        return displayName;
    }

    public String getPinCode(String userId) {
        String pinCode = null;
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `pin_code` FROM `sfs_user` WHERE `id`=? LIMIT 1")) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pinCode = rs.getString(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getPinCode", e);
        }
        return pinCode;
    }

    public byte getPinStatus(String userId) {
        byte status = 0;
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT `pin_status` FROM `sfs_user` WHERE `id`=? LIMIT 1")) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        status = rs.getByte(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getPinStatus", e);
        }
        return status;
    }

    public boolean updatePinCode(String userId, String pinCode) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `sfs_user` SET `pin_status`=2, `pin_code`=? WHERE `id`=? LIMIT 1")) {
                ps.setString(1, pinCode);
                ps.setString(2, userId);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePinCode", e);
        }
        return false;
    }

    public boolean updatePinStatus(String userId, byte status) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE `sfs_user` SET `pin_status`=? WHERE `id`=? LIMIT 1")) {
                ps.setByte(1, status);
                ps.setString(2, userId);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePinStatus", e);
        }
        return false;
    }

    public int[] getPinError(String userId) {
        int[] error = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `count`, TIMESTAMPDIFF(SECOND, NOW(), `time_unlock`) FROM `sfs_pin_error` WHERE `user_id`=? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        error = new int[2];
                        error[0] = rs.getInt(1);
                        error[1] = rs.getInt(2);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getPinError", e);
        }
        return error;
    }

    public void resetPinError(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE `sfs_pin_error` SET `count`=0, `time_unlock`=NULL WHERE `user_id`=? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.resetPinError", e);
        }
    }

    public void createPinError(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO `sfs_pin_error` (`user_id`) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePinError", e);
        }
    }

    public void updatePinError(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE `sfs_pin_error` SET `count`=`count`+1 WHERE `user_id`=? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePinError", e);
        }
    }

    public void updatePinError(String userId, int minutes) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE `sfs_pin_error` SET `count`=`count`+1, `time_unlock`=TIMESTAMPADD(MINUTE, ?, NOW()) WHERE `user_id`=? LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, minutes);
                ps.setString(2, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePinError", e);
        }
    }

    public boolean updateDisplayName(String userId, String displayName) {
        boolean result = false;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE `sfs_user` SET `display_name`=? WHERE `id`=? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, displayName);
                ps.setString(2, userId);
                result = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateDisplayName", e);
        }
        return result;
    }

    public boolean updateAvatar(String userId, String avatar) {
        boolean result = false;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE `sfs_user` SET `avatar`=? WHERE `id`=? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, avatar);
                ps.setString(2, userId);
                result = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateAvatar", e);
        }
        return result;
    }

    public String getUserAvatar(String userId) {
        String avatar = "";
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `avatar` FROM `sfs_user` where id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        avatar = rs.getString(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateAvatar", e);
        }
        return avatar;
    }

    public boolean insertFBUser(String userId, String fbId, String businessToken) {
        boolean result = false;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO `sfs_facebook_user` (`user_id`, `fb_id`, `business_token`) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, fbId);
                ps.setString(3, businessToken);
                result = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.insertFBUser", e);
        }
        return result;
    }

    public boolean insertGGUser(String userId, String googleId) {
        boolean result = false;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO `sfs_google_user` (`user_id`, `gg_id`) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, userId);
                ps.setString(2, googleId);
                result = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.error("Database.insertGGUser", e);
        }
        return result;
    }

    /**
     * lấy iap item
     * @param money
     * @param platform
     * @return
     */
    public IAPItem getIAPItem(double money, String platform) {
        IAPItem item = null;
        try (Connection conn = dbManager.getConnection()) {
            if (!platform.equals("ios")) {
                platform = "android";
            }
            String sql = String.format("SELECT * FROM `sfs_iap_item_%s` WHERE `money` = ? LIMIT 1;", platform);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, money);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        item = new IAPItem();
                        item.setMoney(money);
                        item.setPoint(rs.getInt("point"));
                        item.setPromotion(rs.getInt("promotion"));
                        item.setUrl(rs.getString("url"));
                        item.setUrlEn(rs.getString("url_en"));
                        item.setSpecial(rs.getBoolean("is_special"));
                        item.setEnable(rs.getBoolean("is_enable"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getIAPItem", e);
        }
        return item;
    }

    /**
     * lấy danh sách tất cả item in-app theo platform (android/ios)
     * @param platform
     * @return
     */
    public List getListIAPItem(String platform) {
        List list = new ArrayList();
        try (Connection conn = dbManager.getConnection()) {
            if (!platform.equals("ios")) {
                platform = "android";
            }
            String sql = String.format("SELECT * FROM `sfs_iap_item_%s`", platform);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        IAPItem item = new IAPItem();
                        item.setId(rs.getInt("id"));
                        item.setMoney(rs.getDouble("money"));
                        item.setPoint(rs.getInt("point"));
                        item.setPromotion(rs.getInt("promotion"));
                        item.setSpecial(rs.getBoolean("is_special"));
                        item.setEnable(rs.getBoolean("is_enable"));
                        item.setUrl(rs.getString("url"));
                        item.setUrlEn(rs.getString("url_en"));
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getListIAPItem", e);
        }
        return list;
    }

    /**
     *
     * @param platform
     * @param items
     */
    public void updateListIAPItem(String platform, List<IAPItem> items) {
        try (Connection conn = dbManager.getConnection()) {
            if (!platform.equals("ios")) {
                platform = "android";
            }
            for (IAPItem item : items) {
                String sql;
                if (item.getId() >= 0) {
                    sql = String.format("UPDATE `sfs_iap_item_%s` SET money=?, point=?, url=?, is_special=? WHERE `id`=?", platform);
                } else {
                    sql = String.format("INSERT INTO `sfs_iap_item_%s` (money, point, url, is_special) VALUES (?, ?, ?, ?)", platform);
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, item.getMoney());
                    ps.setInt(2, item.getPoint());
                    ps.setString(3, item.getUrl());
                    ps.setBoolean(4, item.isSpecial());
                    if (item.getId() >= 0) {
                        ps.setInt(5, item.getId());
                    }
                    ps.execute();
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.updateListIAPItem", e);
        }
    }

    /**
     * lấy danh sách tin tức đang chạy (trạng thái bật và trong thời gian chạy)
     * @param lang
     * @return
     */
    public List<News> getListNews(String lang) {
        List<News> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `sfs_news` WHERE `enable`=1 AND `lang`=? AND NOW() BETWEEN start_time AND end_time ORDER BY `start_time` DESC ;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        News news = new News();
                        news.setId(rs.getInt("id"));
                        String caption = rs.getString("button1");
                        if (!caption.isEmpty()) {
                            NewsButton btn = new NewsButton(caption);
                            btn.setType(rs.getByte("type1"));
                            btn.setData(rs.getString("data1"));
                            news.setButton1(btn);
                        }
                        caption = rs.getString("button2");
                        if (!caption.isEmpty()) {
                            NewsButton btn = new NewsButton(caption);
                            btn.setType(rs.getByte("type2"));
                            btn.setData(rs.getString("data2"));
                            news.setButton2(btn);
                        }
                        news.setCategory(rs.getByte("category"));
                        news.setContent(rs.getString("content"));
                        news.setIcon(rs.getString("icon"));
                        news.setImage(rs.getString("image"));
                        news.setImageLarge(rs.getString("image_large"));
                        news.setTitle(rs.getString("title"));
                        news.setStartTime(rs.getTimestamp("start_time").getTime());
                        news.setPopup(rs.getBoolean("popup"));
                        list.add(news);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getListNews", e);
        }
        return list;
    }

    /**
     * lấy danh sách popup
     * @param lang
     * @param isCheckEvent
     * @return
     */
    public List<News> getListPopup(String lang, boolean isCheckEvent) {
        List<News> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `sfs_news` WHERE `enable`=1 AND `lang`=? AND `popup`=1 AND `category`< 4 AND NOW() BETWEEN start_time AND end_time ORDER BY `start_time` DESC;";
            if(isCheckEvent) {
                sql = "SELECT * FROM `sfs_news` WHERE `enable`=1 AND `lang`=? AND `popup`=1 AND `category` = 4 AND NOW() BETWEEN start_time AND end_time ORDER BY `start_time` DESC;";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        News news = new News();
                        news.setId(rs.getInt("id"));
                        String caption = rs.getString("button1");
                        if (!caption.isEmpty()) {
                            NewsButton btn = new NewsButton(caption);
                            btn.setType(rs.getByte("type1"));
                            btn.setData(rs.getString("data1"));
                            news.setButton1(btn);
                        }
                        caption = rs.getString("button2");
                        if (!caption.isEmpty()) {
                            NewsButton btn = new NewsButton(caption);
                            btn.setType(rs.getByte("type2"));
                            btn.setData(rs.getString("data2"));
                            news.setButton2(btn);
                        }
                        news.setCategory(rs.getByte("category"));
                        news.setContent(rs.getString("content"));
                        news.setIcon(rs.getString("icon"));
                        news.setImage(rs.getString("image"));
                        news.setImageLarge(rs.getString("image_large"));
                        news.setTitle(rs.getString("title"));
                        news.setStartTime(rs.getTimestamp("start_time").getTime());
                        news.setPopup(true);
                        list.add(news);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getListPopup", e);
        }
        return list;
    }

    /**
     * lấy danh sách toàn bộ tin tức, không kiểm tra điều kiện bật/tắt & thời gian
     * @return
     */
    public List<News> loadAllNews() {
        List<News> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `sfs_news`";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        News news = new News();
                        news.setId(rs.getInt("id"));
                        String caption = rs.getString("button1");
                        if (!caption.isEmpty()) {
                            NewsButton btn = new NewsButton(caption);
                            btn.setType(rs.getByte("type1"));
                            btn.setData(rs.getString("data1"));
                            news.setButton1(btn);
                        }
                        caption = rs.getString("button2");
                        if (!caption.isEmpty()) {
                            NewsButton btn = new NewsButton(caption);
                            btn.setType(rs.getByte("type2"));
                            btn.setData(rs.getString("data2"));
                            news.setButton2(btn);
                        }
                        news.setCategory(rs.getByte("category"));
                        news.setContent(rs.getString("content"));
                        news.setIcon(rs.getString("icon"));
                        news.setImage(rs.getString("image"));
                        news.setImageLarge(rs.getString("image_large"));
                        news.setTitle(rs.getString("title"));
                        news.setStartTime(rs.getTimestamp("start_time").getTime());
                        news.setEndTime(rs.getTimestamp("end_time").getTime());
                        news.setPopup(rs.getBoolean("popup"));
                        list.add(news);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.loadAllNews", e);
        }
        return list;
    }

    /**
     * thêm tin tức mới
     * @param news
     */
    public void createNews(News news) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO `sfs_news` (`title`, `content`, `icon`, `image`, `image_large`, `button1`, `type1`, `data1`,"
                    + " `button2`, `type2`, `data2`, `category`, `popup`, `start_time`, `end_time`, `creator`)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, news.getTitle());
                ps.setString(2, news.getContent());
                ps.setString(3, news.getIcon());
                ps.setString(4, news.getImage());
                ps.setString(5, news.getImageLarge());
                if (news.getButton1() != null) {
                    ps.setString(6, news.getButton1().getCaption());
                    ps.setByte(7, news.getButton1().getType());
                    ps.setString(8, news.getButton1().getData());
                } else {
                    ps.setString(6, "");
                    ps.setByte(7, (byte) 0);
                    ps.setString(8, "");
                }
                if (news.getButton2() != null) {
                    ps.setString(9, news.getButton2().getCaption());
                    ps.setByte(10, news.getButton2().getType());
                    ps.setString(11, news.getButton2().getData());
                } else {
                    ps.setString(9, "");
                    ps.setByte(10, (byte) 0);
                    ps.setString(11, "");
                }
                ps.setByte(12, news.getCategory());
                ps.setBoolean(13, news.isPopup());
                ps.setTimestamp(14, new Timestamp(news.getStartTime()));
                ps.setTimestamp(15, new Timestamp(news.getEndTime()));
                ps.setString(16, news.getCreator());
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.createNews", e);
        }
    }

    /**
     * cập nhật tin tức (tiêu đề, nội dung, loại ...)
     * @param news
     */
    public void updateNews(News news) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE `sfs_news` SET `title`=?, `content`=?, `icon`=?, `image`=?, `image_large`=?, `button1`=?, `type1`=?, `data1`=?, `button2`=?, `type2`=?, `data2`=?, `category`=?, `popup`=?, `start_time`=?, `end_time`=? "
                    + "WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, news.getTitle());
                ps.setString(2, news.getContent());
                ps.setString(3, news.getIcon());
                ps.setString(4, news.getImage());
                ps.setString(5, news.getImageLarge());
                if (news.getButton1() != null) {
                    ps.setString(6, news.getButton1().getCaption());
                    ps.setByte(7, news.getButton1().getType());
                    ps.setString(8, news.getButton1().getData());
                } else {
                    ps.setString(6, "");
                    ps.setByte(7, (byte) 0);
                    ps.setString(8, "");
                }
                if (news.getButton2() != null) {
                    ps.setString(9, news.getButton2().getCaption());
                    ps.setByte(10, news.getButton2().getType());
                    ps.setString(11, news.getButton2().getData());
                } else {
                    ps.setString(9, "");
                    ps.setByte(10, (byte) 0);
                    ps.setString(11, "");
                }
                ps.setByte(12, news.getCategory());
                ps.setBoolean(13, news.isPopup());
                ps.setTimestamp(14, new Timestamp(news.getStartTime()));
                ps.setTimestamp(15, new Timestamp(news.getEndTime()));
                ps.setInt(16, news.getId());
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateNews", e);
        }
    }

    /**
     * xóa tin tức
     * @param id
     */
    public void deleteNews(int id) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "DELETE FROM `sfs_news` WHERE id=? LIMIT 1;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.deleteNews", e);
        }
    }

    /**
     * Update thông tin tournament
     * @param sumMoney
     * @param serviceId
     */
    public void updateTournament(int serviceId, double sumMoney) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO sfs_tournament(serviceId,summoney) VALUES(?,?) ON DUPLICATE KEY UPDATE summoney=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, serviceId);
                ps.setDouble(2, sumMoney);
                ps.setDouble(3, sumMoney);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateTournament", e);
        }
    }

    /**
     * Lấy ra tất cả danh sách tournament
     * @return
     */
    public List<SumMoneyByBetBoard> getTournaments(){
        List<SumMoneyByBetBoard> sumMoneys = new ArrayList();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `sfs_tournament`";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SumMoneyByBetBoard  bonus = new SumMoneyByBetBoard();
                        bonus.setServiceId(rs.getByte("serviceId"));
                        bonus.setSumMoney(rs.getDouble("summoney"));
                        sumMoneys.add(bonus);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getTournaments", e);
        }
        return sumMoneys;
    }

    /**
     * mua tẩy vào bàn, chuyển tiền từ bảng chính qua bảng tạm
     *
     * @param userId
     * @param value
     * @return
     */
    private UpdateMoneyResult callBuyStackUSDProcedure(String userId, BigDecimal value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_buyin_money_return_stack(?, ?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.registerOutParameter(5, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
                umr.stack = call.getBigDecimal(5);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callBuyStackUSDProcedure " + userId + " - " + value.toString(), e);
        }
        return umr;
    }

    /**
     * mua tẩy vào bàn, chuyển tiền từ bảng chính qua bảng tạm
     *
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callBuyStackMoneyProcedure(String userId, BigDecimal value) {
        return callBuyStackUSDProcedure(userId, value);
    }

    /**
     * mua tẩy vào bàn, chuyển điểm từ bảng chính qua bảng tạm
     *
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callBuyStackPointProcedure(String userId, BigDecimal value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_buyin_point_return_stack(?, ?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.registerOutParameter(5, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
                umr.stack = call.getBigDecimal(5);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callBuyStackPointProcedure " + userId + " - " + value.toString(), e);
        }
        return umr;
    }

    /**
     * Get điểm của user trong bàn
     *
     * @param userId
     * @return
     */
    public double getUserPointStack(String userId) {
        double point = 0;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `point` FROM `sfs_user_point_stack` WHERE `user_id` = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        point = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserPointStack", e);
        }
        return point;
    }

    /**
     * get tiền của user trong bàn
     *
     * @param userId
     * @return
     */
    public double getUserMoneyStack(String userId) {
        return getUserUSDStack(userId);
    }

    /**
     * get tiền của user trong bàn
     *
     * @param userId
     * @return
     */
    private double getUserUSDStack(String userId) {
        double money = 0;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `money` FROM `sfs_user_money_stack` WHERE `user_id` = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        money = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserUSDStack error:", e);
        }
        return money;
    }

    /**
     * cập nhật tiền của user trong bàn
     *
     * @param userId
     * @param value
     * @return
     */
    private UpdateMoneyResult callUpdateUSDStackProcedure(String userId, BigDecimal value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_update_money_stack(?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callUpdateUSDStackProcedure " + userId + " - " + value.toString(), e);
        }
        return umr;
    }

    /**
     * cập nhật điểm của user trong bàn chơi
     *
     * @param userId
     * @param value
     * @return
     */
    public UpdateMoneyResult callUpdatePointStackProcedure(String userId, BigDecimal value) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_update_point_stack(?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, value);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(3);
                umr.after = call.getBigDecimal(4);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callUpdatePointStackProcedure " + userId + " - " + value.toString(), e);
        }
        return umr;
    }

    /**
     *
     * @param userId
     * @return
     */
    private UpdateMoneyResult callCashoutUSDProcedure(String userId) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_cashout_money(?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.registerOutParameter(2, Types.DECIMAL);
                call.registerOutParameter(3, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(2);
                umr.after = call.getBigDecimal(3);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callCashoutUSDProcedure " + userId, e);
        }
        return umr;
    }

    /**
     *
     * @param userId
     * @return
     */
    public UpdateMoneyResult callCashoutPointProcedure(String userId) {
        UpdateMoneyResult umr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_cashout_point(?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.registerOutParameter(2, Types.DECIMAL);
                call.registerOutParameter(3, Types.DECIMAL);
                call.executeUpdate();
                umr = new UpdateMoneyResult();
                umr.before = call.getBigDecimal(2);
                umr.after = call.getBigDecimal(3);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callCashoutPointProcedure " + userId, e);
        }
        return umr;
    }

    /**
     * trả tiền từ stack vào tong tien cua user
     * @param userId
     * @return
     */
    public UpdateMoneyResult callCashoutMoneyStackProcedure(String userId) {
        return callCashoutUSDProcedure(userId);
    }

    /**
     *
     * @param userId
     * @return lay user id tu db len, neu chua co record lay avatart default -1
     */
    public int getUserAvatarId(String userId) {
        int userAvatarId = -1;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT avatar_id FROM sfs_user_avatar WHERE user_id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userAvatarId = rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserAvatarId error", e);
        }
        return userAvatarId;
    }

    /**
     * get total bot money, input money, stack money
     * @param serviceId
     * @return
     */
    public BotMoneyResult getTotalBotMoney(int serviceId) {
        BotMoneyResult rs = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_get_total_bot_money(?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setInt(1, serviceId);
                call.registerOutParameter(2, Types.DECIMAL);
                call.registerOutParameter(3, Types.DECIMAL);
                call.registerOutParameter(4, Types.DECIMAL);
                call.executeUpdate();
                rs = new BotMoneyResult();
                rs.inputMoney = call.getBigDecimal(2);
                rs.money = call.getBigDecimal(3);
                rs.stackMoney = call.getBigDecimal(4);
            }
        } catch (Exception e) {
            LOGGER.error("Database.getBotTotalMoney", e);
        }
        return rs;
    }
    
    /**
     *
     * @param userId
     * @return
     */
    public int getUserType(String userId) {
        int type = 0;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `user_type` FROM `sfs_user` WHERE `id` = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        type = rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getUserType", e);
        }
        return type;
    }

    /**
     * lấy số tiền user CÓ THỂ rút/chuyển
     * @param userId
     * @return
     */
    public double getUserAvailableWithdrawMoney(String userId) {
        double money = 0;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `quota` FROM `sfs_user_withdraw_info` WHERE `user_id` = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        money = rs.getDouble(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getUserWithdrawMoney", e);
        }
        return money;
    }
    
    public void addUserAvailableWithdrawMoney(String userId, BigDecimal quota) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO sfs_user_withdraw_info (user_id, quota) VALUES (?, ?) ON DUPLICATE KEY UPDATE quota = quota + ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setBigDecimal(2, quota);
                ps.setBigDecimal(3, quota);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.addUserAvailableWithdrawMoney error", e);
        }
    }
    
    public void updateUserAvailableWithdrawMoney(String userId, BigDecimal moneyBefore) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE sfs_user_withdraw_info SET quota = ? WHERE `user_id` = ? AND quota > ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, moneyBefore);
                ps.setString(2, userId);
                ps.setBigDecimal(3, moneyBefore);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("checkUserAvailableWithdrawMoney", e);
        }
    }

    /**
     * lấy thông tin version config
     * @param bundleIdClient
     * @param platformClient
     * @param versionClient
     * @return
     */
    public ClientVersionUpdate getVersionConfig(String bundleIdClient, String platformClient, String versionClient) {
        ClientVersionUpdate cvu = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM UPDATE_CLIENT_CONFIG WHERE versions LIKE ? AND platform = ? AND bundleId = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + versionClient + "%");
                ps.setString(2, platformClient);
                ps.setString(3, bundleIdClient);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cvu = new ClientVersionUpdate();
                        cvu.setBundleId(rs.getString("bundleId"));
                        cvu.setPlatform(rs.getString("platform"));
                        cvu.setVersions(rs.getString("versions"));
                        cvu.setAction(rs.getInt("action"));
                        cvu.setLink(rs.getString("link_update"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getVersionConfig", e);
        }
        return cvu;
    }

    public void addRechargeCard(String userId, String telco, String serial, String pin, double price, String transaction, String result) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO USER_SCRATCH_CARD_LOG (user_id, telco, pin, series, price, `transaction`, `result`) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, telco);
                ps.setString(3, pin);
                ps.setString(4, serial);
                ps.setDouble(5, price);
                ps.setString(6, transaction);
                ps.setString(7, result);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.addRechargeCard error", e);
        }
    }

    public void insertCCULog(String data) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO sfs_ccu_log (data) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.insertCCULog error", e);
        }
    } 
    
    public PointConvertConfig getPointConvertConfig(){
        PointConvertConfig wcc  = new PointConvertConfig();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM sfs_point_convert_config";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        wcc.setTurnOver(rs.getInt("turn_over"));
                        wcc.setConvertRate(rs.getInt("convert_rate"));
                        wcc.setEnable(rs.getBoolean("enable"));
                        wcc.setEnableAutoConvert(rs.getBoolean("enable_auto_convert"));
                        wcc.setConvertLimitPerDay(rs.getBigDecimal("convert_limit_per_day"));
                        wcc.setMinConvertPerTime(rs.getBigDecimal("min_convert_per_time"));
                        wcc.setMaxConvertPerTime(rs.getBigDecimal("max_convert_per_time"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getWinConvertConfig", e);
        }
        return wcc;
    }

    public ConvertMoneyResult callConvertMoneyResult(String userId, BigDecimal subPoint, BigDecimal addMoney) {
        ConvertMoneyResult cmr = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_convert_point_2_money(?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setBigDecimal(2, subPoint);
                call.setBigDecimal(3, addMoney);
                call.registerOutParameter(4, Types.DECIMAL);
                call.registerOutParameter(5, Types.DECIMAL);
                call.registerOutParameter(6, Types.DECIMAL);
                call.registerOutParameter(7, Types.DECIMAL);
                call.executeUpdate();
                cmr = new ConvertMoneyResult();
                cmr.pointBefore = call.getBigDecimal(4);
                cmr.pointAfter = call.getBigDecimal(5);
                cmr.moneyBefore = call.getBigDecimal(6);
                cmr.moneyAfter = call.getBigDecimal(7);
            }
        } catch (Exception e) {
            LOGGER.error("Database.callConvertMoneyResult " + userId + " " + subPoint.toString() + " " + addMoney.toString(), e);
        }
        return cmr;
    }

    public BigDecimal getPointConvertQuota(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT quota FROM sfs_user_convert_quota WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("quota");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getQuota", e);
        }
        return BigDecimal.ZERO;
    }

    public void addPointConvertQuota(String userId, BigDecimal quota) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO sfs_user_convert_quota (user_id, quota) VALUES (?, ?) ON DUPLICATE KEY UPDATE quota = quota + ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setBigDecimal(2, quota);
                ps.setBigDecimal(3, quota);

                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.addPointConvertQuota", e);
        }
    }

    public void updatePointConvertQuota(String userId, BigDecimal quota) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE sfs_user_convert_quota SET quota = ? WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, quota);
                ps.setString(2, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePointConvertQuota", e);
        }
    }
    
    public BotAdvantage getBotAdvance(int serviceId, byte moneyType) {
        BotAdvantage botAdv = new BotAdvantage();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM sfs_bot_advantage WHERE service_id = ? AND money_type = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, serviceId);
                ps.setByte(2, moneyType);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        botAdv.setAdvRatio(rs.getInt("adv_ratio"));
                        botAdv.setMinPoint(rs.getInt("min_point"));
                        botAdv.setEnable(rs.getBoolean("enable"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getBotAdvance", e);
        }
        return botAdv;
    }

    public BotAdvantage getBotAdvance(String userId) {
        BotAdvantage botAdv = new BotAdvantage();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT first_rate, advantage FROM sfs_bot_advantage_group"
                    + " JOIN sfs_user ON sfs_user.adv_group = sfs_bot_advantage_group.id"
                    + " WHERE sfs_user.id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        botAdv.setAdvRatio(rs.getInt("advantage"));
                        botAdv.setFirstRatio(rs.getInt("first_rate"));
                        botAdv.setEnable(botAdv.getAdvRatio() > 0 || botAdv.getFirstRatio() > 0);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getBotAdvance", e);
        }
        return botAdv;
    }
    
    public BotAdvantage getBotAdvance(int groupId) {
        BotAdvantage botAdv = new BotAdvantage();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT first_rate, advantage FROM sfs_bot_advantage_group WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        botAdv.setAdvRatio(rs.getInt("advantage"));
                        botAdv.setFirstRatio(rs.getInt("first_rate"));
                        botAdv.setEnable(botAdv.getAdvRatio() > 0 || botAdv.getFirstRatio() > 0);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getBotAdvance", e);
        }
        return botAdv;
    }
    
    public int getBotAdvanceGroupId(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT adv_group FROM sfs_user WHERE id=? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return 0;
    }

    /**
     *
     * @param userId
     * @return số tiền đã đổi được từ điểm trong ngày
     */
    public BigDecimal getMoneyReceiveFromPointInfo(String userId) {
        BigDecimal money = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT SUM(money_receive) FROM sfs_approval_convert_point "
                    + "WHERE user_id = ? AND DATE(create_time) = CURDATE() AND (`status` = ? OR `status` = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setInt(2, Transaction.STATUS_SUCCESS);
                ps.setInt(3, Transaction.STATUS_PENDING);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        money = rs.getBigDecimal(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getMoneyReceiveFromPointInfo", e);
        }
        if (money == null) {
            return BigDecimal.ZERO;
        }
        return money;
    }
    
    public void insertApprovalConvertPoint(String id, String userId, String email, BigDecimal pointConvert, String channel, BigDecimal moneyReceive, byte status) {
        try (Connection conn = dbManager.getConnection()) {
            String sql;
            if (status == Transaction.STATUS_SUCCESS) {
               sql = "INSERT INTO sfs_approval_convert_point (id, user_id, email, point_convert, channel, money_receive, status, decided_by, decided_time) VALUES (?,?,?,?,?,?,?, 'Auto', NOW())"; 
            } else {
               sql = "INSERT INTO sfs_approval_convert_point (id, user_id, email, point_convert, channel, money_receive, status) VALUES (?,?,?,?,?,?,?)"; 
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, userId);
                ps.setString(3, email);
                ps.setBigDecimal(4, pointConvert);
                ps.setString(5, channel);
                ps.setBigDecimal(6, moneyReceive);
                ps.setByte(7, status);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.insertApprovalConvertPoint error", e);
        }
    }
    
    public void insertReceiveMoneyOffline(String from, String to, BigDecimal money) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO receive_money_offline (from_user, to_user, receive_money) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, from);
                ps.setString(2, to);
                ps.setBigDecimal(3, money);
                
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.insertReceiveMoneyOffline error", e);
        }
    }
    
    public List<UserReceiveMoneyOffline> getMoneyReceiveOffline(String userId) {
        List<UserReceiveMoneyOffline> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT from_user, SUM(receive_money) FROM receive_money_offline WHERE to_user = ? GROUP BY from_user";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UserReceiveMoneyOffline userReceive = new UserReceiveMoneyOffline();
                        userReceive.setFromUser(rs.getString(1));
                        userReceive.setMoney(rs.getBigDecimal(2));

                        list.add(userReceive);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getMoneyReceiveOffline", e);
        }
        return list;
    }
    
    public void deleteMoneyReceiveOffline(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "DELETE FROM receive_money_offline WHERE to_user = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.deleteMoneyReceiveOffline", e);
        }
    }
    
    public String getUserPlatform(String userId) {
        String platform = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `platform` FROM `sfs_user` where `id` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        platform = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserPlatform", e);
        }
        return platform;
    }
    
    public void updatePlatform(String userId, String platform) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sfs_user SET platform = ? WHERE id = ?")) {
                ps.setString(1, platform);
                ps.setString(2, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updatePlatform", e);
        }
    }
    
    public String getUserChannel(String userId) {
        String channel = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `channel` FROM `sfs_user` where `id` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        channel = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserChannel", e);
        }
        return channel;
    }
    
    public void updateChannel(String userId, String channel) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sfs_user SET channel = ? WHERE id = ?")) {
                ps.setString(1, channel);
                ps.setString(2, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateChannel", e);
        }
    }
    
    public boolean checkEvent() {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `id` FROM casino_api.`CATEGORY_QUEST_CONFIG` WHERE NOW() BETWEEN startdate AND enddate AND TYPE = 1 LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.checkEvent", e);
        }
        return false;
    }
    
    public void updateUserType(String userId, int userType) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "{CALL sfs_update_user_type(?, ?)}";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setString(1, userId);
                call.setInt(2, userType);                
                call.executeUpdate(); 
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateUserType", e);
        }
    }
    
    public void updateFacebookInfo(String facebookId, String userId, String displayName, String avatar) {
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sfs_user SET `facebook_id` = ?, `display_name` = ?, `avatar` = ? WHERE id = ?")) {
                ps.setString(1, facebookId);
                ps.setString(2, displayName);
                ps.setString(3, avatar);
                ps.setString(4, userId);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateFacebookId", e);
        }
    }
    
    public TransactionHistory getUserTransaction(String userId, int page, int moneyType, String from, String to, int size) {
        TransactionHistory history = new TransactionHistory(userId, page);

        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtil.parseString(to, "yyyy-MM-dd"));
        String sql = "";
        // lấy trong 2 tháng gần nhất
        for (int i = 0; i < 2; i++) {
            if (!sql.isEmpty()) {
                sql += " UNION ";
            }
            sql += String.format("SELECT * FROM casino_api.`transaction_log_%s`"
                    + " WHERE `user_id`='%s' AND `money_type`=%d AND '%s' <= DATE(`log_time`) AND DATE(`log_time`) <= '%s'",
                    DateUtil.getDateString(cal, "yyyyMM"), userId, moneyType, from, to);
            cal.add(Calendar.MONTH, -1);
        }
        sql += " ORDER BY `log_time` DESC, `id` DESC LIMIT ? OFFSET ?";

        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, size);
                ps.setInt(2, size * page);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Transaction tx = new Transaction(rs.getString("id"));
                        tx.setMoney(rs.getDouble("money"));
                        tx.setValue(rs.getDouble("value"));
                        tx.setTime(rs.getTimestamp("log_time").getTime());
                        tx.setType(rs.getInt("type"));
                        tx.setStatus(rs.getByte("status"));
                        history.addTransaction(tx);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserTransaction", e);
        }
        return history;
    }
    
    public void logTransaction(String id, String userId, byte moneyType, double value, double money, byte type, byte status){
        try (Connection conn = dbManager.getConnection()) {
            if (id == null) {
                id = Utils.md5String(userId + moneyType + value + money + type + status + System.currentTimeMillis()).substring(0, 20);
            }
            String sql = String.format("INSERT INTO casino_api.transaction_log_%s (id, user_id, value, money, type, money_type, status) VALUES (?, ?, ?, ?, ?, ?, ?);",
                    DateUtil.getDateString(new Date(), "yyyyMM"));
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, userId);
                ps.setDouble(3, value);
                ps.setDouble(4, money);
                ps.setByte(5, type);
                ps.setInt(6, moneyType);
                ps.setByte(7, status);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("logTransaction", e);
        }
    }
    
    /**
     *
     * @param userId
     * @param type
     * @return thông tin nhận điểm từ lần cuối cùng trong ngày
     */
    public PointReceiveInfo getTimeReceivePointInfo(String userId, byte type) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT time_receive, receive_count FROM sfs_point_last_receive"
                    + " WHERE user_id = ? AND type = ? AND DATE(time_receive)=CURDATE() LIMIT 1;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setByte(2, type);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Date time_receive = new Date(rs.getTimestamp(1).getTime());
                        int receive_count = rs.getInt(2);
                        return new PointReceiveInfo(userId, type, time_receive, receive_count);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getTimeReceivePointInfo error", e);
        }
        return null;
    }

    /**
     * cập nhật thời gian nhận điểm, số lần nhận free
     * @param userId
     * @param type
     * @param newCount
     */
    public void updateInfoReceivePoint(String userId, byte type, int newCount) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO sfs_point_last_receive (user_id, type) VALUES (?, ?)"
                    + " ON DUPLICATE KEY UPDATE time_receive = NOW(),receive_count = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setByte(2, type);
                ps.setInt(3, newCount);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Database.updateInfoReceivePoint error", e);
        }
    }
    
    public List<ChargeCardInfo> getChargeCardInfo(boolean cardPromotion) {
        List<ChargeCardInfo> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sfs_charge_card_info")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChargeCardInfo info = new ChargeCardInfo();
                        int promotion = cardPromotion ? rs.getInt("promotion") : 0;
                        info.setPromotion(promotion);
                        info.setTelco(rs.getInt("telco"));
                        info.setVnd(rs.getInt("vnd"));
                        int win = rs.getInt("win") * (100 + promotion) / 100;
                        info.setWin(win);
                        list.add(info);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return list;
    }

    public List<ChargeBankingInfo> getChargeBankingInfo(boolean mbankPromotion, boolean eeziepayPromotion) {
        List<ChargeBankingInfo> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sfs_charge_banking_info")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChargeBankingInfo info = new ChargeBankingInfo();
                        info.setBank(rs.getInt("bank"));
                        info.setVndFrom(rs.getInt("vnd_from"));
                        info.setVndTo(rs.getInt("vnd_to"));
                        info.setType(rs.getByte("type"));
                        int promotion = 0;
                        if (info.getType() == ChargePromotionSchedule.TYPE_MBANK && mbankPromotion) {
                            if (mbankPromotion) {
                                promotion = rs.getInt("promotion");
                            }
                        } else {
                            if (eeziepayPromotion) {
                                promotion = rs.getInt("promotion");
                            }
                        }
                        info.setPromotion(promotion);
                        double price = rs.getDouble("price") * (100 + promotion) / 100;
                        info.setPrice(price);
                        list.add(info);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return list;
    }

    public List<ChargePromotionSchedule> getChargePromotionSchedule() {
        List<ChargePromotionSchedule> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sfs_charge_promotion_schedule WHERE enable = 1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChargePromotionSchedule schedule = new ChargePromotionSchedule();
                        schedule.setId(rs.getInt("id"));
                        schedule.setType(rs.getByte("type"));
                        schedule.setRepeat(rs.getByte("repeat"));
                        if (rs.getDate("start_date") != null) {
                            schedule.setStartDate(DateUtil.getDateString(rs.getDate("start_date"), "yyyy-MM-dd"));
                        }
                        if (rs.getDate("end_date") != null) {
                            schedule.setEndDate(DateUtil.getDateString(rs.getDate("end_date"), "yyyy-MM-dd"));
                        }
                        schedule.setEnable(rs.getBoolean("enable"));
                        String[] tmp = rs.getString("time").split(";");
                        List listTime = new ArrayList();
                        for (String s : tmp) {
                            String[] ss = s.split("-");
                            ChargePromotionTime promotionTime = new ChargePromotionTime(ss[0], ss[1]);
                            listTime.add(promotionTime);
                        }
                        schedule.setTime(listTime);
                        list.add(schedule);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getChargePromotionSchedule", e);
        }
        return list;
    }

    public String getUserCurrency(String userId) {
        String currency = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `currency` FROM `sfs_user` where `id` = ? ";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currency = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Database.getUserCurrency error", e);
        }
        return currency;
    }
    
    public int getGameId(String userId, int moneyType) {
        try (Connection conn = dbManager.getConnection()) {
            String suffix = DateUtil.getDateString(new Date(), "yyyyMM");
            String sql;
            if (moneyType == MoneyContants.MONEY) {
                sql = "SELECT `requestId` FROM casino_api.api_money_log_" + suffix + " WHERE `userid` = ? AND `requestId` IS NOT NULL";
            } else {
                sql = "SELECT `requestId` FROM casino_api.api_point_log_" + suffix + " WHERE `userid` = ? AND `requestId` IS NOT NULL";
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String gameId = rs.getString(1);
                        if (!gameId.isEmpty()) {
                            return Integer.parseInt(gameId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Database.getGameId error", e);
        }
        return Integer.MIN_VALUE;
    }
    
    public int getUserTypeByEmail(String email) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT user_type FROM sfs_user WHERE email = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return 0;
    }
    
    public String getUserMerchantId(String userId) {
        String merchantId = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT `merchant_id` FROM `sfs_user` where `id` = ? ";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        merchantId = rs.getString(1);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return merchantId;
    }
    
    public UserInfo getUserInfo(String email) {
        UserInfo userInfo = null;
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `sfs_user` where `email` = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userInfo = new UserInfo();
                        userInfo.setUserId(rs.getString("id"));
                        userInfo.setAvatar(rs.getString("avatar"));
                        userInfo.setCurrency(rs.getString("currency"));
                        userInfo.setDisplayName(rs.getString("display_name"));
                        userInfo.setMerchantId(rs.getString("merchant_id"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return userInfo;
    }

    public P2PTransferConfig getTransferConfig() {
        P2PTransferConfig transferConfig = new P2PTransferConfig();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM sfs_transfer_config";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        transferConfig.setTransferFee(rs.getDouble("transfer_fee"));
                        transferConfig.setMinPerTrans(rs.getBigDecimal("min_per_trans"));
                        transferConfig.setMaxPerTrans(rs.getBigDecimal("max_per_trans"));
                        transferConfig.setMaxPerDay(rs.getBigDecimal("max_per_day"));
                        transferConfig.setEnable(rs.getBoolean("enable"));
                        transferConfig.setTransferFeeAgent(rs.getDouble("transfer_fee_agent"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return transferConfig;
    }

    public BigDecimal getCurDateTranferMoney(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = String.format("SELECT SUM(`money`) FROM casino_api.transfer_log_%s WHERE from_user_id = ? AND DATE(time) = CURDATE()", DateUtil.getDateString(new Date(), "yyyyMM"));
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal money = rs.getBigDecimal(1);
                        if (money != null) {
                            return money;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return BigDecimal.ZERO;
    }
    
    public List<Reward> getRewards(){
        List<Reward> rewards = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `tlmn_spin_go_ticket`";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Reward reward = new Reward();
                        reward.setId(rs.getInt("id"));
                        reward.setBet(rs.getDouble("bet"));
                        reward.setFund(rs.getInt("fund"));
                        rewards.add(reward);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getRewards() error", e);
        }
        return rewards;
    }
    
    public List<RewardMulti> getRewardMultis(int id){
        List<RewardMulti> rewardMultis = new ArrayList<>();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT * FROM `tlmn_spin_go_reward` where `ticket_id` = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        RewardMulti rewardMulti = new RewardMulti();
                        rewardMulti.setId(rs.getInt("id"));
                        rewardMulti.setFrequency(rs.getDouble("frequency"));
                        rewardMulti.setMultiplier(rs.getInt("multi"));
                        rewardMulti.setPlace_1st(rs.getDouble("place_1st"));
                        rewardMultis.add(rewardMulti);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("getRewardMultis() error", e);
        }
        
        return rewardMultis;
    }
    
    public void setBotOnline(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO casino_api.bot_online_time (user_id) VALUES (?) ON DUPLICATE KEY UPDATE time_offline = NULL";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public void updateBotOnline(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE casino_api.bot_online_time SET time_offline = NOW() WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public boolean checkVerify(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "SELECT id FROM sfs_user WHERE id = ? AND verify = 1 LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }

            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return false;
    }
    
    public void updateVerify(String userId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "UPDATE sfs_user SET verify = 1 WHERE id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

    }
    
    public void insertTranferLog(String fromUserId, String toUserId, BigDecimal money, BigDecimal fee) {
        try (Connection conn = dbManager.getConnection()) {
            String date = DateUtil.getDateString(Calendar.getInstance(), "yyyyMM");
            String sql = String.format("INSERT INTO casino_api.transfer_log_%s (from_user_id, to_user_id, money, fee) VALUES (?, ?, ?, ?)", date);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fromUserId);
                ps.setString(2, toUserId);
                ps.setBigDecimal(3, money);
                ps.setBigDecimal(4, fee);

                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public void insertTax(String userId, int serviceId, BigDecimal tax) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "INSERT INTO sfs_user_tax (user_id, service_id, tax) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setInt(2, serviceId);
                ps.setBigDecimal(3, tax);
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public JsonArray getTop(int serviceId) {
        JsonArray arr = new JsonArray();
        try (Connection conn = dbManager.getConnection()) {
            String sql = "CALL `sfs_get_top`(?)";
            try (CallableStatement call = conn.prepareCall(sql)) {
                call.setInt(1, serviceId);
                try (ResultSet rs = call.executeQuery()) {
                    int order = 1;
                    while (rs.next()) {
                        JsonObject json = new JsonObject();
                        json.addProperty("order", order++);
                        json.addProperty("userId", rs.getString("user_id"));
                        json.addProperty("username", rs.getString("username"));
                        json.addProperty("tax", rs.getBigDecimal("tax"));
                        arr.add(json);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return arr;
    }
}
