/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.api;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hanv
 */
public class TransactionHistory {
    private final String userId;
    private final int page;
    private final List<Transaction> listTX = new ArrayList();

    public TransactionHistory(String userId, int page) {
        this.userId = userId;
        this.page = page;
    }

    public void addTransaction(Transaction transaction) {
        listTX.add(transaction);
    }

    public String getUserId() {
        return userId;
    }

    public int getPage() {
        return page;
    }

    public List<Transaction> getListTX() {
        return listTX;
    }
}
