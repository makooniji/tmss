package cn.iocoder.yudao.module.system.dal.mysql.vendortenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.vendortenant.VendorTenantDO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantPageReqVO;
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

}