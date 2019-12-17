/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh.object;

import game.vn.common.card.object.Card;
import game.vn.game.maubinh.MauBinhConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author binhnt
 */
public class Cards implements Comparable<Cards> {

    /**
     * Cards of this player in game.
     */
    private List<Card> cards;
    // Sets: cac chi.
    private SmallSet set01;
    private MiddleSet set02;
    private LastSet set03;
    private int maubinhType;
    private int winchi;

    public Cards() {
        this.cards = new ArrayList<>();

        this.set01 = new SmallSet();
        this.set02 = new MiddleSet();
        this.set03 = new LastSet();

        this.maubinhType = MauBinhType.NOT_MAU_BINH;
    }

    /**
     * get all current card of this player.
     *
     * @return a list of card.
     */
    public List<Card> getCards() {
        return this.cards;
    }

    public void setCards(List<Card> listcards) {
        if (listcards.size() != MauBinhConfig.DEFAULT_NUMBER_CARD) {
            return;
        }
        cards = listcards;
    }

    public List<Card> getArrangeCards() {
        if (this.isFinishArrangement() == false) {
            return null;
        }

        List<Card> ret = new ArrayList<>();
        ret.addAll(this.get1stSet().getCards());
        ret.addAll(this.get2ndSet().getCards());
        ret.addAll(this.get3rdSet().getCards());
        return ret;
    }
    /**
     * Lấy ra dánh sách ids cards
     * @return 
     */
    public List<Short> getArrangeCardsID(){
        List<Card> listCards= getArrangeCards();
        List<Short> arrayIdS = new ArrayList<>();
        if(listCards==null){
             for (int j = 0; j < getCards().size(); j++) {
                arrayIdS.add((short) getCards().get(j).getId());
            }
            return arrayIdS;
        }

        for(int i=0;i<listCards.size();i++){
            arrayIdS.add((short)listCards.get(i).getId());
        }
        return arrayIdS;
    }

    public void clearArrangement() {
        this.set01.clear();
        this.set02.clear();
        this.set03.clear();

        //2 loại mậu binh này thì client sắp xếp rùi gửi lên
        if (this.maubinhType == MauBinhType.THREE_STRAIGHT
                || this.maubinhType == MauBinhType.THREE_FLUSH) {
            this.maubinhType = MauBinhType.NOT_MAU_BINH;
        }
    }

    /**
     * gọi hàm này  khi 2 loại mậu binh này thì client sắp xếp rùi gửi lên
     * còn không sắp xếp 2 loại mậu binh này thì không tinh
     */
    public void setMauBinhTypeAfterArrangement() {
        // Check cards.
        if (this.maubinhType != MauBinhType.NOT_MAU_BINH) {
            return;
        }

        // 3 chi thung.
        if (this.set01.isFlush() && this.set02.isFlush() && this.set03.isFlush()) {
            this.maubinhType = MauBinhType.THREE_FLUSH;
            return;
        }

        // 3 chi sanh.
        if (this.set01.isStraight() && this.set02.isStraight() && this.set03.isStraight()) {
            this.maubinhType = MauBinhType.THREE_STRAIGHT;
        }
    }

    /**
     * received a new card from board.
     *
     * @param card card that player received.
     */
    public boolean receivedCard(Card card) {
        // Enough cards.
        if (this.cards.size() > MauBinhConfig.DEFAULT_NUMBER_CARD) {
            return false;
        }

        // Check dupplication.
        for (Card card2 : this.cards) {
            if (card.getId() == card2.getId()) {
                return false;
            }
        }

        this.cards.add(card);
        Collections.sort(this.cards);

        if (this.cards.size() == MauBinhConfig.DEFAULT_NUMBER_CARD) {
            this.setMauBinhType();
        }

        return true;
    }

    public void sort(){
        Collections.sort(this.cards);
    }
    /**
     * received a new card for 1st set.
     *
     * @param card card that player received.
     */
    public boolean receivedCardTo1stSet(Card card) {
        // Check including condition.
        if (this.isContainCard(card) == false) {
            return false;
        }

        return this.get1stSet().receivedCard(card);
    }

    /**
     * received a new card for 2nd set.
     *
     * @param card card that player received.
     */
    public boolean receivedCardTo2ndSet(Card card) {
        // Check including condition.
        if (this.isContainCard(card) == false) {
            return false;
        }

        return this.get2ndSet().receivedCard(card);
    }

    /**
     * received a new card for 3rd set.
     *
     * @param card card that player received.
     */
    public boolean receivedCardTo3rdSet(Card card) {
        // Check including condition.
        if (this.isContainCard(card) == false) {
            return false;
        }

        return this.get3rdSet().receivedCard(card);
    }

    public int getMauBinhType() {
        return this.maubinhType;
    }

    public boolean isEnoughCard() {
        return this.getCards().size() == MauBinhConfig.DEFAULT_NUMBER_CARD;
    }

    // Binh xong.
    public boolean isFinishArrangement() {
        return this.get1stSet().getType() != SetType.NOT_ENOUGH_CARD
                && this.get2ndSet().getType() != SetType.NOT_ENOUGH_CARD
                && this.get3rdSet().getType() != SetType.NOT_ENOUGH_CARD;
    }

    public int getNumberOfAce() {
        if (this.getCards() == null || this.getCards().isEmpty()) {
            return 0;
        }

        int ret = 0;
        int lastIndex = this.getCards().size() - 1;
        int temp = Math.min(lastIndex + 1, MauBinhConfig.DEFAULT_NUMBER_TYPE);
        for (int i = 0; i < temp; i++) {
            // Count Ace number.
            if (MauBinhCardSet.isAce(this.getCards().get(lastIndex - i))) {
                ret++;
            } else {
                // It can be break because card list always sorted.
                break;
            }
        }

        return ret;
    }

    // Must be call after enough cards.
    public boolean IsFailedArrangement() {
        if (this.isMauBinh()) {
            return false;
        }

        if (this.get1stSet().isEnough() == false || this.get2ndSet().isEnough() == false
                || this.get3rdSet().isEnough() == false) {
            return true;
        }

        int result01 = this.get3rdSet().compareWith(this.get2ndSet());
        int result02 = this.get2ndSet().compareWith(this.get1stSet());
        return result01 == MauBinhConfig.RESULT_ERROR || result02 == MauBinhConfig.RESULT_ERROR
                || result01 == MauBinhConfig.RESULT_LOSE || result02 == MauBinhConfig.RESULT_LOSE;
    }

    /**
     * Compare with specified card list.
     *
     * @param cards a set of cards.
     * @return a Result object.
     */
    public Result compareWith(Cards cards) {
        // Check input condition.
        if (cards == null
                || this.getCards() == null || cards.getCards() == null
                || this.isEnoughCard() == false || cards.isEnoughCard() == false) {
            return null;
        }

        // Compare in the not-mau-binh case.
        if (this.isMauBinh() == false) {
            if (cards.isMauBinh()) {
                // Compare in the mau-binh case.
                Result result = new Result();
                result.setWinChiMauBinh(-cards.getMauBinhWinChi());
                result.setWinChiAce(this.getNumberOfAce() - cards.getNumberOfAce());
                return result;
            }
            // Compare in the not-mau-binh case.
            return this.compareNotMauBinhWithNotMauBinh(cards);
        } else { // Compare in the mau-binh case.
            // Compare in the not-mau-binh case.
            if (cards.isMauBinh() == false) {
                // Mau-binh win not-mau-binh, so return the number of win chi.
                Result result = new Result();
                result.setWinChiMauBinh(this.getMauBinhWinChi());
                result.setWinChiAce(this.getNumberOfAce() - cards.getNumberOfAce());

                return result;
            }
            // Compare in the mau-binh case.
            return this.compareMauBinhWithMauBinh(cards);
        }
    }

    public boolean isMauBinh() {
        return this.maubinhType != MauBinhType.NOT_MAU_BINH;
    }

    private boolean isContainCard(Card card) {
        for (Card card2 : this.cards) {
            if (card.getId() == card2.getId()) {
                return true;
            }
        }

        return false;
    }

    private SmallSet get1stSet() {
        return this.set01;
    }

    private BigSet get2ndSet() {
        return this.set02;
    }

    private BigSet get3rdSet() {
        return this.set03;
    }

    /**
     * Get win "chi" in the Mau Binh win case.
     *
     * @return the number or "win".
     */
    private int getMauBinhWinChi() {
        switch (this.getMauBinhType()) {
            case MauBinhType.SIX_PAIR: // 6 doi.
                return MauBinhConfig.getInstance().getChiMauBinhSixPair();
            case MauBinhType.THREE_STRAIGHT: // 3 sanh.
                return MauBinhConfig.getInstance().getChiMauBinhThreeStraight();
            case MauBinhType.THREE_FLUSH: // 3 thung.
                return MauBinhConfig.getInstance().getChiMauBinhThreeFlush();
            case MauBinhType.SAME_COLOR_12: // Dong hoa 12.
                return MauBinhConfig.getInstance().getChiMauBinhSameColor12();
            case MauBinhType.SIX_PAIR_WITH_THREE: // Luc phe bon.
                return MauBinhConfig.getInstance().getChiMauBinhSixPairWithThree();
            case MauBinhType.STRAIGHT_13: // Sanh rong.
                return MauBinhConfig.getInstance().getChiMauBinhStraight13();
            case MauBinhType.FOUR_OF_THREE: // 4 xam.
                return MauBinhConfig.getInstance().getChiMauBinhFourOfThree();
            case MauBinhType.SAME_COLOR_13: // Dong hoa 13.
                return MauBinhConfig.getInstance().getChiMauBinhSameColor13();
            default:
                return MauBinhConfig.RESULT_ERROR;
        }
    }

    /**
     * Compare with specified card list in not-mau-binh case.
     *
     * @param cards a set of cards.
     * @return a Result object.
     */
    private Result compareNotMauBinhWithNotMauBinh(Cards cards) {
        // Check thung bai.
        if (this.IsFailedArrangement()) {
            if (cards.IsFailedArrangement()) {
                // Draw if cung thung.
                return new Result(); // All field is 0 (multiK = 1).
            }
            // Lose because bai thung.
            return cards.compareNotMauBinhWithFailed().getNegative();
        }

        if (cards.IsFailedArrangement()) {
            return this.compareNotMauBinhWithFailed();
        }

        // There is no any failed card list.
        Result ret = new Result();
        // An Sap ham.
        boolean isWinThreeSet = true;
        // Thua Sap ham
        boolean isLoseThreeSet = true;
        // Compare 1st chi.
        int result = this.get1stSet().getWinChiInComparisonWith(cards.get1stSet());
        if (result == MauBinhConfig.RESULT_ERROR) {
            return null;
        }

        ret.setWinChi01(result);
        isWinThreeSet = isWinThreeSet && (result > 0);
        isLoseThreeSet = isLoseThreeSet && (result < 0);

        // Compare middle chi.
        result = this.get2ndSet().getWinChiInComparisonWith(cards.get2ndSet());
        if (result == MauBinhConfig.RESULT_ERROR) {
            return null;
        }

        ret.setWinChi02(result);
        isWinThreeSet = isWinThreeSet && (result > 0);
        isLoseThreeSet = isLoseThreeSet && (result < 0);

        // Compare last chi.
        result = this.get3rdSet().getWinChiInComparisonWith(cards.get3rdSet());
        if (result == MauBinhConfig.RESULT_ERROR) {
            return null;
        }

        ret.setWinChi03(result);
        isWinThreeSet = isWinThreeSet && (result > 0);
        isLoseThreeSet = isLoseThreeSet && (result < 0);

        // Win Sap ham.
        if (isWinThreeSet || isLoseThreeSet) {
            ret.setMultiK(MauBinhConfig.getInstance().getChiWinThreeSetRate());
        }

        // Calculate Ace number.
        ret.setWinChiAce(this.getNumberOfAce() - cards.getNumberOfAce());

        return ret;
    }

    /**
     * Compare with specified card list in not-mau-binh case with failed
     * arrangement.
     *
     * @return a Result object.
     */
    private Result compareNotMauBinhWithFailed() {
        Result ret = new Result();
        // Win 1st chi.
        int result = this.get1stSet().getWinChi();
        if (result == MauBinhConfig.RESULT_ERROR) {
            return null;
        }

        ret.setWinChi01(result);

        // Win middle chi.
        result = this.get2ndSet().getWinChi();
        if (result == MauBinhConfig.RESULT_ERROR) {
            return null;
        }

        ret.setWinChi02(result);

        // Win last chi.
        result = this.get3rdSet().getWinChi();
        if (result == MauBinhConfig.RESULT_ERROR) {
            return null;
        }

        ret.setWinChi03(result);

        // Win Sap ham.
        ret.setMultiK(MauBinhConfig.getInstance().getChiWinThreeSetRate());

        // Calculate Ace number. Do NOT consider Ace of opponient.
        ret.setWinChiAce(this.getNumberOfAce());

        return ret;
    }

    /**
     * Compare with specified card list in mau-binh case.
     *
     * @param cards a set of cards.
     * @return a Result object.
     */
    private Result compareMauBinhWithMauBinh(Cards cards) {
        Result result = new Result();
        // Lose.
        if (this.getMauBinhType() < cards.getMauBinhType()) {
            result.setWinChiMauBinh(-cards.getMauBinhWinChi());
        } else if (this.getMauBinhType() > cards.getMauBinhType()) { // Win.
            result.setWinChiMauBinh(this.getMauBinhWinChi());
        } else { // Types are equal then equal.
            // Do NOT compare 2 mau binh with the same type.
            result.setWinChiMauBinh(0);
        }

        return result;
    }

    public void setMauBinhType() {
        // Check cards.
        if (this.cards == null || this.cards.isEmpty()
                || this.cards.size() < MauBinhConfig.DEFAULT_NUMBER_CARD) {
            this.maubinhType = MauBinhType.NOT_MAU_BINH;
            return;
        }

        int redCardNo = 0;
        if (MauBinhCardSet.isRed(this.cards.get(0))) {
            redCardNo++;
        }

        int pairNo = 0;
        int threeNo = 0;
        int fourNo = 0;
        int sameCardNo = 0;
        for (int i = 1; i < this.cards.size(); i++) {
            // Count the same color card number.
            if (MauBinhCardSet.isRed(this.cards.get(i))) {
                redCardNo++;
            }

            // Check pair, three or four of a kind.
            if (this.cards.get(i).getCardNumber() == this.cards.get(i - 1).getCardNumber()) {
                sameCardNo++;
            } else {
                switch (sameCardNo) {
                    case 0:
                        break;
                    case 1: // a pair.
                        pairNo++;
                        break;
                    case 2: // Three of a kind.
                        threeNo++;
                        break;
                    case 3: // Four of a Kind.
                        fourNo++;
                        break;
                    default:
                        break;
                }

                sameCardNo = 0;
            }
        }

        switch (sameCardNo) {
            case 0:
                break;
            case 1: // a pair.
                pairNo++;
                break;
            case 2: // Three of a kind.
                threeNo++;
                break;
            case 3: // Four of a Kind.
                fourNo++;
                break;
            default:
                break;
        }

        // Dong hoa 13 la: 13 reds or 13 blacks.
        if (redCardNo == MauBinhConfig.DEFAULT_NUMBER_CARD || redCardNo == 0) {
            this.maubinhType = MauBinhType.SAME_COLOR_13;
            return;
        }

        // Bon cai xam: 4 of three of a kind.
        if (threeNo + fourNo == 4) {
            this.maubinhType = MauBinhType.FOUR_OF_THREE;
            return;
        }

        // Sanh rong: 234578910JQKA. There is NOT any pair.
        if (pairNo == 0 && threeNo == 0 && fourNo == 0) {
            this.maubinhType = MauBinhType.STRAIGHT_13;
            return;
        }

        // Luc phe bon: 5 pairs and 1 three of a kind.
        if (pairNo + fourNo * 2 == 5 && threeNo == 1) {
            this.maubinhType = MauBinhType.SIX_PAIR_WITH_THREE;
            return;
        }

        // Dong hoa 12 la: 12 reds and 1 black, or 12 blacks and 1 red.
//        if (redCardNo == 12 || redCardNo == 1) {
//            this.maubinhType = MauBinhType.SAME_COLOR_12;
//            return;
//        }

        // 3 chi thung.
        if (this.set01.isFlush() && this.set02.isFlush() && this.set03.isFlush()) {
            this.maubinhType = MauBinhType.THREE_FLUSH;
            return;
        }

        // 3 chi sanh.
        if (this.set01.isStraight() && this.set02.isStraight() && this.set03.isStraight()) {
            this.maubinhType = MauBinhType.THREE_STRAIGHT;
            return;
        }

        // 6 doi: 6 pairs, no three of a kind.
        if (pairNo + fourNo * 2 == 6) {
            this.maubinhType = MauBinhType.SIX_PAIR;
            return;
        }

        // It is NOT mau binh.
        this.maubinhType = MauBinhType.NOT_MAU_BINH;
    }
    
    /**
     * Có thùng phá sảnh hạ
     *
     * @return
     */
    public boolean haveSmallStraightFlush() {
        if (this.set02.getType() == SetType.STRAIGHT_FLUSH
                && MauBinhCardSet.isAce(this.set02.getCards().get(this.set02.getCards().size() - 1))
                && this.set02.getCards().get(this.set02.getCards().size() - 2).getCardNumber() <= 3) {
            return true;
        }
        if (this.set03.getType() == SetType.STRAIGHT_FLUSH
                && MauBinhCardSet.isAce(this.set03.getCards().get(this.set03.getCards().size() - 1))
                && this.set03.getCards().get(this.set03.getCards().size() - 2).getCardNumber() <= 3) {
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra bài có thùng phá sảnh
     *
     * @return
     */
    public boolean haveStraightFlush() {
        if (this.set02.getType() == SetType.STRAIGHT_FLUSH || this.set03.getType() == SetType.STRAIGHT_FLUSH) {
            return true;
        }
        return false;
    }

    public int getWinchi() {
        return winchi;
    }

    public void setWinchi(int winchi) {
        this.winchi = winchi;
    }

    @Override
    public int compareTo(Cards o) {
        if (this.getWinchi() == o.getWinchi()) {
            return 0;
        }
        
        if (this.getWinchi() < o.getWinchi()) {
            return -1;
        }
        
        return 1;
    }

    
}
