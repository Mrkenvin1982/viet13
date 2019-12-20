/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.constant;

/**
 *
 * @author tuanp
 */
public class ExtensionConstant {
    
    //group lobby tiền ảo
    public static final String LOBBY_GROUP_NAME = "gr_lobby";
    public static final String BAI_CAO_LOBBY_NAME = "lo_baicao";
    public static final String BLACK_JACK_LOBBY_NAME = "lo_xizach";
    public static final String PHOM_LOBBY_NAME = "lo_phom";
    public static final String TLMN_LOBBY_NAME = "lo_tlmn";
    public static final String MAUBINH_LOBBY_NAME = "lo_maubinh";
    public static final String XITO_LOBBY_NAME = "lo_xito";
    public static final String LIENG_LOBBY_NAME = "lo_lieng";
    public static final String SAM_LOBBY_NAME = "lo_sam";
    public static final String TLDL_LOBBY_NAME = "lo_tldl";
    public static final String TLDL_SOLO_LOBBY_NAME = "lo_tldl_sl";
    
    //group lobby tiền thiệt
    public static final String LOBBY_GROUP_NAME_REAL = "gr_lobby_r";
    public static final String BAI_CAO_LOBBY_NAME_REAL = "lo_baicao_r";
    public static final String BLACK_JACK_LOBBY_NAME_REAL = "lo_xizach_r";
    public static final String PHOM_LOBBY_NAME_REAL = "lo_phom_r";
    public static final String TLMN_LOBBY_NAME_REAL = "lo_tlmn_r";
    public static final String MAUBINH_LOBBY_NAME_REAL = "lo_maubinh_r";
    public static final String XITO_LOBBY_NAME_REAL = "lo_xito_r";
    public static final String LIENG_LOBBY_NAME_REAL = "lo_lieng_r";
    public static final String SAM_LOBBY_NAME_REAL = "lo_sam_r";
    public static final String TLDL_LOBBY_NAME_REAL = "lo_tldl_r";
    public static final String TL_TOUR_LOBBY_NAME_REAL = "lo_tl_tour_r";
    public static final String TLDL_SOLO_LOBBY_NAME_REAL = "lo_tldl_sl_r";
    
    //game BAI CAO
    public static final String BAICAO_GROUP_NAME = "gr_baicao";
    public static final String BAICAO_EXT_ID = "GameBaiCaoExtension";
    public static final String BAICAO_EXT_CLASS = "game.vn.game.baicao.BaiCaoGame";
    
    //game BLACK JACK
    public static final String BLACKJACK_GROUP_NAME = "gr_blackjack";
    public static final String BLACKJACK_EXT_ID = "GameBlackJackExtension";
    public static final String BLACKJACK_EXT_CLASS = "game.vn.game.blackjack.BlackJackGame";
    
    //game PHOM
    public static final String PHOM_GROUP_NAME = "gr_phom";
    public static final String PHOM_EXT_ID = "GamePhomExtension";
    public static final String PHOM_EXT_CLASS = "game.vn.game.phom.PhomGame";
    
    //TLMN
    public static final String TLMN_GROUP_NAME = "gr_tlmn";
    public static final String TLMN_EXT_ID = "GameTienLenExtension";
    public static final String TLMN_EXT_CLASS = "game.vn.game.tienlen.TienLenGame";
    
    // game MAU BINH
    public static final String MAUBINH_GROUP_NAME = "gr_maubinh";
    public static final String MAUBINH_EXT_ID = "GameMauBinhExtension";
    public static final String MAUBINH_EXT_CLASS = "game.vn.game.maubinh.MauBinhGame";
    
    // game XITO
    public static final String XITO_GROUP_NAME = "gr_xito";
    public static final String XITO_EXT_ID = "GameXiToExtension";
    public static final String XITO_EXT_CLASS = "game.vn.game.xito.XiToGame";
    
    // game LIENG
    public static final String LIENG_GROUP_NAME = "gr_lieng";
    public static final String LIENG_EXT_ID = "GameLiengExtension";
    public static final String LIENG_EXT_CLASS = "game.vn.game.lieng.LiengGame";
    
    // game SAM
    public static final String SAM_GROUP_NAME = "gr_sam";
    public static final String SAM_EXT_ID = "GameSamExtension";
    public static final String SAM_EXT_CLASS = "game.vn.game.sam.SamGame";
    
    //TLDL
    public static final String TLDL_GROUP_NAME = "gr_tldl";
    public static final String TLDL_EXT_ID = "GameTienLenDemLaExtension";
    public static final String TLDL_EXT_CLASS = "game.vn.game.tienlendemla.TienLenDemLaGame";
    
    //Tien len tour
    public static final String TL_TOUR_GROUP_NAME = "gr_tl_tour";
    public static final String TL_TOUR_EXT_ID = "GameTienLenTourExtension";
    public static final String TL_TOUR_EXT_CLASS = "game.vn.game.tienlentour.TienLenTourGame";
    
    public static final String TLDL_SL_GROUP_NAME = "gr_tldl_sl";
    public static final String TLDL_SL_EXT_ID = "GameTienLenDemLaSoloExtension";
    public static final String TLDL_SL_EXT_CLASS = "game.vn.game.tienlendemla.TienLenDemLaGame";
    
    public static final byte SERVER_TYPE_LOGIN = 0;
    public static final byte SERVER_TYPE_GAME = 1;
    
    public static final byte PIN_STATUS_INACTIVE = 0;
    public static final byte PIN_STATUS_ACTIVATING = 1;
    public static final byte PIN_STATUS_ACTIVE = 2;

    public static final byte POINT_TYPE_FREE = 0;
    public static final byte POINT_TYPE_VIDEO = 1;
    public static final byte POINT_TYPE_IAP = 2;

    public static final byte LOGIN_TYPE_FB = 0;
    public static final byte LOGIN_TYPE_GG = 1;
    public static final byte LOGIN_TYPE_TEST = 2;
}
