/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao.object;

import game.vn.common.card.object.Card;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tuanp
 */
public class BaiCaoPlayer{
    private static final int CON_TIEN = 11;
    private List<Card> cards = null;
    private boolean isPlaying = false;
    private boolean isHolding = false;
    private boolean isWin = false;
    private final List<Short> listShow = new ArrayList<>();
    //số tiền thắng thua từng ván, chi dung de gui ve cho client hien thi thắng thua
    private BigDecimal moneyWinLose =  BigDecimal.ZERO;
    /*
    kiểm tra nhà con đã đặt cược chưa
    Mỗi nhà con chỉ được phép đặt cược 1 lần trong ván
    */
    private boolean isBetted;
    

    public BaiCaoPlayer() {
        resetGame();
    }

    /**
     * Tinh diem 3 la bai.
     *
     * @return diem so cua 3 la bai. bu la 0 <br> 3 cao la 10 diem.
     */
    public int getResult() {
        int result = 0;
        int nCount = 0;
        for (int i = 0; i < cards.size(); i++) {
            int number = cards.get(i).getCardNumber() + 1;
            if (number >= CON_TIEN) {
                nCount++;
            }
        }

        if (nCount == 3) {  //ba tien
            return 10;
        } else {
            for (int i = 0; i < cards.size(); i++) {
                int number = cards.get(i).getCardNumber() + 1;
                if (number < CON_TIEN) {
                    result += number;
                }
            }
            if (result >= 10) {
                result = result%10;
            }
        }
        return result;
    }

    /**
     * @return the mCard
     */
    public List<Card> getCard() {
        return cards;
    }

    /**
     * bo het nhung con bai dang cam tren tay.
     */
    public void resetGame() {
        cards = new ArrayList<>();
        listShow.clear();
        setPlaying(false);
        setHolding(false);
        setIsWin(false);
        isBetted = false;
        moneyWinLose= BigDecimal.ZERO;
    }

    /**
     * nhan 1 con bai duoc chia.
     *
     * @param card con bai duoc nhan.
     */
    public void receivedCard(Card card) {
        cards.add(card);
    }

    /**
     *
     * @return
     */
    public List<Short> card2List() {
        List<Short> cardBs = new ArrayList<>();
        for (int i = 0; i < this.cards.size(); i++) {
            cardBs.add((short)this.cards.get(i).getId());
        }
        return cardBs;
    }

    /**
     * @return the isPlaying
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * @param isPlaying the isPlaying to set
     */
    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    /**
     * @return the isHolding
     */
    public boolean isHolding() {
        return isHolding;
    }

    /**
     * @param isHolding the isHolding to set
     */
    public void setHolding(boolean isHolding) {
        this.isHolding = isHolding;
    }

    public void setIsWin(boolean isWin) {
        this.isWin = isWin;
    }

    public boolean isWin() {
        return isWin;
    }

    public void printCards() {
        for (int i = 0; i < cards.size(); i++) {
            System.err.println(" " + (cards.get(i).getCardNumber() + 1));
        }
    }

    public void clearCards() {
        this.cards.clear();
    }
    
    public String getStringCardList(){
        String result = "";
        for (Card c: cards){
            result+= getStringCard(c) + " ";
        }
        return result;
    }
    /**
     * lấy quân bài ở dạng text để ghi log
     *
     * @param card
     * @return
     */
    public String getStringCard(Card card) {
        String strCard = "";
        if (card == null){
            return strCard;
        }
        
        switch (card.getCardNumber()) {
            case 0:
                strCard = "A";
                break;
            case 1:
                strCard = "2";
                break;
            case 2:
                strCard = "3";
                break;
            case 3:
                strCard = "4";
                break;
            case 4:
                strCard = "5";
                break;
            case 5:
                strCard = "6";
                break;
            case 6:
                strCard = "7";
                break;
            case 7:
                strCard = "8";
                break;
            case 8:
                strCard = "9";
                break;
            case 9:
                strCard = "10";
                break;
            case 10:
                strCard = "J";
                break;
            case 11:
                strCard = "Q";
                break;
            case 12:
                strCard = "K";
                break;
        }

        switch (card.getCardType()) {
            case 0:
                strCard += " bích";
                break;
            case 1:
                strCard += " chuồn";
                break;
            case 2:
                strCard += " rô";
                break;
            case 3:
                strCard += " cơ";
                break;
        }
        return strCard;
    }
    
    /**
     * User đã đặt cược
     * @return 
     */
    public boolean isBetted(){
        return isBetted;
    }
    /**
     * xét user đã đặt cược
     * @param isBetted
     */
    public void setBetted(boolean isBetted){
         this.isBetted=isBetted;
    } 
    /**
     * Get danh sách cardId
     * @return 
     */
    public List<Integer> getListCardIds(){
       List<Integer> cardIds= new ArrayList<>();
        for (Card card : this.cards) {
            cardIds.add((int) card.getId());
        }
        return cardIds;
    }

    public BigDecimal getMoneyWinLose() {
        return moneyWinLose;
    }

    public void setMoneyWinLose(BigDecimal moneyWinLose) {
        this.moneyWinLose = moneyWinLose;
    }

    public boolean isShowCardsAll() {
        return listShow.size() >= 3;
    }
    
    /**
     * card chưa lật thì =-1
     * @return 
     */
    public List<Short> getListShowOnReturn() {
        List<Short> list = new ArrayList<>(listShow);
        int size = list.size();
        for(int i=size; i<3;i++){
            list.add((short)-1);
        }
        return list;
    }
    
    public void addListShow(short cardId) {
        if(listShow.contains(cardId)){
            return;
        }
        if (this.listShow.size() >= 3) {
            return ;
        }
         this.listShow.add(cardId);
    }
}
