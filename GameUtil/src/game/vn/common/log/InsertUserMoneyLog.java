/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.log;

import game.vn.common.config.QueueConfig;
import game.vn.common.config.ServerConfig;
import game.vn.common.lib.log.Invoices;
import game.vn.common.queue.QueueHistory;
import game.vn.common.queue.QueueServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class này xử lý gửi log ván chơi lên queue
 * @author tuanp
 */
public class InsertUserMoneyLog implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(InsertUserMoneyLog.class);
    Invoices invoices;

    public InsertUserMoneyLog(Invoices invoices) {
        this.invoices = invoices.clone();
    }

    @Override
    public void run() {
        if (ServerConfig.getInstance().isSendHistToTTKT()) {
            QueueServiceApi.getInstance().sendData(QueueConfig.getInstance().getKeyInvoice(), true, invoices);
        }
        if (ServerConfig.getInstance().isSendHistToGame()) {
           QueueHistory.instance().sendData(invoices); 
        }
        
    }
}
