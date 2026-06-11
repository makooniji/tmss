package cn.iocoder.yudao.report.convert;


import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.kafka.StaticDTO;
import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import cn.iocoder.yudao.report.model.SystemMessageFlowDo;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import cn.iocoder.yudao.report.model.merchant.MerchantUserDayStaticDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaticConvert {

    StaticConvert INSTANCE = Mappers.getMapper(StaticConvert.class);

    cn.iocoder.yudao.framework.mq.kafka.StaticDTO convert(TenantDayStaticDO tenantDayStaticDO);

    cn.iocoder.yudao.framework.mq.kafka.StaticDTO convert(TenantUserDayStaticDO userDayStaticDO);


    TenantDayStaticDO convertTenantDO(StaticDTO staticDTO);

    TenantUserDayStaticDO convertUserDO(StaticDTO staticDTO);

    List<TenantDayStaticDO> convertTenantDO(List<StaticDTO> staticDTO);

    List<TenantUserDayStaticDO> convertUserDO(List<StaticDTO> staticDTO);

    List<MerchantUserDayStaticDO> toMerchantUser(List<MerchantStaticDTO> staticDTO);

    List<SystemMessageFlowDo> toMessageFLow(List<MerchantStaticDTO> staticDTO);

    // 声明单体转换
    SystemMessageFlowDo toMessageFLowSingle(MerchantStaticDTO src);

    /**
     * 在 MapStruct 映射完常规字段后，自动触发此方法
     *
     * @MappingTarget 代表已经转换好的目标对象
     * src 代表原来的源对象
     */
    @AfterMapping
    default void encryptOrConvertJson(MerchantStaticDTO src, @MappingTarget SystemMessageFlowDo target) {
        if (src != null && target != null) {
            // 直接把整个源对象序列化后塞给 target 的 content
            target.setContent(JsonUtils.toJsonString(src));
        }
    }
}
