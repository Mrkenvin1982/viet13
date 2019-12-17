/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.vip;

import java.util.Date;

/**
 *
 * @author anlh
 */
public class VipInfoObj {


    //thứ tự sắp xếp
    private int index;
    //tên bậc
    private String rankName;
    //mô tả bậc
    private String rankDescription;
    //hình ảnh logo bậc
    private String imgUrl;

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the rankName
     */
    public String getRankName() {
        return rankName;
    }

    /**
     * @param rankName the rankName to set
     */
    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    /**
     * @return the rankDescription
     */
    public String getRankDescription() {
        return rankDescription;
    }

    /**
     * @param rankDescription the rankDescription to set
     */
    public void setRankDescription(String rankDescription) {
        this.rankDescription = rankDescription;
    }

    /**
     * @return the imgUrl
     */
    public String getImgUrl() {
        return imgUrl;
    }

    /**
     * @param imgUrl the imgUrl to set
     */
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    
}
