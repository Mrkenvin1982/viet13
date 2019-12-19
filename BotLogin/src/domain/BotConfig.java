/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.util.List;

/**
 *
 * @author hanv
 */
public class BotConfig {
    private int minBuyIn;
    private int maxBuyIn;
    private int timeJoinBoard;
    private int timeChangeBoardFrom;
    private int timeChangeBoardTo;
    private int botInBoard;
    private int botEachTurn;
    private int botEachTurnTo;
    private int botEachTurnFrom;
    private boolean enable;
    private List<Double> listBet;

    public BotConfig() {
    }

    public int getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(int minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    public int getMaxBuyIn() {
        return maxBuyIn;
    }

    public void setMaxBuyIn(int maxBuyIn) {
        this.maxBuyIn = maxBuyIn;
    }

    public int getTimeJoinBoard() {
        return timeJoinBoard;
    }

    public void setTimeJoinBoard(int timeJoinBoard) {
        this.timeJoinBoard = timeJoinBoard;
    }

    public int getTimeChangeBoardFrom() {
        return timeChangeBoardFrom;
    }

    public void setTimeChangeBoardFrom(int timeChangeBoardFrom) {
        this.timeChangeBoardFrom = timeChangeBoardFrom;
    }

    public int getTimeChangeBoardTo() {
        return timeChangeBoardTo;
    }

    public void setTimeChangeBoardTo(int timeChangeBoardTo) {
        this.timeChangeBoardTo = timeChangeBoardTo;
    }

    public int getBotInBoard() {
        return botInBoard;
    }

    public void setBotInBoard(int botInBoard) {
        this.botInBoard = botInBoard;
    }

    public int getBotEachTurn() {
        return botEachTurn;
    }

    public void setBotEachTurn(int botEachTurn) {
        this.botEachTurn = botEachTurn;
    }

    public int getBotEachTurnTo() {
        return botEachTurnTo;
    }

    public void setBotEachTurnTo(int botEachTurnTo) {
        this.botEachTurnTo = botEachTurnTo;
    }

    public int getBotEachTurnFrom() {
        return botEachTurnFrom;
    }

    public void setBotEachTurnFrom(int botEachTurnFrom) {
        this.botEachTurnFrom = botEachTurnFrom;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<Double> getListBet() {
        return listBet;
    }

    public void setListBet(List<Double> listBet) {
        this.listBet = listBet;
    }

    @Override
    public String toString() {
        return "BotConfig{" + "minBuyIn=" + minBuyIn + ", maxBuyIn=" + maxBuyIn + ", timeJoinBoard=" + timeJoinBoard + ", timeChangeBoardFrom=" + timeChangeBoardFrom + ", timeChangeBoardTo=" + timeChangeBoardTo + ", botInBoard=" + botInBoard + ", botEachTurn=" + botEachTurn + ", botEachTurnTo=" + botEachTurnTo + ", botEachTurnFrom=" + botEachTurnFrom + ", enable=" + enable + ", listBet=" + listBet + '}';
    }
}
