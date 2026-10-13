package cn.iocoder.yudao.module.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.system.model.GameRecordsDO;
import cn.iocoder.yudao.module.system.model.TenantDayStaticDO;
import cn.iocoder.yudao.module.system.model.vo.*;
import cn.iocoder.yudao.module.system.records.RecordsMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.RECORDS_NOT_EXISTS;


/**
 * 用户游戏记录 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class GameRecordsServiceImpl implements GameRecordsService {

    @Resource
    private RecordsMapper recordsMapper;
    @Resource
    private TenantService tenantService;

    @Override
    public Long createRecords(RecordsSaveReqVO createReqVO) {
        // 插入
        GameRecordsDO records = BeanUtils.toBean(createReqVO, GameRecordsDO.class);
        recordsMapper.insert(records);

        // 返回
        return records.getId();
    }

    @Override
    public void updateRecords(RecordsSaveReqVO updateReqVO) {
        // 校验存在
        validateRecordsExists(updateReqVO.getId());
        // 更新
        GameRecordsDO updateObj = BeanUtils.toBean(updateReqVO, GameRecordsDO.class);
        recordsMapper.updateById(updateObj);
    }

    @Override
    public void deleteRecords(Long id) {
        // 校验存在
        validateRecordsExists(id);
        // 删除
        recordsMapper.deleteById(id);
    }

    @Override
        public void deleteRecordsListByIds(List<Long> ids) {
        // 删除
        recordsMapper.deleteByIds(ids);
        }


    private void validateRecordsExists(Long id) {
        if (recordsMapper.selectById(id) == null) {
            throw exception(RECORDS_NOT_EXISTS);
        }
    }

    @Override
    public GameRecordsDO getRecords(Long id) {
        return recordsMapper.selectById(id);
    }

    @Override
    public PageResult<GameRecordsDO> getRecordsPage(RecordsPageReqVO pageReqVO) {
        tenantService.resolveQueryTenantIds(pageReqVO.getTenantId());
        LambdaQueryWrapperX<GameRecordsDO> wrapper = new LambdaQueryWrapperX<GameRecordsDO>()
                .eqIfPresent(GameRecordsDO::getUserId, pageReqVO.getUserId())
                .likeIfPresent(GameRecordsDO::getUsername, pageReqVO.getUsername())
                .eqIfPresent(GameRecordsDO::getAction, pageReqVO.getAction())
                .eqIfPresent(GameRecordsDO::getOrderNo, pageReqVO.getOrderNo())
                .eqIfPresent(GameRecordsDO::getRefOrderNo, pageReqVO.getRefOrderNo())
                .eqIfPresent(GameRecordsDO::getRoomNo, pageReqVO.getRoomNo())
                .eqIfPresent(GameRecordsDO::getIssueNo, pageReqVO.getIssueNo())
                .eqIfPresent(GameRecordsDO::getVendorCode, pageReqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getGameCode, pageReqVO.getGameCode())
                .eqIfPresent(GameRecordsDO::getGameKind, pageReqVO.getGameKind())
                .eqIfPresent(GameRecordsDO::getTenantCode, pageReqVO.getTenantCode())
                .betweenIfPresent(GameRecordsDO::getCreateTime, pageReqVO.getCreateTime())
                .geIfPresent(GameRecordsDO::getCreateTime, pageReqVO.getDays())
                .leIfPresent(GameRecordsDO::getCreateTime, pageReqVO.getEndDays())
                .orderByDesc(GameRecordsDO::getId);

        return recordsMapper.selectPage(pageReqVO, wrapper);
    }

    @Override
    public PageResult<GameRecordsDO> getRecordsPageByUpdate(RecordsPageReqVO pageReqVO) {
        tenantService.resolveQueryTenantIds(pageReqVO.getTenantId());
        LambdaQueryWrapperX<GameRecordsDO> wrapper = new LambdaQueryWrapperX<GameRecordsDO>()
                .eqIfPresent(GameRecordsDO::getUserId, pageReqVO.getUserId())
                .likeIfPresent(GameRecordsDO::getUsername, pageReqVO.getUsername())
                .eqIfPresent(GameRecordsDO::getAction, pageReqVO.getAction())
                .eqIfPresent(GameRecordsDO::getOrderNo, pageReqVO.getOrderNo())
                .eqIfPresent(GameRecordsDO::getRefOrderNo, pageReqVO.getRefOrderNo())
                .eqIfPresent(GameRecordsDO::getRoomNo, pageReqVO.getRoomNo())
                .eqIfPresent(GameRecordsDO::getIssueNo, pageReqVO.getIssueNo())
                .eqIfPresent(GameRecordsDO::getVendorCode, pageReqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getGameCode, pageReqVO.getGameCode())
                .eqIfPresent(GameRecordsDO::getGameKind, pageReqVO.getGameKind())
                .eqIfPresent(GameRecordsDO::getTenantCode, pageReqVO.getTenantCode())
                .betweenIfPresent(GameRecordsDO::getUpdateTime, pageReqVO.getCreateTime())
                .geIfPresent(GameRecordsDO::getUpdateTime, pageReqVO.getDays())
                .leIfPresent(GameRecordsDO::getUpdateTime, pageReqVO.getEndDays())
                .orderByDesc(GameRecordsDO::getId);

        return recordsMapper.selectPage(pageReqVO, wrapper);
    }


    @Override
    public Boolean exists(String transferId) {

        return recordsMapper.exists(Wrappers.<GameRecordsDO>lambdaQuery().eq(GameRecordsDO::getTraceId,transferId));
    }

    @Override
    public GameRecordsDO getRecordsByOrderId(String orderId) {
        return recordsMapper.selectOne("orderNo",orderId);
    }

    @Override
    public void update(Wrapper<GameRecordsDO> set) {
        recordsMapper.update(set);
    }

    @Override
    public PageResult<RecordsTotalRespVO> getRecordsTotalPage(RecordsTotalPageReqVO reqVO) {
        // game_tenant_day_static 非 TenantBaseDO，拦截器常忽略；自定义 SQL 必须显式 IN
        LambdaQueryWrapperX<GameRecordsDO> gameRecordsDOLambdaQueryWrapperX = new LambdaQueryWrapperX<GameRecordsDO>()
                .inIfPresent(GameRecordsDO::getTenantId, tenantService.getVisibleTenantIds())
                .eqIfPresent(GameRecordsDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getTenantCode, reqVO.getTenantCode())
                .geIfPresent(GameRecordsDO::getCreateTime, reqVO.getDays())
                .leIfPresent(GameRecordsDO::getCreateTime, reqVO.getEndDays());
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO,null);
        IPage<RecordsTotalRespVO> recordsTotalRespVOIPage = recordsMapper.selectRecordsTotalPage(mpPage,gameRecordsDOLambdaQueryWrapperX);
        return new PageResult<>(recordsTotalRespVOIPage.getRecords(), recordsTotalRespVOIPage.getTotal());
    }

    @Override
    public VendorTotalRespVO getVendorDataTotal(RecordsTotalReqVO reqVO) {
        tenantService.resolveQueryTenantIds(reqVO.getTenantId());
        LambdaQueryWrapperX<GameRecordsDO> ew = new LambdaQueryWrapperX<GameRecordsDO>()
                .eqIfPresent(GameRecordsDO::getUserId, reqVO.getUserId())
                .likeIfPresent(GameRecordsDO::getUsername, reqVO.getUsername())
                .eqIfPresent(GameRecordsDO::getAction, reqVO.getAction())
                .eqIfPresent(GameRecordsDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(GameRecordsDO::getRefOrderNo, reqVO.getRefOrderNo())
                .eqIfPresent(GameRecordsDO::getRoomNo, reqVO.getRoomNo())
                .eqIfPresent(GameRecordsDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(GameRecordsDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getGameCode, reqVO.getGameCode())
                .eqIfPresent(GameRecordsDO::getGameKind, reqVO.getGameKind())
                .eqIfPresent(GameRecordsDO::getTenantCode, reqVO.getTenantCode())
                .betweenIfPresent(GameRecordsDO::getCreateTime, reqVO.getCreateTime());
        return recordsMapper.getVendorDataTotal(ew);
    }

}