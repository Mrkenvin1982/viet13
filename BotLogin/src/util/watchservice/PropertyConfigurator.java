/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.watchservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Xử dụng để load file config
 *
 * @author tuanp
 */
public class PropertyConfigurator extends WatchServiceConfig {

    private Properties properties = new Properties();
    private static final String STRING_EMPTY = "";
    private static final int UNKNOWN_VALUE = Integer.MIN_VALUE;
    //tên file
    private String fileName = "";
    //đường dẫn thu mục file
    private String pathDirectory = "";

    public PropertyConfigurator(String path, String nameFile) {
        super(path);
        this.pathDirectory = path;
        this.fileName = nameFile;
        this.doChanged();
    }

    /**
     * Load configuration from file.
     *
     * @param path Full file name.
     * @return 
     */
    public Properties loadConfigFile(String path) {
        FileInputStream inputStream;
        Properties returnProperties = null;
        try {
            returnProperties = new Properties();
            inputStream = new FileInputStream(path);
            returnProperties.load(inputStream);
            inputStream.close();
        } catch (IOException ex) {
        }
        return returnProperties;
    }

    @Override
    protected void doChanged() {
        this.properties = loadConfigFile(this.pathDirectory + this.fileName);
    }

    @Override
    protected boolean isFileModified(String fileNameInput) {
        return this.fileName.equals(fileNameInput);
    }

    /**
     * Get Attribute as String.
     *
     * @param attributeName
     * @return String value or {@link PropertyConfigurator#STRING_EMPTY} if
     * unparsable
     */
    public String getStringAttribute(String attributeName) {
        if (attributeName == null) {
            return STRING_EMPTY;
        }
        return this.properties.getProperty(attributeName, STRING_EMPTY);
    }

    /**
     *
     * @param attributeName
     * @param defaultStr
     * @return
     */
    public String getStringAttribute(String attributeName, String defaultStr) {
        if (attributeName == null) {
            return STRING_EMPTY;
        }
        return this.properties.getProperty(attributeName, defaultStr);
    }

    /**
     * Get Attribute as Integer.
     *
     * @param attributeName
     * @return Integer value or {@link PropertyConfigurator#UNKNOWN_VALUE} if
     * unparsable
     */
    public int getIntAttribute(String attributeName) {
        return getIntAttribute(attributeName, UNKNOWN_VALUE);
    }

    public int getIntAttribute(String attributeName, int defaultValue) {
        String value = getStringAttribute(attributeName);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get Attribute as Float.
     *
     * @param attributeName
     * @return Float value or {@link PropertyConfigurator#UNKNOWN_VALUE} if
     * unparsable
     */
    public float getFloatAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Get Attribute as Double.
     *
     * @param attributeName
     * @return Double value or {@link PropertyConfigurator#UNKNOWN_VALUE} if
     * unparsable
     */
    public double getDoubleAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Get Attribute as Boolean.
     *
     * @param attributeName
     * @return Boolean value or false if unparsable
     */
    public boolean getBooleanAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get Attribute as Calendar from string date. Ex: 07-11-2007 format: dd -
     * MM - yyyy
     *
     * @param attributeName
     * @return Calendar value or current date if unparsable
     */
    public Calendar getDateAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        Calendar cal = Calendar.getInstance();
        Date date;
        try {
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            date = formatter.parse(value);
        } catch (Exception e) {
            date = new Date();
        }
        cal.setTime(date);
        return cal;
    }

    /**
     * Get Attribute as Calendar from string datetime. Ex: 12:45:03 07-01-2007
     * format: HH:mm:ss dd-MM-yyyy
     *
     * @param attributeName
     * @return Calendar value or current date if unparsable
     */
    public Calendar getDateTimeAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        Calendar cal = Calendar.getInstance();
        Date date;
        try {
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
            date = formatter.parse(value);
        } catch (Exception e) {
            date = new Date();
        }
        cal.setTime(date);
        return cal;
    }

    /**
     * Get Attribute as Long.
     *
     * @param attributeName
     * @return Long value or {@link PropertyConfigurator#UNKNOWN_VALUE} if
     * unparsable
     */
    public long getLongAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Get Attribute as Byte.
     *
     * @param attributeName
     * @return Byte value or {@link Byte#MIN_VALUE} if unparsable
     */
    public Byte getByteAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        try {
            return Byte.parseByte(value);
        } catch (Exception e) {
            return Byte.MIN_VALUE;
        }
    }
}
