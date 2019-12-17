/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.blackjack;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author tuanp
 */
public class BlackJackGame extends GameExtension{

    public BlackJackGame() {
        super(ExtensionConstant.BLACKJACK_GROUP_NAME, ExtensionConstant.BLACKJACK_EXT_ID, ExtensionConstant.BLACKJACK_EXT_CLASS);
    }

    @Override
    public void init() {
        super.init();
    }
    @Override
    public void destroy() {
        trace("destroy!!!!");
        super.destroy();
    }

    @Override
    protected void initGameController() {
        gameController = new BlackJackGameController(getParentRoom(), this);
    }   
}
