package cn.iocoder.yudao.report.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.report.controller.vo.GameStaticTotalVO;
import cn.iocoder.yudao.report.controller.vo.UserStaticPageReqVO;
import cn.iocoder.yudao.report.controller.vo.UserStaticRespVO;
import cn.iocoder.yudao.report.mapper.TenantUserDayStaticMapper;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import cn.iocoder.yudao.report.service.UserDayStaticService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 用户游戏日报 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class UserDayStaticServiceImpl implements UserDayStaticService {

    @Resource
    private TenantUserDayStaticMapper tenantUserDayStaticMapper;

    @Override
    public PageResult<UserStaticRespVO> getUserStaticPage(UserStaticPageReqVO reqVO) {
        IPage<TenantUserDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        IPage<TenantUserDayStaticDO> page = tenantUserDayStaticMapper.selectUserStaticPage(mpPage, buildQueryWrapper(reqVO));
        PageResult<TenantUserDayStaticDO> pageResult = new PageResult<>(page.getRecords(), page.getTotal());
        return BeanUtils.toBean(pageResult, UserStaticRespVO.class);
    }

    @Override
    public GameStaticTotalVO getUserStaticTotal(UserStaticPageReqVO reqVO) {
        return tenantUserDayStaticMapper.selectUserStaticTotal(buildQueryWrapper(reqVO));
    }

    private LambdaQueryWrapperX<TenantUserDayStaticDO> buildQueryWrapper(UserStaticPageReqVO reqVO) {
        return new LambdaQueryWrapperX<TenantUserDayStaticDO>()
                .geIfPresent(TenantUserDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantUserDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantUserDayStaticDO::getUserId, reqVO.getUserId())
                .eqIfPresent(TenantUserDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(TenantUserDayStaticDO::getGameCode, reqVO.getGameCode())
                .likeIfPresent(TenantUserDayStaticDO::getUsername, reqVO.getUsername())
                .eqIfPresent(TenantUserDayStaticDO::getTenantId, reqVO.getTenantId())
                .eqIfPresent(TenantUserDayStaticDO::getCurrency, reqVO.getCurrency());
    }

}
