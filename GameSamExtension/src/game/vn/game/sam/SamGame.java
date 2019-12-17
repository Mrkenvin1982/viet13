/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.sam;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author tuanp
 */
public class SamGame extends GameExtension{

    public SamGame() {
         super(ExtensionConstant.SAM_GROUP_NAME, ExtensionConstant.SAM_EXT_ID, ExtensionConstant.SAM_EXT_CLASS);
    }

    @Override
    public void init() {
//        trace("init SamExtension...");
        super.init();
//        trace("init SamExtension... DONE");
    }

    
    @Override
    protected void initGameController() {
        gameController = new SamController(getParentRoom(), this);
    }
    
}
