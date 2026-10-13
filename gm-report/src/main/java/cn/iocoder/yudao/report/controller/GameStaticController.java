package cn.iocoder.yudao.report.controller;

import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantSubScope;
import cn.iocoder.yudao.report.controller.vo.*;
import cn.iocoder.yudao.report.controller.vo.excel.VendorDayStaticRespExcelVO;
import cn.iocoder.yudao.report.controller.vo.excel.VendorTenantDayStaticRespExcelVO;
import cn.iocoder.yudao.report.framework.tenant.AdminReportTenantHelper;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
import com.fhs.core.trans.anno.TransMethodResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租户游戏报表")
@RestController
@RequestMapping("/game/tenant-static")
@Validated
@TenantSubScope
public class GameStaticController {

    @Resource
    private TenantDayStaticService tenantDayStaticService;

    @GetMapping("/page")
    @Operation(summary = "游戏报表分页")
    @TransMethodResult
    public CommonResult<PageResult<TenantDayStaticRespVO>> getTenantDayStaticPage(@Valid TenantDayStaticPageReqVO pageReqVO) {
        return success( tenantDayStaticService.getTenantGameStaticPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出游戏报表 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTenantDayStaticExcel(@Valid TenantDayStaticPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TenantDayStaticRespVO> list = tenantDayStaticService.getTenantGameStaticPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "游戏数据报表.xlsx", "数据", TenantDayStaticRespVO.class,
                list);
    }


    @GetMapping("/total")
    @Operation(summary = "游戏总计数据")
    public CommonResult<TenantStaticTotalVO> getStaticTotal(@Valid TenantDayStaticReqVO pageReqVO) {
        return success( tenantDayStaticService.getTenantStaticTotal(pageReqVO));
    }

    @GetMapping("/vendorTotal")
    @Operation(summary = "厂商总计数据")
    public CommonResult<VendorStaticTotalVO> getStaticVendorTotal(@Valid TenantDayStaticReqVO pageReqVO) {
        VendorStaticTotalVO result = tenantDayStaticService.getStaticVendorTotal(pageReqVO);
        return success(result);
    }

    @GetMapping("/vendor/page")
    @Operation(summary = "厂商报表分页")
    public CommonResult<PageResult<VendorDayStaticRespVO>> getVendorStaticPage(@Valid TenantDayStaticPageReqVO pageReqVO) {
        return success(tenantDayStaticService.getVendorStaticPage(pageReqVO));
    }

    @GetMapping("/vendor/export-excel")
    @Operation(summary = "导出厂商报表 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorStaticExcel(@Valid TenantDayStaticPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VendorDayStaticRespVO> list = tenantDayStaticService.getVendorStaticPage(pageReqVO).getList();
        ExcelUtils.write(response, "厂商报表数据.xlsx", "数据", VendorDayStaticRespExcelVO.class,
                BeanUtils.toBean(list, VendorDayStaticRespExcelVO.class));
    }

    @GetMapping("/vendor/tenant/page")
    @Operation(summary = "厂商商户报表分页（二级）")
    public CommonResult<PageResult<VendorTenantDayStaticRespVO>> getVendorTenantStaticPage(@Valid TenantDayStaticPageReqVO pageReqVO) {
        return success(tenantDayStaticService.getVendorTenantStaticPage(pageReqVO));
    }

    @GetMapping("/vendor/tenant/export-excel")
    @Operation(summary = "导出厂商商户报表 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorTenantStaticExcel(@Valid TenantDayStaticPageReqVO pageReqVO,
                                              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VendorTenantDayStaticRespVO> list = tenantDayStaticService.getVendorTenantStaticPage(pageReqVO).getList();
        ExcelUtils.write(response, "厂商商户报表数据.xlsx", "数据", VendorTenantDayStaticRespExcelVO.class,
                BeanUtils.toBean(list, VendorTenantDayStaticRespExcelVO.class));
    }

    @GetMapping("/vendor/tenant/total")
    @Operation(summary = "厂商商户报表总计（二级）")
    public CommonResult<TenantStaticTotalVO> getVendorTenantStaticTotal(@Valid TenantDayStaticPageReqVO pageReqVO) {
        return success(tenantDayStaticService.getVendorTenantStaticTotal(pageReqVO));
    }

    /**
     * 游戏数据总览- 方块统计
     * @param pageReqVO
     * @return
     */
    @GetMapping("/vendor/total")
    @Operation(summary = "厂商报表总计")
    public CommonResult<TenantStaticTotalVO> getVendorStaticTotal(@Valid TenantDayStaticPageReqVO pageReqVO) {
        return success( tenantDayStaticService.getVendorStaticTotal(pageReqVO));
    }

    @GetMapping("/gameTotal")
    @Operation(summary = "指定游戏数据")
    public CommonResult<TenantGameStaticTotalVO> getStaticGameTotal(@Valid TenantDayStaticReqVO pageReqVO) {
        TenantGameStaticTotalVO result = tenantDayStaticService.getStaticGameTotal(pageReqVO);
        return success(result);
    }

    /**
     * 游戏柱状图- chartDayInfo
     */
    @GetMapping("/chartGameTotal")
    public CommonResult<Object> chartGameTotal(@Valid TenantChartsReqVO pageReqVO) {
        var staticChart = tenantDayStaticService.getChartGameTotal(pageReqVO);
        return success(staticChart);
    }

    /**
     * 游戏曲线图-chartDayTotal
     *
     * @param pageReqVO
     * @return
     */
    @GetMapping("/chartGameDayTotal")
    public CommonResult<Object> chartGameDayTotal(@Valid TenantChartsReqVO pageReqVO) {
        var staticChart = tenantDayStaticService.getChartGameDayTotal(pageReqVO);
        return success(staticChart);
    }


}