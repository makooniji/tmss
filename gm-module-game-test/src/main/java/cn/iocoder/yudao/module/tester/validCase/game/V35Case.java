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
public class V35Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId=IdUtil.fastUUID();

    @Override
    public String code() {
        return "v35";
    }

    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {

        try {
            List<ValidationResult> details = FlowBuilder.start(ctx,ws)
                    // 1️⃣ 查询余额
                    .step("3.05.01", this::stepGetBalance)
                    .after((res, c) -> {
                        c.getVars().put("initBalance", res.getData().getBalance());
                    })
                    .verify("status == 'OK'")

                    // 下注5
                    .step("3.05.02", (context -> {
                        DSBetRequest dsRequest = new DSBetRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setBetId(betRollBackId);
                        dsRequest.setBetAmount(BigDecimal.valueOf(5));


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }))
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("initBalance"))
                                .subtract(BigDecimal.valueOf(5));
                        c.getVars().put("expectedBalance", expected);
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")


                    // 2️⃣ 结算派奖（+95）
                    .step("3.05.03", (context -> {
                        DSBetResultRequest dsRequest = new DSBetResultRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setBetId(betRollBackId);
                        dsRequest.setBetAmount(BigDecimal.valueOf(5));
                        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);
                        dsRequest.setWinAmount(BigDecimal.valueOf(15));
                        dsRequest.setResultType("WIN");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }))
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("expectedBalance"))
                                .add(BigDecimal.valueOf(10));
                        c.getVars().put("expectedBalance2", expected);
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance2")

                    // 3️⃣ 下注180
                    .step("3.05.04",  (context -> {
                        DSBetResultRequest dsRequest = new DSBetResultRequest();
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());
                        dsRequest.setBetId(betRollBackId);
                        dsRequest.setBetAmount(BigDecimal.valueOf(5));
                        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);
                        dsRequest.setWinAmount(BigDecimal.valueOf(15));
                        dsRequest.setResultType("END");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }))
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("expectedBalance2"));
                        c.getVars().put("betBalance", expected);
                    })
                    .verify("status == 'OK' && data.balance == #betBalance")

                    // 扣款10000
                    .step("3.05.05", (context)->{
                        DSAdjustment dsBalance = new DSAdjustment();
                        dsBalance.setTraceId(IdUtil.fastUUID());
                        dsBalance.setTimestamp(System.currentTimeMillis());
                        dsBalance.setUsername(context.getPullUsername());
                        dsBalance.setTransactionId(context.getTransferId());
                        dsBalance.setExternalTransactionId(context.getOrderId());
                        dsBalance.setAmount(context.getBalance().add(BigDecimal.valueOf(10000)).negate());

                        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.ADJUSTMENT.getPath(), dsBalance);
                        context.setBalance(response.getData().getBalance());
                        return response;
                    })
                    .verify("status == 'INSUFFICIENT_FUNDS'")

                    .execute();

            return ValidationResult.success(code(), "SUCCESS");

        } catch (Exception e) {
            return ValidationResult.failure(code(), "FAIL", e.getMessage());
        }


    }

    private DownstreamResult stepRefund(SportContext context) {

        // 构建回滚请求
        DSRollback dsRollback = new DSRollback();
        dsRollback.setUsername(context.getPullUsername());
        dsRollback.setTransactionId(IdUtil.fastUUID());
        dsRollback.setExternalTransactionId(betRollBackId);
        dsRollback.setBetId(betRollBackId);
        dsRollback.setGameCode(context.getGameCode());
        dsRollback.setRoundId(context.getRoundId());

        // 调用下游系统
        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.REFUND.getPath(), dsRollback);
        context.setBalance(response.getData().getBalance());

        return  response;
    }

    private DownstreamResult stepBet(SportContext context) {
        //存储下注信息
        DSBetRequest bet = new DSBetRequest();
        bet.setUsername(context.getPullUsername());
        bet.setTransactionId(IdUtil.fastUUID());
        bet.setExternalTransactionId(context.getOrderId());
        bet.setBetId(context.getOrderId());
        //下注金额大于余额
        bet.setBetAmount(BigDecimal.valueOf(180));
        bet.setGameCode(context.getGameCode());
        bet.setRoundId(context.getRoundId());

        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), bet);
        context.setBalance(response.getData().getBalance());
        return response;
    }



    private DownstreamResult stepGetBalance(SportContext ctx) {
        DSBalance dsBalance = new DSBalance();
        dsBalance.setUsername(ctx.getPullUsername());

        DownstreamResult response =
                dsManager.push(ctx, ctx.getTenant(), ActionEnum.BALANCE.getPath(), dsBalance);

        ctx.setBalance(response.getData().getBalance());
        return response;

    }


}
