package cn.iocoder.yudao.module.game.adapter;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.mq.kafka.StatProducer;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.game.service.IBalanceService;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameIdleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.records.IdleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.records.RecordsMapper;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.enums.LanguageEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.*;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
import cn.iocoder.yudao.module.system.service.info.GameInfoService;
import cn.iocoder.yudao.module.system.service.vendoruser.VendorUserService;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public abstract class BaseAbstractPlatform implements IPlatform {

    @Resource
    protected StatProducer statProducer;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected VendorUserService userService;
    @Autowired
    protected RecordsMapper recordsMapper;

    @Resource
    private DSManager DSManager;

    @Resource
    protected IdleMapper idleMapper;

    @Resource
    protected GameInfoService gameInfoService;


    protected abstract SportContext buildContext(PlatformRequest param);

    /**
     * 设置下发的货币
     *
     * @param ctx
     */
    protected abstract void downCurrency(SportContext ctx);

    /**
     * 设置上游货币
     *
     * @param currency
     * @return
     */
    protected abstract String upCurrency(String currency);


    // ================== 模板方法 ==================
    @Override
    public PlatformResponse<Object> balance(ActionEnum changeType, PlatformRequest param) {

        SportContext ctx = buildContext(param);
        downCurrency(ctx);
        return TenantUtils.executeIgnore(() -> doBalance(changeType, param, ctx));
    }


    @Override
    public PlatformResponse<Object> forward(ActionEnum changeType, PlatformRequest param) {

        SportContext ctx = buildContext(param);
        downCurrency(ctx);
        gameInfoService.reportOnlineStatus(ctx.getGameCode(), ctx.getPullUsername(), 0L);
        return TenantUtils.executeIgnore(() -> doForward(changeType, param, ctx));
    }

    public abstract PlatformResponse<Object> doForward(
            ActionEnum changeType,
            PlatformRequest param, SportContext ctx);

    public abstract PlatformResponse<Object> doBalance(
            ActionEnum changeType,
            PlatformRequest param, SportContext ctx);

    // ================== 通用能力 ==================

    protected VendorUserDO getUser(String username,String vendorCode) {
        return userService.getUserByVendor(username,vendorCode);
    }

    protected void operateBalance(BigDecimal amount, String username, String currency) {

        balanceService.updateBalance(
                username, amount, currency
        );
    }

    protected GameRecordsDO getRecord(String orderId) {
        return recordsMapper.selectOne("order_no", orderId);
    }

    protected List<GameRecordsDO> getRecords(List<String> orderId) {
        return recordsMapper.selectList("order_no", orderId);
    }

    protected GameRecordsDO getRecordByRoundId(String roundId) {
        return recordsMapper.selectOne("issue_no", roundId);
    }

    protected boolean idleRecord(String transactionId, Object data) {
        GameIdleDO gameIdleDO = new GameIdleDO();
        gameIdleDO.setTransactionId(transactionId);
        gameIdleDO.setContent(JSONObject.toJSONString(data));
        return idleMapper.insertIgnore(gameIdleDO) > 0;
    }

    protected Long saveRecord(SportContext ctx, Long type, String transNo, String orderId, String roundId, ActionEnum status,
                              Double betAmount, String username, String betContent, BigDecimal odds, String gameCode, Long tenantId
    ) {


        GameRecordsDO game = new GameRecordsDO();
        Long id = IdUtil.getSnowflakeNextId();
        game.setId(id);
        game.setGameCode(gameCode);
        game.setUsername(username);
        game.setRefOrderNo(transNo);
        game.setOrderNo(orderId);
        game.setBetContent(betContent);
        game.setIssueNo(roundId);
        game.setAction(status);
        game.setGameKind(type);
        game.setBetAmount(BigDecimal.valueOf(betAmount));
        game.setWinAmount(BigDecimal.ZERO);
        game.setRemark(status.getDesc());
        game.setOdds(odds);
        game.setCreateTime(LocalDateTime.now());
        game.setTenantId(tenantId);
        game.setVendorCode(ctx.getVendorCode());
        recordsMapper.insert(game);

        return id;
    }

    protected void asyncBalance(BigDecimal amount, String username, String currencyCode) {

        balanceService.asyncBalance(username, amount, currencyCode);
    }

    protected void updateRecord(GameRecordsDO record) {
        recordsMapper.updateById(record);
    }

    protected void updateRecords(List<GameRecordsDO> record) {
        recordsMapper.updateById(record);
    }

    /**
     * 核心模板方法：适用于所有 Action (BET, SETTLE, CANCEL等)
     * * @param requestDownstream 下游推送的具体逻辑 (Lambda)
     *
     * @param context
     * @param localAction 本地数据库/余额的操作逻辑 (Lambda)
     * @return 最终处理后的余额
     */
    public ExecuteResult execute(SportContext context, TenantDO tenant, BigDecimal transferAmount, String path,
                                 Object dsRequest,
                                 RecordAction orderAction,
                                 TransactionAction localAction) {
        DownstreamResult result = DownstreamResult.buildEmpty();
        if (tenant.getImplType().equals(0)) {
            // 2. 推送下游
            result = DSManager.push(context, tenant, path, dsRequest);
            if (!result.getStatus().equals("OK")) {
                throw new GameException(result.getStatus());
            }
        }

        DownstreamResult finalResult = result;
        return TenantUtils.execute(tenant.getId(), () -> {
            // 创建注单
            Long orderId = orderAction.doOperate(dsRequest);

            // 3. 执行本地账变（operateBalance）并返回最新余额
            localAction.doOperate(transferAmount);

            return new ExecuteResult(orderId, finalResult.getData().getBalance());
        });

    }


    @Data
    @AllArgsConstructor
    public static class ExecuteResult {
        private Long orderId;
        private BigDecimal balance;
    }

    @FunctionalInterface
    public interface TransactionAction {
        void doOperate(BigDecimal amount);
    }

    @FunctionalInterface
    public interface RecordAction {
        Long doOperate(Object order);
    }

    @Override
    public PlatformResponse<String> createMember(GameVendorDO vendor, GameApiRequest request) {
        String dsCurrency = upCurrency(request.getCurrency());
        request.setCurrency(dsCurrency);
        request.setLanguage(upLanguage(request.getLanguage()));
        return doCreateMember(vendor, request);
    }

    @Override
    public PlatformResponse<String> getGameLink(GameVendorDO vendor, GameApiRequest request) {
        String dsCurrency = upCurrency(request.getCurrency());
        request.setCurrency(dsCurrency);
        request.setLanguage(upLanguage(request.getLanguage()));
        return doGetGameLink(vendor, request);

    }

    @Override
    public PlatformResponse<BigDecimal> exitGame(GameVendorDO vendor, String account) {
        return doExitGame(vendor, account);
    }

    @Override
    public PlatformResponse<BigDecimal> transfer(GameVendorDO vendor, GameApiTransfer request) {
        String dsCurrency = upCurrency(request.getCurrency());
        request.setCurrency(dsCurrency);
        return doTransfer(vendor, request);
    }


    public abstract PlatformResponse<String> doCreateMember(GameVendorDO vendor, GameApiRequest request);

    public abstract PlatformResponse<String> doGetGameLink(GameVendorDO vendor, GameApiRequest request);


    public abstract PlatformResponse<BigDecimal> doExitGame(GameVendorDO vendor, String account);


    public abstract PlatformResponse<BigDecimal> doTransfer(GameVendorDO vendor, GameApiTransfer transfer);

    public abstract String buildSign(Map<String, Object> dataMap, String key);

    //默认返回简写，需要全称可以重写该方法
    public  String  upLanguage(String language){
        return LanguageEnum.findByCode(language).getShortCode();
    }

    // 游戏用户名命名 可以根据厂商规则设定
    protected String nameFormat(String username) {
        return username;
    }


}