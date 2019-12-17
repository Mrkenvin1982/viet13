/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author tuanp
 */
public class BoardLogInGame {

    private static final int MAX_LENGTH = 20000;
    private StringBuilder strBuilderBoardLog;
    private DateFormat dateFormat;

    public BoardLogInGame() {
        strBuilderBoardLog = new StringBuilder();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    /**
     * ghi lại hành động của user
     *
     * @param userName
     * @param moneyOfUser
     * @param action : hành động của user được ghi lại
     * @param money : số tiền đi kèm với action, nếu hành động không phát sinh
     * tiền thì money bằng 0
     */
    public void addLog(String userName, double moneyOfUser, String action, double money) {
        addLog(userName, moneyOfUser, action, money, "");
    }

    /**
     * ghi lại bài và hành động của user
     *
     * @param userName
     * @param moneyOfUser
     * @param action : hành động của user được ghi lại
     * @param money : số tiền đi kèm với action, nếu hành động không phát sinh
     * tiền thì money bằng 0
     * @param listStringCard : bài của user
     */
    public void addLog(String userName, double moneyOfUser, String action, double money, String listStringCard) {
        if (strBuilderBoardLog.length() < MAX_LENGTH) {
            strBuilderBoardLog.append(parseTimeToString(System.currentTimeMillis()))
                    .append(" ")
                    .append(userName)
                    .append(" - have money: ")
                    .append(moneyOfUser)
                    .append(" - action:")
                    .append(action)
                    .append(" ")
                    .append(money > 0 ? money : "")
                    .append(" ")
                    .append(listStringCard.length() > 0 ? listStringCard : "")
                    .append("; ");
        }
    }

    /**
     * lấy log đã ghi
     *
     * @return
     */
    public String getLog() {
        return strBuilderBoardLog.toString();
    }

    /**
     * Xóa log đã ghi
     */
    public void clear() {
        strBuilderBoardLog = new StringBuilder();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    private String parseTimeToString(long milisecons) {
        Date date = new Date(milisecons);
        return dateFormat.format(date);
    }
}
