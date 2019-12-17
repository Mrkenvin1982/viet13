/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.device;

import game.vn.util.CompareStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class chua thong tin version cua tung client version.
 *
 * @author kuluan
 */
public class Version implements Comparable<Version> {
    transient Logger logger = LoggerFactory.getLogger(Version.class);

    public static final Version UNKNOWN_VERSION = new Version("0.0.0");

    /**
     * String of version from client.
     */
    private final String versionName;
    /**
     * dung de parse version ra 3 phan.
     */
    private Integer version, versionSub1, versionSub2;

    /**
     * Constructor new Verion. it must have a verions String.
     *
     * @param version Name of this version
     */
    public Version(String version) {
        this.versionName = version;
        parseVersion(versionName);
    }

    public String getVersionName() {
        return versionName;
    }

    private void parseVersion(String versionName) {
        version = versionSub1 = versionSub2 = 0;
        try {
            // version la so dau tien
            String[] versions = versionName.split("\\.");
            if (versions.length > 0) {
                try {
                    version = Integer.parseInt(versions[0]);
                } catch (Exception e) {
                    logger.error(versionName, e);
                }
                // bo dau cham ke tiep di la subversion 1
                if (versions.length > 1) {
                    try {
                        versionSub1 = Integer.parseInt(versions[1]);
                    } catch (Exception e) {
                        logger.error(versionName, e);
                    }
                    if (versions.length > 2) {
                        // bo tiep dau cham nua la subversion 2
                        try {
                            versionSub2 = Integer.parseInt(versions[2]);
                        } catch (Exception e) {
                            logger.error(versionName, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("parseVersion got exception :", e);
        }
    }

    /**
     * 
     * @return true if version number > 0
     */
    public boolean isValidVersion() {
        return version > 0 && versionSub1 >= 0 && versionSub2 >= 0;
    }

    public int compareTo(Version o) {
        int compareValue = this.version.compareTo(o.version);
        if (compareValue == CompareStatus.EQUAL) {
            compareValue = this.versionSub1.compareTo(o.versionSub1);
            if (compareValue == CompareStatus.EQUAL) {
                compareValue = this.versionSub2.compareTo(o.versionSub2);
            }
        }
        return compareValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Version other = (Version) obj;
        if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) {
            return false;
        }
        if (this.versionSub1 != other.versionSub1 && (this.versionSub1 == null || !this.versionSub1.equals(other.versionSub1))) {
            return false;
        }
        if (this.versionSub2 != other.versionSub2 && (this.versionSub2 == null || !this.versionSub2.equals(other.versionSub2))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 83 * hash + (this.versionSub1 != null ? this.versionSub1.hashCode() : 0);
        hash = 83 * hash + (this.versionSub2 != null ? this.versionSub2.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return versionName;
    }
}
