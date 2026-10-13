package cn.iocoder.yudao.report.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.report.controller.vo.GameStaticTotalVO;
import cn.iocoder.yudao.report.controller.vo.UserStaticPageReqVO;
import cn.iocoder.yudao.report.controller.vo.UserStaticRespVO;
import jakarta.validation.Valid;

/**
 * 用户游戏日报 Service 接口
 *
 * @author osca
 */
public interface UserDayStaticService {

    /**
     * 获得会员统计分页
     *
     * @param pageReqVO 分页查询
     * @return 会员统计分页
     */
    PageResult<UserStaticRespVO> getUserStaticPage(@Valid UserStaticPageReqVO pageReqVO);

    /**
     * 获得会员统计总计
     *
     * @param reqVO 查询条件
     * @return 会员统计总计
     */
    GameStaticTotalVO getUserStaticTotal(@Valid UserStaticPageReqVO reqVO);

}
