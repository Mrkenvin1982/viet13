/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack.object;

import game.vn.game.blackjack.lang.BlackJackLanguage;
import game.vn.common.card.object.Card;
import game.vn.util.CommonMoneyReasonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tuanp
 */
public class BlackJackPlayer{

     //giằng non, chưa đủ tuổi
    public static final int GIANG_NON = -1;                   
    public static final int QUAC = 0;
    public static final int WIN_POINT = 21;
    public static final int NGU_LINH = 22;
    public static final int XI_DACH = 23;
    //xì bàng
    public static final int XI_LAC = 24; 
    //tuổi bài người chơi thường
    public static final int TUOI_BAI = 16;                    
    //tuổi bài nhà cái
    public static final int TUOI_BAI_CAI = 15;                

    public static final int THANG_STATUS = 1;
    public static final int THUA_STATUS = -1;
    public static final int HOA_STATUS = 0;
    public static final int CHUA_XET_STATUS = -2;
    //điểm lớn nhất của 1 quân bài là 10 (J, Q, K cũng chỉ tính là 10)
    private static final int MAX_POINT = 10;                  
    //id của quân xì trong bộ bài là 0->3
    private static final int ACE_MAX_ID = 4;                  
    private static final int XI_TINH_11 = 11;
    private static final int XI_TINH_10 = 10;
    private static final int XI_TINH_1 = 1;
    //các giá trị của quân xì trong bài xì dách
    private static int[] arrAceValue = {XI_TINH_11, XI_TINH_10, XI_TINH_1};     
    //bài của người chơi 
    private List<Card> cards =  new ArrayList<>();
    //kiểm tra xem người chơi có trong ván hay ko
    private boolean isPlaying;
    /*
     kiểm tra nhà con đã đặt cược chưa
     Mỗi nhà con chỉ được phép đặt cược 1 lần trong ván
     */
    private boolean isBetted;
    /**
     * Status cua player khi choi.
     */
    private int playingStatus;//chỉ có 3 giá trị: 0 - hoà; 1 - thắng; -1 - thua
    private int reasonId = -1; //ăn theo playing status để lấy reason
    public BlackJackPlayer() {
        reset();
    }
     /**
     * reset tất cả các thuộc tính của player
     */
    public  void reset() {
        clearCards();
        setPlaying(false);
        setStatus(CHUA_XET_STATUS);
        isBetted = false;
    }

    /**
     * nhan 1 con bai duoc chia.
     *
     * @param card con bai duoc nhan.
     */
    public void receivedCard(Card card) {
        this.cards.add(card);
    }

    /**
     *
     * @return
     */
    public List<Short> card2List() {
        List<Short> cardBs = new ArrayList<>();
        for (int i = 0; i < this.cards.size(); i++) {
            cardBs.add((short)this.cards.get(i).getId());
        }
        return cardBs;
    }
    
    public List<Integer> getListCardIds(){
       List<Integer> cardIds= new ArrayList<>();
        for (Card card : this.cards) {
            cardIds.add((int) card.getId());
        }
        return cardIds;
    }
    
    /**
     *
     * @return
     */
    public List<Short> card2ListHide() {
        List<Short> cardBs = new ArrayList<>();
        for (int i = 0; i < this.cards.size(); i++) {
            cardBs.add((short)-1);
        }
        return cardBs;
    }

    /**
     * kiểm tra 1 quân bài bất kì có phải là quân xì hay ko
     *
     * @param card
     * @return boolean - true: quân xì; false: quân bài khác
     */
    private boolean isAce(Card card) {
        boolean bResult = false;

        if (card.getId() < ACE_MAX_ID) {   //con xì
            bResult = true;
        }
        return bResult;
    }

    /**
     * tính điểm bài: xì lác - 24, xì dách - 23, ngũ linh -22, quắc - 0, giằng
     * non - (-1)
     *
     * @return int - số điểm
     */
    public int getResult() {
//        log.debug(this+" getResult");
        int number = 0;         //biến tạm
        int nResult = 0;
        int nAce = countAce();  //đếm số quân xì
        int numberCards = this.cards.size();
        //tính điểm tất cả quân bài không phải xì
        for (int i = 0; i < numberCards; i++) {
//            log.debug(this.cards.get(i).toString());
            if (!isAce(this.cards.get(i))) {
                number = this.cards.get(i).getCardNumber() + 1;
                if (number > MAX_POINT) {
                    number = MAX_POINT;
                }
                nResult += number;
            }
        }

        switch (numberCards) {    //bài xì dách chỉ có 5 con bài
            case 2:     //2 lá - xì tính 11
                if (nAce == 2) { //có 2 con xì --> thắng tuyệt đối
                    nResult = XI_LAC;
                    return nResult;
                } else if (nAce == 1) {  //chỉ có 1 xì --> cộng tiếp giá trị lớn nhất của con xì vào kết quả ở trên
                    nResult += XI_TINH_11;
//                } else if (nAce == 0) {  //không có xì --> dùng kết quả ở trên
                    //do nothing
                }
                if (nResult == WIN_POINT) {
                    nResult = XI_DACH;
                    return nResult;
                }
                break;
            case 3:     //3 lá  - xì tính 11, 10, 1
                //trường hợp này trong bộ bài chỉ có tối đa 2 con xì
                //giảm dần giá trị của quân xì cho tới khi đạt đc kết quả nhỏ nhất với giá trị quân xì lớn nhất
                if (nAce == 1) {
                    int tmp = 0;
                    for (int i = 0; i < arrAceValue.length; i++) {
                        tmp = nResult;
                        tmp += arrAceValue[i];
                        if (tmp <= WIN_POINT) {
                            break;
                        }
                    }
                    nResult = tmp;
                } else if (nAce == 2) {
                    int tmp = 0;
                    for (int i = 0; i < arrAceValue.length; i++) {
                        for (int j = 0; j < arrAceValue.length; j++) {
                            tmp = nResult;
                            tmp += arrAceValue[i] + arrAceValue[j];
                            if (tmp <= WIN_POINT) {
                                break;
                            }
                        }
                    }
                    nResult = tmp;
                } else if (nAce == 0) {
                    //do nothing
                }
                break;
            case 4:     //4 lá, xử lý giống 5 lá
            case 5:     //5 lá  -   xì tính 1
                if (nAce > 0) {
                    nResult += nAce;
                }
                if (numberCards == 5 && nResult <= WIN_POINT) {        //ngũ linh
                    nResult = NGU_LINH;
                    return nResult;
                }
                break;
        }
        if (nResult > WIN_POINT) {
            nResult = QUAC;
        }

//        log.debug(this+" getResult done");
        return nResult;
    }
    
    public int getResult(List<Card> inputCards) {
        int number = 0; //biến tạm
        int nResult = 0;
        int nAce = countAce();  //đếm số quân xì
        for (int i = 0; i < inputCards.size(); i++) {
            if (isAce(inputCards.get(i))) {
                nAce++;
            }
        }
        int numberCards =inputCards.size();
        //tính điểm tất cả quân bài không phải xì
        for (int i = 0; i < numberCards; i++) {
            if (!isAce(inputCards.get(i))) {
                number = inputCards.get(i).getCardNumber() + 1;
                if (number > MAX_POINT) {
                    number = MAX_POINT;
                }
                nResult += number;
            }
        }

        switch (numberCards) {    //bài xì dách chỉ có 5 con bài
            case 2:     //2 lá - xì tính 11
                if (nAce == 2) { //có 2 con xì --> thắng tuyệt đối
                    nResult = XI_LAC;
                    return nResult;
                } else if (nAce == 1) {  //chỉ có 1 xì --> cộng tiếp giá trị lớn nhất của con xì vào kết quả ở trên
                    nResult += XI_TINH_11;
//                } else if (nAce == 0) {  //không có xì --> dùng kết quả ở trên
                    //do nothing
                }
                if (nResult == WIN_POINT) {
                    nResult = XI_DACH;
                    return nResult;
                }
                break;
            case 3:     //3 lá  - xì tính 11, 10, 1
                //trường hợp này trong bộ bài chỉ có tối đa 2 con xì
                //giảm dần giá trị của quân xì cho tới khi đạt đc kết quả nhỏ nhất với giá trị quân xì lớn nhất
                if (nAce == 1) {
                    int tmp = 0;
                    for (int i = 0; i < arrAceValue.length; i++) {
                        tmp = nResult;
                        tmp += arrAceValue[i];
                        if (tmp <= WIN_POINT) {
                            break;
                        }
                    }
                    nResult = tmp;
                } else if (nAce == 2) {
                    int tmp = 0;
                    for (int i = 0; i < arrAceValue.length; i++) {
                        for (int j = 0; j < arrAceValue.length; j++) {
                            tmp = nResult;
                            tmp += arrAceValue[i] + arrAceValue[j];
                            if (tmp <= WIN_POINT) {
                                break;
                            }
                        }
                    }
                    nResult = tmp;
                } else if (nAce == 0) {
                    //do nothing
                }
                break;
            case 4:     //4 lá, xử lý giống 5 lá
            case 5:     //5 lá  -   xì tính 1
                if (nAce > 0) {
                    nResult += nAce;
                }
                if (numberCards == 5 && nResult <= WIN_POINT) {        //ngũ linh
                    nResult = NGU_LINH;
                    return nResult;
                }
                break;
        }
        if (nResult > WIN_POINT) {
            nResult = QUAC;
        }
        return nResult;
    }

    /**
     *
     * @param locale
     * @param result
     * @return ten cua so diem hien tai.
     */
    public String getResultString(Locale locale, int result) {
        String resultString;
        switch (result) {
            case QUAC:
                resultString = BlackJackLanguage.getMessage(BlackJackLanguage.QUAC, locale);
                break;
            case XI_LAC:
                resultString = BlackJackLanguage.getMessage(BlackJackLanguage.XIBANG, locale);
                break;
            case XI_DACH:
                resultString = BlackJackLanguage.getMessage(BlackJackLanguage.XIDACH, locale);
                break;
            case NGU_LINH:
                resultString = BlackJackLanguage.getMessage(BlackJackLanguage.NGULINH, locale);
                break;
            case GIANG_NON:
                resultString = BlackJackLanguage.getMessage(BlackJackLanguage.NOT_ENOUGH_POINT, locale);
                break;
            default:
                resultString = result + " " + BlackJackLanguage.getMessage(BlackJackLanguage.POINT, locale);
                break;
        }
        return resultString;
    }

    /**
     * đếm số con xì trong bài
     *
     * @return int
     */
    private int countAce() {
        int nCount = 0;

        for (int i = 0; i < this.cards.size(); i++) {
            if (isAce(this.cards.get(i))) {
                nCount++;
            }
        }
        return nCount;
    }

    /**
     * xoá hết bài của user
     */
    public void clearCards() {
        this.cards.clear();
    }

    private String cardString(Card card) {
        String faceDesc;
        switch (card.getCardNumber()) {
            case 10:
                faceDesc = "J";
                break;
            case 11:
                faceDesc = "Q";
                break;
            case 12:
                faceDesc = "K";
                break;
            case 0:
                faceDesc = "A";
                break;
            default:
                faceDesc = String.valueOf(card.getCardNumber() + 1);
                break;
        }
        String suitDesc = "";
//        switch (card.getCardType()) {
//            case 0:
//                suitDesc = "bick";
//                break;
//            case 1:
//                suitDesc = "chuon";
//                break;
//            case 3:
//                suitDesc = "co";
//                break;
//            case 2:
//                suitDesc = "ro";
//                break;
//        }

        return faceDesc + " " + suitDesc;
    }

    public String getStrCard() {
        String c = "";
        for (int i = 0; i < getListCards().size(); i++) {
            if (i < getListCards().size() - 1) {
                c += cardString(getListCards().get(i)) + " ";
            } else {
                c += cardString(getListCards().get(i));
            }

        }
        return c;
    }
    /**
     * @return the status of player: true - in game; false - waiting
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * true: khi start game false: khi đã thanh toán xong
     *
     * @param isPlaying
     */
    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    /**
     * @return list of cards
     */
    public List<Card> getListCards() {
        return this.cards;
    }

    /**
     * @return the nStatus
     */
    public int getStatus() {
        return playingStatus;
    }

    /**
     * @param nStatus the nStatus to set
     */
    public void setStatus(int nStatus) {
        this.playingStatus = nStatus;
    }

    public void checkStatusWithCai(int diemCheck) {
        int status;
        int diem = getResult();
        if (diem < TUOI_BAI && diem > QUAC) {
            diem = GIANG_NON;
        }
        if (diemCheck < TUOI_BAI_CAI && diemCheck > QUAC) {
            diemCheck = GIANG_NON;
        }
        if (diem < diemCheck) {
            status = THUA_STATUS;
        } else if (diem > diemCheck) {
            status = THANG_STATUS;
        } else {
            status = HOA_STATUS;
        }
        setStatus(status);
    }
    public void setReason() {
        switch (getStatus()) {
            case THANG_STATUS:
                reasonId = CommonMoneyReasonUtils.THANG;
                break;
            case THUA_STATUS:
                reasonId = CommonMoneyReasonUtils.THUA;
                break;
            default:
                reasonId = CommonMoneyReasonUtils.HOA;
                break;
        }
    }

    public int getReasonId() {
        return this.reasonId;
    }

    /**
     * User đã đặt cược
     *
     * @return
     */
    public boolean isBetted() {
        return isBetted;
    }

    /**
     * xét user đã đặt cược
     */
    public void setBetted(boolean isBetted) {
        this.isBetted = isBetted;
    }
}
