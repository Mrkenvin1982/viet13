/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.user.state;

import java.math.BigDecimal;

/**
 * quản lý tất cả các trạng thái của user
 * @author tuanp
 */
public class MagagerUserState {
    
    //user là player
    private final IUserState playerState;
    //user là waiter ngồi giữ chổ
    private final IUserState waiterState;
    //trạng thái hiện tại
    private IUserState stateCurrent;
    //check xem user có auto hay khong
    private boolean isAutoBuyIn;
    //tự động thoát bàn khi kết thúc ván
    private boolean isLeaveGame;
    //luu stack user buy in de sun dung cho auto
    private BigDecimal autoBuyInStack = BigDecimal.ZERO;
    
    public MagagerUserState(){
        playerState= new PlayerState();
        waiterState = new WaiterState();
        stateCurrent=waiterState;
    }
    
    public IUserState getStateCurrent(){
        return stateCurrent;
    }
    public void setWaiterState() throws Exception{
        waiterState.setWaitingBoard(0);
        stateCurrent=waiterState;
    }
    public void setPlayerState(){
        stateCurrent=playerState;
    }
    public void setWaitingBoard(int count) throws Exception{
        waiterState.setWaitingBoard(count);
    }
    public boolean isPlayer(){
        return playerState.equals(getStateCurrent());
    }

    public boolean isIsAutoBuyIn() {
        return isAutoBuyIn;
    }

    public void setIsAutoBuyIn(boolean isAutoBuyIn) {
        this.isAutoBuyIn = isAutoBuyIn;
    }

    public BigDecimal getAutoBuyInStack() {
        return autoBuyInStack;
    }

    public void setAutoBuyInStack(BigDecimal autoBuyInStack) {
        this.autoBuyInStack = autoBuyInStack;
    }

    public boolean isIsLeaveGame() {
        return isLeaveGame;
    }

    public void setIsLeaveGame(boolean isLeaveGame) {
        this.isLeaveGame = isLeaveGame;
    }
    
}
