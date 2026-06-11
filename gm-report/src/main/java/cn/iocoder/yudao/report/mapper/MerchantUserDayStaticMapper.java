package cn.iocoder.yudao.report.mapper;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.report.model.merchant.MerchantUserDayStaticDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商户报表 Mapper
 *
 * @author osca
 */
@Mapper
public interface MerchantUserDayStaticMapper extends BaseMapperX<MerchantUserDayStaticDO> {

    @Insert("""
            <script>
            INSERT INTO merchant_user_day_static (
                days, user_id, username, bet_count, bet_amount, pay_amount, win_loss, agent_code, 
                recharge_amount, recharge_count, withdraw_amount, withdraw_count, bonus, tenant_id,
                new_user_count, active_user_count, valid_user_count
            )
            VALUES
            <foreach collection="list" item="item" separator=",">
            (
                #{item.days}, #{item.userId}, #{item.username}, #{item.betCount}, #{item.betAmount}, #{item.payAmount},
                #{item.winLoss}, #{item.agentCode}, #{item.rechargeAmount}, #{item.rechargeCount}, #{item.withdrawAmount},
                #{item.withdrawCount}, #{item.bonus}, #{item.tenantId},
                #{item.newUserCount}, #{item.activeUserCount}, #{item.validUserCount}
            )
            </foreach>
            ON DUPLICATE KEY UPDATE
                bet_count = IFNULL(bet_count, 0) + VALUES(bet_count),
                bet_amount = IFNULL(bet_amount, 0) + VALUES(bet_amount),
                pay_amount = IFNULL(pay_amount, 0) + VALUES(pay_amount),
                recharge_amount = IFNULL(recharge_amount, 0) + VALUES(recharge_amount),  
                withdraw_amount = IFNULL(withdraw_amount, 0) + VALUES(withdraw_amount),
                recharge_count = IFNULL(recharge_count, 0) + VALUES(recharge_count),
                withdraw_count = IFNULL(withdraw_count, 0) + VALUES(withdraw_count),
                bonus = IFNULL(bonus, 0) + VALUES(bonus),
                -- 状态类计数指标：同人同一天多次重算时，以最新的状态值为准(通常传入1或0)
                new_user_count = VALUES(new_user_count),
                active_user_count = VALUES(active_user_count),
                valid_user_count = VALUES(valid_user_count),
                -- 修正 win_loss 的计算链，避免直接对已有变量和 VALUES 交叉运算引起混乱
                win_loss = (IFNULL(pay_amount, 0) + VALUES(pay_amount)) - (IFNULL(bet_amount, 0) + VALUES(bet_amount)),
                rtp = CASE
                    WHEN (IFNULL(bet_amount, 0) + VALUES(bet_amount)) = 0 THEN 0
                    ELSE ROUND((IFNULL(pay_amount, 0) + VALUES(pay_amount)) / (IFNULL(bet_amount, 0) + VALUES(bet_amount)), 6)
                END
            </script>
            """)
    int batchUpsert(@Param("list") List<MerchantUserDayStaticDO> list);


    @Select("""
            SELECT
                user_id,
                username,
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                SUM(win_loss) AS winLoss,
                SUM(recharge_amount) AS rechargeAmount,
                SUM(withdraw_amount) AS withdrawAmount,
                SUM(withdraw_count) AS withdrawCount,
                SUM(recharge_count) AS rechargeCount,
                SUM(bonus) AS bonus,
                SUM(rtp) AS rtp,
                SUM(new_user_count) AS newUserCount,
                SUM(active_user_count) AS activeUserCount,
                SUM(valid_user_count) AS validUserCount
            FROM merchant_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY user_id, username
            """)
    IPage<MerchantUserDayStaticDO> selectUserStaticPage(IPage<?> page, @Param("ew") Wrapper<MerchantUserDayStaticDO> wrapper);


    @Select("""
            SELECT
                agent_code AS agentCode,
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                SUM(win_loss) AS winLoss,
                SUM(recharge_amount) AS rechargeAmount,
                SUM(withdraw_amount) AS withdrawAmount,
                SUM(withdraw_count) AS withdrawCount,
                SUM(recharge_count) AS rechargeCount,
                SUM(bonus) AS bonus,
                SUM(new_user_count) AS newUserCount,
                SUM(active_user_count) AS activeUserCount,
                SUM(valid_user_count) AS validUserCount
            FROM merchant_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY agent_code
            """)
    IPage<MerchantUserDayStaticDO> selectAgentStaticPage(IPage<?> page, @Param("ew") Wrapper<MerchantUserDayStaticDO> wrapper);

    @Select("""
            SELECT
                agent_code AS agentCode,
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                SUM(win_loss) AS winLoss,
                SUM(recharge_amount) AS rechargeAmount,
                SUM(withdraw_amount) AS withdrawAmount,
                SUM(withdraw_count) AS withdrawCount,
                SUM(recharge_count) AS rechargeCount,
                SUM(bonus) AS bonus,
                SUM(new_user_count) AS newUserCount,
                SUM(active_user_count) AS activeUserCount,
                SUM(valid_user_count) AS validUserCount
            FROM merchant_user_day_static
            ${ew.getCustomSqlSegment}
            GROUP BY agent_code
            """)
    List<MerchantUserDayStaticDO> selectAgentStaticList(@Param("ew") Wrapper<MerchantUserDayStaticDO> wrapper);


    @Select("""
            SELECT
                SUM(bet_count) AS betCount,
                SUM(bet_amount) AS betAmount,
                SUM(pay_amount) AS payAmount,
                SUM(win_loss) AS winLoss,
                SUM(recharge_amount) AS rechargeAmount,
                SUM(withdraw_amount) AS withdrawAmount,
                SUM(withdraw_count) AS withdrawCount,
                SUM(recharge_count) AS rechargeCount,
                SUM(bonus) AS bonus,
                SUM(new_user_count) AS newUserCount,
                SUM(active_user_count) AS activeUserCount,
                SUM(valid_user_count) AS validUserCount
            FROM merchant_user_day_static
            ${ew.getCustomSqlSegment}
            """)
    MerchantUserDayStaticDO selectUserStaticTotal(@Param("ew") Wrapper<MerchantUserDayStaticDO> wrapper);
}