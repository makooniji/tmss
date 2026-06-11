package cn.iocoder.yudao.report.pipeline;

import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import cn.iocoder.yudao.report.convert.StaticConvert;
import cn.iocoder.yudao.report.mapper.SystemMessageFlowMapper;
import cn.iocoder.yudao.report.model.SystemMessageFlowDo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SystemMessageFlowPipeline implements StatPipelineHandler {

    private final SystemMessageFlowMapper flowMapper;

    @Override
    public void handle(List<MerchantStaticDTO> tenantMessages) {
        List<SystemMessageFlowDo> messageFlow = StaticConvert.INSTANCE.toMessageFLow(tenantMessages);
        flowMapper.insertBatch(messageFlow);
    }
}