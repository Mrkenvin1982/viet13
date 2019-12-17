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
public interface IUserState {
    public void setWaitingBoard(int countWaitingboard) throws Exception;
    public int getWaittingBoard();   
    
}
