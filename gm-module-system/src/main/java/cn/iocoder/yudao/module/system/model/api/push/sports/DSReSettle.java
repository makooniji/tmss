package cn.iocoder.yudao.module.system.model.api.push.sports;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.system.model.api.push.BaseDSRequest;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
public class DSReSettle extends BaseDSRequest {

    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String betId;
    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String roundId;
    /**
     * 下注交易的金额
     */
    private BigDecimal betAmount;
    /**
     * 赢取的金额，当winAmount金额>0时需要钱包添加金额操作
     */
    private BigDecimal winAmount;
    /**
     * 此新下注交易的更新赢取金额。
     */
    private BigDecimal newWinAmount;

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
    /**
     * 存入金额：当creditAmount大于0时，运营商需要向用户的钱包添加该creditAmount金额。
     */
    private BigDecimal creditAmount;

    /**
     * 在WooAPI集成系统中选定游戏的游戏代码。
     */
    private String gameCode;
    private String currency;
    /**
     *此交易的Unix时间戳（毫秒）。
     */
    private Long timestamp;
}
