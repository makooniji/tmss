package cn.iocoder.yudao.report.pipeline;

import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;

import java.util.List;

public interface StatPipelineHandler {
    /**
     * 执行具体的管道数据转换和落库
     *
     * @param tenantMessages 当前租户下的消息列表
     */
    void handle(List<MerchantStaticDTO> tenantMessages);
}