package cn.iocoder.yudao.report.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.report.controller.game.vo.*;
import cn.iocoder.yudao.report.model.game.TenantDayStaticDO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
                                   days,
                                   tenant_code,
                                   tenant_name,
                                   vendor_code,
                                   vendor_name,
                                   game_code,
                                   game_name,
                                   bet_count,
                                   bet_amount,
                                   pay_amount,
                                   user_count,
                                   user_bet_count,
                                   user_win_count
                               )
                               VALUES (#{days}, #{tenantCode},#{tenantName}, #{vendorCode},#{vendorName}, #{gameCode},#{gameName},#{betCount}, #{betAmount}, #{payAmount},#{userCount}, 
                                       #{userBetCount}, #{userWonCount})
                               ON DUPLICATE KEY UPDATE
                                   bet_count = IFNULL(bet_count,0) + VALUES(bet_count),
                                   bet_amount = IFNULL(bet_amount,0) + VALUES(bet_amount),
                                   pay_amount = IFNULL(pay_amount,0) + VALUES(pay_amount),
                                   user_count = IFNULL(user_count,0) + VALUES(user_count),
                                   user_bet_count = IFNULL(user_bet_count,0) + VALUES(user_bet_count),
                                   user_win_count = IFNULL(user_win_count,0) + VALUES(user_win_count),
                                   rtp = CASE
                                       WHEN (IFNULL(bet_amount,0) + VALUES(bet_amount)) = 0 THEN 0
                                       ELSE ROUND((IFNULL(pay_amount,0) + VALUES(pay_amount)) / (IFNULL(bet_amount,0) + VALUES(bet_amount)),6)
                                   END;
            """)
    Integer upsertStatic(TenantDayStaticDO tenantDayStaticDO);

    @Insert("""
            <script>
            INSERT INTO game_tenant_day_static (
                       days, tenant_code, tenant_name, vendor_code, vendor_name,
                       game_code, game_name, bet_count, bet_amount, pay_amount,
                       user_count, user_bet_count, user_win_count,win_loss,tenant_id
                   )
                   VALUES
                   <foreach collection="list" item="item" separator=",">
                   (
                       #{item.days}, #{item.tenantCode}, #{item.tenantName}, #{item.vendorCode}, #{item.vendorName},
                       #{item.gameCode}, #{item.gameName}, #{item.betCount}, #{item.betAmount}, #{item.payAmount},
                       #{item.userCount}, #{item.userBetCount}, #{item.userWinCount},#{item.winLoss},#{item.tenantId}
                   )
                   </foreach>
                   ON DUPLICATE KEY UPDATE
                       bet_count = IFNULL(bet_count,0) + VALUES(bet_count),
                       bet_amount = IFNULL(bet_amount,0) + VALUES(bet_amount),
                       pay_amount = IFNULL(pay_amount,0) + VALUES(pay_amount),
                       user_count = IFNULL(user_count,0) + VALUES(user_count),
                       user_bet_count = IFNULL(user_bet_count,0) + VALUES(user_bet_count),
                       user_win_count =IFNULL(user_win_count,0) +(CASE WHEN (pay_amount + VALUES(pay_amount)) > (bet_amount + VALUES(bet_amount)) THEN 1 ELSE 0 END),
                       win_loss =IFNULL(win_loss,0) + VALUES(win_loss),

                       rtp = CASE
                           WHEN (IFNULL(bet_amount,0) + VALUES(bet_amount)) = 0 THEN 0
                           ELSE ROUND((IFNULL(pay_amount,0) + VALUES(pay_amount)) / (IFNULL(bet_amount,0) + VALUES(bet_amount)),6)
                       END
            </script>
            """)
    int batchUpsert(@Param("list") List<TenantDayStaticDO> list);

    @Insert("""
            <script>
            INSERT INTO game_user_day_static (
                       days, tenant_code, tenant_name, vendor_code, vendor_name,
                       game_code, game_name,user_id,username, bet_count, bet_amount, pay_amount,win_loss,tenant_id
                   )
                   VALUES
                   <foreach collection="list" item="item" separator=",">
                   (
                       #{item.days}, #{item.tenantCode}, #{item.tenantName}, #{item.vendorCode}, #{item.vendorName},
                       #{item.gameCode}, #{item.gameName},#{item.userId}, #{item.username}, #{item.betCount}, #{item.betAmount}, #{item.payAmount}
                       ,#{item.winLoss},#{item.tenantId}
                   )
                   </foreach>
                   ON DUPLICATE KEY UPDATE
                       bet_count = IFNULL(bet_count,0) + VALUES(bet_count),
                       bet_amount = IFNULL(bet_amount,0) + VALUES(bet_amount),
                       pay_amount = IFNULL(pay_amount,0) + VALUES(pay_amount),
                       user_count = IFNULL(user_count,0) + VALUES(user_count),
                       win_loss =IFNULL(win_loss,0) + VALUES(win_loss),
                       rtp = CASE
                           WHEN (IFNULL(bet_amount,0) + VALUES(bet_amount)) = 0 THEN 0
                           ELSE ROUND((IFNULL(pay_amount,0) + VALUES(pay_amount)) / (IFNULL(bet_amount,0) + VALUES(bet_amount)),6)
                       END
            </script>
            """)
    int batchUserUpsert(@Param("list") List<TenantUserDayStaticDO> list);

    /**
     * 查询游戏数据总报表-维度游戏
     *
     * @param page
     * @param wrapper
     * @return
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
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    SUM(win_loss) AS winLoss,
                    SUM(rtp) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
                    GROUP BY tenant_code, tenant_name,vendor_code,vendor_name,game_code,game_name
            
            """)
    IPage<TenantDayStaticDO> selectGameTotalPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);


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
                    tenant_name,
                    tenant_id,
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
            
                    COUNT(DISTINCT user_id) AS userCount,
                    SUM(DISTINCT CASE WHEN bet_amount > 0 THEN 1 END) AS userBetCount,
                    SUM(DISTINCT CASE WHEN pay_amount > bet_amount THEN 1 END) AS userWinCount,
                    SUM(win_loss) AS winLoss,
                    SUM(pay_amount) / SUM(bet_amount) as rtp
                    FROM game_user_day_static
                    ${ew.getCustomSqlSegment}
                    GROUP BY tenant_code, tenant_name,tenant_id
            
            """)
    IPage<TenantDayStaticDO> selectTenantTotal(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总计
     *
     * @param wrapper
     * @return
     */
    @Select("""
            SELECT
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
                    COUNT(DISTINCT user_id) AS userCount,
                    SUM(DISTINCT CASE WHEN bet_amount > 0 THEN 1 END) AS userBetCount,
                    SUM(DISTINCT CASE WHEN pay_amount > bet_amount THEN 1 END) AS userWinCount,
                    SUM(pay_amount) / SUM(bet_amount) as rtp,
                    SUM(win_loss) AS winLoss
                    FROM game_user_day_static
                    ${ew.getCustomSqlSegment}
            """)
    GameStaticTotalVO selectTotalCount(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);


    /**
     * 查询游戏数据总报表-维度租户
     *
     * @param page
     * @param wrapper
     * @return
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
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    SUM(win_loss) AS winLoss,
                    SUM(rtp) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
                    GROUP BY tenant_id,tenant_code, tenant_name, vendor_code,vendor_name,game_code,game_name
            
            
            """)
    IPage<TenantDayStaticDO> selectTenantGameTotalPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总报表-维度租户，并优先使用游戏信息表的厂商/游戏名称。
     * 未匹配到 game_info 时，保留统计表原始值。
     *
     * @param page
     * @param wrapper
     * @return
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
            
                    SUM(a.user_bet_count) AS userBetCount,
                    SUM(a.user_win_count) AS userWinCount,
                    SUM(a.user_count) AS userCount,
                    SUM(a.win_loss) AS winLoss,
                    SUM(a.rtp) AS rtp
                    FROM (
                        SELECT *
                        FROM game_tenant_day_static
                        ${ew.getCustomSqlSegment}
                    ) a
                    LEFT JOIN game_info g
                        ON g.vendor_code = a.vendor_code
                        AND g.game_code = a.game_code
                    GROUP BY a.tenant_id, a.tenant_code, a.tenant_name, a.vendor_code,
                             COALESCE(g.vendor_name, a.vendor_name), a.game_code,
                             COALESCE(g.game_name, a.game_name), g.cate_id
            
            
            """)
    IPage<TenantDayStaticRespVO> selectTenantGameTotalPageWithGameInfo(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    /**
     * 查询游戏数据总计
     *
     * @param wrapper
     * @return
     */
    @Select("""
            
                        SELECT
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    
                    SUM(win_loss) AS winLoss,
                    SUM(rtp) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
            """)
    TenantStaticTotalVO selectTenantCount(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);


    @Select("""
            
                        SELECT
                    game_code as gameCode,
                    game_name as gameName,
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    SUM(win_loss) AS winLoss,
                    SUM(rtp) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
                    group by game_code,game_name
                    order by SUM(bet_count)
                    limit #{limit}
            """)
    Map<String,Object> selectGameTop(@Param("ew") Wrapper<TenantDayStaticDO> wrapper,@Param("limit") Integer limit);

    @Select("""
                <script>
                SELECT
                    a.tenant_code as tenantCode,
                    a.tenant_name as tenantName,
                    COUNT(*) as activeDays,
                    SUM(a.bet_count) AS betCount,
                    SUM(a.bet_amount) AS betAmount,
                    SUM(a.pay_amount) AS payAmount,
                    SUM(a.user_bet_count) AS userBetCount,
                    SUM(a.user_win_count) AS userWinCount,
                    SUM(a.user_count) AS userCount,
                      SUM(a.win_loss) AS winLoss,
                    SUM(a.rtp) as rtp,
                    b.status as status,
                    b.create_time as createTime,
                    DATEDIFF(CURDATE(), b.create_time) AS openDays
                FROM game_tenant_day_static a
                INNER JOIN system_tenant b ON a.tenant_code = b.code
                WHERE 1=1
                    <if test="reqVO.days != null"> AND a.days &gt;= #{reqVO.days} </if>
                    <if test="reqVO.endDays != null"> AND a.days &lt;= #{reqVO.endDays} </if>
                    <if test="reqVO.status != null"> AND b.status = #{reqVO.status} </if>
                    AND a.tenant_id &gt;= 1
                GROUP BY a.tenant_code, a.tenant_name, b.status, b.create_time,a.days
                ORDER BY
                    <if test="reqVO.orderBy == 'time'"> a.days DESC </if>
                    <if test="reqVO.orderBy != 'time'"> SUM(a.bet_amount) DESC </if>
                LIMIT #{reqVO.limit}
                </script>
            """)
    List<Map<String, Object>> selectStaticChart(@Param("reqVO") TenantDayStaticReqVO reqVO);

    @Select("""
                <script>
                SELECT
                    a.tenant_code as tenantCode,
                    a.tenant_name as tenantName,
                    SUM(a.bet_count) AS betCount,
                    SUM(a.bet_amount) AS betAmount,
                    SUM(a.pay_amount) AS payAmount,
                    SUM(a.user_bet_count) AS userBetCount,
                    SUM(a.user_win_count) AS userWinCount,
                    SUM(a.user_count) AS userCount,
                    SUM(a.win_loss) AS winLoss,
                    SUM(a.rtp) as rtp,
                    b.status as status
                FROM game_tenant_day_static a
                INNER JOIN system_tenant b ON a.tenant_id = b.id
                WHERE 1=1
                    <if test="reqVO.days != null"> AND a.days &gt;= #{reqVO.days} </if>
                    <if test="reqVO.endDays != null"> AND a.days &lt;= #{reqVO.endDays} </if>
                    <if test="reqVO.status != null"> AND b.status = #{reqVO.status} </if>
                GROUP BY a.tenant_code, a.tenant_name, b.status
                ORDER BY SUM(#{orderBy}) DESC 
                </script>
            """)
    List<Map<String, Object>> selectStaticChartDayInfo(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
                <script>
                SELECT
                    a.game_code as gameCode,
                    a.game_name as gameName,
                    SUM(a.bet_count) AS betCount,
                    SUM(a.bet_amount) AS betAmount,
                    SUM(a.pay_amount) AS payAmount,
                    SUM(a.user_bet_count) AS userBetCount,
                    SUM(a.user_win_count) AS userWinCount,
                    SUM(a.user_count) AS userCount,
                    SUM(a.win_loss) AS winLoss,
                    SUM(a.rtp) as rtp
                FROM game_tenant_day_static a
                WHERE 1=1
                    <if test="reqVO.days != null"> AND a.days &gt;= #{reqVO.days} </if>
                    <if test="reqVO.endDays != null"> AND a.days &lt;= #{reqVO.endDays} </if>
                GROUP BY a.game_code, a.game_name
                ORDER BY SUM(#{orderBy}) DESC 
                </script>
            """)
    List<Map<String, Object>> selectChartGameTotal(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
                <script>
                SELECT
                    a.days,
                    SUM(a.bet_count) AS betCount,
                    SUM(a.bet_amount) AS betAmount,
                    SUM(a.pay_amount) AS payAmount,
                    SUM(a.user_bet_count) AS userBetCount,
                    SUM(a.user_win_count) AS userWinCount,
                    SUM(a.user_count) AS userCount,
                    SUM(a.win_loss) AS winLoss,
                    SUM(a.rtp) as rtp
                FROM game_tenant_day_static a
                WHERE 1=1
                    <if test="reqVO.days != null"> AND a.days &gt;= #{reqVO.days} </if>
                    <if test="reqVO.endDays != null"> AND a.days &lt;= #{reqVO.endDays} </if>
                GROUP BY a.days
                ORDER BY SUM(#{orderBy}) DESC 
                </script>
            """)
    List<Map<String, Object>> selectChartGameDayTotal(@Param("reqVO") TenantChartsReqVO reqVO, @Param("orderBy") String orderBy);

    @Select("""
                <script>
                SELECT
                    a.days,
                    SUM(a.bet_amount) AS betAmount,
                    SUM(a.pay_amount) AS payAmount,
                    SUM(a.user_bet_count) AS userBetCount,
                    SUM(a.user_win_count) AS userWinCount,
                    SUM(a.user_count) AS userCount,
                    SUM(a.win_loss) AS winLoss
                FROM game_tenant_day_static a
                INNER JOIN system_tenant b ON a.tenant_id = b.id
                WHERE 1=1
                    <if test="reqVO.days != null"> AND a.days &gt;= #{reqVO.days} </if>
                    <if test="reqVO.endDays != null"> AND a.days &lt;= #{reqVO.endDays} </if>
                    <if test="reqVO.status != null"> AND b.status = #{reqVO.status} </if>
                    AND a.tenant_id &gt; 1
                GROUP BY a.days
                </script>
            """)
    List<Map<String, Object>> selectDayTotal(@Param("reqVO") TenantChartsReqVO reqVO);
    @Select("""
            
                        SELECT
                            vendor_code,
                            vendor_name,   
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    SUM(win_loss) AS winLoss,
                    AVG(bet_amount/user_count) as avgBetAmount,
                    SUM(rtp) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
                    group by vendor_code,vendor_name   
            """)
    VendorStaticTotalVO selectStaticVendorTotal(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);
    @Select("""
            
                        SELECT
                            game_code,
                            game_name,   
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    SUM(win_loss) AS winLoss,
                    AVG(bet_amount/user_count) as avgBetAmount,
                    SUM(rtp) as rtp
                    FROM game_tenant_day_static
                    ${ew.getCustomSqlSegment}
                    group by game_code,game_name   
            """)
    TenantGameStaticTotalVO selectStaticGameTotal(@Param("ew") Wrapper<TenantDayStaticDO> wrapper);

    @Select("""
            
                        SELECT
                    
                    user_id,
                    username,
            
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
            
                    SUM(user_bet_count) AS userBetCount,
                    SUM(user_win_count) AS userWinCount,
                    SUM(user_count) AS userCount,
                    SUM(win_loss) AS winLoss,
                    SUM(rtp) as rtp
                    FROM game_user_day_static
                    ${ew.getCustomSqlSegment}
                    GROUP BY user_id,username
            
            
            """)
    IPage<TenantUserDayStaticDO> selectUserStaticPage(IPage<?> page, @Param("ew") Wrapper<TenantDayStaticDO> wrapper);
}