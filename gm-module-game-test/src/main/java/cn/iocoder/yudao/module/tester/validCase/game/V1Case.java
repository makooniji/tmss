package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.*;
import cn.iocoder.yudao.module.system.model.api.push.sports.DSBetRequest;
import cn.iocoder.yudao.module.tester.flow.FlowBuilder;
import cn.iocoder.yudao.module.tester.validation.ValidationCase;
import cn.iocoder.yudao.module.tester.validation.ValidationResult;
import cn.iocoder.yudao.module.tester.validation.ValidationRunRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * JLS 平台余额验证案例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V1Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId=IdUtil.fastUUID();

    @Override
    public String code() {
        return "v1";
    }


    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {
        try {
            List<ValidationResult> details = FlowBuilder.start(ctx,ws)
                    // 1️⃣ 查询余额
                    .step("1.01.01", (context -> {
                        DSBalance dsBalance = new DSBalance();
                        dsBalance.setUsername(ctx.getPullUsername());

                        DownstreamResult response =
                                dsManager.push(ctx, ctx.getTenant(), ActionEnum.BALANCE, dsBalance,IdUtil.nanoId());

                        ctx.setBalance(response.getData().getBalance());
                        return response;

                    }))
                    .after((res, c) -> {
                        c.getVars().put("initBalance", res.getData().getBalance());
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")
                    // 3️⃣ 下注5
                    .step("1.02.01", context ->{

                        //存储下注信息
                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(IdUtil.fastUUID());
                        bet.setExternalTransactionId(context.getOrderId());
                        bet.setBetId(context.getOrderId());
                        //下注金额大于余额
                        bet.setBetAmount(BigDecimal.valueOf(5));
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());

                        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.BET, bet,IdUtil.nanoId());
                        context.setBalance(response.getData().getBalance());
                        return response;
                    } )
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("initBalance"))
                                .subtract(BigDecimal.valueOf(5));
                        c.getVars().put("betBalance", expected);
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")

                    // 派奖 10
                    .step("1.03.01", (context -> {
                        DSBetResultRequest dsRequest = new DSBetResultRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setBetId(betRollBackId);
                        dsRequest.setBetAmount(BigDecimal.valueOf(0));
                        dsRequest.setWinAmount(BigDecimal.valueOf(10));
                        dsRequest.setJackpotAmount(BigDecimal.ZERO);
                        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE, dsRequest,IdUtil.nanoId());

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }))
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("betBalance"))
                                .add(BigDecimal.valueOf(10));
                        c.getVars().put("expectedBalance", expected);
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")

                    .step("1.04.01", (context -> {
                        DSBetResultRequest dsRequest = new DSBetResultRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setBetId(betRollBackId);
                        dsRequest.setBetAmount(BigDecimal.valueOf(0));
                        dsRequest.setWinAmount(BigDecimal.valueOf(10));
                        dsRequest.setJackpotAmount(BigDecimal.ZERO);
                        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);
                        dsRequest.setResultType("END");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE, dsRequest,IdUtil.nanoId());

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }))
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("expectedBalance"));
                        c.getVars().put("expectedBalance2", expected);
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")


                    // 扣款10000
                    .step("1.05.01", (context)->{
                        DSAdjustment dsBalance = new DSAdjustment();
                        dsBalance.setTraceId(IdUtil.fastUUID());
                        dsBalance.setTimestamp(System.currentTimeMillis());
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(context.getTransferId());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setAmount(BigDecimal.valueOf(-10000));

                        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.ADJUSTMENT, dsBalance,IdUtil.nanoId());
                        context.setBalance(response.getData().getBalance());
                        return response;
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")

                    .step("1.06.01", (context)->{
                        DSDebit dsBalance = new DSDebit();
                        dsBalance.setTraceId(IdUtil.fastUUID());
                        dsBalance.setTimestamp(System.currentTimeMillis());
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(context.getTransferId());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setAmount(BigDecimal.valueOf(1000));

                        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.DEBIT, dsBalance,IdUtil.nanoId());
                        context.setBalance(response.getData().getBalance());
                        return response;
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")

                    .step("1.07.01", (context)->{
                        DSCredit dsBalance = new DSCredit();
                        dsBalance.setTraceId(IdUtil.fastUUID());
                        dsBalance.setTimestamp(System.currentTimeMillis());
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(context.getTransferId());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setBetAmount(BigDecimal.valueOf(1000));

                        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.CREDIT, dsBalance,IdUtil.nanoId());
                        context.setBalance(response.getData().getBalance());
                        return response;
                    })
                    .verify("status == 'SC_INVALID_SIGNATURE'")

                    .execute();

            return ValidationResult.success(code(), "SUCCESS");

        } catch (Exception e) {
            return ValidationResult.failure(code(), "FAIL", e.getMessage());
        }


    }

}
