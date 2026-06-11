package cn.iocoder.yudao.framework.mq.kafka.merchant;

import cn.iocoder.yudao.framework.mq.enums.MessageTypeEnum;
import cn.iocoder.yudao.framework.mq.kafka.StaticDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 通道报表模型
 *
 * @author osca
 */
@Data
public class MerchantStaticDTO extends StaticDTO {

    /**
     * 商户编码
     */
    private String merchantCode;

    /**
     * 代理ID
     */
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

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType;

    private BigDecimal bonus;
}
