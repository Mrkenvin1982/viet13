/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author tuanp
 */
public class XiToGame extends GameExtension{

    public XiToGame() {
        super(ExtensionConstant.XITO_GROUP_NAME, ExtensionConstant.XITO_EXT_ID, ExtensionConstant.XITO_EXT_CLASS);
    }
    @Override
    public void init() {
        super.init();
    }
    @Override
    protected void initGameController() {
        gameController = new XiToController(getParentRoom(), this);
    }
    
}
