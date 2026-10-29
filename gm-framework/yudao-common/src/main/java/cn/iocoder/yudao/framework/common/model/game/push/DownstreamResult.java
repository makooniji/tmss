package cn.iocoder.yudao.framework.common.model.game.push;


import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 下游下发结果模型
 */
@Data
public class DownstreamResult {

    @Data
    public static class BaseData {
        private String username;
        private String currency;
        private BigDecimal balance;
        private Long timestamp;
        private Map<String,Object> otherData;

    }
    private String traceId;

    private String status;
    private BaseData data;

    public static DownstreamResult buildEmpty(){
        DownstreamResult result=new DownstreamResult();
        BaseData baseData = new BaseData();
        baseData.setBalance(BigDecimal.ZERO);
        result.setData(baseData);
        result.setStatus("OK");
       return result;

    }

    public static DownstreamResult error(String traceId) {
        DownstreamResult result = new DownstreamResult();
        result.setStatus(PlatformCodeEnum.DOWN_STREAM_ERROR.getCode());
        result.setTraceId(traceId);
        return result;

    }
}


