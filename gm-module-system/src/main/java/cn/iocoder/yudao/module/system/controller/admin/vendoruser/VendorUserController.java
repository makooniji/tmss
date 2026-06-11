package cn.iocoder.yudao.module.system.controller.admin.vendoruser;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserRespVO;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.service.vendoruser.VendorUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;



@Tag(name = "管理后台 - 厂商玩家")
@RestController
@RequestMapping("/game/vendor-user")
@Validated
public class VendorUserController {

    @Resource
    private VendorUserService vendorUserService;

    @PostMapping("/create")
    @Operation(summary = "创建厂商玩家")
    @PreAuthorize("@ss.hasPermission('game:vendor-user:create')")
    public CommonResult<Long> createVendorUser(@Valid @RequestBody VendorUserSaveReqVO createReqVO) {
        return success(vendorUserService.createVendorUser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新厂商玩家")
    @PreAuthorize("@ss.hasPermission('game:vendor-user:update')")
    public CommonResult<Boolean> updateVendorUser(@Valid @RequestBody VendorUserSaveReqVO updateReqVO) {
        vendorUserService.updateVendorUser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除厂商玩家")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('game:vendor-user:delete')")
    public CommonResult<Boolean> deleteVendorUser(@RequestParam("id") Long id) {
        vendorUserService.deleteVendorUser(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除厂商玩家")
                @PreAuthorize("@ss.hasPermission('game:vendor-user:delete')")
    public CommonResult<Boolean> deleteVendorUserList(@RequestParam("ids") List<Long> ids) {
        vendorUserService.deleteVendorUserListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得厂商玩家")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('game:vendor-user:query')")
    public CommonResult<VendorUserRespVO> getVendorUser(@RequestParam("id") Long id) {
        VendorUserDO vendorUser = vendorUserService.getUserByVendor(id);
        return success(BeanUtils.toBean(vendorUser, VendorUserRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得厂商玩家分页")
    @PreAuthorize("@ss.hasPermission('game:vendor-user:query')")
    public CommonResult<PageResult<VendorUserRespVO>> getVendorUserPage(@Valid VendorUserPageReqVO pageReqVO) {
        PageResult<VendorUserDO> pageResult = vendorUserService.getVendorUserPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, VendorUserRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出厂商玩家 Excel")
    @PreAuthorize("@ss.hasPermission('game:vendor-user:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVendorUserExcel(@Valid VendorUserPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VendorUserDO> list = vendorUserService.getVendorUserPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "厂商玩家.xls", "数据", VendorUserRespVO.class,
                        BeanUtils.toBean(list, VendorUserRespVO.class));
    }

}