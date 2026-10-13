package cn.iocoder.yudao.module.game.adapter.game.lottery.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author osca
 * @email oosca666999@gmail.com
 * @date 2026-06-23 11:26:56
 */
@Data
public class PushDataDto {

    private String orderId;
    private BigDecimal winAmount;
    private BigDecimal betAmount;
    private String gameCode;
    private String memberAccount;
}
