/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.object;

import game.vn.game.xito.lang.XiToLanguage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * tính kết quả loại bài của user
 * @author tuanp
 */
public class XiToCardUtil {
    public static final byte ROYAL_FLUSH_VALUE = 9;
    public static final byte STRAIGHT_FLUSH_VALUE = 8;
    public static final byte FOUR_OF_A_KIND_VALUE = 7;
    public static final byte FULL_HOUSE_VALUE = 6;
    public static final byte FLUSH_VALUE = 5;
    public static final byte STRAIGHT_VALUE = 4;
    public static final byte THREE_OF_A_KIND_VALUE = 3;
    public static final byte TWO_PAIRS_VALUE = 2;
    public static final byte PAIR_VALUE = 1;
    public static final byte HIGH_CARD_VALUE = 0;

    /**
     * Kiem tra neu la doi
     *
     * @param list
     * @return
     */
    private static boolean isPair(List<XiToCard> list) {
        if (list.size() != 2) {
            return false;
        }
        if (list.get(0).getCardNumber() != list.get(1).getCardNumber()) {
            return false;
        }
        return true;
    }

    /**
     * Lay cap lon nhat trong 5 la
     *
     * @param list
     * @return tra ve cap lon nhat trong 5 la, ko co tra ve null
     */
    private static List<XiToCard> getHighestPair(List<XiToCard> list) {
        if (list.size() == 2 && isPair(list)) {
            return list;
        }
        if (list.size() == 3) {
            //xet 2 la sau
            if (isPair(list.subList(1, 3))) {
                return list.subList(1, 3);
            }
            //xet 2 la dau
            if (isPair(list.subList(0, 2))) {
                return list.subList(0, 2);
            }
        }
        if (list.size() == 4) {
            if (isPair(list.subList(2, 4))) {
                return list.subList(2, 4);
            }
            if (isPair(list.subList(1, 3))) {
                return list.subList(1, 3);
            }
            if (isPair(list.subList(0, 2))) {
                return list.subList(0, 2);
            }
        }
        if (list.size() == 5) {
            if (isPair(list.subList(3, 5))) {
                return list.subList(3, 5);
            }
            if (isPair(list.subList(2, 4))) {
                return list.subList(2, 4);
            }
            if (isPair(list.subList(1, 3))) {
                return list.subList(1, 3);
            }
            if (isPair(list.subList(0, 2))) {
                return list.subList(0, 2);
            }
        }
        return null;
    }

    /**
     * Xet 4 hoac 5 la: neu co 2 doi, tra ve 2 doi 
     *
     * @param list
     * @return co doi tra ve doi, ko co tra ve null
     */
    private static List<XiToCard> getTwoPairs(List<XiToCard> list) {
        Collections.sort(list);
        if (list.size() == 4) {
            if (isPair(list.subList(0, 2)) && isPair(list.subList(2, 4))) {
                return list.subList(2, 4);
            }
        }
        if (list.size() == 5) {
            //xet 2 con dau tien, ko phai la cap thi 4 con con lai phai la mot cap
            if (!isPair(list.subList(0, 2))) {
                if (isPair(list.subList(1, 3)) && isPair(list.subList(3, 5))) {
                    return list.subList(1, 5);
                }
            } else {
                //2 la dau la 1 cap, tim doi trong 3 la con lai
                if (isPair(list.subList(2, 4))) {
                    return list.subList(0, 4);
                }
                if (isPair(list.subList(3, 5))) {
                    List<XiToCard> listTeamp=new ArrayList<XiToCard>();
                    listTeamp.add(list.get(0));
                    listTeamp.add(list.get(1));
                    listTeamp.add(list.get(3));
                    listTeamp.add(list.get(4));
                    return listTeamp;
//                    return list.subList(3, 5);
                }
            }
        }
        return null;
    }

    /**
     * Kiem tra 3 la co phai la dong chat hay ko
     *
     * @param list
     * @return dong chat tra ve true, ko thi false
     */
    private static boolean isThreeOfAKind(List<XiToCard> list) {
        Collections.sort(list);
        if (list.size() != 3) {
            return false;
        }
        if (list.get(0).getCardNumber()== list.get(1).getCardNumber()
                && list.get(0).getCardNumber()== list.get(2).getCardNumber()) {
            return true;
        }
        return false;
    }

    /**
     * Kiem tra xam chi: xet bai co 3 -> 5 la
     *
     * @param list
     * @return tra ve bo 3 xam chi, ko thi tra ve null
     */
    private static List<XiToCard> getThreeOfAKind(List<XiToCard> list) {
        Collections.sort(list);
        if (list.size() == 3) {
            if (isThreeOfAKind(list)) {
                return list;
            }
        }
        if (list.size() == 4) {
            if (isThreeOfAKind(list.subList(0, 3))) {
                return list.subList(0, 3);
            }
            if (isThreeOfAKind(list.subList(1, 4))) {
                return list.subList(1, 4);
            }
        }
        if (list.size() == 5) {
            if (isThreeOfAKind(list.subList(0, 3))) {
                return list.subList(0, 3);
            }
            if (isThreeOfAKind(list.subList(1, 4))) {
                return list.subList(1, 4);
            }
            if (isThreeOfAKind(list.subList(2, 5))) {
                return list.subList(2, 5);
            }
        }
        return null;
    }

    /**
     * Kiem tra tu qui, truong hop 4 va 5 la
     *
     * @param list
     * @return 4 la tu qui, ko thi tra ve null
     */
    private static List<XiToCard> getFourOfAKind(List<XiToCard> list) {
        Collections.sort(list);
        if (list.size() == 4) {
            XiToCard firstCard = list.get(0);
            for (int i = 1; i < 4; i++) {
                XiToCard card = list.get(i);
                if (firstCard.getCardNumber() != card.getCardNumber()) {
                    return null;
                }
                firstCard = card;
            }
            return list;
        }
        if (list.size() == 5) {
            //xet 2 la dau, khac nha thi tinh tu la thu 2 toi la thu 5
            if (list.get(0).getCardNumber() != list.get(1).getCardNumber()) {
                XiToCard firstCard = list.get(1);
                for (int i = 2; i < 5; i++) {
                    XiToCard card = list.get(i);
                    if (firstCard.getCardNumber() != card.getCardNumber()) {
                        return null;
                    }
                    firstCard = card;
                }
                return list.subList(1, 5);
            } else {
                XiToCard firstCard = list.get(0);
                for (int i = 1; i < 4; i++) {
                    XiToCard card = list.get(i);
                    if (firstCard.getCardNumber() != card.getCardNumber()) {
                        return null;
                    }
                    firstCard = card;
                }
                return list.subList(0, 4);
            }
        }
        return null;
    }

    /**
     * Xet bai la sanh
     *
     * @param list
     * @return
     */
    private static boolean isStraight(List<XiToCard> list) {
        if (list.size() != 5) {
            return false;
        }

        Collections.sort(list);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1).getCardNumber() != list.get(i).getCardNumber() - 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Xet bai la thung: 5 con dong chat
     *
     * @param list
     * @return
     */
    private static boolean isFlush(List<XiToCard> list) {
        if (list.size() != 5) {
            return false;
        }
        Collections.sort(list);
        XiToCard firstCard = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (firstCard.getCardType() != list.get(i).getCardType()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Kiem tra 5 la co phai la thung pha sanh hay ko
     *
     * @param list
     * @return
     */
    private static boolean isStraightFlush(List<XiToCard> list) {
        if (list.size() != 5) {
            return false;
        }
        if (isFlush(list) && isStraight(list)) {
            return true;
        }
        return false;
    }

    /**
     * Sanh chua: sanh ket thuc bang xi
     *
     * @param list
     * @return
     */
    private static boolean isRoyalFlush(List<XiToCard> list) {
        if (list.size() != 5) {
            return false;
        }
        if (isStraight(list) && list.get(4).isAce()) {
            return true;
        }
        return false;
    }

    /**
     * Xet cu lu: mot bo ba va mot bo doi
     *
     * @param list
     * @return tra ve bo ba, ko thi tra ve null
     */
    private static List<XiToCard> getFullHouse(List<XiToCard> list) {
        if (list.size() != 5) {
            return null;
        }
        //xet 3 la dau
        if (isThreeOfAKind(list.subList(0, 3)) && isPair(list.subList(3, 5))) {
            return list.subList(0, 3);
        }
        //xet 2 la dau
        if (isPair(list.subList(0, 2)) && isThreeOfAKind(list.subList(2, 5))) {
            return list.subList(2, 5);
        }
        return null;
    }

    public static ResultCard evalCards(Collection<XiToCard> colCards) {
        List<XiToCard> list = new ArrayList<>(colCards);
        ResultCard result = new ResultCard();
         if (isStraightFlush(list)) {
            result.setValue(STRAIGHT_FLUSH_VALUE);
            result.setStrValue(XiToLanguage.STRAIGHT_FLUSH);
            result.setHighestCards(list);
            return result;
        } else if (getFourOfAKind(list) != null) {
            result.setValue(FOUR_OF_A_KIND_VALUE);
            result.setStrValue(XiToLanguage.FOUR_OF_A_KIND);
            result.setHighestCards(getFourOfAKind(list));
            return result;
        } else if (getFullHouse(list) != null) {
            result.setValue(FULL_HOUSE_VALUE);
            result.setStrValue(XiToLanguage.FULL_HOUSE);
            result.setHighestCards(getFullHouse(list));
            return result;
        } else if (isFlush(list)) {
            result.setValue(FLUSH_VALUE);
            result.setStrValue(XiToLanguage.FLUSH);
            result.setHighestCards(list);
            return result;
        } else if (isStraight(list)||isRoyalFlush(list)) {
            result.setValue(STRAIGHT_VALUE);
            result.setStrValue(XiToLanguage.STRAIGHT);
            result.setHighestCards(list);
            return result;
        } else if (getThreeOfAKind(list) != null) {
            result.setValue(THREE_OF_A_KIND_VALUE);
            result.setStrValue(XiToLanguage.THREE_OF_A_KIND);
            result.setHighestCards(getThreeOfAKind(list));
            return result;
        } else if (getTwoPairs(list) != null) {
            result.setValue(TWO_PAIRS_VALUE);
            result.setStrValue(XiToLanguage.TWO_PAIRS);
            result.setHighestCards(getTwoPairs(list));
            return result;
        } else if (getHighestPair(list) != null) {
            result.setValue(PAIR_VALUE);
            result.setStrValue(XiToLanguage.PAIR);
            result.setHighestCards(getHighestPair(list));
            return result;
        } else {
            result.setValue(HIGH_CARD_VALUE);
            result.setStrValue(XiToLanguage.HIGH_CARD);
            result.setHighestCards(list);
            return result;
        }
    }
    public static byte[] list2Array(List<Byte> cardset) {
        byte[] cards = new byte[cardset.size()];
        for (int i = 0; i < cardset.size(); i++) {
            cards[i] = cardset.get(i);
        }
        return cards;
    }
}
