package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;

import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;

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
 * 验签失败验证案例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V1Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;

    @Override
    public String code() {
        return "v1";
    }


    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {
        try {
            List<ValidationResult> details = FlowBuilder.start(ctx,ws)
                    // 1.01 查询余额 -> /wallet/balance
                    .step("1.01.01", context -> {
                        DSBalance dsBalance = new DSBalance();
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setCurrency(context.getWrapCurrency());
                        return push(context, ActionEnum.BALANCE, dsBalance);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))
            
                    // 1.02 下注 -> /wallet/bet
                    .step("1.02.01", context ->{

                        //存储下注信息
                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(IdUtil.fastUUID());
                        bet.setExternalTransactionId(context.getOrderId());
                        bet.setBetId(context.getOrderId());
                        //下注金额大于余额
                        bet.setBetAmount(BigDecimal.valueOf(5));
                        bet.setCurrency(context.getWrapCurrency());
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());
                        return push(context, ActionEnum.BET, bet);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))

                    // 1.03 投注结果 -> /wallet/bet_result
                    .step("1.03.01", context -> {
                        DSBetResultRequest dsRequest = new DSBetResultRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setExternalTransactionId(context.getOrderId());
                        dsRequest.setBetId(context.getOrderId());
                        dsRequest.setRoundId(context.getRoundId());
                        dsRequest.setBetAmount(BigDecimal.valueOf(0));
                        dsRequest.setWinAmount(BigDecimal.valueOf(10));
                        dsRequest.setJackpotAmount(BigDecimal.ZERO);
                        dsRequest.setGameCode(context.getGameCode());
                        dsRequest.setActionEnum(ActionEnum.SETTLED);
                        return push(context, ActionEnum.SETTLED, dsRequest);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))

                    // 1.04 回滚 -> /wallet/rollback
                    .step("1.04.01", context -> {
                        DSRollback dsRollback = new DSRollback();
                        dsRollback.setUsername(context.getPullUsername());
                        dsRollback.setTransactionId(IdUtil.fastUUID());
                        dsRollback.setExternalTransactionId(context.getOrderId());
                        dsRollback.setBetId(context.getOrderId());
                        dsRollback.setGameCode(context.getGameCode());
                        dsRollback.setRoundId(context.getRoundId());
                        return push(context, ActionEnum.REFUND, dsRollback);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))


                    // 1.05 扣款10000 -> /wallet/adjustment
                    .step("1.05.01", (context)->{
                        DSAdjustment dsBalance = new DSAdjustment();
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(IdUtil.fastUUID());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setGameCode(context.getGameCode());
                        dsBalance.setRoundId(context.getRoundId());
                        dsBalance.setAmount(BigDecimal.valueOf(-10000));
                        return push(context, ActionEnum.ADJUSTMENT, dsBalance);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))
                    // 1.06 投注借记 -> /wallet/bet_debit
                    .step("1.06.01", (context)->{
                        DSDebit dsBalance = new DSDebit();
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(IdUtil.fastUUID());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setGameCode(context.getGameCode());
                        dsBalance.setRoundId(context.getRoundId());
                        dsBalance.setBetId(context.getOrderId());
                        dsBalance.setAmount(BigDecimal.valueOf(1000));
                        return push(context, ActionEnum.DEBIT, dsBalance);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))

                    // 1.07 投注信用 -> /wallet/bet_credit
                    .step("1.07.01", (context)->{
                        DSCredit dsBalance = new DSCredit();
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(IdUtil.fastUUID());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setGameCode(context.getGameCode());
                        dsBalance.setRoundId(context.getRoundId());
                        dsBalance.setBetId(context.getOrderId());
                        dsBalance.setBetAmount(BigDecimal.valueOf(1000));
                        return push(context, ActionEnum.CREDIT, dsBalance);
                    })
                    .verify(ValidationCaseSupport.statusRule(PlatformCodeEnum.INVALID_SIGNATURE))

                    .execute();

            return ValidationResult.fromSteps(code(), details);

        } catch (Exception e) {
            return ValidationResult.failure(code(), ValidationResult.TYPE_ERROR, e.getMessage());
        }
    }

    /** 故意无效签 push + 余额回写 */
    private DownstreamResult push(SportContext context, ActionEnum action, Object body) {
        DownstreamResult response = dsManager.push(
                context, context.getTenant(), action, body, IdUtil.nanoId());
        ValidationCaseSupport.applyBalanceIfPresent(response, context);
        return response;
    }

}
