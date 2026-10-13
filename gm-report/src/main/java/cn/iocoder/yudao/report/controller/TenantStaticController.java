package cn.iocoder.yudao.report.controller;

import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantSubScope;
import cn.iocoder.yudao.report.controller.vo.*;
import cn.iocoder.yudao.report.controller.vo.excel.TenantDayStaticRespExcelVO;
import cn.iocoder.yudao.report.framework.tenant.AdminReportTenantHelper;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


/**
 * 管理后台 - 商户数据报表
 */

@Tag(name = "管理后台 - 商户数据报表")
@RestController
@RequestMapping("/game/tenant-day-static")
@Validated
@TenantSubScope
public class TenantStaticController {

    @Resource
    private TenantDayStaticService tenantDayStaticService;

    @GetMapping("/page")
    @Operation(summary = "商户报表分页")
    public CommonResult<PageResult<TenantDayStaticRespVO>> getTenantDayStaticPage(@Valid TenantDayStaticPageReqVO pageReqVO) {
        return success(tenantDayStaticService.getTenantStaticPage(pageReqVO));
    }


    @GetMapping("/pageUser")
    @Operation(summary = "会员统计分页")
    public CommonResult<PageResult<UserStaticRespVO>> getUserStaticPage(@Valid UserStaticPageReqVO pageReqVO) {
        PageResult<UserStaticRespVO> pageResult = tenantDayStaticService.getUserStaticPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商户总览报表报表 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTenantDayStaticExcel(@Valid TenantDayStaticPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TenantDayStaticRespVO> list = tenantDayStaticService.getTenantStaticPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "商户总览报表数据.xlsx", "数据", TenantDayStaticRespExcelVO.class,
                BeanUtils.toBean(list, TenantDayStaticRespExcelVO.class));
    }


    /**
     * 商户数据总计
     *
     * @param pageReqVO
     * @return
     */
    @GetMapping("/total")
    public CommonResult<GameStaticTotalVO> getStaticTotal(@Valid TenantDayStaticReqVO pageReqVO) {
        GameStaticTotalVO result = tenantDayStaticService.getGameStaticTotal(pageReqVO);
        return success(result);
    }

    /**
     * 商户总览- 图表数据
     */
    @GetMapping("/chartTenant")
    public CommonResult<Object> chartTenant(@Valid TenantDayStaticReqVO pageReqVO) {
        Map<String, Object> staticChart = tenantDayStaticService.getStaticChart(pageReqVO);
        return success(staticChart);
    }


    /**
     * 商户总览- chartDayInfo
     */
    @GetMapping("/chartDayInfo")
    public CommonResult<Object> chartDayInfo(@Valid TenantChartsReqVO pageReqVO) {
        var staticChart = tenantDayStaticService.getStaticDayInfo(pageReqVO);
        return success(staticChart);
    }
    /**
     * 商户总览- chartOpenDays
     */
    @GetMapping("/tenantOpenDays")
    public CommonResult<Object> tenantOpenDays(@Valid TenantChartsReqVO pageReqVO) {
        var staticChart = tenantDayStaticService.getStaticTenantDaysInfo(pageReqVO);
        return success(staticChart);
    }

    /**
     * 商户总览-chartDayTotal
     *
     * @param pageReqVO
     * @return
     */
    @GetMapping("/chartDayTotal")
    public CommonResult<Object> chartDayTotal(@Valid TenantChartsReqVO pageReqVO) {
        var staticChart = tenantDayStaticService.getStaticDayTotal(pageReqVO);
        return success(staticChart);
    }

    /**
     * 商户状态统计
     */
    @GetMapping("/tenantStatus")
    @Operation(summary = "商户状态统计")
    public CommonResult<TenantStatusCountVO> tenantStatus() {
        return success(tenantDayStaticService.getTenantStatus());
    }

}