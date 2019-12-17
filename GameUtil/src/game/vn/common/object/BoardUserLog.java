/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 * lưu lai thông tin user khi start bàn chơi
 * @author hanv
 */
public class BoardUserLog {
    private int _id;
    private int _userId;
    private BoardLog _boardLog;
    private String _userName="";

    public BoardLog getBoardLog() {
        return _boardLog;
    }

    public void setBoardLog(BoardLog _boardLog) {
        this._boardLog = _boardLog;
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public int getUserId() {
        return _userId;
    }

    public void setUserId(int _userId) {
        this._userId = _userId;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String _userName) {
        this._userName = _userName;
    }

    @Override
    public String toString() {
        return "BoardUserLog{" + "_id=" + _id + ", _userId=" + _userId + ", _userName=" + _userName + '}';
    }
}
