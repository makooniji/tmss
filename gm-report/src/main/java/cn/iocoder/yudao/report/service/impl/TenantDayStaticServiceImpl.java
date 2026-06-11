package cn.iocoder.yudao.report.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.mysql.info.GameInfoMapper;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.report.controller.game.vo.*;
import cn.iocoder.yudao.report.mapper.TenantDayStaticMapper;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import cn.iocoder.yudao.report.service.TenantDayStaticService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
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

    /**
     * 自定义 @Select 统计 SQL 需显式限制可见租户（系统租户 null=不限制，商户=自己+子代理）
     */
    private void applyVisibleTenantScope(LambdaQueryWrapperX<TenantDayStaticDO> wrapper) {
        wrapper.inIfPresent(TenantDayStaticDO::getTenantId, tenantService.getVisibleTenantIds());
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
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO,null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);

        IPage<TenantDayStaticDO> tenantDayStaticDOIPage = tenantDayStaticMapper.selectTenantTotal(mpPage, queryWrapperX);


        PageResult<TenantDayStaticDO> tenantDayStaticDOPageResult = new PageResult<>(tenantDayStaticDOIPage.getRecords(), tenantDayStaticDOIPage.getTotal());
        PageResult<TenantDayStaticRespVO> bean = BeanUtils.toBean(tenantDayStaticDOPageResult, TenantDayStaticRespVO.class);
        if(ObjectUtil.isEmpty(bean.getList())){
            return new PageResult<>();
        }
        List<Long> list = bean.getList().stream().map(TenantDayStaticRespVO::getTenantId).toList();
        // 查询租户信息
        List<TenantDO> tenantDOS = tenantMapper.selectByIds(list);
        bean.getList().forEach(item->{
            tenantDOS.stream().filter(p->p.getId().equals(item.getTenantId())).findFirst().ifPresent(item::setTenantDO);
        });
        return bean;
    }

    @Override
    public GameStaticTotalVO getGameStaticTotal(TenantDayStaticReqVO reqVO) {
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);
        return tenantDayStaticMapper.selectTotalCount(queryWrapperX);
    }

    @Override
    public PageResult<TenantDayStaticRespVO> getTenantGameStaticPage(TenantDayStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO,null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(TenantDayStaticDO::getGameCode, reqVO.getGameCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName());
        applyVisibleTenantScope(queryWrapperX);

        IPage<TenantDayStaticRespVO> gameTotalPage = tenantDayStaticMapper.selectTenantGameTotalPageWithGameInfo(mpPage, queryWrapperX);
        if(ObjectUtil.isEmpty( gameTotalPage.getRecords())){
            return new PageResult<>();
        }
        return new PageResult<>(gameTotalPage.getRecords(), gameTotalPage.getTotal());

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
                .eq(TenantDO::getStatus,reqVO.getStatus());
        //查询每天注册多少商户
        List<Map<String, Object>> dayCounts = tenantMapper.selectDayCounts(queryWrapperX1);

       return MapUtil.<String,Object>builder().put("dayCounts",dayCounts).put("dayData",dayData).build();
    }

    @Override
    public List<Map<String, Object>> getStaticDayInfo(TenantChartsReqVO reqVO) {
        TenantDayStaticReqVO.StatMetricEnum statMetricEnum = TenantDayStaticReqVO.StatMetricEnum.fromCode(reqVO.getOrderBy());
        return tenantDayStaticMapper.selectStaticChartDayInfo(reqVO,statMetricEnum.getFormula());
    }

    @Override
    public List<Map<String, Object>> getStaticDayTotal(@Valid TenantChartsReqVO pageReqVO) {
        return tenantDayStaticMapper.selectDayTotal(pageReqVO);
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
        List<Map<String, Object>> maps = tenantMapper.selectTenantStatus(wrapper);
        Map<String, Long> statusMap = maps.stream()
                .collect(Collectors.toMap(
                        m -> m.get("status").toString(),
                        m -> (Long) m.getOrDefault("counts", 0L),
                        (existing, replacement) -> existing // 解决重复 key 风险
                ));
        TenantStatusCountVO result=new TenantStatusCountVO();
        Long orDefault = statusMap.getOrDefault("0", 0L);
        result.setOpenCounts(orDefault);
        Long orDefault1 = statusMap.getOrDefault("2", 0L);
        result.setCloseCounts(orDefault1);
        Long orDefault2 = statusMap.getOrDefault("1", 0L);
        result.setOperCounts(orDefault2);
        return result;
    }

    @Override
    public PageResult<UserStaticRespVO> getUserStaticPage(UserStaticPageReqVO reqVO) {
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO, null);
        LambdaQueryWrapperX<TenantDayStaticDO> queryWrapperX = new LambdaQueryWrapperX<TenantDayStaticDO>()
                .geIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .leIfPresent(TenantDayStaticDO::getDays, reqVO.getEndDays())
                .eq(TenantDayStaticDO::getTenantId, reqVO.getTenantId());

        IPage<TenantUserDayStaticDO> tenantUserDayStaticDOIPage = tenantDayStaticMapper.selectUserStaticPage(mpPage, queryWrapperX);
        PageResult<TenantUserDayStaticDO> pageResult = new PageResult<>(tenantUserDayStaticDOIPage.getRecords(), tenantUserDayStaticDOIPage.getTotal());

        return BeanUtils.toBean(pageResult ,UserStaticRespVO.class);
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

}