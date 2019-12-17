/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.phom;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author tuanp
 */
public class PhomGame extends GameExtension{

     public PhomGame() {
        super(ExtensionConstant.PHOM_GROUP_NAME, ExtensionConstant.PHOM_EXT_ID, ExtensionConstant.PHOM_EXT_CLASS);
    }

    @Override
    public void init() {
//        trace("init PhomExtension...");
        super.init();
//        trace("init PhomExtension... DONE");
    }

    @Override
    protected void initGameController() {
        gameController = new PhomController(getParentRoom(), this);
    }
    @Override
    public void destroy() {
        trace("destroy!!!!");
        super.destroy();
    }

}
