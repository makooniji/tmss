package cn.iocoder.yudao.module.system.controller.admin.records;


import cn.iocoder.yudao.module.system.model.GameRecordsDO;
import cn.iocoder.yudao.module.system.model.vo.*;
import cn.iocoder.yudao.module.system.model.vo.excel.RecordsPageExcelVO;
import cn.iocoder.yudao.module.system.model.vo.excel.RecordsRespExcelVO;
import cn.iocoder.yudao.module.system.model.CategoryDO;

import cn.iocoder.yudao.module.system.service.category.CategoryService;
import cn.iocoder.yudao.module.system.service.GameRecordsService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.*;



@Tag(name = "管理后台 - 用户游戏记录")
@RestController
@RequestMapping("/game/records")
@Validated
public class RecordsController {

    @Resource
    private GameRecordsService gameRecordsService;

    @Resource
    private CategoryService categoryService;





    @DeleteMapping("/delete")
    @Operation(summary = "删除用户游戏记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('game:records:delete')")
    public CommonResult<Boolean> deleteRecords(@RequestParam("id") Long id) {
        gameRecordsService.deleteRecords(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除用户游戏记录")
                @PreAuthorize("@ss.hasPermission('game:records:delete')")
    public CommonResult<Boolean> deleteRecordsList(@RequestParam("ids") List<Long> ids) {
        gameRecordsService.deleteRecordsListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户游戏记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('game:records:query')")
    public CommonResult<RecordsRespVO> getRecords(@RequestParam("id") Long id) {
        GameRecordsDO records = gameRecordsService.getRecords(id);
        return success(RecordsRespVO.from(records));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户游戏记录分页")
    @PreAuthorize("@ss.hasPermission('game:records:query')")
    public CommonResult<PageResult<RecordsRespVO>> getRecordsPage(@Valid RecordsPageReqVO pageReqVO) {
        PageResult<GameRecordsDO> pageResult = gameRecordsService.getRecordsPage(pageReqVO);
        return success(RecordsRespVO.fromPage(pageResult));
    }

    /**
     * 查询场馆统计数据
     * @param pageReqVO
     * @return
     */
    @GetMapping("/vendorTotal")
    @PreAuthorize("@ss.hasPermission('game:records:query')")
    public CommonResult<VendorTotalRespVO> getVendorTotal(@Valid RecordsTotalReqVO pageReqVO) {
        VendorTotalRespVO recordsTotalRespVOIPage = gameRecordsService.getVendorDataTotal(pageReqVO);
        return success(recordsTotalRespVOIPage);
    }

    /**
     * 获得游戏统计数据分页
     * @param pageReqVO
     * @return
     */
    @GetMapping("/pageTotal")
    @PreAuthorize("@ss.hasPermission('game:records:query')")
    public CommonResult<PageResult<RecordsTotalRespVO>> getRecordsPageTotal(@Valid RecordsTotalPageReqVO pageReqVO) {
        PageResult<RecordsTotalRespVO> pageResult = gameRecordsService.getRecordsTotalPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户游戏记录 Excel")
    @PreAuthorize("@ss.hasPermission('game:records:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRecordsExcel(@Valid RecordsPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GameRecordsDO> list = gameRecordsService.getRecordsPage(pageReqVO).getList();
        ExcelUtils.write(response, "用户游戏记录.xls", "数据", RecordsRespVO.class,
                RecordsRespVO.fromList(list));
    }

    @GetMapping("/export-list-excel")
    @Operation(summary = "导出用户游戏记录列表 Excel")
    @PreAuthorize("@ss.hasPermission('game:records:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRecordsListExcel(@Valid RecordsPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GameRecordsDO> list = gameRecordsService.getRecordsPage(pageReqVO).getList();
        List<RecordsRespExcelVO> excelList = list.stream().map(record -> {
            RecordsRespExcelVO excelVO = BeanUtils.toBean(record, RecordsRespExcelVO.class);
            if (record.getAction() != null) {
                excelVO.setActionName(record.getAction().getDesc());
            }
            excelVO.setWinLoss(record.getWinLoss());
            return excelVO;
        }).toList();
        ExcelUtils.write(response, "用户游戏记录列表.xlsx", "数据", RecordsRespExcelVO.class, excelList);
    }

    @GetMapping("/export-page-excel")
    @Operation(summary = "导出用户游戏记录查询页 Excel")
    @PreAuthorize("@ss.hasPermission('game:records:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRecordsPageExcel(@Valid RecordsPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GameRecordsDO> list = gameRecordsService.getRecordsPage(pageReqVO).getList();
        Map<Long, String> gameKindNameMap = categoryService.getList().stream()
                .collect(Collectors.toMap(CategoryDO::getId, CategoryDO::getTitle, (a, b) -> a));
        List<RecordsPageExcelVO> excelList = list.stream().map(record -> {
            RecordsPageExcelVO excelVO = BeanUtils.toBean(record, RecordsPageExcelVO.class);
            if (record.getAction() != null) {
                excelVO.setActionName(record.getAction().getDesc());
            }
            excelVO.setWinLoss(record.getWinLoss());
            if (record.getGameKind() != null) {
                excelVO.setGameKindName(gameKindNameMap.get(record.getGameKind()));
            }
            return excelVO;
        }).toList();
        ExcelUtils.write(response, "用户游戏记录查询.xlsx", "数据", RecordsPageExcelVO.class, excelList);
    }

}