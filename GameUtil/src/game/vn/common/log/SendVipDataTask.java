/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.log;

import game.vn.common.lib.vip.UserTaxData;
import game.vn.common.queue.QueueServiceVip;

/**
 * Class này xử lý gửi log ván chơi lên queue
 *
 * @author tuanp
 */
public class SendVipDataTask implements Runnable {

    private final UserTaxData data;

    public SendVipDataTask(UserTaxData data) {
        this.data = data;
    }

    @Override
    public void run() {
        QueueServiceVip.getInstance().sendVipData(data);
    }
}
