package cn.iocoder.yudao.framework.mq.kafka;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;

import cn.iocoder.yudao.framework.mq.enums.MessageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 通道报表模型
 *
 * @author osca
 */
@Data
public class StaticDTO implements Serializable {


    /**
     * 主键
     */
    private Long id;

    public Long getId() {
        return IdUtil.getSnowflakeNextId();
    }

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
    @Deprecated(since = "使用flow查询人数")
    private Integer userBetCount = 0;
    /**
     * 盈利人数
     */
    @Deprecated(since = "使用flow查询人数")
    private Integer userWinCount = 0;
    /**
     * 参与人数
     */
    @Deprecated(since = "使用flow查询人数")
    private Integer userCount = 0;

    /**
     * 租户标识
     */
    private Long tenantId;


    private Long userId;
    private String username;

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType = MessageTypeEnum.UNKNOW;

    public static final StatProducer statProducer = SpringUtils.getBean(StatProducer.class);

}
