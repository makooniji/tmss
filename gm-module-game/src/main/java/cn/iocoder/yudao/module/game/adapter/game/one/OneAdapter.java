package cn.iocoder.yudao.module.game.adapter.game.one;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.adapter.game.base.BaseAdapter;
import cn.iocoder.yudao.module.game.adapter.game.one.model.JLSResponse;
import cn.iocoder.yudao.module.game.adapter.game.one.pull.JLSResultImpl;
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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * WooAPI平台服务实现
 */
@Service
@Slf4j
public class OneAdapter extends BaseAdapter {

    @Resource
    private JLSResultImpl jlsResult;


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
    public PlatformResponse<Object> doForward(ActionEnum actionEnum, PlatformRequest param, SportContext ctx) {
        JSONObject parseObject = new JSONObject();


        if (ObjectUtil.isEmpty(ctx.getPullUsername())) {
            throw new GameException("SC_USER_NOT_EXISTS");
        }
        ctx.setGameVendor(param.getGameVendor());
        VendorUserDO user = getUser(ctx.getPullUsername(),param.getVendorCode());
        if (ObjectUtil.isEmpty(user)) {
            throw new GameException("SC_USER_NOT_EXISTS");
        }
        ctx.setPushUsername(user.getUsername());
        VendorUserBalanceDO balance = balanceService.getBalance(ctx.getPushUsername(), ctx.getWrapCurrency());
        if (ObjectUtil.isEmpty(balance)) {
            throw new GameException("SC_WRONG_CURRENCY");
        }
        ctx.setBalance(balance.getBalance());
        ctx.setUser(user);



        TenantUtils.execute(user.getTenantId(), () -> {
            var vendorTenantDO = vendorTenantService.getVendorByCode(param.getVendorCode());

            if (ObjectUtil.isEmpty(vendorTenantDO)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            ctx.setVendorTenant(vendorTenantDO);
            if(ObjectUtil.isNotEmpty(vendorTenantDO.getCurrency())){
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
                throw new GameException("SC_INVALID_SIGNATURE");
            }

        }


        if ("/wallet/balance".equals(changeType)) {

            return doBalance(actionEnum, param, ctx);

        }

        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", ctx.getGameCode());
        if (ObjectUtil.isEmpty(gameInfoDO)) {
            throw new GameException("SC_INVALID_GAME");
        }
        GameRecordsDO exists = recordsMapper.selectOne("ref_order_no", ctx.getTransferId());
        if (ObjectUtil.isNotEmpty(exists)) {
            return buildIdleResult(ctx);
        }

        switch (changeType) {
            case "/wallet/bet" -> {
                DSBetRequest DSBetRequest = buildBet(param, ctx);
                if (NumberUtil.isLess(ctx.getBalance(), DSBetRequest.getAmount())) {
                    throw new GameException("SC_INSUFFICIENT_FUNDS");
                }
                GameRecordsDO record = super.getRecord(DSBetRequest.getBetId());
                if(ObjectUtil.isEmpty(record)){
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
                }


                parseObject.put("username", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("balance", ctx.getBalance());
                parseObject.put("timestamp", ctx.getTimestamp());
                return PlatformResponse.success(JLSResponse.success(parseObject));


            }
            case "/wallet/rollback" -> {
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

                                GameStaticDTO.buildRefund(user, ctx.getGameVendor(), gameInfoDO, record.getBetAmount());

                                return record.getId();
                            },
                            (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                }

                parseObject.put("username", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("balance", ctx.getBalance());
                parseObject.put("timestamp", ctx.getTimestamp());
                return PlatformResponse.success(JLSResponse.success(parseObject));
            }

            /*需特殊处理
             resultType：
                          -""WIN"" -  赢了，加额度
                          -""BET_WIN"" - 赢了，加额度 (需扣除下注额度)
                          -""BET_LOSE"" - 输了，扣额度 (需扣除下注额度)
                          -""LOSE"" - 无需作任何动作，无需加减额度
                          - ""END"" - 只是通知贵司该下注已结算，无需作任何动
            */
            case "/wallet/bet_result" -> {

                String resultType = dataMap.getOrDefault("resultType", "").toString();



                DSBetResultRequest betResult = buildBetResult(param, ctx);


                if (Arrays.asList("LOSE", "END", "WIN").contains(resultType)) {

                    GameRecordsDO record = super.getRecord(ctx.getOrderId());
                    //需要验证订单是否存在
                    if (ObjectUtil.isEmpty(record)) {
                        throw new GameException("SC_TRANSACTION_NOT_EXISTS");
                    }
                }

                GameRecordsDO record = super.getRecord(ctx.getOrderId());
                if(ObjectUtil.isEmpty(record)) {
                    if (NumberUtil.isLess(ctx.getBalance(), betResult.getBetAmount())) {
                        throw new GameException("SC_INSUFFICIENT_FUNDS");
                    }
                }

                BigDecimal changeAmount = betResult.getWinAmount().subtract(betResult.getBetAmount()).add(betResult.getJackpotAmount());

                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), changeAmount, ActionEnum.SETTLED.getPath(),
                        betResult,

                        (order) -> {
                            DSBetResultRequest bet = (DSBetResultRequest) order;
                            if (!Arrays.asList("LOSE", "END").contains(resultType)) {


                                if(ObjectUtil.isEmpty(record)) {


                                    GameRecordsDO game = new GameRecordsDO();
                                    Long id = IdUtil.getSnowflakeNextId();
                                    game.setId(id);
                                    game.setGameCode(bet.getGameCode());
                                    game.setUsername(bet.getUsername());
                                    game.setRefOrderNo(bet.getTransactionId());
                                    game.setOrderNo(bet.getBetId());
                                    game.setIssueNo(bet.getRoundId());
                                    game.setAction(bet.getActionEnum());
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
                                    recordsMapper.insert(game);
                                    GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, game.getWinAmount(), game.getWinLoss(), game.getWinLoss().doubleValue() > 0);
                                    return game.getId();
                                }else {
                                    record.setAction(ActionEnum.SETTLED);
                                    record.setWinAmount(bet.getWinAmount());
                                    record.setRemark(bet.getActionEnum().getDesc());
                                    record.setOdds(bet.getOdds());

                                    record.setUpdateTime(LocalDateTime.now());
                                    record.setJackpotAmount(bet.getJackpotAmount());
                                    recordsMapper.updateById(record);
                                    GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinLoss().doubleValue() > 0);
                                    return record.getId();
                                }
                            }
                            return null;
                        },
                        (amt) -> {
                            if (!Arrays.asList("LOSE", "END").contains(resultType)) {
                                super.operateBalance(amt, betResult.getUsername(), ctx.getWrapCurrency());
                            }
                        });

                parseObject.put("username", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("balance", ctx.getBalance());
                parseObject.put("timestamp", ctx.getTimestamp());

                return PlatformResponse.success(JLSResponse.success(parseObject));
            }

            case "/wallet/bet_debit" -> {


                DSDebit betDSRequest = buildDebit(param, ctx);

                BigDecimal changeAmount;
                if (betDSRequest.getTakeAll().equals(1)) {
                    changeAmount = ctx.getBalance().negate();
                } else {
                    changeAmount = betDSRequest.getAmount().negate();
                }

                if (changeAmount.doubleValue() < 0) {
                    if (NumberUtil.isLess(ctx.getBalance(), changeAmount.abs())) {
                        throw new GameException("SC_INSUFFICIENT_FUNDS");
                    }
                }
                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), changeAmount, ActionEnum.DEBIT.getPath(),
                        betDSRequest,

                        (order) -> {
                            DSDebit item = (DSDebit) order;
                            GameRecordsDO game = new GameRecordsDO();
                            Long id = IdUtil.getSnowflakeNextId();
                            game.setId(id);
                            game.setGameCode(ctx.getGameCode());
                            game.setUsername(ctx.getPushUsername());
                            game.setRefOrderNo(item.getTransactionId());

                            game.setIssueNo(item.getRoundId());
                            game.setAction(ActionEnum.DEBIT);
                            game.setGameKind(gameInfoDO.getCateId());
                            game.setBetAmount(item.getAmount());
                            game.setWinAmount(BigDecimal.ZERO);
                            game.setRemark(ActionEnum.DEBIT.getDesc());
                            game.setOdds(BigDecimal.ZERO);
                            game.setCreateTime(LocalDateTime.now());
                            game.setTenantId(user.getTenantId());
                            game.setUpdateTime(LocalDateTime.now());
                            game.setJackpotAmount(BigDecimal.ZERO);
                            game.setTenantCode(ctx.getTenant().getCode());
                            game.setCurrency(ctx.getCurrency());
                            game.setUserId(user.getId());
                            recordsMapper.insert(game);
                            return game.getId();

                        },
                        (amt) -> super.operateBalance(amt, betDSRequest.getUsername(), ctx.getWrapCurrency()));


                parseObject.put("username", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("balance", ctx.getBalance());
                parseObject.put("timestamp", ctx.getTimestamp());

                return PlatformResponse.success(JLSResponse.success(parseObject));
            }
            case "/wallet/bet_credit" -> {


                DSCredit betDSRequest = buildCredit(param, ctx);
                BigDecimal changeAmount = betDSRequest.getAmount();

                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), changeAmount, ActionEnum.CREDIT.getPath(),
                        betDSRequest,

                        (order) -> {
                            DSCredit item = (DSCredit) order;
                            GameRecordsDO game = new GameRecordsDO();
                            Long id = IdUtil.getSnowflakeNextId();
                            game.setId(id);
                            game.setGameCode(ctx.getGameCode());
                            game.setUsername(ctx.getPushUsername());
                            game.setRefOrderNo(item.getTransactionId());
                            game.setOrderNo(item.getBetId());
                            game.setIssueNo(item.getRoundId());
                            game.setAction(ActionEnum.DEBIT);
                            game.setGameKind(gameInfoDO.getCateId());
                            game.setBetAmount(BigDecimal.ZERO);
                            game.setWinAmount(item.getAmount());
                            game.setRemark(ActionEnum.DEBIT.getDesc());
                            game.setOdds(BigDecimal.ZERO);
                            game.setCreateTime(LocalDateTime.now());
                            game.setTenantId(user.getTenantId());
                            game.setUpdateTime(LocalDateTime.now());
                            game.setJackpotAmount(BigDecimal.ZERO);
                            game.setTenantCode(ctx.getTenant().getCode());
                            game.setCurrency(ctx.getCurrency());
                            game.setUserId(user.getId());
                            recordsMapper.insert(game);
                            return game.getId();
                        },
                        (amt) -> super.operateBalance(amt, betDSRequest.getUsername(), ctx.getWrapCurrency()));

                parseObject.put("username", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("balance", ctx.getBalance());
                parseObject.put("timestamp", ctx.getTimestamp());

                return PlatformResponse.success(JLSResponse.success(parseObject));
            }

            case "/wallet/adjustment" -> {

                DSAdjustment adjustment = buildAdjustment(param, ctx);
                if (adjustment.getAmount().doubleValue() < 0) {
                    if (NumberUtil.isLess(ctx.getBalance(), adjustment.getAmount().abs())) {
                        throw new GameException("SC_INSUFFICIENT_FUNDS");
                    }
                }


                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), adjustment.getAmount(), ActionEnum.ADJUSTMENT.getPath(),
                        adjustment,

                        (order) -> {
                            GameRecordsDO existsOrder = recordsMapper.selectOne(Wrappers.<GameRecordsDO>lambdaQuery().eq(GameRecordsDO::getIssueNo, ctx.getRoundId()));

                            if (ObjectUtil.isEmpty(existsOrder)) {
                                throw new GameException("SC_TRANSACTION_NOT_EXISTS");
                            }
                            existsOrder.setJackpotAmount(adjustment.getAmount());
                            updateRecord(existsOrder);
                            return existsOrder.getId();
                        },
                        (amt) -> super.operateBalance(amt, adjustment.getUsername(), ctx.getWrapCurrency()));


                parseObject.put("username", ctx.getPullUsername());
                parseObject.put("currency", ctx.getCurrency());
                parseObject.put("balance", ctx.getBalance());
                parseObject.put("timestamp", ctx.getTimestamp());
                return PlatformResponse.success(JLSResponse.success(parseObject));
            }
        }

        parseObject.put("username", ctx.getPullUsername());
        parseObject.put("currency", ctx.getCurrency());
        parseObject.put("balance", ctx.getBalance());
        parseObject.put("timestamp", ctx.getTimestamp());

        return PlatformResponse.success(JLSResponse.success(parseObject));

    }


    @Override
    public String getVendorCode() {
        return "ONEAPI";
    }

    @Override
    public PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request) {

        String username = vendor.getVendorChannel() + "_" + request.getUsername();
//        Map<String, Object> params = new HashMap<>();
//        params.put("username", username);
//        params.put("traceId", IdUtil.fastUUID());
//        params.put("gameCode", request.getGameCode());
//        params.put("language", request.getLanguage());
//        params.put("platform", request.getPlatform());
//        params.put("currency", request.getCurrency());
//        params.put("lobbyUrl", request.getLobbyUrl());
//        params.put("ipAddress", request.getIpAddress());
//
//        String sign =this.buildSign(params, vendor.getVendorKey());
//
//        JSONObject result = doRequest(vendor.getApiUrl()+"/game/url", params, "JLS创建会员账号", sign, true);
//        if (ObjectUtil.isEmpty(result)) {
//            log.info("JLS创建会员失败:{}", result);
//            return PlatformResponse.error(PlatformCodeEnum.USER_NOT_EXISTS.getCode());
//        }
//        String success = result.getString("status");
//        if (!success.equals("SC_OK")) {
//            log.info("JLS创建会员失败:{}", result);
//        }
        return PlatformResponse.success(username);
    }


    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", request.getUsername());
        params.put("traceId", request.getTraceId());
        params.put("gameCode", request.getGameCode());
        params.put("language", request.getLanguage());
        params.put("platform", request.getPlatform());
        params.put("currency", request.getCurrency());
        params.put("lobbyUrl", request.getLobbyUrl());
        params.put("ipAddress", request.getIpAddress());

        String sign = this.buildSign(params, vendor.getVendorKey());

        JSONObject dataMap = doRequest(vendor, "/game/url", params, "JLS获取游戏地址", sign, false);

        if (!dataMap.getString("status").equals("SC_OK")) {
            throw new GameException(dataMap.getString("status"));
        }

        JSONObject data = dataMap.getJSONObject("data");
        return PlatformResponse.success(data.getString("gameUrl"));
    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {
        Map<String, Object> params = new HashMap<>();
        params.put("traceId", IdUtil.fastUUID());
        params.put("username", account);
        String sign = this.buildSign(params, vendor.getVendorKey());
        this.doRequest(vendor, "/game/terminate", params, "JLS退出用户", sign, true);
        return PlatformResponse.success();
    }

    @Override
    public PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer) {
        return PlatformResponse.success();
    }

    @Override
    public String buildSign(Map<String, Object> dataMap, String key) {
        String data = JsonUtils.toJsonString(dataMap);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(data);
    }

    @Override
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {

        return jlsResult.execute(CollectionUtil.join(List.of(startTime, endTime), ","));
    }

    @Override
    protected SportContext buildContext(PlatformRequest param) {

        SportContext ctx = new SportContext();
        String traceId = MDC.get("traceId");
        ctx.setTraceId(traceId);
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setSign(param.getHeader().getOrDefault("X-Signature".toLowerCase(), ""));
        ctx.setTransferId(param.getExtraData().getOrDefault("transactionId", "").toString());
        ctx.setExternalTransactionId(param.getExtraData().getOrDefault("externalTransactionId", "").toString());
        ctx.setPullUsername(param.getExtraData().getOrDefault("username", "").toString());
        ctx.setOrderId(param.getExtraData().getOrDefault("betId", "").toString());
        ctx.setCurrency(param.getExtraData().getOrDefault("currency", "").toString());
        ctx.setRoundId(param.getExtraData().getOrDefault("roundId", "").toString());
        ctx.setGameCode(param.getExtraData().getOrDefault("gameCode", "").toString());
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
        return currency;
    }

    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext sportContext) {

        JSONObject parseObject = new JSONObject();
        parseObject.put("username", sportContext.getPullUsername());
        parseObject.put("currency", sportContext.getCurrency());
        parseObject.put("balance", sportContext.getBalance().multiply(BigDecimal.valueOf(sportContext.getCurrencyRate())));
        parseObject.put("timestamp", sportContext.getTimestamp());

        return PlatformResponse.success(JLSResponse.success(parseObject));
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
        double amount = Convert.toDouble(param.getExtraData().getOrDefault("amount", "0")) * context.getCurrencyRate();

        //存储下注信息
        DSBetRequest DSBetRequest = new DSBetRequest();
        DSBetRequest.setUsername(context.getPushUsername());
        DSBetRequest.setTransactionId(context.getTransferId());
        DSBetRequest.setExternalTransactionId(context.getOrderId());
        DSBetRequest.setBetId(context.getOrderId());
        DSBetRequest.setAmount(BigDecimal.valueOf(amount));
        DSBetRequest.setGameCode(context.getGameCode());
        DSBetRequest.setRoundId(context.getRoundId());

        return DSBetRequest;
    }

    /**
     * 下注&结算
     *
     * @param param
     * @param context
     * @return
     */
    @Override
    protected DSBetResultRequest buildBetResult(PlatformRequest param, SportContext context) {
        double betAmount = Convert.toDouble(param.getExtraData().getOrDefault("betAmount", "0")) * context.getCurrencyRate();
        double winAmount = Convert.toDouble(param.getExtraData().getOrDefault("winAmount", "0")) * context.getCurrencyRate();
        double winLoss = Convert.toDouble(param.getExtraData().getOrDefault("winLoss", "0")) * context.getCurrencyRate();
        double jackpotAmount = Convert.toDouble(param.getExtraData().getOrDefault("jackpotAmount", "0")) * context.getCurrencyRate();
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
        betDSRequest.setWinLoss(BigDecimal.valueOf(winLoss));
        betDSRequest.setJackpotAmount(BigDecimal.valueOf(jackpotAmount));
        betDSRequest.setResultType(resultType);

        return betDSRequest;
    }


    @Override
    protected List<DSRollback> buildRefund(PlatformRequest param, SportContext context) {

        GameRecordsDO record = getRecordByRoundId(context.getRoundId());
        if (ObjectUtil.isEmpty(record)) {
            throw new GameException("SC_TRANSACTION_NOT_EXISTS");
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
        double winAmount = Convert.toDouble(param.getExtraData().getOrDefault("winAmount", "0")) * context.getCurrencyRate();
        DSSettle dsBalance = new DSSettle();
        dsBalance.setTraceId(IdUtil.fastUUID());
        dsBalance.setTimestamp(context.getTimestamp());
        dsBalance.setUsername(context.getPushUsername());
        dsBalance.setTransactionId(context.getTransferId());
        dsBalance.setExternalTransactionId(context.getOrderId());
        dsBalance.setBetId(context.getOrderId());
        dsBalance.setGameCode(context.getGameCode());
        dsBalance.setWinAmount(BigDecimal.valueOf(winAmount));


        return List.of(dsBalance);
    }


    @Override
    protected DSAdjustment buildAdjustment(PlatformRequest param, SportContext context) {


        double amount = Convert.toDouble(param.getExtraData().getOrDefault("amount", "0")) * context.getCurrencyRate();
        DSAdjustment dsBalance = new DSAdjustment();
        dsBalance.setTraceId(IdUtil.fastUUID());
        dsBalance.setTimestamp(context.getTimestamp());
        dsBalance.setUsername(context.getPushUsername());
        dsBalance.setTransactionId(context.getTransferId());
        dsBalance.setExternalTransactionId(context.getOrderId());
        dsBalance.setAmount(BigDecimal.valueOf(amount));
        dsBalance.setRoundId(context.getRoundId());
        dsBalance.setGameCode(context.getGameCode());


        return dsBalance;
    }

    @Override
    protected DSDebit buildDebit(PlatformRequest param, SportContext context) {

        Integer takeAll = Convert.toInt(param.getExtraData().getOrDefault("takeAll", "0"));
        BigDecimal amount = Convert.toBigDecimal(param.getExtraData().getOrDefault("amount", "0"));


        DSDebit betDSRequest = new DSDebit();
        betDSRequest.setUsername(context.getPushUsername());
        betDSRequest.setTransactionId(context.getTransferId());
        betDSRequest.setGameCode(context.getGameCode());
        betDSRequest.setRoundId(context.getRoundId());
        betDSRequest.setTakeAll(takeAll);
        betDSRequest.setCurrency(context.getCurrency());
        betDSRequest.setTimestamp(context.getTimestamp());
        betDSRequest.setAmount(amount);


        return betDSRequest;
    }

    @Override
    protected DSCredit buildCredit(PlatformRequest param, SportContext context) {
        Integer isRefund = Convert.toInt(param.getExtraData().getOrDefault("isRefund", "0"));

        Long betTime = Convert.toLong(param.getExtraData().getOrDefault("betTime", "0"));
        Long settledTime = Convert.toLong(param.getExtraData().getOrDefault("settledTime", "0"));
        BigDecimal amount = Convert.toBigDecimal(param.getExtraData().getOrDefault("amount", "0"));
        BigDecimal betAmount = Convert.toBigDecimal(param.getExtraData().getOrDefault("betAmount", "0"));
        BigDecimal winAmount = Convert.toBigDecimal(param.getExtraData().getOrDefault("winAmount", "0"));
        BigDecimal effectiveTurnover = Convert.toBigDecimal(param.getExtraData().getOrDefault("effectiveTurnover", "0"));
        BigDecimal winLoss = Convert.toBigDecimal(param.getExtraData().getOrDefault("winLoss", "0"));
        BigDecimal jackpotAmount = Convert.toBigDecimal(param.getExtraData().getOrDefault("jackpotAmount", "0"));

        DSCredit item = new DSCredit();
        item.setUsername(context.getPushUsername());
        item.setTransactionId(context.getTransferId());
        item.setExternalTransactionId(context.getOrderId());
        item.setBetId(context.getOrderId());
        item.setGameCode(context.getGameCode());
        item.setRoundId(context.getRoundId());
        item.setCurrency(context.getCurrency());
        item.setTimestamp(context.getTimestamp());
        item.setAmount(amount);
        item.setBetAmount(betAmount);
        item.setIsRefund(isRefund);
        item.setWinAmount(winAmount);
        item.setEffectiveTurnover(effectiveTurnover);
        item.setWinLoss(winLoss);
        item.setJackpotAmount(jackpotAmount);
        item.setBetTime(betTime);
        item.setSettledTime(settledTime);
        return item;
    }

    @SneakyThrows
    protected JSONObject doRequest(GameVendorDO vendor, String url, Map<String, Object> paramMap, String remark, String sign, Boolean async) {


        String path = vendor.getApiUrl() + url;
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("X-API-Key", vendor.getVendorAgent());
        header.put("X-Signature", sign);
        log.info("{}，请求参数 paramMap={},header:{}", path, paramMap, header);
        var result = HttpUtils.doJsonRequest(path, paramMap, header, async).get(10, TimeUnit.SECONDS);

        JSONObject parseObject = JSONObject.parseObject(result);
        if (ObjectUtil.isEmpty(parseObject)) {
            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }
        String code = parseObject.getString("status");
        if (!code.equals("SC_OK")) {
            throw new GameException(parseObject.getString("status"),parseObject.getString("message"));
        }
        return parseObject;
    }
}
