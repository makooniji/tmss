package cn.iocoder.yudao.module.system.model.api.push;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认下注模型
 */
@Data
public class DSRollback extends BaseDSRequest {

    private String betId;
    private String roundId;
    @JsonIgnore
    private BigDecimal creditAmount;

    public static DSRollback of(String s) {
        return JsonUtils.parseObject(s, DSRollback.class);
    }
}
