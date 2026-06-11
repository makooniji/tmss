package cn.iocoder.yudao.module.tester.validCase.game;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DSBalance;
import cn.iocoder.yudao.module.system.model.api.push.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.api.push.DSDebit;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
import cn.iocoder.yudao.module.system.model.api.push.sports.DSBetRequest;
import cn.iocoder.yudao.module.tester.flow.FlowBuilder;
import cn.iocoder.yudao.module.tester.validation.ValidationCase;
import cn.iocoder.yudao.module.tester.validation.ValidationResult;
import cn.iocoder.yudao.module.tester.validation.ValidationRunRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JLS 平台余额验证案例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V6Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId = IdUtil.fastUUID();

    @Override
    public String code() {
        return "v6";
    }

    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {


        try {
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("6.01.01", (context -> {
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
                    .step("6.01.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"8748c6f6-0bc1-4ac5-bf02-3c758c484e53\",\"username\":\"dever10506\",\"transactionId\":\"OneApie9d77d81-e556-44b8-882c-04ca726257af\",\"betId\":\"OneApib7c37b80-eb3b-422b-b643-f70c8d61f795\",\"externalTransactionId\":\"OneApi1754497736126\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi1512516160493\",\"timestamp\":1773217455574}");




                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("6.01.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"c407bd5f-72c5-4b5e-a767-fa48d5f83e57\",\"username\":\"dever10506\",\"transactionId\":\"OneApia9f232d4-3a51-490d-9d75-9b7c2232711f\",\"betId\":\"OneApib7c37b80-eb3b-422b-b643-f70c8d61f795\",\"externalTransactionId\":\"OneApi1754497736126\",\"roundId\":\"OneApi1512516160493\",\"betAmount\":0,\"winAmount\":0,\"effectiveTurnover\":0,\"winLoss\":0,\"jackpotAmount\":1000,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217455797,\"settledTime\":1773217455797}");
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();


            //6.2
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("6.02.01", (context -> {
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
                    .step("6.02.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"e8b12380-6739-42cb-b8dd-e853223fddcf\",\"username\":\"dever10506\",\"transactionId\":\"OneApi4fec1728-4ae8-4586-8cad-5a83cb6c7a93\",\"betId\":\"OneApic86308b8-dab3-4eb1-a301-912cdfc6872f\",\"externalTransactionId\":\"OneApi8992182193623\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi5904065883900\",\"timestamp\":1773217456231}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("6.02.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"ad4b7d2b-7b10-48b9-b4ac-1f6bacc50718\",\"username\":\"dever10506\",\"transactionId\":\"OneApi6e0ce1a7-3886-4b31-9b61-ed25064d76a6\",\"betId\":\"OneApic86308b8-dab3-4eb1-a301-912cdfc6872f\",\"externalTransactionId\":\"OneApi8992182193623\",\"roundId\":\"OneApi5904065883900\",\"betAmount\":0,\"winAmount\":0,\"effectiveTurnover\":0,\"winLoss\":0,\"jackpotAmount\":1000,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217456464,\"settledTime\":1773217456464}");
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());

                        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("6.02.04", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"6a1614b4-21d2-4a38-a24f-66131ca5bd78\",\"username\":\"dever10506\",\"transactionId\":\"OneApi1ab7cb07-7802-4a9f-9621-ec66ddcfd66a\",\"betId\":\"OneApic86308b8-dab3-4eb1-a301-912cdfc6872f\",\"externalTransactionId\":\"OneApi8992182193623\",\"roundId\":\"OneApi5904065883900\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":1000,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217456696,\"settledTime\":1773217456696}");
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());

                        dsRequest.setActionEnum(ActionEnum.BET_UPDATE);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();



            //6.3
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.03.01", (context -> {
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
                    .step("5.03.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"068d66bd-92d1-45cd-8953-c1d74dc8432b\",\"username\":\"dever10506\",\"transactionId\":\"OneApi08aa0c13-82d3-432f-a73c-0cb5238674b0\",\"betId\":\"OneApi77e71746-30ba-49f6-9926-87f93630ca1c\",\"externalTransactionId\":\"OneApi2955739812980\",\"roundId\":\"OneApi3802636931331\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":1000,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217457138,\"settledTime\":1773217457138}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .execute();

            // 6.4
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("6.04.01", (context -> {
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
                    .step("6.04.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"dde890ec-02e0-4f68-a63f-5802aadde073\",\"username\":\"dever10506\",\"transactionId\":\"OneApi72a5a9ac-a006-4151-9a85-c3ca7a0cd051\",\"betId\":\"OneApied8511b4-f34e-4526-877d-09a7596b60d3\",\"externalTransactionId\":\"OneApi3189415313908\",\"roundId\":\"OneApi319966342324\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":1000,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217457614,\"settledTime\":1773217457614}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("6.04.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"c1276d20-596a-4f27-ade2-f84fee60dc01\",\"username\":\"dever10506\",\"transactionId\":\"OneApi11a71227-a77c-49a2-ba9a-a01c8e0c463d\",\"betId\":\"OneApied8511b4-f34e-4526-877d-09a7596b60d3\",\"externalTransactionId\":\"OneApi3189415313908\",\"roundId\":\"OneApi319966342324\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":1000,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217457879,\"settledTime\":1773217457879}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();

            // 6.5
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("6.05.01", (context -> {
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
                    .step("6.05.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"fca9114d-e4f0-4f2c-8f3d-fec88cb9c5e6\",\"username\":\"dever10506\",\"transactionId\":\"OneApif8a15d9b-d110-45b7-9b87-d50a06d7eea1\",\"betId\":\"OneApi7c2a1e3e-f7ee-4489-a578-51e7e77acf7d\",\"externalTransactionId\":\"OneApi2428657207995\",\"roundId\":\"OneApi1437208495621\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":1000,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217458343,\"settledTime\":1773217458343}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("6.05.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"3900941b-2b3c-4f71-9f31-170eb62577e4\",\"username\":\"dever10506\",\"transactionId\":\"OneApi97f5c76a-90de-47b6-84b3-85316dca3216\",\"betId\":\"OneApi7c2a1e3e-f7ee-4489-a578-51e7e77acf7d\",\"externalTransactionId\":\"OneApi2428657207995\",\"roundId\":\"OneApi1437208495621\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":0,\"winLoss\":0,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217458610,\"settledTime\":1773217458610}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .step("6.05.04", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"0e991a03-7f34-41c3-9984-939803219562\",\"username\":\"dever10506\",\"transactionId\":\"OneApie333e988-71a8-450d-8e25-976cd77237d7\",\"betId\":\"OneApi7c2a1e3e-f7ee-4489-a578-51e7e77acf7d\",\"externalTransactionId\":\"OneApi2428657207995\",\"roundId\":\"OneApi1437208495621\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":1000,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217458835,\"settledTime\":1773217458835}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();

            // 6.6

            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("6.06.01", (context -> {
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
                    .step("6.06.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"ade6b90e-25a2-4433-8e87-79ec72787b70\",\"username\":\"dever10506\",\"transactionId\":\"OneApi039e6f7c-5c3e-46c8-9269-bff272d26395\",\"betId\":\"OneApi4590aaac-ead5-40ae-8da8-009efe34576b\",\"externalTransactionId\":\"OneApi9978356436879\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi3211996431966\",\"timestamp\":1773217459252}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("6.06.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"c1946187-ca29-4044-a267-7a507e23a46a\",\"username\":\"dever10506\",\"transactionId\":\"OneApi62c19b87-5914-47a2-982d-3cf7ef33c1cf\",\"betId\":\"OneApi4590aaac-ead5-40ae-8da8-009efe34576b\",\"externalTransactionId\":\"OneApi9978356436879\",\"roundId\":\"OneApi3211996431966\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":1000,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217459469,\"settledTime\":1773217459469}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")



                    .execute();
            // 6.7
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("6.07.01", (context -> {
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
                    .step("6.07.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"2c1630b7-7d10-496c-8ba1-94489baab4b7\",\"username\":\"dever10506\",\"transactionId\":\"OneApi8134a209-2302-417a-a0a4-051e3b1c0ddb\",\"betId\":\"OneApi208b1b61-c457-4acf-9670-fc5d0e9755a6\",\"externalTransactionId\":\"OneApi7942485217249\",\"roundId\":\"OneApi1957172051696\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":1000,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217459939,\"settledTime\":1773217459939}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")




                    .execute();

            return ValidationResult.success(code(), "SUCCESS");

        } catch (Exception e) {
            return ValidationResult.failure(code(), "FAIL", e.getMessage());
        }


    }


}
