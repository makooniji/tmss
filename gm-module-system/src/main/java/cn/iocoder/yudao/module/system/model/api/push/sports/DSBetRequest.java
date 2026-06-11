package cn.iocoder.yudao.module.system.model.api.push.sports;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.model.api.push.BaseDSRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 下游下注推送模型
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DSBetRequest extends BaseDSRequest {

    private String betId;
    private String roundId;
    /**
     * 体育特有
     */
    private BigDecimal betAmount;
    /**
     * 游戏特有
     */
    private BigDecimal amount;
    /**
     *1 单式投注 2串关投注
     */
    private Integer betType;

    /**
     * 赔率类型
     * 0 : 特殊赔率（或平台自定义）
     * 1 : 马来盘
     * 2 : 中国盘（大陆盘）
     * 3 : 欧洲盘（十进制盘）
     * 4 : 印尼盘
     * 5 : 美式盘
     * 6 : 欧赔（欧式盘）
     * 7 : 香港盘
     * 999 : 未知赔率类型
     */
    private String oddsType;
    private BigDecimal odds;
    private Long timestamp;

    private String betContent;

    /**
     * 一个包含多个投注记录的数组。数组中的每个对象应具有以下属性：
     *
     * betId：投注的唯一标识符。
     *
     * betAmount：相应投注IDbetId所需扣除的金额。
     */
    private List<MultipleItem> multipleBetIds;

    public static DSBetRequest of(String s) {
        return JsonUtils.parseObject(s, DSBetRequest.class);
    }

    @Data
    public static class MultipleItem{
        private String betId;
        private BigDecimal betAmount=BigDecimal.ZERO;
        private String betContent;
        private BigDecimal odds= BigDecimal.ZERO;
    }

}
