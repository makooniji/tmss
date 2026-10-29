package cn.iocoder.yudao.report.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.report.controller.api.dto.*;
import cn.iocoder.yudao.report.controller.vo.*;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 租户游戏日报 Service 接口
 *
 * @author osca
 */
public interface TenantDayStaticService {

    /**
     * 创建租户游戏日报
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTenantDayStatic(@Valid TenantDayStaticSaveReqVO createReqVO);

    /**
     * 更新租户游戏日报
     *
     * @param updateReqVO 更新信息
     */
    void updateTenantDayStatic(@Valid TenantDayStaticSaveReqVO updateReqVO);

    /**
     * 删除租户游戏日报
     *
     * @param id 编号
     */
    void deleteTenantDayStatic(Long id);

    /**
     * 批量删除租户游戏日报
     *
     * @param ids 编号
     */
    void deleteTenantDayStaticListByIds(List<Long> ids);

    /**
     * 获得租户游戏日报
     *
     * @param id 编号
     * @return 租户游戏日报
     */
    TenantDayStaticDO getTenantDayStatic(Long id);

    /**
     * 获得租户报表分页
     *
     * @param pageReqVO 分页查询
     * @return 租户游戏日报分页
     */
    PageResult<TenantDayStaticRespVO> getTenantStaticPage(TenantDayStaticPageReqVO pageReqVO);

    /**
     * 获得厂商维度报表分页
     *
     * @param pageReqVO 分页查询
     * @return 厂商报表分页
     */
    PageResult<VendorDayStaticRespVO> getVendorStaticPage(TenantDayStaticPageReqVO pageReqVO);

    List<VendorDayStaticRespVO> getVendorStatic(TenantDayStaticPageReqVO pageReqVO);

    /**
     * 获得厂商 + 商户维度报表分页（筛选条件与厂商分页一致）
     *
     * @param pageReqVO 分页查询
     * @return 厂商商户报表分页
     */
    PageResult<VendorTenantDayStaticRespVO> getVendorTenantStaticPage(TenantDayStaticPageReqVO pageReqVO);

    /**
     * 获得厂商 + 商户维度报表总计（筛选条件与厂商商户分页一致）
     *
     * @param pageReqVO 查询条件
     * @return 厂商商户报表总计
     */
    TenantStaticTotalVO getVendorTenantStaticTotal(TenantDayStaticPageReqVO pageReqVO);

    /**
     * 获得厂商维度报表总计（筛选条件与厂商分页一致）
     *
     * @param pageReqVO 查询条件
     * @return 厂商报表总计
     */
    TenantStaticTotalVO getVendorStaticTotal(TenantDayStaticPageReqVO pageReqVO);

    /**
     * 查询数据总计
     *
     * @param pageReqVO
     * @return
     */
    GameStaticTotalVO getGameStaticTotal(@Valid TenantDayStaticReqVO pageReqVO);


    /**
     * 获得游戏报表分页
     *
     * @param pageReqVO 分页查询
     * @return 租户游戏日报分页
     */
    PageResult<TenantDayStaticRespVO> getTenantGameStaticPage(TenantDayStaticPageReqVO pageReqVO);

    /**
     * 查询数据总计
     *
     * @param pageReqVO
     * @return
     */
    TenantStaticTotalVO getTenantStaticTotal(@Valid TenantDayStaticReqVO pageReqVO);

    Map<String, Object> getStaticChart(@Valid TenantDayStaticReqVO pageReqVO);

    List<Map<String, Object>> getStaticDayInfo(@Valid TenantChartsReqVO pageReqVO);
    List<Map<String, Object>> getStaticTenantDaysInfo(@Valid TenantChartsReqVO pageReqVO);

    List<Map<String, Object>> getStaticDayTotal(@Valid TenantChartsReqVO pageReqVO);

    VendorStaticTotalVO getStaticVendorTotal(@Valid TenantDayStaticReqVO pageReqVO);

    TenantGameStaticTotalVO getStaticGameTotal(@Valid TenantDayStaticReqVO pageReqVO);

    TenantStatusCountVO getTenantStatus();

    PageResult<UserStaticRespVO> getUserStaticPage(@Valid UserStaticPageReqVO pageReqVO);

    List<Map<String, Object>> getChartGameTotal(@Valid TenantChartsReqVO pageReqVO);

    List<Map<String, Object>> getChartGameDayTotal(@Valid TenantChartsReqVO pageReqVO);

    PageResult<TenantStaticRespDto> getSupportMerchantStaticPage(@Valid MerchantPageReqDto pageReqVO);
    PageResult<MerchantRespDto> getSupportMerchantInfoPage(@Valid MerchantPageReqDto pageReqVO);

    PageResult<VendorStaticRespDto> getSupportVendorStaticPage(@Valid VendorPageReqDto pageReqVO);

    MerchantTotalRespDto getSupportMerchantTotal(Integer tid);

    List<VendorTotalRespDto> getSupportVendorTotal(VendorTotalReqDto pageReqVO);

    List<VendorTotalRespDto> getVendorMerchantTotal(VendorTotalReqDto pageReqVO);

    List<String> getCurrencies(String start, String end,Integer tid,String merchantCode);
}