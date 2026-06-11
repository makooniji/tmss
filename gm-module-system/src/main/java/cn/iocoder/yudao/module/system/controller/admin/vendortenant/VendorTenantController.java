package cn.iocoder.yudao.module.system.controller.admin.vendortenant;

import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantRespVO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendortenant.VendorTenantDO;
import cn.iocoder.yudao.module.system.service.vendortenant.VendorTenantService;
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



@Tag(name = "管理后台 - 租户厂商")
@RestController
@RequestMapping("/game/vendor-tenant")
@Validated
public class VendorTenantController {

    @Resource
    private VendorTenantService vendorTenantService;

    @PostMapping("/create")
    @Operation(summary = "创建租户厂商")
    @PreAuthorize("@ss.hasPermission('game:vendor-tenant:create')")
    public CommonResult<Long> createVendorTenant(@Valid @RequestBody VendorTenantSaveReqVO createReqVO) {
        return success(vendorTenantService.createVendorTenant(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新租户厂商")
    @PreAuthorize("@ss.hasPermission('game:vendor-tenant:update')")
    public CommonResult<Boolean> updateVendorTenant(@Valid @RequestBody VendorTenantSaveReqVO updateReqVO) {
        vendorTenantService.updateVendorTenant(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除租户厂商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('game:vendor-tenant:delete')")
    public CommonResult<Boolean> deleteVendorTenant(@RequestParam("id") Long id) {
        vendorTenantService.deleteVendorTenant(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除租户厂商")
                @PreAuthorize("@ss.hasPermission('game:vendor-tenant:delete')")
    public CommonResult<Boolean> deleteVendorTenantList(@RequestParam("ids") List<Long> ids) {
        vendorTenantService.deleteVendorTenantListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得租户厂商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('game:vendor-tenant:query')")
    public CommonResult<VendorTenantRespVO> getVendorTenant(@RequestParam("id") Long id) {
        VendorTenantDO vendorTenant = vendorTenantService.getVendorTenant(id);
        return success(BeanUtils.toBean(vendorTenant, VendorTenantRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得租户厂商分页")
    @PreAuthorize("@ss.hasPermission('game:vendor-tenant:query')")
    public CommonResult<PageResult<VendorTenantRespVO>> getVendorTenantPage(@Valid VendorTenantPageReqVO pageReqVO) {
        PageResult<VendorTenantDO> pageResult = vendorTenantService.getVendorTenantPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, VendorTenantRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出租户厂商 Excel")
    @PreAuthorize("@ss.hasPermission('game:vendor-tenant:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorTenantExcel(@Valid VendorTenantPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VendorTenantDO> list = vendorTenantService.getVendorTenantPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "租户厂商.xls", "数据", VendorTenantRespVO.class,
                        BeanUtils.toBean(list, VendorTenantRespVO.class));
    }

}