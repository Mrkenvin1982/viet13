/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.poker.lasthand;

/**
 *
 * @author tuanp
 */
public class LastHandManager {
    private final LastHandView lastHandView;
    private final LastHandDetail lastHandDetail;
    // Thời gian tạo
    private long createdAt;
    
    public LastHandManager(){
        lastHandView = new LastHandView();
        lastHandDetail = new LastHandDetail();
    }

    public LastHandView getLastHandView() {
        return lastHandView;
    }

    public LastHandDetail getLastHandDetail() {
        return lastHandDetail;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void reset(){
        lastHandView.reset();
        lastHandDetail.reset();
    }
}
