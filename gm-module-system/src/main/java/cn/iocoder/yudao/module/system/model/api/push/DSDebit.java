package cn.iocoder.yudao.module.system.model.api.push;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
public class DSDebit extends BaseDSRequest {
    private String roundId;
    /**
     * 用于指示是全额扣款还是部分扣款状态：
     *
     * true (1)，则扣除用户钱包的全部余额 ，amount 会设置为 0。
     *
     * false (0), 则根据提供的 amount 扣除部分金额。
     */
    private Integer takeAll;
    /**
     * 从用户钱包中扣除的金额
     */
    private BigDecimal amount;

    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private BigDecimal effectiveTurnover;
    private BigDecimal winLoss;
    private BigDecimal jackpotAmount;


    private String betId;

    public static DSDebit of(String s) {
        return JsonUtils.parseObject(s, DSDebit.class);
    }
}
