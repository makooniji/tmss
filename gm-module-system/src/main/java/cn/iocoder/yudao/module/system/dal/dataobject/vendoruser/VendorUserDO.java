package cn.iocoder.yudao.module.system.dal.dataobject.vendoruser;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 厂商玩家 DO
 *
 * @author osca
 */
@TableName("game_vendor_user")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorUserDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 游戏厂商ID
     */
    private Long vendorId;
    /**
     * 游戏厂商编码
     */
    private String vendorCode;

    /**
     * 用户名 vendorChannel+username
     */
    private String username;

    /**
     * 上游厂商账号名
     */
    private String vendorUsername;

    /**
     * 状态: 0开启 1 关闭
     */
    private Integer status;
    /**
     * 租户ID
     */
    private Long tenantId;

    private String tenantCode;

    private String tenantName;

}
