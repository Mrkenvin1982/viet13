/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util.watchservice;

import java.io.FileOutputStream;

/**
 *
 * @author tuanp
 */
public class UpdateConfigFile {
    
    private String fileConfig;
    public UpdateConfigFile(String fileConfig){
        this.fileConfig=fileConfig;
    }
 
    /**
     * cập nhật lại file review
     *
     * @param data
     */
    public void updateFileReview(String data) throws Exception {
        byte[] mdata = data.getBytes("UTF-8");
        try (FileOutputStream out = new FileOutputStream(fileConfig)) {
            out.write(mdata);
        }
    }
}
