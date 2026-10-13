package cn.iocoder.yudao.module.game.adapter.game.tj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.iocoder.yudao.framework.common.model.game.GameApiRequest;
import cn.iocoder.yudao.framework.common.model.game.GameApiRtp;
import cn.iocoder.yudao.framework.common.model.game.GameApiTransfer;
import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSSettle;
import cn.iocoder.yudao.framework.common.util.GameInfoSqlUtil;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.module.system.enums.ResultTypeEnum;
import cn.iocoder.yudao.module.system.model.*;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.module.game.adapter.game.tj.model.TJResponse;
import cn.iocoder.yudao.framework.common.enums.CurrencyEnum;
import cn.iocoder.yudao.module.system.enums.GameKindEnum;
import cn.iocoder.yudao.module.system.model.game.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.model.game.SportContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class TJAdapter extends BaseAdapter {
    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {
        JSONObject result = new JSONObject();
        result.put("op_id", sportContext.getPullUsername());
        int li = sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate()*1000)).intValue();
        result.put("availableAmount",li);
        return PlatformResponse.success(TJResponse.success(result));
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext sportContext) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        int li = sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate()*1000)).intValue();
        parseObject.put("balance", li);
        parseObject.put("timestamp", sportContext.getTimestamp());
        return PlatformResponse.success(TJResponse.success(parseObject));
    }

    @Override
    protected List<DSBetRequest> buildBet(PlatformRequest param, SportContext context) {

        List<DSBetRequest> result = new ArrayList<>();
        if(param.getAction().equals("/balance/batch_transfer")){

            JSONArray orders = JsonUtils.convertObject(param.getExtraData().get("orders"), JSONArray.class);
            String gameId = param.getExtraData().get("game_id").toString();
            String originalDrawId = param.getExtraData().get("draw_id").toString();
            String action = param.getExtraData().get("action").toString();
            for (Object obj : orders) {
                JSONObject order = (JSONObject) obj;

                String transNo = order.getString("trans_no");
                order.put("draw_id", originalDrawId);

                String drawId = resolveDrawId(originalDrawId, gameId, action, order.get("extra_business"));


                drawId = buildDrawId(gameId, context.getPullUsername(), drawId);

                //变动金额是0
                java.math.BigDecimal changeAmount = order.getBigDecimal("amount").abs().divide(BigDecimal.valueOf(context.getCurrencyRate()*1000),2, RoundingMode.HALF_DOWN);

                DSBetRequest dsbetRequest = new DSBetRequest();
                dsbetRequest.setAmount(changeAmount);
                dsbetRequest.setBetId(drawId);
                dsbetRequest.setTimestamp(context.getTimestamp());
                dsbetRequest.setGameCode(gameId);
                dsbetRequest.setUsername(context.getPushUsername());
                dsbetRequest.setTransactionId(transNo);
                dsbetRequest.setRoundId(context.getRoundId());
                result.add(dsbetRequest);
            }
            return result;
        }


        JSONObject order = JsonUtils.convertObject(param.getExtraData().get("order"),JSONObject.class);

        BigDecimal amount = order.getBigDecimal("amount").abs()
                .divide(BigDecimal.valueOf(context.getCurrencyRate()*1000),2, RoundingMode.HALF_DOWN);
        //存储下注信息
        DSBetRequest DSBetRequest = new DSBetRequest();
        DSBetRequest.setUsername(context.getPushUsername());
        DSBetRequest.setTransactionId(context.getTransferId());
        DSBetRequest.setExternalTransactionId(context.getOrderId());
        DSBetRequest.setBetId(context.getOrderId());
        DSBetRequest.setAmount(amount);
        DSBetRequest.setGameCode(context.getGameCode());
        DSBetRequest.setRoundId(context.getRoundId());

        return List.of(DSBetRequest);
    }

    @Override
    protected List<DSRollback> buildRefund(PlatformRequest param, SportContext context) {
        List<DSRollback> result = new ArrayList<>();
        if(param.getAction().equals("/balance/batch_transfer")) {

            JSONArray orders = JsonUtils.convertObject(param.getExtraData().get("orders"), JSONArray.class);
            String gameId = param.getExtraData().get("game_id").toString();
            String originalDrawId = param.getExtraData().get("draw_id").toString();
            String action = param.getExtraData().get("action").toString();
            for (Object obj : orders) {
                JSONObject order = (JSONObject) obj;

                String transNo = order.getString("trans_no");
                order.put("draw_id", originalDrawId);
                String drawId = resolveDrawId(originalDrawId, gameId, action, order.get("extra_business"));


                drawId = buildDrawId(gameId, context.getPullUsername(), drawId);

                //变动金额是0
                BigDecimal changeAmount = order.getBigDecimal("amount").divide(BigDecimal.valueOf(context.getCurrencyRate()*1000),2, RoundingMode.HALF_DOWN);

                DSRollback betResultRequest = new DSRollback();
                betResultRequest.setCreditAmount(changeAmount.abs());
                betResultRequest.setBetId(drawId);
                betResultRequest.setTimestamp(context.getTimestamp());
                betResultRequest.setGameCode(gameId);
                betResultRequest.setUsername(context.getPushUsername());
                betResultRequest.setTransactionId(transNo);
                betResultRequest.setRoundId(context.getRoundId());

                result.add(betResultRequest);
            }
            return  result;
        }

        GameRecordsDO record = getRecord(context.getOrderId());
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
    protected List<DSBetResultRequest> buildBetResult(PlatformRequest param, SportContext context) {

        List<DSBetResultRequest> result = new ArrayList<>();
        if(param.getAction().equals("/balance/batch_transfer")){

            JSONArray orders = JsonUtils.convertObject(param.getExtraData().get("orders"), JSONArray.class);
            String gameId = param.getExtraData().get("game_id").toString();
            String originalDrawId = param.getExtraData().get("draw_id").toString();
            String action = param.getExtraData().get("action").toString();
            for (Object obj : orders) {
                JSONObject order = (JSONObject) obj;

                String transNo = order.getString("trans_no");
                order.put("draw_id", originalDrawId);
                JSONObject extraBusiness = order.getJSONObject("extra_business");

                String drawId = resolveDrawId(originalDrawId, gameId, action, order.get("extra_business"));


                drawId = buildDrawId(gameId, context.getPullUsername(), drawId);

                //变动金额是0
                BigDecimal changeAmount = order.getBigDecimal("amount").divide(BigDecimal.valueOf(context.getCurrencyRate()*1000),2, RoundingMode.HALF_DOWN);

                DSBetResultRequest betResultRequest = new DSBetResultRequest();
                betResultRequest.setCreditAmount(changeAmount);
                betResultRequest.setBetId(drawId);
                betResultRequest.setTimestamp(context.getTimestamp());
                betResultRequest.setGameCode(gameId);
                betResultRequest.setUsername(context.getPushUsername());
                betResultRequest.setTransactionId(transNo);
                betResultRequest.setRoundId(context.getRoundId());
                betResultRequest.setBetAmount(extraBusiness.getBigDecimal("total_bet"));
                betResultRequest.setJackpotAmount(extraBusiness.getBigDecimal("total_award"));
                betResultRequest.setWinLoss(changeAmount);
                result.add(betResultRequest);
            }
            return result;
        }


        JSONObject order = JsonUtils.convertObject(param.getExtraData().get("order"),JSONObject.class);

        GameRecordsDO record = getRecord(context.getOrderId());
        if (ObjectUtils.isEmpty(record)) {
            throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
        }

        double amountChange = order.getDoubleValue("amount") / context.getCurrencyRate()/1000;
        double jackpotAmount = 0;
        double winAmount = Math.max(amountChange, 0);


        ResultTypeEnum resultType =ResultTypeEnum.LOSE;
        if(amountChange>0){
            resultType=ResultTypeEnum.WIN;
        }

        //存储下注信息
        DSBetResultRequest betDSRequest = new DSBetResultRequest();
        betDSRequest.setUsername(context.getPushUsername());
        betDSRequest.setTransactionId(context.getTransferId());
        betDSRequest.setExternalTransactionId(context.getOrderId());
        betDSRequest.setBetId(context.getOrderId());
        betDSRequest.setBetAmount(record.getBetAmount());
        betDSRequest.setGameCode(context.getGameCode());
        betDSRequest.setRoundId(context.getRoundId());
        betDSRequest.setWinAmount(BigDecimal.valueOf(winAmount));
        betDSRequest.setActionEnum(ActionEnum.SETTLED);
        betDSRequest.setJackpotAmount(BigDecimal.valueOf(jackpotAmount));
        betDSRequest.setWinLoss(BigDecimal.valueOf(amountChange));
        betDSRequest.setResultType(resultType);

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
        String paramOrder = param.getExtraData().getOrDefault("order", "").toString();
        if(ObjectUtil.isNotEmpty(paramOrder)) {
            JSONObject order = JsonUtils.convertObject(param.getExtraData().get("order"),JSONObject.class);
            ctx.setTransferId(order.getString("trans_no"));
            ctx.setExternalTransactionId(order.getString("trans_no"));
            ctx.setOrderId(order.getString("draw_id"));
            ctx.setRoundId(order.getString("draw_id"));
            ctx.setGameCode(order.getString("game_id"));
        }


        String traceId = MDC.get("traceId");
        ctx.setTraceId(traceId);
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setSign(param.getParamMap().getOrDefault("sign", ""));

        ctx.setPullUsername(param.getExtraData().getOrDefault("op_id", "").toString());

        ctx.setCurrency( param.getGameVendor().getCurrency().get(0));

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
        return CurrencyEnum.UNKNOWN.getCode();
    }

    private JSONObject parseObjectValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        try {
            return JSON.parseObject(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
    private String resolveDrawId(String drawId, String gameId, String action, Object extraBusinessValue) {
        if (!"2".equals(action) || gameId == null || !gameId.startsWith("3")) {
            return drawId;
        }
        JSONObject extraBusiness = parseObjectValue(extraBusinessValue);
        if (extraBusiness == null) {
            return drawId;
        }
        String refDrawId = extraBusiness.getString("ref_draw_id");
        return StringUtils.isNotBlank(refDrawId) ? refDrawId : drawId;
    }
    private String buildDrawId(String gameId, String userId, String drawId) {
        if (StringUtils.isBlank(drawId)) {
            return drawId;
        }
        if (gameId != null && gameId.startsWith("2")) {
            int index = drawId.lastIndexOf("-");
            if (index != -1) {
                drawId = drawId.substring(index + 1);
            }
            return gameId + "-" + userId + "-" + drawId;
        }
        return drawId;
    }

    @Override
    public PlatformResponse<Object> doForward(ActionEnum actionEnum, PlatformRequest param, SportContext ctx) {

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
            var vendorTenantDO = vendorTenantService.getVendorByCode(param.getVendorCode(),user.getTenantId() );
            if (ObjectUtil.isEmpty(vendorTenantDO)) {
                throw new GameException(PlatformCodeEnum.VENDOR_PLATFORM_NOT_SUPPORTED);
            }
            var tenant = tenantMapper.selectById(user.getTenantId());
            if (ObjectUtil.isEmpty(tenant)) {
                throw new GameException(PlatformCodeEnum.TENANT_ERROR);
            }
            ctx.setTenant(tenant);
        });


        Map<String, Object> dataMap = param.getExtraData();

        String changeType = param.getAction();

        Map<String, Object> signMap = new HashMap<>();
        signMap.put("jsonBody",param.getBody());
        signMap.put("ts", param.getParamMap().getOrDefault("ts",""));

        //根据data构建签名,上游验签
        String newSign = this.buildSign(signMap, ctx.getGameVendor().getVendorKey());
        // 验签失败
        if (!newSign.equals(ctx.getSign())) {
            return PlatformResponse.error(TJResponse.error(101, PlatformCodeEnum.INVALID_SIGNATURE.getMessage()));
        }
        if("/member/profile".equals(changeType)) {
            Map<Object, Object> data = MapUtil.builder().put("nickname", ctx.getPullUsername()).put("gender", 0).build();
            return PlatformResponse.success(TJResponse.success(data));
        }

        if ("/balance/get".equals(changeType)) {
            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException(PlatformCodeEnum.INVALID_GAME);
        }

        // 1：下注，2：派彩, 3：下注撤销
        String action = dataMap.get("action").toString();
        switch (action) {
            case "1":
                List<DSBetRequest> dsBetRequests = buildBet(param, ctx);

                for (DSBetRequest item : dsBetRequests) {
                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getAmount(), ActionEnum.BET.getPath(),
                            item,
                            (order) -> {
                                DSBetRequest bet = (DSBetRequest) order;

                                GameStaticDTO.buildBet(user, ctx.getGameVendor(), gameInfoDO, bet.getAmount(),ctx.getCurrency());
                                return saveRecord(ctx, gameInfoDO.getCateId(), bet.getTransactionId(), bet.getBetId(), bet.getRoundId(), ActionEnum.BET, bet.getAmount().abs().doubleValue(), ctx.getUser().getUsername(),
                                        bet.getBetContent(), item.getOdds(), bet.getGameCode(), ctx.getUser().getTenantId());
                            },
                            (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                }
                break;
            case "2":
                List<DSBetResultRequest> dsSettles = buildBetResult(param, ctx);
                for (DSBetResultRequest item : dsSettles) {
                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.SETTLED.getPath(),
                            item, (order) -> {
                                DSBetResultRequest bet = (DSBetResultRequest) order;
                                GameRecordsDO record = super.getRecord(bet.getBetId());
                                if (ObjectUtil.isEmpty(record)) {
                                    throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                                }
                                record.setAction(ActionEnum.SETTLED);
                                record.setWinAmount(record.getBetAmount().add(bet.getJackpotAmount()).add(bet.getWinLoss()));
                                record.setRemark(bet.getActionEnum().getDesc());
                                record.setOdds(bet.getOdds());
                                record.setUpdateTime(LocalDateTime.now());
                                record.setJackpotAmount(bet.getJackpotAmount());
                                recordsMapper.updateById(record);
                                GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinAmount().compareTo(record.getBetAmount()) > 0,ctx.getCurrency());
                                return record.getId();

                            },
                            (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));

                }
                break;
            case "3":
                GameRecordsDO record = super.getRecord(ctx.getOrderId());
                if(ObjectUtil.isEmpty(record)) {
                    return buildBalanceResult(ctx);
                }
                if ( record.getAction() == ActionEnum.SETTLED) {
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
                                GameRecordsDO roundByRecord = super.getRecord(updateBet.getBetId());

                                roundByRecord.setRemark(ActionEnum.REFUND.getDesc());
                                roundByRecord.setAction(ActionEnum.REFUND);

                                super.updateRecord(roundByRecord);

                                GameStaticDTO.buildRefund(user, ctx.getGameVendor(), gameInfoDO, roundByRecord.getBetAmount(),ctx.getCurrency());

                                return roundByRecord.getId();
                            },
                            (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                }
                break;

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
        return PlatformResponse.success(true);
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
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request, VendorUserDO gameUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("op_id", request.getUsername());
        params.put("game_id", request.getGameCode());
        params.put("backlink", request.getLobbyUrl());
        params.put("lang", request.getLanguage());
        params.put("ret_lobby_btn", true);
        params.put("device_type", request.getPlatform());
        JSONObject dataMap = this.doRequest(vendor, "/open2j/c/launch", params, "2J获取游戏地址", "", false);
        String gameUrl = dataMap.getString("url");
        if (StringUtils.isBlank(gameUrl)) {
            log.error("2J返回url为空: {}", dataMap);
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
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
        return "TJ";
    }

    @Override
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {
        return List.of();
    }


    protected JSONObject doRequest(GameVendorDO vendor, String path, Map<String, Object> paramMap, String remark, String sign, Boolean async) {
        JSONObject parseObject;
        try {
            String buiParams = this.buildReqParams(paramMap, vendor.getVendorAgent(), vendor.getVendorKey());
            var result = HttpUtils.doJsonRequest(vendor.getApiUrl() + path + buiParams, paramMap, null, async).get(10, TimeUnit.SECONDS);
            parseObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("{}，请求厂商:{} 异常", remark, vendor.getVendorName(), e);
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }

        if (ObjectUtil.isEmpty(parseObject)) {
            log.error("{}，请求厂商:{} 返回为空", remark, vendor.getVendorName());
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }
        JSONObject header = parseObject.getJSONObject("header");
        Integer code = header == null ? null : header.getInteger("code");
        String msg = header == null ? null : header.getString("msg");
        if (!Integer.valueOf(0).equals(code)) {
            log.error("{}，请求厂商:{} 上游返回失败 code={}, msg={}", remark, vendor.getVendorName(), code, msg);
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR.getCode(), StringUtils.defaultIfBlank(msg, "2J请求失败"));
        }
        return parseObject;
    }

    @Override
    public String buildSign(Map<String, Object> dataMap, String key) {
        return SecureUtil.md5(dataMap.get("jsonBody").toString() + dataMap.get("ts") + key);
    }

    public String buildReqParams(Map<String, Object> paramMap, String mchId, String key) {
        String body = JsonUtils.toJsonString(paramMap);
        long time = DateUtils.ts();
        Map<String, Object> mp = new HashMap<>(Map.of(
                "jsonBody", body,
                "ts", time
        ));
        String sign = this.buildSign(mp, key);
        return "?mch=" + mchId + "&ts=" + time + "&sign=" + sign;
    }

    // ---------- 临时工具：读 Google Sheets 生成 game_info SQL ----------
    private static final String TRANSLATE_LOCALES = "zh-CN,en-US,pt-BR,ms-MY";

    /** 要拉取的页：名称 + 分类 + xlsx 导出链接（加类型往列表追加即可） */
    private static final List<SheetSource> SHEET_SOURCES = List.of(
            new SheetSource("Slots", GameKindEnum.ELECTRONIC,
                    "https://docs.google.com/spreadsheets/d/1jU7XNjp02nmp0A29tnCUbw01gHsN9AIM/export?format=xlsx&gid=1677103144")
            // new SheetSource("Fish", GameKindEnum.FISHING, "https://...export?format=xlsx&gid=xxxx"),
    );

    // xlsx 关系：rId -> 超链接 Target
    private static final Pattern REL_PATTERN = Pattern.compile(
            "Id=\"(rId\\d+)\"[^>]*Target=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    // V 列 Icon 单元格 -> rId
    private static final Pattern ICON_LINK_PATTERN = Pattern.compile(
            "ref=\"V(\\d+)\"[^>]*r:id=\"(rId\\d+)\"|r:id=\"(rId\\d+)\"[^>]*ref=\"V(\\d+)\"",
            Pattern.CASE_INSENSITIVE);
    // Drive URL 取文件 id
    private static final Pattern DRIVE_ID_PATTERN = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)");
    // 每一行
    private static final Pattern ROW_PATTERN = Pattern.compile(
            "<row[^>]*r=\"(\\d+)\"[^>]*>(.*?)</row>", Pattern.DOTALL);
    // 单元格：列 + 属性 + 值
    private static final Pattern CELL_PATTERN = Pattern.compile(
            "<c r=\"([A-Z]+)(\\d+)\"([^>]*)>(?:.*?<v>(.*?)</v>)?", Pattern.DOTALL);
    // sharedStrings 文本
    private static final Pattern SHARED_T_PATTERN = Pattern.compile("<t[^>]*>(.*?)</t>", Pattern.DOTALL);

    public static String getGameCode() {
        List<GameInfoSqlUtil.GameInfoRow> rows = new ArrayList<>();
        List<String> gameCodes = new ArrayList<>();
        for (SheetSource source : SHEET_SOURCES) {
            loadSheet(source, rows, gameCodes);
        }
        String sql = "-- compare\n"
                + GameInfoSqlUtil.buildGameCodeCompareSql("TJ", gameCodes)
                + "-- game_info (TJ)\n"
                + GameInfoSqlUtil.buildGameInfoBatchUpsertSql(rows);
        log.info("有效游戏合计: {}", rows.size());
        System.out.println(sql);
        return sql;
    }

    /** 下载 + 解析一页，写入 rows / gameCodes */
    private static void loadSheet(SheetSource source,
                                  List<GameInfoSqlUtil.GameInfoRow> rows,
                                  List<String> gameCodes) {
        // 下载
        HttpResponse resp = HttpRequest.get(source.xlsxUrl)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .setFollowRedirects(true)
                .timeout(60_000)
                .execute();
        byte[] xlsx = resp.bodyBytes();
        log.info("下载 xlsx [{}] status={}, bytes={}", source.name, resp.getStatus(),
                xlsx == null ? 0 : xlsx.length);
        if (!resp.isOk() || xlsx == null || xlsx.length < 100) {
            throw new IllegalStateException("xlsx 下载失败: " + source.name);
        }

        // 解压三个 XML
        String sharedXml = null, sheetXml = null, relsXml = null;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(xlsx))) {
            for (ZipEntry e; (e = zis.getNextEntry()) != null; ) {
                String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                switch (e.getName()) {
                    case "xl/sharedStrings.xml" -> sharedXml = xml;
                    case "xl/worksheets/sheet1.xml" -> sheetXml = xml;
                    case "xl/worksheets/_rels/sheet1.xml.rels" -> relsXml = xml;
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("解析 xlsx 失败: " + source.name, e);
        }
        if (StringUtils.isAnyBlank(sharedXml, sheetXml, relsXml)) {
            throw new IllegalStateException("xlsx 结构不完整: " + source.name);
        }

        // sharedStrings
        List<String> shared = new ArrayList<>();
        for (String si : sharedXml.split("<si>")) {
            if (!si.contains("</si>")) {
                continue;
            }
            si = si.substring(0, si.indexOf("</si>"));
            StringBuilder sb = new StringBuilder();
            Matcher tm = SHARED_T_PATTERN.matcher(si);
            while (tm.find()) {
                sb.append(tm.group(1).replace("&lt;", "<").replace("&gt;", ">")
                        .replace("&amp;", "&").replace("&quot;", "\""));
            }
            shared.add(sb.toString());
        }

        // V 列 Icon：行号 -> view url
        Map<String, String> ridToUrl = new HashMap<>();
        Matcher rm = REL_PATTERN.matcher(relsXml);
        while (rm.find()) {
            ridToUrl.put(rm.group(1), rm.group(2).replace("&amp;", "&"));
        }
        Map<Integer, String> iconByRow = new HashMap<>();
        Matcher hm = ICON_LINK_PATTERN.matcher(sheetXml);
        while (hm.find()) {
            String row = hm.group(1) != null ? hm.group(1) : hm.group(4);
            String rid = hm.group(2) != null ? hm.group(2) : hm.group(3);
            String url = ridToUrl.get(rid);
            if (StringUtils.isBlank(url) || !url.contains("drive.google.com")) {
                continue;
            }
            Matcher idm = DRIVE_ID_PATTERN.matcher(url);
            iconByRow.put(Integer.parseInt(row),
                    idm.find() ? "https://drive.google.com/uc?export=view&id=" + idm.group(1) : url);
        }

        // 数据行
        List<SheetGame> games = new ArrayList<>();
        Matcher rowMatcher = ROW_PATTERN.matcher(sheetXml);
        while (rowMatcher.find()) {
            int rowNum = Integer.parseInt(rowMatcher.group(1));
            if (rowNum == 1) {
                continue;
            }
            Map<String, String> cols = new HashMap<>();
            Matcher cm = CELL_PATTERN.matcher(rowMatcher.group(2));
            while (cm.find()) {
                String raw = cm.group(4);
                if (raw == null) {
                    continue;
                }
                String attrs = cm.group(3) == null ? "" : cm.group(3);
                String value = raw;
                if (attrs.contains("t=\"s\"") && StringUtils.isNumeric(raw.trim())) {
                    int si = Integer.parseInt(raw.trim());
                    if (si >= 0 && si < shared.size()) {
                        value = shared.get(si);
                    }
                }
                cols.put(cm.group(1), value);
            }
            String gameId = StringUtils.trimToEmpty(cols.get("A"));
            if (gameId.endsWith(".0")) {
                gameId = gameId.substring(0, gameId.length() - 2);
            }
            String nameEn = StringUtils.trimToEmpty(cols.get("C"));
            if (StringUtils.isAnyBlank(gameId, nameEn)) {
                continue;
            }
            String logo = iconByRow.get(rowNum);
            if (StringUtils.isBlank(logo)) {
                logo = StringUtils.trimToEmpty(cols.get("V"));
                log.warn("无 Icon 超链接，回退文件名: gameId={}, icon={}", gameId, logo);
            }
            games.add(new SheetGame(gameId, StringUtils.trimToEmpty(cols.get("B")), nameEn, logo));
        }
        if (games.isEmpty()) {
            throw new IllegalStateException("无有效数据行: " + source.name);
        }

        log.info("开始处理 TJ {}，共 {} 条", source.name, games.size());
        for (int i = 0; i < games.size(); i++) {
            SheetGame g = games.get(i);
            log.info("---------- [{}] [{}/{}] {} ({}) ----------",
                    source.name, i + 1, games.size(), g.nameEn, g.gameId);
            rows.add(GameInfoSqlUtil.buildGameInfoRowWithZhEn(
                    "TJ", "TJ", g.nameZh, g.nameEn, g.gameId, source.kind.getType(), g.logo,
                    "en-US", TRANSLATE_LOCALES));
            gameCodes.add(g.gameId);
        }
    }

    private static final class SheetSource {
        final String name;
        final GameKindEnum kind;
        final String xlsxUrl;

        SheetSource(String name, GameKindEnum kind, String xlsxUrl) {
            this.name = name;
            this.kind = kind;
            this.xlsxUrl = xlsxUrl;
        }
    }

    private static final class SheetGame {
        final String gameId;
        final String nameZh;
        final String nameEn;
        final String logo;

        SheetGame(String gameId, String nameZh, String nameEn, String logo) {
            this.gameId = gameId;
            this.nameZh = nameZh;
            this.nameEn = nameEn;
            this.logo = logo;
        }
    }

    public static void main(String[] args) {
        getGameCode();
    }

}
