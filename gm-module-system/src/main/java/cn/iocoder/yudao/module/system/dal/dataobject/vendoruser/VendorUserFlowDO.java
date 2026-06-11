package cn.iocoder.yudao.module.system.dal.dataobject.vendoruser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 厂商玩家资金流水 DO
 *
 * @author osca
 */
@TableName("game_vendor_user_flow")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorUserFlowDO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;



    /**
     * 账变金额
     */
    private BigDecimal amount;
    /**
     * 账变前金额
     */
    private BigDecimal beforeAmount;
    /**
     * 账变后金额
     */
    private BigDecimal afterAmount;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 货币
     */
    private String currency;

    private String referenceId;


}
