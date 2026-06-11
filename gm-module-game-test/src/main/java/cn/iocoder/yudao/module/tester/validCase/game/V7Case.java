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
public class V7Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId = IdUtil.fastUUID();

    @Override
    public String code() {
        return "v7";
    }


    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {


        try {
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("7.01.01", (context -> {
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
                    .step("7.01.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"bd978426-930d-434c-894f-b650879f3dc9\",\"username\":\"dever10506\",\"transactionId\":\"OneApi71ff2fd7-5804-4425-aef1-cc5c7e88522e\",\"betId\":\"OneApi6912e76c-f52b-434e-97e0-ea9db6a812d1\",\"externalTransactionId\":\"OneApi6346285768052\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi6848408278246\",\"timestamp\":1773217460423}");




                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("7.01.03", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"10a8df49-9a8f-4648-8e2c-dc7296a80b6d\",\"transactionId\":\"OneApi41423701-e586-41c8-8b09-67887e6982a1\",\"betId\":\"OneApi6912e76c-f52b-434e-97e0-ea9db6a812d1\",\"externalTransactionId\":\"OneApi6346285768052\",\"roundId\":\"OneApi6848408278246\",\"gameCode\":\"PP_ar10plinko\",\"username\":\"dever10506\",\"currency\":\"IDR(K)\",\"timestamp\":1773217460647}");
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


            //7.2
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("7.02.01", (context -> {
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
                    .step("7.02.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"e6dc209b-664d-4c38-b953-bed92cdd5836\",\"username\":\"dever10506\",\"transactionId\":\"OneApi7cd9105b-e0bc-477e-9ca1-2e8deb8e2874\",\"betId\":\"OneApi363046e6-ba42-4a3a-a0d4-26aa45aff5a0\",\"externalTransactionId\":\"OneApi5673506797485\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi1257936007113\",\"timestamp\":1773217461085}");


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

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"21fcc53e-338e-4822-a352-fe707bbcec0b\",\"username\":\"dever10506\",\"transactionId\":\"OneApiab403ede-52ca-4a40-9118-3a3df049e6bc\",\"betId\":\"OneApi363046e6-ba42-4a3a-a0d4-26aa45aff5a0\",\"externalTransactionId\":\"OneApi5673506797485\",\"roundId\":\"OneApi1257936007113\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":0,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217461322,\"settledTime\":1773217461322}");
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
                    .step("7.02.04", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"732bad11-d81b-48ff-b449-1fa2895ef184\",\"transactionId\":\"OneApi5ab06763-454e-44f0-9fc3-8d806a4b0804\",\"betId\":\"OneApi363046e6-ba42-4a3a-a0d4-26aa45aff5a0\",\"externalTransactionId\":\"OneApi5673506797485\",\"roundId\":\"OneApi1257936007113\",\"gameCode\":\"PP_ar10plinko\",\"username\":\"dever10506\",\"currency\":\"IDR(K)\",\"timestamp\":1773217461536}");
                        dsRequest.setUsername(context.getPullUsername());
                        dsRequest.setTransactionId(IdUtil.fastUUID());



                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET_UPDATE.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();



            //7.3
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("7.03.01", (context -> {
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
                    .step("7.03.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"e8351bd2-b638-4cac-a01e-936c2347b19a\",\"username\":\"dever10506\",\"transactionId\":\"OneApi857dd303-54a5-46d9-b7ef-4a72597b5b91\",\"betId\":\"OneApi5342c8a1-2511-407b-af4d-994714cf8fcf\",\"externalTransactionId\":\"OneApi253140776943\",\"roundId\":\"OneApi2520961110886\",\"betAmount\":5,\"winAmount\":0,\"effectiveTurnover\":5,\"winLoss\":-5,\"jackpotAmount\":0,\"resultType\":\"BET_LOSE\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217461998,\"settledTime\":1773217461998}");


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
                    .step("7.03.03", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"a6668fe9-ba4a-4dd9-b649-723919820660\",\"transactionId\":\"OneApi87e943ac-37ec-481f-a259-3b6e587023e1\",\"betId\":\"OneApi5342c8a1-2511-407b-af4d-994714cf8fcf\",\"externalTransactionId\":\"OneApi253140776943\",\"roundId\":\"OneApi2520961110886\",\"gameCode\":\"PP_ar10plinko\",\"username\":\"dever10506\",\"currency\":\"IDR(K)\",\"timestamp\":1773217462219}");



                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")
                    .execute();

            // 7.4
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("7.04.01", (context -> {
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
                    .step("7.04.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"403e8998-868c-4b5b-a85f-0b0d9e70711d\",\"username\":\"dever10506\",\"transactionId\":\"OneApi37a50ffd-5fef-4d82-98d8-11a252fbf501\",\"betId\":\"OneApi2a8c681e-dc04-4e00-8a24-7d1c67def110\",\"externalTransactionId\":\"OneApi4872270261570\",\"roundId\":\"OneApi2610222981368\",\"betAmount\":5,\"winAmount\":15,\"effectiveTurnover\":5,\"winLoss\":10,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217462672,\"settledTime\":1773217462672}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("7.04.03", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"7c3c79fd-b7bd-4902-9324-412a71c9acdb\",\"transactionId\":\"OneApi792456ba-3e38-40b8-a52b-06ccc3b04565\",\"betId\":\"OneApi2a8c681e-dc04-4e00-8a24-7d1c67def110\",\"externalTransactionId\":\"OneApi4872270261570\",\"roundId\":\"OneApi2610222981368\",\"gameCode\":\"PP_ar10plinko\",\"username\":\"dever10506\",\"currency\":\"IDR(K)\",\"timestamp\":1773217462942}");




                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .execute();

            // 7.5
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("7.05.01", (context -> {
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
                    .step("7.05.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"2a9ec76b-44ab-45ce-a859-fc7ca577340f\",\"username\":\"dever10506\",\"transactionId\":\"2a9ec76b-44ab-45ce-a859-fc7ca577340f\",\"roundId\":\"OneApi2907490667465\",\"amount\":1000,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217463417}");


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("7.05.03", context -> {

                        DSRollback dsRequest = DSRollback.of("{\"traceId\":\"ab311cd6-1479-4a0b-a69b-66857cd2a2cb\",\"username\":\"dever10506\",\"transactionId\":\"ab311cd6-1479-4a0b-a69b-66857cd2a2cb\",\"betId\":\"ab311cd6-1479-4a0b-a69b-66857cd2a2cb\",\"roundId\":\"OneApi2907490667465\",\"isRefund\":1,\"amount\":1000,\"betAmount\":1000,\"winAmount\":0,\"effectiveTurnover\":1000,\"winLoss\":-1000,\"jackpotAmount\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217463642,\"settledTime\":1773217463642,\"timestamp\":1773217463642}");



                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

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
