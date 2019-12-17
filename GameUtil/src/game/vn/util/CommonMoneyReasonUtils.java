/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util;

/**
 *lưu lại id khi update tiền của user tương ứng với id 
 * của bảng money_log_reason trong database
 * @author tuanp
 */
public class CommonMoneyReasonUtils {
    public static final int THUA = 0;
    public static final int THANG = 1;
    public static final int HOA = 2;
    public static final int DAT_CUOC = 3;
    public static final int CAI_DAT_CUOC=4;
    public static final int BO_CUOC = 5;
    public static final int NHAN_NHIEM_VU = 6;
    public static final int UNDEFINED =7;
    
    //bài phỏm
    static public final int CHAY = 8;
    static public final int THUA_U = 9;
    static public final int U = 10;
    static public final int BI_AN_CHOT_HA = 11;
    static public final int AN_CHOT_HA = 12;
    static public final int BI_AN_GA_1 = 13;
    static public final int AN_GA_1 = 14;
    
    static public final int DEN = 15; //đền bài
    static public final int THANG_DEN_BAI = 16; //thắng đền bài
    static public final int PENALTY_SYTEM = 17; // bỏ cuộc hệ thống ăn
    
    //bài tiến lên
    static public final int CHAT_HEO_DEN = 18;
    static public final int CHAT_HEO_DO = 19;
    static public final int CHAT_HEO_1DEN_1DO = 21;
    static public final int CHAT_HEO_2DEN_1DO = 23;
    static public final int CHAT_HEO_1DEN_2DO = 24;
    static public final int CHAT_HEO_2DEN_2DO = 25;
    static public final int CHAT_HEO_2DEN = 26;
    static public final int CHAT_HEO_2DO = 27;
    static public final int CHAT_3_DOI_THONG = 28;
    static public final int CHAT_TU_QUY = 29;
    static public final int CHAT_4_DOI_THONG = 30;
    static public final int THUI_3_BICH = 31;
    static public final int THUI_HEO_1DEN = 32;
    static public final int THUI_HEO_2DEN = 33;
    static public final int THUI_HEO_1DO = 34;
    static public final int THUI_HEO_2DO = 35;
    static public final int THUI_1_3DOI_THONG = 36;
    static public final int THUI_1_TU_QUY = 37;
    static public final int THUI_2_TU_QUY = 38;
    static public final int THUI_4_DOI_THONG = 39;
    static public final int THUONG_TOI_TRANG = 40;
    static public final int THUONG_TOI_3BICH = 41;
    static public final int PHAT_TOI_TRANG = 42;
    static public final int PHAT_TOI_3BICH = 43;
    static public final int TOI = 44;
    static public final int TIEN_PHAT_CONG = 45;
    static public final int TONG_TIEN_CONG = 46;
    static public final int BI_CHAT = 47; //Bị chặt: heo, thông
    static public final int TOI_1 = 48;
    
    public static final int TRA_TIEN =52;
    public static final int BET =53;
    public static final int CALL =54;
    public static final int CHECK =55;
    public static final int FOLD =56;
    public static final int RAISE =57;
    public static final int ALL_IN =58;
    public static final int THUI =59;
    public static final int THANG_CONG =60;
    public static final int THANG_CHAT =61;
    public static final int THANG_TOI_3_BICH=62;
    public static final int BO_CUOC_KHI_BAO_SAM=63;
    public static final int AUTO_BINH=64;
    public static final int GET_CARD=65;
    public static final int HA_PHOM=66;
    public static final int MOVE=67;
    public static final int SEND_CARD=68;
    public static final int ADD_CARD=69;
    public static final int XAM=70;
    public static final int SKIP_XAM=71;
    public static final int SKIP=72;
    public static final int BUY_TICKET=73;
    public static final int BONUS=74;
    public static final int BUY_STACK=75;
    
    //phom
    static public final int BI_AN_GA_2 = 76;
    static public final int AN_GA_2 = 77;
    static public final int BI_AN_GA_3 = 78;
    static public final int AN_GA_3 = 79;
    
    public static String getReasonDescription(int reasonId) {
        switch (reasonId) {
            case THUA:
                return "Lose";
            case THANG:
                return "Win";
            case HOA:
                return "Draw";
            case DAT_CUOC:
                return "Bet";
            case CAI_DAT_CUOC:
                return "House bet";
            case BO_CUOC:
                return "Give-up";
            case PENALTY_SYTEM:
                return "Give-up fine";
            case NHAN_NHIEM_VU:
                return "Mission Received";
            case UNDEFINED:
                return "Unknown";
            case CHAY:
                return "Burn";
            case THUA_U:
                return "Rummy lose";
            case U:
                return "Rummy";
            case BI_AN_CHOT_HA:
                return "Last play lose";
            case AN_CHOT_HA:
                return "Last play win";
            case BI_AN_GA_1:
                return "Being melded 1st card";
            case AN_GA_1:
                return "Melded 1st card";
            case DEN:
                return "Penalty lose";
            case THANG_DEN_BAI:
                return "Penalty win";
            case CHAT_HEO_DEN:
                return "Bomb Black 2s";
            case CHAT_HEO_DO:
                return "Bomb Red 2s";
            case CHAT_HEO_1DEN_1DO:
                return "Bomb a Red 2s & Black 2s";
            case CHAT_HEO_2DEN_1DO:
                return "Bomb a Red 2s & pair of Black 2s";
            case CHAT_HEO_1DEN_2DO:
                return "Bomb pair of Red 2s & a Black 2s";
            case CHAT_HEO_2DEN_2DO:
                return "Bomb pair of Red 2s & pair of Black 2s";
            case CHAT_HEO_2DEN:
                return "Bomb pair of Black 2s";
            case CHAT_HEO_2DO:
                return "Bomb pair of Red 2s";
            case CHAT_3_DOI_THONG:
                return "Bomb 3 sequence pairs";
            case CHAT_TU_QUY:
                return "Bomb 4 of a kind";
            case CHAT_4_DOI_THONG:
                return "Bomb 4 sequence pairs";
            case THUI_3_BICH:
                return "Wasted 3 Spade";
            case THUI_HEO_1DEN:
                return "Wasted a Black 2s";
            case THUI_HEO_2DEN:
                return "Wasted pair of Black 2s";
            case THUI_HEO_1DO:
                return "Wasted a Red 2s";
            case THUI_HEO_2DO:
                return "Wasted pair of Red 2s";
            case THUI_1_3DOI_THONG:
                return "Wasted 3 sequence pairs";
            case THUI_1_TU_QUY:
                return "Wasted 4 of a kind";
            case THUI_2_TU_QUY:
                return "Wasted double 4 of a kind";
            case THUI_4_DOI_THONG:
                return "Wasted 4 sequence pairs";
            case THUONG_TOI_TRANG:
                return "Instant win";
            case THUONG_TOI_3BICH:
                return "3 Spade win";
            case PHAT_TOI_TRANG:
                return "Instant lose";
            case PHAT_TOI_3BICH:
                return "3 Spade lose";
            case TOI:
                return "Win";
            case TIEN_PHAT_CONG:
                return "Instant win amount";
            case TONG_TIEN_CONG:
                return "Total instant win amount";
            case BI_CHAT:
                return "Being bomb";
            case TOI_1:
                return "1st win";
            case TRA_TIEN:
                return "Paid";
            case BET:
                return "Bet";
            case CHECK:
                return "Check";
            case FOLD:
                return "Fold";
            case RAISE:
                return "Raise";
            case ALL_IN:
                return "All in";
            case THUI:
                return "Wasted";
            case THANG_CONG:
                return "Instant win";
            case THANG_CHAT:
                return "Bomb win";
            case THANG_TOI_3_BICH:
                return "3 Spade win";
            case BO_CUOC_KHI_BAO_SAM:
                return "Give up 'SAM'"; 
            case AUTO_BINH:
                return "Auto arrange";
            case GET_CARD:
                return "Get card";
            case HA_PHOM:
                return "Melding";
            case MOVE:
                return "Move";
            case SEND_CARD:
                return "Send Card";
            case ADD_CARD:
                return "Add card";
            case XAM:
                return "Sam";
            case SKIP_XAM:
                return "Skip Sam";
            case SKIP:
                return "Skip";
            case BUY_TICKET:
                return "Buy ticket";
            case BONUS:
                return "Bonus";
            case BUY_STACK:
                return "Buy-in";
            case BI_AN_GA_2:
                return "Being melded 2nd card";
            case AN_GA_2:
                return "Melded 2nd card";
            case BI_AN_GA_3:
                return "Being melded 3rd card";
            case AN_GA_3:
                return "Melded 3rd card";
        }
        return "";
    }
}
