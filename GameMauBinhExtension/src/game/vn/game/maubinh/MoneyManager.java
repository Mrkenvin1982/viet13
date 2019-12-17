/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh;

import com.smartfoxserver.v2.entities.User;
import game.vn.game.maubinh.lang.MauBinhLanguage;
import game.vn.game.maubinh.object.Player;
import game.vn.game.maubinh.object.Result;
import game.vn.util.CommonMoneyReasonUtils;
import game.vn.util.Utils;
import java.math.BigDecimal;

/**
 *
 * @author 
 */
public class MoneyManager {

    private  BigDecimal gameMoney;
    private final MauBinhGameController gameController;

    public MoneyManager(BigDecimal gameMoney,MauBinhGameController gameControllerInput) {
        this.gameMoney = gameMoney;
        this.gameController=gameControllerInput;

    }

    public BigDecimal getGameMoney() {
        return this.gameMoney;
    }

    public boolean enoughMoneyToStart(User user, int minJoinGame) {
        if (user == null) {
            return false;
        }
        
        BigDecimal minJoin = Utils.multiply(gameMoney, new BigDecimal(String.valueOf(minJoinGame)));

        return this.gameController.getMoneyFromUser(user).compareTo(minJoin) >= 0;
    }

    public BigDecimal getGuaranteeMoney(User user) {
        BigDecimal moneyCheck = Utils.multiply( new BigDecimal(String.valueOf(MauBinhConfig.getInstance().getMaxLoseChi())), gameMoney);
        moneyCheck = Utils.subtract(this.gameController.getMoneyFromUser(user), moneyCheck);
        return moneyCheck.max(BigDecimal.ZERO);
    }

    public double[][] getWinChi(Result[][] result) {
        if (result == null || result[0] == null) {
            return null;
        }

        double[][] ret = new double[result.length][result[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                ret[i][j] = result[i][j] == null ? 0 : result[i][j].getWinChi();
            }
        }

        return ret;
    }

    /**
     * Tiền thưởng khi có user rời bàn
     * @param controller
     * @param players 
     */
    public void addBonusMoney(MauBinhGameController controller, Player[] players) {
        if (players == null ) {
            return ;
        }
        
        for (int i = 0; i < players.length; i++) {
            // Is player.
            if (players[i].getUser() == null || players[i].getBonusMoney().signum()== 0) {
                continue;
            }
            BigDecimal[] arrResultMoney = new BigDecimal[2];
            arrResultMoney = controller.getMoneyAfterTax(players[i].getBonusMoney());
            controller.updateMoney(players[i].getUser(), arrResultMoney[MauBinhGameController.MONEY_MB], null ,arrResultMoney[MauBinhGameController.TAX_MB],CommonMoneyReasonUtils.BONUS);
            controller.sendBonusMoneyMessage(players[i].getUser(), arrResultMoney[MauBinhGameController.MONEY_MB], "");
        }
    }

    public double[] updateMoney(MauBinhGameController controller, Player[] players, double[] winMoney, int[] winChi, Result[][] result) {
        if (controller == null || players == null || winMoney == null || winChi == null || result == null) {
            return null;
        }
  
        /**
         * Vì sử dung updateMoney3 nên trừ tiền tất cả user thua trước
         * sau đó cộng tiền cho tất cả user thắng sau để đảm bảo không âm win
         */
        //tru tien user thua- hòa truoc
        int maxChi = 0;
        for (int i = 0; i < players.length; i++) {
            if(winChi[i]>0) {
                if (winChi[i] > maxChi) {
                    maxChi = winChi[i];
                }
                continue;
            }
            // Is player.
            if (players[i].getUser() == null) {
                continue;
            }
            // kiểm tra tiền của user trước khi trừ, đảm bảo không âm tiền
            if (winMoney[i] < 0) {
                winMoney[i] = -Math.min(-winMoney[i], this.gameController.getMoneyFromUser(players[i].getUser()).doubleValue());
            }
            BigDecimal money = new BigDecimal(String.valueOf(winMoney[i]));
            money = Utils.getRoundBigDecimal(money);
            controller.updateMoney(players[i].getUser(), money, players[i].getCards().getArrangeCardsID(),BigDecimal.ZERO);
        }
        
        //cộng tiền user thắng sau
        for (int i = 0; i < players.length; i++) {
            if(winChi[i]<=0)
                continue;
            // Is player.
            if (players[i].getUser() == null) {
                continue;
            }
            BigDecimal []arrResultMoney= new BigDecimal[2];
            arrResultMoney[MauBinhGameController.MONEY_MB] = BigDecimal.ZERO;
            arrResultMoney[MauBinhGameController.TAX_MB] = BigDecimal.ZERO;
            if (winMoney[i] > 0) {
                arrResultMoney =controller.getMoneyAfterTax(new BigDecimal(String.valueOf(winMoney[i])));
                winMoney[i] = arrResultMoney[MauBinhGameController.MONEY_MB].doubleValue();
            }
            controller.updateMoney(players[i].getUser(), new BigDecimal(String.valueOf(winMoney[i])), players[i].getCards().getArrangeCardsID(),arrResultMoney[MauBinhGameController.TAX_MB]);
            if (maxChi > 0 && winChi[i] == maxChi) {
                controller.sendRankingData(players[i].getUser(), arrResultMoney[MauBinhGameController.TAX_MB].doubleValue(), 1);
            }
        }

        return winMoney;
    }

    public double[] updateMoneyForAutoArrangementUsing(MauBinhGameController controller, Player[] players, double[] winMoney) {
        if (controller == null || players == null || winMoney == null) {
            return null;
        }

        for (int i = 0; i < players.length; i++) {
            // Is player.
            if (players[i].getUser() == null) {
                continue;
            }

            // Discard losers or don't use service.
            if (winMoney[i] <= 0 || players[i].isUsedAutoArrangement() == false) {
                continue;
            }

            double money = controller.getAutoArrangementPrice();
            if (winMoney[i] < money) {
                money = winMoney[i];
            }

            controller.updateMoneyForService(players[i].getUser(), new BigDecimal(-money));
            winMoney[i] = Utils.subtract(winMoney[i], money);
        }

        return winMoney;
    }

    public void updateMoneyForLeave(MauBinhGameController controller, int leaveSeat, int playerNo, Player[] players, BigDecimal penalizeLeaver) {
        if (playerNo < MauBinhConfig.MIN_NUMBER_PLAYER || playerNo > MauBinhConfig.DEFAULT_NUMBER_PLAYER) {
            return;
        }

        if (leaveSeat < 0 || leaveSeat >= MauBinhConfig.DEFAULT_NUMBER_PLAYER) {
            return;
        }

        // Update money of game quitter.
        User user = players[leaveSeat].getUser();
        if (user == null) {
            return;
        }

        String description = MauBinhLanguage.getMessage(MauBinhLanguage.LEAVE_GAME,controller.getLocaleOfUser(user));
        penalizeLeaver = Utils.multiply(penalizeLeaver, new BigDecimal(String.valueOf(playerNo - 1)));
        BigDecimal value =  this.gameController.getMoneyFromUser(user).min(penalizeLeaver);
        
        controller.updateMoneyforLeaver(user, value.negate(),CommonMoneyReasonUtils.BO_CUOC ,null);
        controller.sendBonusMoneyMessage(user, value.negate(), description);
        
        value = Utils.add(value, players[leaveSeat].getBonusMoney());
        // Remove user from table.
        players[leaveSeat].setUser(null);
        players[leaveSeat].reset();

        // Note bonus chi for remaining.
        value = Utils.divide(value, new BigDecimal(String.valueOf(playerNo - 1)));
        for (int i = 0; i < players.length; i++) {
            if (players[i].getUser() == null) {
                continue;
            }

            players[i].addBonusMoney(value);
            players[i].addBonusChi(MauBinhConfig.getInstance().getChiLeaveBonus());
        }
    }


    /**
     * Cách tính tiền thắng-thua dựa vào số tiền user đang có
     *Mô tả:
     *  + Tính chi thắng thua thành kết quả 1 lần trước khi chung 
     *  + Người có số win ít khi vào ván chỉ ăn được tối đa số win bằng với số win họ có khi bắt đầu ván.
     *  + Tổng số win thắng thua (trước thuế) của ván chơi phải = 0
     * 
     * Cách tính:
     *  - Dựa vào số tiền hiện có của user và số chi thắng thua ta tính đươc tổng
     * chi thực thắng và thực thua của user và toàn ván bằng cách:
     *        Gọi moneyinGame: số tiền cược trong game
     *        Goi userMoney: la số tiền user hiện có
     *        goi winLoseChi: là số chi user thắng thua
     *        Gọi A:  chi thực thắng và thua 
     *        A = Min( (userMoney/moneyinGame),winLoseChi)
     * - Dựa vào chi thực thắng và thực thua, ta tính được số tiền thắng thua tối đa user
     * có thể thắng được trong ván
     * 
     * @param moneyOfUser: danh sách tiền của từng user
     * @param winChi:danh sách số chi thắng, thua của từng user
     * @return 
     */
    public double[] caculateMoneyNewFromMoneyOfUser(double[] moneyOfUser, int[] winChi) {
        //số tiền thắng(thua) cuối cùng của từng user
        double[] winMoney = new double[moneyOfUser.length];
        //số chi thắng thua thực tế
        double[] winChiActual = new double[winChi.length];
        //tổng chi thực thắng
        double sumWinChiActual = 0;
        //tổng chi thực thua
        double sumLoseChiActual = 0;

        //tính số chi thắng thua thực tế dựa vào số tiền thực có của user
        for (int i = 0; i < winChi.length; i++) {

            if (winChi[i] == 0) {
                continue;
            }
            /*
             Số chi thắng-thua nhiều hơn số tiền hiện có của user
             thì tính lại số chi=tiền của user/mức cược của bàn
             */
            if (Math.abs(winChi[i]) * getGameMoney().doubleValue() > moneyOfUser[i]) {
                winChiActual[i] = (double) (winChi[i] < 0 ? 
                        -((double)  moneyOfUser[i] / getGameMoney().doubleValue())
                        : (double) (moneyOfUser[i] / getGameMoney().doubleValue()));
            } else {
                winChiActual[i] = winChi[i];
            }

            //tính tổng chi thua và tổng chi thắng thực tế
            if (winChiActual[i] < 0) {
                sumLoseChiActual += winChiActual[i];
            } else {
                sumWinChiActual += winChiActual[i];
            }
        }

        //tính tiền thắng thua thực tế cho từng user
        for (int i = 0; i < winChi.length; i++) {

            if (winChi[i] == 0) {
                continue;
            }

            if (winChi[i] < 0) {
                /**
                 * 1.Tổng chi thắng bé hơn tổng chi thua
                 *     Số tiền thua=(tổng chi thắng/tổng chi thua) * số chi thực thắng thua * tiền cược
                 * 2.Tổng chi thắng >=Tổng chi thua
                 *     Số tiền thua=số chi thực thắng thua * tiền cược
                 */
                if (sumWinChiActual < Math.abs(sumLoseChiActual)) {
                    winMoney[i] = Math.round( (double) (winChiActual[i] * sumWinChiActual * getGameMoney().doubleValue()) / (Math.abs(sumLoseChiActual)) );
                } else {
                    winMoney[i] = Utils.multiply(winChiActual[i] , getGameMoney().doubleValue());
                }
            } else {//tính tiền cho user thắng
                /**
                 * 1.Tổng chi thắng lớn hơn hơn tổng chi thua
                 *     Số tiền thua=(tổng chi thua/tổng chi thắng) * số chi thực thắng thua * tiền cược
                 * 2.Tổng chi thắng <= Tổng chi thua
                 *     Số tiền thua=số chi thực thắng thua * tiền cược
                 */
                if (sumWinChiActual > Math.abs(sumLoseChiActual)) {
                    winMoney[i] = Math.round( (double) (winChiActual[i] * Math.abs(sumLoseChiActual)* getGameMoney().doubleValue()) / (sumWinChiActual ) );
                } else {
                    winMoney[i] = Utils.multiply(winChiActual[i], getGameMoney().doubleValue());
                }
            }
        }

        return winMoney;
    }
    
    public void setMoneyGame(BigDecimal money){
        this.gameMoney = money;
    }    
    
}
