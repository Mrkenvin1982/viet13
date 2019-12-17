/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.object;

import game.vn.game.phom.utils.PhomUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tuanp
 */
public class PhomPlayer implements Comparable<PhomPlayer> {

    private static Logger log = LoggerFactory.getLogger(PhomPlayer.class.getName());
    /**
     * bài đã ăn
     */
    private List<Byte> cardEat = new ArrayList<>();
    /**
     * Bài đã đánh, bài rác
     */
    private List<Byte> cardMove = new ArrayList<>();
    /**
     * Bài đã rút
     */
    private List<Byte> cardGet = new ArrayList<>();
    /*
     * K: Bai da bi an
     */
    private List<Byte> cardBeEated = new ArrayList<>();
    /**
     * Danh sách bài được chia
     */
    private List<CardPhom> cardDeal = new ArrayList<>();
    /**
     * Danh sách bài hiện tại
     */
    private List<CardPhom> currCards = new ArrayList<>();
    /**
     * So Phom cua nguoi choi sau khi ha Phom cach nhau -1
     */
    private List<Phom> cardPhom = new ArrayList<>();
    /*
     * tien trong van choi 
     * 
     */
    private BigDecimal bonusMoney = BigDecimal.ZERO;
    /**
     * trạng thái player đã thoát khỏi bàn hay chưa dùng khi ván còn 3 người thì
     * người thoát vẫn tự động đánh
     */
    private boolean isLeaved = false;
    /**
     * điểm của người chơi
     */
    private int scores = 0;
    /**
     * trạng thái player đã hạ phỏm hay chưa
     */
    private boolean isHa = false;
    /**
     * trạng thái player có cháy hay không
     */
    private boolean isChay = false;
    /**
     * Ghi lại lý do thay đổi tiền
     */
//    private String logEventMoney = "";
    /**
     * Trạng thái player đã bốc bài hay chưa
     */
    private boolean isGotCard = false;
    /**
     * Kiểm tra trạng thái ù của player
     */
    private boolean isU = false;
    /**
     * bị thua ù
     */
    private boolean isThuaU = false;
    /**
     * đếm số lượt player đã đánh
     */
    private int countTurn = 0;
    /**
     * đánh dấu người thắng
     */
    private boolean isWinner = false;
    /**
     * xác định lượt tái hạ
     */
    private boolean isTaiHa = false;
    private long timeHaPhom = 0;
    private int seat=-1;
    
    public void reset(){
        cardEat.clear();
        cardMove.clear();
        cardGet.clear();
        cardBeEated.clear();
        cardDeal.clear();
        currCards.clear();
        cardPhom.clear();
        bonusMoney= BigDecimal.ZERO;
        isLeaved=false;
        scores=0;
        isHa=false;
        isChay=false;
        isGotCard = false;
        isU=false;
        isThuaU = false;
        countTurn = 0;
        isWinner = false;
        isTaiHa = false;
        timeHaPhom = 0;
        seat=-1;
    }
    /*
     * K: Kiem tra bai nay da bi an hay chua?
     * @param cardId
     */

    public boolean isCardBeEated(byte cardId) {
        if (getCardBeEated().contains(cardId)) {
            return true;
        }
        return false;
    }

    /*
     * K: Them bai da bi an
     * @param cardId
     */
    public void addCardBeEated(int cardId) {
        this.getCardBeEated().add((byte)cardId);
    }
    /*
     *K: tra ve so bai da bi an
     */

    public int numCardBeEated() {
        return this.getCardBeEated().size();
    }

    /**
     * Thêm bài đã rút
     *
     * @param card
     */
    public void addCardGet(CardPhom card) {
        this.cardGet.add(card.getId());
        addCurrCard(card);
    }
    /**
     * Thêm bài đã ăn
     *
     * @param cardId
     */
    public void addCardEat(byte cardId) {
        this.cardEat.add(cardId);
    }

    /**
     * Thêm bài đã đánh
     *
     * @param cardId
     */
    public void addCardMove(byte cardId) {
        this.getCardMove().add(cardId);
        if (this.getCardMove().size() == 4) {
            this.setIsHa(true);
        }
    }

    public byte getLastCardIdMove() {
        return this.getCardMove().get(this.getCardMove().size() - 1);
    }

    /**
     * Kiểm tra nếu quân bài này user đã ăn
     *
     * @param cardId
     * @return
     */
    public boolean isEatenCard(byte cardId) {
        if (cardEat == null || cardEat.isEmpty()) {
            return false;
        }
        for (Byte cId : cardEat) {
            if (cId == cardId) {
                return true;
            }
        }
        return false;
    }

    public int getNumCardsEat() {
        return this.cardEat.size();
    }

    public List<Byte> getCardEat() {
        return cardEat;
    }
    /**
     * Trả về số quân bài player đã đánh
     *
     * @return
     */
    public int numCardMove() {
        return this.getCardMove().size();
    }

    public void removeCardMove(byte cardId) {
        this.getCardMove().remove((Byte) cardId);
    }

    @Override
    public Object clone() {
        try {
            PhomPlayer result = (PhomPlayer) super.clone();

            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * @return the cardMove
     */
    public List<Byte> getCardMove() {
        return cardMove;
    }

    /**
     * @param cardMove the cardMove to set
     */
    public void setCardMove(List<Byte> cardMove) {
        this.cardMove = cardMove;
    }

    /**
     * @return the cardBeEated
     */
    public List<Byte> getCardBeEated() {
        return cardBeEated;
    }

    /**
     * @param cardBeEated the cardBeEated to set
     */
    public void setCardBeEated(List<Byte> cardBeEated) {
        this.cardBeEated = cardBeEated;
    }

    /**
     * @return the bonusMoney
     */
    public BigDecimal getBonusMoney() {
        return bonusMoney;
    }

    public void setBonusMoney(BigDecimal bonusMoney) {
        this.bonusMoney = bonusMoney;
    }

    public boolean isIsLeaved() {
        return isLeaved;
    }

    public void setIsLeaved(boolean isLeaved) {
        this.isLeaved = isLeaved;
    }

    public boolean isIsHa() {
        return isHa;
    }

    public void setIsHa(boolean isHa) {
        this.isHa = isHa;
        if (isHa) {
            this.timeHaPhom = System.currentTimeMillis();
        }
    }

    public List<CardPhom> getCardDeal() {
        Collections.sort(cardEat);
        return cardDeal;
    }

    public List<Byte> getCardDealId() {
        List<Byte> result = new ArrayList<>();
        for (CardPhom cp : cardDeal) {
            result.add(cp.getId());
        }
        return result;
    }

    public synchronized void addCardDeal(CardPhom cp) {
        this.cardDeal.add(cp);
    }

    public void sortCards() {
        Collections.sort(cardDeal);
    }

    public void sortCurrCards() {
        Collections.sort(currCards);
    }

    /**
     * @return the cardPhom
     */
    public List<Phom> getCardPhom() {
        return cardPhom;
    }

    public List<Byte> getCardsIdInPhom() {
        List<Byte> temp = null;
        List<Byte> result = new ArrayList<>();
        for (Phom phom : cardPhom) {
            temp = new ArrayList<>(phom.getCardIds());
            result.addAll(temp);
        }
        if (temp == null) {
            return new ArrayList<>();
        }
        return result;
    }
    
    public void addCardPhom(Phom phom) {
        if (!this.cardPhom.contains(phom)) {
            this.cardPhom.add(phom);
        }
    }

    /**
     * remove phom bang card id
     *
     * @param cardId
     */
    public void removePhomByCardId(Byte cardId) {
        Phom removePhom = null;
        for (Phom phom : cardPhom) {
            if (phom.isContainCardId(cardId)) {
                removePhom = phom;
            }
        }
        if (removePhom != null) {
            //trả mấy con còn lai về bộ bài đang cầm current card
            for (Byte b : removePhom.getCardIds()) {
                if (b != cardId) {
                    addCurrCard(new CardPhom(b));
                }
            }
            cardPhom.remove(removePhom);
        }
    }

    /**
     * kiểm tra xem bài đã có trong danh sách phỏm chưa
     *
     * @param cardId
     * @return
     */
    private boolean isPhomContainCardId(Byte cardId) {
        for (Phom phom : cardPhom) {
            if (phom.isContainCardId(cardId)) {
                log.debug(PhomUtils.getCardName(cardId) + " is contain in Phom " + phom);
                return true;
            }
        }
        return false;
    }

    /**
     * thêm danh sách cardid vô phỏm
     *
     * @param cards
     */
    public void addCardPhoms(List<Byte> cards) {
        Phom p = new Phom(cards);
        //kiểm tra xem bài này đã có trong các phỏm hiện tại ko
        for (Byte b : cards) {
            removePhomByCardId(b);
        }
        //phỏm này chưa tồn tại thì add
        addCardPhom(p);
        removeCurrCard(cards);
    }

    /**
     * thêm một quân bài vô phỏm đã có
     *
     * @param cardId
     * @param phom
     * @return
     */
    public boolean addCardPhoms(Byte cardId, Phom phom) {
        //kiểm tra xem bài này đã có trong các phỏm hiện tại ko
        if (isPhomContainCardId(cardId)) {
            return false;
        }
        //phỏm này chưa tồn tại thì add
        phom.addCard(cardId);
        return true;
    }

    /**
     * *
     * @return so card phom sau khi ha
     */
    public int numCardPhom() {
        return this.getCardPhom().size();
    }

    /**
     * rồi khỏi bàn thì coi như cháy
     *
     * @return
     */
    public int getScores() {
        if (this.isThuaU) {
            return -2;
        }
        //bài ko phỏm và ko ù khan thì cháy
        if (this.cardPhom.isEmpty() && !isU) {
            setIsChay(true);
        }

        if (this.isLeaved || this.isIsChay()) {
            return -1;
        }

        if (isU) {
            return 0;
        }
        //reset player score
        //tinh diem lai
        scores = 0;
        for (CardPhom cp : getCurrCards()) {
            addScores(cp.getCardNumber());
        }
        return scores;
    }

    public void setScores(int scores) {
        this.scores = scores;
    }

    public void addScores(int scores) {
        this.scores += scores;
    }

    public synchronized void addCurrCard(CardPhom c) {
        if (!currCards.contains(c)) {
            this.currCards.add(c);
        }
    }

    public void removeCurrCard(byte c) {
        this.currCards.remove(new CardPhom(c));
    }

    public void removeCurrCard(byte[] arr) {
        for (byte b : arr) {
            this.currCards.remove(new CardPhom(b));
        }
    }

    public void removeCurrCard(List<Byte> list) {
        for (Byte b : list) {
            this.currCards.remove(new CardPhom(b));
        }
    }

    public CardPhom getBiggestCurrentCard() {
        Collections.sort(currCards);
        return currCards.get(currCards.size() - 1);
    }

    public List<CardPhom> getCurrCards() {
        if (isChay || isThuaU) {
//            currCards.clear();
            return currCards;
        }
        Collections.sort(currCards);
        return currCards;
    }

    public List<Byte> getCurrCardsId() {
        List<Byte> temp = new ArrayList<>();
//        if (isChay || isThuaU) {
//            currCards.clear();
//            return temp;
//        }
        Collections.sort(currCards);

        for (CardPhom c : currCards) {
            temp.add(c.getId());
        }
        return temp;
    }
    public List<Short> getCurrCardsId2Array(){
        Collections.sort(currCards);
        List<Short> ids= new ArrayList<>();

        for(int i=0;i<currCards.size();i++){
            ids.add((short)currCards.get(i).getId());
        }
        return ids;
    }

    public void setCurrCards(List<CardPhom> currCards) {
        this.currCards = currCards;
    }

    public boolean isIsChay() {
        return isChay;
    }

    public void setIsChay(boolean isChay) {
        this.isChay = isChay;
    }
    public boolean isIsGotCard() {
        return isGotCard;
    }

    public void setIsGotCard(boolean isGotCard) {
        this.isGotCard = isGotCard;
    }

    public void resetIsGotCard() {
        this.isGotCard = false;
    }

    public boolean isIsU() {
        return isU;
    }

    public void setIsU(boolean isU) {
        this.isU = isU;
    }

    public boolean isIsThuaU() {
        return isThuaU;
    }

    public synchronized void setIsThuaU(boolean isThuaU) {
        this.isThuaU = isThuaU;
    }

    public int getCountTurn() {
        return countTurn;
    }

    public void setCountTurn(int countTurn) {
        this.countTurn = countTurn;
    }

    public void incrCountTurn() {
        countTurn++;
    }

    /**
     * lấy danh sách phỏm chưa hạ
     *
     * @return
     */
    private List<Phom> getPhomChuaHa() {
        List<Phom> list = new ArrayList<>();

        for (Phom p : cardPhom) {
            if (!p.isIsHa()) {
                list.add(p);
            }
        }
        return list;
    }

    public List<CardPhom> getCardPhomChuaHa() {
        List<Phom> list = new ArrayList<>();
        List<CardPhom> list2 = new ArrayList<>();

        for (Phom p : cardPhom) {
            if (!p.isIsHa()) {
                list.add(p);
            }
        }

        if (!list.isEmpty()) {
            for (Phom p : list) {
                list2.addAll(p.getCardPhom());
            }
        }
        return list2;
    }

    /**
     * trả những quân bài trong phỏm chưa hạ, về bộ bài đang cầm trên tay (ko có
     * phỏm) dùng khi reset phỏm
     */
    private void moveCardFromPhomToCurrentCard() {
        this.currCards.addAll(getCardPhomChuaHa());
        this.cardPhom.removeAll(getPhomChuaHa());
    }

    /**
     * remove những phỏm chưa hạ
     */
    public void resetPhom() {
        if (cardPhom.isEmpty()) {
            return;
        }
        moveCardFromPhomToCurrentCard();
    }

    public boolean isIsWinner() {
        return isWinner;
    }

    public void setIsWinner(boolean isWinner) {
        this.isWinner = isWinner;
    }

    public boolean isIsTaiHa() {
        return isTaiHa;
    }

    public void setIsTaiHa(boolean isTaiHa) {
        this.isTaiHa = isTaiHa;
        if (isTaiHa) {
            this.timeHaPhom = System.currentTimeMillis();
        }
    }

    public double getTimeHaPhom() {
        return timeHaPhom;
    }

    @Override
    public int compareTo(PhomPlayer player) {

        if (isLeaved || isChay) {
            return 1;
        } else if (player.isIsChay() || player.isIsLeaved()) {
            return -1;
        } else {
            if (this.getScores() > player.getScores()) {
                return 1;
            } else if (this.getScores() < player.getScores()) {
                return -1;
            } else {
                if (this.getTimeHaPhom() > player.getTimeHaPhom()) {
                    return 1;
                } else if (this.getTimeHaPhom() < player.getTimeHaPhom()) {
                    return -1;
                }
                return 0;
            }
        }
    }

    @Override
    public String toString() {
        scores = 0;
        for (CardPhom cp : getCurrCards()) {
            addScores(cp.getCardNumber());
        }
        StringBuilder returnString = new StringBuilder("userId: ")
                .append(" bonusMoney: ")
                .append(bonusMoney)
                .append(" isLeaved: ").append(isLeaved)
                .append(" isChay: ").append(isChay)
                .append(" isU: ").append(isU)
                .append(" scores: ").append(scores)
                .append(" numCardMoved: ").append(numCardMove())
                .append(" isHa: ").append(isHa)
                .append(" currentCardId");
        for (CardPhom c : currCards) {
            returnString.append(" ").append(PhomUtils.getCardName(c.getId()));
        }

        returnString.append(" currentPhomCardId");
        for (Phom phom : cardPhom) {
            for (Byte b : phom.getCardIds()) {
                returnString.append(" ").append(PhomUtils.getCardName(b));
            }
            returnString.append(" | ");
        }
        return returnString.toString();
    }

    public String getStringCards() {
        StringBuilder str = new StringBuilder();
        for (CardPhom card : currCards) {
            str.append(PhomUtils.getCardName(card.getId())).append(" ");
        }
        return str.toString();
    }
    /**
     * Lấy thong tin cards cua user để ghi log
     * @return 
     */
    public String getInforCards(){
        return String.format("(%d điểm - %s)", scores,getStringCards());
    }
    public void setSeat(int seatInput){
        this.seat=seatInput;
    }
    public int getSeat(){
        return this.seat;
    }

    @Override
    public boolean equals(Object obj) {
         if (this == obj) {
            return true;
        }
        if (!(obj instanceof PhomPlayer)) {
            return false;
        }
        PhomPlayer b = (PhomPlayer) obj;
        return this.getSeat() == b.getSeat();
    }
}
