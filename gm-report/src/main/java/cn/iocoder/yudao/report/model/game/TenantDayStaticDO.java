package cn.iocoder.yudao.report.model.game;

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
 * 租户游戏日报 DO
 *
 * @author osca
 */
@TableName("game_tenant_day_static")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TenantDayStaticDO implements Serializable {


    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;
    /**
     * 时间维度
     */
    private String days;
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 租户名称
     */
    private String tenantName;
    /**
     * 厂商编码
     */
    private String vendorCode;
    /**
     * 厂商名称
     */
    private String vendorName;


    /**
     * 游戏编码
     */
    private String gameCode;
    /**
     * 游戏名称
     */
    private String gameName;
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
    private BigDecimal winLoss = BigDecimal.ZERO;
    /**
     * 中奖概率
     */
    private BigDecimal rtp = BigDecimal.ZERO;
    /**
     * 投注人数
     */
    private Integer userBetCount = 0;
    /**
     * 盈利人数
     */
    private Integer userWinCount = 0;
    /**
     * 参与人数
     */
    private Integer userCount = 0;

    /**
     * 租户标识
     */
    private Long tenantId;

}
