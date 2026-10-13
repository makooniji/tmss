package cn.iocoder.yudao.module.game.adapter.game.lottery;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.model.game.*;
import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSSettle;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.module.system.model.*;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.module.game.adapter.game.lottery.model.LotteryReq;
import cn.iocoder.yudao.module.game.adapter.game.lottery.model.LotteryResponse;
import cn.iocoder.yudao.module.game.adapter.game.lottery.model.PushDataDto;


import cn.iocoder.yudao.module.system.model.game.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.model.game.SportContext;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * WooAPI平台服务实现
 */
@Service
@Slf4j
public class LotteryAdapter extends BaseAdapter {


    @Override
    public PlatformResponse<Object> doBalance(ActionEnum changeType, PlatformRequest param, SportContext ctx) {

        if (ctx.getTenant().getImplType().equals(0)) {
            DSBalance dsBalance = new DSBalance();

            dsBalance.setUsername(ctx.getPushUsername());
            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), BigDecimal.ZERO, ActionEnum.BALANCE.getPath(),
                    dsBalance,

                    (order) -> null,
                    (amt) -> {
                        TenantUtils.execute(ctx.getTenant().getId(), () -> super.asyncBalance(ctx.getBalance(), ctx.getUser().getUsername(), ctx.getWrapCurrency()));
                    });
        }
        return buildBalanceResult(ctx);
    }

    @Override
    public PlatformResponse<Boolean> doSetRtp(GameVendorDO gameVendor, GameApiRtp body) {
        return PlatformResponse.success(true);
    }

    @Override
    public PlatformResponse<Object> doForward(ActionEnum actionEnum, PlatformRequest param, SportContext ctx) {
        JSONObject parseObject = new JSONObject();


        if (ObjectUtil.isEmpty(ctx.getPullUsername())) {
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        ctx.setGameVendor(param.getGameVendor());
        VendorUserDO user = getUser(ctx.getPullUsername(), param.getVendorCode());
        if (ObjectUtil.isEmpty(user)) {
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        String pushUsername = user.getUsername().replace(user.getTenantId() + "_", "");
        ctx.setPushUsername(pushUsername);
        VendorUserBalanceDO balance = balanceService.getBalance(user.getUsername(), ctx.getWrapCurrency());
        if (ObjectUtil.isEmpty(balance)) {
            throw new GameException(PlatformCodeEnum.WRONG_CURRENCY);
        }
        ctx.setBalance(balance.getBalance());
        ctx.setUser(user);


        TenantUtils.execute(user.getTenantId(), () -> {
            var vendorTenantDO = vendorTenantService.getVendorByCode(param.getVendorCode(), user.getTenantId());

            if (ObjectUtil.isEmpty(vendorTenantDO)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            ctx.setVendorTenant(vendorTenantDO);
            if (ObjectUtil.isNotEmpty(vendorTenantDO.getCurrency())) {
                if (!vendorTenantDO.getCurrency().contains(ctx.getCurrency())) {
                    throw new GameException(PlatformCodeEnum.VENDOR_CURRENCY_NOT_SUPPORTED);
                }
            }

            var tenant = tenantMapper.selectById(user.getTenantId());
            if (ObjectUtil.isEmpty(tenant)) {
                throw new GameException(PlatformCodeEnum.TENANT_ERROR);
            }
            ctx.setTenant(tenant);
        });


        Map<String, Object> dataMap = param.getExtraData();

        String changeType = dataMap.getOrDefault("transferType", "0").toString();

        //根据data构建签名,上游验签
        if (ObjectUtil.isNotEmpty(ctx.getSign())) {
            String result = this.buildSign(dataMap, ctx.getGameVendor().getVendorKey());
            if (!result.equals(ctx.getSign())) {
                throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
            }

        }


        if ("/igaming/getBalance".equals(param.getAction())) {

            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException(PlatformCodeEnum.INVALID_GAME);
        }
        GameRecordsDO exists = recordsMapper.selectOne("ref_order_no", ctx.getTransferId());
        if (ObjectUtil.isNotEmpty(exists)) {
            return buildIdleResult(ctx);
        }
        BigDecimal currentBalance = ctx.getBalance();
        switch (changeType) {
            case "1" -> {
                List<DSBetRequest> DSBetRequests = buildBet(param, ctx);

                for (DSBetRequest dsbetRequest : DSBetRequests) {
                    if (NumberUtil.isLess(ctx.getBalance(), dsbetRequest.getAmount())) {
                        throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
                    }
                    GameRecordsDO record = super.getRecord(dsbetRequest.getBetId());
                    if (ObjectUtil.isEmpty(record)) {
                        ExecuteResult execute = super.execute(ctx, ctx.getTenant(), dsbetRequest.getAmount().negate(), ActionEnum.BET.getPath(),
                                dsbetRequest,

                                (order) -> {
                                    DSBetRequest bet = (DSBetRequest) order;

                                    GameRecordsDO game = new GameRecordsDO();
                                    Long id = IdUtil.getSnowflakeNextId();
                                    game.setId(id);
                                    game.setGameCode(ctx.getGameCode());
                                    game.setUsername(ctx.getPushUsername());
                                    game.setRefOrderNo(bet.getTransactionId());
                                    game.setOrderNo(bet.getBetId());
                                    game.setBetContent("");
                                    game.setIssueNo(bet.getRoundId());
                                    game.setAction(ActionEnum.BET);
                                    game.setGameKind(gameInfoDO.getCateId());
                                    game.setBetAmount(bet.getAmount());
                                    game.setWinAmount(BigDecimal.ZERO);
                                    game.setRemark(ActionEnum.BET.getDesc());
                                    game.setOdds(BigDecimal.ZERO);
                                    game.setCreateTime(LocalDateTime.now());
                                    game.setTenantId(ctx.getTenant().getId());
                                    game.setTenantCode(ctx.getTenant().getCode());
                                    game.setCurrency(ctx.getCurrency());
                                    game.setUserId(user.getId());
                                    game.setVendorCode(ctx.getVendorCode());

                                    recordsMapper.insert(game);

                                    GameStaticDTO.buildBet(user, ctx.getGameVendor(), gameInfoDO, game.getBetAmount(),ctx.getCurrency());

                                    return game.getId();
                                },
                                (amt) -> super.operateBalance(amt, dsbetRequest.getUsername(), ctx.getWrapCurrency()));
                    }

                }

                parseObject.put("transferBefore", currentBalance);
                parseObject.put("transferAfter", ctx.getBalance());
                parseObject.put("transferId", ctx.getTransferId());
                parseObject.put("memberAccount", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("timestamp", ctx.getTimestamp());
                return PlatformResponse.success(LotteryResponse.success(parseObject));


            }
            case "3" -> {
                List<DSRollback> dsRollbacks = buildRefund(param, ctx);


                for (DSRollback item : dsRollbacks) {
                    if (item.getCreditAmount().doubleValue() < 0) {
                        if (NumberUtil.isLess(ctx.getBalance(), item.getCreditAmount().abs())) {
                            throw new GameException("SC_INSUFFICIENT_FUNDS");
                        }
                    }
                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.REFUND.getPath(),
                            item,

                            (order) -> {
                                DSRollback updateBet = (DSRollback) order;
                                GameRecordsDO record = super.getRecordByRoundId(updateBet.getRoundId());
                                record.setRemark(ActionEnum.REFUND.getDesc());
                                record.setAction(ActionEnum.REFUND);

                                super.updateRecord(record);

                                GameStaticDTO.buildRefund(user, ctx.getGameVendor(), gameInfoDO, record.getBetAmount(),ctx.getCurrency());

                                return record.getId();
                            },
                            (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                }

                parseObject.put("transferBefore", currentBalance);
                parseObject.put("transferAfter", ctx.getBalance());
                parseObject.put("transferId", ctx.getTransferId());
                parseObject.put("memberAccount", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("timestamp", ctx.getTimestamp());
                return PlatformResponse.success(LotteryResponse.success(parseObject));
            }
            case "2" -> {

                List<DSBetResultRequest> betResults = buildBetResult(param, ctx);

                for (DSBetResultRequest betResult : betResults) {

                    GameRecordsDO record = super.getRecord(ctx.getOrderId());
                    if (ObjectUtil.isEmpty(record)) {
                        if (NumberUtil.isLess(ctx.getBalance(), betResult.getBetAmount())) {
                            throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
                        }
                    }

                    BigDecimal changeAmount = betResult.getWinAmount().subtract(betResult.getBetAmount()).add(betResult.getJackpotAmount());

                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), changeAmount, ActionEnum.SETTLED.getPath(),
                            betResult,

                            (order) -> {
                                DSBetResultRequest bet = (DSBetResultRequest) order;

                                record.setAction(ActionEnum.SETTLED);
                                record.setWinAmount(bet.getWinAmount());
                                record.setRemark(bet.getActionEnum().getDesc());
                                record.setOdds(bet.getOdds());

                                record.setUpdateTime(LocalDateTime.now());
                                record.setJackpotAmount(bet.getJackpotAmount());
                                recordsMapper.updateById(record);
                                GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinLoss().doubleValue() > 0,ctx.getCurrency());
                                return record.getId();
                            },
                            (amt) -> {
                                super.operateBalance(amt, betResult.getUsername(), ctx.getWrapCurrency());
                            });
                }


                parseObject.put("transferBefore", currentBalance);
                parseObject.put("transferAfter", ctx.getBalance());
                parseObject.put("transferId", ctx.getTransferId());
                parseObject.put("memberAccount", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("timestamp", ctx.getTimestamp());

                return PlatformResponse.success(LotteryResponse.success(parseObject));
            }

        }

        parseObject.put("transferBefore", currentBalance);
        parseObject.put("transferAfter", ctx.getBalance());
        parseObject.put("transferId", ctx.getTransferId());
        parseObject.put("memberAccount", ctx.getPullUsername());
        parseObject.put("currency", ctx.getCurrency());
        parseObject.put("timestamp", ctx.getTimestamp());

        return PlatformResponse.success(LotteryResponse.success(parseObject));

    }


    @Override
    public String getVendorCode() {
        return "LOTTERY";
    }

    @Override
    public PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request) {

        String username = vendor.getVendorChannel() + "_" + request.getUsername();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", vendor.getVendorAgent());
        params.put("accounts", username);
        BigDecimal memberBalance = resolveBalance(request.getUsername(), request.getCurrency());

        params.put("key", "memberAccount=" + username + "&memberBalance=" + memberBalance + "&memberName=" + username + "&merchantAccount=" + vendor.getVendorAgent());
        Map<Object, Object> build = MapUtil.builder().put("avatar", "").put("email", "")
                .put("memberAccount", username)
                .put("memberName", username)
                .put("merchantAccount", vendor.getVendorAgent())
                .put("mobile", "")
                .build();
        params.put("data", build);
        params.put("ts", System.currentTimeMillis());
        String sign = buildSign(params, vendor.getVendorKey());
        params.put("sign", sign);
        this.doRequest(vendor, "/api/member/register", params, "LOTTERY创建会员账号", sign, false);
        return PlatformResponse.success(username);
    }


    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request, VendorUserDO gameUser) {
        Map<String, Object> params = new HashMap<>();
        BigDecimal memberBalance = resolveBalance(gameUser.getUsername(), request.getCurrency());
        params.put("merchantAccount", vendor.getVendorAgent());
        params.put("memberAccount", request.getUsername());
        params.put("memberName", request.getUsername());
        params.put("ticketCode", request.getGameCode());
        params.put("memberBalance", memberBalance);
        String country = "VIET";
        params.put("country", country);
        String lang = "en";
        params.put("lobbyUrl", request.getLobbyUrl());
        params.put("language", lang);


        Map<Object, Object> build = MapUtil.builder().put("avatar", "").put("email", "")
                .put("memberAccount", request.getUsername())
                .put("memberName", request.getUsername())
                .put("memberBalance", memberBalance)
                .put("merchantAccount", vendor.getVendorAgent())
                .put("mobile", "")
                .put("ticketCode", request.getGameCode())
                .build();
        params.put("data", build);

        params.put("ts", System.currentTimeMillis());

        String sign = this.buildSign(params, vendor.getVendorKey());
        params.put("sign", sign);

        JSONObject dataMap = doRequest(vendor, "/api/member/login", params, "LOTTERY获取游戏地址", sign, false);

        if (!dataMap.getString("code").equals("0")) {
            throw new GameException(dataMap.getString("code"));
        }

        JSONObject data = dataMap.getJSONObject("data");
        return PlatformResponse.success(data.getString("gameUrl"));
    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", vendor.getVendorAgent());
        params.put("accounts", account);

        Map<Object, Object> build = MapUtil.builder()
                .put("memberAccount", account)
                .put("merchantAccount", vendor.getVendorAgent())
                .build();
        params.put("data", build);
        params.put("ts", System.currentTimeMillis());
        String sign = this.buildSign(params, vendor.getVendorKey());
        params.put("sign", sign);
        this.doRequest(vendor, "/game/terminate", params, "JLS退出用户", sign, true);
        return PlatformResponse.success();
    }

    @Override
    public PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer) {
        return PlatformResponse.success();
    }

    @Override
    public String buildSign(Map<String, Object> dataMap, String key) {
        TreeMap<String, Object> data = new TreeMap<>(JSONUtil.parseObj(dataMap.get("data")));

        String sign = JSONObject.toJSONString(data) + dataMap.get("ts").toString() + key;
        log.info("签名原始内容：{}", sign);
        return DigestUtil.md5Hex(sign);
    }

    @Override
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {

        return new ArrayList<>();
    }

    @Override
    protected SportContext buildContext(PlatformRequest param) {

        String data = JsonUtils.toJsonString(param.getExtraData());
        LotteryReq lotteryReq = JsonUtils.parseObject(data, LotteryReq.class);
        if (lotteryReq == null || lotteryReq.getData() == null) {
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }
        SportContext ctx = new SportContext();
        String traceId = MDC.get("traceId");
        ctx.setTraceId(traceId);
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setSign(param.getExtraData().getOrDefault("sign", "").toString());
        ctx.setTransferId(param.getExtraData().getOrDefault("ts", "").toString());
        ctx.setExternalTransactionId(param.getExtraData().getOrDefault("ts", "").toString());
        ctx.setPullUsername(lotteryReq.getData().getMemberAccount());
        ctx.setOrderId(lotteryReq.getData().getOrderId());
        ctx.setCurrency(lotteryReq.getCurrency());
        ctx.setRoundId("");
        ctx.setGameCode(lotteryReq.getGameCode());
        ctx.setVendorCode(param.getVendorCode());
        return ctx;
    }

    @Override
    protected void downCurrency(SportContext ctx) {
        ctx.setWrapCurrency(ctx.getCurrency());
        cn.iocoder.yudao.framework.common.enums.CurrencyEnum currencyEnum = cn.iocoder.yudao.framework.common.enums.CurrencyEnum.fromCode(ctx.getWrapCurrency());
        ctx.setCurrencyRate(currencyEnum.getUnit().doubleValue());
    }

    @Override
    protected String upCurrency(String currency) {
        return currency;
    }

    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {

        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        parseObject.put("memberBalance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("timestamp", sportContext.getTimestamp());

        return PlatformResponse.success(LotteryResponse.success(parseObject));
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext sportContext) {

        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        parseObject.put("memberBalance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("timestamp", sportContext.getTimestamp());
        return PlatformResponse.success(LotteryResponse.success(parseObject));
    }

    @Override
    protected List<DSBetRequest> buildBet(PlatformRequest param, SportContext context) {
        String data = JsonUtils.toJsonString(param.getExtraData().getOrDefault("data", "{}"));
        List<PushDataDto> orders = JsonUtils.parseArray(data, PushDataDto.class);
        List<DSBetRequest> bets = new ArrayList<>();
        for (PushDataDto order : orders) {
            BigDecimal amount = order.getBetAmount().divide(BigDecimal.valueOf(context.getCurrencyRate()), 2, RoundingMode.HALF_UP);

            //存储下注信息
            DSBetRequest dsBetRequest = new DSBetRequest();
            dsBetRequest.setUsername(context.getPushUsername());
            dsBetRequest.setTransactionId(context.getTransferId());
            dsBetRequest.setExternalTransactionId(order.getOrderId());
            dsBetRequest.setBetId(order.getOrderId());
            dsBetRequest.setAmount(amount);
            dsBetRequest.setGameCode(context.getGameCode());
            dsBetRequest.setRoundId(context.getRoundId());
            bets.add(dsBetRequest);
        }


        return bets;
    }

    /**
     * 下注&结算
     *
     * @param param
     * @param context
     * @return
     */
    @Override
    protected List<DSBetResultRequest> buildBetResult(PlatformRequest param, SportContext context) {
        String data = JsonUtils.toJsonString(param.getExtraData().getOrDefault("data", "{}"));
        List<PushDataDto> orders = JsonUtils.parseArray(data, PushDataDto.class);
        List<DSBetResultRequest> betResults = new ArrayList<>();
        for (PushDataDto order : orders) {
            double betAmount = order.getBetAmount().doubleValue() / context.getCurrencyRate();
            double winAmount = order.getWinAmount().doubleValue() / context.getCurrencyRate();
            double winLoss = (winAmount - betAmount);
            double jackpotAmount = 0d;
            //存储下注信息
            DSBetResultRequest betDSRequest = new DSBetResultRequest();
            betDSRequest.setUsername(context.getPushUsername());
            betDSRequest.setTransactionId(context.getTransferId());
            betDSRequest.setExternalTransactionId(context.getOrderId());
            betDSRequest.setBetId(context.getOrderId());
            betDSRequest.setBetAmount(BigDecimal.valueOf(betAmount));
            betDSRequest.setGameCode(context.getGameCode());
            betDSRequest.setRoundId(context.getRoundId());
            betDSRequest.setWinAmount(BigDecimal.valueOf(winAmount));
            betDSRequest.setActionEnum(ActionEnum.SETTLED);
            betDSRequest.setWinLoss(BigDecimal.valueOf(winLoss));
            betDSRequest.setJackpotAmount(BigDecimal.valueOf(jackpotAmount));
            betResults.add(betDSRequest);

        }


        return betResults;
    }


    @Override
    protected List<DSRollback> buildRefund(PlatformRequest param, SportContext context) {

        GameRecordsDO record = getRecordByRoundId(context.getRoundId());
        if (ObjectUtil.isEmpty(record)) {
            throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
        }
        DSRollback dsBalance = new DSRollback();
        dsBalance.setTraceId(IdUtil.fastUUID());
        dsBalance.setTimestamp(context.getTimestamp());
        dsBalance.setUsername(context.getPushUsername());
        dsBalance.setTransactionId(context.getOrderId());
        dsBalance.setExternalTransactionId(context.getTransferId());
        dsBalance.setBetId(context.getOrderId());
        dsBalance.setGameCode(context.getGameCode());
        dsBalance.setRoundId(context.getRoundId());
        dsBalance.setCreditAmount(record.getWinAmount().subtract(record.getBetAmount()).negate());
        return List.of(dsBalance);
    }

    @Override
    protected List<DSSettle> buildSettle(PlatformRequest param, SportContext context) {
        return new ArrayList<>();
    }


    @Override
    protected DSAdjustment buildAdjustment(PlatformRequest param, SportContext context) {
        DSAdjustment dsBalance = new DSAdjustment();
        dsBalance.setTraceId(IdUtil.fastUUID());
        dsBalance.setTimestamp(context.getTimestamp());
        dsBalance.setUsername(context.getPushUsername());
        dsBalance.setTransactionId(context.getTransferId());
        dsBalance.setExternalTransactionId(context.getOrderId());
        dsBalance.setAmount(BigDecimal.ZERO);
        dsBalance.setRoundId(context.getRoundId());
        dsBalance.setGameCode(context.getGameCode());


        return dsBalance;
    }

    @Override
    protected DSDebit buildDebit(PlatformRequest param, SportContext context) {


        DSDebit betDSRequest = new DSDebit();
        betDSRequest.setUsername(context.getPushUsername());
        betDSRequest.setTransactionId(context.getTransferId());
        betDSRequest.setGameCode(context.getGameCode());
        betDSRequest.setRoundId(context.getRoundId());
        betDSRequest.setTakeAll(0);
        betDSRequest.setCurrency(context.getCurrency());
        betDSRequest.setTimestamp(context.getTimestamp());
        betDSRequest.setAmount(BigDecimal.ZERO);
        return betDSRequest;
    }

    @Override
    protected DSCredit buildCredit(PlatformRequest param, SportContext context) {


        DSCredit item = new DSCredit();
        item.setUsername(context.getPushUsername());
        item.setTransactionId(context.getTransferId());
        item.setExternalTransactionId(context.getOrderId());
        item.setBetId(context.getOrderId());
        item.setGameCode(context.getGameCode());
        item.setRoundId(context.getRoundId());
        item.setCurrency(context.getCurrency());
        item.setTimestamp(context.getTimestamp());

        return item;
    }

    @SneakyThrows
    protected JSONObject doRequest(GameVendorDO vendor, String url, Map<String, Object> paramMap, String remark, String sign, Boolean async) {


        String path = vendor.getApiUrl() + url;
        log.info("{}，请求参数 paramMap={},header:{}", path, paramMap, null);
        var result = HttpUtils.doJsonRequest(path, paramMap, null, async).get(10, TimeUnit.SECONDS);

        JSONObject parseObject = JSONObject.parseObject(result);
        if (ObjectUtil.isEmpty(parseObject)) {
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }

        return parseObject;
    }

    private BigDecimal resolveBalance(String username, String currency) {
        VendorUserBalanceDO balance = balanceService.getBalance(username, currency);
        if (balance == null) {
            log.info("Lottery ：注册或进入游戏查询余额为空：用户名：{}，货币：{}",username,currency);
            return BigDecimal.ZERO;
        }
        return balance.getBalance();
    }


    private Boolean verifySign(Map<String, Object> dataMap, GameVendorDO vendor, String reqSign) {
        TreeMap<String, Object> data = new TreeMap<>(JSONUtil.parseObj(dataMap.get("data")));
        String sign = JSONObject.toJSONString(data) + dataMap.get("ts").toString() + vendor.getVendorKey();
        log.info("IGAMING原始签名:{}", sign);
        String signData = DigestUtil.md5Hex(sign);
        log.info("IGAMING加密签名:{}", signData);
        return signData.equals(reqSign);
    }

}
