/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.tienlentour;

import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;

/**
 *
 * @author TuanP
 */
public class TienLenTourGame extends GameExtension {

    public TienLenTourGame() {
        super(ExtensionConstant.TL_TOUR_GROUP_NAME,
                ExtensionConstant.TL_TOUR_EXT_ID,
                ExtensionConstant.TL_TOUR_EXT_CLASS);
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
        gameController = new TienLenTourController(getParentRoom(), this);
    }
}
