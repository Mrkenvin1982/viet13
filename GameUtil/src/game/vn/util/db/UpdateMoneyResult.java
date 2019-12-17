package game.vn.util.db;

import java.math.BigDecimal;

/**
 * Ket qua tra ve khi call procedure update_money3
 * @author minhhnb
 */
public class UpdateMoneyResult {
    /**
     * so win truoc khi update
     */
    public BigDecimal before = BigDecimal.ZERO;
    /**
     * so win sau khi update: after = before khi co loi xay ra
     */
    public BigDecimal after = BigDecimal.ZERO;
    /**
     * Tiền tẩy hiện có trong bàn
     */
    public BigDecimal stack = BigDecimal.ZERO;
}
