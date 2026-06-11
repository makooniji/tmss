package cn.iocoder.yudao.report.pipeline;

import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import cn.iocoder.yudao.report.convert.StaticConvert;
import cn.iocoder.yudao.report.mapper.MerchantUserDayStaticMapper;
import cn.iocoder.yudao.report.model.merchant.MerchantUserDayStaticDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MerchantUserDayStaticPipeline implements StatPipelineHandler {

    private final MerchantUserDayStaticMapper mapper;

    @Override
    public void handle(List<MerchantStaticDTO> tenantMessages) {
        List<MerchantUserDayStaticDO> tenantDayStaticDOS = StaticConvert.INSTANCE.toMerchantUser(tenantMessages);
        mapper.batchUpsert(tenantDayStaticDOS);
    }
}