/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.command;

/**
 * command riêng của game black jack
 * @author tuanp
 */
public class BlackJackCommand {
    //rút bài
    public static final byte GET_CARD=100;
    //tới lượt user
    public static final byte TURN_AROUND_USER=101;
    //nhà cái xét bài nhà con
    public static final byte OWNER_CHECK_CARD=102;
}
