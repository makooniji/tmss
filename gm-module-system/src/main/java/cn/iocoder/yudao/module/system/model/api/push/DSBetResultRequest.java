package cn.iocoder.yudao.module.system.model.api.push;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 下游下注推送模型
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DSBetResultRequest extends BaseDSRequest {

    private String betId;
    private String roundId;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
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


    private ActionEnum actionEnum;

    private BigDecimal winLoss;
    private BigDecimal jackpotAmount;
    private String resultType;

    public static DSBetResultRequest of(String json){
       return JsonUtils.parseObject(json, DSBetResultRequest.class);
    }

}
