/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.lieng;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author tuanp
 */
public class LiengGame extends GameExtension{

    public LiengGame() {
        super(ExtensionConstant.LIENG_GROUP_NAME, ExtensionConstant.LIENG_EXT_ID, ExtensionConstant.LIENG_EXT_CLASS);
    }

    @Override
    public void init() {
//        trace("init LiengExtension...");
        super.init();
//        trace("init LiengExtension... DONE");
    }

    @Override
    protected void initGameController() {
        gameController = new LiengController(getParentRoom(), this);
    }
}
