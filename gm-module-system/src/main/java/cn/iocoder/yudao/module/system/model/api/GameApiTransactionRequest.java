package cn.iocoder.yudao.module.system.model.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameApiTransactionRequest {

    /**
     * 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。
     */
    @NotEmpty
    private String traceId;

    /**
     * 游戏应该打开的选择语言。
     *
     * 默认：'en'
     */
    @NotNull
    private Long fromTime;

    @NotNull
    private Long toTime;

    /**
     * ISO-4217货币代码（例如，USD）
     */
    @NotNull
    private Integer pageNo;

    @NotNull
    private Integer pageSize;
}
