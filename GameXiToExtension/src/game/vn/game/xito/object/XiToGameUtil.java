/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * compare bài của 2 user
 * @author tuanp
 */
public class XiToGameUtil {
    /**
     * trả về player có điểm cao hơn, nếu điểm = nhau thì lấy con có id lớn nhất
     * để so
     *
     * @param player1
     * @param player2
     * @return
     * @throws Exception
     */
    public static XiToPlayer comparePlayerScore(XiToPlayer player1, XiToPlayer player2) throws Exception {
        if (player2 == null) {
            return player1;
        }

        if (player1.getResultCards().getValue() > player2.getResultCards().getValue()) {
            return player1;
            //bai ngang nhau, xet con lon nhat
        } else if (player1.getResultCards().getValue() == player2.getResultCards().getValue()) {

            List<XiToCard> list1 = new ArrayList<>(player1.getHoldCards().values());
            Collections.sort(list1);
            List<XiToCard> list2 = new ArrayList<>(player2.getHoldCards().values());
            Collections.sort(list2);

            //xét trường hợp là 2 đôi
            //trường hợp giá trị lớn nhất
            if (player1.getResultCards().getHighestCard().getCardNumber()
                    > player2.getResultCards().getHighestCard().getCardNumber()) {
                return player1;
            } else if (player1.getResultCards().getHighestCard().getCardNumber()
                    < player2.getResultCards().getHighestCard().getCardNumber()) {
                return player2;
            } else {
                //kiểm tra trường hợp user có 2 đôi thì xét cả hai đôi
                if (player1.getResultCards().getValue() == XiToCardUtil.TWO_PAIRS_VALUE) {
                    if (player1.getResultCards().getHighestCards().get(0).getCardNumber()
                            > player2.getResultCards().getHighestCards().get(0).getCardNumber()) {
                        return player1;
                    } else if (player1.getResultCards().getHighestCards().get(0).getCardNumber()
                            < player2.getResultCards().getHighestCards().get(0).getCardNumber()) {
                        return player2;
                    }
                }
                //xet truong hợp bài không giống nhau (5 lá không giống nhau)xet tu con lớn đến nhỏ nhất
                for (int i = 4; i >= 0; i--) {
                    if (list1.get(i).getCardNumber() > list2.get(i).getCardNumber()) {
                        return player1;
                    } else if (list1.get(i).getCardNumber() < list2.get(i).getCardNumber()) {
                        return player2;
                    }
                }
            }

            /* kiem tra trường hợp  khi đã xét 5 lá bài đều giống nhau mới thực hiện xét chất lớn nhất
             (vi dụ đôi bằng nhau thì set chat cua hai doi)
             */
            if (player1.getResultCards().getHighestCard().getId()
                    > player2.getResultCards().getHighestCard().getId()) {
                return player1;
            }
            return player2;
        }
        return player2;
    }    
}
