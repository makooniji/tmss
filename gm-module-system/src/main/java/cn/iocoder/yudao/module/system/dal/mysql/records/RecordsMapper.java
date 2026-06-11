package cn.iocoder.yudao.module.system.dal.mysql.records;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.RecordsPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.RecordsTotalRespVO;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.VendorTotalRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户游戏记录 Mapper
 *
 * @author osca
 */
@Mapper
public interface RecordsMapper extends BaseMapperX<GameRecordsDO> {

    default PageResult<GameRecordsDO> selectPage(RecordsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GameRecordsDO>()
                .eqIfPresent(GameRecordsDO::getUserId, reqVO.getUserId())
                .eqIfPresent(GameRecordsDO::getAction, reqVO.getAction())
                .eqIfPresent(GameRecordsDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(GameRecordsDO::getRefOrderNo, reqVO.getRefOrderNo())
                .eqIfPresent(GameRecordsDO::getRoomNo, reqVO.getRoomNo())
                .eqIfPresent(GameRecordsDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(GameRecordsDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(GameRecordsDO::getGameCode, reqVO.getGameCode())
                .eqIfPresent(GameRecordsDO::getTenantId, reqVO.getTenantId())
                .betweenIfPresent(GameRecordsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GameRecordsDO::getId));
    }

    /**
     * 查询游戏数据总报表-维度租户
     *
     * @param page
     * @param wrapper
     * @return
     */
    @Select("""
                SELECT
                    tenant_code,
                    vendor_code,
                    tenant_name,
                    vendor_name,
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS winAmount,
                    SUM(win_loss) as winLoss,
                    SUM(pay_amount/bet_amount) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
                    GROUP BY tenant_code, vendor_code, tenant_name,vendor_name
            """)

    IPage<RecordsTotalRespVO> selectRecordsTotalPage(IPage<?> page, @Param("ew") Wrapper<GameRecordsDO> wrapper);

    @Select("""
                SELECT
                    vendor_code,
                    COUNT(*) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(win_amount) AS winAmount,
                    SUM(win_loss) as winLoss,
                    SUM(win_amount/bet_amount) as rtp,
                    AVG(bet_amount) as avgBetAmount
                    FROM game_records
                    ${ew.getCustomSqlSegment}
                    GROUP BY vendor_code
            """)
    VendorTotalRespVO getVendorDataTotal(@Param("ew") Wrapper<GameRecordsDO> wrapper);
}