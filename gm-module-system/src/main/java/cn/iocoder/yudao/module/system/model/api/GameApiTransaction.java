package cn.iocoder.yudao.module.system.model.api;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameApiTransaction {
    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID
     */
    private String betId;
    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID
     */
    private String roundId;
    /**
     * 由游戏供应商提供的外部交易ID
     */
    private String externalTransactionId;
    /**
     * 会员账号
     */
    private String username;
    /**
     * 在WooAPI集成系统中选定游戏的游戏代码。
     */
    private String gameCode;
    /**
     * 货币编码
     */
    private String currencyCode;
    /**
     * 场馆编码
     */
    private String vendorCode;
    /**
     * 投注内容
     */
    private String betContent;
    /**
     * 投注金额
     */
    private BigDecimal betAmount;
    /**
     * 派彩金额
     */
    private BigDecimal winAmount;
    /**
     * 输赢金额
     */
    private BigDecimal winLoss;
    /**
     * 有效投注额的金额。
     */
    private BigDecimal effectiveTurnover;
    /**
     * 奖池金额，当jackpotAmount金额>0时需要钱包贷记操作。
     */
    private BigDecimal jackpotAmount;
    /**
     * 结算状态
     */
    private Integer status;
    /**
     * 厂商下注时间
     */
    private Long vendorBetTime;
    /**
     * 厂商结算时间
     */
    private Long vendorSettleTime;
}
