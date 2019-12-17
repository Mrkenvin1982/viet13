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
public class PlayerState implements IUserState{

    @Override
    public void setWaitingBoard(int countWaitingboard) throws Exception {
        throw new Exception("setWaitingBoard fail");
    }

    @Override
    public int getWaittingBoard() {
        return 0;
    }
    
}
