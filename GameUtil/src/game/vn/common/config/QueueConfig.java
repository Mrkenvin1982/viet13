/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.config;

import game.vn.util.watchservice.PropertyConfigurator;

/**
 *
 * @author hanv
 */
public class QueueConfig extends PropertyConfigurator {
    private static final QueueConfig INSTANCE = new QueueConfig("conf/", "queue.properties");
    
    public QueueConfig(String path, String nameFile) {
        super(path, nameFile);
    }
    
    public static QueueConfig getInstance() {
        return INSTANCE;
    }
    
    public String getQueueApiHost(){
        return this.getStringAttribute("api.host");
    }
    
    public int getQueueApiPort(){
        return this.getIntAttribute("api.port", 5672);
    }
    
    public int getQueueApiPoolSize(){
        return this.getIntAttribute("api.poolSize", 3); 
    }
    
    public String getQueueApiUserName(){
        return this.getStringAttribute("api.username");
    }
    
    public String getQueueApiPassWord(){
        return this.getStringAttribute("api.password");
    }

    public String getQueueApiExchange(){
        return this.getStringAttribute("api.exchange", "z88.direct");
    }

    public String getQueueHost() {
        return getStringAttribute("host");
    }
    
    public int getQueuePort() {
        return getIntAttribute("port", 5672);
    }
    
    public String getQueueUsername() {
        return getStringAttribute("username");
    }
    
    public String getQueuePassword() {
        return getStringAttribute("password");
    }

    public int getQueuePoolSize() {
        return getIntAttribute("poolSize", 3);
    }

    private String getPrefix() {
        return getStringAttribute("prefix", "");
    }

    private String getSuffix() {
        return getStringAttribute("suffix", "");
    }

    public String getExchangeFindBoard() {
        return getPrefix() + getStringAttribute("exchange.findboard", "EXCHANGE_FIND_BOARD") + getSuffix();
    }

    public String getExchangeUpdateConfig() {
        return getPrefix() + getStringAttribute("exchange.updateconfig", "EXCHANGE_UPDATE_CONFIG") + getSuffix();
    }
    
    public String getExchangeUpdatePoint() {
        return getPrefix() + getStringAttribute("exchange.updatepoint", "EXCHANGE_UPDATE_POINT") + getSuffix();
    }
    
    public String getExchangeUpdateLeaderBoard() {
        return getPrefix() + getStringAttribute("exchange.updateleaderboard", "EXCHANGE_UPDATE_LEADER_BOARD") + getSuffix();
    }
    
    public String getExchangeHistory() {
        return getPrefix() + getStringAttribute("exchange.history", "EXCHANGE_HISTORY") + getSuffix();
    }

    public String getExchangeNotify() {
        return getPrefix() + getStringAttribute("exchange.notify", "EXCHANGE_NOTIFY") + getSuffix();
    }
    
    public String getExchangeRankingData() {
        return getPrefix() + getStringAttribute("exchange.ranking.data", "EXCHANGE_USER_RANKING_DATA") + getSuffix();
    }

    public String getExchangeRankingRequest() {
        return getPrefix() + getStringAttribute("exchange.ranking.request", "EXCHANGE_REQUEST_USER_RANKING") + getSuffix();
    }

    public String getExchangeRankingResponse() {
        return getPrefix() + getStringAttribute("exchange.ranking.response", "EXCHANGE_RESPONSE_USER_RANKING") + getSuffix();
    }

    public String getExchangeVipRequest() {
        return getPrefix() + getStringAttribute("exchange.vip.request", "EXCHANGE_REQUEST_USER_VIP") + getSuffix();
    }

    public String getExchangeVipResponse() {
        return getPrefix() + getStringAttribute("exchange.vip.response", "EXCHANGE_RESPONSE_USER_VIP") + getSuffix();
    }

    public String getExchangeUserManager() {
        return getPrefix() + getStringAttribute("exchange.usermanager", "EXCHANGE_USER_MANAGER") + getSuffix();
    }

    public String getExchangeTaiXiu() {
        return getPrefix() + getStringAttribute("exchange.taixiu", "EXCHANGE_Z88_TAIXIU") + getSuffix();
    }

    public String getExchangeTaiXiuNotify() {
        return getPrefix() + getStringAttribute("exchange.taixiu.notify", "EXCHANGE_Z88_TAIXIU_NOTIFY") + getSuffix();
    }

    public String getExchangeTransactionHistory() {
        return getPrefix() + getStringAttribute("exchange.transhistory", "EXCHANGE_TRANSACTION_HISTORY") + getSuffix();
    }

    public String getExchangeQuest() {
        return getPrefix() + getStringAttribute("exchange.quest", "EXCHANGE_QUEST") + getSuffix();
    }

    public String getQueueFindBoardResponse() {
        return getPrefix() + getStringAttribute("queue.findboard.response", "QUEUE_FIND_BOARD_RESPONSE_") + getSuffix();
    }

    public String getQueueNotify() {
        return getPrefix() + getStringAttribute("queue.notify", "QUEUE_NOTIFY_") + getSuffix();
    }
    
    public String getQueueRankingData() {
        return getPrefix() + getStringAttribute("queue.ranking.data", "QUEUE_USER_RANKING_DATA") + getSuffix();
    }
    
    public String getQueueRankingResponse() {
        return getPrefix() + getStringAttribute("queue.ranking.response", "QUEUE_RESPONSE_USER_RANKING_") + getSuffix();
    }

    public String getQueueVipRequest() {
        return getPrefix() + getStringAttribute("queue.vip.request", "QUEUE_REQUEST_USER_VIP") + getSuffix();
    }
    
    public String getQueueVipResponse() {
        return getPrefix() + getStringAttribute("queue.vip.response", "QUEUE_RESPONSE_USER_VIP_") + getSuffix();
    }

    public String getQueueUpdateConfigGame() {
        return getPrefix() + getStringAttribute("queue.gameconfig.update", "QUEUE_UPDATE_CONFIG_GAME_") + getSuffix();
    }

    public String getQueueConfigGame() {
        return getPrefix() + getStringAttribute("queue.gameconfig.get", "QUEUE_GET_CONFIG_GAME") + getSuffix();
    }
    
    public String getQueueServices() {
        return getPrefix() + getStringAttribute("queue.services", "QUEUE_GET_SERVICES") + getSuffix();
    }

    public String getQueueTurnOffGame() {
        return getPrefix() + getStringAttribute("queue.turnoffgame.get", "QUEUE_GET_TURN_OFF_GAME") + getSuffix();
    }

    public String getQueueUpdateTurnOffGame() {
        return getPrefix() + getStringAttribute("queue.turnoffgame.update", "QUEUE_UPDATE_TURN_OFF_GAME_") + getSuffix();
    }

    public String getQueueKickUser() {
        return getPrefix() + getStringAttribute("queue.kickuser", "QUEUE_KICK_USER_") + getSuffix();
    }
    
    public String getQueueMaintain() {
        return getPrefix() + getStringAttribute("queue.maintain", "QUEUE_MAINTAIN_GAME_") + getSuffix();
    }

    public String getQueueTournamentConfig() {
        return getPrefix() + getStringAttribute("queue.tournament.get", "QUEUE_GET_CONFIG_GAME_TOURNAMENT") + getSuffix();
    }

    public String getQueueUpdateTournament() {
        return getPrefix() + getStringAttribute("queue.tournament.update", "QUEUE_UPDATE_CONFIG_GAME_TOURNAMENT_") + getSuffix();
    }

    public String getQueueTransactionHistoryResponse() {
        return getPrefix() + getStringAttribute("queue.transhistory.response", "QUEUE_TRANSACTION_HISTORY_RESPONSE_") + getSuffix();
    }
    
    public String getQueueRemoveUserHazelcast() {
        return getPrefix() + getStringAttribute("queue.removehazelcast", "QUEUE_REMOVE_USER_ON_HAZELCAST") + getSuffix();
    }

    public String getQueuePaymentConfig() {
        return getPrefix() + getStringAttribute("queue.paymentconfig", "QUEUE_PAYMENT_CONFIG") + getSuffix();
    }
    
    public String getQueueLeaderboardConfig() {
        return getPrefix() + getStringAttribute("queue.leaderboard.get", "QUEUE_LEADER_BOARD_CONFIG") + getSuffix();
    }
    
    public String getQueueUpdateLeaderboard() {
        return getPrefix() + getStringAttribute("queue.leaderboard.update", "QUEUE_UPDATE_LEADER_BOARD_") + getSuffix();
    }
    
    public String getQueueUpdatePoint() {
        return getPrefix() + getStringAttribute("queue.updatepoint", "QUEUE_UPDATE_POINT_") + getSuffix();
    }
    
    public String getQueueTaiXiuResponse() {
        return getPrefix() + getStringAttribute("queue.taixiu.response", "QUEUE_Z88_TAIXIU_RESPONSE_") + getSuffix();
    }

    public String getQueueQuestRequest() {
        return getPrefix() + getStringAttribute("queue.quest.request", "QUEUE_QUEST_REQUEST") + getSuffix();
    }

    public String getQueueQuestResponse() {
        return getPrefix() + getStringAttribute("queue.quest.response", "QUEUE_QUEST_RESPONSE_") + getSuffix();
    }

    public String getKeyInvoice() {
        return getStringAttribute("api.key.invoice", "invoice");
    }
    
    public String getKeyLogin() {
        return getStringAttribute("api.key.login", "login");
    }
    
    public String getKeyLogout() {
        return getStringAttribute("api.key.logout", "logout");
    }
    
    public String getKeyRegister() {
        return getStringAttribute("api.key.register", "register");
    }
    
    public String getKeyCCU() {
        return getStringAttribute("api.key.ccu", "ccu");
    }

    public String getKeyPayment() {
        return getStringAttribute("api.key.payment", "payment");
    }
    
    public String getKeyBalance() {
        return getStringAttribute("api.key.balance", "balance");
    }
    
    /**
     * Event săn bài đẹp
     * @return 
     */
    public String getExchangeUserQuestData() {
        return getPrefix() + getStringAttribute("exchange.user.quest.data", "EXCHANGE_USER_QUEST_DATA") + getSuffix();
    }
    
    public String getQueueUserQuestData() {
        return getPrefix() + getStringAttribute("queue.user.quest.data", "QUEUE_USER_QUEST_DATA") + getSuffix();
    }

}