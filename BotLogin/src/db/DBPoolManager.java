/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import snaq.db.ConnectionPoolManager;

/**
 *
 * @author hanv
 */
public class DBPoolManager {
    private ConnectionPoolManager poolManager;
    private final Logger log = LoggerFactory.getLogger(Database.class);
    private static final String POOL_NAME = "casino";
    private static final String API_POOL_NAME = "casino_api";
    public static final int DEFAULT_CONNECTION_TIME_OUT = 1000;
    private static final DBPoolManager INSTANCE = new DBPoolManager();

    public DBPoolManager() {
        try {
            poolManager = ConnectionPoolManager.getInstance(new File("conf/jdbc.properties"));
        } catch (IOException ex) {
            log.error("error init pool manager", ex);
        }
    }

    public static DBPoolManager getInstance() {
        return INSTANCE;
    }

    public ConnectionPoolManager getPoolManager() {
        return poolManager;
    }

    /**
     * 
     * @return
     * @throws SQLException 
     */
    public Connection getConnection() throws SQLException {
        return INSTANCE.getPoolManager().getConnection(POOL_NAME, DEFAULT_CONNECTION_TIME_OUT);
    }
    
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public Connection getApiConnection() throws SQLException {
        return INSTANCE.getPoolManager().getConnection(API_POOL_NAME, DEFAULT_CONNECTION_TIME_OUT);
    }

    public void releaseAll() {
        log.info("release all connections db");
        INSTANCE.getPoolManager().getPool(POOL_NAME).release();
        INSTANCE.getPoolManager().getPool(API_POOL_NAME).release();
    }
}