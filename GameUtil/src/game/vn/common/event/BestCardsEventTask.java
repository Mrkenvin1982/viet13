/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.event;

import game.vn.common.lib.event.UserCardsObj;
import game.vn.common.queue.QueueServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event săn bài đẹp
 * @author
 */
public class BestCardsEventTask implements Runnable {

    protected static final Logger log = LoggerFactory.getLogger("BestCardsEventTask");
    private final UserCardsObj userCardsObj;
    
    public BestCardsEventTask(UserCardsObj userCardsObjInput){
        this.userCardsObj = userCardsObjInput;
    }
    @Override
    public void run() {
         Thread.currentThread().setName("BestCardsEventTask");
         try {
             QueueServiceEvent.getInstance().sendEventInfor(userCardsObj);
        } catch (Exception e) {
            log.error("run EventTask error", e);
        }        
    }
    
}
