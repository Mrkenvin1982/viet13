
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.baicao;

import com.smartfoxserver.v2.core.SFSEventType;
import game.vn.common.GameExtension;
import game.vn.common.constant.ExtensionConstant;
import game.vn.game.baicao.filter.CustomFilter;
import game.vn.game.baicao.handler.ReconnectHandler;
import game.vn.game.baicao.handler.ReconnectTryHandler;

/**
 *
 * @author minhhnb
 */
public class BaiCaoGame extends GameExtension {
    public BaiCaoGame() {
        super(ExtensionConstant.BAICAO_GROUP_NAME,
              ExtensionConstant.BAICAO_EXT_ID,
              ExtensionConstant.BAICAO_EXT_CLASS);
    }

    @Override
    public void init() {
//        trace("init BaiCaoExtension...");
        super.init();
//        trace("init BaiCaoExtension... DONE");
    }

    @Override
    public void destroy() {
        trace("destroy!!!!");
        super.destroy();
    }

    /**
     * init những event extension sẽ bắt và xử lý khi client gửi len
     */
    private void addEventHandler() {
        addEventHandler(SFSEventType.USER_RECONNECTION_SUCCESS, ReconnectHandler.class);
        addEventHandler(SFSEventType.USER_RECONNECTION_TRY, ReconnectTryHandler.class);
      
        addFilter("customFilter", new CustomFilter());
    }

    @Override
    protected void initGameController() {
        gameController = new BaiCaoController(getParentRoom(), this);
    }
}