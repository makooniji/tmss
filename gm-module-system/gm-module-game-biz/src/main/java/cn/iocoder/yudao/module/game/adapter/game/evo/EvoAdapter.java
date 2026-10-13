package cn.iocoder.yudao.module.game.adapter.game.evo;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.framework.common.model.game.GameApiRequest;
import cn.iocoder.yudao.framework.common.model.game.GameApiRtp;
import cn.iocoder.yudao.framework.common.model.game.GameApiTransfer;
import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.framework.common.model.game.push.*;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSSettle;
import cn.iocoder.yudao.framework.common.util.GameInfoSqlUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.framework.common.enums.CurrencyEnum;
import cn.iocoder.yudao.module.system.enums.ResultTypeEnum;
import cn.iocoder.yudao.module.system.enums.GameKindEnum;
import cn.iocoder.yudao.module.system.model.*;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.module.system.model.game.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.model.game.SportContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EvoAdapter extends BaseAdapter {


    @Override
    public String getVendorCode() {
        return "EVO";
    }

    @Override
    public PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request) {


        Map<String, Object> params = new HashMap<>();
        params.put("uuid", IdUtil.fastUUID());
        Map<String, Object> player = new HashMap<>();
        player.put("id", request.getUsername());
        player.put("update", true);
        player.put("firstName", request.getUsername());
        player.put("lastName", request.getUsername());


        String country = switch (request.getCurrency()){
            case "IDR"->"ID";
            case  "VND"->"VN";
            case  "BRL"->"BR";
            case  "MYR"->"MY";
            default->"AD";
        };

        player.put("country", country);

        String lang = request.getLanguage().split("-")[0];
        player.put("language", lang);
        player.put("currency", request.getCurrency());
        Map<String, Object> session = new HashMap<>();
        session.put("id", IdUtil.fastUUID());
        session.put("ip", request.getIpAddress());
        player.put("session", session);
        params.put("player", player);
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> game = new HashMap<>();
        config.put("game", game);
        Map<String, Object> channel = new HashMap<>();
        channel.put("wrapped", false);
        channel.put("mobile", false);
        config.put("channel", channel);
        params.put("config", config);

        this.doRequest(vendor, "/ua/v1/"+vendor.getVendorKey()+"/"+vendor.getVendorAgent(), params, "EVO创建用户", "", false);
        return PlatformResponse.success(request.getUsername());
    }

    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request, VendorUserDO gameUser) {
        Map<String, Object> params = new HashMap<>();
        params.put("uuid", IdUtil.fastUUID());
        Map<String, Object> player = new HashMap<>();
        player.put("id", gameUser.getVendorUsername());
        player.put("update", true);
        player.put("firstName", gameUser.getVendorUsername());
        player.put("lastName", gameUser.getVendorUsername());

        String country = switch (request.getCurrency()){
            case "IDR"->"ID";
            case  "VND"->"VN";
            case  "BRL"->"BR";
            case  "MYR"->"MY";
            default->"AD";
        };
        player.put("country", country);
        String lang = request.getLanguage().split("-")[0];
        player.put("language", lang);
        player.put("currency", request.getCurrency());
        Map<String, Object> session = new HashMap<>();
        session.put("id", IdUtil.fastUUID());
        session.put("ip", request.getIpAddress());
        player.put("session", session);
        params.put("player", player);
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> game = new HashMap<>();
        Map<String, Object> table = new HashMap<>();
        table.put("id", request.getGameCode());
        game.put("table", table);
        config.put("game", game);
        Map<String, Object> channel = new HashMap<>();
        channel.put("wrapped", false);
        channel.put("mobile", false);
        config.put("channel", channel);
        params.put("config", config);

        JSONObject dataMap = this.doRequest(vendor, "/ua/v1/"+vendor.getVendorKey()+"/"+vendor.getVendorAgent(), params, "EVO获取游戏地址", "", false);
        String gameUrl = dataMap.getString("entry");
        if (StringUtils.isBlank(gameUrl)) {
            log.error("EVO返回url为空: {}", dataMap);
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }
        return PlatformResponse.success(gameUrl);
    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {


        Map<String, Object> params = new HashMap<>();
        params.put("uuid", IdUtil.fastUUID());
        params.put("playerLogin", account);
        this.doRequest(vendor, "/api/external/kickPlayer/v1/"+vendor.getVendorKey()+"/"+vendor.getVendorAgent(), params, "EVO退出用户", "", true);
        return PlatformResponse.success();
    }

    @Override
    public PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer) {
        return null;
    }



    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("status", "OK");
        parseObject.put("balance",  sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("uuid", IdUtil.fastUUID());
        return PlatformResponse.success(parseObject);
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext sportContext) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        parseObject.put("balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("timestamp", sportContext.getTimestamp());
        return PlatformResponse.success(parseObject);
    }

    @Override
    protected List<DSBetRequest> buildBet(PlatformRequest param, SportContext context) {
        JSONObject transaction = (JSONObject) param.getExtraData().get("transaction");
        if (transaction == null) {
            transaction = new JSONObject();
        }
        BigDecimal amount = Convert.toBigDecimal(transaction.getString("amount"), BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(context.getCurrencyRate()), 2, RoundingMode.HALF_DOWN);
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

        GameRecordsDO record = getRecord(context.getOrderId());
        if (ObjectUtil.isEmpty(record)) {
            throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
        }

        JSONObject order = (JSONObject) param.getExtraData().get("transaction");
        if (order == null) {
            order = new JSONObject();
        }

        double amountChange = Convert.toDouble(order.getString("amount"), 0D) / context.getCurrencyRate();
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
        betDSRequest.setWinLoss(BigDecimal.valueOf(winAmount).subtract(record.getBetAmount()));
        betDSRequest.setActionEnum(ActionEnum.SETTLED);
        betDSRequest.setJackpotAmount(BigDecimal.valueOf(jackpotAmount));
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
        JSONObject extraData = JSON.parseObject(JSON.toJSONString(param.getExtraData()));
        if (extraData == null) {
            extraData = new JSONObject();
        }
        param.setExtraData(extraData);

        String traceId = MDC.get("traceId");
        ctx.setTraceId(traceId);
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setSign(param.getParamMap().getOrDefault("sign", ""));

        ctx.setPullUsername(extraData.getOrDefault("userId", "").toString());
        JSONObject transJson =  extraData.getJSONObject("transaction");

        if (ObjectUtil.isNotEmpty(transJson)) {
            JSONObject gameJson =  extraData.getJSONObject("game");
            if (gameJson == null) {
                gameJson = new JSONObject();
            }

            String gameCode = Optional.ofNullable(gameJson.getJSONObject("details"))
                    .map(details -> details.getJSONObject("table"))
                    .map(table -> table.getString("id"))
                    .orElse("");

            String transactionId = transJson.getString("id");

            ctx.setTransferId(transactionId);
            ctx.setExternalTransactionId(transactionId);
            ctx.setOrderId(gameJson.getString("id"));

            ctx.setRoundId(gameJson.getString("id"));
            ctx.setGameCode(gameCode);
        }
        ctx.setCurrency(param.getGameVendor().getCurrency().get(0));
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
        return CurrencyEnum.fromCode(currency).getCode();
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

        String changeType = param.getAction();

        if ("/balance".equals(changeType)) {
            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException(PlatformCodeEnum.INVALID_GAME);
        }


        switch (changeType) {
            case "/debit":
                List<DSBetRequest> dsBetRequests = buildBet(param, ctx);

                for (DSBetRequest item : dsBetRequests) {
                    ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getAmount(), ActionEnum.BET.getPath(),
                            item,
                            (order) -> {
                                DSBetRequest bet = (DSBetRequest) order;
                                return saveRecord(ctx, gameInfoDO.getCateId(), bet.getTransactionId(), bet.getBetId(), bet.getRoundId(), ActionEnum.BET, bet.getAmount().abs().doubleValue(), ctx.getUser().getUsername(),
                                        bet.getBetContent(), item.getOdds(), bet.getGameCode(), ctx.getUser().getTenantId());
                            },
                            (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                }
                break;
            case "/credit":
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
                                record.setWinAmount(bet.getWinAmount());
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
            case "/cancel":
                GameRecordsDO record = super.getRecord(ctx.getOrderId());
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
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {
        return List.of();
    }


    protected JSONObject doRequest(GameVendorDO vendor, String path, Map<String, Object> paramMap, String remark, String sign, Boolean async) {
        JSONObject parseObject;
        String result=null;
        try {
            result = HttpUtils.doJsonRequest(vendor.getApiUrl() + path, paramMap, null, async).get(10, TimeUnit.SECONDS);
            parseObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("{}，请求厂商:{} 异常", remark, vendor.getVendorName(), e);
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR,e.getMessage());
        }

        if (ObjectUtil.isEmpty(parseObject)) {
            log.error("{}，请求厂商:{} 返回为空", remark, vendor.getVendorName());
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }
        String entry = parseObject == null ? null : parseObject.getString("entry");
        if (StringUtils.isBlank(entry)) {
            log.error("{}，请求厂商:{} 上游返回失败  msg={}", remark, vendor.getVendorName(),  parseObject);
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR.getCode(), StringUtils.defaultIfBlank(Objects.toString(parseObject, null), vendor.getVendorName()+"：请求失败"
            ));
        }
        return parseObject;
    }

    @Override
    public String buildSign(Map<String, Object> dataMap, String key) {
        return SecureUtil.md5(dataMap.get("jsonBody").toString() + dataMap.get("ts") + key);
    }




    // ---------- 临时工具：拉 EVO/EVS tablelist，生成 game_info SQL ----------
    private static final String TRANSLATE_LOCALES = "zh-CN,en-US,pt-BR,ms-MY";

    /**
     * 共用请求形态：GET {host}/api/lobby/v1/{casinoKey}/tablelist + Basic(casinoKey:token)
     * 域名与密钥由调用方传入（EVO / EVS 各自一套）。
     */
    private static String fetchTableList(String hostname, String casinoKey, String apiToken) {
        String url = StringUtils.removeEnd(hostname, "/") + "/api/lobby/v1/" + casinoKey + "/tablelist";
        String auth = "Basic " + Base64.encode(casinoKey + ":" + apiToken);
        log.info("tablelist GET: {}", url);
        String result = HttpRequest.get(url)
                .header("Authorization", auth)
                .header("Accept", "application/json")
                .timeout(60_000)
                .execute()
                .body();
        log.info("tablelist 返回长度: {}", result == null ? 0 : result.length());
        if (StringUtils.isBlank(result)) {
            throw new IllegalStateException("tablelist 返回空: " + url);
        }
        return result;
    }

    /**
     * EVO 真人：game_code = Direct Launch Table ID，分类 LIVE。
     */
    public static String getGameCode(String hostname,String casinoKey,String apiToken) {
        List<JSONObject> tables = parseTableList(fetchTableList(hostname, casinoKey, apiToken));
        if (tables.isEmpty()) {
            throw new IllegalStateException("EVO拉游戏解析桌台为空");
        }
        String SQL_VENDOR_EVO = "EVO";
        int cateId = GameKindEnum.LIVE.getType();
        List<GameInfoSqlUtil.GameInfoRow> gameInfoRows = new ArrayList<>();
        LinkedHashSet<String> gameCodes = new LinkedHashSet<>();
        Set<String> insertedLaunchIds = new HashSet<>();

        log.info("开始处理 EVO 桌台，接口返回 {} 条", tables.size());
        for (JSONObject table : tables) {
            String directLaunchId = table.getString("Direct Launch Table ID");
            if (StringUtils.isBlank(directLaunchId) || !insertedLaunchIds.add(directLaunchId)) {
                continue;
            }
            String tableId = table.getString("Table ID");
            String tableName = table.getString("Table Name");
            String gameCode = directLaunchId;
            String gameName = StringUtils.isNotBlank(tableName)
                    ? tableName
                    : (StringUtils.isNotBlank(tableId) ? tableId : directLaunchId);

            log.info("---------- [{}/{}] {} ({}) ----------",
                    gameInfoRows.size() + 1, tables.size(), gameName, gameCode);
            gameInfoRows.add(GameInfoSqlUtil.buildGameInfoRow(
                    SQL_VENDOR_EVO, SQL_VENDOR_EVO, gameName, gameCode, cateId, "",
                    "en-US", TRANSLATE_LOCALES));
            gameCodes.add(gameCode);
            if (StringUtils.isNotBlank(tableId)) {
                gameCodes.add(tableId);
            }
        }

        String sql = "-- compare: db exists but api not returned\n"
                + GameInfoSqlUtil.buildGameCodeCompareSql(SQL_VENDOR_EVO, new ArrayList<>(gameCodes))
                + "-- game_info (EVO LIVE)\n"
                + GameInfoSqlUtil.buildGameInfoBatchUpsertSql(gameInfoRows);
        log.info("EVO有效桌台: {}", gameInfoRows.size());
        System.out.println(sql);
        return sql;
    }

    /**
     * EVS 电子：走 EVS 自己的 hostname/casinoKey/token。
     * game_code = Table ID，logo = Thumbnails.M，分类 ELECTRONIC。
     */
    public static String getEvsGameCode(String hostname,String casinoKey,String apiToken) {
        List<JSONObject> tables = parseTableList(fetchTableList(hostname, casinoKey, apiToken));
        if (tables.isEmpty()) {
            throw new IllegalStateException("EVS拉游戏解析桌台为空");
        }
        String SQL_VENDOR_EVS = "EVS";

        int cateId = GameKindEnum.ELECTRONIC.getType();
        List<GameInfoSqlUtil.GameInfoRow> gameInfoRows = new ArrayList<>();
        LinkedHashSet<String> gameCodes = new LinkedHashSet<>();
        Set<String> insertedTableIds = new HashSet<>();
        Map<String, Integer> providerCount = new LinkedHashMap<>();

        log.info("开始处理 EVS 游戏，接口返回 {} 条", tables.size());
        for (JSONObject table : tables) {
            String tableId = table.getString("Table ID");
            if (StringUtils.isBlank(tableId) || !insertedTableIds.add(tableId)) {
                continue;
            }
            String tableName = table.getString("Table Name");
            String gameName = cleanEvsGameName(StringUtils.isNotBlank(tableName) ? tableName : tableId);
            String provider = table.getString("Game Provider");
            String logo = extractThumbnailM(table);
            providerCount.merge(StringUtils.defaultIfBlank(provider, "null"), 1, Integer::sum);

            log.info("---------- [{}/{}] {} ({}) provider={} ----------",
                    gameInfoRows.size() + 1, tables.size(), gameName, tableId, provider);
            gameInfoRows.add(GameInfoSqlUtil.buildGameInfoRow(
                    SQL_VENDOR_EVS, SQL_VENDOR_EVS, gameName, tableId, cateId, logo,
                    "en-US", TRANSLATE_LOCALES));
            gameCodes.add(tableId);
        }

        System.out.println("===== Game Provider 统计 =====");
        providerCount.forEach((p, c) -> System.out.printf("  %-16s : %d%n", p, c));
        System.out.printf("EVS有效: %d%n", gameInfoRows.size());

        String sql = "-- compare: db exists but api not returned\n"
                + GameInfoSqlUtil.buildGameCodeCompareSql(SQL_VENDOR_EVS, new ArrayList<>(gameCodes))
                + "-- game_info (EVS ELECTRONIC)\n"
                + GameInfoSqlUtil.buildGameInfoBatchUpsertSql(gameInfoRows);
        System.out.println(sql);
        return sql;
    }

    private static String extractThumbnailM(JSONObject table) {
        JSONObject thumbs = table.getJSONObject("Thumbnails");
        if (thumbs == null) {
            return "";
        }
        return StringUtils.defaultString(thumbs.getString("M"));
    }

    /** 只去掉名称末尾的 DNT（如 "xxx DNT DNT" -> "xxx"） */
    private static String cleanEvsGameName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        return name.replaceAll("(?i)(\\s+DNT)+$", "").trim();
    }

    /** 共用：解析 tablelist 全部 data 条目（不过滤字段） */
    private static List<JSONObject> parseTableList(String result) {
        List<JSONObject> tables = new ArrayList<>();
        Object root = JSON.parse(result);
        List<JSONObject> blocks = new ArrayList<>();
        if (root instanceof JSONArray arr) {
            for (int i = 0; i < arr.size(); i++) {
                Object item = arr.get(i);
                if (item instanceof JSONObject obj) {
                    blocks.add(obj);
                }
            }
        } else if (root instanceof JSONObject obj) {
            blocks.add(obj);
        }
        for (JSONObject block : blocks) {
            JSONArray data = block.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                continue;
            }
            for (int i = 0; i < data.size(); i++) {
                JSONObject table = data.getJSONObject(i);
                if (table != null) {
                    tables.add(table);
                }
            }
        }
        return tables;
    }

    public static void main(String[] args) {
        // EVO 真人（live）
        String EVO_HOSTNAME = "https://site-stag-api.nimstad99.com";
        String EVO_CASINO_KEY = "y9kkudfany3gax9s";
        String EVO_API_TOKEN = "12b5321e08294efbd9214e2956ec3565";

        // getGameCode();   // EVO 真人


        // EVS 电子（slots）——域名/密钥与 EVO 不同，按集成环境自行填写
        String EVS_HOSTNAME = "https://site-stag-api.klonostek.com";
        String EVS_CASINO_KEY = "ulm5vk6ezxiopywq";
        String EVS_API_TOKEN = "21cbec1e1ebda0ec349207b894a6eacb";

        getEvsGameCode(EVS_HOSTNAME, EVS_CASINO_KEY, EVS_API_TOKEN);   // EVS 电子
    }

}
