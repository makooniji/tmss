package cn.iocoder.yudao.module.system.model.api.push;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
public class DSAdjustment  extends BaseDSRequest{
    /**
     * 用于将所有下注和赢取分组在单个回合中的游戏回合ID。
     */
    private String roundId;
    /**
     * 此交易需调整的金额：
     *
     * 正数（增加余额）
     *
     * 负数（减少余额）
     */
    private BigDecimal amount;

    public static DSAdjustment of(String s) {
        return JsonUtils.parseObject(s, DSAdjustment.class);
    }
}
