package cn.iocoder.yudao.module.game.adapter.game.lottery.model;

import lombok.Data;

/**
 * @author osca
 * @email oosca666999@gmail.com
 * @date 2026-06-23 11:26:56
 */
@Data
public class LotteryReq {

    private String sign;
    private String merchantAccount;
    private Long ts;
    private String currency;
    private String gameCode;

    private PushDataDto data;
}
