package cn.iocoder.yudao.module.system.controller.admin.gameInfo;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoRespVO;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.service.info.GameInfoService;
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



@Tag(name = "管理后台 - 游戏信息")
@RestController
@RequestMapping("/game/info")
@Validated
public class GameInfoController {

    @Resource
    private GameInfoService gameInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建游戏信息")
    @PreAuthorize("@ss.hasPermission('game:info:create')")
    public CommonResult<Integer> createInfo(@Valid @RequestBody InfoSaveReqVO createReqVO) {
        return success(gameInfoService.createInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新游戏信息")
    @PreAuthorize("@ss.hasPermission('game:info:update')")
    public CommonResult<Boolean> updateInfo(@Valid @RequestBody InfoSaveReqVO updateReqVO) {
        gameInfoService.updateInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除游戏信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('game:info:delete')")
    public CommonResult<Boolean> deleteInfo(@RequestParam("id") Integer id) {
        gameInfoService.deleteInfo(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除游戏信息")
                @PreAuthorize("@ss.hasPermission('game:info:delete')")
    public CommonResult<Boolean> deleteInfoList(@RequestParam("ids") List<Integer> ids) {
        gameInfoService.deleteInfoListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得游戏信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('game:info:query')")
    public CommonResult<InfoRespVO> getInfo(@RequestParam("id") Integer id) {
        GameInfoDO info = gameInfoService.getInfo(id);
        return success(BeanUtils.toBean(info, InfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得游戏信息分页")
    @PreAuthorize("@ss.hasPermission('game:info:query')")
    @TransMethodResult
    public CommonResult<PageResult<InfoRespVO>> getInfoPage(@Valid InfoPageReqVO pageReqVO) {
        PageResult<GameInfoDO> pageResult = gameInfoService.getInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, InfoRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出游戏信息 Excel")
    @PreAuthorize("@ss.hasPermission('game:info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInfoExcel(@Valid InfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GameInfoDO> list = gameInfoService.getInfoPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "游戏信息.xls", "数据", InfoRespVO.class,
                        BeanUtils.toBean(list, InfoRespVO.class));
    }

}