/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.payment;

/**
 *
 * @author anlh
 */
public class PaymentInfo {

    private String playerName;
    private String playerId;
    private double balance;

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the playerId
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * @param playerId the playerId to set
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

}
