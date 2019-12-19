/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package botlogin;

import service.BotManager;
import service.QueueService;
import util.Configs;

/**
 *
 * @author hanv
 */
public class BotLogin {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        if (Configs.getInstance().queueEnable()) {
            QueueService.getInstance().init();
        }
        BotManager.getInstance().start();
    }

}