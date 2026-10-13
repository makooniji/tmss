package cn.iocoder.yudao.module.game.adapter.game.dg;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;

import cn.iocoder.yudao.framework.common.model.game.GameApiRequest;
import cn.iocoder.yudao.framework.common.model.game.GameApiRtp;
import cn.iocoder.yudao.framework.common.model.game.GameApiTransfer;
import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSSettle;

import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.module.system.enums.LanguageEnum;
import cn.iocoder.yudao.module.system.enums.ResultTypeEnum;
import cn.iocoder.yudao.module.system.model.*;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.module.game.adapter.game.dg.model.PGResponse;
import cn.iocoder.yudao.module.game.adapter.game.dg.pull.PGResultImpl;

import cn.iocoder.yudao.module.system.model.game.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.model.game.SportContext;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import cn.iocoder.yudao.framework.common.enums.CurrencyEnum;


@Service
@Slf4j
public class DGAdapter extends BaseAdapter {

    @Resource
    private PGResultImpl pgResult;


    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {

        JSONObject data = new JSONObject();

        data.put("Balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));


        return PlatformResponse.success(PGResponse.success(data));
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext sportContext) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("Balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));

        return PlatformResponse.success(PGResponse.success(parseObject));
    }

    @Override
    protected List<DSBetRequest> buildBet(PlatformRequest param, SportContext context) {


        JSONObject data = JsonUtils.convertObject(param.getExtraData(), JSONObject.class);


        BigDecimal amount = data.getBigDecimal("Amount").divide(BigDecimal.valueOf(context.getCurrencyRate()), 2, RoundingMode.HALF_UP);

        DSBetRequest DSBetRequest = new DSBetRequest();
        DSBetRequest.setUsername(context.getPushUsername());
        DSBetRequest.setTransactionId(context.getTransferId());
        DSBetRequest.setExternalTransactionId(context.getOrderId());
        DSBetRequest.setBetId(context.getOrderId());
        DSBetRequest.setAmount(amount.abs());
        DSBetRequest.setGameCode(context.getGameCode());
        DSBetRequest.setRoundId(context.getRoundId());
        DSBetRequest.setOdds(data.getBigDecimal("Bm"));

        return List.of(DSBetRequest);
    }

    @Override
    protected List<DSRollback> buildRefund(PlatformRequest param, SportContext context) {
        return List.of();
    }

    @Override
    protected List<DSBetResultRequest> buildBetResult(PlatformRequest param, SportContext context) {
        JSONObject data = JsonUtils.convertObject(param.getExtraData(), JSONObject.class);


        BigDecimal changeAmount = data.getBigDecimal("Amount").divide(BigDecimal.valueOf(context.getCurrencyRate()), 2, RoundingMode.HALF_UP);


        DSBetResultRequest betDSRequest = new DSBetResultRequest();
        betDSRequest.setUsername(context.getPushUsername());
        betDSRequest.setTransactionId(context.getTransferId());
        betDSRequest.setExternalTransactionId(context.getOrderId());
        betDSRequest.setBetId(context.getOrderId());

        betDSRequest.setGameCode(context.getGameCode());
        betDSRequest.setRoundId(context.getRoundId());
        betDSRequest.setWinAmount(changeAmount.max(BigDecimal.ZERO));
        betDSRequest.setActionEnum(ActionEnum.SETTLED);
        betDSRequest.setJackpotAmount(BigDecimal.ZERO);
        if (changeAmount.compareTo(BigDecimal.ZERO) > 0) {
            betDSRequest.setResultType(ResultTypeEnum.WIN);
        }
        return List.of(betDSRequest);
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
        ctx.setSign("");
        ctx.setTransferId(param.getExtraData().getOrDefault("TransactionID", "").toString());
        ctx.setExternalTransactionId(param.getExtraData().getOrDefault("TransactionID", "").toString());
        ctx.setPullUsername(param.getExtraData().getOrDefault("UserID", "").toString());
        ctx.setOrderId(param.getExtraData().getOrDefault("BetID", "").toString());
        ctx.setCurrency(param.getExtraData().getOrDefault("currency", "BRL").toString());
        ctx.setRoundId(param.getExtraData().getOrDefault("RoundID", "").toString());
        ctx.setGameCode(param.getExtraData().getOrDefault("GameID", "").toString());
        ctx.setBalance(MoneyUtils.stringToBigDecimal(param.getExtraData().getOrDefault("Amount", "").toString()));
        ctx.setBody(param.getExtraData());
        ctx.setVendorCode(param.getVendorCode());
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
        VendorUserDO user = getUser(username, param.getVendorCode());
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

        String changeType = param.getAction();

        // 与出站 buildSign 同一套：排序 k=v + HMAC-SHA1（勿用 SignUtils 的 JSON+SHA256）
//        String expectSign = buildSign(dataMap, ctx.getGameVendor().getVendorKey());
//        if (!StringUtils.equals(expectSign, ctx.getSign())) {
//            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
//        }


        if ("/Cash/Get".equals(changeType)) {
            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException(PlatformCodeEnum.INVALID_GAME);
        }

        GameRecordsDO record = super.getRecord(ctx.getOrderId());

        if ("/Cash/TransferInOut".equals(changeType)) {
            String changeTypeCode = param.getExtraData().get("Reason").toString();
            switch (changeTypeCode) {
                case "bet" -> {
                    if (ObjectUtil.isNotEmpty(record)) {
                        return buildIdleResult(ctx);
                    }

                    DSBetRequest DSBetRequest = buildBet(param, ctx).get(0);

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
                                game.setBetAmount(bet.getAmount().abs());
                                game.setWinAmount(BigDecimal.ZERO);
                                game.setRemark(ActionEnum.BET.getDesc());
                                game.setOdds(BigDecimal.ZERO);
                                game.setCreateTime(LocalDateTime.now());
                                game.setTenantId(ctx.getTenant().getId());
                                game.setTenantCode(ctx.getTenant().getCode());
                                game.setCurrency(ctx.getCurrency());
                                game.setUserId(user.getId());
                                game.setVendorCode(ctx.getVendorCode());
                                game.setTraceId(ctx.getTransferId());
                                recordsMapper.insert(game);
                                GameStaticDTO.buildBet(user, ctx.getGameVendor(), gameInfoDO, game.getBetAmount(), ctx.getCurrency());
                                return game.getId();
                            },
                            (amt) -> super.operateBalance(amt, DSBetRequest.getUsername(), ctx.getWrapCurrency()));

                    return buildBalanceResult(ctx);
                }

                case "win" -> {

                    DSBetResultRequest betResult = buildBetResult(param, ctx).get(0);
                    if (ObjectUtil.isEmpty(record)) {
                        throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                    }

                    betResult.setBetAmount(record.getBetAmount());
                    betResult.setWinLoss(betResult.getWinAmount().subtract(betResult.getBetAmount()));

                    BigDecimal changeAmount = betResult.getWinAmount().add(betResult.getJackpotAmount());
                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), changeAmount, ActionEnum.SETTLED.getPath(),
                            betResult,

                            (order) -> {
                                DSBetResultRequest payout = (DSBetResultRequest) order;
                                record.setAction(ActionEnum.SETTLED);
                                record.setWinAmount(payout.getWinAmount());
                                record.setRemark(payout.getActionEnum().getDesc());
                                record.setOdds(payout.getOdds());
                                record.setUpdateTime(LocalDateTime.now());
                                record.setJackpotAmount(payout.getJackpotAmount());
                                recordsMapper.updateById(record);
                                GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinAmount().compareTo(record.getBetAmount()) > 0, ctx.getCurrency());

                                return record.getId();


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
                        TenantUtils.execute(ctx.getTenant().getId(), () -> super.asyncBalance(ctx.getBalance(), ctx.getUser().getUsername(), ctx.getWrapCurrency()));
                    });
        }
        return buildBalanceResult(ctx);

    }

    @Override
    public PlatformResponse<Boolean> doSetRtp(GameVendorDO gameVendor, GameApiRtp body) {

        // JSONObject dataMap = this.doRequest(gameVendor, "/api/v1/game/launch", params, "PG获取游戏地址", sign, false);
        return PlatformResponse.success(true);
    }

    @Override
    public PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request) {
        String username = vendor.getVendorChannel() + "_" + request.getUsername();
        return PlatformResponse.success(username);
    }

    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request, VendorUserDO gameUser) {
        String gameUrl = "";

        String appSecret = vendor.getVendorKey();
        Map<String, Object> params = new HashMap<>();
        params.put("UserID", gameUser.getVendorUsername());
        params.put("GameID", request.getGameCode());
        params.put("Language", LanguageEnum.findByCode(request.getLanguage()).getShortCode());
        if (request.getRtp() != null) {
            params.put("RTP", request.getRtp().setScale(2, RoundingMode.DOWN));
        }

        String sign = this.buildSign(params, appSecret);
        //params.put("sign", sign);
        JSONObject dataMap = this.doRequest(vendor, "/api/v1/game/launch", params, "PG获取游戏地址", sign, false);
        if (ObjectUtils.isEmpty(dataMap)) {
            log.error("PG获取游戏地址失败：返回结果为空");
            return null;
        }
        String dataStr = dataMap.getString("data");
        if (StringUtils.isBlank(dataStr)) {
            log.error("PG返回 data 为空: {}", dataMap);
            throw new GameException(dataMap.getString("error"));
        }
        JSONObject data = JSONObject.parseObject(dataStr);
        gameUrl = data.getString("Url");
        return PlatformResponse.success(gameUrl);

    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {
        String appId = vendor.getVendorAgent();
        String appSecret = vendor.getVendorKey();

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
        header.put("AppID", vendor.getVendorAgent());
        header.put("AppSecret", vendor.getVendorKey());
        JSONObject parseObject = new JSONObject();
        try {
            var result = HttpUtils.doJsonRequest(vendor.getApiUrl() + path, paramMap, header, async).get(10, TimeUnit.SECONDS);
            parseObject = JSONObject.parseObject(result);

        } catch (Exception e) {
            log.error("{}，请求厂商:{} 异常:{}", remark, vendor.getVendorName(), e.getMessage());
            throw new GameException(parseObject.getString("msg"));
        }
        String code = parseObject.getString("error");
        if (ObjectUtil.isNotEmpty(code)) {
            log.error("{}，请求厂商:{} 异常:{}", remark, vendor.getVendorName(), code);
            throw new GameException(code);
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
