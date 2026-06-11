package cn.iocoder.yudao.module.game.adapter.game.tj;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.module.game.adapter.game.one.model.JLSResponse;
import cn.iocoder.yudao.module.game.adapter.game.tj.model.TJResponse;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.enums.CurrencyEnum;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.*;
import cn.iocoder.yudao.module.system.model.api.push.*;
import cn.iocoder.yudao.module.system.model.api.push.sports.DSBetRequest;
import cn.iocoder.yudao.module.system.model.api.push.sports.DSSettle;
import cn.iocoder.yudao.module.system.mq.model.GameStaticDTO;
import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.util.MD5Util;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TJAdapter extends BaseAdapter {
    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {
        JSONObject result = new JSONObject();
        result.put("op_id", sportContext.getPullUsername());
        result.put("availableAmount", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        return PlatformResponse.success(TJResponse.success(result));
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext sportContext) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        parseObject.put("balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("timestamp", sportContext.getTimestamp());
        return PlatformResponse.success(JLSResponse.success(parseObject));
    }

    @Override
    protected DSBetRequest buildBet(PlatformRequest param, SportContext context) {

        JsonNode order = JsonUtils.parseTree(param.getExtraData().get("order").toString());

        BigDecimal amount = new BigDecimal(order.path("amount").asText("0"))
                .divide(BigDecimal.valueOf(context.getCurrencyRate())).abs();
        //存储下注信息
        DSBetRequest DSBetRequest = new DSBetRequest();
        DSBetRequest.setUsername(context.getPushUsername());
        DSBetRequest.setTransactionId(context.getTransferId());
        DSBetRequest.setExternalTransactionId(context.getOrderId());
        DSBetRequest.setBetId(context.getOrderId());
        DSBetRequest.setAmount(amount);
        DSBetRequest.setGameCode(context.getGameCode());
        DSBetRequest.setRoundId(context.getRoundId());

        return DSBetRequest;
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
        dsBalance.setCreditAmount(record.getWinAmount().subtract(record.getBetAmount()).negate());
        dsBalance.setGameCode(context.getGameCode());
        dsBalance.setRoundId(context.getRoundId());

        return List.of(dsBalance);
    }

    @Override
    protected DSBetResultRequest buildBetResult(PlatformRequest param, SportContext context) {
        JsonNode order = JsonUtils.parseTree(param.getExtraData().get("order").toString());
        double betAmount = Convert.toDouble(param.getExtraData().get("betAmount").toString());
        double amountChange = Convert.toDouble(order.path("amount").asText("0")) / context.getCurrencyRate();
        double jackpotAmount = 0;
        double winAmount = Math.max(amountChange, 0);


        String resultType = param.getExtraData().getOrDefault("resultType", "").toString();

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
        betDSRequest.setJackpotAmount(BigDecimal.valueOf(jackpotAmount));
        betDSRequest.setResultType(resultType);

        return betDSRequest;
    }

    @Override
    protected List<DSSettle> buildSettle(PlatformRequest param, SportContext context) {
        return List.of();
    }

    @Override
    protected DSAdjustment buildAdjustment(PlatformRequest param, SportContext context) {
        return null;
    }

    @Override
    protected DSDebit buildDebit(PlatformRequest param, SportContext context) {
        return null;
    }

    @Override
    protected DSCredit buildCredit(PlatformRequest param, SportContext context) {
        return null;
    }

    @Override
    protected SportContext buildContext(PlatformRequest param) {
        SportContext ctx = new SportContext();
        JsonNode order = JsonUtils.parseTree(param.getExtraData().getOrDefault("order", "").toString());
        String traceId = MDC.get("traceId");
        ctx.setTraceId(traceId);
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setSign(param.getExtraData().getOrDefault("sign", "").toString());
        ctx.setTransferId(order.path("trans_no").asText(""));
        ctx.setExternalTransactionId(order.path("trans_no").asText(""));
        ctx.setPullUsername(param.getExtraData().getOrDefault("op_id", "").toString());
        ctx.setOrderId(order.path("trans_no").asText(""));
        ctx.setCurrency(CurrencyEnum.UNKNOWN.getCode());
        ctx.setRoundId(order.path("draw_id").asText(""));
        ctx.setGameCode(order.path("game_id").asText(""));
        ctx.setBalance(MoneyUtils.stringToBigDecimal(order.path("amount").asText("")));
        ctx.setBody(param.getExtraData());
        ctx.setVendorCode(param.getVendorCode());
        return ctx;
    }

    @Override
    protected void downCurrency(SportContext ctx) {
        ctx.setWrapCurrency(ctx.getCurrency());
//        CurrencyEnum currencyEnum = CurrencyEnum.fromCode(ctx.getWrapCurrency());
        ctx.setCurrencyRate(1000.0);
    }

    @Override
    protected String upCurrency(String currency) {
        return CurrencyEnum.UNKNOWN.getCode();
    }

    @Override
    public PlatformResponse<Object> doForward(ActionEnum actionEnum, PlatformRequest param, SportContext ctx) {

        if (ObjectUtil.isEmpty(ctx.getPullUsername())) {
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        ctx.setGameVendor(param.getGameVendor());
        VendorUserDO user = getUser(ctx.getPullUsername(),param.getVendorCode());
        if (ObjectUtil.isEmpty(user)) {
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        ctx.setPushUsername(user.getUsername());
        VendorUserBalanceDO balance = balanceService.getBalance(ctx.getPushUsername(), ctx.getWrapCurrency());
        if (ObjectUtil.isEmpty(balance)) {
            throw new GameException(PlatformCodeEnum.WRONG_CURRENCY);
        }
        ctx.setBalance(balance.getBalance());
        ctx.setUser(user);


        TenantUtils.execute(user.getTenantId(), () -> {
            var vendorTenantDO = vendorTenantService.getVendorByCode(param.getVendorCode());
            if (ObjectUtil.isEmpty(vendorTenantDO)) {
                throw new GameException(PlatformCodeEnum.VENDOR_PLATFORM_NOT_SUPPORTED);
            }
            //  🚫 没有货币 金额参数都以厘为单位 比如 1分 = 10厘 ， 1元 = 1000厘
            //  ctx.setVendorTenant(vendorTenantDO);
            //  if(ObjectUtil.isNotEmpty(vendorTenantDO.getCurrency())){
            //      if (!vendorTenantDO.getCurrency().contains(ctx.getCurrency())) {
            //          throw new GameException(PlatformCodeEnum.WRONG_CURRENCY);
            //      }
            //  }

            var tenant = tenantMapper.selectById(user.getTenantId());
            if (ObjectUtil.isEmpty(tenant)) {
                throw new GameException(PlatformCodeEnum.TENANT_ERROR);
            }
            ctx.setTenant(tenant);
        });


        Map<String, Object> dataMap = param.getExtraData();

        String changeType = param.getAction();

        //根据data构建签名,上游验签
        String newSign = this.buildSign(dataMap, ctx.getSign());
        // 验签失败
        if (!newSign.equals(ctx.getSign())) {
            return PlatformResponse.error(TJResponse.error(101, PlatformCodeEnum.INVALID_SIGNATURE.getMessage()));
        }


        if ("/balance/get".equals(changeType)) {
            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException(PlatformCodeEnum.INVALID_GAME);
        }


        GameRecordsDO record = super.getRecord(ctx.getOrderId());

        if ("/order/transfer".equals(changeType)) {
            // 1：下注，2：派彩, 3：下注撤销
            String action = param.getExtraData().get("action").toString();
            switch (action) {
                case "1" -> {
                    DSBetRequest DSBetRequest = buildBet(param, ctx);
                    if (NumberUtil.isLess(ctx.getBalance(), DSBetRequest.getAmount())) {
                        throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
                    }
                    if (ObjectUtil.isNotEmpty(record)) {
                        return buildIdleResult(ctx);
                    }

                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), DSBetRequest.getAmount().negate(), ActionEnum.BET.getPath(),

                            DSBetRequest,
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
                                GameStaticDTO.buildBet(user, ctx.getGameVendor(), gameInfoDO, game.getBetAmount());
                                return game.getId();
                            }, (amt) -> super.operateBalance(amt, DSBetRequest.getUsername(), ctx.getWrapCurrency()));



                    return buildBalanceResult(ctx);

                }
                case "2" -> {
                    //查询订单
                    if (ObjectUtil.isNotEmpty(record) && record.getAction() == ActionEnum.SETTLED) {
                        return buildBalanceResult(ctx);
                    }

                    param.getExtraData().put("betAmount", record != null && record.getBetAmount() != null
                            ? record.getBetAmount() : 0);
                    DSBetResultRequest betResult = buildBetResult(param, ctx);
                    Map<String, Object> extra = param.getExtraData();
//                    String betContent = extra == null ? "{}" : JSONUtil.toJsonStr(extra);
                    BigDecimal changeAmount = betResult.getWinAmount().add(betResult.getJackpotAmount());
                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), changeAmount, ActionEnum.SETTLED.getPath(),
                            betResult, (order) -> {
                                DSBetResultRequest bet = (DSBetResultRequest) order;

                                if (ObjectUtil.isEmpty(record)) {

                                    GameRecordsDO game = new GameRecordsDO();
                                    Long id = IdUtil.getSnowflakeNextId();
                                    game.setId(id);
                                    game.setGameCode(bet.getGameCode());
                                    game.setUsername(bet.getUsername());
                                    game.setRefOrderNo(bet.getTransactionId());
                                    game.setOrderNo(bet.getBetId());
                                    game.setIssueNo(bet.getRoundId());
                                    game.setAction(ActionEnum.SETTLED);
                                    game.setGameKind(gameInfoDO.getCateId());
//                                    game.setBetAmount(bet.getBetAmount());
                                    game.setWinAmount(bet.getWinAmount());
                                    game.setRemark(bet.getActionEnum().getDesc());
                                    game.setOdds(bet.getOdds());
                                    game.setCreateTime(LocalDateTime.now());
                                    game.setTenantId(user.getTenantId());
                                    game.setUpdateTime(LocalDateTime.now());
                                    game.setJackpotAmount(bet.getJackpotAmount());
                                    game.setTenantCode(ctx.getTenant().getCode());
                                    game.setCurrency(ctx.getCurrency());
                                    game.setUserId(user.getId());
                                    game.setVendorCode(ctx.getVendorCode());
                                    //game.setBetContent(betContent);
                                    recordsMapper.insert(game);
                                    GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, bet.getWinAmount(), game.getWinLoss(), game.getWinLoss().compareTo(bet.getBetAmount()) > 0);
                                    return game.getId();
                                } else {


                                    record.setAction(ActionEnum.SETTLED);
                                    record.setWinAmount(bet.getWinAmount());
                                    record.setRemark(bet.getActionEnum().getDesc());
                                    record.setOdds(bet.getOdds());
                                    record.setUpdateTime(LocalDateTime.now());
                                    record.setJackpotAmount(bet.getJackpotAmount());
                                    //  record.setBetContent(betContent);
                                    recordsMapper.updateById(record);
                                    GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinAmount().compareTo(record.getBetAmount()) > 0);
                                    return record.getId();
                                }

                            },
                            (amt) -> super.operateBalance(amt, betResult.getUsername(), ctx.getWrapCurrency()));


                    return buildBalanceResult(ctx);

                }


                case "3" -> {
                    if (ObjectUtil.isNotEmpty(record) && record.getAction() == ActionEnum.SETTLED) {
                        return buildBalanceResult(ctx);
                    }

                    List<DSRollback> dsRollbacks = buildRefund(param, ctx);


                    for (DSRollback item : dsRollbacks) {
                        if (item.getCreditAmount().doubleValue() < 0) {
                            if (NumberUtil.isLess(ctx.getBalance(), item.getCreditAmount().abs())) {
                                throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
                            }
                        }
                        ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.REFUND.getPath(),
                                item,

                                (order) -> {
                                    DSRollback updateBet = (DSRollback) order;
                                    GameRecordsDO roundByRecord = super.getRecordByRoundId(updateBet.getRoundId());
                                    roundByRecord.setRemark(ActionEnum.REFUND.getDesc());
                                    roundByRecord.setAction(ActionEnum.REFUND);

                                    super.updateRecord(roundByRecord);

                                    GameStaticDTO.buildRefund(user, ctx.getGameVendor(), gameInfoDO, roundByRecord.getBetAmount());

                                    return roundByRecord.getId();
                                },
                                (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                    }

                    return buildBalanceResult(ctx);

                }
            }
        }
        return buildBalanceResult(ctx);

    }

    @Override
    public PlatformResponse<Object> doBalance(ActionEnum changeType, PlatformRequest param, SportContext ctx) {


        if (ctx.getTenant().getImplType().equals(0)) {

            DSBalance dsBalance = new DSBalance();

            dsBalance.setUsername(ctx.getPushUsername());

            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), BigDecimal.ZERO, ActionEnum.BALANCE.getPath(),
                    dsBalance,

                    (order) -> null,
                    (amt) -> {
                        TenantUtils.execute(ctx.getTenant().getId(), () -> super.asyncBalance(ctx.getBalance(), ctx.getPushUsername(), ctx.getWrapCurrency()));
                    });
        }
        return buildBalanceResult(ctx);

    }

    @Override
    public PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request) {
        Map<String, Object> paramMap = new HashMap<>();
        JSONObject userInfo = new JSONObject();
        String username = request.getUsername();
        userInfo.put("nickname", username);
        userInfo.put("gender", 0);
        paramMap.put("user_info", userInfo);
        paramMap.put("op_id", username);
        this.doRequest(vendor, "/open2j/c/create", paramMap, "2J创建用户", "", false);
        return PlatformResponse.success(username);
    }

    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request) {
        String gameUrl = "";
        Map<String, Object> params = new HashMap<>();
        params.put("op_id", request.getUsername());
        params.put("game_id", request.getGameCode());
        params.put("backlink", request.getLobbyUrl());
        params.put("lang", request.getLanguage());
        params.put("ret_lobby_btn", true);
        params.put("device_type", request.getPlatform());
        JSONObject dataMap = this.doRequest(vendor, "/open2j/c/launch", params, "2J获取游戏地址", "", false);
        if (ObjectUtils.isEmpty(dataMap)) {
            log.error("2J获取游戏地址失败：返回结果异常:{}", dataMap);
            return null;
        }
        if (StringUtils.isBlank(gameUrl = dataMap.getString("url"))) {
            log.error("2J返回url为空: {}", dataMap);
            return null;
        }
        return PlatformResponse.success(gameUrl);
    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("op_id", account);
        this.doRequest(vendor, "/open2j/c/evict", paramMap, "2J退出用户", "", true);
        return PlatformResponse.success();
    }

    @Override
    public PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer) {
        return null;
    }


    @Override
    public String getVendorCode() {
        return "2J";
    }

    @Override
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {
        return List.of();
    }


    protected JSONObject doRequest(GameVendorDO vendor, String path, Map<String, Object> paramMap, String remark, String sign, Boolean async) {
        JSONObject parseObject = new JSONObject();
        try {
            String buiParams = this.buildReqParams(paramMap, vendor.getVendorAgent(), vendor.getVendorKey());
            var result = HttpUtils.doJsonRequest(vendor.getApiUrl() + path + buiParams, paramMap, null, async).get(10, TimeUnit.SECONDS);
            parseObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("{}，请求厂商:{} 异常:{}", remark, vendor.getVendorName(), e.getMessage());
            return null;
        }
        JSONObject header = parseObject.getJSONObject("header");
        Integer code = header != null ? header.getInteger("code") : null;
        if (code == null || code != 0) {
            log.error("{}，错误代码 code={}", remark, code);
            return null;
        }
        return parseObject;
    }

    @Override
    public String buildSign(Map<String, Object> dataMap, String key) {
        return MD5Util.md5(dataMap.get("reqBody").toString() + dataMap.get("timestamp") + key);
    }

    public String buildReqParams(Map<String, Object> paramMap, String mchId, String key) {
        String body = JsonUtils.toJsonString(paramMap);
        long time = DateUtils.ts();
        Map<String, Object> mp = new HashMap<>(Map.of(
                "reqBody", body,
                "timestamp", time
        ));
        String sign = this.buildSign(mp, key);
        return "?mch=" + mchId + "&ts=" + time + "&sign=" + sign;
    }

}
