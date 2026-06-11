package cn.iocoder.yudao.module.system.model.api.push;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BaseDSRequest {

    /**
     * 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。
     */
    private String traceId;
    /**
     * 在运营商系统中的用户名
     */
    private String username;
    /**
     * 用于标识此交易的唯一ID。
     */
    private String transactionId;
    /**
     * 由游戏供应商提供的外部交易ID。
     */
    private String externalTransactionId;

    private Long timestamp;

    private String currency;


    private String gameCode;

    /**
     * 变动金额
     */
    private BigDecimal creditAmount;

    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String roundId;

}
