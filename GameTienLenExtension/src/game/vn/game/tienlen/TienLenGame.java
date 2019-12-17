/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlen;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author hoanghh
 */
public class TienLenGame extends GameExtension {

    public TienLenGame() {
        super(ExtensionConstant.TLMN_GROUP_NAME,
                ExtensionConstant.TLMN_EXT_ID,
                ExtensionConstant.TLMN_EXT_CLASS);
//        this.setReloadMode(ExtensionReloadMode.AUTO);
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
        gameController = new TienLenController(getParentRoom(), this);
    }
}
