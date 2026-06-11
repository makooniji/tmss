package cn.iocoder.yudao.module.system.model.api;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameApiRequest {
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
     * 游戏应该打开的选择语言。
     *
     * 默认：'en'
     */
    @NotEmpty
    private String language;
    /**
     * 游戏将在其上打开的平台。
     *
     * 可能的值：
     *
     * 1. web (默认)
     *
     * 2. H5
     */
    @NotEmpty
    private String platform="web";
    /**
     * ISO-4217货币代码（例如，USD）
     */
    @NotEmpty
    private String currency;

    /**
     * 运营商网站URL，以将用户带回游戏大厅。
     */
    @NotEmpty
    private String lobbyUrl;
    /**
     * 用户所在地的IP地址，可以是IPv4或IPv6格式。
     */
    @NotEmpty
    private String ipAddress;

    /**
     * 厂商带入金额,免转为0
     */
    private BigDecimal balance;

}
