package cn.iocoder.yudao.report.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.report.controller.vo.GameStaticTotalVO;
import cn.iocoder.yudao.report.model.game.TenantUserDayStaticDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户游戏日报 Mapper
 *
 * @author osca
 */
@Mapper
public interface TenantUserDayStaticMapper extends BaseMapperX<TenantUserDayStaticDO> {

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
    int batchUpsert(@Param("list") List<TenantUserDayStaticDO> list);

    @Select("""
            SELECT
                vendor_code AS vendorCode,
                username,
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                SUM(win_loss) AS winLoss
            FROM game_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY username,vendor_code
            """)
    IPage<TenantUserDayStaticDO> selectUserStaticPage(IPage<?> page, @Param("ew") Wrapper<TenantUserDayStaticDO> wrapper);

    /**
     * 会员统计总计
     */
    @Select("""
            SELECT
                    SUM(bet_count) AS betCount,
                    SUM(bet_amount) AS betAmount,
                    SUM(pay_amount) AS payAmount,
                    COUNT(DISTINCT username) AS userCount,
                    COUNT(DISTINCT CASE WHEN bet_amount > 0 THEN username END) AS userBetCount,
                    COUNT(DISTINCT CASE WHEN win_loss>0  THEN username END) AS userWinCount,
                    SUM(pay_amount) / SUM(bet_amount) as rtp,
                    SUM(win_loss) AS winLoss
                    FROM game_user_day_static
                    ${ew.getCustomSqlSegment}
            """)
    GameStaticTotalVO selectUserStaticTotal(@Param("ew") Wrapper<TenantUserDayStaticDO> wrapper);

}
