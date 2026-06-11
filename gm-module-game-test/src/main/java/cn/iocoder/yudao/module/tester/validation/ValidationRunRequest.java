package cn.iocoder.yudao.module.tester.validation;

import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 单元测试入参模型
 */
@Data
public class ValidationRunRequest {
    /**
     * 游戏编码
     */
    private String gameCode;
    /**
     * 货币
     */
    private String currency;
    /**
     * 游戏账号
     */
    private String username;

    private String groupCode;

    @JsonIgnore
    private TenantDO tenantDO;

    @JsonIgnore
    private Long userId;
}
