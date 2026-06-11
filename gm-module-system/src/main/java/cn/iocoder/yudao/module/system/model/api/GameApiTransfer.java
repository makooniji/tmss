package cn.iocoder.yudao.module.system.model.api;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameApiTransfer {
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
     * 在集成系统中选定游戏的游戏代码。
     */
    @NotEmpty
    private String gameCode;

    /**
     * ISO-4217货币代码（例如，USD）
     */
    @NotEmpty
    private String currency;

    /**
     * 厂商带入金额
     */
    private BigDecimal amount;
    /**
     * 0转入 1转出
     */
    private Integer direction;

}
