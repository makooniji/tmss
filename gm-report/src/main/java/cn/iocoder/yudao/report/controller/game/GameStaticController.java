package cn.iocoder.yudao.report.controller.game;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.report.controller.game.vo.*;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租户游戏报表")
@RestController
@RequestMapping("/game/tenant-static")
@Validated
public class GameStaticController {

    @Resource
    private TenantDayStaticService tenantDayStaticService;

    @GetMapping("/page")
    @Operation(summary = "游戏报表分页")
    public CommonResult<PageResult<TenantDayStaticRespVO>> getTenantDayStaticPage(@Valid TenantDayStaticPageReqVO pageReqVO) {
        PageResult<TenantDayStaticRespVO> pageResult = tenantDayStaticService.getTenantGameStaticPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出游戏报表 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTenantDayStaticExcel(@Valid TenantDayStaticPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TenantDayStaticRespVO> list = tenantDayStaticService.getTenantGameStaticPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "游戏数据报表.xls", "数据", TenantDayStaticRespVO.class,
                        list);
    }


    @GetMapping("/total")
    @Operation(summary = "游戏总计数据")
    public CommonResult<TenantStaticTotalVO> getStaticTotal(@Valid TenantDayStaticReqVO pageReqVO) {
        TenantStaticTotalVO result = tenantDayStaticService.getTenantStaticTotal(pageReqVO);
        return success(result);
    }

    @GetMapping("/vendorTotal")
    @Operation(summary = "厂商总计数据")
    public CommonResult<VendorStaticTotalVO> getStaticVendorTotal(@Valid TenantDayStaticReqVO pageReqVO) {
        VendorStaticTotalVO result = tenantDayStaticService.getStaticVendorTotal(pageReqVO);
        return success(result);
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