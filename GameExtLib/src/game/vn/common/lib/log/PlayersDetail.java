/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.log;

/**
 *
 * @author tuanp
 */
public class PlayersDetail {

    private String playerId;
    private String playerName;
    //tiền đặt cược
    private double betCredit;
    //1: point, 2: money
    private double betUnit;
    //tiền của user trước khi vào bàn
    private double creditBefore;
    //tiền của user sau khi vào bàn
    private double creditAfter;
    private int resultId;
    private double winAmount;       // tổng tiền thắng thua
    private double returnAmount;    // hanv add, use for BigSmall
    private double rake;
    private double totalTurnover;   // tổng tiền thắng trước thuế
    private double penalty;         // tiền phạt bỏ cuộc
    private String betId;
    private String roundId;
    private boolean isBot;
    private String userChannel;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public double getBetCredit() {
        return betCredit;
    }

    public void setBetCredit(double betCredit) {
        this.betCredit = betCredit;
    }

    public double getBetUnit() {
        return betUnit;
    }

    public void setBetUnit(double betUnit) {
        this.betUnit = betUnit;
    }

    public double getCreditBefore() {
        return creditBefore;
    }

    public void setCreditBefore(double creditBefore) {
        this.creditBefore = creditBefore;
    }

    public double getCreditAfter() {
        return creditAfter;
    }

    public void setCreditAfter(double creditAfter) {
        this.creditAfter = creditAfter;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public void setWinAmount(double winAmount) {
        this.winAmount = winAmount;
    }

    public double getWinAmount() {
        return winAmount;
    }

    public void setRake(double rake) {
        this.rake = rake;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public void setTotalTurnover(double totalTurnover) {
        this.totalTurnover = totalTurnover;
    }

    public void setReturnAmount(double returnAmount) {
        this.returnAmount = returnAmount;
    }

    public void setIsBot(boolean isBot) {
        this.isBot = isBot;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public String getUserChannel() {
        return userChannel;
    }

    public void setUserChannel(String userChannel) {
        this.userChannel = userChannel;
    }

    public double getReturnAmount() {
        return returnAmount;
    }

    public double getRake() {
        return rake;
    }

    public double getTotalTurnover() {
        return totalTurnover;
    }

    public String getBetId() {
        return betId;
    }

    public String getRoundId() {
        return roundId;
    }

    public boolean isIsBot() {
        return isBot;
    }

}
