package cn.iocoder.yudao.module.system.controller.admin.vendor;

import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorRespVO;
import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.service.vendor.VendorService;
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

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;



@Tag(name = "管理后台 - 游戏厂商")
@RestController
@RequestMapping("/game/vendor")
@Validated
public class VendorController {

    @Resource
    private VendorService vendorService;

    @PostMapping("/create")
    @Operation(summary = "创建游戏厂商")
    @PreAuthorize("@ss.hasPermission('game:vendor:create')")
    public CommonResult<Long> createVendor(@Valid @RequestBody VendorSaveReqVO createReqVO) {
        return success(vendorService.createVendor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新游戏厂商")
    @PreAuthorize("@ss.hasPermission('game:vendor:update')")
    public CommonResult<Boolean> updateVendor(@Valid @RequestBody VendorSaveReqVO updateReqVO) {
        vendorService.updateVendor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除游戏厂商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('game:vendor:delete')")
    public CommonResult<Boolean> deleteVendor(@RequestParam("id") Long id) {
        vendorService.deleteVendor(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除游戏厂商")
                @PreAuthorize("@ss.hasPermission('game:vendor:delete')")
    public CommonResult<Boolean> deleteVendorList(@RequestParam("ids") List<Long> ids) {
        vendorService.deleteVendorListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得游戏厂商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('game:vendor:query')")
    public CommonResult<VendorRespVO> getVendor(@RequestParam("id") Long id) {
        GameVendorDO vendor = vendorService.getVendor(id);
        return success(BeanUtils.toBean(vendor, VendorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得游戏厂商分页")
    @PreAuthorize("@ss.hasPermission('game:vendor:query')")
    public CommonResult<PageResult<VendorRespVO>> getVendorPage(@Valid VendorPageReqVO pageReqVO) {
        PageResult<GameVendorDO> pageResult = vendorService.getVendorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, VendorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出游戏厂商 Excel")
    @PreAuthorize("@ss.hasPermission('game:vendor:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorExcel(@Valid VendorPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GameVendorDO> list = vendorService.getVendorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "游戏厂商.xls", "数据", VendorRespVO.class,
                        BeanUtils.toBean(list, VendorRespVO.class));
    }

}