package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DSBalance;
import cn.iocoder.yudao.module.system.model.api.push.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.api.push.DSRollback;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
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
public class V34Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId=IdUtil.fastUUID();

    @Override
    public String code() {
        return "v34";
    }

    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {


        try {
            List<ValidationResult> details = FlowBuilder.start(ctx,ws)
                    // 1️⃣ 查询余额
                    .step("3.04.01", this::stepGetBalance)
                    .after((res, c) -> {
                        c.getVars().put("initBalance", res.getData().getBalance());
                    })
                    .verify("status == 'OK'")

                    // 2️⃣ 结算派奖（+95）
                    .step("3.04.02", this::stepBetUpdate)
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("initBalance"))
                                .add(BigDecimal.valueOf(95));
                        c.getVars().put("expectedBalance", expected);
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 3️⃣ 下注180
                    .step("3.04.03", this::stepBet)
                    .after((res, c) -> {
                        BigDecimal expected = ((BigDecimal) c.getVars().get("expectedBalance"))
                                .subtract(BigDecimal.valueOf(180));
                        c.getVars().put("betBalance", expected);
                    })
                    .verify("status == 'OK' && data.balance == #betBalance")

                    // 4️⃣ 退款20（失败）
                    .step("3.04.04", this::stepRefund)
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

    private DownstreamResult stepBetUpdate(SportContext context) {
        DSBetResultRequest dsRequest = new DSBetResultRequest();
        dsRequest.setUsername(context.getPullUsername());
        dsRequest.setTransactionId(IdUtil.fastUUID());
        dsRequest.setBetId(betRollBackId);
        dsRequest.setBetAmount(BigDecimal.valueOf(5));
        dsRequest.setWinAmount(BigDecimal.valueOf(100));
        dsRequest.setJackpotAmount(BigDecimal.ZERO);
        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);

        DownstreamResult response =
                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE.getPath(), dsRequest);

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
