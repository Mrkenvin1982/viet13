/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

/**
 *
 * @author hanv
 */
public class APIResult {
    public static final int CODE_SUCESS = 1;
    public static final int CODE_ERROR = 0;
    
    public static final String MSG_SUCCESS = "Cập nhật thành công";
    public static final String MSG_ERROR = "Có lỗi xảy ra!";
    
    private int code = CODE_SUCESS;
    private String message = MSG_SUCCESS;
    private Object data;

    public APIResult() {
    }

    public APIResult(int code) {
        this.code = code;
    }

    public APIResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
