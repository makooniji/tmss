package cn.iocoder.yudao.module.system.model.api.push.sports;

import cn.iocoder.yudao.module.system.model.api.push.BaseDSRequest;
import lombok.Data;

/**
 * 下游下注推送模型
 */
@Data
public class DSUnSettle extends BaseDSRequest {

    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String betId;
    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String roundId;

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
