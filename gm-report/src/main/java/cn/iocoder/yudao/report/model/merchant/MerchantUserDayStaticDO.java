package cn.iocoder.yudao.report.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商户会员日统计数据
 *
 * @author osca
 */
@TableName("merchant_user_day_static")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MerchantUserDayStaticDO implements Serializable {


    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;
    /**
     * 时间维度
     */
    private String days;

    private Long userId;
    private String username;

    /**
     * 投注次数
     */
    private Integer betCount = 0;
    /**
     * 投注金额
     */
    private BigDecimal betAmount = BigDecimal.ZERO;
    /**
     * 派彩金额
     */
    private BigDecimal payAmount = BigDecimal.ZERO;

    /**
     * 输赢金额
     */
    private BigDecimal winLoss;
    /**
     * 中奖概率
     */
    private BigDecimal rtp = BigDecimal.ZERO;

    /**
     * 租户标识
     */
    private Long tenantId;

    private String agentCode;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;
    /**
     * 提现金额
     */
    private BigDecimal withdrawAmount;
    /**
     * 充值次数
     */
    private Long rechargeCount;
    /**
     * 提现次数
     */
    private Long withdrawCount;
    /**
     * 活动奖金
     */
    private BigDecimal bonus;

    /**
     * 新增会员数量
     */
    private Long newUserCount;
    /**
     * 活跃会员数据
     */
    private Long activeUserCount;
    /**
     * 有效会员数量
     */
    private Long validUserCount;

}
