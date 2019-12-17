/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author minhhnb
 */
public class BettingUtil {

    private static final BigDecimal K5 = new BigDecimal("5000.00");
    private static final BigDecimal K10 = new BigDecimal("10000.00");
    private static final BigDecimal K20 = new BigDecimal("20000.00");
    private static final BigDecimal K50 = new BigDecimal("50000.00");
    private static final BigDecimal K100 = new BigDecimal("100000.00");
    private static final BigDecimal K200 = new BigDecimal("200000.00");
    private static final BigDecimal K500 = new BigDecimal("500000.00");
    private static final BigDecimal M1 = new BigDecimal("1000000.00");
    private static final BigDecimal M2 = new BigDecimal("2000000.00");
    private static final BigDecimal M5 = new BigDecimal("5000000.00");
    private static final BigDecimal M10 = new BigDecimal("10000000.00");
    private static final List<BigDecimal> listBinhDan = Arrays.asList(K5, K10, K20, K50, K100);
    private static final List<BigDecimal> listDaiGia = Arrays.asList(K50, K100, K200, K500, M1);
    private static final List<BigDecimal> listVIP = Arrays.asList(K500, M1, M2, M5, M10);

    private static List<BigDecimal> getListChips(BigDecimal userMoney, BigDecimal boardMoney, List<BigDecimal> listChips) {
        List<BigDecimal> list = new ArrayList<>();
        for (BigDecimal i : listChips) {
            if (i.compareTo(userMoney) <= 0 && i.compareTo(boardMoney) >= 0) {
                list.add(i);
            }
        }
        
        /**
         * kiểm tra user còn tiền có thể bet thì cứ cho user bet
         * */
        if (list.isEmpty() && (userMoney.signum() >= 0)) {
            list.add(boardMoney);
        }
        return list;
    }

    public static List<BigDecimal> getListChips(BigDecimal userMoney, BigDecimal boardMoney) {
        return getListChips(userMoney, boardMoney, listDaiGia);
    }
}
