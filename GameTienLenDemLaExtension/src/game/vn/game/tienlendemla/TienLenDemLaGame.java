/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlendemla;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author hoanghh
 */
public class TienLenDemLaGame extends GameExtension {

    public TienLenDemLaGame() {
        super(ExtensionConstant.TLDL_GROUP_NAME,
                ExtensionConstant.TLDL_EXT_ID,
                ExtensionConstant.TLDL_EXT_CLASS);
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
        gameController = new TienLenDemLaController(getParentRoom(), this);
    }
}
