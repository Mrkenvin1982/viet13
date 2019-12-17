/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 *
 * @author anlh
 */
public class VerifyAccessTokenResponse {
    
//    //Dịch vụ đang gặp gián đoạn. Vui lòng quay lại sau
//    public static final int SERVICE_ERROR = 500;
//    //Dịch vụ đang gặp gián đoạn. Vui lòng quay lại sau
//    public static final int INVALID_KEY = 407;
//    //Dịch vụ đang gặp gián đoạn. Vui lòng quay lại sau
//    public static final int VERIFY_SUCCESS = 15000;
//    //Dịch vụ đang gặp gián đoạn. Vui lòng quay lại sau
//    public static final int VERIFY_FAILED = 15001;
    
    private int code;
    private VerifyResponseData data;

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the data
     */
    public VerifyResponseData getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(VerifyResponseData data) {
        this.data = data;
    }

}
