package cn.iocoder.yudao.module.game.adapter.sports.saba;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.kafka.StatProducer;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.adapter.sports.base.BaseSportAdapter;
import cn.iocoder.yudao.module.game.adapter.sports.saba.enums.SabaParlayTypeEnum;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.info.GameInfoMapper;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.enums.CurrencyEnum;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.*;
import cn.iocoder.yudao.module.system.model.api.push.BaseDSRequest;
import cn.iocoder.yudao.module.system.model.api.push.DSAdjustment;
import cn.iocoder.yudao.module.system.model.api.push.DSBalance;
import cn.iocoder.yudao.module.system.model.api.push.DSRollback;
import cn.iocoder.yudao.module.system.model.api.push.sports.*;
import cn.iocoder.yudao.module.system.mq.model.GameStaticDTO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SabaAdapter extends BaseSportAdapter {
    @Resource
    private StatProducer statProducer;

    @Resource
    private DSManager DSManager;

    @Resource
    private GameInfoMapper gameInfoMapper;

    @Override
    protected String gameCode() {
        return "SABA_2";
    }

    @Override
    public String getVendorCode() {
        return "SABA";
    }

    @Override
    public PlatformResponse<Object> doForward(ActionEnum changeType, PlatformRequest param, SportContext ctx) {

        ctx.setVendorCode(param.getVendorCode());
        ctx.setGameVendor(param.getGameVendor());
        GameInfoDO gameInfoDO = gameInfoMapper.selectOne("game_code", gameCode());

        JSONObject parseObject = new JSONObject();

        // 绑定会员信息
        if (ObjectUtil.isNotEmpty(ctx.getPullUsername())) {
            bindTenantInfo(ctx);
        }
        if (changeType == ActionEnum.HEALTHCHECK) {
            parseObject.put("message", "success");
            parseObject.put("status", "0");
            return PlatformResponse.success(parseObject);

        }
        if (changeType == ActionEnum.BALANCE) {

            return doBalance(changeType, param, ctx);

        }
        boolean idled = super.idleRecord(ctx.getTransferId(), param.getExtraData());
        if (!idled) {
            return buildIdleResult(ctx);
        }


        if (changeType == ActionEnum.MARKETPLACE) {

            JSONArray result = new JSONArray();
            changeType = ActionEnum.BET;

            DSBetRequest dsUpdateBet = buildBetPar(param, changeType, ctx);

            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), dsUpdateBet.getCreditAmount(), ActionEnum.MARKETPLACE.getSportPath(),
                    dsUpdateBet,

                    (order) -> {
                        DSBetRequest bet = (DSBetRequest) order;

                        // 串关构建两条订单
                        for (DSBetRequest.MultipleItem item : dsUpdateBet.getMultipleBetIds()) {


                            saveRecord(ctx, gameInfoDO.getCateId(), bet.getTransactionId(), item.getBetId(), bet.getRoundId(),
                                    ActionEnum.BET, item.getBetAmount().doubleValue(), ctx.getUser().getUsername(),
                                    item.getBetContent(), item.getOdds(), bet.getGameCode(), ctx.getUser().getTenantId());

                        }
                        return null;
                    },
                    (amt) -> super.operateBalance(amt, ctx.getPushUsername(), ctx.getWrapCurrency()));

            for (DSBetRequest.MultipleItem item : dsUpdateBet.getMultipleBetIds()) {

                JSONObject jr = new JSONObject();
                jr.put("refId", item.getBetId());
                jr.put("licenseeTxId", item.getBetId());
                result.add(jr);
            }

            parseObject.put("status", "0");
            parseObject.put("balance", ctx.getBalance());
            parseObject.put("txns", result);
            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.CONFIRMATORY) {

            changeType = ActionEnum.BET_UPDATE;

            List<DSUpdateBet> dsUpdateBet = buildConfirm(param, changeType, ctx);
            for (DSUpdateBet item : dsUpdateBet) {
                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.BET_UPDATE.getSportPath(),
                        item,

                        (order) -> {
                            DSUpdateBet updateBet = (DSUpdateBet) order;
                            GameRecordsDO record = super.getRecord(updateBet.getBetId());
                            //更新下注金额
                            record.setRemark(ActionEnum.BET_UPDATE.getDesc());
                            record.setBetAmount(updateBet.getNewBetAmount());
                            record.setOdds(updateBet.getOdds());

                            super.updateRecord(record);


                            GameStaticDTO.buildBet(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getBetAmount());

                            return record.getId();
                        },
                        (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
            }
            parseObject.put("status", "0");
            parseObject.put("balance", ctx.getBalance());
            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.BET) {

            DSBetRequest DSBetRequest = buildBet(param, ctx);
            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), DSBetRequest.getBetAmount(), ActionEnum.BET.getSportPath(),
                    DSBetRequest,

                    (order) -> {
                        DSBetRequest bet = (DSBetRequest) order;
                        return saveRecord(ctx, gameInfoDO.getCateId(), bet.getTransactionId(), bet.getBetId(), bet.getRoundId(), ActionEnum.BET, bet.getBetAmount().doubleValue(), ctx.getUser().getUsername(),
                                bet.getBetContent(), DSBetRequest.getOdds(), bet.getGameCode(), ctx.getUser().getTenantId());
                    },
                    (amt) -> super.operateBalance(amt, DSBetRequest.getUsername(), ctx.getWrapCurrency()));


            parseObject.put("status", "0");
            parseObject.put("refId", ctx.getOrderId());
            parseObject.put("balance", execute.getBalance());
            parseObject.put("licenseeTxId", execute.getOrderId());
            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.BET_UPDATE) {


            List<DSUpdateBet> dsUpdateBet = buildConfirm(param, changeType, ctx);

            for (DSUpdateBet item : dsUpdateBet) {
                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.BET_UPDATE.getSportPath(),
                        item,

                        (order) -> {
                            DSUpdateBet updateBet = (DSUpdateBet) order;
                            GameRecordsDO record = super.getRecord(updateBet.getBetId());
                            //更新下注金额
                            record.setRemark(ActionEnum.BET_UPDATE.getDesc());
                            record.setBetAmount(updateBet.getNewBetAmount());
                            record.setOdds(updateBet.getOdds());

                            super.updateRecord(record);

                            GameStaticDTO.buildBet(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getBetAmount());

                            return record.getId();
                        },
                        (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
            }
            parseObject.put("status", "0");
            parseObject.put("balance", ctx.getBalance());
            return PlatformResponse.success(parseObject);

        } else if (ActionEnum.REFUND == changeType) {
            List<DSRollback> dsUpdateBet = buildRefund(param, changeType, ctx);


            for (DSRollback item : dsUpdateBet) {
                ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.REFUND.getSportPath(),
                        item,

                        (order) -> {
                            DSRollback updateBet = (DSRollback) order;
                            GameRecordsDO record = super.getRecord(updateBet.getBetId());
                            //更新下注金额
                            record.setRemark(ActionEnum.REFUND.getDesc());
                            record.setAction(ActionEnum.REFUND);
                            super.updateRecord(record);


                            GameStaticDTO.buildRefund(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getBetAmount());

                            return record.getId();
                        },
                        (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
            }

            parseObject.put("status", "0");
            parseObject.put("balance", ctx.getBalance());
            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.SETTLED) {

            List<DSSettle> dsSettles = buildSettle(param, changeType, ctx);

            if (ObjectUtil.isNotEmpty(dsSettles)) {
                List<String> ids = dsSettles.stream().map(DSSettle::getBetId).toList();

                List<GameRecordsDO> records = super.getRecords(ids);

                Map<String, List<DSSettle>> collect = dsSettles.stream().collect(Collectors.groupingBy(BaseDSRequest::getUsername));
                List<GameRecordsDO> updates = new ArrayList<>();
                for (var entry : collect.entrySet()) {
                    if (ObjectUtil.isNotEmpty(entry) && ObjectUtil.isEmpty(ctx.getPullUsername())) {
                        ctx.setPullUsername(entry.getKey());
                        bindTenantInfo(ctx);
                    }
                    entry.getValue().forEach(item -> {
                        records.stream().filter(p -> p.getOrderNo().equals(item.getBetId())).findFirst().ifPresent(record -> {


                            if (ObjectUtil.isEmpty(record)) {
                                throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                            }
                            item.setBetAmount(record.getBetAmount());
                            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.SETTLED.getSportPath(),
                                    item,

                                    (order) -> {
                                        DSSettle updateBet = (DSSettle) order;

                                        //更新下注金额
                                        record.setRemark(ActionEnum.SETTLED.getDesc());
                                        record.setAction(ActionEnum.SETTLED);
                                        record.setWinAmount(updateBet.getWinAmount());

                                        updates.add(record);

                                        GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinAmount().compareTo(record.getBetAmount()) > 0);

                                        return record.getId();
                                    },
                                    (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));

                        });
                    });
                }
                super.updateRecords(updates);
            }

            parseObject.put("balance", ctx.getBalance());
            parseObject.put("status", "0");
            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.UNSETTLE) {


            List<DSUnSettle> dsSettles = buildUnSettle(param, changeType, ctx);

            if (ObjectUtil.isNotEmpty(dsSettles)) {
                List<String> ids = dsSettles.stream().map(DSUnSettle::getBetId).toList();

                List<GameRecordsDO> records = super.getRecords(ids);
                Map<String, List<DSUnSettle>> collect = dsSettles.stream().collect(Collectors.groupingBy(BaseDSRequest::getUsername));
                List<GameRecordsDO> updates = new ArrayList<>();

                for (var entry : collect.entrySet()) {
                    if (ObjectUtil.isNotEmpty(entry.getKey()) && ObjectUtil.isEmpty(ctx.getPullUsername())) {
                        ctx.setPullUsername(entry.getKey());
                        bindTenantInfo(ctx);
                    }

                    entry.getValue().forEach(item -> {

                        records.stream().filter(p -> p.getOrderNo().equals(item.getBetId())).findFirst().ifPresent(record -> {
                            if (ObjectUtil.isEmpty(record)) {
                                throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                            }
                            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), item.getCreditAmount(), ActionEnum.UNSETTLE.getSportPath(),
                                    item,

                                    (order) -> {

                                        //更新下注金额
                                        record.setRemark(ActionEnum.UNSETTLE.getDesc());
                                        record.setAction(ActionEnum.UNSETTLE);

                                        GameStaticDTO.buildUnSettle(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinAmount().compareTo(record.getBetAmount()) > 0);

                                        record.setWinAmount(BigDecimal.ZERO);
                                        updates.add(record);
                                        return record.getId();
                                    },
                                    (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                        });


                    });

                }
                super.updateRecords(updates);

            }
            parseObject.put("balance", ctx.getBalance());
            parseObject.put("status", "0");

            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.RESETTLE) {


            List<DSReSettle> dsSettles = buildReSettle(param, changeType, ctx);

            if (ObjectUtil.isNotEmpty(dsSettles)) {
                List<String> ids = dsSettles.stream().map(DSReSettle::getBetId).toList();
                List<GameRecordsDO> records = super.getRecords(ids);
                Map<String, List<DSReSettle>> collect = dsSettles.stream().collect(Collectors.groupingBy(BaseDSRequest::getUsername));
                List<GameRecordsDO> updates = new ArrayList<>();
                for (var entry : collect.entrySet()) {
                    if (ObjectUtil.isNotEmpty(entry.getKey()) && ObjectUtil.isEmpty(ctx.getPullUsername())) {
                        ctx.setPullUsername(entry.getKey());
                        bindTenantInfo(ctx);
                    }
                    entry.getValue().forEach(item -> {

                        records.stream().filter(p -> p.getOrderNo().equals(item.getBetId())).findFirst().ifPresent(record -> {

                            if (ObjectUtil.isEmpty(record)) {
                                throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                            }
                            record.setWinAmount(item.getNewWinAmount());
                            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), NumberUtil.sub(item.getCreditAmount(), item.getCreditAmount()), ActionEnum.RESETTLE.getSportPath(),
                                    item,

                                    (order) -> {

                                        //更新下注金额
                                        record.setRemark(ActionEnum.RESETTLE.getDesc());
                                        record.setAction(ActionEnum.RESETTLE);


                                        GameStaticDTO.buildPayout(ctx.getUser(), ctx.getGameVendor(), gameInfoDO, record.getWinAmount(), record.getWinLoss(), record.getWinLoss().doubleValue() > 0);
                                        updates.add(record);
                                        return record.getId();
                                    },
                                    (amt) -> super.operateBalance(amt, item.getUsername(), ctx.getWrapCurrency()));
                        });
                    });
                }
                super.updateRecords(updates);
            }


            parseObject.put("balance", ctx.getBalance());
            parseObject.put("status", "0");
            return PlatformResponse.success(parseObject);
        } else if (changeType == ActionEnum.ADJUSTMENT) {
            DSAdjustment adjustment = buildAdjustment(param, changeType, ctx);

            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), adjustment.getAmount(), ActionEnum.ADJUSTMENT.getSportPath(),
                    adjustment,

                    (order) -> null,
                    (amt) -> super.operateBalance(amt, adjustment.getUsername(), ctx.getWrapCurrency()));

            parseObject.put("balance", ctx.getBalance());
            parseObject.put("status", "0");
            return PlatformResponse.success(parseObject);
        }


        parseObject.put("status", "0");
        parseObject.put("refId", ctx.getOrderId());
        return PlatformResponse.success(parseObject);

    }

    @Override
    public PlatformResponse<Object> doBalance(ActionEnum changeType, PlatformRequest param, SportContext ctx) {

        if (ctx.getTenant().getImplType().equals(0)) {

            DSBalance dsBalance = new DSBalance();

            dsBalance.setUsername(ctx.getPushUsername());

            ExecuteResult execute = super.execute(ctx, ctx.getTenant(), BigDecimal.ZERO, ActionEnum.BALANCE.getSportPath(),
                    dsBalance,

                    (order) -> null,
                    (amt) -> {
                        TenantUtils.execute(ctx.getTenant().getId(), () -> super.asyncBalance(ctx.getBalance(), ctx.getPushUsername(), ctx.getWrapCurrency()));
                    });
        }

        return buildBalanceResult(ctx);
    }

    private void bindTenantInfo(SportContext ctx) {
        VendorUserDO user = getUser(ctx.getPullUsername(),ctx.getGameVendor().getVendorCode());
        if (ObjectUtil.isEmpty(user)) {
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        ctx.setUser(user);
        ctx.setPushUsername(user.getUsername());
        TenantUtils.execute(user.getTenantId(), () -> {

            var vendorTenantDO = vendorTenantService.getVendorByCode(ctx.getVendorCode());
            if (ObjectUtil.isEmpty(vendorTenantDO)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            if (ObjectUtil.isNotEmpty(vendorTenantDO.getCurrency())) {
                if (!vendorTenantDO.getCurrency().contains(ctx.getWrapCurrency())) {
                    throw new GameException(PlatformCodeEnum.VENDOR_CURRENCY_NOT_SUPPORTED);
                }
            }
            ctx.setVendorTenant(vendorTenantDO);
            var tenant = tenantMapper.selectById(user.getTenantId());
            if (ObjectUtil.isEmpty(tenant)) {
                throw new GameException(PlatformCodeEnum.TENANT_ERROR);
            }
            ctx.setTenant(tenant);
            VendorUserBalanceDO balance = balanceService.getBalance(ctx.getPushUsername(), ctx.getWrapCurrency());
            if (ObjectUtil.isEmpty(balance)) {
                throw new GameException(PlatformCodeEnum.WRONG_CURRENCY);
            }
            ctx.setBalance(balance.getBalance());
        });

    }

    @Override
    protected SportContext buildContext(PlatformRequest param) {
        SportContext ctx = new SportContext();
        String traceId = MDC.get("traceId");
        ctx.setTransferId(param.getExtraData().getOrDefault("operationId", "").toString());
        ctx.setPullUsername(param.getExtraData().getOrDefault("userId", "").toString());
        ctx.setOrderId(param.getExtraData().getOrDefault("refId", "").toString());
        ctx.setCurrency(param.getExtraData().getOrDefault("currency", "").toString());
        ctx.setRoundId(param.getExtraData().getOrDefault("roundId", "").toString());
        ctx.setTxns(JsonUtils.toJsonString(param.getExtraData().getOrDefault("txns", "[]")));
        ctx.setTimestamp(Instant.now().toEpochMilli());
        ctx.setTraceId(traceId);
        return ctx;
    }

    @Override
    protected void downCurrency(SportContext ctx) {
        CurrencyEnum currencyEnum = CurrencyEnum.getSaBa(Convert.toInt(ctx.getCurrency()));
        if (ObjectUtil.isNotEmpty(currencyEnum)) {
            ctx.setWrapCurrency(currencyEnum.getCode());
        }
        //配置货币比例
        ctx.setCurrencyRate(currencyEnum.getUnit().doubleValue());
    }

    @Override
    protected String upCurrency(String currency) {
        return CurrencyEnum.fromCode(currency).getSaba().toString();
    }

    @Override
    protected PlatformResponse<Object> buildBalanceResult(SportContext ctx) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("status", "0");
        parseObject.put("userId", ctx.getPullUsername());
        parseObject.put("balance", ctx.getBalance().multiply(BigDecimal.valueOf(ctx.getCurrencyRate())));
        parseObject.put("balanceTs", LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.of("-04:00")).toLocalDateTime());

        return PlatformResponse.success(parseObject);
    }

    @Override
    protected PlatformResponse<Object> buildIdleResult(SportContext ctx) {
        JSONObject parseObject = new JSONObject();
        parseObject.put("status", "1");
        parseObject.put("message", PlatformCodeEnum.DUPLICATE_REQUEST);
        return PlatformResponse.success(parseObject);
    }


    @Override
    public PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request) {
        String memberId = vendor.getVendorChannel() + "_" + request.getUsername();
        Map<String, Object> params = new HashMap<>();
        params.put("vendor_id", vendor.getVendorAgent());
        params.put("vendor_member_id", memberId);
        params.put("operatorId", vendor.getVendorChannel());
        params.put("username", memberId);
        /**
         * 0
         * 特殊盘口,不属于任何一种市场盘口(仅出现于 sport_type=157, 168, 245 或
         * bet_type=468, 469, 8700 或游戏组别為 Casino 及 Live Casino)
         * 1 Malay Odds(马来盘)
         * 2 China Odds(中国盘)
         * 3 Decimal Odds(欧洲盘)
         * 4 Indo Odds(印度尼西亚盘)
         * 5 American Odds(美国盘)
         */
        params.put("oddstype", 1);
        /**
         * 121 PKR 巴基斯坦卢比 1:1
         */
//        CurrencyEnum currencyEnum = CurrencyEnum.fromCode(request.getCurrency());
        params.put("currency", request.getCurrency());
        params.put("maxtransfer", 9999999999L);
        params.put("mintransfer", 0L);

        JSONObject result = this.doRequest(vendor.getApiUrl() + "/api/CreateMember", params, gameCode() + "创建会员账号", "", false);

        String success = result.getString("error_code");
        if (!success.equals("0")) {
            return PlatformResponse.error(result.getString("message"));
        }
        return PlatformResponse.success(memberId);
    }


    @Override
    public PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request) {


        Map<String, Object> params = new HashMap<>();
        params.put("vendor_id", vendor.getVendorAgent());
        params.put("vendor_member_id", request.getUsername());
        params.put("platform", request.getPlatform().equals("web") ? 1 : 2);

        JSONObject result = doRequest(vendor.getApiUrl() + "/api/GetSabaUrl", params, gameCode() + "获取游戏地址", "", false);

        String success = result.getString("error_code");
        if (!success.equals("0")) {
            return PlatformResponse.error(result.getString("message"));
        }
        return PlatformResponse.success(result.getString("Data"));
    }

    @Override
    public PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account) {
        Map<String, Object> params = new HashMap<>();
        params.put("vendor_id", vendor.getVendorAgent());
        params.put("vendor_member_id", account);
        super.doRequest(vendor.getApiUrl() + "/api/KickUser", params, gameCode() + "退出用户", "", true);
        return PlatformResponse.success(BigDecimal.ZERO);
    }


    @Override
    public PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer) {
        Map<String, Object> params = new HashMap<>();
        params.put("vendor_id", vendor.getVendorAgent());
        params.put("vendor_member_id", transfer.getUsername());
        params.put("vendor_trans_id", IdUtil.nanoId());
        params.put("amount", transfer.getAmount());
        params.put("currency", transfer.getCurrency());
        params.put("direction", transfer.getDirection());
        params.put("wallet_id", 1);
        JSONObject jsonObject = super.doRequest(vendor.getApiUrl() + "/api/FundTransfer", params, gameCode() + "退出用户", "", true);
        BigDecimal balance = jsonObject.getJSONObject("Data").getBigDecimal("after_amount");
        return PlatformResponse.success(balance);
    }

    @Override
    public String buildSign(Map<String, Object> dataMap, String key) {
        return "";
    }

    @Override
    public List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime) {
        return List.of();
    }


    @Override
    protected DSBetRequest buildBet(PlatformRequest param, SportContext context) {
        double amount = Convert.toDouble(param.getExtraData().getOrDefault("betAmount", "0")) * context.getCurrencyRate();

        Integer oddsType = Convert.toInt(param.getExtraData().getOrDefault("oddsType", "999"));
        Double odds = Convert.toDouble(param.getExtraData().getOrDefault("odds", "0.0"));

        String username = param.getExtraData().getOrDefault("userId", "").toString();
        String orderId = param.getExtraData().getOrDefault("refId", "").toString();
        //String txns = context.getTxns();
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();
        //投注内容
        JSONArray legs = new JSONArray();
        JSONObject leg = new JSONObject();
        leg.put("homeName", param.getExtraData().getOrDefault("homeName_en", ""));
        leg.put("awayName", param.getExtraData().getOrDefault("awayName_en", ""));
        leg.put("league", param.getExtraData().getOrDefault("leagueName_en", ""));
        leg.put("betChoice", param.getExtraData().getOrDefault("betChoice", ""));
        leg.put("betType", param.getExtraData().getOrDefault("betTypeName", ""));
        leg.put("odds", odds);
        leg.put("kickOffTime", param.getExtraData().getOrDefault("kickOffTime", ""));
        leg.put("point", param.getExtraData().getOrDefault("point", ""));
        legs.add(leg);
        JSONObject betContentObj = new JSONObject();
        betContentObj.put("parlayType", "Single Bet");
        betContentObj.put("legs", legs);
        betContentObj.put("totalOdds", odds);

        //存储下注信息
        DSBetRequest DSBetRequest = new DSBetRequest();
        DSBetRequest.setUsername(username);
        DSBetRequest.setTransactionId(transferId);
        DSBetRequest.setExternalTransactionId(orderId);
        DSBetRequest.setBetId(orderId);
        DSBetRequest.setBetAmount(BigDecimal.valueOf(amount));
        DSBetRequest.setGameCode(gameCode());
        DSBetRequest.setBetType(1);
        DSBetRequest.setOddsType(String.valueOf(oddsType));
        DSBetRequest.setOdds(BigDecimal.valueOf(odds));
        DSBetRequest.setRoundId(IdUtil.nanoId());
        DSBetRequest.setBetContent(betContentObj.toJSONString());
        return DSBetRequest;
    }

    @Override
    protected DSBetRequest buildBetPar(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String txns = context.getTxns();
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();
        String username = param.getExtraData().getOrDefault("userId", "").toString();

        BigDecimal totalBetAmount = Convert.toBigDecimal(param.getExtraData().getOrDefault("totalBetAmount", "0"));


        DSBetRequest dsBalance = new DSBetRequest();


        dsBalance.setUsername(username);
        dsBalance.setTransactionId(transferId);
        dsBalance.setExternalTransactionId(transferId);
        dsBalance.setBetId(transferId);
        dsBalance.setBetAmount(totalBetAmount);
        dsBalance.setGameCode(gameCode());
        dsBalance.setBetType(2);
        dsBalance.setCreditAmount(totalBetAmount.negate());
        dsBalance.setTimestamp(context.getTimestamp());
        dsBalance.setRoundId(IdUtil.nanoId());

        Integer oddsType = 999;
        Double odds = 0.0;
        //解析odds
        String ticketDetail = JsonUtils.toJsonString(param.getExtraData().getOrDefault("ticketDetail", "[]"));
        Map<Long, JSONObject> matchMap = new HashMap<>();

        if (ObjectUtil.isNotEmpty(ticketDetail)) {
            JSONArray objects = JSONObject.parseArray(ticketDetail);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                odds += item.getDouble("odds");
                oddsType = item.getInteger("oddsType");
                //获取 matchId
                Long matchId = item.getLong("matchId");
                if (matchId != null) {
                    matchMap.put(matchId, item);
                }
            }
        }

        dsBalance.setOddsType(oddsType.toString());
        dsBalance.setOdds(BigDecimal.valueOf(odds));

        List<DSBetRequest.MultipleItem> result = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(txns)) {
            JSONArray objects = JSONObject.parseArray(txns);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                //组装并且获取对应的投注内容
                JSONObject showData = buildShowAndIssue(item, matchMap);
                String orderId = item.getString("refId");
                double newBetAmount = item.getDouble("betAmount") * context.getCurrencyRate();
                double creditAmount = item.getDouble("creditAmount") * context.getCurrencyRate();
                double debitAmount = item.getDouble("debitAmount") * context.getCurrencyRate();


                JSONArray detailArr = item.getJSONArray("detail");
                double totalOdds = 0.0;
                if (detailArr != null && !detailArr.isEmpty()) {
                    JSONObject d = detailArr.getJSONObject(0);
                    if (d.get("odds") != null) {
                        totalOdds = d.getDouble("odds");
                    }
                }
                //投注内容
                JSONObject betContentObj = new JSONObject();
                betContentObj.put("refId", orderId);
                betContentObj.put("parlayType", showData.getString("parlayLabel"));
                betContentObj.put("legs", showData.getJSONArray("legs"));
                betContentObj.put("totalOdds",(totalOdds == 0.0d) ? odds : totalOdds);
                // 投注金额有变更
                double transferAmount = creditAmount - debitAmount;
                DSBetRequest.MultipleItem multipleItem = new DSBetRequest.MultipleItem();
                multipleItem.setBetId(orderId);
                multipleItem.setBetAmount(BigDecimal.valueOf(newBetAmount));
                multipleItem.setBetContent(betContentObj.toJSONString());
                result.add(multipleItem);
            }
        }
        dsBalance.setMultipleBetIds(result);
        return dsBalance;
    }

    @Override
    protected List<DSUpdateBet> buildConfirm(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String txns = context.getTxns();
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();
        String username = param.getExtraData().getOrDefault("userId", "").toString();

        List<DSUpdateBet> result = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(txns)) {
            JSONArray objects = JSONObject.parseArray(txns);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                String orderId = item.getString("refId");
                double newBetAmount = item.getDouble("actualAmount") * context.getCurrencyRate();
                double creditAmount = item.getDouble("creditAmount") * context.getCurrencyRate();
                double debitAmount = item.getDouble("debitAmount") * context.getCurrencyRate();
                Integer oddsType = Convert.toInt(param.getExtraData().getOrDefault("oddsType", "999"));
                BigDecimal odds = Convert.toBigDecimal(param.getExtraData().getOrDefault("odds", "0"));

                // 投注金额有变更
                double transferAmount = creditAmount - debitAmount;


                GameRecordsDO record = super.getRecord(orderId);
                if (ObjectUtil.isEmpty(record)) {
                    throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                }

                DSUpdateBet dsBalance = new DSUpdateBet();


                dsBalance.setUsername(username);
                dsBalance.setTransactionId(transferId);
                dsBalance.setExternalTransactionId(orderId);
                dsBalance.setBetId(orderId);
                dsBalance.setBetAmount(record.getBetAmount());
                dsBalance.setGameCode(gameCode());
                dsBalance.setNewBetAmount(BigDecimal.valueOf(newBetAmount));
                dsBalance.setCreditAmount(BigDecimal.valueOf(transferAmount));
                dsBalance.setOddsType(String.valueOf(oddsType));
                dsBalance.setOdds(odds);
                dsBalance.setTimestamp(context.getTimestamp());
                result.add(dsBalance);

            }
        }
        return result;
    }

    @Override
    protected List<DSRollback> buildRefund(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String txns = context.getTxns();


        List<DSRollback> result = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(txns)) {
            JSONArray objects = JSONObject.parseArray(txns);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                String orderId = item.getString("refId");
                GameRecordsDO record = super.getRecord(orderId);
                if (ObjectUtil.isEmpty(record)) {
                    throw new GameException(PlatformCodeEnum.TRANSACTION_NOT_EXISTS);
                }
                double creditAmount = item.getDouble("creditAmount") * context.getCurrencyRate();
                double debitAmount = item.getDouble("debitAmount") * context.getCurrencyRate();
                // 投注金额有变更
                double transferAmount = creditAmount - debitAmount;


                DSRollback dsBalance = new DSRollback();
                dsBalance.setTraceId(IdUtil.fastUUID());
                dsBalance.setTimestamp(System.currentTimeMillis());
                dsBalance.setUsername(context.getPushUsername());
                dsBalance.setTransactionId(context.getTransferId());
                dsBalance.setExternalTransactionId(orderId);
                dsBalance.setBetId(orderId);
                dsBalance.setCreditAmount(BigDecimal.valueOf(transferAmount));
                dsBalance.setGameCode(gameCode());
                result.add(dsBalance);
            }
        }
        return result;
    }

    @Override
    protected List<DSSettle> buildSettle(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String txns = context.getTxns();
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();

        List<DSSettle> result = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(txns)) {
            JSONArray objects = JSONObject.parseArray(txns);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                String orderId = item.getString("refId");
                String username = item.getString("userId");


                GameRecordsDO record = super.getRecord(orderId);
                double creditAmount = item.getDouble("creditAmount") * context.getCurrencyRate();
                double debitAmount = item.getDouble("debitAmount") * context.getCurrencyRate();


                double payout = item.getDouble("payout") * context.getCurrencyRate();
                // 投注金额有变更
                double transferAmount = creditAmount - debitAmount;

                DSSettle dsBalance = new DSSettle();
                dsBalance.setTraceId(IdUtil.fastUUID());
                dsBalance.setTimestamp(System.currentTimeMillis());
                dsBalance.setUsername(username);
                dsBalance.setTransactionId(transferId);
                dsBalance.setExternalTransactionId(orderId);
                dsBalance.setBetId(orderId);
                dsBalance.setGameCode(gameCode());
                dsBalance.setWinAmount(BigDecimal.valueOf(payout));

                dsBalance.setEffectiveTurnover(record.getBetAmount());
                dsBalance.setCreditAmount(BigDecimal.valueOf(transferAmount));


                result.add(dsBalance);
            }
        }
        return result;
    }

    @Override
    protected List<DSUnSettle> buildUnSettle(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String txns = context.getTxns();
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();

        List<DSUnSettle> result = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(txns)) {
            JSONArray objects = JSONObject.parseArray(txns);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                String orderId = item.getString("refId");
                String username = item.getString("userId");

                double creditAmount = item.getDouble("creditAmount") * context.getCurrencyRate();
                double debitAmount = item.getDouble("debitAmount") * context.getCurrencyRate();


                // 投注金额有变更
                double transferAmount = creditAmount - debitAmount;

                DSUnSettle dsBalance = new DSUnSettle();
                dsBalance.setTraceId(IdUtil.fastUUID());
                dsBalance.setTimestamp(System.currentTimeMillis());
                dsBalance.setUsername(username);
                dsBalance.setTransactionId(transferId);
                dsBalance.setExternalTransactionId(orderId);
                dsBalance.setBetId(orderId);
                dsBalance.setGameCode(gameCode());
                dsBalance.setCreditAmount(BigDecimal.valueOf(transferAmount));

                result.add(dsBalance);
            }
        }
        return result;
    }

    @Override
    protected List<DSReSettle> buildReSettle(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String txns = context.getTxns();
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();

        List<DSReSettle> result = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(txns)) {
            JSONArray objects = JSONObject.parseArray(txns);
            for (int i = 0; i < objects.size(); i++) {
                JSONObject item = objects.getJSONObject(i);
                String orderId = item.getString("refId");
                String username = item.getString("userId");

                double creditAmount = item.getDouble("creditAmount") * context.getCurrencyRate();
                double debitAmount = item.getDouble("debitAmount") * context.getCurrencyRate();
                double payout = item.getDouble("payout") * context.getCurrencyRate();

                DSReSettle reSettle = new DSReSettle();
                reSettle.setTraceId(IdUtil.fastUUID());
                reSettle.setTimestamp(System.currentTimeMillis());
                reSettle.setUsername(username);
                reSettle.setTransactionId(transferId);
                reSettle.setExternalTransactionId(orderId);
                reSettle.setBetId(orderId);
                reSettle.setGameCode(gameCode());
                reSettle.setWinAmount(BigDecimal.valueOf(payout));
                reSettle.setNewWinAmount(reSettle.getWinAmount());
                reSettle.setCreditAmount(BigDecimal.valueOf(creditAmount));

                result.add(reSettle);
            }
        }
        return result;
    }

    @Override
    protected DSAdjustment buildAdjustment(PlatformRequest param, ActionEnum actionEnum, SportContext context) {
        String transferId = param.getExtraData().getOrDefault("operationId", "").toString();
        String username = param.getExtraData().getOrDefault("userId", "").toString();
        String orderId = param.getExtraData().getOrDefault("refId", "").toString();
        Object bo = param.getExtraData().getOrDefault("balanceInfo", new HashMap<>());
        if (ObjectUtil.isNotEmpty(bo)) {
            Map<String, Double> balanceInfo = Convert.toMap(String.class, Double.class, bo);
            if (ObjectUtil.isNotEmpty(balanceInfo)) {
                double creditAmount = balanceInfo.getOrDefault("creditAmount", 0.0d) * context.getCurrencyRate();
                double debitAmount = balanceInfo.getOrDefault("debitAmount", 0.0d) * context.getCurrencyRate();

                double transferAmount = creditAmount - debitAmount;


                DSAdjustment dsBalance = new DSAdjustment();
                dsBalance.setTraceId(IdUtil.fastUUID());
                dsBalance.setTimestamp(System.currentTimeMillis());
                dsBalance.setUsername(username);
                dsBalance.setTransactionId(transferId);
                dsBalance.setExternalTransactionId(orderId);
                dsBalance.setAmount(BigDecimal.valueOf(transferAmount));
                dsBalance.setGameCode("SABA_2");
                dsBalance.setRoundId(IdUtil.nanoId());

                return dsBalance;
            }
        }
        return null;

    }

    private JSONObject buildShowAndIssue(JSONObject txn, Map<Long, JSONObject> matchMap) {

        JSONObject result = new JSONObject();

        JSONArray legs = new JSONArray();
        JSONArray issueNoArr = new JSONArray();

        String parlayType = txn.getString("parlayType");
        if (parlayType == null) parlayType = "";

        JSONArray detailArr = txn.getJSONArray("detail");
        if (detailArr == null) detailArr = new JSONArray();

        String parlayLabel = "Parlay"; // 默认串关兜底

        // ===== 先识别 detail.name=====
        String detailName = "";
        if (!detailArr.isEmpty()) {
            JSONObject d0 = detailArr.getJSONObject(0);
            detailName = d0.getString("name");
        }

        // =====判断类型 =====
        boolean isSingle = parlayType.contains("Single");
        boolean isBetBuilder = parlayType.toLowerCase().contains("builder");
        boolean isLucky = parlayType.contains("Lucky") || detailName.startsWith("Lucky");
        boolean isSystem = isSystemType(detailName);

        // =====计算 parlayLabel =====
        if (isSingle) {
            parlayLabel = "Single";
        } else if (isBetBuilder) {
            parlayLabel = "Bet Builder";
        } else if (detailName != null && !detailName.isEmpty()) {
            parlayLabel = SabaParlayTypeEnum.resolveEn(detailName);
        }

        // 单关 / builder：用 matchId 精确匹配 构建 legs（核心逻辑）
        if (isSingle || isBetBuilder) {
            for (int i = 0; i < detailArr.size(); i++) {
                JSONObject d = detailArr.getJSONObject(i);
                Long matchId = d.getLong("matchId");

                if (matchId == null) continue;

                JSONObject t = matchMap.get(matchId);

                if (t == null) continue;

                legs.add(buildLeg(t));
                issueNoArr.add(t.getString("homeName_en") + " VS " + t.getString("awayName_en"));
            }
        }

        // 串关/ Lucky / System
        else {

            for (JSONObject t : matchMap.values()) {
                legs.add(buildLeg(t));
                issueNoArr.add(t.getString("homeName_en") + " VS " + t.getString("awayName_en"));
            }
        }

        result.put("parlayLabel", parlayLabel);
        result.put("legs", legs);
        result.put("issueNo", issueNoArr);
        return result;
    }

    private boolean isSystemType(String name) {
        if (name == null) return false;

        return name.equalsIgnoreCase("Trixie")
                || name.equalsIgnoreCase("Yankee")
                || name.equalsIgnoreCase("Canadian")
                || name.equalsIgnoreCase("Heinz")
                || name.equalsIgnoreCase("Goliath")
                || name.startsWith("Lucky");
    }

    private JSONObject buildLeg(JSONObject t) {
        JSONObject leg = new JSONObject();

        leg.put("point", t.getString("point"));
        leg.put("homeName", t.getString("homeName_en"));
        leg.put("awayName", t.getString("awayName_en"));
        leg.put("league", t.getString("leagueName_en"));
        leg.put("betChoice", t.getString("betChoice"));
        leg.put("betType", t.getString("betTypeName"));
        leg.put("odds", t.getDouble("odds"));
        leg.put("kickOffTime", t.getString("kickOffTime"));
        return leg;
    }
}