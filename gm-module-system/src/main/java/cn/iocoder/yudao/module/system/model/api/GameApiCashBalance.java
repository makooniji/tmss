package cn.iocoder.yudao.module.system.model.api;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class GameApiCashBalance {
    /**
     * 运营商系统中用户的用户名。
     */
    @NotEmpty
    private String username;
    /**
     * 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。
     */
    @NotEmpty
    private String traceId;
    /**
     * ISO-4217货币代码（例如，USD）
     */
    @NotEmpty
    private String currency;

}
