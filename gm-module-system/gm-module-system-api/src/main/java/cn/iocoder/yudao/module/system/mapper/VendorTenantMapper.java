package cn.iocoder.yudao.module.system.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.model.VendorTenantDO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantPageReqVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户厂商 Mapper
 *
 * @author osca
 */
@Mapper
public interface VendorTenantMapper extends BaseMapperX<VendorTenantDO> {

    default PageResult<VendorTenantDO> selectPage(VendorTenantPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VendorTenantDO>()
                .eqIfPresent(VendorTenantDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(VendorTenantDO::getTenantCode, reqVO.getTenantCode())
                .eqIfPresent(VendorTenantDO::getTenantName, reqVO.getTenantName())
                .likeIfPresent(VendorTenantDO::getVendorName, reqVO.getVendorName())
                .eqIfPresent(VendorTenantDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(VendorTenantDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(VendorTenantDO::getId));
    }

    /**
     * 仅查询指定租户自身的厂商数据（不含子级租户范围）
     */
    default PageResult<VendorTenantDO> selectPageByTenantId(VendorTenantPageReqVO reqVO, Long tenantId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VendorTenantDO>()
                .eq(VendorTenantDO::getTenantId, tenantId)
                .eqIfPresent(VendorTenantDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(VendorTenantDO::getTenantCode, reqVO.getTenantCode())
                .eqIfPresent(VendorTenantDO::getTenantName, reqVO.getTenantName())
                .likeIfPresent(VendorTenantDO::getVendorName, reqVO.getVendorName())
                .eqIfPresent(VendorTenantDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(VendorTenantDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(VendorTenantDO::getId));
    }

}