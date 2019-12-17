/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.common.card.object.Card;
import game.vn.common.card.object.CardSet;
import game.vn.game.maubinh.MauBinhConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author binhnt
 */
public class MauBinhCardSet extends CardSet {

    public static final int NUMBER_ACE = 12;
    public static final int NUMBER_FIVE = 3;
    public static final int NUMBER_THREE = 1;
    public static final int NUMBER_TWO = 0;

    public static final int TYPE_HEART = 3;
    public static final int TYPE_DIAMOND = 2;
    public static final int TYPE_CLUB = 1;
    public static final int TYPE_SPADE = 0;
    
    public static final int MAUBINH_FULL_HOUSE=1;
    public static final int MAUBINH_3_XI=2;
    public static final int MAUBINH_4_OF_A_KIND=3;
    public static final int MAUBINH_4XI=4;
    public static final int MAUBINH_FLUSH=5;
    public static final int MAUBINH_STRAIGHT_FLUSH=6;
    public static final int MAUBINH_BIG_STRAIGHT_FLUSH=7;
    public static final int MAUBINH_SMALL_STRAIGHT_FLUSH=8;
    public static final int MAUBINH_6_PAIR=9;
    public static final int MAUBINH_13_SAME_HEART=24;
    public static final int MAUBINH_13_SAME_DIAMOND=26;
    public static final int MAUBINH_13_SAME_CLUB=27;
    public static final int MAUBINH_13_SAME_SPADE=28;
    public static final int MAUBINH_3STRAIGHT=29;
    public static final int MAUBINH_BIG_STRAIGHT_FLUSH_HEART=30;
    public static final int MAUBINH_SMAILL_STRAIGHT_FLUSH_HEART=31;

    public MauBinhCardSet() {
        //compiled code
        super();
    }

    public static boolean isAce(Card card) {
        return card.getCardNumber() == NUMBER_ACE;
    }

    public static boolean is3(Card card) {
        return card.getCardNumber() == NUMBER_THREE;
    }

    public static boolean is2(Card card) {
        return card.getCardNumber() == NUMBER_TWO;
    }

    public static boolean is5(Card card) {
        return card.getCardNumber() == NUMBER_FIVE;
    }

    public static boolean isRed(Card card) {
        return (card.getCardType() == TYPE_HEART) || (card.getCardType() == TYPE_DIAMOND);
    }

    public static List<Card> getMauBinhCards(int maubinhType) {
        Random random = new Random();
        List<Card> cardList = new ArrayList<>();
        switch (maubinhType) {
            case MauBinhType.SAME_COLOR_13:
                for (int i = 0; i < MauBinhConfig.DEFAULT_NUMBER_CARD; i++) {
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE + random.nextInt(1))));
                }

                return cardList;
            case MauBinhType.FOUR_OF_THREE:
                byte id = 0;
                for (int i = 0; i < MauBinhConfig.DEFAULT_NUMBER_CARD; i++) {
                    cardList.add(CardSet.getCard(id));
                    id++;
                    if (id % MauBinhConfig.DEFAULT_NUMBER_TYPE == 3) {
                        id++;
                    }
                }

                return cardList;
            case MauBinhType.STRAIGHT_13:
                for (int i = 0; i < MauBinhConfig.DEFAULT_NUMBER_CARD; i++) {
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE + random.nextInt(3))));
                }

                return cardList;
            case MauBinhType.SIX_PAIR_WITH_THREE:
                for (int i = 0; i < MauBinhConfig.DEFAULT_NUMBER_CARD / 2; i++) {
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE)));
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE + 1)));
                }

                cardList.add(CardSet.getCard((byte) (random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD / 2) * MauBinhConfig.DEFAULT_NUMBER_TYPE + 2)));
                return cardList;
            case MauBinhType.SAME_COLOR_12:
                for (int i = 1; i < MauBinhConfig.DEFAULT_NUMBER_CARD; i++) {
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE + random.nextInt(1))));
                }

                cardList.add(CardSet.getCard((byte) (2 + random.nextInt(1))));
                return cardList;
            case MauBinhType.THREE_FLUSH:
                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_BIG_SET; i++) {
                    cardList.add(CardSet.getCard((byte) (random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD) * MauBinhConfig.DEFAULT_NUMBER_TYPE)));
                }

                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_BIG_SET; i++) {
                    cardList.add(CardSet.getCard((byte) (random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD) * MauBinhConfig.DEFAULT_NUMBER_TYPE + 1)));
                }

                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_SMALL_SET; i++) {
                    cardList.add(CardSet.getCard((byte) (random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD) * MauBinhConfig.DEFAULT_NUMBER_TYPE + 2)));
                }

                return cardList;
            case MauBinhType.THREE_STRAIGHT:
                id = (byte) random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD - MauBinhConfig.NUMBER_CARD_BIG_SET);
                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_BIG_SET; i++) {
                    cardList.add(CardSet.getCard((byte) ((id + i) * MauBinhConfig.DEFAULT_NUMBER_TYPE
                            + random.nextInt(MauBinhConfig.DEFAULT_NUMBER_TYPE))));
                }

                id = (byte) random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD - MauBinhConfig.NUMBER_CARD_BIG_SET);
                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_BIG_SET; i++) {
                    cardList.add(CardSet.getCard((byte) ((id + i) * MauBinhConfig.DEFAULT_NUMBER_TYPE
                            + random.nextInt(MauBinhConfig.DEFAULT_NUMBER_TYPE))));
                }

                id = (byte) random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD - MauBinhConfig.NUMBER_CARD_SMALL_SET);
                for (int i = 0; i < MauBinhConfig.NUMBER_CARD_SMALL_SET; i++) {
                    cardList.add(CardSet.getCard((byte) ((id + i) * MauBinhConfig.DEFAULT_NUMBER_TYPE
                            + random.nextInt(MauBinhConfig.DEFAULT_NUMBER_TYPE))));
                }

                return cardList;
            case MauBinhType.SIX_PAIR:
                for (int i = 0; i < MauBinhConfig.DEFAULT_NUMBER_CARD / 2; i++) {
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE)));
                    cardList.add(CardSet.getCard((byte) (i * MauBinhConfig.DEFAULT_NUMBER_TYPE + 1)));
                }

                cardList.add(CardSet.getCard((byte) ((MauBinhConfig.DEFAULT_NUMBER_CARD / 2
                        + random.nextInt(MauBinhConfig.DEFAULT_NUMBER_CARD / 2)) * MauBinhConfig.DEFAULT_NUMBER_TYPE + 2)));
                return cardList;
            default:
                break;
        }

        return null;
    }

    public static List<Card> getBigStraightCards(int type) {
        Random random = new Random();
        List<Card> cardList = new ArrayList<>();
        switch (type) {
            case 1: // 10JQKA
                cardList.add(CardSet.getCard((byte) 33));
                cardList.add(CardSet.getCard((byte) 38));
                cardList.add(CardSet.getCard((byte) 41));
                cardList.add(CardSet.getCard((byte) 46));
                cardList.add(CardSet.getCard((byte) 50));

                cardList.add(CardSet.getCard((byte) 0));
                cardList.add(CardSet.getCard((byte) 1));
                cardList.add(CardSet.getCard((byte) 6));
                cardList.add(CardSet.getCard((byte) 7));
                cardList.add(CardSet.getCard((byte) 8));
                cardList.add(CardSet.getCard((byte) 10));
                cardList.add(CardSet.getCard((byte) 11));
                cardList.add(CardSet.getCard((byte) 13));

                return cardList;
            case 2: // A2345
                cardList.add(CardSet.getCard((byte) 1));
                cardList.add(CardSet.getCard((byte) 6));
                cardList.add(CardSet.getCard((byte) 9));
                cardList.add(CardSet.getCard((byte) 14));
                cardList.add(CardSet.getCard((byte) 50));

                cardList.add(CardSet.getCard((byte) 17));
                cardList.add(CardSet.getCard((byte) 19));
                cardList.add(CardSet.getCard((byte) 23));
                cardList.add(CardSet.getCard((byte) 24));
                cardList.add(CardSet.getCard((byte) 25));
                cardList.add(CardSet.getCard((byte) 27));
                cardList.add(CardSet.getCard((byte) 28));
                cardList.add(CardSet.getCard((byte) 30));

                return cardList;
            default:
                return null;
        }
    }

    public static List<Card> getCardList(int index) {
        List<Card> cardList = new ArrayList<>();
        switch (index) {
            case 0:
                cardList.add(CardSet.getCard((byte) 0)); // 2 bich.
                cardList.add(CardSet.getCard((byte) 4)); // 3 bich.
                cardList.add(CardSet.getCard((byte) 7)); // 3 co.
                cardList.add(CardSet.getCard((byte) 12)); // 5 bich.
                cardList.add(CardSet.getCard((byte) 13)); // 5 chuon.
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 15)); // 5 co.
                cardList.add(CardSet.getCard((byte) 17)); // 6 chuon.
                cardList.add(CardSet.getCard((byte) 20)); // 7 bich.
                cardList.add(CardSet.getCard((byte) 22)); // 7 ro.
                cardList.add(CardSet.getCard((byte) 47)); // 8 bich.
                cardList.add(CardSet.getCard((byte) 50)); // 8 co.
                cardList.add(CardSet.getCard((byte) 51)); // J bich.
                break;
            case 1:
                cardList.add(CardSet.getCard((byte) 1)); // 2 chuon.
                cardList.add(CardSet.getCard((byte) 3)); // 2 co.
                cardList.add(CardSet.getCard((byte) 5)); // 3 chuon.
                cardList.add(CardSet.getCard((byte) 9)); // 4 chuon.
                cardList.add(CardSet.getCard((byte) 17)); // 6 chuon.
                cardList.add(CardSet.getCard((byte) 29)); // 9 chuon.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 35)); // 10 co.
                cardList.add(CardSet.getCard((byte) 40)); // Q bich.
                cardList.add(CardSet.getCard((byte) 41)); // Q chuon.
                cardList.add(CardSet.getCard((byte) 43)); // Q co.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                cardList.add(CardSet.getCard((byte) 50)); // A ro.
                break;
            case 2:
                cardList.add(CardSet.getCard((byte) 2)); // 2 ro.
                cardList.add(CardSet.getCard((byte) 6)); // 3 ro.
                cardList.add(CardSet.getCard((byte) 11)); // 4 co.
                cardList.add(CardSet.getCard((byte) 18)); // 6 ro.
                cardList.add(CardSet.getCard((byte) 28)); // 9 bich.
                cardList.add(CardSet.getCard((byte) 33)); // 10 chuon.
                cardList.add(CardSet.getCard((byte) 34)); // 10 ro.
                cardList.add(CardSet.getCard((byte) 38)); // J ro.
                cardList.add(CardSet.getCard((byte) 42)); // Q ro.
                cardList.add(CardSet.getCard((byte) 46)); // K ro.
                cardList.add(CardSet.getCard((byte) 47)); // K co.
                cardList.add(CardSet.getCard((byte) 49)); // A chuon.
                cardList.add(CardSet.getCard((byte) 51)); // A co.
                break;
            case 3://luc phe bon
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 22)); // 6 co.
                cardList.add(CardSet.getCard((byte) 21)); // 7 chuon.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 25)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 26)); // 8 ro.
                cardList.add(CardSet.getCard((byte) 30)); // 9 ro.
                cardList.add(CardSet.getCard((byte) 31)); // 9 co.
                cardList.add(CardSet.getCard((byte) 37)); // J chuon.
                cardList.add(CardSet.getCard((byte) 39)); // J co.
                cardList.add(CardSet.getCard((byte) 44)); // K bich.
                cardList.add(CardSet.getCard((byte) 45)); // K chuon.
                break;
            case 4: // 10JQKA thùng phá sảnh
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 19)); // 6 co.
                cardList.add(CardSet.getCard((byte) 21)); // 7 chuon.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 25)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 26)); // 8 ro.
                cardList.add(CardSet.getCard((byte) 30)); // 9 ro.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 36)); // J bich.
                cardList.add(CardSet.getCard((byte) 40)); // Q bich.
                cardList.add(CardSet.getCard((byte) 44)); // K bich.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                break;
            case 5: // 910JQK thùng phá sảnh
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 19)); // 6 co.
                cardList.add(CardSet.getCard((byte) 21)); // 7 chuon.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 25)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 26)); // 8 ro.
                cardList.add(CardSet.getCard((byte) 30)); // 9 ro.
                cardList.add(CardSet.getCard((byte) 28)); // 9 bich.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 36)); // J bich.
                cardList.add(CardSet.getCard((byte) 40)); // Q bich.
                cardList.add(CardSet.getCard((byte) 44)); // K bich.
                break;
            case 6: // A2345 thùng phá sảnh
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 19)); // 6 co.
                cardList.add(CardSet.getCard((byte) 21)); // 7 chuon.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 25)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 26)); // 8 ro.
                cardList.add(CardSet.getCard((byte) 30)); // 9 ro.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                cardList.add(CardSet.getCard((byte) 0)); // 2 bich.
                cardList.add(CardSet.getCard((byte) 4)); // 3 bich.
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 12)); // 5 bich.
                break;
            case 7: // 910JQK thùng phá sảnh + tứ quý Ace
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 19)); // 6 co.
                cardList.add(CardSet.getCard((byte) 21)); // 7 chuon.
                cardList.add(CardSet.getCard((byte) 28)); // 9 bich.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 36)); // J bich.
                cardList.add(CardSet.getCard((byte) 40)); // Q bich.
                cardList.add(CardSet.getCard((byte) 44)); // K bich.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                cardList.add(CardSet.getCard((byte) 49)); // A chuon.
                cardList.add(CardSet.getCard((byte) 50)); // A ro.
                cardList.add(CardSet.getCard((byte) 51)); // A co.
                break;
            case 8: // cù lủ Ace
                cardList.add(CardSet.getCard((byte) 0)); // 2 bich.
                cardList.add(CardSet.getCard((byte) 1)); // 2 chuon.
                cardList.add(CardSet.getCard((byte) 7)); // 3 co.
                cardList.add(CardSet.getCard((byte) 12)); // 5 bich.
                cardList.add(CardSet.getCard((byte) 13)); // 5 chuon.
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 15)); // 5 co.
                cardList.add(CardSet.getCard((byte) 17)); // 6 chuon.
                cardList.add(CardSet.getCard((byte) 20)); // 7 bich.
                cardList.add(CardSet.getCard((byte) 22)); // 7 ro.
                cardList.add(CardSet.getCard((byte) 49)); // A chuon.
                cardList.add(CardSet.getCard((byte) 50)); // A ro.
                cardList.add(CardSet.getCard((byte) 51)); // A co.
                break;
            case 9: // cù lủ Q
                cardList.add(CardSet.getCard((byte) 1)); // 2 chuon.
                cardList.add(CardSet.getCard((byte) 4)); // 3 bich.
                cardList.add(CardSet.getCard((byte) 5)); // 3 chuon.
                cardList.add(CardSet.getCard((byte) 9)); // 4 chuon.
                cardList.add(CardSet.getCard((byte) 17)); // 6 chuon.
                cardList.add(CardSet.getCard((byte) 29)); // 9 chuon.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 35)); // 10 co.
                cardList.add(CardSet.getCard((byte) 40)); // Q bich.
                cardList.add(CardSet.getCard((byte) 41)); // Q chuon.
                cardList.add(CardSet.getCard((byte) 43)); // Q co.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                cardList.add(CardSet.getCard((byte) 50)); // A ro.
                break;
            case 10: // Mau Binh 3 sảnh.
                cardList.add(CardSet.getCard((byte) 6)); // 3 ro.
                cardList.add(CardSet.getCard((byte) 7)); // 3 co.
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 12)); // 5 bich.
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 18)); // 6 ro.
                cardList.add(CardSet.getCard((byte) 19)); // 6 co.
                cardList.add(CardSet.getCard((byte) 21)); // 7 chuon.
                cardList.add(CardSet.getCard((byte) 22)); // 7 ro.
                cardList.add(CardSet.getCard((byte) 25)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 31)); // 9 co.
                cardList.add(CardSet.getCard((byte) 33)); // 10 chuon.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                break;
            case 11: // Mau Binh 3 thùng.
                cardList.add(CardSet.getCard((byte) 20)); // 7 bich.
                cardList.add(CardSet.getCard((byte) 9)); // 4 chuon .
                cardList.add(CardSet.getCard((byte) 51)); // A co.
                cardList.add(CardSet.getCard((byte) 39)); // J co.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 28)); // 9 bich.
                cardList.add(CardSet.getCard((byte) 29)); // 9 chuon.
                cardList.add(CardSet.getCard((byte) 31)); // 9 co.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 35)); // 10 co.
                cardList.add(CardSet.getCard((byte) 36)); // J bich.
                cardList.add(CardSet.getCard((byte) 33)); // 10 chuon.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                break;
            case 12: // 3 xám chi.
                cardList.add(CardSet.getCard((byte) 1)); // 2 chuon.
                cardList.add(CardSet.getCard((byte) 2)); // 2 ro.
                cardList.add(CardSet.getCard((byte) 3)); // 2 co.
                cardList.add(CardSet.getCard((byte) 9)); // 4 chuon.
                cardList.add(CardSet.getCard((byte) 13)); // 5 chuon.
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 15)); // 5 co.
                cardList.add(CardSet.getCard((byte) 16)); // 6 bich.
                cardList.add(CardSet.getCard((byte) 27)); // 8 co.
                cardList.add(CardSet.getCard((byte) 36)); // J bich.
                cardList.add(CardSet.getCard((byte) 45)); // K chuon.
                cardList.add(CardSet.getCard((byte) 46)); // K ro.
                cardList.add(CardSet.getCard((byte) 47)); // K co.
                break;
            case 13: // 4 đôi.
                cardList.add(CardSet.getCard((byte) 5)); // 3 chuon.
                cardList.add(CardSet.getCard((byte) 6)); // 3 ro.
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 9)); // 4 chuon.
                cardList.add(CardSet.getCard((byte) 13)); // 5 chuon.
                cardList.add(CardSet.getCard((byte) 20)); // 8 bich.
                cardList.add(CardSet.getCard((byte) 21)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 24)); // 9 bich.
                cardList.add(CardSet.getCard((byte) 28)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 31)); // 10 co.
                cardList.add(CardSet.getCard((byte) 32)); // J bich.
                cardList.add(CardSet.getCard((byte) 34)); // J ro.
                cardList.add(CardSet.getCard((byte) 44)); // K bich.
                break;
            case 14: // 2 cù lủ + 1 đôi.
                cardList.add(CardSet.getCard((byte) 2)); // 2 ro.
                cardList.add(CardSet.getCard((byte) 12)); // 5 bich.
                cardList.add(CardSet.getCard((byte) 13)); // 5 chuon.
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 37)); // J chuon.
                cardList.add(CardSet.getCard((byte) 39)); // J co.
                cardList.add(CardSet.getCard((byte) 41)); // Q chuon.
                cardList.add(CardSet.getCard((byte) 43)); // Q co.
                cardList.add(CardSet.getCard((byte) 45)); // K chuon.
                cardList.add(CardSet.getCard((byte) 46)); // K ro.
                cardList.add(CardSet.getCard((byte) 47)); // K co.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                cardList.add(CardSet.getCard((byte) 51)); // A co.
                break;
            case 15: // A2345 + 45678
                cardList.add(CardSet.getCard((byte) 1)); // 2 chuon.
                cardList.add(CardSet.getCard((byte) 6)); // 3 ro.
                cardList.add(CardSet.getCard((byte) 9)); // 4 chuon.
                cardList.add(CardSet.getCard((byte) 10)); // 4 ro.
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 15)); // 5 co.
                cardList.add(CardSet.getCard((byte) 19)); // 6 co.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 24)); // 8 bich.
                cardList.add(CardSet.getCard((byte) 25)); // 8 chuon.
                cardList.add(CardSet.getCard((byte) 31)); // 9 co.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                cardList.add(CardSet.getCard((byte) 50)); // A ro.
                break;
            case 16: // Mau Binh 3 thùng.
                cardList.add(CardSet.getCard((byte) 8)); // 4 bich.
                cardList.add(CardSet.getCard((byte) 12)); // 5 bich .
                cardList.add(CardSet.getCard((byte) 14)); // 5 ro.
                cardList.add(CardSet.getCard((byte) 18)); // 6 ro.
                cardList.add(CardSet.getCard((byte) 22)); // 7 ro.
                cardList.add(CardSet.getCard((byte) 23)); // 7 co.
                cardList.add(CardSet.getCard((byte) 26)); // 8 ro.
                cardList.add(CardSet.getCard((byte) 27)); // 8 co.
                cardList.add(CardSet.getCard((byte) 28)); // 9 bich.
                cardList.add(CardSet.getCard((byte) 31)); // 9 co.
                cardList.add(CardSet.getCard((byte) 32)); // 10 bich.
                cardList.add(CardSet.getCard((byte) 42)); // Q ro.
                cardList.add(CardSet.getCard((byte) 48)); // A bich.
                break;
            default:
                break;
        }

        return cardList;
    }
    /**
     * Xét bài để test ở đây
     * @param testcase
     * @return 
     */
    public List<Card> getTestCase(int testcase) {
        List<Card> cards = new ArrayList<>();
        switch (testcase) {
            case MAUBINH_FULL_HOUSE:
                cards = getCardList(9);
                break;
            case MAUBINH_3_XI:
                cards = getCardList(17);
                break;
            case MAUBINH_4_OF_A_KIND:
                cards = getCardList(19);
                break;
            case MAUBINH_4XI:
                cards = getCardList(20);
                break;
            case MAUBINH_FLUSH:
                cards = getCardList(18);
                break;
            case MAUBINH_STRAIGHT_FLUSH:
                cards = getCardList(21);
                break;
            case MAUBINH_BIG_STRAIGHT_FLUSH:
                cards = getCardList(4);
                break;
            case MAUBINH_SMALL_STRAIGHT_FLUSH:
                cards = getCardList(6);
                break;
            case MAUBINH_6_PAIR:
                cards = getCardList(23);
                break;
            case 10:
                cards = getCardList(23);
                break;
            case 11:
                cards = getCardList(24);
                break;
            case 12:
                cards = getCardList(1);
                break;
             case MAUBINH_13_SAME_CLUB:
                cards = getCardList(MAUBINH_13_SAME_CLUB);
                break;
            case MAUBINH_13_SAME_DIAMOND:
                cards = getCardList(MAUBINH_13_SAME_DIAMOND);
                break;
            case MAUBINH_13_SAME_HEART:
                cards = getCardList(MAUBINH_13_SAME_HEART);
                break;
            case MAUBINH_13_SAME_SPADE:
                cards = getCardList(MAUBINH_13_SAME_SPADE);
                break; 
            case MAUBINH_3STRAIGHT:
                cards = getCardList(MAUBINH_3STRAIGHT);
                break;
            case MAUBINH_BIG_STRAIGHT_FLUSH_HEART:
                cards = getCardList(MAUBINH_BIG_STRAIGHT_FLUSH_HEART);
                break;
                
            case MAUBINH_SMAILL_STRAIGHT_FLUSH_HEART:
                cards = getCardList(MAUBINH_SMAILL_STRAIGHT_FLUSH_HEART);
                break;
        }
        return cards;
    }
}
