package cn.iocoder.yudao.module.system.model.api.push;

import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import lombok.Data;

import java.math.BigDecimal;

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


