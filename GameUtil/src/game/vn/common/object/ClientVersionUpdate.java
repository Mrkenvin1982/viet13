/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.object;

/**
 *
 * @author minhvtd
 */
public class ClientVersionUpdate {
    public static final int ALLOW = 0;
    public static final int WARNING = 1;
    public static final int BLOCK = 2;
    
    
    private String platform;
    private String bundleId;
    private String versions;
    private int action;
    private String link;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "ClientVersionUpdate{" + "platform=" + platform + ", bundleId=" + bundleId + ", versions=" + versions + ", action=" + action + ", link=" + link + '}';
    }
}
