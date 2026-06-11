package cn.iocoder.yudao.module.system.model.api.push.sports;

import cn.iocoder.yudao.module.system.model.api.push.BaseDSRequest;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认下注模型
 */
@Data
public class DSUpdateBet extends BaseDSRequest {

    private String betId;
    private String roundId;
    private BigDecimal betAmount;
    private BigDecimal newBetAmount;
    private BigDecimal creditAmount;


    private String gameCode;
    private String currency;
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



}
