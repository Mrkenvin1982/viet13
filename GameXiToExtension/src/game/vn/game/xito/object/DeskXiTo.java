/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 *Dung de chia bai cho nguoi choi
 * @author tuanp
 */
public class DeskXiTo {

    public static final int NUM_CARD = 52;
    public static final XiToCard[] XI_TO_CARDS;

    static {
        XI_TO_CARDS = new XiToCard[NUM_CARD];
        for (byte i = 0; i < XI_TO_CARDS.length; i++) {
            XI_TO_CARDS[i] = new XiToCard(i);
        }
    }
    private List<XiToCard> cards = new ArrayList<>();
    private transient int usedcardnumber = 0;

    public DeskXiTo(int startCard) {
        for (int i = startCard; i < NUM_CARD; i++) {
            XiToCard card = XI_TO_CARDS[i];
            card.setIsHideCard(true);
            cards.add(XI_TO_CARDS[i]);
        }
        Collections.shuffle(cards);
    }

    /**
     * tạo bộ bài test
     *
     * @param setCards
     * @param startCard
     */
    public DeskXiTo(String setCards,int startCard) {
        try {
            cards.clear();
            ResourceBundle rb = ResourceBundle.getBundle(DeskXiTo.class.getName());
            String scards = rb.getString(setCards);
            String[] aCards = scards.split(",");
            for (String c : aCards) {
                String[] array = c.split(":");
                byte id = (byte) (Byte.parseByte(array[0]) * 4 + Byte.parseByte(array[0]));
                cards.add(new XiToCard(id));
            }
        } catch (Exception e) {
        }
        //add full card
        for (int i = startCard; i < NUM_CARD; i++) {
            if (!cards.contains(XI_TO_CARDS[i])) {
                cards.add(XI_TO_CARDS[i]);
            }
        }
    }
    
        /**
     * tạo bộ bài test
     * @param setCard 
     */
    public DeskXiTo(String setCard){
        try{
            ResourceBundle rb = ResourceBundle.getBundle(DeskXiTo.class.getName());
            String scards = rb.getString(setCard);
            String[] aCards = scards.split(",");
            cards.clear();
            for(String c : aCards){
                cards.add(new XiToCard(Byte.parseByte(c)));
            }
        }catch(Exception e){
            
        }
    }

    public XiToCard dealCard() {
        return this.cards.get(usedcardnumber++);
    }

    /**
     * chia bài theo thứ tự và chất để test
     *
     * @param number
     * @param type
     * @return
     */
    public XiToCard dealCard(int number, int type) {
        for (XiToCard card : cards) {
            if (card.getCardNumber() == number && card.getCardType() == type) {
                return card;
            }
        }
        return null;
    }
}
