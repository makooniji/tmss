package cn.iocoder.yudao.report.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.web.currency.CurrencyContextHolder;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.mapper.GameInfoMapper;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.service.category.CategoryService;
import cn.iocoder.yudao.module.system.service.TenantService;
import cn.iocoder.yudao.report.controller.api.dto.*;
import cn.iocoder.yudao.report.controller.vo.*;
import cn.iocoder.yudao.report.mapper.TenantDayStaticMapper;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 租户游戏日报 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class TenantDayStaticServiceImpl implements TenantDayStaticService {

    @Resource
    private TenantDayStaticMapper tenantDayStaticMapper;
    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private GameInfoMapper gameInfoMapper;
    @Resource
    private TenantService tenantService;
    @Resource
    private CategoryService categoryService;

    /**
     * 自定义 @Select 统计 SQL 需显式限制可见租户（系统租户 null=不限制，商户=自己+子代理）
     */
    private void applyVisibleTenantScope(LambdaQueryWrapperX<TenantDayStaticDO> wrapper) {
//        wrapper.inIfPresent(TenantDayStaticDO::getTenantId, tenantService.getVisibleTenantIds());
    }

    @Override
    public Long createTenantDayStatic(TenantDayStaticSaveReqVO createReqVO) {
        // 插入
        TenantDayStaticDO tenantDayStatic = BeanUtils.toBean(createReqVO, TenantDayStaticDO.class);
        tenantDayStaticMapper.insert(tenantDayStatic);

        // 返回
        return tenantDayStatic.getId();
    }

    @Override
    public void updateTenantDayStatic(TenantDayStaticSaveReqVO updateReqVO) {

        // 更新
        TenantDayStaticDO updateObj = BeanUtils.toBean(updateReqVO, TenantDayStaticDO.class);
        tenantDayStaticMapper.updateById(updateObj);
    }

    @Override
    public void deleteTenantDayStatic(Long id) {
        // 删除
        tenantDayStaticMapper.deleteById(id);
    }

    @Override
    public void deleteTenantDayStaticListByIds(List<Long> ids) {
        // 删除
        tenantDayStaticMapper.deleteByIds(ids);
    }


    @Override
    public TenantDayStaticDO getTenantDayStatic(Long id) {
        return tenantDayStaticMapper.selectById(id);
    }

    @Override
    public PageResult<TenantDayStaticRespVO> getTenantStaticPage(TenantDayStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(TenantDayStaticDO::getCurrency, reqVO.getCurrency())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);

        IPage<TenantDayStaticDO> tenantDayStaticDOIPage = tenantDayStaticMapper.selectTenantPageTotal(mpPage, queryWrapperX);

        TenantDayStaticDO tenantDayStaticDO = tenantDayStaticMapper.selectTenantTotal(queryWrapperX);

        PageResult<TenantDayStaticDO> tenantDayStaticDOPageResult = new PageResult<>(tenantDayStaticDOIPage.getRecords(), tenantDayStaticDOIPage.getTotal());
        PageResult<TenantDayStaticRespVO> bean = BeanUtils.toBean(tenantDayStaticDOPageResult, TenantDayStaticRespVO.class);
        if (ObjectUtil.isEmpty(bean.getList())) {
            return new PageResult<>();
        }
        List<Long> list = bean.getList().stream().map(TenantDayStaticRespVO::getTenantId).toList();
        // 查询租户信息
        List<TenantDO> tenantDOS = tenantMapper.selectByIds(list);
        bean.getList().forEach(item -> {
            tenantDOS.stream().filter(p -> p.getId().equals(item.getTenantId())).findFirst().ifPresent(item::setTenantDO);
        });
        bean.setTotalCount(tenantDayStaticDO);
        return bean;
    }

    @Override
    public PageResult<VendorDayStaticRespVO> getVendorStaticPage(TenantDayStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = buildVendorStaticQueryWrapper(reqVO);

        IPage<VendorDayStaticRespVO> vendorStaticPage = tenantDayStaticMapper.selectVendorStaticPage(mpPage, queryWrapperX);
        if (ObjectUtil.isEmpty(vendorStaticPage.getRecords())) {
            return new PageResult<>();
        }
        return new PageResult<>(vendorStaticPage.getRecords(), vendorStaticPage.getTotal());
    }

    @Override
    public List<VendorDayStaticRespVO> getVendorStatic(TenantDayStaticPageReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = buildVendorStaticQueryWrapper(reqVO);

        return tenantDayStaticMapper.selectVendorStatic( queryWrapperX);
    }

    @Override
    public PageResult<VendorTenantDayStaticRespVO> getVendorTenantStaticPage(TenantDayStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = buildVendorStaticQueryWrapper(reqVO);

        IPage<VendorTenantDayStaticRespVO> vendorTenantStaticPage =
                tenantDayStaticMapper.selectVendorTenantStaticPage(mpPage, queryWrapperX);
        if (ObjectUtil.isEmpty(vendorTenantStaticPage.getRecords())) {
            return new PageResult<>();
        }
        return new PageResult<>(vendorTenantStaticPage.getRecords(), vendorTenantStaticPage.getTotal());
    }

    @Override
    public TenantStaticTotalVO getVendorTenantStaticTotal(TenantDayStaticPageReqVO reqVO) {
        return buildVendorStaticTotal(reqVO);
    }

    @Override
    public TenantStaticTotalVO getVendorStaticTotal(TenantDayStaticPageReqVO reqVO) {
        return buildVendorStaticTotal(reqVO);
    }

    private TenantStaticTotalVO buildVendorStaticTotal(TenantDayStaticPageReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = buildVendorStaticQueryWrapper(reqVO);
        TenantStaticTotalVO tenantStaticTotalVO = tenantDayStaticMapper.selectTenantCount(queryWrapperX);
        if (ObjectUtil.isEmpty(tenantStaticTotalVO)) {
            return tenantStaticTotalVO;
        }
        var top1 = tenantDayStaticMapper.selectGameTop(queryWrapperX, 1);
        tenantStaticTotalVO.setHotGame(top1);
        return tenantStaticTotalVO;
    }

    private LambdaQueryWrapperX<TenantDayStaticDO> buildVendorStaticQueryWrapper(TenantDayStaticPageReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
               .eqIfPresent(TenantDayStaticDO::getTenantId, reqVO.getTenantId())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);
        return queryWrapperX;
    }

    @Override
    public GameStaticTotalVO getGameStaticTotal(TenantDayStaticReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(TenantDayStaticDO::getCurrency, reqVO.getCurrency())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);
        return tenantDayStaticMapper.selectTotalCount(queryWrapperX);
    }

    @Override
    public PageResult<TenantDayStaticRespVO> getTenantGameStaticPage(TenantDayStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(TenantDayStaticDO::getGameCode, reqVO.getGameCode())
                .eqIfPresent(TenantDayStaticDO::getCurrency, reqVO.getCurrency())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);

        IPage<TenantDayStaticRespVO> gameTotalPage = tenantDayStaticMapper.selectTenantGameTotalPageWithGameInfo(mpPage, queryWrapperX);
        if (ObjectUtil.isEmpty(gameTotalPage.getRecords())) {
            return new PageResult<>();
        }
        TenantDayStaticRespVO totalCount = tenantDayStaticMapper.selectTenantGameTotalWithGameInfo(queryWrapperX);
        return new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal(),totalCount);

    }

//    public PageResult<TenantDayStaticRespVO> getTenantGameStaticPage(TenantDayStaticPageReqVO reqVO) {
//        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO,null);
//        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
//                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
//                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
//                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
//                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
//                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
//                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
//        applyVisibleTenantScope(queryWrapperX);
//
//        IPage<TenantDayStaticDO> gameTotalPage = tenantDayStaticMapper.selectTenantGameTotalPage(mpPage, queryWrapperX);
//        // gameCode 查找类型
//        if(ObjectUtil.isEmpty( gameTotalPage.getRecords())){
//            return new PageResult<>();
//        }
//        List<String> list = gameTotalPage.getRecords().stream().map(TenantDayStaticDO::getGameCode).toList();
//        List<GameInfoDO> gameInfoDOS = gameInfoMapper.selectList(Wrappers.<GameInfoDO>lambdaQuery().in(GameInfoDO::getGameCode, list));
//       /* if(ObjectUtil.isEmpty(gameInfoDOS)){
//            return new PageResult<>();
//        }*/
//        PageResult<TenantDayStaticDO> tenantDayStaticDOPageResult = new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal());
//
//        return BeanUtils.toBean(tenantDayStaticDOPageResult, TenantDayStaticRespVO.class);
//
//    }

    @Override
    public TenantStaticTotalVO getTenantStaticTotal(TenantDayStaticReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName())
                .eqIfPresent(TenantDayStaticDO::getGameCode, reqVO.getGameCode())
                .likeIfPresent(TenantDayStaticDO::getGameName, reqVO.getGameName());
        applyVisibleTenantScope(queryWrapperX);

        TenantStaticTotalVO tenantStaticTotalVO = tenantDayStaticMapper.selectTenantCount(queryWrapperX);
        if (ObjectUtil.isEmpty(tenantStaticTotalVO)) {
            return tenantStaticTotalVO;
        }
        var top1 = tenantDayStaticMapper.selectGameTop(queryWrapperX, 1);
        tenantStaticTotalVO.setHotGame(top1);
        return tenantStaticTotalVO;
    }

    @Override
    public Map<String, Object> getStaticChart(TenantDayStaticReqVO reqVO) {
        List<Map<String, Object>> dayData = tenantDayStaticMapper.selectStaticChart(reqVO);


        LambdaQueryWrapperX<TenantDO> queryWrapperX1 = new LambdaQueryWrapperX<TenantDO>()
                .geIfPresent(TenantDO::getCreateTime, reqVO.getDays())
                .leIfPresent(TenantDO::getCreateTime, reqVO.getEndDays())
                .eq(TenantDO::getStatus, reqVO.getStatus());
        //查询每天注册多少商户
        List<Map<String, Object>> dayCounts = tenantMapper.selectDayCounts(queryWrapperX1);

        return MapUtil.<String, Object>builder().put("dayCounts", dayCounts).put("dayData", dayData).build();
    }

    @Override
    public List<Map<String, Object>> getStaticDayInfo(TenantChartsReqVO reqVO) {

        List<Long> scopedTenantIds = tenantService.getScopedTenantIds(TenantContextHolder.getTenantId());
        reqVO.setScopeIds(scopedTenantIds);
        //查询租户状态统计
        if(reqVO.getOrderBy().equals("openCounts")) {
            //运营的数量
            if(reqVO.getStatus().equals(1)) {
                reqVO.setStatus(0);
                reqVO.setOperateStatus(0);
                reqVO.setOrderBy("betAmount");

            }
            //关闭的数量
           else if(reqVO.getStatus().equals(2)) {
                reqVO.setStatus(1);
                reqVO.setOrderBy("betAmount");

            }
            //开启的数量
           else if(reqVO.getStatus().equals(0)){
                reqVO.setStatus(0);
                reqVO.setOrderBy("openDays");
            }
        }

        TenantDayStaticReqVO.StatMetricEnum statMetricEnum = TenantDayStaticReqVO.StatMetricEnum.fromCode(reqVO.getOrderBy());
        return tenantDayStaticMapper.selectStaticChartDayInfo(reqVO, statMetricEnum.getFormula());
    }

    @Override
    public List<Map<String, Object>> getStaticTenantDaysInfo(TenantChartsReqVO reqVO) {
        TenantDayStaticReqVO.StatMetricEnum statMetricEnum = TenantDayStaticReqVO.StatMetricEnum.fromCode(reqVO.getOrderBy());
        return tenantDayStaticMapper.selectStaticTenantDaysInfo(reqVO, statMetricEnum.getFormula());
    }

    @Override
    public List<Map<String, Object>> getStaticDayTotal(@Valid TenantChartsReqVO pageReqVO) {

        List<Long> scopedTenantIds = tenantService.getScopedTenantIds(TenantContextHolder.getTenantId());
        //查询租户状态统计
        if(pageReqVO.getOrderBy().equals("openCounts")) {
            //运营的
            if(pageReqVO.getStatus().equals(1)) {
               return tenantMapper.selectTenantDayOperateCount(0, 0,scopedTenantIds);
            }
            //关闭的
            if(pageReqVO.getStatus().equals(2)) {
              return   tenantMapper.selectTenantDayCloseCount(1, null,scopedTenantIds);
            }

          return  tenantMapper.selectTenantDayCount(0,null,scopedTenantIds);
        }

        QueryWrapper<TenantDayStaticDO> queryWrapper = Wrappers.<TenantDayStaticDO>query().gt(pageReqVO.getDays() != null, "a.days", pageReqVO.getDays())
                .lt(pageReqVO.getEndDays() != null, "a.days", pageReqVO.getEndDays())
                .eq(pageReqVO.getStatus() != null, "b.status", pageReqVO.getStatus())
                .in(scopedTenantIds != null && !scopedTenantIds.isEmpty(), "a.tenant_id", scopedTenantIds)
                .gt("a.tenant_id", 1);

        return tenantDayStaticMapper.selectDayTotal(queryWrapper);
    }

    @Override
    public VendorStaticTotalVO getStaticVendorTotal(TenantDayStaticReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName())
                .eqIfPresent(TenantDayStaticDO::getGameCode, reqVO.getGameCode())
                .likeIfPresent(TenantDayStaticDO::getGameName, reqVO.getGameName());
        applyVisibleTenantScope(queryWrapperX);
        return tenantDayStaticMapper.selectStaticVendorTotal(queryWrapperX);
    }

    @Override
    public TenantGameStaticTotalVO getStaticGameTotal(TenantDayStaticReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName())
                .eqIfPresent(TenantDayStaticDO::getGameCode, reqVO.getGameCode())
                .inIfPresent(TenantDayStaticDO::getTenantId, tenantService.resolveQueryTenantIds(reqVO.getTenantId()))
                .likeIfPresent(TenantDayStaticDO::getGameName, reqVO.getGameName());
        applyVisibleTenantScope(queryWrapperX);

        return tenantDayStaticMapper.selectStaticGameTotal(queryWrapperX);
    }

    @Override
    public TenantStatusCountVO getTenantStatus() {
        LambdaQueryWrapperX<TenantDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.gt(TenantDO::getId, 1);
        wrapper.apply("deleted = 0");
        wrapper.inIfPresent(TenantDO::getId, tenantService.getVisibleTenantIds());
        TenantMapper.TenantStatusCount tenantStatusCount = tenantMapper.selectTenantStatus(wrapper);

        return BeanUtils.toBean(tenantStatusCount, TenantStatusCountVO.class);
    }

    @Override
    public PageResult<UserStaticRespVO> getUserStaticPage(UserStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getCurrency, reqVO.getCurrency())
                .eq(TenantDayStaticDO::getTenantId, reqVO.getTenantId());

        IPage<TenantUserDayStaticDO> tenantUserDayStaticDOIPage = tenantDayStaticMapper.selectUserStaticPage(mpPage, queryWrapperX);
        PageResult<TenantUserDayStaticDO> pageResult = new PageResult<>(tenantUserDayStaticDOIPage.getRecords(), tenantUserDayStaticDOIPage.getTotal());

        return BeanUtils.toBean(pageResult, UserStaticRespVO.class);
    }

    @Override
    public List<Map<String, Object>> getChartGameTotal(TenantChartsReqVO reqVO) {
        TenantDayStaticReqVO.StatMetricEnum statMetricEnum = TenantDayStaticReqVO.StatMetricEnum.fromCode(reqVO.getOrderBy());
        return tenantDayStaticMapper.selectChartGameTotal(reqVO, statMetricEnum.getFormula());
    }

    @Override
    public List<Map<String, Object>> getChartGameDayTotal(TenantChartsReqVO pageReqVO) {
        TenantDayStaticReqVO.StatMetricEnum statMetricEnum = TenantDayStaticReqVO.StatMetricEnum.fromCode(pageReqVO.getOrderBy());
        return tenantDayStaticMapper.selectChartGameDayTotal(pageReqVO, statMetricEnum.getFormula());
    }

    @Override
    public PageResult<TenantStaticRespDto> getSupportMerchantStaticPage(MerchantPageReqDto reqVO) {

        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        QueryWrapper<Object> queryWrapperX = new QueryWrapper<>()
                .ge(reqVO.getStartTime()!=null,"a.days", reqVO.getStartTime())
                .le(reqVO.getEndTime()!=null,"a.days", reqVO.getEndTime())
                .and(reqVO.getIdOrName()!=null,p->p.like("a.tenant_code",reqVO.getIdOrName()).or().like("a.tenant_name",reqVO.getIdOrName()))

                .in(reqVO.getTid()!=null,"c.tid", reqVO.getTid())
                .eq(reqVO.getStatus()!=null,"c.status", reqVO.getStatus())
                .isNotNull("c.tid")
                .eq(reqVO.getOperateStatus()!=null,"c.operate_status", reqVO.getOperateStatus());


        IPage<TenantStaticRespDto> gameTotalPage = tenantDayStaticMapper.selectSupportTenantPageTotal(mpPage, queryWrapperX);
        if (ObjectUtil.isEmpty(gameTotalPage.getRecords())) {
            return new PageResult<>();
        }
        TenantStaticRespDto totalCount = tenantDayStaticMapper.selectSupportTenantTotal(queryWrapperX);
        return new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal(),totalCount);
    }

    @Override
    public PageResult<MerchantRespDto> getSupportMerchantInfoPage(MerchantPageReqDto reqVO) {
        IPage<TenantDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapper<TenantDO> queryWrapperX = new LambdaQueryWrapperX<TenantDO>()
                .geIfPresent(TenantDO::getCreateTime, reqVO.getStartTime())
                .leIfPresent(TenantDO::getCreateTime, reqVO.getEndTime())
                .eqIfPresent(TenantDO::getStatus, reqVO.getStatus())
                .inIfPresent(TenantDO::getTid, reqVO.getTid())
                .eqIfPresent(TenantDO::getOperateStatus,reqVO.getOperateStatus())
                .findContainsOrAll(TenantDO::getCurrency, reqVO.getCurrency())
                .and(reqVO.getIdOrName()!=null,p->p.like(TenantDO::getName,reqVO.getIdOrName()).or().like(TenantDO::getCode,reqVO.getIdOrName()))
                .isNotNull(TenantDO::getTid);
        IPage<TenantDO> tenantRespVOPageResult = tenantMapper.selectPage(mpPage, queryWrapperX);
        PageResult<TenantDO> bean = new PageResult<>(tenantRespVOPageResult.getRecords(), tenantRespVOPageResult.getTotal());

        return BeanUtils.toBean(bean, MerchantRespDto.class);
    }

    @Override
    public PageResult<VendorStaticRespDto> getSupportVendorStaticPage(VendorPageReqDto reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        QueryWrapper<Object> queryWrapperX = new QueryWrapper<>()
                .eq(reqVO.getCategoryCode()!=null,"d.code", reqVO.getCategoryCode())
                .eq(reqVO.getMerchantCode()!=null,"a.tenant_code", reqVO.getMerchantCode())
                .eq(reqVO.getVendorCode()!=null,"b.vendor_code", reqVO.getVendorCode())
                .eq(reqVO.getTid()!=null,"c.tid", reqVO.getTid())
                .eq(ObjectUtil.isNotEmpty( CurrencyContextHolder.getCurrency()),"a.currency", CurrencyContextHolder.getCurrency())
                ;

        IPage<VendorStaticRespDto> gameTotalPage = tenantDayStaticMapper.selectSupportVendorPageTotal(mpPage, queryWrapperX);
        if (ObjectUtil.isEmpty(gameTotalPage.getRecords())) {
            return new PageResult<>();
        }
        return new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal());
    }

    @Override
    public MerchantTotalRespDto getSupportMerchantTotal(Integer tid) {
        MerchantTotalRespDto result=new MerchantTotalRespDto();
        QueryWrapper<Object> lastWeekQ = new QueryWrapper<>()
                .ge("a.days",DateUtil.beginOfWeek( DateUtil.lastWeek()))
                .le("a.days", DateUtil.endOfWeek( DateUtil.lastWeek()))
                .eq("a.currency", CurrencyContextHolder.getCurrency())
                .eq(tid!=null,"c.tid",tid);


        QueryWrapper<Object> thisWeekQ = new QueryWrapper<>()
                .ge("a.days",DateUtil.beginOfWeek( DateUtil.date()))
                .le("a.days", DateUtil.endOfWeek( DateUtil.date()))
                .eq("a.currency", CurrencyContextHolder.getCurrency())
                .eq(tid!=null,"c.tid",tid);
        QueryWrapper<Object> total = new QueryWrapper<>()
                .eq("a.currency", CurrencyContextHolder.getCurrency())
                .eq(tid != null, "c.tid", tid);

        result.setTotal(tenantDayStaticMapper.selectSupportMerchantTotal(total));

        result.setPrevWeek(tenantDayStaticMapper.selectSupportMerchantTotal(lastWeekQ));
        result.setThisWeek(tenantDayStaticMapper.selectSupportMerchantTotal(thisWeekQ));
        return result;
    }

    @Override
    @TenantIgnore
    public List<VendorTotalRespDto> getSupportVendorTotal(VendorTotalReqDto reqVO) {

        QueryWrapper<Object> queryWrapperX = new QueryWrapper<>()
                .eq(reqVO.getCategoryCode()!=null,"d.code", reqVO.getCategoryCode())
                .eq(reqVO.getVendorCode()!=null," a.vendor_code", reqVO.getVendorCode())
                .eq(reqVO.getTid()!=null,"c.tid", reqVO.getTid())
                .eq(reqVO.getMerchantCode()!=null,"a.tenant_code", reqVO.getMerchantCode())
                .ge(reqVO.getStartTime()!=null,"a.days", reqVO.getStartTime())
                .le(reqVO.getEndTime()!=null,"a.days", reqVO.getEndTime())
                .isNotNull("c.tid")
                .eq(ObjectUtil.isNotEmpty(CurrencyContextHolder.getCurrency()), "a.currency", CurrencyContextHolder.getCurrency())
                ;

        List<VendorTotalRespDto.VendorCount> supportVendorStaticRespVO = tenantDayStaticMapper.selectVendorDetailTotal(queryWrapperX);
        if(ObjectUtil.isEmpty( supportVendorStaticRespVO)){
            return new ArrayList<>();
        }

        Map<String, List<VendorTotalRespDto.VendorCount>> groupedMap = supportVendorStaticRespVO.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getMerchantCode() + "_" + item.getCurrency(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 2. 遍历分组，计算各组的总和并组装 VendorTotalRespDto
        return groupedMap.values().stream().map(groupList -> {
            VendorTotalRespDto.VendorCount firstItem = groupList.get(0);

            // 累加计算该商户+币种下的总额
            BigDecimal totalBetAmount = groupList.stream()
                    .map(VendorTotalRespDto.VendorCount::getBetAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalWinLoss = groupList.stream()
                    .map(VendorTotalRespDto.VendorCount::getWinLoss)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExchangeAmount = groupList.stream()
                    .map(VendorTotalRespDto.VendorCount::getExchangeAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 构造外层汇总 DTO
            VendorTotalRespDto dto = new VendorTotalRespDto();
            dto.setMerchantCode(firstItem.getMerchantCode());
            dto.setCurrency(firstItem.getCurrency());
            dto.setBetAmount(totalBetAmount);
            dto.setWinLoss(totalWinLoss);
            dto.setExchangeAmount(totalExchangeAmount);
            dto.setList(groupList);

            return dto;
        }).collect(Collectors.toList());


    }


    @Override
    public List<VendorTotalRespDto> getVendorMerchantTotal(VendorTotalReqDto reqVO) {

        QueryWrapper<Object> queryWrapperX = new QueryWrapper<>()
                .eq(reqVO.getCategoryCode()!=null,"d.code", reqVO.getCategoryCode())
                .eq(reqVO.getVendorCode()!=null," b.vendor_code", reqVO.getVendorCode())
                .eq(reqVO.getTid()!=null,"c.tid", reqVO.getTid())
                .eq(reqVO.getMerchantCode()!=null,"a.tenant_code", reqVO.getMerchantCode())
                .ge(reqVO.getStartTime()!=null,"a.days", reqVO.getStartTime())
                .le(reqVO.getEndTime()!=null,"a.days", reqVO.getEndTime())
                .eq(ObjectUtil.isNotEmpty(CurrencyContextHolder.getCurrency()), "a.currency", CurrencyContextHolder.getCurrency())
                ;

        List<TenantDayStaticMapper.VendorMerchantDetailDto> data = tenantDayStaticMapper.selectVendorMerchantDetailTotal(queryWrapperX);
        if(ObjectUtil.isEmpty(data)){
            return new ArrayList<>();
        }
        record MerchantCurrencyKey(String merchantCode, String currency) {}

        // 3. 按 (merchantCode + currency) 进行双字段分组与汇总
        return data.stream()
                .collect(Collectors.groupingBy(item -> new MerchantCurrencyKey(item.getMerchantCode(), item.getCurrency())))
                .entrySet().stream()
                .map(entry -> {
                    MerchantCurrencyKey key = entry.getKey();
                    List<TenantDayStaticMapper.VendorMerchantDetailDto> details = entry.getValue();

                    // 汇总该 (商户 + 币种) 下的总投注额、总输赢、总汇算金额
                    BigDecimal totalBetAmount = details.stream()
                            .map(TenantDayStaticMapper.VendorMerchantDetailDto::getBetAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalWinLoss = details.stream()
                            .map(TenantDayStaticMapper.VendorMerchantDetailDto::getWinLoss)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);



                    // 映射场馆明细列表（SQL 已保证 vendorCode 唯一，无需二次分组）
                    List<VendorTotalRespDto.VendorCount> vendorCounts = details.stream().map(detail -> {
                        VendorTotalRespDto.VendorCount count = new VendorTotalRespDto.VendorCount();
                        count.setMerchantCode(detail.getMerchantCode());
                        count.setVendorCode(detail.getVendorCode());
                        count.setBetAmount(detail.getBetAmount());
                        count.setWinLoss(detail.getWinLoss());
                        count.setExchangeAmount(detail.getWinLoss());
                        return count;
                    }).toList();
                    String country = details.get(0).getCountry();

                    // 组装外层商户按币种汇总对象
                    VendorTotalRespDto resp = new VendorTotalRespDto();
                    resp.setMerchantCode(key.merchantCode());
                    resp.setCurrency(key.currency());
                    resp.setCountry(country);
                    resp.setBetAmount(totalBetAmount);
                    resp.setWinLoss(totalWinLoss);
                    resp.setExchangeAmount(totalWinLoss);
                    resp.setList(vendorCounts);

                    return resp;
                }).toList();
    }

    @Override
    public List<String> getCurrencies(String start, String end, Integer tid, String merchantCode) {
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>()
                .ge("a.days",start)
                .le("a.days", end)
                .eq(tid!=null, "b.tid", tid)
                .eq(merchantCode!=null,"a.tenant_code", merchantCode);

       return tenantDayStaticMapper.selectCurrency(queryWrapper);
    }

}