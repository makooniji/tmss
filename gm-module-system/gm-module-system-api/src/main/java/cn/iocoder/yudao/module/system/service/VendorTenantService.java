package cn.iocoder.yudao.module.system.service;

import java.util.*;

import cn.iocoder.yudao.framework.common.model.game.GameApiVendorRequest;
import cn.iocoder.yudao.module.system.model.vo.tenant.GameRespVO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantPageReqVO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantRespVO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantSaveReqVO;
import cn.iocoder.yudao.module.system.model.GameInfoDO;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;

import jakarta.validation.*;
import cn.iocoder.yudao.module.system.model.VendorTenantDO;
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
     * 填充厂商大类 code（来自 game_vendor）
     */
    void fillCategoryCode(List<VendorTenantRespVO> list);

    /**
     * 获得当前租户自身的厂商分页（不含子级租户数据）
     *
     * @param pageReqVO 分页查询
     * @return 租户厂商分页
     */
    PageResult<VendorTenantDO> getTenantVendorPage(VendorTenantPageReqVO pageReqVO);

    /**
     * 根据场馆查询商户对应场馆信息
     *
     * @param vendor
     * @param tenantId
     * @return
     */
    VendorTenantDO getVendorByCode(String vendor,Long tenantId);

    VendorTenantDO getVendorByTenantCode(String agent);

    /**
     * 查询租户厂商
     * @return
     */
    List<GameVendorDO> getListVendors(GameApiVendorRequest gameApiVendorRequest);

    void copyToTenant(List<String> vendorCodes,TenantDO tenantId);

    GameRespVO getTenantGameConfig(Long tenantId);

    Map<GameVendorDO, List<GameInfoDO>> getVendorGames();

    List<String> getSupportVendors();
}