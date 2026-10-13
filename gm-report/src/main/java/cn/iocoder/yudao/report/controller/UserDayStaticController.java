package cn.iocoder.yudao.report.controller;

import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantSubScope;
import cn.iocoder.yudao.report.controller.vo.GameStaticTotalVO;
import cn.iocoder.yudao.report.controller.vo.UserStaticPageReqVO;
import cn.iocoder.yudao.report.controller.vo.UserStaticRespVO;
import cn.iocoder.yudao.report.service.UserDayStaticService;
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

import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 会员游戏日报
 */
@Tag(name = "管理后台 - 会员游戏日报")
@RestController
@RequestMapping("/game/user-day-static")
@Validated
@TenantSubScope
public class UserDayStaticController {

    @Resource
    private UserDayStaticService userDayStaticService;

    @GetMapping("/page")
    @Operation(summary = "会员统计分页")
    public CommonResult<PageResult<UserStaticRespVO>> getUserStaticPage(@Valid UserStaticPageReqVO pageReqVO) {
        return success(userDayStaticService.getUserStaticPage(pageReqVO));
    }

    @GetMapping("/total")
    @Operation(summary = "会员统计总计")
    public CommonResult<GameStaticTotalVO> getUserStaticTotal(@Valid UserStaticPageReqVO pageReqVO) {
        return success(userDayStaticService.getUserStaticTotal(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出会员统计 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserStaticExcel(@Valid UserStaticPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserStaticRespVO> list = userDayStaticService.getUserStaticPage(pageReqVO).getList();
        ExcelUtils.write(response, "会员统计报表.xls", "数据", UserStaticRespVO.class, list);
    }

}
