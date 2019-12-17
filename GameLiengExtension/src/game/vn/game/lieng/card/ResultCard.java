/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng.card;

/**
 *
 * @author tuanp
 */
public class ResultCard {

    private byte value;
    private byte score;
    private String strValue;

    public ResultCard() {
        strValue = "";
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     * score là id con lớn nhất
     *
     * @return
     */
    public byte getScore() {
        return score;
    }

    public void setScore(byte score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "ResultCard{" + "value=" + value + ", score=" + score + ", strValue=" + strValue + '}';
    }
}

