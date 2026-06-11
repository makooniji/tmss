package cn.iocoder.yudao.module.system.dal.dataobject.vendoruser;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 厂商玩家 DO
 *
 * @author osca
 */
@TableName("game_vendor_user_balance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorUserBalanceDO implements Serializable {

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 货币
     */
    private String currency;

    /**
     * 用户余额
     */
    private BigDecimal balance;
    /**
     * 租户ID
     */
    private Long tenantId;
}
