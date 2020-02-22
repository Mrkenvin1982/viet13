/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.handler.request;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import game.command.SFSAction;
import game.command.SFSCommand;
import game.key.SFSKey;
import game.vn.common.GameExtension;
import game.vn.common.config.RoomConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.constant.MoneyContants;
import game.vn.common.constant.Service;
import game.vn.common.lang.GameLanguage;
import game.vn.common.lib.contants.PlayMode;
import game.vn.common.lib.findboard.FindBoardRequest;
import game.vn.common.lib.hazelcast.Board;
import game.vn.common.lib.hazelcast.PlayingBoardManager;
import game.vn.common.lib.hazelcast.UserState;
import game.vn.common.message.MessageController;
import game.vn.common.properties.RoomInforPropertiesKey;
import game.vn.common.properties.UserInforPropertiesKey;
import game.vn.common.tournament.TournamentManager;
import game.vn.util.HazelcastUtil;
import game.vn.util.Utils;
import game.vn.util.db.Database;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Xử lý các message trong core(room lobby)
 * @author tuanp
 */
public class CommonClientRequest extends BaseClientRequestHandler {

    private final static int BUY_STACK_SUCCESS=0;
    private final static int BUY_STACK_ERROR=1;
    
    @Override
    public void handleClientRequest(User user, ISFSObject isfso) {
        GameExtension getExt = (GameExtension) getParentExtension();
        if (!getExt.getParentRoom().isGame()) {
            processClientMessage(user, isfso);
        }
    }

    /**
     * Xử lý các message liên quan tới lobby chổ này
     */
    private void processClientMessage(User user, ISFSObject isfso) {
        try {
            trace(user.getName(), "send common client request action in lobby", isfso.getDump());
            int action = isfso.getInt(SFSKey.ACTION_INCORE);
            if (action == SFSAction.LOBBY_LIST_COUNTER) {
                //gửi về danh sách mức cược của game khi join lobby
                SFSObject ob = getLobbyCounterListMessage(getParentExtension().getParentRoom().getName(), getMoneyType());
                getParentExtension().send(SFSCommand.CLIENT_REQUEST_INGAME, ob, user);
                return;
            }
            //is lobby
            //kiểm tra game có đang bảo trì
            int idService=Utils.getServiceId(getParentExtension().getParentRoom().getName());
            if (RoomConfig.getInstance().isMaintainGame(idService) || RoomConfig.getInstance().isMaintainAllGame()) {
                SFSObject ob = MessageController.getToastMessage(RoomConfig.getInstance().getMaintainInfor(Utils.getUserLocale(user)),3);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                return;
            }

            switch (action) {
                case SFSAction.BUY_STACK_IN_LOBBY:
                    //mức cược bàn user chọn
                    double betBoard=isfso.getDouble(SFSKey.BET_BOARD);
                    //số tiền user mua tẩy
                    double moneyStack=isfso.getDouble(SFSKey.MONEY_STACK);
                    boolean isOwner=isfso.getBool(SFSKey.IS_OWNER);
                    boolean isTournament = RoomConfig.getInstance().getTournamentNameGames().contains(getParentExtension().getParentRoom().getName());
                    if(isTournament){
                        buyTicketInTournament(user, betBoard);
                    }else{
                        processBuyStack(user, betBoard, moneyStack, isOwner, false);
                    }
                   
                    break;
                case SFSAction.AUTO_JOIN:
                    autoJoinBoard(user);
                    break;
            }
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.processClientMessage() error: ", e);
        }
    }
    
    /**
     * lấy ra danh sách mức cược trong lobby game
     * @return 
     */
    private List<Double> getBets() {
        List<Double> bets= new ArrayList<>();
        try {
            //lấy ra danh sách mức cược trong lobby game
        String strBets = RoomConfig.getInstance().getListBet(getParentExtension().getParentRoom().getName());
        bets=Utils.convertToListDouble(strBets);
        Collections.sort(bets);
        } catch (Exception e) {
             this.getLogger().error("CommonClientRequest.getBets() error: ", e);
        }
        return  bets;
    }
    
    /**
     * Làm tròn theo danh sách mức cược hiện có
     * @param bet
     * @return 
     */
    private double getRoundingBet(double bet){
        List<Double> listValues = getBets();
        if(listValues.isEmpty()){
            return 0;
        }
        if(bet >listValues.get((listValues.size()-1))){
            bet = listValues.get((listValues.size()-1));
            return bet;
        }
            
        //chọn cạnh dưới(50<value<70 ==> value=50)
        if (!listValues.contains(bet)) {
            for (int i = 1; i < listValues.size(); i++) {
                if (bet < listValues.get(i)) {
                    bet = listValues.get(i - 1);
                    break;
                }
            }
        }
        return bet;
    }
    
    private int getMoneyType(){
        return getParentExtension().getParentRoom().getVariable(RoomInforPropertiesKey.MONEY_TYPE).getIntValue();
    }
    
    /**
     * auto join bàn
     * @param user 
     */
    private void autoJoinBoard(User user) {
        try {
            double moneyOfUser = getMoneyType() == MoneyContants.MONEY ? Utils.getMoneyOfUser(user): Utils.getPointOfUser(user);
            double boardBet = getRoundingBet(moneyOfUser / RoomConfig.getInstance().getDefaultMoneyMultiplier(getParentExtension().getParentRoom().getName()));
            double moneyBuyStack =Utils.multiply(boardBet, RoomConfig.getInstance().getMinJoinOwner(getParentExtension().getParentRoom().getName()));
            moneyBuyStack = Math.min(moneyBuyStack, moneyOfUser);

            processBuyStack(user, boardBet, moneyBuyStack, false, true);
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.autoJoinBoard()error: ", e);
        }
    }
    
    /**
     * Xử lý mua tẩy và tìm bàn gửi về cho client
     * @param user
     * @param betBoard
     * @param moneyStack 
     */
    private void processBuyStack(User user, double betBoard, double moneyStack, boolean isOwner, boolean isAutoJoin) {
        try {
            String lobbyName = getParentExtension().getParentRoom().getName();
            String idDBUser = Utils.getIdDBOfUser(user);
            //check xem có chơi bàn nào không, nếu có thi phai reconnect khong cho thao tac khac
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(idDBUser);
            if (playingBoard != null) {
                if (playingBoard.getBoardPlaying() != null) {
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user));
                    SFSObject ob = getMessageBuyStack(infor,BUY_STACK_ERROR);
                    getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                    this.getLogger().error(idDBUser + " reconnect buy stack " + betBoard + " " + moneyStack);
                    return;
                }
            }

            //kiem tra mưc cược client gửi lên mua tẩy có tồn tại 
            if (!getBets().contains(betBoard)) {
                String infor = GameLanguage.getMessage(GameLanguage.NOT_EXIST_BET_BOARD, Utils.getUserLocale(user));
                SFSObject ob = getMessageBuyStack(infor,BUY_STACK_ERROR);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                this.getLogger().error("user send bet money not exist"+ betBoard);
                return;
            }
            
            double moneySum = 0;
            if(getMoneyType() == MoneyContants.MONEY){
                moneySum = Database.instance.getUserMoney(idDBUser);
                Utils.updateMoneyOfUser(user, moneySum);
            }else{
                moneySum = Database.instance.getUserPoint(idDBUser);
                Utils.updatePointOfUser(user, moneySum);
            }
            moneyStack = Math.min(moneyStack, moneySum);
            
            //số lần mức cược tối thiểu mua tẩy
            int moneyFactory = RoomConfig.getInstance().getMinJoinGame(lobbyName);
            if (isOwner) {
                moneyFactory = RoomConfig.getInstance().getMinJoinOwner(lobbyName);
            }
            //số tiền tối thiểu mua tẩy
            double minBetBoard =Utils.multiply(betBoard, moneyFactory) ;
            //đảm bảo số tiền mua tẩy lờn hơn hoặc = mức cược tối thiểu của board
            if (moneyStack < minBetBoard) {
                SFSObject ob = getMessage(user, GameLanguage.NO_MONEY_USER, moneyFactory);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                this.getLogger().error("not enough min bet money");
                return;
            }

            byte mode = Utils.getUserPlayMode(user, Utils.getServiceId(lobbyName));

            if (!buyStack(user, moneyStack, betBoard, isAutoJoin, mode)) {
                SFSObject ob = getMessage(user, GameLanguage.NO_MONEY_USER, moneyFactory);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
            }
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.processBuyStack()error: ", e);
        }
    }
    
    private void buyTicketInTournament(User user, double moneyTicket) {
        try {
            String idDBUser = Utils.getIdDBOfUser(user);
            //check xem có chơi bàn nào không, nếu có thi phai reconnect khong cho thao tac khac
            PlayingBoardManager playingBoard = HazelcastUtil.getPlayingBoard(idDBUser);
            if (playingBoard != null) {
                if (playingBoard.getBoardPlaying() != null) {
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user));
                    SFSObject ob = getMessageBuyStack(infor, BUY_STACK_ERROR);
                    getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                    this.getLogger().error(idDBUser + " reconnect buy stack " + moneyTicket);
                    return;
                }
            }

            //kiem tra mưc cược client gửi lên mua tẩy có tồn tại 
            if (!TournamentManager.getInstance().getTicketValues().contains(moneyTicket)) {
                String infor = GameLanguage.getMessage(GameLanguage.NOT_EXIST_BET_BOARD, Utils.getUserLocale(user));
                infor = String.format(infor, Utils.getCurrencyName(Utils.getMoneyTypeOfUser(user), Utils.getUserLocale(user)));
                SFSObject ob = getMessageBuyStack(infor, BUY_STACK_ERROR);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                this.getLogger().error("user send bet money not exist" + moneyTicket);
                return;
            }

            int moneyType = Utils.getMoneyTypeOfUser(user);
            double moneySum = 0;
            if (moneyType == MoneyContants.MONEY) {
                moneySum = Database.instance.getUserMoney(idDBUser);
                Utils.updateMoneyOfUser(user, moneySum);
            } else {
                moneySum = Database.instance.getUserPoint(idDBUser);
                Utils.updatePointOfUser(user, moneySum);
            }

            //đảm bảo số tiền mua tẩy lờn hơn hoặc = mức cược tối thiểu của board
            if (moneySum < moneyTicket) {
                SFSObject ob = getMessage(user, GameLanguage.NO_MONEY_USER, (int) moneyTicket);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
                this.getLogger().error("not enough min bet money");
                return;
            }
            if (!buyStack(user, moneyTicket, moneyTicket, false, PlayMode.NORMAL)) {
                SFSObject ob = getMessage(user, GameLanguage.NO_MONEY_USER, (int) moneyTicket);
                getParentExtension().send(SFSCommand.CLIENT_REQUEST, ob, user);
            }
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.buyTicketInTournament()error: ", e);
        }
    }
    
    /**
     * get message buy stack error
     * @param infor
     * @return 
     */
    private SFSObject getMessageBuyStack(String infor, int type) {
        SFSObject fObject = new SFSObject();
        fObject.putInt(SFSKey.ACTION_INCORE, SFSAction.BUY_STACK_IN_LOBBY);
        fObject.putInt("type", type);
        fObject.putUtfString(SFSKey.TOAST_MESSAGE, infor);
        return fObject;
    }
    
    /**
     * Mua tẩy để vào bàn chơi
     */
    private boolean buyStack(User user, double moneyStack, double betBoard, boolean isAutoJoin, byte mode) {
        int moneyType = getMoneyType();
        boolean isSuccess;
        if (moneyType == MoneyContants.MONEY) {
            isSuccess = buyMoneyStack(user, moneyStack, betBoard);
        } else {
            isSuccess = buyPointStack(user, moneyStack, betBoard);
        }

        if (isSuccess) {
            return findBoard(user, betBoard, moneyType, isAutoJoin, mode);
        }
        return false;
    }
    
    /**
     * Mua tẩy bằng điểm
     * @param user
     * @param moneyStack
     * @return 
     */
    private boolean buyPointStack(User user, double moneyStack, double betBoard){
        try {
            String idDBUser=Utils.getIdDBOfUser(user);
            
            UserState userState = HazelcastUtil.getUserState(idDBUser);
            userState.setPointStack(moneyStack);
            userState.setMoneyType(MoneyContants.POINT);
            userState.setBetBoard(betBoard);
            HazelcastUtil.updateUserState(userState);  
            
            return true;
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.buyPointStack()error: ", e);
        }
        return false;
    }
    
    /**
     * Mua tẩy bằng tiền thật
     * @param user
     * @param moneyStack
     * @return 
     */
    private boolean buyMoneyStack(User user,double moneyStack,double betBoard){
        try {
            String idDBUser=Utils.getIdDBOfUser(user);
            
            UserState userState = HazelcastUtil.getUserState(idDBUser);
            userState.setMoneyStack(moneyStack);
            userState.setMoneyType(MoneyContants.MONEY);
            userState.setBetBoard(betBoard);
            HazelcastUtil.updateUserState(userState);   
            return true;
        } catch (Exception e) {
            this.getLogger().error("CommonClientRequest.buyMoneyStack()error: ", e);
        }
        return false;
    }
    
    private SFSObject getMessage(User user, String key, int minFactory) {
        Locale lo = Utils.getUserLocale(user);
        String infor = GameLanguage.getMessage(key, lo);
        infor = String.format(infor , minFactory);
        SFSObject ob = getMessageBuyStack(infor,BUY_STACK_ERROR);
        return ob;
    }
    
    /**
     *  Tìm bàn cho user theo loại tiền và mức cược
     * @param user
     * @param bet 
     */
    private boolean findBoard(User user, double bet, int moneyType, boolean isAutoJoin, byte mode) {
        byte serviceId = (byte)user.getProperty(UserInforPropertiesKey.SERVICE_ID);
        String userId = Utils.getIdDBOfUser(user);
        FindBoardRequest request = new FindBoardRequest();
        request.setUserId(userId);
        request.setBetMoney(bet);
        request.setMoneyType(moneyType);
        request.setServiceId(serviceId);
        request.setServerId(ServerConfig.getInstance().getServerId());
        request.setAutoJoin(isAutoJoin);
        request.setIpUser(user.getSession().getAddress());
        request.setMode(mode);
        List<Board> boards = HazelcastUtil.findBoard(request);
        
        try {
            Room room = boards.isEmpty() ? createRoom(user) : getParentExtension().getParentZone().getRoomByName(boards.get(0).getName());
            getApi().joinRoom(user, room, null, false, null);
            return true;
        } catch (Exception ex) {
            getLogger().error("findboard: " + userId +  " " + bet + " " + isAutoJoin + " " + mode, ex);
        }
        return false;
    }
    
    private Room createRoom(User user) {
        try {
            String userId =  Utils.getIdDBOfUser(user);
            Locale localeOfUser = Utils.getUserLocale(user);
            
            UserState userState = HazelcastUtil.getUserState(userId);
            if(userState == null){
                String infor=GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                throw new Exception("----userState null in gameLogin ");
            }
            if (userState.getCurrentLobbyName().isEmpty()) {
                String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                this.getLogger().error("----getCurrentLobbyName is empty in gameLogin error ");
                return null;
            }
           
            List<Double> listBet;
            boolean isTournament = RoomConfig.getInstance().getTournamentNameGames().contains(userState.getCurrentLobbyName());
            if (isTournament) {
                listBet = TournamentManager.getInstance().getTicketValues();
            } else {
                //lấy ra danh sách mức cược trong lobby game
                String strBets = RoomConfig.getInstance().getListBet(userState.getCurrentLobbyName());
                //không tồn tại lobby request tạo bàn
                if (strBets.isEmpty()) {
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                    getApi().kickUser(user, null, infor, 1);
                    throw new Exception(infor + "----strBets is empty in gameLogin ");
                }
                //không  tồn tại mức cược gửi lên tạo bàn
                listBet = Utils.convertToListDouble(strBets);
            }

            
            if(!listBet.contains(userState.getBetBoard())){
                String infor = GameLanguage.getMessage(GameLanguage.NOT_EXIST_BET_BOARD, localeOfUser);
                getApi().kickUser(user, null, infor, 1);
                return null;
            }
            
            //check xem có chơi bàn nào không, nếu có cho vao khong tạo nữa
            PlayingBoardManager playingBoard= HazelcastUtil.getPlayingBoard(userId);
            if (playingBoard != null) {
                if (playingBoard.getBoardPlaying() != null) {
                    Room room = getParentExtension().getParentZone().getRoomByName(playingBoard.getBoardPlaying().getName());
                    if (room != null) {
                        getApi().joinRoom(user, room);
                        return null;
                    }
                    String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, localeOfUser);
                    getApi().kickUser(user, null, infor, 1);
                    throw new Exception(infor + "----can not comback playing game :"+playingBoard.getBoardPlaying().getName());
                }
            }
            
            //số lần mức cược tối thiểu mua tẩy
            int moneyFactory = RoomConfig.getInstance().getMinJoinOwner(userState.getCurrentLobbyName());
            //số tiền tối thiểu mua tẩy
            double minBetBoard =Utils.multiply(userState.getBetBoard(), moneyFactory) ;
            
            //xét tiền cho user chổ này
            double money;
            int moneyType = Utils.getMoneyTypeOfUser(user);
            if (moneyType  == MoneyContants.MONEY) {
                money = userState.getMoneyStack();
                if (money < minBetBoard && Database.instance.getUserMoney(userId) >= minBetBoard) {
                    money = minBetBoard;
                    userState.setMoneyStack(money);
                }
            } else {
                money = userState.getPointStack();
                if (money < minBetBoard && Database.instance.getUserPoint(userId) >= minBetBoard) {
                    money = minBetBoard;
                    userState.setPointStack(money);
                }
            }
            HazelcastUtil.updateUserState(userState);
            
            if (money < minBetBoard) {
                String infor = GameLanguage.getMessage(GameLanguage.NO_MONEY_USER, localeOfUser);
                infor = String.format(infor, moneyFactory);
                getApi().kickUser(user, null, infor, 1);
                return null;
            }
            
            //khi vào handler join room thi chặn không cho tạo bàn nữa
            byte serviceId = Utils.getServiceId(userState.getCurrentLobbyName());
            byte mode = Utils.getUserPlayMode(user, serviceId);

            if (serviceId == Service.SYSTEM) {
                String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user));
                getApi().kickUser(user, null, infor, 1);
            } else {
                return Utils.createBoardGame(userState.getBetBoard(), serviceId, getParentExtension().getParentZone(), moneyType , mode, isTournament);
            }
        } catch (Exception e) {
            this.getLogger().error("userJoinRoom error: ", e);
            String infor = GameLanguage.getMessage(GameLanguage.JOIN_ROOM_ERROR, Utils.getUserLocale(user));
            getApi().kickUser(user, null, infor, 1);
        }
        return null;
    }
    
    /**
     * Gửi về danh sách mức cược cho user
     *
     * @param serviceId
     * @return
     */
    private SFSObject getLobbyCounterListMessage(String nameLobby, int moneyType) {
        SFSObject ojBoardInfo = new SFSObject();
        try {
            ojBoardInfo.putInt(SFSKey.ACTION_INCORE, SFSAction.LOBBY_LIST_COUNTER);
            ojBoardInfo.putByte(SFSKey.MONEY_TYPE,(byte)moneyType);
            boolean isTournament = RoomConfig.getInstance().getTournamentNameGames().contains(nameLobby);
            int max=0;
            if (!isTournament) {
                //danh sách mức cược game 
                String strBet = RoomConfig.getInstance().getListBet(nameLobby);
                List<Double> bets = Utils.convertToListDouble(strBet);
                ojBoardInfo.putDoubleArray(SFSKey.LIST_BET_BOARD, bets);
            }else{
                ojBoardInfo.putDoubleArray(SFSKey.LIST_BET_BOARD, TournamentManager.getInstance().getTicketValues());
                max = TournamentManager.getInstance().getBonusMultiMax(TournamentManager.getInstance().getTicketValues().get(0));
            }
            
            //số lần tiền thưởng(game tournament)
            ojBoardInfo.putInt("maxBonus", max);
            
        } catch (Exception e) {
            this.getLogger().error("getLobbyCounterList erro: ", e);
        }
        return ojBoardInfo;
    }
}
