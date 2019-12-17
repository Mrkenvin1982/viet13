/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.game.xito.gamestate;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import game.vn.game.xito.XiToConfig;
import game.vn.game.xito.XiToController;
import game.vn.game.xito.object.XiToCard;
import game.vn.game.xito.object.XiToPlayer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tuanp
 */
public class GamePreFlogState implements IXiToGameState {

    private final XiToController xiToGame;

    public GamePreFlogState(XiToController xiToGame) {
        this.xiToGame = xiToGame;
    }

    /**
     * Kiểm tra player chọn bài mở trong vòng preflog
     * <br />
     * Nếu chưa thì chọn bài cho player
     *
     */
    private void processShowCard() {
        for (User user : xiToGame.getPlayers()) {
            XiToPlayer xitoPlayer = xiToGame.getXiToPlayer(xiToGame.getIdDBOfUser(user));
            if(xitoPlayer==null){
                continue;
            }
            if (!xiToGame.isInturn(xitoPlayer.getUser()) && !xitoPlayer.isAllIn()) {
                continue;
            }
            if (xitoPlayer.getShowedCard() == null) {
                //loc qua 2 la bai dang giu
                for (XiToCard c : xitoPlayer.getHoldCards().values()) {
                    //set 1 la show, 1 la hide
                    if (xitoPlayer.getShowedCard() == null) {
                        xitoPlayer.setShowedCard(c);
                    } else {
                        c.setIsHideCard(true);
                        xitoPlayer.setHideCard(c);
                    }
                }
            }
        }
    }

    /**
     * tìm người tố đầu tiên, là người show lá bài lớn nhất trong ván
     *
     *
     * @return
     */
    private XiToPlayer determineBeginner() {
        XiToPlayer beginner = null;
        for (User user : xiToGame.getInTurnPlayers()) {
            XiToPlayer xitoPlayer = xiToGame.getXiToPlayer(xiToGame.getIdDBOfUser(user));
            if(xitoPlayer==null){
                continue;
            }
            if (xitoPlayer.getStack().compareTo(xiToGame.getMoney()) < 0) {
                continue;
            }
            if (beginner == null) {
                beginner = xitoPlayer;
                continue;
            }
            //chon nguoi co show card lon nhat
            if (xitoPlayer.getShowedCard().getId() > beginner.getShowedCard().getId()) {
                beginner = xitoPlayer;
            }
        }
        return beginner;
    }

    /**
     * trường hợp mới bắt đầu ván mà chỉ có 1 thằng hoặc ko còn ai đủ stack để
     * chơi tiếp
     * <br />
     * chia đủ bài và kết thúc ván
     *
     * @throws Exception
     */
    private void checkToStopGame() throws Exception {
        if (xiToGame.canStop()) {
            //còn lại 3 round chia đủ rồi stop game
            for (int i = 0; i < 3; i++) {
                Map<String, Byte> map = new HashMap<>();
                for (User user : xiToGame.getPlayers()) {
                    String idDB = xiToGame.getIdDBOfUser(user);
                    XiToPlayer xitoPlayer = xiToGame.getXiToPlayer(idDB);
                    if(xitoPlayer==null){
                        continue;
                    }
                    //chia bai
                    XiToCard card = xiToGame.getDeskXiTo().dealCard();
                    xitoPlayer.addCard(card);
                    map.put(idDB, card.getId());
                }
                //gui bai ve cho client
                SFSObject flogMessage = xiToGame.getMessageFactory().getFlopMessage(map);
                xiToGame.sendAllUserMessage(flogMessage);
            }
            xiToGame.stopGame();
            return;
        }
        xiToGame.setCurrentMoveTime();
        xiToGame.setStateGame(xiToGame.getWaittingGameState());
    }

    @Override
    public void preFlog() throws Exception {
//        kiem tra nguoi choi nao chua chon card, thi chon 1 la cho no
        processShowCard();
        //gui message show one card cho ca ban
        SFSObject m = xiToGame.getMessageFactory().getShowOneCardMessage();
        xiToGame.sendAllUserMessage(m);
        //end show card
        //sau khi bắt đầu và ko đủ stack để chơi tiếp
        for (User user : xiToGame.getInTurnPlayers()) {
             XiToPlayer xitoPlayer = xiToGame.getXiToPlayer(xiToGame.getIdDBOfUser(user));
            if (xitoPlayer.getStack().signum() == 0) {
                xiToGame.setInturn(user, false);
                xitoPlayer.setState(xitoPlayer.getAllInState());
            }
        }

        //chon nguoi bat dau van choi
        XiToPlayer beginner = determineBeginner();
        //set cho no la current player
        xiToGame.setCurrentPlayer(beginner.getUser());
        beginner.setDefaultAction();
        //gui message next-turn cho ca ban
        beginner.sendNextTurnMessage();
        // chuyen trang thai sang vong flog
        xiToGame.setStateXiTo(xiToGame.getFlogState());
        xiToGame.setStateWating();

        //trường hợp mới bắt đầu ván mà chỉ có 1 thằng hoặc ko còn ai đủ stack để chơi tiếp
        //=>chia đủ bài và kết thúc ván
        checkToStopGame();
    }
}
