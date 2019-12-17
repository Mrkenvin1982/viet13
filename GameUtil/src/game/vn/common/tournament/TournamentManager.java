/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.tournament;

import game.vn.common.lib.poker.spinandgo.Reward;
import game.vn.common.lib.poker.spinandgo.RewardMulti;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import game.vn.common.config.RoomConfig;
import game.vn.common.constant.Service;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;

/**
 * Xử lý tournament spin && go của game theo mức cược
 *
 * @author
 */
public class TournamentManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(TournamentManager.class);
    private final static TournamentManager INSTANCE = new TournamentManager();
    //quản lý danh sách tournament spin and go theo mức cược
    private final Map<Double, Tournament> tournaments;
    private final static Object LOCK = new Object();

    public TournamentManager() {
         tournaments = new HashMap<>();
    }

    public static TournamentManager getInstance() {
        return INSTANCE;
    }
    
    public void init() {
        if (tournaments != null) {
            tournaments.clear();
        }
    }

    public void initTourToHazelcast(){
        //load tu DB
        List<SumMoneyByBetBoard> sumMoneys = Database.instance.getTournaments();
        for(SumMoneyByBetBoard sumMoney: sumMoneys){
            HazelcastUtil.putBonusInfor(sumMoney.getServiceId(), sumMoney.getSumMoney());
        }
        List<String> tourLobbyNames = RoomConfig.getInstance().getTournamentNameGames();
        for (String lobbyName : tourLobbyNames) {
            byte serviceId = Utils.getServiceId(lobbyName);
            HazelcastUtil.putBonusInfor(serviceId, 0);
        }
    }
    
    /**
     * Lấy ra hệ số nhân random
     *
     * @param betBoard
     * @param valueAdded: số tiền vé thu của user sau khi trừ thuế
     * @return
     */
    public int getMultiRandom(double betBoard, double valueAdded) {
        synchronized (LOCK) {
            Tournament tour = this.getTournament(betBoard);
            if (tour == null) {
                return 0;
            }
            return tour.getMulti(valueAdded);
        }
    }

    /**
     * Lấy ra hệ số giải thưởng cao nhất của 1 chu kỳ tournament
     *
     * @param betboard
     * @return
     */
    public int getBonusMultiMax(double betboard) {
        Tournament tour = this.getTournament(betboard);
        if(tour == null){
            return 0;
        }
        return tour.getBonusMultiMax();
    }

     /**
     * Lấy thông tin giải và tỉ lệ trúng giải tournament
     *
     * @param betBoard
     * @return
     */
    private Tournament getTournament(double betBoard) {
        if(!this.tournaments.containsKey(betBoard)){
            return null;
        }
        return this.tournaments.get(betBoard);
    }
    
    /**
     * Init tất cả các giải theo giá vé
     */
    public void initAllTournaments() {
        synchronized (LOCK) {
            init();
            List<Reward> rewards = Database.instance.getRewards();
            for (Reward reward : rewards) {
                reward.setRewardMulti(Database.instance.getRewardMultis(reward.getId()));
            }
            for (Reward reward : rewards) {
                initTournament(reward);
            }
        }
    }
    
    private void initTournament(Reward reward) {
        //danh sách hệ số nhân và rate
        TreeMap<Integer, Integer> tournamentByMultis = new TreeMap<>();
        Map<Integer, Integer> stPlaces = new HashMap<>();
        int maxBonus = 0;
        /**
         * tỉ số để làm tròn Frequency(tỉ lệ xuất hiện giải) thành số nguyên
         */
        int ratio =0;
        for (RewardMulti value : reward.getRewardMulti()) {
            BigDecimal bd2 = new BigDecimal(String.valueOf(value.getFrequency()));
            int scaleTemp = bd2.scale();//count số thap phan. 10.122 = 3
            int pow = (int) Math.pow(10, scaleTemp);
            if(pow > ratio){
                ratio = pow;
            }
        }

        for (RewardMulti rewardMulti : reward.getRewardMulti()) {
            tournamentByMultis.put(rewardMulti.getMultiplier(), (int) Utils.multiply((double)ratio, rewardMulti.getFrequency()));
            stPlaces.put(rewardMulti.getMultiplier(), (int) rewardMulti.getPlace_1st());
            if (rewardMulti.getMultiplier() > maxBonus) {
                maxBonus = rewardMulti.getMultiplier();
            }
        }

        Tournament tour = new Tournament(Service.TIEN_LEN_TOUR, reward.getBet());
        tour.setBonusMultiMax(maxBonus);
        tour.setTournamentByMultis(tournamentByMultis);
        tour.setFund(reward.getFund());
        tour.setStPlaces(stPlaces);
        tournaments.put(reward.getBet(), tour);
    }

    public int getRewardMulti(double betBoard, int multi){
        if(!this.tournaments.containsKey(betBoard)){
            return 0;
        }
        return tournaments.get(betBoard).getStPlaces().get(multi);
    }

    public int getTax(double betBoard){ 
        if(!this.tournaments.containsKey(betBoard)){
            return 0;
        }
        return (100 - tournaments.get(betBoard).getFund());
    }
    
    public List<Double> getTicketValues(){
        return new ArrayList(tournaments.keySet());
    }

}
