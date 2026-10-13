package cn.iocoder.yudao.report.excute.pipeline;

import cn.iocoder.yudao.framework.mq.enums.MessageTypeEnum;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.report.convert.StaticConvert;
import cn.iocoder.yudao.report.mapper.TenantDayStaticMapper;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TenantDayStaticPipeline extends AbstractStatPipelineHandler {

    private final TenantDayStaticMapper mapper;
    private final Set<MessageTypeEnum> includes= EnumSet.of(MessageTypeEnum.REGISTER);

    @Override
    protected boolean support(GameStaticDTO dto) {
        return !includes.contains( dto.getMessageType());
    }

    @Override
    protected void doHandle(List<GameStaticDTO> tenantMessages) {
        List<TenantDayStaticDO> tenantDayStaticDOS = StaticConvert.INSTANCE.convertTenantDO(tenantMessages);
        mapper.batchUpsert(tenantDayStaticDOS);
    }
}