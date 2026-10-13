package cn.iocoder.yudao.module.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.ibatis.type.EnumTypeHandler;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 厂商玩家资金流水 DO
 *
 * @author osca
 */
@TableName(value = "game_vendor_user_flow", autoResultMap = true)
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorUserFlowDO implements Serializable {

    @Getter
    @AllArgsConstructor
    public enum FlowStatusEnum {

        SUCCESS,
        FAIL


    }

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

    @TableField(typeHandler = EnumTypeHandler.class)
    private FlowStatusEnum status;
    private String remark;


}
