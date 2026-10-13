package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;

import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 钱包余额不足验证案例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V3Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;

    @Override
    public String code() {
        return "v3";
    }

    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {

        ctx.getVars().put("token", "af06ce8f-1b72-42c5-abe9-41fdc771c66d");
        ValidationCaseSupport.bindExpectedUser(ctx);

        try {
            List<ValidationResult> allSteps = new ArrayList<>();

            runSubFlow(ctx, allSteps, b -> b
                    // 3.01 查询余额 -> /wallet/balance
                    .step("3.01.01", this::stepQueryBalance)
                    .after(this::saveInitBalance)
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.01.02 下注金额大于余额 -> /wallet/bet
                    .step("3.01.02", context -> {
                        BigDecimal betAmount = amountExceedingBalance(context);

                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(IdUtil.fastUUID());
                        bet.setBetId(context.getOrderId());
                        bet.setExternalTransactionId(context.getTransferId());
                        bet.setAmount(betAmount);
                        bet.setToken((String) context.getVars().get("token"));
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());

                        return dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), bet);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INSUFFICIENT_FUNDS)));

            runSubFlow(ctx, allSteps, b -> b
                    // 3.02 查询余额 -> /wallet/balance
                    .step("3.02.01", this::stepQueryBalance)
                    .after(this::saveInitBalance)
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.02.02 下注金额大于余额 -> /wallet/bet_result (BET_WIN)
                    .step("3.02.02", context -> {
                        BigDecimal betAmount = amountExceedingBalance(context);
                        BigDecimal winAmount = betAmount.add(BigDecimal.ONE);

                        DSBetResultRequest betResult = new DSBetResultRequest();
                        betResult.setUsername(context.getPullUsername());
                        betResult.setTransactionId(IdUtil.fastUUID());
                        betResult.setBetId(context.getOrderId());
                        betResult.setExternalTransactionId(context.getTransferId());
                        betResult.setRoundId(context.getRoundId());
                        betResult.setBetAmount(betAmount);
                        betResult.setWinAmount(winAmount);
                        betResult.setEffectiveTurnover(betAmount);
                        betResult.setWinLoss(BigDecimal.ONE);
                        betResult.setJackpotAmount(BigDecimal.ZERO);
                        betResult.setResultType(ResultTypeEnum.BET_WIN);
                        betResult.setIsFreespin(0);
                        betResult.setIsEndRound(1);
                        betResult.setCurrency(context.getWrapCurrency());
                        betResult.setToken((String) context.getVars().get("token"));
                        betResult.setGameCode(context.getGameCode());
                        long ts = context.getTimestamp();
                        betResult.setBetTime(ts);
                        betResult.setSettledTime(ts);
                        betResult.setActionEnum(ActionEnum.SETTLED);

                        return dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), betResult);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INSUFFICIENT_FUNDS)));

            runSubFlow(ctx, allSteps, b -> b
                    // 3.03 查询余额 -> /wallet/balance
                    .step("3.03.01", this::stepQueryBalance)
                    .after(this::saveInitBalance)
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.03.02 下注金额大于余额 -> /wallet/bet_result (BET_LOSE)
                    .step("3.03.02", context -> {
                        BigDecimal betAmount = amountExceedingBalance(context);

                        DSBetResultRequest betResult = new DSBetResultRequest();
                        betResult.setUsername(context.getPullUsername());
                        betResult.setTransactionId(IdUtil.fastUUID());
                        betResult.setBetId(context.getOrderId());
                        betResult.setExternalTransactionId(context.getTransferId());
                        betResult.setRoundId(context.getRoundId());
                        betResult.setBetAmount(betAmount);
                        betResult.setWinAmount(BigDecimal.ZERO);
                        betResult.setEffectiveTurnover(betAmount);
                        betResult.setWinLoss(betAmount.negate());
                        betResult.setJackpotAmount(BigDecimal.ZERO);
                        betResult.setResultType(ResultTypeEnum.BET_LOSE);
                        betResult.setIsFreespin(0);
                        betResult.setIsEndRound(1);
                        betResult.setCurrency(context.getWrapCurrency());
                        betResult.setToken((String) context.getVars().get("token"));
                        betResult.setGameCode(context.getGameCode());
                        long ts = context.getTimestamp();
                        betResult.setBetTime(ts);
                        betResult.setSettledTime(ts);
                        betResult.setActionEnum(ActionEnum.SETTLED);

                        return dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), betResult);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INSUFFICIENT_FUNDS)));

            runSubFlow(ctx, allSteps, b -> b
                    // 3.04.01 查询余额 -> /wallet/balance
                    .step("3.04.01", this::stepQueryBalance)
                    .after(this::saveInitBalance)
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())
                    // 3.04.02 派彩结算 -> /wallet/bet_result (BET_WIN)
                    .step("3.04.02", context -> {
                        BigDecimal betAmount = BigDecimal.valueOf(5);
                        BigDecimal winAmount = new BigDecimal("1000");

                        DSBetResultRequest betResult = new DSBetResultRequest();
                        betResult.setUsername(context.getPullUsername());
                        betResult.setTransactionId(IdUtil.fastUUID());
                        betResult.setBetId(context.getOrderId());
                        betResult.setExternalTransactionId(context.getTransferId());
                        betResult.setRoundId(context.getRoundId());
                        betResult.setBetAmount(betAmount);
                        betResult.setWinAmount(winAmount);
                        betResult.setEffectiveTurnover(betAmount);
                        betResult.setWinLoss(winAmount.subtract(betAmount));
                        betResult.setJackpotAmount(BigDecimal.ZERO);
                        betResult.setResultType(ResultTypeEnum.BET_WIN);
                        betResult.setIsFreespin(0);
                        betResult.setIsEndRound(1);
                        betResult.setCurrency(context.getWrapCurrency());
                        betResult.setToken((String) context.getVars().get("token"));
                        betResult.setGameCode(context.getGameCode());
                        long ts = context.getTimestamp();
                        betResult.setBetTime(ts);
                        betResult.setSettledTime(ts);
                        betResult.setActionEnum(ActionEnum.SETTLED);

                        return pushAndApplyBalance(context, ActionEnum.SETTLED.getPath(), betResult);
                    })
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.04.03 下注 -> /wallet/bet
                    .step("3.04.03", context -> {
                        String betTransactionId = IdUtil.fastUUID();

                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(betTransactionId);
                        bet.setBetId(context.getOrderId());
                        bet.setExternalTransactionId(context.getTransferId());
                        bet.setAmount(ValidationCaseSupport.balanceOrThrow(context));
                        bet.setCurrency(context.getWrapCurrency());
                        bet.setToken((String) context.getVars().get("token"));
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());
                        bet.setTimestamp(context.getTimestamp());

                        return pushAndApplyBalance(context, ActionEnum.BET.getPath(), bet);
                    })
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.04.04 回滚 -> /wallet/rollback
                    .step("3.04.04", context -> {
                        DSRollback rollback = new DSRollback();
                        rollback.setUsername(context.getPullUsername());
                        rollback.setTransactionId(IdUtil.fastUUID());
                        rollback.setBetId(context.getOrderId());
                        rollback.setExternalTransactionId(context.getTransferId());
                        rollback.setRoundId(context.getRoundId());
                        rollback.setGameCode(context.getGameCode());
                        rollback.setCurrency(context.getWrapCurrency());
                        rollback.setTimestamp(context.getTimestamp());

                        return dsManager.push(context, context.getTenant(), ActionEnum.REFUND.getPath(), rollback);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INSUFFICIENT_FUNDS)));

            runSubFlow(ctx, allSteps, b -> b
                    // 3.05.01 查询余额 -> /wallet/balance
                    .step("3.05.01", this::stepQueryBalance)
                    .after(this::saveInitBalance)
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())
                    // 3.05.02 下注 -> /wallet/bet
                    .step("3.05.02", context -> {
                        BigDecimal betAmount = betAmountUpToFive(context);

                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(IdUtil.fastUUID());
                        bet.setBetId(context.getOrderId());
                        bet.setExternalTransactionId(context.getTransferId());
                        bet.setAmount(betAmount);
                        bet.setCurrency(context.getWrapCurrency());
                        bet.setToken((String) context.getVars().get("token"));
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());
                        bet.setTimestamp(context.getTimestamp());

                        return pushAndApplyBalance(context, ActionEnum.BET.getPath(), bet);
                    })
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.05.03 派彩 WIN -> /wallet/bet_result
                    .step("3.05.03", context -> {
                        BigDecimal winAmount = BigDecimal.TEN;

                        DSBetResultRequest betResult = new DSBetResultRequest();
                        betResult.setUsername(context.getPullUsername());
                        betResult.setTransactionId(IdUtil.fastUUID());
                        betResult.setBetId(context.getOrderId());
                        betResult.setExternalTransactionId(context.getTransferId());
                        betResult.setRoundId(context.getRoundId());
                        betResult.setBetAmount(BigDecimal.ZERO);
                        betResult.setWinAmount(winAmount);
                        betResult.setEffectiveTurnover(BigDecimal.ZERO);
                        betResult.setWinLoss(BigDecimal.ZERO);
                        betResult.setJackpotAmount(BigDecimal.ZERO);
                        betResult.setResultType(ResultTypeEnum.WIN);
                        betResult.setIsFreespin(0);
                        betResult.setIsEndRound(0);
                        betResult.setCurrency(context.getWrapCurrency());
                        betResult.setToken((String) context.getVars().get("token"));
                        betResult.setGameCode(context.getGameCode());
                        long ts = context.getTimestamp();
                        betResult.setBetTime(ts);
                        betResult.setSettledTime(ts);
                        betResult.setActionEnum(ActionEnum.SETTLED);

                        return pushAndApplyBalance(context, ActionEnum.SETTLED.getPath(), betResult);
                    })
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.05.04 结算 END -> /wallet/bet_result
                    .step("3.05.04", context -> {
                        BigDecimal betAmount = betAmountUpToFive(context);
                        BigDecimal winAmount = betAmount.add(BigDecimal.valueOf(5));

                        DSBetResultRequest betResult = new DSBetResultRequest();
                        betResult.setUsername(context.getPullUsername());
                        betResult.setTransactionId(IdUtil.fastUUID());
                        betResult.setBetId(context.getOrderId());
                        betResult.setExternalTransactionId(context.getTransferId());
                        betResult.setRoundId(context.getRoundId());
                        betResult.setBetAmount(betAmount);
                        betResult.setWinAmount(winAmount);
                        betResult.setEffectiveTurnover(betAmount);
                        betResult.setWinLoss(winAmount.subtract(betAmount));
                        betResult.setJackpotAmount(BigDecimal.ZERO);
                        betResult.setResultType(ResultTypeEnum.END);
                        betResult.setIsFreespin(0);
                        betResult.setIsEndRound(1);
                        betResult.setCurrency(context.getWrapCurrency());
                        betResult.setToken((String) context.getVars().get("token"));
                        betResult.setGameCode(context.getGameCode());
                        long ts = context.getTimestamp();
                        betResult.setBetTime(ts);
                        betResult.setSettledTime(ts);
                        betResult.setActionEnum(ActionEnum.SETTLED);

                        return pushAndApplyBalance(context, ActionEnum.SETTLED.getPath(), betResult);
                    })
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())

                    // 3.05.05 调整余额 -> /wallet/adjustment
                    .step("3.05.05", context -> {
                        DSAdjustment adjustment = new DSAdjustment();
                        adjustment.setUsername(context.getPullUsername());
                        adjustment.setTransactionId(IdUtil.fastUUID());
                        adjustment.setExternalTransactionId(context.getTransferId());
                        adjustment.setRoundId(context.getRoundId());
                        adjustment.setGameCode(context.getGameCode());
                        adjustment.setCurrency(context.getWrapCurrency());
                        adjustment.setAmount(ValidationCaseSupport.balanceOrThrow(context)
                                .add(BigDecimal.valueOf(10000)).negate());
                        adjustment.setTimestamp(context.getTimestamp());

                        return dsManager.push(context, context.getTenant(), ActionEnum.ADJUSTMENT.getPath(), adjustment);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INSUFFICIENT_FUNDS)));

            runSubFlow(ctx, allSteps, b -> b
                    // 3.06.01 查询余额 -> /wallet/balance
                    .step("3.06.01", this::stepQueryBalance)
                    .after(this::saveInitBalance)
                    .verifyFromResponse(ValidationCaseSupport.okBalanceDataRule())
                    // 3.06.02 投注借记 -> /wallet/bet_debit
                    .step("3.06.02", context -> {
                        DSDebit debit = new DSDebit();
                        debit.setUsername(context.getPullUsername());
                        debit.setTransactionId(IdUtil.fastUUID());
                        debit.setRoundId(context.getRoundId());
                        debit.setAmount(amountExceedingBalance(context));
                        debit.setCurrency(context.getWrapCurrency());
                        debit.setToken((String) context.getVars().get("token"));
                        debit.setGameCode(context.getGameCode());
                        debit.setTimestamp(context.getTimestamp());

                        return pushAndApplyBalance(context, ActionEnum.DEBIT.getPath(), debit);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INSUFFICIENT_FUNDS)));

            return ValidationResult.fromSteps(code(), allSteps);

        } catch (Exception e) {
            return ValidationResult.failure(code(), ValidationResult.TYPE_ERROR, e.getMessage());
        }
    }

    /** 每段子场景使用独立的 betId / roundId / externalTransactionId，避免商户幂等冲突 */
    private void resetSubFlowIds(SportContext ctx) {
        ctx.setOrderId(IdUtil.fastUUID());
        ctx.setRoundId(IdUtil.fastUUID());
        ctx.setTransferId(IdUtil.fastUUID());
        ctx.setBalance(BigDecimal.ZERO);
        ctx.getVars().remove("initBalance");
    }

    /** 执行一段子流程（段内任一步 FAIL/ERROR 则跳过本段后续步；段与段之间仍继续） */
    private void runSubFlow(SportContext ctx, List<ValidationResult> allSteps,
                            Function<FlowBuilder, FlowBuilder> configure) {
        resetSubFlowIds(ctx);
        allSteps.addAll(configure.apply(FlowBuilder.start(ctx, ws).stopOnFailure()).execute());
    }

    private DownstreamResult stepQueryBalance(SportContext context) {
        DSBalance dsBalance = new DSBalance();
        dsBalance.setUsername(context.getPullUsername());
        dsBalance.setCurrency(context.getWrapCurrency());
        dsBalance.setToken((String) context.getVars().get("token"));

        DownstreamResult response =
                dsManager.push(context, context.getTenant(), ActionEnum.BALANCE.getPath(), dsBalance);
        ValidationCaseSupport.applyBalanceIfPresent(response, context);
        return response;
    }

    private void saveInitBalance(DownstreamResult res, SportContext c) {
        ValidationCaseSupport.saveBalanceToVarIfPresent(res, c, "initBalance");
    }

    private DownstreamResult pushAndApplyBalance(SportContext context, String path, Object body) {
        DownstreamResult response = dsManager.push(context, context.getTenant(), path, body);
        ValidationCaseSupport.applyBalanceIfPresent(response, context);
        return response;
    }

    /** 比查到的下游余额多 1 */
    private BigDecimal amountExceedingBalance(SportContext context) {
        BigDecimal initBalance = (BigDecimal) context.getVars().get("initBalance");
        if (initBalance == null) {
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }
        return initBalance.add(BigDecimal.ONE);
    }

    /** 3.05 段下注额：余额不足 5 时用全部余额，否则下注 5；余额为 0 时仍尝试下注 5，由商户/verify 判定失败 */
    private BigDecimal betAmountUpToFive(SportContext context) {
        BigDecimal initBalance = (BigDecimal) context.getVars().get("initBalance");
        if (initBalance == null) {
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }
        BigDecimal five = BigDecimal.valueOf(5);
        if (initBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return five;
        }
        return initBalance.compareTo(five) < 0 ? initBalance : five;
    }

}
