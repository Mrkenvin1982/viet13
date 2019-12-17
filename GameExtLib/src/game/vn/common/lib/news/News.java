/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.news;

/**
 *
 * @author hanv
 */
public class News {

    public static byte CATEGORY_PROMOTON = 0;
    public static byte CATEGORY_SYSTEM = 1;
    public static byte CATEGORY_TOURNAMENT = 2;

    private int id;
    private String title;
    private String content;
    private NewsButton button1;
    private NewsButton button2;
    private String image;
    private String imageLarge;
    private String icon;
    private byte category;
    private long startTime;
    private long endTime;
    private boolean popup;
    private String creator;
    private String lang;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NewsButton getButton1() {
        return button1;
    }

    public void setButton1(NewsButton button) {
        this.button1 = button;
    }

    public NewsButton getButton2() {
        return button2;
    }

    public void setButton2(NewsButton button) {
        this.button2 = button;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageLarge() {
        return imageLarge;
    }

    public void setImageLarge(String imageLarge) {
        this.imageLarge = imageLarge;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public byte getCategory() {
        return category;
    }

    public void setCategory(byte category) {
        this.category = category;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isPopup() {
        return popup;
    }

    public void setPopup(boolean popup) {
        this.popup = popup;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
