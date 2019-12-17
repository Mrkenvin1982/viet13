/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.tournament;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.HashMap;

/**
 * Giải thưởng tournament spin and go theo từng mức cược
 * @author
 */
public class Tournament {
    
    private final double betBoard;
    private final byte serviceId;
    
    private TreeMap<Integer,Integer>tournamentByMultis;
    private Map<Integer, Integer> stPlaces ;
    private final SecureRandom secureRandom ;
    private int fund;
    //hệ số nhân cao nhất của 1 chu kỳ tournament
    private int bonusMultiMax;

    public Tournament(byte serviceIdInput, double betBoardInput){
        this.betBoard = betBoardInput;
        this.serviceId = serviceIdInput;
        this.tournamentByMultis = new TreeMap<>();
        this.stPlaces= new HashMap<>();
        this.secureRandom = new SecureRandom(); 
    }

    public double getBetBoard() {
        return betBoard;
    }
     
    /***
     * Lấy ra giải thưởng và update thông tin
     * @param valueAdded: tiền mua vé của user
     * @return 
     */
    public int getMulti(double valueAdded) {
        HazelcastUtil.lockTourByBetBoard(serviceId);
        
        double sumMoney = HazelcastUtil.getSumMoneyByBetBoard(serviceId);
        sumMoney = Utils.add(sumMoney, valueAdded);

        //số lần tiền lớn nhất có thể trúng
        int multiMax = (int)Utils.divide(sumMoney, betBoard);

        int index = secureRandom.nextInt(this.getRateSum(multiMax));
        
        //lấy ra giá trị giải trung được
        int multi = this.getMultiByRate(index);
        if(multi == 0){
            HazelcastUtil.unlockTourByBetBoard(serviceId);
            return multi;
        }
        double valueMoney = Utils.multiply(multi, betBoard);//so tien thang giai
        sumMoney = Utils.subtract(sumMoney, valueMoney);
        
        Database.instance.updateTournament(serviceId, sumMoney);
        
        //update thông tin xuống cast
        HazelcastUtil.updateBonusInfor(serviceId, sumMoney);
        
        HazelcastUtil.unlockTourByBetBoard(serviceId);
        
        return multi;
    }

    public int getBonusMultiMax() {
        return bonusMultiMax;
    }

    public void setBonusMultiMax(int bonusMultiMax) {
        this.bonusMultiMax = bonusMultiMax;
    }

    public void setTournamentByMultis(TreeMap<Integer, Integer> tournamentByMultis) {
        this.tournamentByMultis = tournamentByMultis;
    }
    
    /**
     *
     * @param rateInput
     * @return
     */
    private int getMultiByRate(int rateInput) {
        int rateSum = 0;
        int bonusValue = 0;
        rateInput += 1;

        // Get a set of the entries
        Set set = tournamentByMultis.entrySet();
        // Get an iterator
        Iterator it = set.iterator();
        // Display elements
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            int rate = (int) me.getValue();
            int multi = (int) me.getKey();
            if (rate == 0) {
                continue;
            }

            rateSum += rate;
            if (rateInput <= rateSum) {
                bonusValue = multi;
                break;
            }
        }
        return bonusValue;
    }

    /**
     * Lấy ra số tỉ lệ để random trúng giải
     * hệ số khong được vượt quá tổng số tiền thu được
     * @param maxMulti
     * @return 
     */
    private int getRateSum(int maxMulti) {
        int rateSum = 0;
        Set set = tournamentByMultis.entrySet();
        // Get an iterator
        Iterator it = set.iterator();
        // Display elements
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            int rate = (int) me.getValue();
            int multi = (int) me.getKey();
            if(rate ==0 || multi > maxMulti){
                continue;
            }
            
            rateSum += rate;
        }
        
        return rateSum;
    }
    
    /**
     * get thông tin tournamnet hiện tại theo hệ số để luu xuống DB
     *
     * @return
     */
    public String getInfor() {
        String infor = "";
        for (Integer key : tournamentByMultis.keySet()) {
            if (infor.isEmpty()) {
                infor += key + "," + tournamentByMultis.get(key);
            } else {
                infor += ";" + key + "," + tournamentByMultis.get(key);
            }
        }
        return infor;
    }

    public int getFund() {
        return fund;
    }

    public void setFund(int fund) {
        this.fund = fund;
    }

    public Map<Integer, Integer> getStPlaces() {
        return stPlaces;
    }

    public void setStPlaces(Map<Integer, Integer> stPlaces) {
        this.stPlaces = stPlaces;
    }

}
