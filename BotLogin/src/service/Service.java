/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import constant.Constant;

/**
 *
 * @author hanv
 */
public class Service {

    public static final byte BLACKJACK = 1;
    public static final byte TIENLEN = 2;
    public static final byte XI_TO = 3;
    public static final byte PHOM = 4;
    public static final byte BAI_CAO = 5;
    public static final byte MAUBINH = 6;
    public static final byte LIENG = 7;
    public static final byte SAM = 9;
    public static final byte TIEN_LEN_DEM_LA = 10;
    public static final byte TIEN_LEN_TOUR = 11;
    public static final byte TAI_XIU = -10;
    
    public static final String LOBBY_BLACKJACK = "lo_xizach";
    public static final String LOBBY_BLACKJACK_MONEY = "lo_xizach_r";
    public static final String LOBBY_TIENLEN = "lo_tlmn";
    public static final String LOBBY_TIENLEN_MONEY = "lo_tlmn_r";
    public static final String LOBBY_XI_TO = "lo_xito";
    public static final String LOBBY_XI_TO_MONEY = "lo_xito_r";
    public static final String LOBBY_PHOM = "lo_phom";
    public static final String LOBBY_PHOM_MONEY = "lo_phom_r";
    public static final String LOBBY_BAICAO = "lo_baicao";
    public static final String LOBBY_BAICAO_MONEY = "lo_baicao_r";
    public static final String LOBBY_MAUBINH = "lo_maubinh";
    public static final String LOBBY_MAUBINH_MONEY = "lo_maubinh_r";
    public static final String LOBBY_LIENG = "lo_lieng";
    public static final String LOBBY_LIENG_MONEY = "lo_lieng_r";
    public static final String LOBBY_SAM = "lo_sam";
    public static final String LOBBY_SAM_MONEY = "lo_sam_r";
    public static final String LOBBY_TIEN_LEN_DEM_LA = "lo_tldl";
    public static final String LOBBY_TIEN_LEN_DEM_LA_MONEY = "lo_tldl_r";
    public static final String LOBBY_TIEN_LEN_TOUR_MONEY = "lo_tl_tour_r";
    
    public static String getLobbyName(byte serviceId, byte moneyType) {
        if (moneyType == Constant.MONEY) {
            return getLobbyNameMoney(serviceId);
        }
        return getLobbyNamePoint(serviceId);
    }

    public static String getLobbyNameMoney(byte serviceId) {
        switch (serviceId) {
            case BLACKJACK:return LOBBY_BLACKJACK_MONEY;
            case TIENLEN:return LOBBY_TIENLEN_MONEY;
            case XI_TO:return LOBBY_XI_TO_MONEY;
            case PHOM:return LOBBY_PHOM_MONEY;
            case BAI_CAO:return LOBBY_BAICAO_MONEY;
            case MAUBINH:return LOBBY_MAUBINH_MONEY;
            case LIENG:return LOBBY_LIENG_MONEY;
            case SAM:return LOBBY_SAM_MONEY;
            case TIEN_LEN_DEM_LA:return LOBBY_TIEN_LEN_DEM_LA_MONEY;
            case TIEN_LEN_TOUR:return LOBBY_TIEN_LEN_TOUR_MONEY;
        }
        return null;
    }
    
    public static String getLobbyNamePoint(byte serviceId) {
        switch (serviceId) {
            case BLACKJACK:return LOBBY_BLACKJACK;
            case TIENLEN:return LOBBY_TIENLEN;
            case XI_TO:return LOBBY_XI_TO;
            case PHOM:return LOBBY_PHOM;
            case BAI_CAO:return LOBBY_BAICAO;
            case MAUBINH:return LOBBY_MAUBINH;
            case LIENG:return LOBBY_LIENG;
            case SAM:return LOBBY_SAM;
            case TIEN_LEN_DEM_LA:return LOBBY_TIEN_LEN_DEM_LA;
        }
        return null;
    }

    public static byte getServiceId(int userType) {
        switch (userType) {
            case Constant.USER_TYPE_BOT_BC_CAI:
            case Constant.USER_TYPE_BOT_BC_CAI_OFF:
            case Constant.USER_TYPE_BOT_BC_CON:
            case Constant.USER_TYPE_BOT_BC_CON_OFF:
            case Constant.POINT_TYPE_BOT_BC_CAI:
            case Constant.POINT_TYPE_BOT_BC_CAI_OFF:
            case Constant.POINT_TYPE_BOT_BC_CON:
            case Constant.POINT_TYPE_BOT_BC_CON_OFF:
                return BAI_CAO;
            case Constant.USER_TYPE_BOT_XD_CAI:
            case Constant.USER_TYPE_BOT_XD_CAI_OFF:
            case Constant.USER_TYPE_BOT_XD_CON:
            case Constant.USER_TYPE_BOT_XD_CON_OFF:
            case Constant.POINT_TYPE_BOT_XD_CAI:
            case Constant.POINT_TYPE_BOT_XD_CAI_OFF:
            case Constant.POINT_TYPE_BOT_XD_CON:
            case Constant.POINT_TYPE_BOT_XD_CON_OFF:
                return BLACKJACK;
            case Constant.USER_TYPE_BOT_MB_CAI:
            case Constant.USER_TYPE_BOT_MB_CAI_OFF:
            case Constant.USER_TYPE_BOT_MB_CON:
            case Constant.USER_TYPE_BOT_MB_CON_OFF:
            case Constant.POINT_TYPE_BOT_MB_CAI:
            case Constant.POINT_TYPE_BOT_MB_CAI_OFF:
            case Constant.POINT_TYPE_BOT_MB_CON:
            case Constant.POINT_TYPE_BOT_MB_CON_OFF:
                return MAUBINH;
            case Constant.USER_TYPE_BOT_TL_CAI:
            case Constant.USER_TYPE_BOT_TL_CAI_OFF:
            case Constant.USER_TYPE_BOT_TL_CON:
            case Constant.USER_TYPE_BOT_TL_CON_OFF:
            case Constant.POINT_TYPE_BOT_TL_CAI:
            case Constant.POINT_TYPE_BOT_TL_CAI_OFF:
            case Constant.POINT_TYPE_BOT_TL_CON:
            case Constant.POINT_TYPE_BOT_TL_CON_OFF:
                return TIENLEN;
            case Constant.USER_TYPE_BOT_TX:
            case Constant.USER_TYPE_BOT_TX_OFF:
            case Constant.POINT_TYPE_BOT_TX:
            case Constant.POINT_TYPE_BOT_TX_OFF:
                return TAI_XIU;
        }
        return 0;
    }

    public static byte getMoneyType(int userType) {
        switch (userType) {
            case Constant.USER_TYPE_BOT_BC_CAI:
            case Constant.USER_TYPE_BOT_BC_CON:
            case Constant.USER_TYPE_BOT_XD_CAI:
            case Constant.USER_TYPE_BOT_XD_CON:
            case Constant.USER_TYPE_BOT_MB_CAI:
            case Constant.USER_TYPE_BOT_MB_CON:
            case Constant.USER_TYPE_BOT_TL_CAI:
            case Constant.USER_TYPE_BOT_TL_CON:
            case Constant.USER_TYPE_BOT_TX:
                return Constant.MONEY;
        }
        return Constant.POINT;
    }
}