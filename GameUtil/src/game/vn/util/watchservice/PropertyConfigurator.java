/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.util.watchservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xử dụng để load file config
 *
 * @author tuanp
 */
public class PropertyConfigurator extends WatchServiceConfig {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(PropertyConfigurator.class);

    private Properties properties = new Properties();
    
    private static final String STRING_EMPTY = "";
    private static final int UNKNOWN_VALUE = Integer.MIN_VALUE;
    //tên file
    private String fileName = "";
    //đường dẫn thu mục file
    private String pathDirectory = "";
    PropertiesConfiguration config ;
    private String text;

    public PropertyConfigurator(String path, String nameFile) {
        super(path);
        this.pathDirectory = path;
        this.fileName = nameFile;
        this.doChanged();
        try {
            config = new PropertiesConfiguration(path+nameFile);
        } catch (ConfigurationException ex) {
            LOGGER.error("load ConfigurationException error: ", ex);
        }

    }

    /**
     * Load configuration from file.
     *
     * @param path Full file name.
     * @return 
     */
    public Properties loadConfigFile(String path) {
        Properties returnProperties = new Properties();
        try {
            try (FileInputStream inputStream = new FileInputStream(path)) {
                returnProperties.load(inputStream);
            }
        } catch (IOException ex) {
            LOGGER.error("loadConfigFile error: ", ex);
        }
        return returnProperties;
    }

    @Override
    protected void doChanged() {
            this.properties = loadConfigFile(this.pathDirectory + this.fileName);
            this.text = loadTextFile(this.pathDirectory + this.fileName, Charset.forName("UTF-8"));
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

    public double getFloatAttribute(String attributeName, float defaultValue) {
        String value = getStringAttribute(attributeName);
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
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
    public double getDoubleAttribute(String attributeName, double defaultValue) {
        String value = getStringAttribute(attributeName);
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
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
    
    public boolean getBooleanAttribute(String attributeName, boolean defaultValue) {
        String value = getStringAttribute(attributeName);
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
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
        Date date = null;
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
        Date date = null;
        try {
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
            date = formatter.parse(value);
        } catch (ParseException e) {
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
        } catch (NumberFormatException e) {
            return UNKNOWN_VALUE;
        }
    }
    public long getLongAttribute(String attributeName, long defaultValue) {
        String value = getStringAttribute(attributeName);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get Attribute as Byte.
     *
     * @param attributeName
     * @return Byte value or {@link Byte#MIN_VALUE} if unparsable
     */
    public byte getByteAttribute(String attributeName) {
        String value = getStringAttribute(attributeName);
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            return Byte.MIN_VALUE;
        }
    }
    
    /**
     * 
     * @param attributeName
     * @param defaultValue
     * @return 
     */
    public byte getByteAttribute(String attributeName, byte defaultValue) {
        String value = getStringAttribute(attributeName);
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public void reloadConfig(){
        try {
            config = new PropertiesConfiguration(this.pathDirectory+this.fileName);
        } catch (ConfigurationException ex) {
            LOGGER.error("load ConfigurationException error: ", ex);
        }
    }
    
    public void updateProperties(String key, String value){
        this.config.setProperty(key, value);
    }
    
    public void save() {
            try {
                this.config.save();
            } catch (ConfigurationException ex) {
                LOGGER.error("save error: ", ex);
            }
    }
    
    /**
     * Load file text
     *
     * @param path
     * @param encoding
     * @return
     */
    private String loadTextFile(String path, Charset encoding) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(path));
                return encoding.decode(ByteBuffer.wrap(encoded)).toString();
            } catch (IOException e) {
                LOGGER.error("updateProperties error: ", e);
            }
            return "";

    }
    
    protected  String getText(){
        return text;
    }
}