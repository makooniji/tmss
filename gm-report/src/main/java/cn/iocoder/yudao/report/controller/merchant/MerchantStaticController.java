package cn.iocoder.yudao.report.controller.merchant;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.report.controller.merchant.vo.MerchantStaticPageReqVO;
import cn.iocoder.yudao.report.controller.merchant.vo.MerchantStaticRespVO;
import cn.iocoder.yudao.report.service.MerchantStaticService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 商户报表")
@RestController
@RequestMapping("/merchant")
@Validated
public class MerchantStaticController {

    @Resource
    private MerchantStaticService staticService;

    /**
     * 会员数据统计
     *
     * @param pageReqVO
     * @return
     */
    @GetMapping("/user/page")
    public CommonResult<PageResult<MerchantStaticRespVO>> getUserStaticPage(@Valid MerchantStaticPageReqVO pageReqVO) {
        PageResult<MerchantStaticRespVO> pageResult = staticService.getUserStaticPage(pageReqVO);
        return success(pageResult);
    }

    /**
     * 会员数据导出
     *
     * @param pageReqVO
     * @param response
     * @throws IOException
     */
    @GetMapping("/user/export-excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTenantDayStaticExcel(@Valid MerchantStaticPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MerchantStaticRespVO> list = staticService.getUserStaticPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "会员数据报表.xls", "数据", MerchantStaticRespVO.class,
                list);
    }

    /**
     * 代理数据统计
     *
     * @param pageReqVO
     * @return
     */
    @GetMapping("/agent/page")
    public CommonResult<PageResult<MerchantStaticRespVO>> getAgentStaticPage(@Valid MerchantStaticPageReqVO pageReqVO) {
        PageResult<MerchantStaticRespVO> pageResult = staticService.getAgentStaticPage(pageReqVO);
        return success(pageResult);
    }

    /**
     * 代理相关会员总计
     *
     * @param pageReqVO
     * @return
     */
    @GetMapping("/user/total")
    public CommonResult<MerchantStaticRespVO> getUserStaticTotal(@Valid MerchantStaticPageReqVO pageReqVO) {
        MerchantStaticRespVO pageResult = staticService.getUserStaticTotal(pageReqVO);
        return success(pageResult);
    }


}