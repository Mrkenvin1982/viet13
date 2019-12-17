/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.vip;

/**
 *
 * @author anlh
 */
public class UpLevelInfo {
    
    //rank vua len
    private int rankUp;
    //cap vua len
    private int stepUp;
    //cau chuc mung neu co, ko co bo qua
    private String desc;

    /**
     * @return the rankUp
     */
    public int getRankUp() {
        return rankUp;
    }

    /**
     * @param rankUp the rankUp to set
     */
    public void setRankUp(int rankUp) {
        this.rankUp = rankUp;
    }

    /**
     * @return the stepUp
     */
    public int getStepUP() {
        return stepUp;
    }

    /**
     * @param stepUP the stepUp to set
     */
    public void setStepUP(int stepUP) {
        this.stepUp = stepUP;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
}
