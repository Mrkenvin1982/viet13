
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.maubinh;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author minhhnb
 */
public class MauBinhGame extends GameExtension {


    public MauBinhGame() {
        super(ExtensionConstant.MAUBINH_GROUP_NAME,
              ExtensionConstant.MAUBINH_EXT_ID,
              ExtensionConstant.MAUBINH_EXT_CLASS);
    }

    @Override
    public void init() {
//        trace("init MauBinhExtension...");
        super.init();
//        trace("init MauBinhExtension... DONE");
    }

    @Override
    public void destroy() {
        trace("destroy!!!!");
        super.destroy();
    }

    @Override
    protected void initGameController() {
        gameController = new MauBinhGameController(getParentRoom(), this);
    }
}