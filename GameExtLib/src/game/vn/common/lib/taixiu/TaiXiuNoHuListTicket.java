/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.taixiu;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hanv
 */
public class TaiXiuNoHuListTicket {
    private final List tickets = new ArrayList();

    public void addTicket(byte dice1, byte dice2, byte dice3) {
        tickets.add(new byte[]{dice1, dice2, dice3});
    }

    public List getListTicket() {
        return tickets;
    }
}
