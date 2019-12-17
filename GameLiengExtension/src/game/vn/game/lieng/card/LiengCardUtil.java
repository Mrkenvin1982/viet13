/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.card;

import game.vn.game.lieng.lang.LiengLanguage;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class LiengCardUtil{

    public static final byte SAP = 3;
    public static final byte LIENG = 2;
    public static final byte ANH = 1;
    public static final byte SCORE = 0;

    /**
     * Sáp: 3 cây cùng số
     *
     * @param list
     * @return
     */
    private static boolean isSap(List<LiengCard> list) {
        Collections.sort(list);
        if (list.get(0).getCardNumber() == list.get(1).getCardNumber()
                && list.get(0).getCardNumber() == list.get(2).getCardNumber()) {
            return true;
        }
        return false;
    }

    /**
     * Liêng: 3 cây có số liên tiếp (chấp nhận bộ Liêng A, 2, 3 và bộ Q, K, A)
     *
     * @param list
     * @return
     */
    private static boolean isLieng(List<LiengCard> list) {
        Collections.sort(list);
        if (list.get(0).getCardNumber() == list.get(1).getCardNumber() - 1
                && list.get(1).getCardNumber() == list.get(2).getCardNumber() - 1) {
            return true;
        }
        //trường hợp lieng A - 2 - 3
        if (list.get(2).isAce() && list.get(0).getCardNumber() == 2 && list.get(1).getCardNumber() == 3) {
            Collections.sort(list, new LiengCardComparator());
            return true;
        }
        return false;
    }

    /**
     * 3 cây toàn đầu người (J, Q, K), không tính bộ liêng J, Q, K
     *
     * @param list
     * @return
     */
    private static boolean isAnh(List<LiengCard> list) {
        Collections.sort(list);
        for (LiengCard card : list) {
            if (!card.isHuman()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Các trường hợp còn lại sẽ tính điểm: bằng số dư của tổng điểm của 3 cây
     * trong phép chia cho 10. Trong đó các cây có số 2->10 tính theo điểm tương
     * ứng với số. Cây A tính 1 điểm, các cây J, Q, K đều tính 0 điểm. Ví dụ bài
     * 3 cây 3 Cơ, 5 Rô, Q Bích có điểm là 8. Bài có điểm cao hơn sẽ mạnh hơn (9
     * điểm mạnh nhất)
     *
     * @param list
     * @return
     */
    private static byte getScore(List<LiengCard> list) {
        int score = 0;
        for (LiengCard card : list) {
            if (card.isHuman()) {
                continue;
            }
            if (card.isAce()) {
                score++;
            } else {
                score += card.getCardNumber();
            }
        }
        return (byte) (score % 10);
    }

    /**
     *
     * @param list
     * @return
     */
    public static ResultCard getResult(List<LiengCard> list, Locale locale) {
        ResultCard result = new ResultCard();
        if (list.size() < 3){
            return result;
        }
        if (isSap(list)) {
            result.setValue(SAP);
            result.setStrValue(LiengLanguage.getMessage(LiengLanguage.SAP, locale));
            result.setScore(list.get(2).getId());
        } else if (isLieng(list)) {
            result.setValue(LIENG);
            result.setStrValue(LiengLanguage.getMessage(LiengLanguage.LIENG, locale));
            result.setScore(list.get(2).getId());
        } else if (isAnh(list)) {
            result.setValue(ANH);
            result.setStrValue(LiengLanguage.getMessage(LiengLanguage.ANH, locale));
            result.setScore(list.get(2).getId());
        } else {
            result.setValue(SCORE);
            byte score = getScore(list);
            Collections.sort(list, new LiengCardComparator());
            String strValue = String.format(LiengLanguage.getMessage(LiengLanguage.SCORE, locale), score);
            result.setStrValue(strValue);
            result.setScore(score);
        }
        return result;
    }
    /**
     * kiểm tra trong bài Liêng có đôi
     * @param list
     * @return 
     */
    public static boolean havePair(List<LiengCard> list) {
        Collections.sort(list);
        if (list.size() != 3) {
            return false;
        }
        if (list.get(0).getCardNumber() == list.get(1).getCardNumber() || list.get(1).getCardNumber() == list.get(2).getCardNumber()) {
            return true;
        }
        return false;
    }
}

