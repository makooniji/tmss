package cn.iocoder.yudao.module.system.model.api.push;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
public class DSWalletBet extends DSBalance {

    /**
     * 用于标识此交易的唯一ID。
     */
    private String transactionId;
    /**
     * 由游戏供应商提供的外部交易ID。
     */
    private String externalTransactionId;
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
    private BigDecimal amount;


    /**
     * 在WooAPI集成系统中选定游戏的游戏代码。
     */
    private String gameCode;
}
