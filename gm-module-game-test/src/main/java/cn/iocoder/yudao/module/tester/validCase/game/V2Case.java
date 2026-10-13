package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.enums.ResultTypeEnum;
import cn.iocoder.yudao.module.system.model.game.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.game.SportContext;
import cn.iocoder.yudao.module.tester.flow.FlowBuilder;
import cn.iocoder.yudao.module.tester.validCase.support.ValidationCaseSupport;
import cn.iocoder.yudao.module.tester.validation.ValidationCase;
import cn.iocoder.yudao.module.tester.validation.ValidationResult;
import cn.iocoder.yudao.module.tester.validation.ValidationRunRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 无效用户名验证案例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V2Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;

    @Override
    public String code() {
        return "v2";
    }



    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {

        try {
            // 无效用户名场景：必须使用商户不存在的账号，不能用商户里可能已存在的用户名
            ctx.setPullUsername(code() + "_invalid_" + IdUtil.nanoId());
            ctx.getVars().put("token", IdUtil.fastUUID());

            List<ValidationResult> details = FlowBuilder.start(ctx,ws)
                    // 2.01 查询余额 -> /wallet/balance
                    .step("2.01.01", context -> {
                        DSBalance dsBalance = new DSBalance();
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setCurrency(context.getWrapCurrency());
                        dsBalance.setToken((String) context.getVars().get("token"));
                        return push(context, ActionEnum.BALANCE, dsBalance);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))

                    // 2.02 下注 -> /wallet/bet
                    .step("2.02.01", context -> {
                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(IdUtil.fastUUID());
                        bet.setBetId(context.getOrderId());
                        bet.setExternalTransactionId(context.getTransferId());
                        bet.setAmount(BigDecimal.valueOf(5));
                        bet.setCurrency(context.getWrapCurrency());
                        bet.setToken((String) context.getVars().get("token"));
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());
                        return push(context, ActionEnum.BET, bet);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))

                    // 2.03 投注结果 -> /wallet/bet_result
                    .step("2.03.01", context -> {
                        DSBetResultRequest dsRequest = new DSBetResultRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setBetId(context.getOrderId());
                        dsRequest.setExternalTransactionId(context.getTransferId());
                        dsRequest.setRoundId(context.getRoundId());
                        dsRequest.setBetAmount(BigDecimal.valueOf(5));
                        dsRequest.setWinAmount(BigDecimal.valueOf(10));
                        dsRequest.setEffectiveTurnover(BigDecimal.valueOf(5));
                        dsRequest.setWinLoss(BigDecimal.valueOf(5));
                        dsRequest.setJackpotAmount(BigDecimal.ZERO);
                        dsRequest.setResultType(ResultTypeEnum.BET_WIN);
                        dsRequest.setIsFreespin(0);
                        dsRequest.setIsEndRound(1);
                        dsRequest.setCurrency(context.getWrapCurrency());
                        dsRequest.setToken((String) context.getVars().get("token"));
                        dsRequest.setGameCode(context.getGameCode());
                        long settledTime = context.getTimestamp();
                        dsRequest.setBetTime(settledTime);
                        dsRequest.setSettledTime(settledTime);
                        return push(context, ActionEnum.SETTLED, dsRequest);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))

                    // 2.04 回滚 -> /wallet/rollback
                    .step("2.04.01", context -> {
                        DSRollback dsRollback = new DSRollback();
                        dsRollback.setUsername(context.getPullUsername());
                        dsRollback.setTransactionId(IdUtil.fastUUID());
                        dsRollback.setExternalTransactionId(context.getTransferId());
                        dsRollback.setBetId(context.getOrderId());
                        dsRollback.setGameCode(context.getGameCode());
                        dsRollback.setRoundId(context.getRoundId());
                        return push(context, ActionEnum.REFUND, dsRollback);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))


                    // 2.05 调整余额 -> /wallet/adjustment
                    .step("2.05.01", context -> {
                        DSAdjustment dsAdjustment = new DSAdjustment();
                        dsAdjustment.setUsername(context.getPullUsername());
                        dsAdjustment.setTransactionId(IdUtil.fastUUID());
                        dsAdjustment.setExternalTransactionId(context.getTransferId());
                        dsAdjustment.setRoundId(context.getRoundId());
                        dsAdjustment.setGameCode(context.getGameCode());
                        dsAdjustment.setCurrency(context.getWrapCurrency());
                        dsAdjustment.setAmount(BigDecimal.valueOf(10));
                        return push(context, ActionEnum.ADJUSTMENT, dsAdjustment);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))

                    // 2.06 投注借记 -> /wallet/bet_debit
                    .step("2.06.01", context -> {
                        DSDebit dsDebit = new DSDebit();
                        dsDebit.setUsername(context.getPullUsername());
                        dsDebit.setTransactionId(IdUtil.fastUUID());
                        dsDebit.setExternalTransactionId(context.getTransferId());
                        dsDebit.setBetId(context.getOrderId());
                        dsDebit.setRoundId(context.getRoundId());
                        dsDebit.setGameCode(context.getGameCode());
                        dsDebit.setAmount(BigDecimal.valueOf(1000));
                        dsDebit.setToken((String) context.getVars().get("token"));
                        return push(context, ActionEnum.DEBIT, dsDebit);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))

                    // 2.07 投注信用 -> /wallet/bet_credit
                    .step("2.07.01", context -> {
                        DSCredit dsCredit = new DSCredit();
                        dsCredit.setUsername(context.getPullUsername());
                        dsCredit.setTransactionId(IdUtil.fastUUID());
                        dsCredit.setExternalTransactionId(context.getTransferId());
                        dsCredit.setBetId(context.getOrderId());
                        dsCredit.setRoundId(context.getRoundId());
                        dsCredit.setGameCode(context.getGameCode());
                        dsCredit.setToken((String) context.getVars().get("token"));
                        dsCredit.setIsRefund(0);
                        dsCredit.setAmount(BigDecimal.valueOf(500));
                        dsCredit.setBetAmount(BigDecimal.valueOf(1000));
                        dsCredit.setWinAmount(BigDecimal.valueOf(3000));
                        dsCredit.setEffectiveTurnover(BigDecimal.valueOf(1000));
                        dsCredit.setWinLoss(BigDecimal.valueOf(2000));
                        dsCredit.setJackpotAmount(BigDecimal.ZERO);
                        long settledTime = context.getTimestamp();
                        dsCredit.setBetTime(settledTime);
                        dsCredit.setSettledTime(settledTime);
                        return push(context, ActionEnum.CREDIT, dsCredit);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.USER_NOT_EXISTS))

                    .execute();

            return ValidationResult.fromSteps(code(), details);

        } catch (Exception e) {
            return ValidationResult.failure(code(), ValidationResult.TYPE_ERROR, e.getMessage());
        }
    }

    /** 正常签名 push + 余额回写 */
    private DownstreamResult push(SportContext context, ActionEnum action, Object body) {
        DownstreamResult response = dsManager.push(
                context, context.getTenant(), action.getPath(), body);
        ValidationCaseSupport.applyBalanceIfPresent(response, context);
        return response;
    }

}
