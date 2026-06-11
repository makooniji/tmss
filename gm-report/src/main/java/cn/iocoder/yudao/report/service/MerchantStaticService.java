package cn.iocoder.yudao.report.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.report.controller.merchant.vo.MerchantStaticPageReqVO;
import cn.iocoder.yudao.report.controller.merchant.vo.MerchantStaticRespVO;
import jakarta.validation.Valid;

/**
 * 租户游戏日报 Service 接口
 *
 * @author osca
 */
public interface MerchantStaticService {


    /**
     * 获得报表分页
     *
     * @param pageReqVO 分页查询
     * @return 租户游戏日报分页
     */
    PageResult<MerchantStaticRespVO> getUserStaticPage(MerchantStaticPageReqVO pageReqVO);

    PageResult<MerchantStaticRespVO> getAgentStaticPage(MerchantStaticPageReqVO pageReqVO);


    MerchantStaticRespVO getUserStaticTotal(@Valid MerchantStaticPageReqVO pageReqVO);
}