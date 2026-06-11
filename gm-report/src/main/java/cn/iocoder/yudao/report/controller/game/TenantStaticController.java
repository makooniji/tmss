package cn.iocoder.yudao.report.controller.game;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.report.controller.game.vo.*;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


/**
 * 管理后台 - 商户数据报表
 */

@Tag(name = "管理后台 - 商户数据报表")
@RestController
@RequestMapping("/game/tenant-day-static")
@Validated
public class TenantStaticController {

    @Resource
    private TenantDayStaticService tenantDayStaticService;

    @GetMapping("/page")
    @Operation(summary = "商户报表分页")
    @PreAuthorize("@ss.hasPermission('game:tenant-day-static:query')")
    public CommonResult<PageResult<TenantDayStaticRespVO>> getTenantDayStaticPage(@Valid TenantDayStaticPageReqVO pageReqVO) {
        PageResult<TenantDayStaticRespVO> pageResult = tenantDayStaticService.getTenantStaticPage(pageReqVO);
        return success(pageResult);
    }


    @GetMapping("/pageUser")
    @Operation(summary = "会员统计分页")
    @PreAuthorize("@ss.hasPermission('game:tenant-day-static:query')")
    public CommonResult<PageResult<UserStaticRespVO>> getUserStaticPage(@Valid UserStaticPageReqVO pageReqVO) {
        PageResult<UserStaticRespVO> pageResult = tenantDayStaticService.getUserStaticPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商户报表 Excel")
    @PreAuthorize("@ss.hasPermission('game:tenant-day-static:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTenantDayStaticExcel(@Valid TenantDayStaticPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TenantDayStaticRespVO> list = tenantDayStaticService.getTenantStaticPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "商户报表数据.xls", "数据", TenantDayStaticRespVO.class,
                        BeanUtils.toBean(list, TenantDayStaticRespVO.class));
    }


    /**
     * 商户数据总计
     * @param pageReqVO
     * @return
     */
    @GetMapping("/total")
    @PreAuthorize("@ss.hasPermission('game:tenant-day-static:query')")
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
     * 商户总览-chartDayTotal
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
     * @return
     */
    @GetMapping("/tenantStatus")
    public CommonResult<TenantStatusCountVO> tenantStatus() {
        var staticChart = tenantDayStaticService.getTenantStatus();
        return success(staticChart);
    }







}