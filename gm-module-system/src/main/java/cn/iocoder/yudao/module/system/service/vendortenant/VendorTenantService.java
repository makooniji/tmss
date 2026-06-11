package cn.iocoder.yudao.module.system.service.vendortenant;

import java.util.*;

import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.GameRespVO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.model.api.GameApiVendorRequest;
import jakarta.validation.*;
import cn.iocoder.yudao.module.system.dal.dataobject.vendortenant.VendorTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 租户厂商 Service 接口
 *
 * @author osca
 */
public interface VendorTenantService {

    /**
     * 创建租户厂商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVendorTenant(@Valid VendorTenantSaveReqVO createReqVO);

    /**
     * 更新租户厂商
     *
     * @param updateReqVO 更新信息
     */
    void updateVendorTenant(@Valid VendorTenantSaveReqVO updateReqVO);

    /**
     * 删除租户厂商
     *
     * @param id 编号
     */
    void deleteVendorTenant(Long id);

    /**
    * 批量删除租户厂商
    *
    * @param ids 编号
    */
    void deleteVendorTenantListByIds(List<Long> ids);

    /**
     * 获得租户厂商
     *
     * @param id 编号
     * @return 租户厂商
     */
    VendorTenantDO getVendorTenant(Long id);

    /**
     * 获得租户厂商分页
     *
     * @param pageReqVO 分页查询
     * @return 租户厂商分页
     */
    PageResult<VendorTenantDO> getVendorTenantPage(VendorTenantPageReqVO pageReqVO);

    /**
     * 根据场馆查询商户对应场馆信息
     * @param vendor
     * @return
     */
    VendorTenantDO getVendorByCode(String vendor);

    VendorTenantDO getVendorByTenantCode(String agent);

    /**
     * 查询租户厂商
     * @return
     */
    List<VendorTenantDO> getListVendors(GameApiVendorRequest gameApiVendorRequest);

    void copyToTenant(List<String> vendorCodes,TenantDO tenantId);

    GameRespVO getTenantGameConfig(Long tenantId);

    Map<GameVendorDO, List<GameInfoDO>> getVendorGames();
}