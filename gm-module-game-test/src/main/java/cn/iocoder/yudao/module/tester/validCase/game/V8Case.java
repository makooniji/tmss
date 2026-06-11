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

/**
 * JLS 平台余额验证案例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V8Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId = IdUtil.fastUUID();

    @Override
    public String code() {
        return "v8";
    }


    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {


        try {
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.01.01", (context -> {
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
                    .step("8.01.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"101576af-50c3-4071-b408-f31f7438562c\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2e722cdc-8878-4159-89f0-33fd0bf306c0\",\"betId\":\"OneApi552464ab-ded2-4c97-8f02-f2b528bbedeb\",\"externalTransactionId\":\"OneApi2358172898907\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi5418566526739\",\"timestamp\":1773217464188}");




                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.01.03", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"ff53b540-c67c-4cbd-9648-8d607a71f4ae\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2e722cdc-8878-4159-89f0-33fd0bf306c0\",\"betId\":\"OneApi552464ab-ded2-4c97-8f02-f2b528bbedeb\",\"externalTransactionId\":\"OneApi2358172898907\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi5418566526739\",\"timestamp\":1773217464402}");


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();


            //8.2
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.02.01", (context -> {
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
                    .step("8.02.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"345793f0-330e-433e-a4f8-eec099f491a6\",\"username\":\"dever10506\",\"transactionId\":\"OneApi4298e707-6f12-4090-8521-215c4f14e84a\",\"betId\":\"OneApi06a68753-5790-4612-b24e-226aa4bdd845\",\"externalTransactionId\":\"OneApi9832223041230\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi7496018873826\",\"timestamp\":1773217464817}");



                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("8.02.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"baf79c4b-7f02-48c9-9171-2b7e80f2f0fc\",\"username\":\"dever10506\",\"transactionId\":\"OneApib100edfb-afe2-4165-86f6-2884c63376b3\",\"betId\":\"OneApi06a68753-5790-4612-b24e-226aa4bdd845\",\"externalTransactionId\":\"OneApi9832223041230\",\"roundId\":\"OneApi7496018873826\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217465075,\"settledTime\":1773217465075}");
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
                    .step("8.02.04", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"71c39307-0c8e-4dd1-87bb-0bd8930c331b\",\"username\":\"dever10506\",\"transactionId\":\"OneApib100edfb-afe2-4165-86f6-2884c63376b3\",\"betId\":\"OneApi06a68753-5790-4612-b24e-226aa4bdd845\",\"externalTransactionId\":\"OneApi9832223041230\",\"roundId\":\"OneApi7496018873826\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217465308,\"settledTime\":1773217465308}");
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



            //8.3
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.03.01", (context -> {
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
                    .step("8.03.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"8ff9f8f5-5e3d-4adf-b4ef-db55a3c7c5c7\",\"username\":\"dever10506\",\"transactionId\":\"OneApi56c892fe-6b0f-4311-b120-62cd412751fe\",\"betId\":\"OneApi1a287d07-af12-400b-b1bd-9cea16656005\",\"externalTransactionId\":\"OneApi646696528984\",\"roundId\":\"OneApi4022888747664\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217465750,\"settledTime\":1773217465750}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    // 下注大于之前余额
                    .step("8.03.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"daf0ba37-51fb-4869-ba12-8706bf450167\",\"username\":\"dever10506\",\"transactionId\":\"OneApi56c892fe-6b0f-4311-b120-62cd412751fe\",\"betId\":\"OneApi1a287d07-af12-400b-b1bd-9cea16656005\",\"externalTransactionId\":\"OneApi646696528984\",\"roundId\":\"OneApi4022888747664\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217465977,\"settledTime\":1773217465977}");


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

            // 8.4
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.04.01", (context -> {
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
                    .step("8.04.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"d477cd76-d59e-49e5-99af-dd03c0f8a7df\",\"username\":\"dever10506\",\"transactionId\":\"OneApi5183273b-c668-4ef7-822e-6f730bccde87\",\"betId\":\"OneApi310a298d-8156-4f88-adee-980502b75dec\",\"externalTransactionId\":\"OneApi528828736071\",\"roundId\":\"OneApi7759151008508\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217466364,\"settledTime\":1773217466364}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.04.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"541df722-3259-4432-bfcb-c4e58a124651\",\"username\":\"dever10506\",\"transactionId\":\"OneApi5183273b-c668-4ef7-822e-6f730bccde87\",\"betId\":\"OneApi310a298d-8156-4f88-adee-980502b75dec\",\"externalTransactionId\":\"OneApi528828736071\",\"roundId\":\"OneApi7759151008508\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217466629,\"settledTime\":1773217466629}");


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

            // 8.5
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.05.01", (context -> {
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
                    .step("8.05.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"3d407337-b9b4-4234-ba47-4d616aabab7a\",\"username\":\"dever10506\",\"transactionId\":\"OneApidacea491-2e83-4196-9c0b-1cde7e64db5e\",\"betId\":\"OneApid00b9987-dbd2-4b05-bbf3-415207a934eb\",\"externalTransactionId\":\"OneApi50699947377\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi8187226554789\",\"timestamp\":1773217467025}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.05.03", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"ce9cbd4e-424c-46b9-8e2f-0ca8630169c6\",\"transactionId\":\"OneApi5bb1d5a6-1a02-44cc-b016-d7c12f4ce4c3\",\"betId\":\"OneApid00b9987-dbd2-4b05-bbf3-415207a934eb\",\"externalTransactionId\":\"OneApi50699947377\",\"roundId\":\"OneApi8187226554789\",\"gameCode\":\"PP_ar10plinko\",\"username\":\"dever10506\",\"currency\":\"IDR(K)\",\"timestamp\":1773217467253}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.REFUND.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .step("8.05.04", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"ff7fb679-854d-44e1-bcae-e3856c5691f4\",\"transactionId\":\"OneApi5bb1d5a6-1a02-44cc-b016-d7c12f4ce4c3\",\"betId\":\"OneApid00b9987-dbd2-4b05-bbf3-415207a934eb\",\"externalTransactionId\":\"OneApi50699947377\",\"roundId\":\"OneApi8187226554789\",\"gameCode\":\"PP_ar10plinko\",\"username\":\"dever10506\",\"currency\":\"IDR(K)\",\"timestamp\":1773217467482}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.REFUND.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();

            // 8.6

            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.06.01", (context -> {
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
                    .step("8.06.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"c48701d2-2254-45dc-91d5-e9724cb5cc62\",\"username\":\"dever10506\",\"transactionId\":\"OneApi3769ac5b-5cfb-46e9-a3f0-b79d389f188c\",\"betId\":\"OneApi4b1c43b1-1265-468c-ae16-7d58e65eaf77\",\"externalTransactionId\":\"OneApi4007219202740\",\"amount\":36640,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi6596800632818\",\"timestamp\":1773217467894}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.06.03", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"330fb87e-03ef-45bb-880b-67a2c3dbe434\",\"username\":\"dever10506\",\"transactionId\":\"OneApi3769ac5b-5cfb-46e9-a3f0-b79d389f188c\",\"betId\":\"OneApi4b1c43b1-1265-468c-ae16-7d58e65eaf77\",\"externalTransactionId\":\"OneApi4007219202740\",\"amount\":36640,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi6596800632818\",\"timestamp\":1773217468114}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")



                    .execute();
            // 8.7
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.07.01", (context -> {
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
                    .step("8.07.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"697359f8-94fe-45c6-8bcb-eb3579ce9cd6\",\"username\":\"dever10506\",\"transactionId\":\"OneApifc830c4c-3913-46f4-98bf-c848bfe00f06\",\"betId\":\"OneApia28dc251-f190-490a-9c21-ddf4a12a703a\",\"externalTransactionId\":\"OneApi2454251476447\",\"roundId\":\"OneApi1693571901728\",\"betAmount\":36640,\"winAmount\":10,\"effectiveTurnover\":36640,\"winLoss\":-36630,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217468552,\"settledTime\":1773217468552}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.07.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"a7e4d208-01b9-47d0-bfaf-c331e81bbe46\",\"username\":\"dever10506\",\"transactionId\":\"OneApifc830c4c-3913-46f4-98bf-c848bfe00f06\",\"betId\":\"OneApia28dc251-f190-490a-9c21-ddf4a12a703a\",\"externalTransactionId\":\"OneApi2454251476447\",\"roundId\":\"OneApi1693571901728\",\"betAmount\":36640,\"winAmount\":10,\"effectiveTurnover\":36640,\"winLoss\":-36630,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217468776,\"settledTime\":1773217468776}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")




                    .execute();


            // 8.8
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.08.01", (context -> {
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
                    .step("8.08.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"3e0c1fa6-1b76-4208-8dce-f9fedff8349e\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2abedc10-be2e-41db-b781-75aaf5b3a0bc\",\"betId\":\"OneApi53198d68-c17a-459f-b0db-8f5ad49043f8\",\"externalTransactionId\":\"OneApi945038917880\",\"roundId\":\"OneApi2853211015518\",\"betAmount\":36640,\"winAmount\":0,\"effectiveTurnover\":36640,\"winLoss\":-36640,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217469196,\"settledTime\":1773217469196}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.08.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"46aff3a3-788c-41ca-be7a-73e633159e3a\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2abedc10-be2e-41db-b781-75aaf5b3a0bc\",\"betId\":\"OneApi53198d68-c17a-459f-b0db-8f5ad49043f8\",\"externalTransactionId\":\"OneApi945038917880\",\"roundId\":\"OneApi2853211015518\",\"betAmount\":36640,\"winAmount\":0,\"effectiveTurnover\":36640,\"winLoss\":-36640,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217469384,\"settledTime\":1773217469384}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .execute();
            //8.9
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.09.01", (context -> {
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
                    .step("8.09.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"3e0c1fa6-1b76-4208-8dce-f9fedff8349e\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2abedc10-be2e-41db-b781-75aaf5b3a0bc\",\"betId\":\"OneApi53198d68-c17a-459f-b0db-8f5ad49043f8\",\"externalTransactionId\":\"OneApi945038917880\",\"roundId\":\"OneApi2853211015518\",\"betAmount\":36640,\"winAmount\":0,\"effectiveTurnover\":36640,\"winLoss\":-36640,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217469196,\"settledTime\":1773217469196}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.09.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"46aff3a3-788c-41ca-be7a-73e633159e3a\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2abedc10-be2e-41db-b781-75aaf5b3a0bc\",\"betId\":\"OneApi53198d68-c17a-459f-b0db-8f5ad49043f8\",\"externalTransactionId\":\"OneApi945038917880\",\"roundId\":\"OneApi2853211015518\",\"betAmount\":36640,\"winAmount\":0,\"effectiveTurnover\":36640,\"winLoss\":-36640,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217469384,\"settledTime\":1773217469384}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.09.04", context -> {

                        DSAdjustment dsRequest = DSAdjustment.of("{\"traceId\":\"46aff3a3-788c-41ca-be7a-73e633159e3a\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2abedc10-be2e-41db-b781-75aaf5b3a0bc\",\"betId\":\"OneApi53198d68-c17a-459f-b0db-8f5ad49043f8\",\"externalTransactionId\":\"OneApi945038917880\",\"roundId\":\"OneApi2853211015518\",\"betAmount\":36640,\"winAmount\":0,\"effectiveTurnover\":36640,\"winLoss\":-36640,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217469384,\"settledTime\":1773217469384}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.09.05", context -> {

                        DSAdjustment dsRequest = DSAdjustment.of("{\"traceId\":\"46aff3a3-788c-41ca-be7a-73e633159e3a\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2abedc10-be2e-41db-b781-75aaf5b3a0bc\",\"betId\":\"OneApi53198d68-c17a-459f-b0db-8f5ad49043f8\",\"externalTransactionId\":\"OneApi945038917880\",\"roundId\":\"OneApi2853211015518\",\"betAmount\":36640,\"winAmount\":0,\"effectiveTurnover\":36640,\"winLoss\":-36640,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217469384,\"settledTime\":1773217469384}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .execute();
            //8.10
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.10.01", (context -> {
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
                    .step("8.10.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"d96cc289-7750-4e4a-a614-9eec74fa0e20\",\"username\":\"dever10506\",\"transactionId\":\"OneApi12d46b96-e3c6-498c-bbef-4939e1a1aee4\",\"betId\":\"OneApi3fb67289-c021-4934-ab16-5ef4f7b82938\",\"externalTransactionId\":\"OneApi1834324239791\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi2243873145463\",\"timestamp\":1773217470973}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.10.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"78b0f806-af37-483a-b9f0-dfcef54792b4\",\"username\":\"dever10506\",\"transactionId\":\"OneApi78741a7c-8961-45ee-94e7-1890226a4548\",\"betId\":\"OneApi3fb67289-c021-4934-ab16-5ef4f7b82938\",\"externalTransactionId\":\"OneApi1834324239791\",\"roundId\":\"OneApi2243873145463\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":0,\"winLoss\":0,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217471213,\"settledTime\":1773217471213}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.10.04", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"2c904b37-cafc-4229-a99b-c0c1ab7d35be\",\"username\":\"dever10506\",\"transactionId\":\"OneApi3b803d48-c4bf-4cb0-b12e-00431c048c3d\",\"betId\":\"OneApi3fb67289-c021-4934-ab16-5ef4f7b82938\",\"externalTransactionId\":\"OneApi1834324239791\",\"roundId\":\"OneApi2243873145463\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217471456,\"settledTime\":1773217471456}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.10.05", context -> {

                        DSAdjustment dsRequest = DSAdjustment.of("{\"traceId\":\"4ad093f7-c7e0-42b5-9f12-41365188cf8c\",\"username\":\"dever10506\",\"transactionId\":\"OneApi74acaf12-6b37-4503-982a-c0aded92c281\",\"externalTransactionId\":\"OneApi1834324239791\",\"roundId\":\"OneApi2243873145463\",\"amount\":5,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"timestamp\":1773217471660}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.ADJUSTMENT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .step("8.10.06", context -> {

                        DSAdjustment dsRequest = DSAdjustment.of("{\"traceId\":\"4ad093f7-c7e0-42b5-9f12-41365188cf8c\",\"username\":\"dever10506\",\"transactionId\":\"OneApi74acaf12-6b37-4503-982a-c0aded92c281\",\"externalTransactionId\":\"OneApi1834324239791\",\"roundId\":\"OneApi2243873145463\",\"amount\":5,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"timestamp\":1773217471660}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.ADJUSTMENT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .execute();

            //8.11
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.11.01", (context -> {
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
                    .step("8.11.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"0a419a40-a6aa-456d-b4b1-83343cca9c1c\",\"username\":\"dever10506\",\"transactionId\":\"0a419a40-a6aa-456d-b4b1-83343cca9c1c\",\"roundId\":\"OneApi547669992672\",\"amount\":1000,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217472312}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.11.03", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"c781fee9-ded1-41f0-b947-230cdf73b4ce\",\"username\":\"dever10506\",\"transactionId\":\"0a419a40-a6aa-456d-b4b1-83343cca9c1c\",\"roundId\":\"OneApi547669992672\",\"amount\":1000,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217472558}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")


                    .execute();
            // 8.12
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.12.01", (context -> {
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
                    .step("8.12.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"06bcaf54-4391-466d-be87-317e2d7d6a7e\",\"username\":\"dever10506\",\"transactionId\":\"06bcaf54-4391-466d-be87-317e2d7d6a7e\",\"roundId\":\"OneApi5521594420297\",\"amount\":35650,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217473060}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.12.03", context -> {

                        DSDebit dsRequest = DSDebit.of("\n" +
                                "{\"traceId\":\"85672afc-d801-45ff-b0fc-4904cb52f3ab\",\"username\":\"dever10506\",\"transactionId\":\"06bcaf54-4391-466d-be87-317e2d7d6a7e\",\"roundId\":\"OneApi5521594420297\",\"amount\":35650,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217473277}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")


                    .execute();

            // 8.13
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.13.01", (context -> {
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
                    .step("8.13.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"582f278e-8d31-4ef7-a7c2-89d72dec68cb\",\"username\":\"dever10506\",\"transactionId\":\"582f278e-8d31-4ef7-a7c2-89d72dec68cb\",\"roundId\":\"OneApi3004945512738\",\"amount\":1000,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217473717}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.13.03", context -> {

                        DSCredit dsRequest = DSCredit.of("{\"traceId\":\"28ff81f3-ff7e-4d30-8fd7-fe6dc11cfb3d\",\"username\":\"dever10506\",\"transactionId\":\"28ff81f3-ff7e-4d30-8fd7-fe6dc11cfb3d\",\"betId\":\"28ff81f3-ff7e-4d30-8fd7-fe6dc11cfb3d\",\"roundId\":\"OneApi3004945512738\",\"isRefund\":0,\"amount\":2000,\"betAmount\":1000,\"winAmount\":2000,\"effectiveTurnover\":1000,\"winLoss\":1000,\"jackpotAmount\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217473945,\"settledTime\":1773217473945,\"timestamp\":1773217473945}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.CREDIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.13.04", context -> {

                        DSCredit dsRequest = DSCredit.of("{\"traceId\":\"970c9462-83da-439a-8471-6af4e2d07520\",\"username\":\"dever10506\",\"transactionId\":\"28ff81f3-ff7e-4d30-8fd7-fe6dc11cfb3d\",\"betId\":\"28ff81f3-ff7e-4d30-8fd7-fe6dc11cfb3d\",\"roundId\":\"OneApi3004945512738\",\"isRefund\":0,\"amount\":2000,\"betAmount\":1000,\"winAmount\":2000,\"effectiveTurnover\":1000,\"winLoss\":1000,\"jackpotAmount\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217474186,\"settledTime\":1773217474186,\"timestamp\":1773217474186}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.CREDIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")


                    .execute();

            // 8.14
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("8.14.01", (context -> {
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
                    .step("8.14.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"81a51ba8-64dc-447a-9d78-d19eb2a1fdb2\",\"username\":\"dever10506\",\"transactionId\":\"81a51ba8-64dc-447a-9d78-d19eb2a1fdb2\",\"roundId\":\"OneApi9243599948132\",\"amount\":1000,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217474589}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.14.03", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"5d9d9baf-8fc0-47cb-a965-8a263f142a07\",\"username\":\"dever10506\",\"transactionId\":\"5d9d9baf-8fc0-47cb-a965-8a263f142a07\",\"betId\":\"5d9d9baf-8fc0-47cb-a965-8a263f142a07\",\"roundId\":\"OneApi9243599948132\",\"isRefund\":1,\"amount\":1000,\"betAmount\":1000,\"winAmount\":0,\"effectiveTurnover\":1000,\"winLoss\":-1000,\"jackpotAmount\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217474804,\"settledTime\":1773217474804,\"timestamp\":1773217474804}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.REFUND.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("8.14.04", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"dedec632-00f3-4559-8797-5e05fb2b31a1\",\"username\":\"dever10506\",\"transactionId\":\"5d9d9baf-8fc0-47cb-a965-8a263f142a07\",\"betId\":\"5d9d9baf-8fc0-47cb-a965-8a263f142a07\",\"roundId\":\"OneApi9243599948132\",\"isRefund\":1,\"amount\":1000,\"betAmount\":1000,\"winAmount\":0,\"effectiveTurnover\":1000,\"winLoss\":-1000,\"jackpotAmount\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217475038,\"settledTime\":1773217475038,\"timestamp\":1773217475038}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.REFUND.getPath(), dsRequest);

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
