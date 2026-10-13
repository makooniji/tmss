package cn.iocoder.yudao.module.system.model.dto;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mq.enums.MessageTypeEnum;
import cn.iocoder.yudao.framework.mq.kafka.StaticDTO;
import cn.iocoder.yudao.framework.common.enums.CurrencyEnum;
import cn.iocoder.yudao.module.system.model.GameInfoDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorUserDO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 通道报表模型
 *
 * @author osca
 */
@Data
public class GameStaticDTO extends StaticDTO {

    private String currency;

    public static void buildOpen(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO, String currency) {
        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString());
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());
        stat.setMessageType(MessageTypeEnum.REGISTER);
        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());
        stat.setCurrency(currency);
        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());

        stat.setBetCount(0);
        stat.setBetAmount(BigDecimal.ZERO);
        stat.setPayAmount(BigDecimal.ZERO);
        stat.setWinLoss(BigDecimal.ZERO);

        statProducer.send(stat);
    }

    public static void buildBet(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                BigDecimal betAmount, String currency) {
        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString());
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());
        stat.setMessageType(MessageTypeEnum.BET);
        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());
        stat.setCurrency(currency);
        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());


        stat.setBetAmount(betAmount.multiply(CurrencyEnum.fromCode(currency).getExchangeRate()) );
        if(stat.getBetAmount().compareTo(BigDecimal.ZERO) > 0){
            stat.setBetCount(1);
        }
        stat.setPayAmount(BigDecimal.ZERO);
        stat.setWinLoss(BigDecimal.ZERO);

        statProducer.send(stat);
    }

    public static void buildRefund(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                   BigDecimal betAmount, String currency) {
        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString());
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());
        stat.setMessageType(MessageTypeEnum.CANCEL);
        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());
        stat.setCurrency(currency);
        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());

        stat.setBetCount(-1);
        stat.setBetAmount(betAmount.multiply(CurrencyEnum.fromCode(currency).getExchangeRate()).negate());
        statProducer.send(stat);
    }

    public static void buildPayout(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                   BigDecimal payAmount, BigDecimal winLoss, Boolean isWin, String currency) {
        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString());
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());
        stat.setMessageType(MessageTypeEnum.PAYOUT);
        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());
        stat.setCurrency(currency);
        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());
        stat.setBetCount(0);
        stat.setBetAmount(BigDecimal.ZERO);
        stat.setPayAmount(payAmount.multiply(CurrencyEnum.fromCode(currency).getExchangeRate()) );
        stat.setWinLoss(winLoss);
        //赢钱记录统计
        if(isWin){
            GameStaticDTO statWin = new GameStaticDTO();
            BeanUtils.copyProperties(stat, statWin);
            statWin.setId(IdUtil.getSnowflakeNextId());
            statWin.setMessageType(MessageTypeEnum.WIN);
            statWin.setBetCount(0);
            statWin.setBetAmount(BigDecimal.ZERO);
            statWin.setPayAmount(BigDecimal.ZERO);
            statWin.setWinLoss(BigDecimal.ZERO); // 避免与 PAYOUT 中的 winLoss 重复统计
            statProducer.send(statWin);
        }
        statProducer.send(stat);
    }

    public static void buildUnSettle(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                     BigDecimal payAmount, Boolean isWin, String currency) {
        GameStaticDTO stat = new GameStaticDTO();
        stat.setId(IdUtil.getSnowflakeNextId());
        stat.setDays(LocalDate.now().toString());
        stat.setTenantId(user.getTenantId());
        stat.setTenantCode(user.getTenantCode());
        stat.setTenantName(user.getTenantName());
        stat.setMessageType(MessageTypeEnum.CANCEL);
        stat.setVendorCode(vendorTenantDO.getVendorCode());
        stat.setVendorName(vendorTenantDO.getVendorName());
        stat.setCurrency(currency);
        stat.setGameCode(gameInfoDO.getGameCode());
        stat.setGameName(gameInfoDO.getGameName());
        stat.setUsername(user.getUsername());
        stat.setUserId(user.getId());

        stat.setBetCount(-1);
        stat.setBetAmount(BigDecimal.ZERO);
        stat.setPayAmount(payAmount.multiply(CurrencyEnum.fromCode(currency).getExchangeRate()).negate());

        statProducer.send(stat);
    }

    public static void buildBetResult(VendorUserDO user, GameVendorDO vendorTenantDO, GameInfoDO gameInfoDO,
                                      BigDecimal payAmount, BigDecimal winLoss, Boolean isWin, BigDecimal betAmount, String currency) {
        // 构建基础模版数据
        GameStaticDTO baseStat = new GameStaticDTO();
        baseStat.setDays(LocalDate.now().toString());
        baseStat.setTenantId(user.getTenantId());
        baseStat.setTenantCode(user.getTenantCode());
        baseStat.setTenantName(user.getTenantName());
        baseStat.setCurrency(currency);
        baseStat.setVendorCode(vendorTenantDO.getVendorCode());
        baseStat.setVendorName(vendorTenantDO.getVendorName());
        baseStat.setGameCode(gameInfoDO.getGameCode());
        baseStat.setGameName(gameInfoDO.getGameName());
        baseStat.setUsername(user.getUsername());
        baseStat.setUserId(user.getId());

        // 1. 发送投注消息 (BET)
        GameStaticDTO statBet = new GameStaticDTO();
        BeanUtils.copyProperties(baseStat, statBet);
        statBet.setId(IdUtil.getSnowflakeNextId());
        statBet.setMessageType(MessageTypeEnum.BET);
        statBet.setBetAmount(betAmount.multiply(CurrencyEnum.fromCode(currency).getExchangeRate()));
        if(statBet.getBetAmount().compareTo(BigDecimal.ZERO) > 0){
            statBet.setBetCount(1);
        }

        statBet.setPayAmount(BigDecimal.ZERO);
        statBet.setWinLoss(BigDecimal.ZERO); // BET 消息不携带 winLoss
        statProducer.send(statBet);

        // 2. 发送派奖消息 (PAYOUT)
        GameStaticDTO statPayout = new GameStaticDTO();
        BeanUtils.copyProperties(baseStat, statPayout);
        statPayout.setId(IdUtil.getSnowflakeNextId()); // 生成独立 ID，防止消费端去重覆盖
        statPayout.setMessageType(MessageTypeEnum.PAYOUT);
        statPayout.setBetCount(0);
        statPayout.setBetAmount(BigDecimal.ZERO);
        statPayout.setPayAmount(payAmount.multiply(CurrencyEnum.fromCode(currency).getExchangeRate()));
        statPayout.setWinLoss(winLoss != null ? winLoss : BigDecimal.ZERO);
        statProducer.send(statPayout);

        // 3. 中奖标记消息 (WIN)
        if (Boolean.TRUE.equals(isWin)) {
            GameStaticDTO statWin = new GameStaticDTO();
            BeanUtils.copyProperties(baseStat, statWin);
            statWin.setId(IdUtil.getSnowflakeNextId());
            statWin.setMessageType(MessageTypeEnum.WIN);
            statWin.setBetCount(0);
            statWin.setBetAmount(BigDecimal.ZERO);
            statWin.setPayAmount(BigDecimal.ZERO);
            statWin.setWinLoss(BigDecimal.ZERO);
            statProducer.send(statWin);
        }
    }
}


