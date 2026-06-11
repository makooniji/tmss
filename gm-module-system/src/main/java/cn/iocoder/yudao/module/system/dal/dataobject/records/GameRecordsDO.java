package cn.iocoder.yudao.module.system.dal.dataobject.records;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户游戏记录 DO
 *
 * @author osca
 */
@TableName("game_records")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordsDO  extends TenantBaseDO implements Serializable {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;

    private String username;
    /**
     * 状态
     *
     */
    private ActionEnum action;

    /**
     * 三方请求流水号
     */
    private String traceId;
    /**
     * 订单号
     */
    private String orderNo;


    /**
     * 三方订单号
     */
    private String refOrderNo;
    /**
     * 房间号
     */
    private String roomNo;
    /**
     * 局号期号
     */
    private String issueNo;
    /**
     * 下注金额
     */
    private BigDecimal betAmount=BigDecimal.ZERO;
    /**
     * 投注内容
     */
    private String betContent;
    /**
     * 派彩金额
     */
    private BigDecimal winAmount=BigDecimal.ZERO;

    /**
     * 输赢金额
     */
    private BigDecimal winLoss = BigDecimal.ZERO;
    public BigDecimal getWinLoss(){
        if (action == null) {
            return winLoss != null ? winLoss : BigDecimal.ZERO;
        }
        if (action.getType()==(ActionEnum.BET.getType())
                || action.getType()==(ActionEnum.REFUND.getType())) {
            return BigDecimal.ZERO;
        }
        return winAmount.compareTo(BigDecimal.ZERO) <= 0 ? betAmount.negate() : winAmount.subtract(betAmount);}

    /**
     * 拓展内容
     */
    private String extraContent;
    /**
     * 厂商编码
     */
    private String vendorCode;
    /**
     * 游戏编码
     */
    private String gameCode;
    /**
     * 游戏类型
     *
     */
    private Long gameKind;
    /**
     * 赔率信息
     */
    private BigDecimal odds=BigDecimal.ZERO;
    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 租户ID
     */
//    private Long tenantId;
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 货币
     */
    private String currency;
    /**
     * 奖金
     */
    private BigDecimal jackpotAmount;

    @TableField(select = false)
    private String creator;

    @TableField(select = false)
    private String updater;


}
