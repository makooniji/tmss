package cn.iocoder.yudao.module.game.adapter.game.pg;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.module.game.adapter.game.pg.model.PGResponse;
import cn.iocoder.yudao.module.game.adapter.game.pg.pull.PGResultImpl;
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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class PGAdapter extends BaseAdapter {

    @Resource
    private PGResultImpl pgResult;



    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {

        JSONObject data = new JSONObject();
        data.put("username", sportContext.getPushUsername());
        data.put("balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        data.put("currency", sportContext.getCurrency());
        data.put("traceId", sportContext.getTraceId());

        return PlatformResponse.success(PGResponse.success(data));
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext sportContext) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        parseObject.put("balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("timestamp", sportContext.getTimestamp());
        return PlatformResponse.success(PGResponse.success(parseObject));
    }

    @Override
    protected DSBetRequest buildBet(PlatformRequest param, SportContext context) {
        BigDecimal amount = new BigDecimal(String.valueOf(param.getExtraData()
                .getOrDefault("change_money", "0"))).divide(BigDecimal
                .valueOf(context.getCurrencyRate())).abs();


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
        return List.of();
    }

    @Override
    protected DSBetResultRequest buildBetResult(PlatformRequest param, SportContext context) {
        double betAmount = Convert.toDouble(param.getExtraData().getOrDefault("betAmount", "0")) * context.getCurrencyRate();
        double amountChange = Convert.toDouble(param.getExtraData().getOrDefault("change_money", "0")) * context.getCurrencyRate();
        double jackpotAmount = Convert.toDouble(param.getExtraData().getOrDefault("jackpotAmount", "0")) * context.getCurrencyRate();
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
        String traceId = MDC.get("traceId");
        ctx.setTraceId(traceId);
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setSign(param.getExtraData().getOrDefault("sign", "").toString());
        ctx.setTransferId(param.getExtraData().getOrDefault("transactionId", "").toString());
        ctx.setExternalTransactionId(param.getExtraData().getOrDefault("transactionId", "").toString());
        ctx.setPullUsername(param.getExtraData().getOrDefault("accounts", "").toString());
        ctx.setOrderId(param.getExtraData().getOrDefault("bet_id", "").toString());
        ctx.setCurrency(param.getExtraData().getOrDefault("currency", "").toString());
        ctx.setRoundId(param.getExtraData().getOrDefault("roundId", "").toString());
        ctx.setGameCode(param.getVendorCode() + "_" + param.getExtraData().getOrDefault("game_code", "").toString());
        ctx.setBalance(MoneyUtils.stringToBigDecimal(param.getExtraData().getOrDefault("change_money", "").toString()));
        ctx.setBody(param.getExtraData());
        return ctx;
    }

    @Override
    protected void downCurrency(SportContext ctx) {
        ctx.setWrapCurrency(ctx.getCurrency());
        CurrencyEnum currencyEnum = CurrencyEnum.fromCode(ctx.getWrapCurrency());
        ctx.setCurrencyRate(currencyEnum.getUnit().doubleValue());
    }

    @Override
    protected String upCurrency(String currency) {

        String country = CurrencyEnum.PKR.getCode();
        if (CurrencyEnum.INR.getCode().equals(currency)) {
            country = "INA";
        } else if (CurrencyEnum.PKR.getCode().equals(currency)) {
            country = "PAK";
        } else if (CurrencyEnum.IDR.getCode().equals(currency)) {
            country = "IDN";
        }
        return country;
    }

    @Override
    public PlatformResponse<Object> doForward(ActionEnum actionEnum, PlatformRequest param, SportContext ctx) {

        String username = ctx.getPullUsername();
        if (ObjectUtil.isEmpty(username)) {
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        ctx.setGameVendor(param.getGameVendor());
        VendorUserDO user = getUser(username,param.getVendorCode());
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

        String changeType = param.getAction();

        //根据data构建签名,上游验签
        if (ObjectUtil.isNotEmpty(ctx.getSign())) {
            Boolean result = SignUtils.verifySign(dataMap, ctx.getGameVendor().getVendorKey(), ctx.getSign());
            if (!result) {
                throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
            }

        }


        if ("/get/UserBalance".equals(changeType)) {
            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException(PlatformCodeEnum.INVALID_GAME);
        }

        GameRecordsDO record = super.getRecord(ctx.getOrderId());
        //同一个接口 根据 change_type区分类型 50 下注 51 游戏结算
        if ("/bet/Result".equals(changeType)) {
            String changeTypeCode = param.getExtraData().get("change_type").toString();
            switch (changeTypeCode) {
                case "50" -> {
                    if (ObjectUtil.isNotEmpty(record)) {
                        return buildIdleResult(ctx);
                    }

                    DSBetRequest DSBetRequest = buildBet(param, ctx);
                    if (NumberUtil.isLess(ctx.getBalance(), DSBetRequest.getAmount())) {
                        throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
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
                            },
                            (amt) -> super.operateBalance(amt, DSBetRequest.getUsername(), ctx.getWrapCurrency()));

                    return buildBalanceResult(ctx);
                }

                case "51" -> {
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
                            betResult,

                            (order) -> {
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
                                    game.setBetAmount(bet.getBetAmount());
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
                                    GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, bet.getWinAmount(), game.getWinLoss(), game.getWinAmount().compareTo(bet.getBetAmount()) > 0);
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
        String username = request.getUsername();
        String appId = vendor.getVendorAgent();
        String appSecret = vendor.getVendorKey();
        String vendorUsername = this.nameFormat(username);
        Map<String, Object> params = new HashMap<>();
        params.put("appId", appId);
        params.put("accounts", vendorUsername);
        params.put("com", vendor.getVendorCode());
        params.put("game_code", request.getGameCode());
        params.put("country", request.getCurrency());
        params.put("lobbyUrl", request.getLobbyUrl());
        params.put("language", request.getLanguage());
        String sign = this.buildSign(params, appSecret);
        params.put("sign", sign);
        this.doRequest(vendor, "?action=requestGameURL", params, "PG创建用户", sign, true);
        return PlatformResponse.success(vendorUsername);
    }

    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request) {
        String gameUrl = "";
        String appId = vendor.getVendorAgent();
        String appSecret = vendor.getVendorKey();
        Map<String, Object> params = new HashMap<>();
        params.put("appId", appId);
        params.put("accounts", this.nameFormat(request.getUsername()));
        params.put("com", vendor.getVendorCode());
        params.put("game_code", request.getGameCode());
        params.put("country", request.getCurrency());
        params.put("language", request.getLanguage());
       /* if (vendor.getRtp() != null) {
            params.put("RTP", vendor.getRtp().setScale(2, RoundingMode.DOWN));
        }*/

        String sign = this.buildSign(params, appSecret);
        params.put("sign", sign);
        JSONObject dataMap = this.doRequest(vendor, "?action=requestGameURL", params, "PG获取游戏地址", sign, false);
        if (ObjectUtils.isEmpty(dataMap)) {
            log.error("PG获取游戏地址失败：返回结果为空");
            return null;
        }
        String dataStr = dataMap.getString("data");
        if (StringUtils.isBlank(dataStr)) {
            log.error("PG返回 data 为空: {}", dataMap);
            throw new GameException(dataMap.getString("message"));
        }
        JSONObject data = JSONObject.parseObject(dataStr);
        gameUrl = data.getString("game_url");
        return PlatformResponse.success(gameUrl);

    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {
        String appId = vendor.getVendorAgent();
        String appSecret = vendor.getVendorKey();
        Map<String, Object> params = new HashMap<>();
        params.put("appId", appId);
        params.put("accounts", this.nameFormat(account));
        String sign = this.buildSign(params, appSecret);
        params.put("sign", sign);
        this.doRequest(vendor, "?action=requestGameExit", params, "PG退出用户", sign, true);
        return PlatformResponse.success();
    }

    @Override
    public PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer) {
        return PlatformResponse.success();
    }

    // 该方法可重写，根据对应厂商逻辑加密
    @Override
    public String buildSign(Map<String, Object> params, String secretkey) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }

            Object value = params.get(key);
            if (value == null) {
                continue; // 是否跳过 null 看你业务
            }
            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(key).append("=").append(value);
        }
        String signSource = sb.toString();
        log.info("签名原始数据: {}", signSource);
        return HmacUtils.hmacSha1Hex(secretkey, signSource);
    }

    @Override
    public String getVendorCode() {
        return "PG";
    }


    @Override
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {
        return pgResult.execute(CollectionUtil.join(List.of(startTime, endTime), ","));

    }


    protected JSONObject doRequest(GameVendorDO vendor, String path, Map<String, Object> paramMap, String remark, String sign, Boolean async) {


        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        JSONObject parseObject = new JSONObject();
        try {
            var result = HttpUtils.doJsonRequest(vendor.getApiUrl() + path, paramMap, header, async).get(10, TimeUnit.SECONDS);
            parseObject = JSONObject.parseObject(result);

        } catch (Exception e) {
            log.error("{}，请求厂商:{} 异常:{}", remark, vendor.getVendorName(), e.getMessage());
            throw new GameException(parseObject.getString("msg"));
        }
        String code = parseObject.getString("code");
        if (!"1".equals(code)) {
            log.error("{}，请求厂商:{} 异常:{}", remark, vendor.getVendorName(), parseObject.getString("message"));
            throw new GameException(parseObject.getString("message"));
        }
        return parseObject;
    }

    @Override
    public String nameFormat(String username) {
        if (username == null || username.isEmpty()) {
            return username;
        }
        int index = username.lastIndexOf("_");
        return index != -1 ? username.substring(index + 1) : username;
    }
}
