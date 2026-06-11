package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DSBalance;
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
public class V31Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId=IdUtil.fastUUID();

    @Override
    public String code() {
        return "v31";
    }

    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {


        try {
            List<ValidationResult> details = FlowBuilder.start(ctx,ws)
                    // 1️⃣ 查询余额
                    .step("3.01.01", (context -> {
                        DSBalance dsBalance = new DSBalance();
                        dsBalance.setUsername(ctx.getPullUsername());

                        DownstreamResult response =
                                dsManager.push(ctx, ctx.getTenant(), ActionEnum.BALANCE.getPath(), dsBalance);

                        ctx.setBalance(response.getData().getBalance());
                        return response;
                    }))
                    .after((res, c) -> {
                        c.getVars().put("initBalance", res.getData().getBalance());
                    })
                    .verify("status == 'OK'")
                    // 下注大于之前余额
                    .step("3.01.02", context ->{

                        //存储下注信息
                        DSBetRequest bet = new DSBetRequest();
                        bet.setUsername(context.getPullUsername());
                        bet.setTransactionId(IdUtil.fastUUID());
                        bet.setExternalTransactionId(context.getOrderId());
                        bet.setBetId(context.getOrderId());
                        //下注金额大于余额
                        bet.setBetAmount(context.getBalance().add(BigDecimal.valueOf(100)));
                        bet.setGameCode(context.getGameCode());
                        bet.setRoundId(context.getRoundId());

                        DownstreamResult response = dsManager.push(context, context.getTenant(), ActionEnum.BET, bet,IdUtil.nanoId());
                        context.setBalance(response.getData().getBalance());
                        return response;
                    })
                    .verify("status == 'SC_INSUFFICIENT_FUNDS'")
                    .execute();

            return ValidationResult.success(code(), "SUCCESS");

        } catch (Exception e) {
            return ValidationResult.failure(code(), "FAIL", e.getMessage());
        }


    }




}
