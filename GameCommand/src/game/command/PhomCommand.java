/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.command;

/**
 *
 * @author tuanp
 */
public class PhomCommand {
    //key
    public static final String  ID_USER_FIRST_HA="pIdFH";
    public static final String  ARRAY_INFOR_CARD_MOVE="pArrM";
    public static final String  ARRAY_INFOR_CARD_EAT="pArrE";
    public static final String  TIME_LIMIT="pTL";
    public static final String  ID_USER_CURRENT="pIdCurr";
    public static final String  NUM_CARD_MOVE="pCM";
    public static final String  NUM_PHOM="pNP";
    public static final String  SCORES="pSco";
    //command action
    public static final int SEND_LIST_ADD_CARD=100;
    public static final int HA_PHOM=101;
    public static final int U=102;
    public static final int GET_CARD=103;
    public static final int EAT_CARD=104;
    public static final int ADD_CARD=105;
    public static final int RESET_PHOM_EAT=106;
}
