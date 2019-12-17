/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * A: card number=1
 * 2: card number=2
 * K: card number =13
 * 
 * @author hoanghh
 */
public class DeckTest {
    private static final int _3_A = 1;
    private static final int _3_K = 2;
    private static final int _TEST_1 = 3;
    private static final int DEFAULT_NUMBER_XAM_CARD = 9;


    
    private final transient CardSetPhom cardSet;
    private final List<CardPhom> cardUsed;
    List<CardPhom> cards5 = new ArrayList<>();
    public DeckTest() {
        cardSet = new CardSetPhom(52);
        cardUsed = new ArrayList<>();
        cards5.add(dealCard(3, 2));
        cards5.add(dealCard(9,1));
    }
    
    public void reset(){
        cardSet.xaoBai();
        cardUsed.clear();
    }
    
    public CardPhom dealCard(int number, int type){
        CardPhom c = cardSet.dealCard(number, type);
        cardUsed.add(c);
        return c;
    }
    
    public CardPhom dealCard(){
        CardPhom c = cardSet.dealCard();
        while(cardUsed.contains(c)){
            c = cardSet.dealCard();
        }
        return c;
    }
    
    
    public void addFullCard(List<CardPhom> cards) {
        while (cards.size() < DEFAULT_NUMBER_XAM_CARD) {
            CardPhom c = dealCard();
            if (!cardUsed.contains(c)) {
                cards.add(c);
                cardUsed.add(c);
            }
        }
        Collections.sort(cards);
    }

    public List<CardPhom> getTestCase(int testcase) {
        List<CardPhom> cards = new ArrayList<>();
        switch(testcase){
            case _3_A:
                cards=get3A();
                break;
            case _3_K:
                cards=get3K();
                break;
             case 3:
                cards=getTest3();
                break;
              case 4:
                cards=getTest5();
                break;

        }
        return cards;
    }

    public List<CardPhom> getTest1() {
        return cards5;
    }
    private List<CardPhom> getTest2() {
        List<CardPhom> cards5 = new ArrayList<>();
        //3,0;5,0;8,2;6,2;8,3;13,3;13,1;8,1;4,1;
        cards5.add(dealCard(3, 0));
        cards5.add(dealCard(5,0));
        cards5.add(dealCard(8,2));
        cards5.add(dealCard(6,2));
        cards5.add(dealCard(8,3));
        cards5.add(dealCard(13,3));
        cards5.add(dealCard(13,1));
        cards5.add(dealCard(8,1));
        cards5.add(dealCard(4,1));
         return cards5;
    }

     private List<CardPhom> getTest3() {
        List<CardPhom> cards5 = new ArrayList<>();
        cards5.add(dealCard(1,0));
        cards5.add(dealCard(1,1));
        cards5.add(dealCard(1,2));
        cards5.add(dealCard(13,0));
        cards5.add(dealCard(13,1));
        cards5.add(dealCard(13,3));
        cards5.add(dealCard(7,3));
        cards5.add(dealCard(8,1));
        cards5.add(dealCard(10,1));
         return cards5;
    }
     
    public List<CardPhom> getTest4() {
        List<CardPhom> cards5 = new ArrayList<>();
        //11,3;11,2;11,1;8,3
        cards5.add(dealCard(11,3));
        cards5.add(dealCard(11,2));
        cards5.add(dealCard(11,1));
        cards5.add(dealCard(8,3));
        return cards5;
    }
      
    public List<CardPhom> getTest5() {
        List<CardPhom> cards5 = new ArrayList<>();
        cards5.add(dealCard(7,0));
        cards5.add(dealCard(7,2));
        return cards5;
    }
                
    private List<CardPhom> get3A() {
        List<CardPhom> cards5 = new ArrayList<>();
        cards5.add(dealCard(2, 0));
        cards5.add(dealCard(2, 1));
        cards5.add(dealCard(2, 2));
        return cards5;
    }
    
    private List<CardPhom> get3K() {
        List<CardPhom> cards5 = new ArrayList<>();
        cards5.add(dealCard(13, 0));
        cards5.add(dealCard(13, 1));
        cards5.add(dealCard(13, 2));
        return cards5;
    }
    
}
