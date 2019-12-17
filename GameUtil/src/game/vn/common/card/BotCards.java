/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.card;

import game.vn.common.card.object.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * sử dụng để chọn ra bài lớn nhất cho pot
 * @author 
 */
public class BotCards {
    
    // đôi, sảnh
    private static final int PAIR_AND_TRAIGHT = 10;
    //HEO
    private static final int HEO = 35;
    //doi thong
    private static final int PAIR_CONT = 70;
    //tứ quý.
    private static final int FOUR_OF_A_KIND = 245;
    //tới trắng
    public static final int FORCE_FINISH = 3000;
    
    private List<Card> cards = new ArrayList<>();
    private final List<Card> specialCards = new ArrayList<>();
    
    private int value;
    
    public void reset(){
        this.value = 0;
        this.cards.clear();
        this.specialCards.clear();
    }
    
    public void setForceFinish(){
        this.value = FORCE_FINISH;
    }
    
    public void caculateValue(){
        Collections.sort(cards);
        
        int count4Quy = countTuQuyInListCard(cards);
        value = FOUR_OF_A_KIND * count4Quy;
        
        int countHeo = countHeo(cards);
        value += (HEO * countHeo);
        
        int countDoiThong = demDoiThong(cards);
        if(countDoiThong >=3){
             value += (countDoiThong * PAIR_CONT);
        }
        processPair();
        processStraight();
        
        for(Card card : cards){
            value += card.getId();
        }
    }
    
    
    public void addCard(Card card){
        cards.add(card);
    }

    private int countHeo(List<Card> cards) {
        int countHeo = 0;
        for (int i = cards.size()-4; i < cards.size(); i++) {
            if (cards.get(i).getCardNumber() != 12) {
                continue;
            }
            countHeo ++;
        }
        return countHeo;
    }

    private int countTuQuyInListCard(List<Card> bai) {
        int count = 0;
        for (int i = 0; i < bai.size() - 3; i++) {
            if (CardUtil.isTuQuy(new Card[]{bai.get(i), bai.get(i + 1), bai.get(i + 2), bai.get(i + 3)})) {
                this.specialCards.add(bai.get(i));
                this.specialCards.add(bai.get(i+1));
                this.specialCards.add(bai.get(i+2));
                this.specialCards.add(bai.get(i+3));
                count++;
            }
        }
        return count;
    }
    
    /**
     * IS PAIR
     *
     * @return
     */
    private void processPair() {;
        try {
            int count = 0;
            for (int i = 1; i < cards.size(); i++) {
                if(this.specialCards.contains(cards.get(i - 1))){
                    continue;
                }
                int temp = cards.get(i - 1).getCardNumber() - cards.get(i).getCardNumber();
                switch (temp) {
                    case 0:
                        count++;
                        if (count == 1) {
                            //2 lá đầu tiên của thì * 2
                            value += (2 * PAIR_AND_TRAIGHT);
                        }
                        if (count >= 2) {
                            value += PAIR_AND_TRAIGHT;
                        }
                        break;
                    default:
                        count = 0;
                        break;
                }
            }
            
        } catch (Exception e) {
        }

    }
    
    /**
     * Xư lý trọng trọng số cho sảnh
     */
    private void processStraight() {
        try {
            int countStraight = 0;
            List<Card> straightTemp = new ArrayList<>();
            for (int i = 2; i < cards.size(); i++) {
                if(this.specialCards.contains(cards.get(i - 2)) || this.specialCards.contains(cards.get(i - 1))){
                    continue;
                }
                int temp = cards.get(i - 1).getCardNumber() - cards.get(i - 2).getCardNumber();
                if(cards.get(i - 1).getCardNumber() == 12){
                    continue;
                }
                switch (temp) {
                    case 0:
                        break;
                    case 1:
                        
                        if(!straightTemp.contains(cards.get(i - 2))){
                            straightTemp.add(cards.get(i - 2));
                        }
                        if(!straightTemp.contains(cards.get(i - 1))){
                            straightTemp.add(cards.get(i - 1));
                        }
                        
                        countStraight++;
                        if(countStraight == 2){
                            //3 card đầu tiền của sảnh
                            value += (3* PAIR_AND_TRAIGHT);
                        }
                        if(countStraight > 2){
                            value += PAIR_AND_TRAIGHT;
                        }
                        break;
                    default:
                        countStraight = 0;
                        straightTemp.clear();
                        break;
                }

            }
        } catch (Exception e) {
        }
    }
    
    private int demDoiThong(List<Card> bai) {
        // boolean valid = false;
        List<Card> cards = new ArrayList<>() ;
        int max = 0;
        int count = 0;
        for (int i = 0; i < bai.size() - 1; i++) {
            if (bai.get(i).getId() >= 48) {
                break;
            }
            if (count == 0 && bai.get(i).getCardNumber() == bai.get(i + 1).getCardNumber()) {
                count = 1;
                if (cards.isEmpty()) {
                    cards.add(bai.get(i));
                    cards.add(bai.get(i + 1));
                }
            } else {
                if (count % 2 != 0) {
                    if (bai.get(i).getCardNumber()== bai.get(i + 1).getCardNumber() - 1) {
                        count++;
                    } else if (bai.get(i).getCardNumber()!= bai.get(i + 1).getCardNumber()) {
                        // valid = false;
                        if (count > max) {
                            max = count;
                        }
                        if(cards.size()<6){
                            cards.clear();
                        }
                        count = 0;
                    }
                } else {
                    if (bai.get(i).getCardNumber()== bai.get(i + 1).getCardNumber()) {
                        if(!cards.contains(bai.get(i))){
                            cards.add(bai.get(i));
                        }
                        if(!cards.contains(bai.get(i+1))){
                            cards.add(bai.get(i+1));
                        }
                        count++;
                    } else {
                        // valid = false;
                        if (count > max) {
                            max = count;
                        }
                        if(cards.size()<6){
                            cards.clear();
                        }
                        count = 0;
                    }
                }
            }
        }
        if (count > max) {
            max = count;
        }
        if (max >= 5) {
            this.specialCards.addAll(cards);
        }
        return (max + 1) / 2;
    }

    public int getValue() {
        return value;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
