package cn.iocoder.yudao.report.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantSubScope;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.TenantService;
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
import lombok.extern.slf4j.Slf4j;
import org.dromara.pdf.fop.handler.TemplateHandler;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租户游戏报表")
@RestController
@RequestMapping("/game/tenant-static")
@Validated
@TenantSubScope
@Slf4j
public class GameStaticController {

    @Resource
    private TenantDayStaticService tenantDayStaticService;
    @Resource
    private TenantService tenantService;

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

    @GetMapping("/vendor/export-pdf")
    @Operation(summary = "导出厂商报表 pdf")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorStaticPdf(@Valid TenantDayStaticPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {

        List<VendorDayStaticRespVO> list = tenantDayStaticService.getVendorStatic(pageReqVO);

        TenantDO tenantDO = TenantUtils.executeIgnore(() -> tenantService.getTenant(pageReqVO.getTenantId()));
        if(ObjectUtil.isEmpty(tenantDO)){
            response.reset();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().println("{\"code\":500,\"msg\":\"导出PDF失败\"}");
            return;
        }

        try {
            // 1. 使用 类路径（classpath）获取 resources 下的配置和模板目录
            String configPath = ResourceUtils.getFile("classpath:codegen/fop.xconf").getAbsolutePath();
            String templatePath = ResourceUtils.getFile("classpath:codegen/freemarker").getAbsolutePath();

            // 设置模板路径
            TemplateHandler.DataSource.Freemarker.setTemplatePath(templatePath);

            // 2. 定义数据根节点 map
            Map<String, Object> data = new HashMap<>();
            String monthDay = LocalDate.now().minusMonths(1)
                    .withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 基础账单信息（后续可以根据 list 或 pageReqVO 动态填充，这里先保留演示数据结构）
            data.put("recipient", "IGAMING");
            data.put("invoiceNo",  tenantDO.getCode()+monthDay);
            data.put("date",  LocalDate.now());
            data.put("dueDate", LocalDate.now());
            data.put("dateRange", pageReqVO.getDays()+"-"+pageReqVO.getEndDays());
            data.put("subtotal", "3.30");
            data.put("exchangeRate", "0.01047507");

            // 3. 将你的真实业务数据 list 转换或直接放入模板
            List<Map<String, Object>> itemList = new ArrayList<>();


            for (VendorDayStaticRespVO item :list) {
                Map<String, Object> item1 = new HashMap<>();
                item1.put("sn", "1");
                item1.put("product", item.getVendorName());
                item1.put("currency", item.getCurrency());
                item1.put("total", item.getWinLoss());
                item1.put("usdt", "47.25");
                item1.put("pt", "6%");
                item1.put("totalUsdt", "2.84");
                itemList.add(item1);
            }

            data.put("itemList", itemList);

            // 4. 设置 HTTP 响应头，准备输出文件流给浏览器下载
            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            // 防止中文文件名乱码，使用 URLEncoder 编码
            String fileName = java.net.URLEncoder.encode("账单.pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 5. 执行转换并直接写入 response.getOutputStream() 输送给前端下载
            TemplateHandler.Template.build()
                    .setConfigPath(configPath)
                    .setDataSource(
                            TemplateHandler.DataSource.Freemarker.build()
                                    .setTemplateName("template.fo")
                                    .setTemplateData(data))
                    .transform(response.getOutputStream());

        } catch (Exception e) {
            log.error("导出厂商报表 PDF 异常", e);
            response.reset();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().println("{\"code\":500,\"msg\":\"导出PDF失败\"}");
        }
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