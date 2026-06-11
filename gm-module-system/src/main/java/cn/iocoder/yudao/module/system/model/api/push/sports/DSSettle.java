package cn.iocoder.yudao.module.system.model.api.push.sports;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.system.model.api.push.BaseDSRequest;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
public class DSSettle extends BaseDSRequest {

    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String betId;

    /**
     * 下注交易的金额
     */
    private BigDecimal betAmount;
    /**
     * 赢取的金额，当winAmount金额>0时需要钱包添加金额操作
     */
    private BigDecimal winAmount;
    /**
     * 有效投注额的金额
     */
    private BigDecimal effectiveTurnover;


    /**
     * 输赢金额
     */
    private BigDecimal winLoss;

    public BigDecimal getWinLoss(){
        if(ObjectUtil.isEmpty(winAmount)||ObjectUtil.isEmpty(betAmount)){
            return BigDecimal.ZERO;
        }

        return winAmount.subtract(betAmount);
    }

    private BigDecimal creditAmount;

}
