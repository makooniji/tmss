package cn.iocoder.yudao.report.convert;


import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.kafka.StaticDTO;
import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import cn.iocoder.yudao.module.system.model.CategoryDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.report.controller.api.dto.CategoryRespDto;
import cn.iocoder.yudao.report.controller.api.dto.VendorRespDto;
import cn.iocoder.yudao.report.model.SystemMessageFlowDo;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import cn.iocoder.yudao.report.model.merchant.MerchantUserDayStaticDO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaticConvert {

    StaticConvert INSTANCE = Mappers.getMapper(StaticConvert.class);

    cn.iocoder.yudao.framework.mq.kafka.StaticDTO convert(TenantDayStaticDO tenantDayStaticDO);

    cn.iocoder.yudao.framework.mq.kafka.StaticDTO convert(TenantUserDayStaticDO userDayStaticDO);


    TenantDayStaticDO convertTenantDO(GameStaticDTO staticDTO);

    TenantUserDayStaticDO convertUserDO(GameStaticDTO staticDTO);

    List<TenantDayStaticDO> convertTenantDO(List<GameStaticDTO> staticDTO);

    List<TenantUserDayStaticDO> convertUserDO(List<GameStaticDTO> staticDTO);

    List<MerchantUserDayStaticDO> toMerchantUser(List<MerchantStaticDTO> staticDTO);

    List<SystemMessageFlowDo> toMessageFLow(List<GameStaticDTO> staticDTO);
    SystemMessageFlowDo toMessageFLow(GameStaticDTO staticDTO);

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


    @Mapping(source = "vendorName", target = "title")
    @Mapping(source = "vendorCode", target = "code")
    VendorRespDto toSupport(GameVendorDO bean);

    List<VendorRespDto> toSupportList(List<GameVendorDO> list);

    CategoryRespDto convert(CategoryDO bean);

    List<CategoryRespDto> convertList(List<CategoryDO> list);

}
