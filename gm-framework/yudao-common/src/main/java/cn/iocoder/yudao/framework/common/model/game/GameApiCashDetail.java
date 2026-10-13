package cn.iocoder.yudao.framework.common.model.game;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameApiCashDetail {
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



    /**
     * 由运营商系统为每个API请求生成的通用唯一标识符（UUID）。
     */
    @NotEmpty
    private String referenceId;

}
