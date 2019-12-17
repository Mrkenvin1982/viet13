/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import game.vn.common.config.ServerConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Quản lý tất cả các thread trong game
 * @author 
 */
public class ThreadPoolGame {
     private static final ThreadPoolGame INSTANCE = new ThreadPoolGame();
     
     //quản lý tất cả các thread của GameController
     private final ThreadPoolExecutor gameControllerPools;
     private ThreadPoolExecutor eventExecutor;
     private boolean isShutdown;
     
    public static ThreadPoolGame getPool() {
        return INSTANCE;
    }
    
    private ThreadPoolGame(){
        this.isShutdown = false;
        //board game
        this.gameControllerPools = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.gameControllerPools.setMaximumPoolSize(ServerConfig.getInstance().getGameControllerPollSize());
        
        eventExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("EventPools-%d").build());
        eventExecutor.setMaximumPoolSize(ServerConfig.getInstance().getEventPoolSize());
        eventExecutor.setKeepAliveTime(ServerConfig.getInstance().getAliveTimeThread(), TimeUnit.SECONDS);
        
    }
    
    public void shutDown() {
        this.isShutdown=true;
        this.gameControllerPools.shutdown();
        this.eventExecutor.shutdown();
    }
    
   @Override
    protected void finalize() throws Throwable {
        if (!this.isShutdown) {
            this.gameControllerPools.shutdown();
            this.eventExecutor.shutdown();
        }
        super.finalize();
    }
    /**
     * run 1 thread trong game controller
     * @param task 
     */
    public void executeGameController(Runnable task){
        this.gameControllerPools.execute(task);
    }
    
    public void executeEvent(Runnable task){
        eventExecutor.execute(task);
    }

    @Override
    public String toString() {
        return "THREAD gameControllerPools: getCorePoolSize= "+gameControllerPools.getPoolSize()+",getActiveCount="+gameControllerPools.getActiveCount()
                +",getMaximumPoolSize="+gameControllerPools.getMaximumPoolSize();
    }
    
}
