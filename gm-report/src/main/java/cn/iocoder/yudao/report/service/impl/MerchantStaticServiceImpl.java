package cn.iocoder.yudao.report.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.report.controller.merchant.vo.MerchantStaticPageReqVO;
import cn.iocoder.yudao.report.controller.merchant.vo.MerchantStaticRespVO;
import cn.iocoder.yudao.report.mapper.MerchantUserDayStaticMapper;
import cn.iocoder.yudao.report.model.merchant.MerchantUserDayStaticDO;
import cn.iocoder.yudao.report.service.MerchantStaticService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;


/**
 * 租户游戏日报 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class MerchantStaticServiceImpl implements MerchantStaticService {

    @Resource
    private MerchantUserDayStaticMapper merchantUserDayStaticMapper;


    @Override
    public PageResult<MerchantStaticRespVO> getUserStaticPage(MerchantStaticPageReqVO reqVO) {
        IPage<MerchantUserDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<MerchantUserDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<MerchantUserDayStaticDO>()
                .geIfPresent(MerchantUserDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(MerchantUserDayStaticDO::getDays, reqVO.getEndDays())
                .inIfPresent(MerchantUserDayStaticDO::getAgentCode, reqVO.getAgentCodes())
                .inIfPresent(MerchantUserDayStaticDO::getTenantId, reqVO.getTenantIds())
                .eqIfPresent(MerchantUserDayStaticDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MerchantUserDayStaticDO::getUsername, reqVO.getUsername());

        IPage<MerchantUserDayStaticDO> gameTotalPage = merchantUserDayStaticMapper.selectUserStaticPage(mpPage, queryWrapperX);

        PageResult<MerchantUserDayStaticDO> tenantDayStaticDOPageResult = new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal());

        return BeanUtils.toBean(tenantDayStaticDOPageResult, MerchantStaticRespVO.class);

    }

    @Override
    public PageResult<MerchantStaticRespVO> getAgentStaticPage(MerchantStaticPageReqVO reqVO) {
        IPage<MerchantUserDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);

        LambdaQueryWrapperX<MerchantUserDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<MerchantUserDayStaticDO>()
                .geIfPresent(MerchantUserDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(MerchantUserDayStaticDO::getDays, reqVO.getEndDays())
                .inIfPresent(MerchantUserDayStaticDO::getAgentCode, reqVO.getAgentCodes())
                .inIfPresent(MerchantUserDayStaticDO::getTenantId, reqVO.getTenantIds())
                .eqIfPresent(MerchantUserDayStaticDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MerchantUserDayStaticDO::getUsername, reqVO.getUsername());

        if (PageParam.PAGE_SIZE_NONE.equals(reqVO.getPageSize())) {
            List<MerchantUserDayStaticDO> merchantUserDayStaticDOS = merchantUserDayStaticMapper.selectAgentStaticList(queryWrapperX);
            List<MerchantStaticRespVO> bean = BeanUtils.toBean(merchantUserDayStaticDOS, MerchantStaticRespVO.class);
            return new PageResult<>(bean, (long) merchantUserDayStaticDOS.size());
        }

        IPage<MerchantUserDayStaticDO> gameTotalPage = merchantUserDayStaticMapper.selectAgentStaticPage(mpPage, queryWrapperX);

        PageResult<MerchantUserDayStaticDO> tenantDayStaticDOPageResult = new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal());

        return BeanUtils.toBean(tenantDayStaticDOPageResult, MerchantStaticRespVO.class);
    }

    @Override
    public MerchantStaticRespVO getUserStaticTotal(MerchantStaticPageReqVO reqVO) {
        LambdaQueryWrapperX<MerchantUserDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<MerchantUserDayStaticDO>()
                .geIfPresent(MerchantUserDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(MerchantUserDayStaticDO::getDays, reqVO.getEndDays())
                .inIfPresent(MerchantUserDayStaticDO::getAgentCode, reqVO.getAgentCodes())
                .inIfPresent(MerchantUserDayStaticDO::getTenantId, reqVO.getTenantIds())
                .eqIfPresent(MerchantUserDayStaticDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MerchantUserDayStaticDO::getUsername, reqVO.getUsername());

        return BeanUtils.toBean(merchantUserDayStaticMapper.selectUserStaticTotal(queryWrapperX), MerchantStaticRespVO.class);
    }


}