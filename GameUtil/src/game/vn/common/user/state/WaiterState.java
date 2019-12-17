/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.user.state;

/**
 *
 * @author tuanp
 */
public class WaiterState implements IUserState{
    //số ván waiter giữ chổ
    private int countWaittingBoard=0;

    @Override
    public void setWaitingBoard(int countWaitingboard) throws Exception {
        countWaittingBoard=countWaitingboard;
    }

    @Override
    public int getWaittingBoard() {
        return countWaittingBoard;
    }
    
    
}
