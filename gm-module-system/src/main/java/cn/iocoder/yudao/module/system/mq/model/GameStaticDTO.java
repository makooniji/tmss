package cn.iocoder.yudao.module.system.mq.model;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.RedisUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import lombok.Data;
import org.redisson.api.RBucket;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 通道报表模型
 *
 * @author osca
 */
@Data
public class GameStaticDTO extends cn.iocoder.yudao.framework.mq.kafka.StaticDTO {

    /**
     * 获取距离当天结束还剩多少秒
     */
    public static Duration getSecondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        return Duration.between(now, endOfDay);
    }

    public static void buildOpen(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO
    ) {

        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString()); // 或用 bet 时间
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());

        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());

        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());

        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());


        // 用户参与去重
        boolean firstUser = isFirstActionToday("open", stat, user.getUsername());
        if (!firstUser) {
            stat.setUserCount(1);
        }
        stat.setUserWinCount(0);
        statProducer.send(stat);
    }


    public static void buildBet(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                BigDecimal betAmount
    ) {

        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString()); // 或用 bet 时间
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());

        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());

        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());

        stat.setBetCount(1);
        stat.setBetAmount(betAmount);
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());

        // 用户参与去重
        boolean firstUser = isFirstActionToday("bet", stat, user.getUsername());
        if (!firstUser) {
            stat.setUserBetCount(1);
        }
        stat.setUserWinCount(0);
        statProducer.send(stat);

    }

    public static void buildRefund(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                   BigDecimal betAmount
    ) {

        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString()); // 或用 bet 时间
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());

        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());

        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());
        stat.setBetCount(-1);
        stat.setBetAmount(betAmount.multiply(BigDecimal.valueOf(-1)));
        statProducer.send(stat);

    }

    public static void buildPayout(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                   BigDecimal payAmount, BigDecimal winLoss, Boolean isWin
    ) {

        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString()); // 或用 bet 时间
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());

        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());

        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());
        stat.setBetCount(0);
        stat.setBetAmount(BigDecimal.ZERO);
        stat.setPayAmount(payAmount);
        stat.setUserCount(0);
        stat.setUserBetCount(0);
        stat.setWinLoss(winLoss);
        statProducer.send(stat);

    }

    public static void buildUnSettle(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                     BigDecimal payAmount, Boolean isWin
    ) {

        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString()); // 或用 bet 时间
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());

        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());

        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());
        stat.setBetCount(0);
        stat.setBetAmount(BigDecimal.ZERO);
        stat.setPayAmount(payAmount.multiply(BigDecimal.valueOf(-1)));
        stat.setUserCount(0);
        stat.setUserBetCount(0);
        statProducer.send(stat);

    }


    // 1. 定义一个通用的去重方法
    private static boolean isFirstActionToday(String type, GameStaticDTO stat, String username) {
        // Key 必须包含日期，确保每天重新计算人数
        String key = String.format("stat:%s:%s:%s:%s:%s",
                type,
                stat.getDays(), // 加入日期
                stat.getTenantCode(),
                stat.getGameCode(),
                username
        );

        RBucket<String> bucket = RedisUtils.getClient().getBucket(key);

        // trySet 相当于 Redis 的 SETNX，原子性操作
        // 如果返回 true，表示之前没有这个 Key，即“第一次”
        return bucket.setIfAbsent("1", getSecondsUntilEndOfDay());
    }

}
