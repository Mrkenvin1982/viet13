/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom.object;

import game.vn.game.phom.utils.PhomUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tuanp
 */
public class Phom implements Cloneable{
    /**
     * Phỏm đã hạ
     */
    private Set<Byte> cardIds = new HashSet<>();
    private long id;
    /**
     * đánh dấu phỏm đã hạ hay chưa
     */
    private boolean isHa=false;

    public Phom(List<Byte> cardIds) {
//        List<Byte> cardPhom = new ArrayList<Byte>(cardIds);
        this.cardIds.addAll(cardIds);
        StringBuilder uniId = new StringBuilder();
        for(Byte b: cardIds){
            uniId.append(b);
        }
        //dung Long.ParseLong khi uniId la 1 day so qua lon
        this.id = Long.parseLong(uniId.toString());
    }

    public Set<Byte> getCardIds() {
        return cardIds;
    }
    public List<Short> getCardIds2List(){
        List<Short> cards = new ArrayList<>();
        for(byte id:this.cardIds){
            cards.add((short)id);
        }
        return cards;
    }

    public List<CardPhom> getCardPhom() {
        List<CardPhom> list = new ArrayList<>();
        if(!cardIds.isEmpty()){
            for(Byte b: cardIds){
                list.add(new CardPhom(b));
            }
        }
        return list;
    }

    public Set<Byte> getCloneCardIds() {
//        List<Byte> listTemp = new ArrayList<Byte>();
//        for(int i = 0; i < cardIds.size(); i++){
//            listTemp.add((byte) -1);
//        }
//        Collections.copy(listTemp, cardIds);

        return new HashSet<>(cardIds);
    }

    public void addCard(Byte b){
        this.cardIds.add(b);
    }

    /**
     * kiem tra xem con bai nay co trong phom hay chua
     * @param cardId
     * @return
     */
    public boolean isContainCardId(Byte cardId){
        return this.cardIds.contains(cardId);
    }
    /**
     * Kiểm tra xem phỏm có chứa 1 trong các con bài trong list không
     * @param listCardId
     * @return 
     */
    public boolean isContainOneOfCard(List<Byte> listCardId){
        if(listCardId == null || listCardId.isEmpty()){
            return false;
        }
        for (byte b : listCardId){
            if(isContainCardId(b)){
                return true;
            }
        }
        return false;
    }
    public boolean isIsHa() {
        return isHa;
    }

    public void setIsHa(boolean isHa) {
        this.isHa = isHa;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Phom other = (Phom) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (int) (79 * hash + this.id);
        return hash;
    }
    
    @Override
    public String toString(){
        StringBuilder returnString = new StringBuilder("phom id: ").append(this.id);
        returnString.append(" cardId: ");
        for(Byte b: cardIds){
            returnString.append(" ").append(PhomUtils.getCardName(b));
        }
        return returnString.toString();
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

