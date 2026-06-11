package cn.iocoder.yudao.module.system.model.api.push;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 加款转账：完成投注结算并更新用户余额操作
 */
@Data
public class DSCredit extends BaseDSRequest {

    private String roundId;
    /**
     * 状态指示此请求是否为先前扣款交易的退款请求。
     *
     * true (1)：表示这是针对之前扣款的退款请求。
     *
     * false (0)：表示这不是退款请求。
     */
    private Integer isRefund;
    /**
     * 从用户钱包中扣除的金额
     */
    private BigDecimal amount;
    /**
     *下注交易的金额。
     */
    private BigDecimal betAmount;
    /**
     * 赢取的金额。
     */
    private BigDecimal winAmount;
    /**
     *
     有效投注额的金额。
     */
    private BigDecimal effectiveTurnover;
    /**
     * 绝对赢取或亏损的金额
     */
    private BigDecimal winLoss;
    /**
     *
     奖池金额，当jackpotAmount金额>0时需要钱包贷记操作。
     */
    private BigDecimal jackpotAmount;
    /**
     * 此交易的初始请求的Unix时间戳（毫秒）。
     */
    private Long betTime;
    /**
     * 下注结算的Unix时间戳（毫秒）此交易。
     */
    private Long settledTime;

    private String betId;

    public static DSCredit of(String s) {
        return JsonUtils.parseObject(s, DSCredit.class);
    }
}
