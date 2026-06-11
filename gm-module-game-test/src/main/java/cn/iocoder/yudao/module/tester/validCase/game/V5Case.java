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
public class V5Case implements ValidationCase {

    private final DSManager dsManager;
    private final WebSocketMessageSender ws;
    private final String betRollBackId = IdUtil.fastUUID();

    @Override
    public String code() {
        return "v5";
    }

    @Override
    public ValidationResult execute(ValidationRunRequest request, SportContext ctx) {


        try {
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.01.01", (context -> {
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
                    .step("5.01.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"090d4405-375d-4f55-b498-d078309afe9b\",\"username\":\"dever10506\",\"transactionId\":\"OneApicbed483c-00ce-4724-910c-747adba60014\",\"betId\":\"OneApie773dee6-9014-4394-b121-579ff869475c\",\"externalTransactionId\":\"OneApi7734834793759\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi9861400700484\",\"timestamp\":1773217449979}");




                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("5.01.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"dfc5f536-4d30-4f93-9a25-a322fc4b4e06\",\"username\":\"dever10506\",\"transactionId\":\"OneApi00311929-32fe-4baf-88c8-9a0cef70087c\",\"betId\":\"OneApie773dee6-9014-4394-b121-579ff869475c\",\"externalTransactionId\":\"OneApi7734834793759\",\"roundId\":\"OneApi9861400700484\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217450235,\"settledTime\":1773217450235}");
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


            //5.2
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.02.01", (context -> {
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
                    .step("5.02.02", context -> {

                        DSBetRequest dsRequest = DSBetRequest.of("{\"traceId\":\"352f465d-2065-45c9-9513-18eb1fc56529\",\"username\":\"dever10506\",\"transactionId\":\"OneApi5974085c-b811-4853-a634-ad9714b21a2c\",\"betId\":\"OneApi4a5e8b26-e9bf-4ab1-847f-ae21737f7d3f\",\"externalTransactionId\":\"OneApi3920741035019\",\"amount\":5,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"roundId\":\"OneApi5574814023809\",\"timestamp\":1773217450685}");




                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.BET.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    // 下注大于之前余额
                    .step("5.02.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of(
                                "{\"traceId\":\"d1f8b504-c643-4291-8b27-bffb22bcb94e\",\"username\":\"dever10506\",\"transactionId\":\"OneApif9c36efa-3ee0-4255-ad8a-06a2225aee9e\",\"betId\":\"OneApi4a5e8b26-e9bf-4ab1-847f-ae21737f7d3f\",\"externalTransactionId\":\"OneApi3920741035019\",\"roundId\":\"OneApi5574814023809\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":0,\"winLoss\":0,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217450917,\"settledTime\":1773217450917}");
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
                    .step("5.02.04", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"17881adc-a5c5-40a1-baab-82e374a4b9a0\",\"username\":\"dever10506\",\"transactionId\":\"OneApi5f62d65f-5ff2-4974-a4a9-f866cbeac406\",\"betId\":\"OneApi4a5e8b26-e9bf-4ab1-847f-ae21737f7d3f\",\"externalTransactionId\":\"OneApi3920741035019\",\"roundId\":\"OneApi5574814023809\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217451153,\"settledTime\":1773217451153}");
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



            //5.3
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

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("\n" +
                                "{\"traceId\":\"90425bba-738d-487b-8bf1-49d802c23ba2\",\"username\":\"dever10506\",\"transactionId\":\"OneApib9d81ee8-6c5a-478d-af27-bdd913c0b309\",\"betId\":\"OneApi2ea22594-adc9-403c-9a95-912b1b546fea\",\"externalTransactionId\":\"OneApi2465372907145\",\"roundId\":\"OneApi1022402563704\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217451584,\"settledTime\":1773217451584}");


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

            // 5.4
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.04.01", (context -> {
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
                    .step("5.04.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"7a944cb6-7494-471f-b820-ae17f2d07913\",\"username\":\"dever10506\",\"transactionId\":\"OneApi9c8bd674-0554-4969-9a30-c055dbb604dc\",\"betId\":\"OneApi5f336079-09ec-46b1-bdb0-fdf4c4d357c0\",\"externalTransactionId\":\"OneApi6748984502470\",\"roundId\":\"OneApi5441123534126\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":0,\"winLoss\":10,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217452070,\"settledTime\":1773217452070}");


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

            // 5.5
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.05.01", (context -> {
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
                    .step("5.05.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"cfbd650a-b273-4a03-a3f9-1b24bc9f49d6\",\"username\":\"dever10506\",\"transactionId\":\"OneApia7a3c73a-1ed7-4145-b5ea-f080d24054d2\",\"betId\":\"OneApi47c3c8bf-e82e-4cbf-ad3f-8e0502b22bc0\",\"externalTransactionId\":\"OneApi6564723921516\",\"roundId\":\"OneApi3403878754031\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217452531,\"settledTime\":1773217452531}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("5.05.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"ca6fde0e-f6e5-4067-a077-487b0a8b97c7\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2fbe3542-3f8b-4ae2-8786-fdbb9cd5b247\",\"betId\":\"OneApi47c3c8bf-e82e-4cbf-ad3f-8e0502b22bc0\",\"externalTransactionId\":\"OneApi6564723921516\",\"roundId\":\"OneApi3403878754031\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217452814,\"settledTime\":1773217452814}");


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

            // 5.6

            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.06.01", (context -> {
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
                    .step("5.06.02", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"f17d1f0c-7410-4ece-acca-0c111ad43c4d\",\"username\":\"dever10506\",\"transactionId\":\"OneApi745126c8-e59a-47cb-b79b-8aeddaef873a\",\"betId\":\"OneApi60ae4acf-cf99-46f5-8890-0983f525ccd5\",\"externalTransactionId\":\"OneApi3944417351480\",\"roundId\":\"OneApi5887286192642\",\"betAmount\":5,\"winAmount\":10,\"effectiveTurnover\":5,\"winLoss\":5,\"jackpotAmount\":0,\"resultType\":\"BET_WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217453219,\"settledTime\":1773217453219}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("5.06.03", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"795e4497-fb37-4864-9538-2d3f23eec226\",\"username\":\"dever10506\",\"transactionId\":\"OneApi2166843d-9b3b-4334-ac1b-2f0e1b17e8d3\",\"betId\":\"OneApi60ae4acf-cf99-46f5-8890-0983f525ccd5\",\"externalTransactionId\":\"OneApi3944417351480\",\"roundId\":\"OneApi5887286192642\",\"betAmount\":0,\"winAmount\":10,\"effectiveTurnover\":0,\"winLoss\":0,\"jackpotAmount\":0,\"resultType\":\"WIN\",\"isFreespin\":0,\"isEndRound\":0,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217453539,\"settledTime\":1773217453539}");


                        dsRequest.setActionEnum(ActionEnum.SETTLED);


                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.SETTLED.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("5.06.04", context -> {

                        DSBetResultRequest dsRequest = DSBetResultRequest.of("{\"traceId\":\"1edcc8a4-4a92-42ad-bbd5-d03d41f74638\",\"username\":\"dever10506\",\"transactionId\":\"OneApia31e97ea-1745-46ba-9e92-eb4d434d3793\",\"betId\":\"OneApi60ae4acf-cf99-46f5-8890-0983f525ccd5\",\"externalTransactionId\":\"OneApi3944417351480\",\"roundId\":\"OneApi5887286192642\",\"betAmount\":5,\"winAmount\":20,\"effectiveTurnover\":5,\"winLoss\":15,\"jackpotAmount\":0,\"resultType\":\"END\",\"isFreespin\":0,\"isEndRound\":1,\"currency\":\"IDR(K)\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"gameCode\":\"PP_ar10plinko\",\"betTime\":1773217453772,\"settledTime\":1773217453772}");


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
            // 5.7
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.07.01", (context -> {
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
                    .step("5.07.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"username\":\"dever10506\",\"transactionId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"roundId\":\"OneApi8264079965379\",\"amount\":19045,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217454216}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("5.07.03", context -> {

                        DSCredit dsRequest = DSCredit.of("{\"traceId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"username\":\"dever10506\",\"transactionId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"roundId\":\"OneApi8264079965379\",\"amount\":19045,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217454216}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.CREDIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")



                    .execute();

            // 5.8
            FlowBuilder.start(ctx, ws)
                    // 1️⃣ 查询余额
                    .step("5.08.01", (context -> {
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
                    .step("5.08.02", context -> {

                        DSDebit dsRequest = DSDebit.of("{\"traceId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"username\":\"dever10506\",\"transactionId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"roundId\":\"OneApi8264079965379\",\"amount\":19045,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217454216}");

                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.DEBIT.getPath(), dsRequest);

                        context.setBalance(response.getData().getBalance());
                        return response;
                    }).after((res, c) -> {
                        c.getVars().put("expectedBalance", ctx.getBalance());
                    })
                    .verify("status == 'OK' && data.balance == #expectedBalance")

                    .step("5.08.03", context -> {

                        DSCredit dsRequest = DSCredit.of("{\"traceId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"username\":\"dever10506\",\"transactionId\":\"e70e35b3-463a-4c74-9114-a02da44a5b7b\",\"roundId\":\"OneApi8264079965379\",\"amount\":19045,\"currency\":\"IDR(K)\",\"gameCode\":\"PP_ar10plinko\",\"token\":\"57aa5d1e-c27c-4cbc-97a9-a674ad487350\",\"timestamp\":1773217454216}");
                        DownstreamResult response =
                                dsManager.push(context, context.getTenant(), ActionEnum.CREDIT.getPath(), dsRequest);

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
