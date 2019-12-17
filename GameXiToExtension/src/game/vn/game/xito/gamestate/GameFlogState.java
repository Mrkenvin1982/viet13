/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.gamestate;

import game.vn.game.xito.XiToController;

/**
 *
 * @author tuanp
 */
public class GameFlogState implements IXiToGameState{
        /**
     * Tổng cộng 4 vòng chia bài: 1 vòng đầu 2 lá và 3 vòng 1 lá. Hết vòng chia
     * bài thì tố lại từ đầu
     */
    private final static byte MAX_ROUND = 4;
    private final XiToController xiToGame;

    public GameFlogState(XiToController xiToGame) {
        this.xiToGame = xiToGame;
    }

    @Override
    public void preFlog() throws Exception {
    }
}
