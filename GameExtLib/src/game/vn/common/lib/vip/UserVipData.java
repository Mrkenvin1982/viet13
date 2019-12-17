/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.vip;

import java.util.Date;

/**
 *
 * @author anlh
 */
public class UserVipData {

    //userid
    private String userid;
    //bậc hiện tại
    private String currentRank;
    //cấp hiện tại
    private String currentStep;
    //bậc tiếp theo
    private String nextRank;
    //cấp tiếp theo
    private String nextStep;
    //điểm vip hiện tại
    private int currentPoint;
    //điểm vip của cấp
    private int totalPoint;
    //số đồng Z đang có
    private long currentZ;
    //hình ảnh cấp hiện tại và cấp kế
    private String current_imgUrl;
    private String next_imgUrl;
    private String current_imgUrl_mini;
    private String next_imgUrl_mini;

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the currentRank
     */
    public String getCurrentRank() {
        return currentRank;
    }

    /**
     * @param currentRank the currentRank to set
     */
    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
    }

    /**
     * @return the currentStep
     */
    public String getCurrentStep() {
        return currentStep;
    }

    /**
     * @param currentStep the currentStep to set
     */
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * @return the currentPoint
     */
    public int getCurrentPoint() {
        return currentPoint;
    }

    /**
     * @param currentPoint the currentPoint to set
     */
    public void setCurrentPoint(int currentPoint) {
        this.currentPoint = currentPoint;
    }

    /**
     * @return the totalPoint
     */
    public int getTotalPoint() {
        return totalPoint;
    }

    /**
     * @param totalPoint the totalPoint to set
     */
    public void setTotalPoint(int totalPoint) {
        this.totalPoint = totalPoint;
    }

    /**
     * @return the currentZ
     */
    public long getCurrentZ() {
        return currentZ;
    }

    /**
     * @param currentZ the currentZ to set
     */
    public void setCurrentZ(long currentZ) {
        this.currentZ = currentZ;
    }

    /**
     * @return the nextRank
     */
    public String getNextRank() {
        return nextRank;
    }

    /**
     * @param nextRank the nextRank to set
     */
    public void setNextRank(String nextRank) {
        this.nextRank = nextRank;
    }

    /**
     * @return the nextStep
     */
    public String getNextStep() {
        return nextStep;
    }

    /**
     * @param nextStep the nextStep to set
     */
    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    /**
     * @return the current_imgUrl
     */
    public String getCurrent_imgUrl() {
        return current_imgUrl;
    }

    /**
     * @param current_imgUrl the current_imgUrl to set
     */
    public void setCurrent_imgUrl(String current_imgUrl) {
        this.current_imgUrl = current_imgUrl;
    }

    /**
     * @return the next_imgUrl
     */
    public String getNext_imgUrl() {
        return next_imgUrl;
    }

    /**
     * @param next_imgUrl the next_imgUrl to set
     */
    public void setNext_imgUrl(String next_imgUrl) {
        this.next_imgUrl = next_imgUrl;
    }

    /**
     * @return the current_imgUrl_mini
     */
    public String getCurrent_imgUrl_mini() {
        return current_imgUrl_mini;
    }

    /**
     * @param current_imgUrl_mini the current_imgUrl_mini to set
     */
    public void setCurrent_imgUrl_mini(String current_imgUrl_mini) {
        this.current_imgUrl_mini = current_imgUrl_mini;
    }

    /**
     * @return the next_imgUrl_mini
     */
    public String getNext_imgUrl_mini() {
        return next_imgUrl_mini;
    }

    /**
     * @param next_imgUrl_mini the next_imgUrl_mini to set
     */
    public void setNext_imgUrl_mini(String next_imgUrl_mini) {
        this.next_imgUrl_mini = next_imgUrl_mini;
    }

}
