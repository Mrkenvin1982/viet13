/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author hanv
 */
public class BoardLog {
    private AtomicInteger _id = new AtomicInteger();
    private int _roomId;
    private int _controllerId;
    private int _totalUserInController;
    private long _startTime;
    private int _tax;
    private List<BoardUserLog> _arrayBoardUserLog;
    /**
     * Tổng tiền cược trong game
     */
    private int _money;
    /**
     * gameid
     */
    private int _gameId;
    /**
     * ngày ghi log: ddMMyyyy
     */
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTax() {
        return _tax;
    }

    public void setTax(int _tax) {
        this._tax = _tax;
    }

    public void addTax(int tax) {
        this._tax += tax;
    }

    public long getStartTime() {
        return _startTime;
    }

    public void setStartTime(long _startTime) {
        this._startTime = _startTime;
    }

    public int getControllerId() {
        return _controllerId;
    }

    public void setControllerId(int _controllerId) {
        this._controllerId = _controllerId;
    }

    public int getGameId() {
        return _gameId;
    }

    public void setGameId(int _game) {
        this._gameId = _game;
    }

    public int getId() {
        return _id.get();
    }

    public void setId(int _id) {
        this._id.set(_id);
    }

    public int getMoney() {
        return _money;
    }

    public void setMoney(int _money) {
        this._money = _money;
    }

    public int getRoomId() {
        return _roomId;
    }

    public void setRoomId(int _roomId) {
        this._roomId = _roomId;
    }

    public int getTotalUserInController() {
        return _totalUserInController;
    }

    public void setTotalUserInController(int _totalUserInController) {
        this._totalUserInController = _totalUserInController;
    }

    public List<BoardUserLog> getArrayBoardUserLog() {
        return _arrayBoardUserLog;
    }

    public void setArrayBoardUserLog(List<BoardUserLog> _arrayBoardUserLog) {
        this._arrayBoardUserLog = _arrayBoardUserLog;
    }

    public void reset() {        
        _startTime = _roomId = _controllerId = _totalUserInController =  _tax = _money = _gameId = 0;         
        _id.set(0);
        _arrayBoardUserLog = null;
        date = "";
    }
    
    public int[] getUserIds() {        
        if(_arrayBoardUserLog != null && _arrayBoardUserLog.size() > 0) {
            int[] userIds = new int[_arrayBoardUserLog.size()];
            BoardUserLog userLog;
            for (int i = 0; i < _arrayBoardUserLog.size(); i++) {
                userLog = _arrayBoardUserLog.get(i);
                userIds[i] = userLog.getUserId();
            }
            return userIds;
        }
        return null;
    }

    @Override
    public String toString() {
        return "BoardLog{" + "_id=" + _id + ", _roomId=" + _roomId + ", _controllerId=" + _controllerId + ", _totalUserInController=" + _totalUserInController + ", _startTime=" + _startTime + ", _tax=" + _tax + ", _arrayBoardUserLog=" + _arrayBoardUserLog + ", _money=" + _money + ", _gameId=" + _gameId + ", date=" + date + '}';
    }
}
