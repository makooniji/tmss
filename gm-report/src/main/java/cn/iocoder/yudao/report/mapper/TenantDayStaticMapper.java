package cn.iocoder.yudao.report.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.report.controller.api.dto.*;
import cn.iocoder.yudao.report.controller.vo.*;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 租户游戏日报 Mapper
 *
 * @author osca
 */
@Mapper
public interface TenantDayStaticMapper extends BaseMapperX<TenantDayStaticDO> {

    default PageResult<TenantDayStaticDO> selectPage(TenantDayStaticPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TenantDayStaticDO>()
                .eqIfPresent(TenantDayStaticDO::getDays, reqVO.getDays())
                .eqIfPresent(TenantDayStaticDO::getTenantCode, reqVO.getTenantCode())
                .likeIfPresent(TenantDayStaticDO::getTenantName, reqVO.getTenantName())
                .eqIfPresent(TenantDayStaticDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(TenantDayStaticDO::getVendorName, reqVO.getVendorName())
                .orderByDesc(TenantDayStaticDO::getId));
    }

    @Insert("""
            INSERT INTO game_tenant_day_static (
                days, tenant_code, tenant_name, vendor_code, vendor_name,
                game_code, game_name, bet_count, bet_amount, pay_amount,
                user_count, user_bet_count, user_win_count
            ) VALUES (
                #{days}, #{tenantCode}, #{tenantName}, #{vendorCode}, #{vendorName},
                #{gameCode}, #{gameName}, #{betCount}, #{betAmount}, #{payAmount},
                #{userCount}, #{userBetCount}, #{userWinCount}
            ) ON DUPLICATE KEY UPDATE
                bet_count = IFNULL(bet_count, 0) + VALUES(bet_count),
                bet_amount = IFNULL(bet_amount, 0) + VALUES(bet_amount),
                pay_amount = IFNULL(pay_amount, 0) + VALUES(pay_amount),
                user_count = IFNULL(user_count, 0) + VALUES(user_count),
                user_bet_count = IFNULL(user_bet_count, 0) + VALUES(user_bet_count),
                user_win_count = IFNULL(user_win_count, 0) + VALUES(user_win_count),
                rtp = CASE
                    WHEN (IFNULL(bet_amount, 0) + VALUES(bet_amount)) = 0 THEN 0
                    ELSE ROUND((IFNULL(pay_amount, 0) + VALUES(pay_amount)) / (IFNULL(bet_amount, 0) + VALUES(bet_amount)), 6)
                END;
            """)
    Integer upsertStatic(TenantDayStaticDO tenantDayStaticDO);

    @Insert("""
            <script>
            INSERT INTO game_tenant_day_static (
                days, tenant_code, tenant_name, vendor_code, vendor_name,
                game_code, game_name, bet_count, bet_amount, pay_amount,
                user_count, user_bet_count, user_win_count, win_loss, tenant_id, currency
            ) VALUES
            <foreach collection="list" item="item" separator=",">
            (
                #{item.days}, #{item.tenantCode}, #{item.tenantName}, #{item.vendorCode}, #{item.vendorName},
                #{item.gameCode}, #{item.gameName}, #{item.betCount}, #{item.betAmount}, #{item.payAmount},
                #{item.userCount}, #{item.userBetCount}, #{item.userWinCount}, #{item.winLoss}, #{item.tenantId}, #{item.currency}
            )
            </foreach>
            ON DUPLICATE KEY UPDATE
                bet_count = IFNULL(bet_count, 0) + VALUES(bet_count),
                bet_amount = IFNULL(bet_amount, 0) + VALUES(bet_amount),
                pay_amount = IFNULL(pay_amount, 0) + VALUES(pay_amount),
                user_count = IFNULL(user_count, 0) + VALUES(user_count),
                user_bet_count = IFNULL(user_bet_count, 0) + VALUES(user_bet_count),
                user_win_count = IFNULL(user_win_count, 0) + VALUES(user_win_count),
                win_loss = IFNULL(win_loss, 0) + VALUES(win_loss),
                rtp = CASE
                    WHEN (IFNULL(bet_amount, 0) + VALUES(bet_amount)) = 0 THEN 0
                    ELSE ROUND((IFNULL(pay_amount, 0) + VALUES(pay_amount)) / (IFNULL(bet_amount, 0) + VALUES(bet_amount)), 6)
                END
            </script>
            """)
    int batchUpsert(@Param("list") List<TenantDayStaticDO> list);

    @Insert("""
            <script>
            INSERT INTO game_user_day_static (
                days, tenant_code, tenant_name, vendor_code, vendor_name,
                game_code, game_name, user_id, username, bet_count, bet_amount, 
                pay_amount, win_loss, tenant_id, currency
            ) VALUES
            <foreach collection="list" item="item" separator=",">
            (
                #{item.days}, #{item.tenantCode}, #{item.tenantName}, #{item.vendorCode}, #{item.vendorName},
                #{item.gameCode}, #{item.gameName}, #{item.userId}, #{item.username}, #{item.betCount}, #{item.betAmount}, 
                #{item.payAmount}, #{item.winLoss}, #{item.tenantId}, #{item.currency}
            )
            </foreach>
            ON DUPLICATE KEY UPDATE
                bet_count = IFNULL(bet_count, 0) + VALUES(bet_count),
                bet_amount = IFNULL(bet_amount, 0) + VALUES(bet_amount),
                pay_amount = IFNULL(pay_amount, 0) + VALUES(pay_amount),
                win_loss = IFNULL(win_loss, 0) + VALUES(win_loss),
                rtp = CASE
                    WHEN (IFNULL(bet_amount, 0) + VALUES(bet_amount)) = 0 THEN 0
                    ELSE ROUND((IFNULL(pay_amount, 0) + VALUES(pay_amount)) / (IFNULL(bet_amount, 0) + VALUES(bet_amount)), 6)
                END
            </script>
            """)
    int batchUserUpsert(@Param("list") List<TenantUserDayStaticDO> list);

    /**
     * 查询游戏数据总报表-维度游戏
     */
    @Select("""
            SELECT
                tenant_code,
                tenant_name,
                vendor_code,
                vendor_name,
                game_code,
                game_name,
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                SUM(win_loss) AS winLoss,
                COUNT(DISTINCT username) AS userCount,
                COUNT(DISTINCT CASE WHEN win_loss > 0 THEN username END) AS userWinCount,
                IFNULL(SUM(pay_amount) / NULLIF(SUM(bet_amount), 0), 0) AS rtp
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY tenant_code, tenant_name, vendor_code, vendor_name, game_code, game_name
            """)
    IPage<TenantDayStaticDO> selectGameTotalPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总报表-维度租户
     */
    @Select("""
            SELECT
                t.tenant_id,
                MAX(t.tenant_code) AS tenant_code,
                MAX(t.tenant_name) AS tenant_name,
                SUM(t.total_bet_count) AS betCount,
                SUM(t.total_bet_amount) AS betAmount,
                SUM(t.total_pay_amount) AS payAmount,
                SUM(t.total_win_loss) AS winLoss,
                COUNT(t.username) AS userCount,
                COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
                COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
                IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
            FROM (
                SELECT
                    tenant_id,
                    MAX(tenant_code) AS tenant_code,
                    MAX(tenant_name) AS tenant_name,
                    username,
                    SUM(bet_count) AS total_bet_count,
                    SUM(bet_amount) AS total_bet_amount,
                    SUM(pay_amount) AS total_pay_amount,
                    SUM(win_loss) AS total_win_loss
                FROM game_user_day_static
                ${ew.customSqlSegment}
                GROUP BY tenant_id, username
            ) t
            GROUP BY t.tenant_id
            """)
    IPage<TenantDayStaticDO> selectTenantPageTotal(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
            SELECT
                c.name AS merchantName,
                c.code AS merchantId,
                SUM(CASE WHEN b.cate_id = 1 THEN a.bet_amount ELSE 0 END) AS slot,    
                SUM(CASE WHEN b.cate_id = 2 THEN a.bet_amount ELSE 0 END) AS live,    
                SUM(CASE WHEN b.cate_id = 3 THEN a.bet_amount ELSE 0 END) AS chess,   
                SUM(CASE WHEN b.cate_id = 4 THEN a.bet_amount ELSE 0 END) AS fishing, 
                SUM(CASE WHEN b.cate_id = 5 THEN a.bet_amount ELSE 0 END) AS sport,   
                SUM(CASE WHEN b.cate_id = 6 THEN a.bet_amount ELSE 0 END) AS lottery, 
                c.tid AS tenantId,
                MAX(c.create_time) AS createTime
            FROM system_tenant c
            LEFT JOIN game_user_day_static a ON c.id = a.tenant_id 
            LEFT JOIN game_info b ON a.game_code = b.game_code
            ${ew.getCustomSqlSegment}
            GROUP BY c.id, c.name, c.code, c.tid
            ORDER BY createTime DESC
            """)
    IPage<TenantStaticRespDto> selectSupportTenantPageTotal(IPage<?> page, @Param("ew") Wrapper<Object> wrapper);

    @Select("""
            SELECT
                SUM(CASE WHEN b.cate_id = 1 THEN a.bet_amount ELSE 0 END) AS slot,   
                SUM(CASE WHEN b.cate_id = 2 THEN a.bet_amount ELSE 0 END) AS live,   
                SUM(CASE WHEN b.cate_id = 3 THEN a.bet_amount ELSE 0 END) AS chess,  
                SUM(CASE WHEN b.cate_id = 4 THEN a.bet_amount ELSE 0 END) AS fishing,
                SUM(CASE WHEN b.cate_id = 5 THEN a.bet_amount ELSE 0 END) AS sport,  
                SUM(CASE WHEN b.cate_id = 6 THEN a.bet_amount ELSE 0 END) AS lottery 
            FROM game_user_day_static a 
            LEFT JOIN game_info b ON a.game_code = b.game_code
            LEFT JOIN system_tenant c ON a.tenant_id = c.id
            ${ew.getCustomSqlSegment}
            """)
    TenantStaticRespDto selectSupportTenantTotal(@Param("ew") Wrapper<Object> wrapper);

    @Select("""
            SELECT
                SUM(a.bet_amount) AS betAmount,   
                SUM(a.pay_amount) AS payAmount,   
                SUM(a.win_loss) AS winLoss,
                COUNT(DISTINCT a.user_id) AS activeUserCount,
                COUNT(DISTINCT CASE WHEN c.status = 0 THEN a.tenant_id END) AS merchantCount,
                COUNT(DISTINCT CASE WHEN c.operate_status = 0 AND c.status = 0 THEN a.tenant_id END) AS runningCount
            FROM system_tenant c 
            LEFT JOIN game_user_day_static a ON c.id = a.tenant_id
            ${ew.getCustomSqlSegment}
            """)
    MerchantTotalRespDto.CountData selectSupportMerchantTotal(@Param("ew") Wrapper<Object> wrapper);

    @Select("""
           SELECT a.currency FROM game_user_day_static a left join system_tenant b on a.tenant_id=b.id
                    ${ew.getCustomSqlSegment}
                    group by  currency
            """)
    List<String> selectCurrency(@Param("ew") Wrapper<Object> wrapper);

    @Select("""
            SELECT
                d.code AS categoryCode,
                b.vendor_code AS vendorCode,
                SUM(a.bet_amount) AS betAmount,
                MAX(a.days) AS createTime
            FROM game_user_day_static a 
            LEFT JOIN game_info b ON a.game_code = b.game_code
            LEFT JOIN system_tenant c ON a.tenant_id = c.id
            LEFT JOIN game_category d ON b.cate_id = d.id
            ${ew.getCustomSqlSegment}
            GROUP BY d.code, b.vendor_code
            """)
    IPage<VendorStaticRespDto> selectSupportVendorPageTotal(IPage<?> page, @Param("ew") Wrapper<Object> wrapper);

    @Data
    class VendorMerchantDetailDto {
        private String merchantCode;
        private String vendorCode;
        private BigDecimal betAmount;
        private BigDecimal winLoss;
        private BigDecimal usdtAmount;
        private String currency;
        private String country;
    }

    @Select("""
            SELECT
                a.tenant_code AS merchantCode,
                b.vendor_code AS vendorCode,
                SUM(a.bet_amount) AS betAmount,
                SUM(a.win_loss) AS winLoss,
                SUM(a.win_loss) AS exchangeAmount,
                a.currency,
                MAX(c.country) AS country
            FROM game_user_day_static a 
            LEFT JOIN game_info b ON a.game_code = b.game_code
            LEFT JOIN system_tenant c ON a.tenant_id = c.id
            LEFT JOIN game_category d ON b.cate_id = d.id
            ${ew.getCustomSqlSegment}
            GROUP BY a.tenant_code, b.vendor_code, a.currency
            """)
    List<VendorMerchantDetailDto> selectVendorMerchantDetailTotal(@Param("ew") Wrapper<Object> wrapper);

    @Select("""
            SELECT
                a.tenant_code AS merchantCode,
                a.vendor_code AS vendorCode,
                a.currency AS currency,
                SUM(a.bet_amount) AS betAmount,
                SUM(a.win_loss) AS winLoss,
                SUM(a.win_loss) AS exchangeAmount
            FROM game_vendor e 
            LEFT JOIN game_user_day_static a ON e.vendor_code = a.vendor_code
            LEFT JOIN game_info b ON a.game_code = b.game_code    
            INNER JOIN system_tenant c ON a.tenant_id = c.id
            LEFT JOIN game_category d ON b.cate_id = d.id
            ${ew.getCustomSqlSegment}
            GROUP BY a.tenant_code, a.vendor_code, a.currency
            """)
    List<VendorTotalRespDto.VendorCount> selectVendorDetailTotal(@Param("ew") Wrapper<Object> wrapper);

    @Select("""
            SELECT
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                COUNT(DISTINCT username) AS userCount,
                COUNT(DISTINCT CASE WHEN bet_amount > 0 THEN username END) AS userBetCount,
                COUNT(DISTINCT CASE WHEN win_loss > 0 THEN username END) AS userWinCount,
                SUM(win_loss) AS winLoss,
                IFNULL(SUM(pay_amount) / NULLIF(SUM(bet_amount), 0), 0) AS rtp
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            """)
    TenantDayStaticDO selectTenantTotal(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询厂商维度报表分页
     */
    @Select("""
        SELECT
            t.vendor_code AS vendor_code,
            MAX(t.vendor_name) AS vendor_name,
            SUM(t.total_bet_count) AS betCount,
            SUM(t.total_bet_amount) AS betAmount,
            SUM(t.total_pay_amount) AS payAmount,
            COUNT(t.username) AS userCount,
            COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
            COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
            IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp,
            SUM(t.total_win_loss) AS winLoss
        FROM (
            SELECT
                vendor_code,
                MAX(vendor_name) AS vendor_name,
                username,
                SUM(bet_count) AS total_bet_count,
                SUM(bet_amount) AS total_bet_amount,
                SUM(pay_amount) AS total_pay_amount,
                SUM(win_loss) AS total_win_loss
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY vendor_code, username
        ) t
        GROUP BY t.vendor_code
        """)
    IPage<VendorDayStaticRespVO> selectVendorStaticPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);


    @Select("""
    SELECT
        t.currency AS currency,
        t.vendor_code AS vendor_code,
        MAX(t.vendor_name) AS vendor_name,
        SUM(t.total_bet_count) AS betCount,
        SUM(t.total_bet_amount) AS betAmount,
        SUM(t.total_pay_amount) AS payAmount,
        COUNT(t.username) AS userCount,
        COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
        COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
        IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp,
        SUM(t.total_win_loss) AS winLoss
    FROM (
        SELECT
            currency,
            vendor_code,
            MAX(vendor_name) AS vendor_name,
            username,
            SUM(bet_count) AS total_bet_count,
            SUM(bet_amount) AS total_bet_amount,
            SUM(pay_amount) AS total_pay_amount,
            SUM(win_loss) AS total_win_loss
        FROM game_user_day_static
        ${ew.getCustomSqlSegment}
        GROUP BY currency, vendor_code, username
    ) t
    GROUP BY t.currency, t.vendor_code
    """)
    List<VendorDayStaticRespVO> selectVendorStatic(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询厂商 + 商户维度报表分页（包含子查询优化的双层写法）
     */
    @Select("""
            SELECT
                t.vendor_code,
                MAX(t.vendor_name) AS vendor_name,
                t.tenant_id,
                MAX(t.tenant_code) AS tenant_code,
                MAX(t.tenant_name) AS tenant_name,
                SUM(t.total_bet_count) AS betCount,
                SUM(t.total_bet_amount) AS betAmount,
                SUM(t.total_pay_amount) AS payAmount,
                SUM(t.total_win_loss) AS winLoss,
                COUNT(t.username) AS userCount,
                COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
                COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
                IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
            FROM (
                SELECT
                    vendor_code,
                    MAX(vendor_name) AS vendor_name,
                    tenant_id,
                    MAX(tenant_code) AS tenant_code,
                    MAX(tenant_name) AS tenant_name,
                    username,
                    SUM(bet_count) AS total_bet_count,
                    SUM(bet_amount) AS total_bet_amount,
                    SUM(pay_amount) AS total_pay_amount,
                    SUM(win_loss) AS total_win_loss
                FROM game_user_day_static
                ${ew.customSqlSegment}
                GROUP BY vendor_code, tenant_id, username
            ) t
            GROUP BY t.vendor_code, t.tenant_id
            """)
    IPage<VendorTenantDayStaticRespVO> selectVendorTenantStaticPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总计
     */
    @Select("""
        SELECT
            SUM(t.total_bet_count) AS betCount,
            SUM(t.total_bet_amount) AS betAmount,
            SUM(t.total_pay_amount) AS payAmount,
            COUNT(t.username) AS userCount,
            COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
            COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
            SUM(t.total_win_loss) AS winLoss,
            IFNULL(SUM(t.total_bet_amount) / NULLIF(COUNT(t.username), 0), 0) AS avgBetAmount,
            IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
        FROM (
            SELECT
                
                username AS username,
                SUM(bet_count) AS total_bet_count,
                SUM(bet_amount) AS total_bet_amount,
                SUM(pay_amount) AS total_pay_amount,
                SUM(win_loss) AS total_win_loss
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY username
        ) t
        """)
    GameStaticTotalVO selectTotalCount(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总报表-维度租户
     */
    @Select("""
            SELECT
                tenant_id,        
                tenant_code,
                tenant_name,
                vendor_code,
                vendor_name,
                game_code,
                game_name,
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                COUNT(DISTINCT username) AS userCount,
                COUNT(DISTINCT CASE WHEN bet_amount > 0 THEN username END) AS userBetCount,
                COUNT(DISTINCT CASE WHEN win_loss > 0 THEN username END) AS userWinCount,
                SUM(win_loss) AS winLoss,
                IFNULL(SUM(pay_amount) / NULLIF(SUM(bet_amount), 0), 0) AS rtp
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY tenant_id, tenant_code, tenant_name, vendor_code, vendor_name, game_code, game_name
            """)
    IPage<TenantDayStaticDO> selectTenantGameTotalPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总报表-维度租户（关联 game_info）
     */
    @Select("""
            SELECT
                a.tenant_id,
                a.tenant_code,
                a.tenant_name,
                a.vendor_code,
                COALESCE(g.vendor_name, a.vendor_name) AS vendor_name,
                a.game_code,
                COALESCE(g.game_name, a.game_name) AS game_name,
                g.cate_id AS cateId,
                SUM(a.bet_count) AS betCount,
                SUM(a.bet_amount) AS betAmount,
                SUM(a.pay_amount) AS payAmount,
                COUNT(DISTINCT a.username) AS userCount,
                COUNT(DISTINCT CASE WHEN a.bet_amount > 0 THEN a.username END) AS userBetCount,
                COUNT(DISTINCT CASE WHEN a.win_loss > 0 THEN a.username END) AS userWinCount,
                SUM(a.win_loss) AS winLoss
            FROM (
                SELECT *
                FROM game_user_day_static
                ${ew.getCustomSqlSegment}
            ) a
            LEFT JOIN game_info g ON g.vendor_code = a.vendor_code AND g.game_code = a.game_code
            GROUP BY a.tenant_id, a.tenant_code, a.tenant_name, a.vendor_code,
                     COALESCE(g.vendor_name, a.vendor_name), a.game_code,
                     COALESCE(g.game_name, a.game_name), g.cate_id
            """)
    IPage<TenantDayStaticRespVO> selectTenantGameTotalPageWithGameInfo(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
            SELECT 
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                COUNT(DISTINCT username) AS userCount,
                COUNT(DISTINCT CASE WHEN bet_amount > 0 THEN username END) AS userBetCount,
                COUNT(DISTINCT CASE WHEN win_loss > 0 THEN username END) AS userWinCount,
                SUM(win_loss) AS winLoss,
                IFNULL(SUM(pay_amount) / NULLIF(SUM(bet_amount), 0), 0) AS rtp
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            """)
    TenantDayStaticRespVO selectTenantGameTotalWithGameInfo(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
        SELECT
            SUM(t.total_bet_count) AS betCount,
            SUM(t.total_bet_amount) AS betAmount,
            SUM(t.total_pay_amount) AS payAmount,
            COUNT(t.username) AS userCount,
            COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
            COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
            SUM(t.total_win_loss) AS winLoss,
            IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
        FROM (
            SELECT
                username,
                SUM(bet_count) AS total_bet_count,
                SUM(bet_amount) AS total_bet_amount,
                SUM(pay_amount) AS total_pay_amount,
                SUM(win_loss) AS total_win_loss
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY username
        ) t
        """)
    TenantStaticTotalVO selectTenantCount(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
        SELECT
            t.game_code AS gameCode,
            MAX(t.game_name) AS gameName,
            SUM(t.total_bet_count) AS betCount,
            SUM(t.total_bet_amount) AS betAmount,
            SUM(t.total_pay_amount) AS payAmount,
            IFNULL(SUM(t.total_bet_amount) / NULLIF(SUM(t.total_bet_count), 0), 0) AS betAvg,
            COUNT(t.username) AS userCount,
            COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
            COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
            SUM(t.total_win_loss) AS winLoss,
            IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
        FROM (
            SELECT
                game_code,
                MAX(game_name) AS game_name,
                username,
                SUM(bet_count) AS total_bet_count,
                SUM(bet_amount) AS total_bet_amount,
                SUM(pay_amount) AS total_pay_amount,
                SUM(win_loss) AS total_win_loss
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY game_code, username
        ) t
        GROUP BY t.game_code
        ORDER BY betAvg DESC 
        LIMIT #{limit}
        """)
    Map<String, Object> selectGameTop(@Param("ew") Wrapper<TenantDayStaticDO> wrapper, @Param("limit") Integer limit);

    @Select("""
            <script>
            SELECT
                a.tenant_code AS tenantCode,
                a.tenant_name AS tenantName,
                COUNT(DISTINCT a.days) AS activeDays,
                SUM(a.bet_count) AS betCount,
                SUM(a.bet_amount) AS betAmount,
                SUM(a.pay_amount) AS payAmount,
                COUNT(DISTINCT CASE WHEN a.bet_amount > 0 THEN a.username END) AS userBetCount,
                COUNT(DISTINCT CASE WHEN a.win_loss > 0 THEN a.username END) AS userWinCount,
                COUNT(DISTINCT a.username) AS userCount,
                SUM(a.win_loss) AS winLoss,
                IFNULL(SUM(a.pay_amount) / NULLIF(SUM(a.bet_amount), 0), 0) AS rtp,
                b.status AS status,
                MAX(b.create_time) AS createTime,
                DATEDIFF(CURDATE(), MAX(b.create_time)) AS openDays
            FROM game_user_day_static a
            INNER JOIN system_tenant b ON a.tenant_code = b.code
            WHERE 1=1
                <if test="reqVO.days != null"> AND a.days &gt;= #{reqVO.days} </if>
                <if test="reqVO.endDays != null"> AND a.days &lt;= #{reqVO.endDays} </if>
                <if test="reqVO.status != null"> AND b.status = #{reqVO.status} </if>
                AND a.tenant_id &gt;= 1
            GROUP BY a.tenant_code, a.tenant_name, b.status
            ORDER BY
                <if test="reqVO.orderBy == 'time'"> MAX(a.days) DESC </if>
                <if test="reqVO.orderBy != 'time'"> SUM(a.bet_amount) DESC </if>
            LIMIT #{reqVO.limit}
            </script>
            """)
    List<Map<String, Object>> selectStaticChart(@Param("reqVO") TenantDayStaticReqVO reqVO);

    @Select("""
        <script>
        SELECT
            b.code AS tenantCode,
            b.name AS tenantName,
            DATEDIFF(CURRENT_DATE, b.create_time) AS openDays,
            IFNULL(SUM(t.total_bet_count), 0) AS betCount,
            IFNULL(SUM(t.total_bet_amount), 0) AS betAmount,
            IFNULL(SUM(t.total_pay_amount), 0) AS payAmount,
            IFNULL(COUNT(t.username), 0) AS userCount,
            IFNULL(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END), 0) AS userBetCount,
            IFNULL(COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END), 0) AS userWinCount,
            IFNULL(SUM(t.total_win_loss), 0) AS winLoss,
            IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp,
            IFNULL(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) / NULLIF(COUNT(t.username), 0), 0) AS userBetCountUserCount,
            IFNULL(SUM(t.total_bet_count) / NULLIF(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END), 0), 0) AS betCountUserBetCount
        FROM system_tenant b 
        LEFT JOIN (
            SELECT
                tenant_id,
                username,
                SUM(bet_count) AS total_bet_count,
                SUM(bet_amount) AS total_bet_amount,
                SUM(pay_amount) AS total_pay_amount,
                SUM(win_loss) AS total_win_loss
            FROM game_user_day_static
            WHERE 1=1
                <if test="reqVO.days != null"> AND days &gt;= #{reqVO.days} </if>
                <if test="reqVO.endDays != null"> AND days &lt;= #{reqVO.endDays} </if>
            GROUP BY tenant_id, username
        ) t ON t.tenant_id = b.id
        WHERE b.id > 1
            <if test="reqVO.status != null"> AND b.status = #{reqVO.status} </if>
            <if test="reqVO.operateStatus != null"> AND b.operate_status = #{reqVO.operateStatus} </if>
            <if test="reqVO.scopeIds != null and reqVO.scopeIds.size() > 0">
                AND b.id IN
                <foreach collection="reqVO.scopeIds" item="id" open="(" separator="," close=")">
                    #{id}
                </foreach>
            </if>
        GROUP BY b.id, b.code, b.name, b.create_time
        ORDER BY ${orderBy} DESC 
        LIMIT #{reqVO.limit}
        </script>
        """)
    List<Map<String, Object>> selectStaticChartDayInfo(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
            <script>
            SELECT
                b.code AS tenantCode,
                b.name AS tenantName,
                DATEDIFF(CURRENT_DATE, b.create_time) AS openDays
            FROM system_tenant b 
            WHERE b.id > 1
                <if test="reqVO.status != null"> AND b.status = #{reqVO.status} </if>
                <if test="reqVO.operateStatus != null"> AND b.operate_status = #{reqVO.operateStatus} </if>
            ORDER BY openDays DESC 
            LIMIT #{reqVO.limit}  
            </script>
            """)
    List<Map<String, Object>> selectStaticTenantDaysInfo(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
           
            <script>
                             SELECT
                                 t.game_code AS gameCode,
                                 MAX(t.game_name) AS gameName,
                                 SUM(t.total_bet_count) AS betCount,
                                 SUM(t.total_bet_amount) AS betAmount,
                                 SUM(t.total_pay_amount) AS payAmount,
                                 COUNT(t.user_id) AS userCount,
                                 COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.user_id END) AS userBetCount,
                                 COUNT(CASE WHEN t.total_win_loss > 0 THEN t.user_id END) AS userWinCount,
                                 SUM(t.total_win_loss) AS winLoss,
                                 IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp,
                                 IFNULL(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.user_id END) / NULLIF(COUNT(t.user_id), 0), 0) AS userBetCountUserCount,
                                 IFNULL(SUM(t.total_bet_count) / NULLIF(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.user_id END), 0), 0) AS betCountUserBetCount
                             FROM (
                                 SELECT
                                     game_code,
                                     MAX(game_name) AS game_name,
                                     user_id,
                                     MAX(username) AS username,
                                     SUM(bet_count) AS total_bet_count,
                                     SUM(bet_amount) AS total_bet_amount,
                                     SUM(pay_amount) AS total_pay_amount,
                                     SUM(win_loss) AS total_win_loss
                                 FROM game_user_day_static
                                 WHERE 1=1
                                     <if test="reqVO.days != null"> AND days &gt;= #{reqVO.days} </if>
                                     <if test="reqVO.endDays != null"> AND days &lt;= #{reqVO.endDays} </if>
                                 GROUP BY game_code, user_id
                             ) t
                             GROUP BY t.game_code
                             ORDER BY ${orderBy} DESC
                             </script>
            """)
    List<Map<String, Object>> selectChartGameTotal(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
          
            <script>
                  SELECT
                      t.days AS days,
                      SUM(t.total_bet_count) AS betCount,
                      SUM(t.total_bet_amount) AS betAmount,
                      SUM(t.total_pay_amount) AS payAmount,
                      COUNT(t.username) AS userCount,
                      COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
                      COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
                      SUM(t.total_win_loss) AS winLoss,
                      IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp,
                      IFNULL(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) / NULLIF(COUNT(t.username), 0), 0) AS userBetCountUserCount,
                      IFNULL(SUM(t.total_bet_count) / NULLIF(COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END), 0), 0) AS betCountUserBetCount
                  FROM (
                      SELECT
                          days,
                          username,
                          SUM(bet_count) AS total_bet_count,
                          SUM(bet_amount) AS total_bet_amount,
                          SUM(pay_amount) AS total_pay_amount,
                          SUM(win_loss) AS total_win_loss
                      FROM game_user_day_static
                      WHERE 1=1
                          <if test="reqVO.days != null"> AND days &gt;= #{reqVO.days} </if>
                          <if test="reqVO.endDays != null"> AND days &lt;= #{reqVO.endDays} </if>
                      GROUP BY days, username
                  ) t
                  GROUP BY t.days
                  ORDER BY ${orderBy} DESC
                  </script>
            """)
    List<Map<String, Object>> selectChartGameDayTotal(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
            
            SELECT
                        t.days,
                        SUM(t.total_bet_amount) AS betAmount,
                        SUM(t.total_pay_amount) AS payAmount,
                        COUNT(t.username) AS userCount,
                        COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
                        COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
                        SUM(t.total_win_loss) AS winLoss
                    FROM (
                        SELECT
                            a.days,
                            a.username,
                            SUM(a.bet_amount) AS total_bet_amount,
                            SUM(a.pay_amount) AS total_pay_amount,
                            SUM(a.win_loss) AS total_win_loss
                        FROM game_user_day_static a
                        INNER JOIN system_tenant b ON a.tenant_id = b.id
                        ${ew.getCustomSqlSegment}
                        GROUP BY a.days, a.username
                    ) t
                    GROUP BY t.days
                    ORDER BY t.days ASC
            
            """)
    List<Map<String, Object>> selectDayTotal(@Param("ew") Wrapper<?> wrapper);

    @Select("""
          
            SELECT
                      t.vendor_code,
                      MAX(t.vendor_name) AS vendor_name,
                      SUM(t.total_bet_count) AS betCount,
                      SUM(t.total_bet_amount) AS betAmount,
                      SUM(t.total_pay_amount) AS payAmount,
                      COUNT(t.username) AS userCount,
                      COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
                      COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
                      SUM(t.total_win_loss) AS winLoss,
                      IFNULL(SUM(t.total_bet_amount) / NULLIF(COUNT(t.username), 0), 0) AS avgBetAmount,
                      IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
                  FROM (
                      SELECT
                          vendor_code,
                          MAX(vendor_name) AS vendor_name,
                          username,
                          SUM(bet_count) AS total_bet_count,
                          SUM(bet_amount) AS total_bet_amount,
                          SUM(pay_amount) AS total_pay_amount,
                          SUM(win_loss) AS total_win_loss
                      FROM game_user_day_static
                      ${ew.getCustomSqlSegment}
                      GROUP BY vendor_code, username
                  ) t
                  GROUP BY t.vendor_code 
            """)
    VendorStaticTotalVO selectStaticVendorTotal(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
            SELECT
                        t.game_code,
                        MAX(t.game_name) AS game_name,
                        SUM(t.total_bet_count) AS betCount,
                        SUM(t.total_bet_amount) AS betAmount,
                        SUM(t.total_pay_amount) AS payAmount,
                        COUNT(t.username) AS userCount,
                        COUNT(CASE WHEN t.total_bet_amount > 0 THEN t.username END) AS userBetCount,
                        COUNT(CASE WHEN t.total_win_loss > 0 THEN t.username END) AS userWinCount,
                        SUM(t.total_win_loss) AS winLoss,
                        IFNULL(SUM(t.total_bet_amount) / NULLIF(COUNT(t.username), 0), 0) AS avgBetAmount,
                        IFNULL(SUM(t.total_pay_amount) / NULLIF(SUM(t.total_bet_amount), 0), 0) AS rtp
                    FROM (
                        SELECT
                            game_code,
                            MAX(game_name) AS game_name,
                            username,
                            SUM(bet_count) AS total_bet_count,
                            SUM(bet_amount) AS total_bet_amount,
                            SUM(pay_amount) AS total_pay_amount,
                            SUM(win_loss) AS total_win_loss
                        FROM game_user_day_static
                        ${ew.getCustomSqlSegment}
                        GROUP BY game_code, username
                    ) t
                    GROUP BY t.game_code
            """)
    TenantGameStaticTotalVO selectStaticGameTotal(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
            
                        SELECT
            
                    user_id,
                    username,
            
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
                    SUM(win_loss) AS winLoss,
                    IFNULL(SUM(pay_amount) / NULLIF(SUM(bet_amount), 0), 0) AS rtp
                    FROM game_user_day_static
                    ${ew.getCustomSqlSegment}
                    GROUP BY user_id,username
            
            
            """)
    IPage<TenantUserDayStaticDO> selectUserStaticPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);
}