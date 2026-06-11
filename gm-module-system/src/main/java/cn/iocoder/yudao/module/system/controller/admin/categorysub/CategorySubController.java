package cn.iocoder.yudao.module.system.controller.admin.categorysub;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubRespVO;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import cn.iocoder.yudao.module.system.service.categorysub.CategorySubService;
import com.fhs.core.trans.anno.TransMethodResult;
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



@Tag(name = "管理后台 - 游戏分类")
@RestController
@RequestMapping("/game/category-sub")
@Validated
public class CategorySubController {

    @Resource
    private CategorySubService categorySubService;

    @PostMapping("/create")
    @Operation(summary = "创建游戏分类")
    @PreAuthorize("@ss.hasPermission('game:category-sub:create')")
    public CommonResult<Long> createCategorySub(@Valid @RequestBody CategorySubSaveReqVO createReqVO) {
        return success(categorySubService.createCategorySub(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新游戏分类")
    @PreAuthorize("@ss.hasPermission('game:category-sub:update')")
    public CommonResult<Boolean> updateCategorySub(@Valid @RequestBody CategorySubSaveReqVO updateReqVO) {
        categorySubService.updateCategorySub(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除游戏分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('game:category-sub:delete')")
    public CommonResult<Boolean> deleteCategorySub(@RequestParam("id") Long id) {
        categorySubService.deleteCategorySub(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除游戏分类")
                @PreAuthorize("@ss.hasPermission('game:category-sub:delete')")
    public CommonResult<Boolean> deleteCategorySubList(@RequestParam("ids") List<Long> ids) {
        categorySubService.deleteCategorySubListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得游戏分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('game:category-sub:query')")
    @TransMethodResult
    public CommonResult<CategorySubRespVO> getCategorySub(@RequestParam("id") Long id) {
        CategorySubDO categorySub = categorySubService.getCategorySub(id);
        return success(BeanUtils.toBean(categorySub, CategorySubRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得游戏分类分页")
    @PreAuthorize("@ss.hasPermission('game:category-sub:query')")
    @TransMethodResult
    public CommonResult<PageResult<CategorySubRespVO>> getCategorySubPage(@Valid CategorySubPageReqVO pageReqVO) {
        PageResult<CategorySubDO> pageResult = categorySubService.getCategorySubPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CategorySubRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出游戏分类 Excel")
    @PreAuthorize("@ss.hasPermission('game:category-sub:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCategorySubExcel(@Valid CategorySubPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CategorySubDO> list = categorySubService.getCategorySubPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "游戏分类.xls", "数据", CategorySubRespVO.class,
                        BeanUtils.toBean(list, CategorySubRespVO.class));
    }

}