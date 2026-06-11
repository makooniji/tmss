package cn.iocoder.yudao.module.system.service.records;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.TenantDayStaticDO;
import cn.iocoder.yudao.module.system.dal.mysql.records.RecordsMapper;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.RECORDS_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.TENANT_VISIBLE_FORBIDDEN;


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
        LambdaQueryWrapperX<GameRecordsDO> wrapper = new LambdaQueryWrapperX<GameRecordsDO>()
                .eqIfPresent(GameRecordsDO::getUserId, pageReqVO.getUserId())
                .eqIfPresent(GameRecordsDO::getAction, pageReqVO.getAction())
                .eqIfPresent(GameRecordsDO::getOrderNo, pageReqVO.getOrderNo())
                .eqIfPresent(GameRecordsDO::getRefOrderNo, pageReqVO.getRefOrderNo())
                .eqIfPresent(GameRecordsDO::getRoomNo, pageReqVO.getRoomNo())
                .eqIfPresent(GameRecordsDO::getIssueNo, pageReqVO.getIssueNo())
                .eqIfPresent(GameRecordsDO::getVendorCode, pageReqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getGameCode, pageReqVO.getGameCode())
                .inIfPresent(GameRecordsDO::getTenantId, tenantService.resolveQueryTenantIds(pageReqVO.getTenantId()))
                .betweenIfPresent(GameRecordsDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(GameRecordsDO::getId);
        return recordsMapper.selectPage(pageReqVO, wrapper);
    }

    /**
     * 系统租户：仅按请求 tenantId 过滤；商户/子代理：未传 tenantId 时自动 IN 可见范围（含子代理）
     */
    private void applyVisibleTenantScope(LambdaQueryWrapperX<GameRecordsDO> wrapper, Long tenantId) {
        List<Long> visibleIds = tenantService.getVisibleTenantIds();
        if (visibleIds == null) {
            wrapper.eqIfPresent(GameRecordsDO::getTenantId, tenantId);
            return;
        }
        if (tenantId != null) {
            if (!visibleIds.contains(tenantId)) {
                throw exception(TENANT_VISIBLE_FORBIDDEN);
            }
            wrapper.eq(GameRecordsDO::getTenantId, tenantId);
            return;
        }
        wrapper.in(GameRecordsDO::getTenantId, visibleIds);
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
        IPage<TenantDayStaticDO> mpPage = MyBatisUtils.buildPage(reqVO,null);
        LambdaQueryWrapperX<GameRecordsDO> gameRecordsDOLambdaQueryWrapperX = new LambdaQueryWrapperX<GameRecordsDO>()
                .inIfPresent(GameRecordsDO::getTenantId, tenantService.getVisibleTenantIds())
                .eqIfPresent(GameRecordsDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getTenantCode, reqVO.getTenantCode())
                .geIfPresent(GameRecordsDO::getCreateTime, reqVO.getDays())
                .leIfPresent(GameRecordsDO::getCreateTime, reqVO.getEndDays());
        IPage<RecordsTotalRespVO> recordsTotalRespVOIPage = recordsMapper.selectRecordsTotalPage(mpPage,gameRecordsDOLambdaQueryWrapperX);
        return new PageResult<>(recordsTotalRespVOIPage.getRecords(), recordsTotalRespVOIPage.getTotal());
    }

    @Override
    public VendorTotalRespVO getVendorDataTotal(RecordsTotalReqVO reqVO) {
        LambdaQueryWrapperX<GameRecordsDO> ew = new LambdaQueryWrapperX<GameRecordsDO>()

                .eqIfPresent(GameRecordsDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getTenantCode, reqVO.getTenantCode())

                .geIfPresent(GameRecordsDO::getCreateTime, reqVO.getDays())
                .leIfPresent(GameRecordsDO::getCreateTime, reqVO.getEndDays());
        return recordsMapper.getVendorDataTotal(ew);
    }

}