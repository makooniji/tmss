package cn.iocoder.yudao.module.system.model.api.push;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
public class DSWalletBetResult extends DSBalance {

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
    private BigDecimal betAmount;
    /**
     *赢取的金额，当winAmount金额>0时需要钱包贷记操作。
     */
    private BigDecimal winAmount;

    /**
     * 有效投注额的金额。
     */
    private BigDecimal effectiveTurnover;

    /**
     * 绝对赢取或亏损的金额
     */
    private BigDecimal winLoss;

    /**
     * 奖池金额，当jackpotAmount金额>0时需要钱包贷记操作。
     */
    private BigDecimal jackpotAmount;
    /**
     * 交易过程的类型
     *
     * 1）"WIN" - (玩家赢得一笔下注)
     *
     * 2）"BET_WIN" - (玩家下注并赢得)
     *
     * 3）"BET_LOSE" - (玩家下注并输掉)
     *
     * 4）"LOSE" - (玩家输掉一笔下注)
     *
     * 5）"END" - (通知运营商回合已结束，不需要钱包借记或贷记操作)
     */
    private Integer resultType;
    /**
     * 用于指示下注为免费旋转下注的状态。
     */
    private Boolean isFreespin=false;

    /**
     * 用于指示下注已完成的状态。
     */
    private Boolean isEndRound=false;

    /**
     * 在WooAPI集成系统中选定游戏的游戏代码。
     */
    private String gameCode;

    private Long betTime;
    private Long settledTime;
}
