package cn.iocoder.yudao.gateway.controller;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import cn.iocoder.yudao.framework.common.model.game.*;
import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.mq.kafka.StatProducer;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.module.system.model.*;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.vo.RecordsPageReqVO;
import cn.iocoder.yudao.module.system.service.GameCategoryService;
import cn.iocoder.yudao.module.system.service.GameInfoService;
import cn.iocoder.yudao.module.system.service.GameRecordsService;
import cn.iocoder.yudao.module.system.service.VendorTenantService;
import cn.iocoder.yudao.module.system.service.VendorUserService;
import cn.iocoder.yudao.module.game.adapter.GamingPlatformFactory;
import cn.iocoder.yudao.module.game.adapter.IPlatform;
import cn.iocoder.yudao.module.game.convert.GameRecordConvert;
import cn.iocoder.yudao.module.game.service.IBalanceService;

import cn.iocoder.yudao.module.system.model.game.response.BalanceFlowRespVO;
import cn.iocoder.yudao.module.system.model.game.response.BalanceRespVO;
import cn.iocoder.yudao.module.system.model.game.response.CategoryRespVO;
import cn.iocoder.yudao.module.system.model.game.response.GameRespVO;
import cn.iocoder.yudao.module.system.model.game.response.VendorRespVO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fhs.core.trans.anno.TransMethodResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.GATEWAY;

/**
 * 下游对接-api接口
 */
@RestController
@Slf4j
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class ApiController {

    private final VendorTenantService vendorTenantService;

    private final cn.iocoder.yudao.module.system.service.VendorService vendorService;
    private final GamingPlatformFactory gamingPlatformFactory;
    private final GameInfoService gameInfoService;
    private final VendorUserService vendorUserService;
    private final cn.iocoder.yudao.module.system.service.TenantService tenantService;
    private final GameRecordsService gameRecordsService;
    private final GameRecordConvert convert;
    private final IBalanceService balanceService;
    private final GameCategoryService gameCategoryService;


    protected final StatProducer statProducer;

    /**
     * 获取游戏链接
     *
     */
    @PostMapping("/game/url")
    @ApiAccessLog(operateType = GATEWAY, operateName = "获取游戏链接")
    public PlatformResponse<String> gameUrl(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {

        log.info("获取游戏链接进来了: key={}", key);
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        //4. 验证下游是否开通当前货币
        GameApiRequest body = JSONObject.parseObject(rawJson, GameApiRequest.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        return TenantUtils.execute(currentTenant.getId(), () -> {


            if (currentTenant.getImplType().equals(1)) {
                if (ObjectUtil.isEmpty(body.getBalance())) {
                    throw new GameException(PlatformCodeEnum.INVALID_BALANCE);
                }
            }
            //5. 查询游戏数据
            GameInfoDO game = gameInfoService.getInfoByCode(body.getGameCode());
            if (ObjectUtil.isEmpty(game)) {
                throw new GameException(PlatformCodeEnum.INVALID_GAME);
            }
            //6. 验证下游是否开通当前厂商和游戏
            if (CollectionUtils.isNotEmpty(currentTenant.getVendorCodes())
                    && !currentTenant.getVendorCodes().contains(game.getVendorCode())) {
                throw new GameException(PlatformCodeEnum.VENDOR_PLATFORM_NOT_SUPPORTED);
            }
            //7. 验证游戏状态
            if (!CommonStatusEnum.isEnable(game.getStatus())) {
                throw new GameException(PlatformCodeEnum.GAME_DISABLED);
            }
            if (!CommonStatusEnum.isEnable(game.getMaintain())) {
                throw new GameException(PlatformCodeEnum.UNDER_MAINTENANCE);
            }
            //8. 验证游戏支持的货币
            if (ObjectUtil.isNotEmpty(game.getSupportCurrency())) {
                if (!game.getSupportCurrency().contains(body.getCurrency())) {
                    throw new GameException(PlatformCodeEnum.GAME_CURRENCY_NOT_SUPPORTED);
                }
            }

            //9. 验证上游厂商
            GameVendorDO gameVendor = vendorService.getVendorByCode(game.getVendorCode());
            if (ObjectUtil.isEmpty(gameVendor)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }



            IPlatform bean = gamingPlatformFactory.getPlatform(gameVendor.getAdapter());
            if (ObjectUtil.isEmpty(bean)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }

            String currency = body.getCurrency();
            if (ObjectUtil.isNotEmpty(gameVendor.getCurrency())) {
                if (!gameVendor.getCurrency().contains(currency)) {
                    throw new GameException(PlatformCodeEnum.GAME_CURRENCY_NOT_SUPPORTED);
                }
            }
            // 多货币厂商适配
            if(ObjectUtil.isNotEmpty(gameVendor.getSubVendors())){
               gameVendor= vendorService.getSubAgent(gameVendor.getVendorCode(),currency);
            }

            String currentName = currentTenant.getId() + "_" + body.getUsername();
            //10. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(currentName, game.getVendorCode());
            if (ObjectUtil.isEmpty(gameUser)) {
                PlatformResponse<String> response = bean.createMember(gameVendor, body);
                gameUser = vendorUserService.makeUser(currency, gameVendor, currentTenant,currentName, response.getData());

            }
            // 11重构用户名已平台定义为准
            body.setUsername(gameUser.getVendorUsername());
            GameStaticDTO.buildOpen(gameUser, gameVendor, game,body.getCurrency());

            //12. 调用上游返回链接
            return bean.getGameLink(gameVendor, body, gameUser);

        });
    }

    /**
     * 金额转入/转出(非单一钱包)
     *
     * @param rawJson
     * @param sign
     * @param key
     * @return
     */
    @PostMapping("/wallet/transfer")
    @ApiAccessLog(operateType = GATEWAY, operateName = "金额转入/转出")
    public PlatformResponse<BigDecimal> gameFundTransFer(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiTransfer body = JSONObject.parseObject(rawJson, GameApiTransfer.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        return TenantUtils.execute(currentTenant.getId(), () -> {

            //2. 查询游戏是否开启
            GameInfoDO game = gameInfoService.getInfoByCode(body.getGameCode());
            if (ObjectUtil.isEmpty(game)) {
                throw new GameException(PlatformCodeEnum.INVALID_GAME);
            }

            if (!CommonStatusEnum.isEnable(game.getMaintain())) {
                throw new GameException(PlatformCodeEnum.GAME_DISABLED);
            }

            //3. 验证上游厂商
            GameVendorDO gameVendor = vendorService.getVendorByCode(game.getVendorCode());
            if (ObjectUtil.isEmpty(gameVendor)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            // 多货币厂商适配
            if(ObjectUtil.isNotEmpty(gameVendor.getSubVendors())){
                gameVendor= vendorService.getSubAgent(gameVendor.getVendorCode(), body.getCurrency());
            }
            IPlatform bean = gamingPlatformFactory.getPlatform(gameVendor.getAdapter());
            if (ObjectUtil.isEmpty(bean)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }

            String currentName = currentTenant.getId() + "_" + body.getUsername();

            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(currentName);
            if (ObjectUtil.isEmpty(gameUser)) {
                GameApiRequest request = BeanUtils.toBean(body, GameApiRequest.class);
                request.setUsername(currentName);
                PlatformResponse<String> response = bean.createMember(gameVendor, request);
                gameUser = vendorUserService.makeUser(body.getCurrency(), gameVendor, currentTenant, currentName, response.getData());
            }
            //5. 调用上游返回链接
            body.setUsername(gameUser.getVendorUsername());
            return bean.transfer(gameVendor, body);

        });
    }

    /**
     * 转入金额
     *
     * @param rawJson
     * @param sign
     * @param key
     * @return
     */
    @PostMapping("/cash/deposit")
    @ApiAccessLog(operateType = GATEWAY, operateName = "金额转入")
    public PlatformResponse<BalanceFlowRespVO> cashDeposit(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiCashDeposit body = JSONObject.parseObject(rawJson, GameApiCashDeposit.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        return TenantUtils.execute(currentTenant.getId(), () -> {
            String currentName = currentTenant.getId() + "_" + body.getUsername();
            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUserByCurrency(currentName, body.getCurrency());
            if (ObjectUtil.isEmpty(gameUser)) {
                gameUser = vendorUserService.makeUser(body.getCurrency(), null, currentTenant, currentName, currentName);
            }

            VendorUserFlowDO vendorUserFlowDO = balanceService.transferAmount(body.getReferenceId(), gameUser.getUsername(), body.getTransferAmount(), body.getCurrency());
            return PlatformResponse.success(convert.toFlowVo(vendorUserFlowDO));

        });
    }

    /**
     * 转出金额
     * @param rawJson
     * @param sign
     * @param key
     * @return
     */
    @PostMapping("/cash/withdraw")
    @ApiAccessLog(operateType = GATEWAY, operateName = "金额转出")
    public PlatformResponse<BalanceFlowRespVO> cashWithdraw(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiCashDeposit body = JSONObject.parseObject(rawJson, GameApiCashDeposit.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        return TenantUtils.execute(currentTenant.getId(), () -> {
            String currentName = currentTenant.getId() + "_" + body.getUsername();
            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUserByCurrency(currentName, body.getCurrency());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
            }

            VendorUserFlowDO vendorUserFlowDO = balanceService.transferAmount(body.getReferenceId(), gameUser.getUsername(), body.getTransferAmount().negate(), body.getCurrency());
            return PlatformResponse.success(convert.toFlowVo(vendorUserFlowDO));

        });
    }

    @PostMapping("/cash/getsingletransaction")
    @ApiAccessLog(operateType = GATEWAY, operateName = "查询转账状态")
    public PlatformResponse<BalanceFlowRespVO> getSingleTransaction(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiCashDetail body = JSONObject.parseObject(rawJson, GameApiCashDetail.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        return TenantUtils.execute(currentTenant.getId(), () -> {
            String currentName = currentTenant.getId() + "_" + body.getUsername();
            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUserByCurrency(currentName, body.getCurrency());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
            }

            VendorUserFlowDO vendorUserFlowDO = balanceService.getTransferFlow(body.getReferenceId(), gameUser.getUsername(), body.getCurrency());
            return PlatformResponse.success(convert.toFlowVo(vendorUserFlowDO));

        });
    }
    /**
     * 查询余额
     *
     * @param rawJson
     * @param sign
     * @param key
     * @return
     */
    @PostMapping("/cash/balance")
    @ApiAccessLog(operateType = GATEWAY, operateName = "查询余额")
    public PlatformResponse<BalanceRespVO> cashBalance(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiCashBalance body = JSONObject.parseObject(rawJson, GameApiCashBalance.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        return TenantUtils.execute(currentTenant.getId(), () -> {
            String currentName = currentTenant.getId() + "_" + body.getUsername();
            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUserByCurrency(currentName, body.getCurrency());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
            }

            VendorUserBalanceDO vendorUserFlowDO = balanceService.getBalance(gameUser.getUsername(), body.getCurrency());
            return PlatformResponse.success(convert.toBalanceVo(vendorUserFlowDO));

        });
    }

    /**
     * 配置指定游戏RTP
     * @param rawJson
     * @param sign
     * @param key
     * @return
     */
    @PostMapping("/game/rtp")
    @ApiAccessLog(operateType = GATEWAY, operateName = "指定游戏RTP")
    public PlatformResponse<Boolean> rtp(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiRtp body = JSONObject.parseObject(rawJson, GameApiRtp.class);

        return TenantUtils.execute(currentTenant.getId(), () -> {
            //2. 查询游戏是否开启
            GameInfoDO game = gameInfoService.getInfoByCode(body.getGameCode());
            if (ObjectUtil.isEmpty(game)) {
                throw new GameException(PlatformCodeEnum.INVALID_GAME);
            }

            if (!CommonStatusEnum.isEnable(game.getMaintain())) {
                throw new GameException(PlatformCodeEnum.GAME_DISABLED);
            }

            //3. 验证上游厂商
            GameVendorDO gameVendor = vendorService.getVendorByCode(game.getVendorCode());
            if (ObjectUtil.isEmpty(gameVendor)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }

            IPlatform bean = gamingPlatformFactory.getPlatform(gameVendor.getAdapter());
            if (ObjectUtil.isEmpty(bean)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            return bean.setRtp(gameVendor, body);

        });
    }


    /**
     * 退出游戏
     *
     * @param rawJson
     * @param sign
     * @param key
     * @param request
     * @return
     */
    @PostMapping("/game/terminate")
    @ApiAccessLog(operateType = GATEWAY, operateName = "退出游戏")
    public PlatformResponse<BigDecimal> gameTerminate(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        GameApiRequest body = JSONObject.parseObject(rawJson, GameApiRequest.class);

        return TenantUtils.execute(currentTenant.getId(), () -> {

            //2. 查询游戏是否开启
            GameInfoDO game = gameInfoService.getInfoByCode(body.getGameCode());
            if (ObjectUtil.isEmpty(game)) {
                throw new GameException(PlatformCodeEnum.INVALID_GAME);
            }
            if (!CommonStatusEnum.isEnable(game.getMaintain())) {
                throw new GameException(PlatformCodeEnum.GAME_DISABLED);
            }

            //3. 验证上游厂商
            GameVendorDO gameVendor = vendorService.getVendorByCode(game.getVendorCode());
            if (ObjectUtil.isEmpty(gameVendor)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            IPlatform bean = gamingPlatformFactory.getPlatform(gameVendor.getAdapter());
            if (ObjectUtil.isEmpty(bean)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }
            String currentName = currentTenant.getId() + "_" + body.getUsername();
            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(currentName);

            //5. 调用上游返回链接
            return bean.exitGame(gameVendor, gameUser.getVendorUsername());

        });
    }


    /**
     * 批量退出游戏
     *
     * @param rawJson
     * @param sign
     * @param key
     * @param request
     * @return
     */
    @PostMapping("/game/terminate/all")
    @ApiAccessLog(operateType = GATEWAY, operateName = "退出所有游戏")
    public PlatformResponse<BigDecimal> gameTerminateAll(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        JSONObject body = JSONObject.parseObject(rawJson);
        String username = body.getString("username");

        return TenantUtils.execute(currentTenant.getId(), () -> {
            //2. 查询游戏是否开启
            String currentName = currentTenant.getId() + "_" + username;
            List<VendorUserVendorDTO> vendorUserList = TenantUtils.executeIgnore(() ->vendorService.getVendorUserByUsernameAndTenantId(currentName, currentTenant.getId()));
            if (CollectionUtil.isEmpty(vendorUserList)) {
                log.error("退出厂商失败,没有查询到数据 tenantName={}, username={}",  currentTenant.getName(),username);
                throw new GameException(PlatformCodeEnum.INVALID_VENDOR);
            }


            // 有数据,退出厂商
            for (VendorUserVendorDTO vendorUser : vendorUserList) {
                try {
                    //查询实现类
                    IPlatform bean = gamingPlatformFactory.getPlatform(vendorUser.getAdapter() != null ?
                            vendorUser.getAdapter() : vendorUser.getVendorCode());
                    if (ObjectUtil.isEmpty(bean)) {
                        log.error("退出厂商异常,没有找到对应的实现 tenant={}, vendorCode={}, username={}",
                                currentTenant.getName(), vendorUser.getVendorCode(), username);
                        continue;
                    }
                    //转 GameVendorDO 对象
                    GameVendorDO gameVendor = BeanUtils.toBean(vendorUser,GameVendorDO.class);
                    bean.exitGame(gameVendor, vendorUser.getVendorUsername());

                } catch (Exception e) {
                    log.error("退出厂商异常 tenant={}, vendorCode={}, username={}",
                            currentTenant.getName(), vendorUser.getVendorCode(), currentName, e);
                }
            }
            return PlatformResponse.success();
        });
    }

    /**
     * 查询厂商
     *
     * @param rawJson
     * @param request
     * @return
     */
    @PostMapping("/game/vendors")
    public PlatformResponse<List<VendorRespVO>> gameVendors(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {
        // 校验商户信息
        TenantDO tenantGame = authenticateTenant(rawJson, sign, key);
        GameApiVendorRequest body = JSONObject.parseObject(rawJson, GameApiVendorRequest.class);
        return TenantUtils.execute(tenantGame.getId(), () -> {

            List<GameVendorDO> listVendors = vendorTenantService.getListVendors(body);
            List<VendorRespVO> vendorRespVOList = listVendors.stream().map(vendor -> {
                VendorRespVO vo = BeanUtils.toBean(vendor, VendorRespVO.class);
                vo.setCategoryCode(vendor.getCategoryCode());
                return vo;
            }).toList();
            return PlatformResponse.success(vendorRespVOList);

        });
    }

    /**
     * 查询游戏
     *
     * @param rawJson
     * @param sign
     * @param key
     * @param request
     * @return
     */
    @TransMethodResult
    @PostMapping("/game/list")
    public PlatformResponse<PageResult<GameRespVO>> games(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {
        // 校验商户信息
        TenantDO tenantGame = authenticateTenant(rawJson, sign, key);
        GameApiVendorRequest body = JSONObject.parseObject(rawJson, GameApiVendorRequest.class);
        List<String> vendorCodes = tenantGame.getVendorCodes();
        if (ObjectUtil.isNotEmpty(body.getVendorCode())
                && !vendorCodes.contains(body.getVendorCode())) {
            throw new GameException(PlatformCodeEnum.VENDOR_PLATFORM_NOT_SUPPORTED);
        }

        return TenantUtils.execute(tenantGame.getId(), () -> {

            var listVendors = gameInfoService.getInfoPageAllVendor(body);
            var games = BeanUtils.toBean(listVendors, GameRespVO.class);
            return PlatformResponse.success(games);

        });
    }

    /**
     * 查询注单明细
     *
     * @param rawJson
     * @param sign
     * @param key
     * @param request
     * @return
     */
    @PostMapping("/transaction/list")
    public PlatformResponse<PageResult<GameApiTransaction>> gameRecords(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key,@RequestParam(required = false) String zone, HttpServletRequest request) {
        // 校验商户信息
        TenantDO tenantGame = authenticateTenant(rawJson, sign, key);
        GameApiTransactionRequest body = JSONObject.parseObject(rawJson, GameApiTransactionRequest.class);
        return TenantUtils.execute(tenantGame.getId(), () -> {
            RecordsPageReqVO pageReqVO = new RecordsPageReqVO();
            pageReqVO.setPageNo(body.getPageNo());
            pageReqVO.setPageSize(body.getPageSize());

            var pageResult = gameRecordsService.getRecordsPageByUpdate(pageReqVO);
            if (ObjectUtil.isEmpty(pageResult.getList())) {
                return PlatformResponse.success(PageResult.empty());
            }

            List<GameApiTransaction> transactionLists = convert.toList(pageResult.getList());

            PageResult<GameApiTransaction> result = new PageResult<>(transactionLists, pageResult.getTotal(), body.getPageNo());
            return PlatformResponse.success(result);

        });
    }

    /**
     * 详细交易信息
     *
     * @param rawJson
     * @param sign
     * @param key
     * @param request
     * @return
     */
    @PostMapping("/transaction/detail")
    public PlatformResponse<GameApiTransaction> gameRecordsDetail(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key,@RequestParam(required = false) String zone, HttpServletRequest request) {
        // 校验商户信息
        TenantDO tenantGame = authenticateTenant(rawJson, sign, key);
        GameApiDetailRequest body = JSONObject.parseObject(rawJson, GameApiDetailRequest.class);
        return TenantUtils.execute(tenantGame.getId(), () -> {

            var item = gameRecordsService.getRecordsByOrderId(body.getBetId());
            if (ObjectUtil.isEmpty(item)) {
                return PlatformResponse.success();
            }

            return PlatformResponse.success(convert.to(item));

        });
    }


        /**
     * 查询游戏大类
     */
    @PostMapping("/game/categories")
    @ApiAccessLog(operateType = GATEWAY, operateName = "查询游戏大类")
    public PlatformResponse<List<CategoryRespVO>> gameCategories(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        // 校验商户信息
        TenantDO tenantGame = authenticateTenant(rawJson, sign, key);
        return TenantUtils.execute(tenantGame.getId(), () ->
                PlatformResponse.success(gameCategoryService.getEnabledListWithChildren()));
    }

    /**
     * 校验商户：查询租户、验签、IP 白名单
     */
    private TenantDO authenticateTenant(String rawJson, String sign, String key) {
        TenantDO tenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(tenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (CommonStatusEnum.isDisable(tenant.getStatus())) {
            throw new GameException(PlatformCodeEnum.TENANT_DISABLED);
        }
        /*if (!SignUtils.verifySign(rawJson, tenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }*/
        if(CommonStatusEnum.isDisable( tenant.getOperateStatus())){
            throw new GameException(PlatformCodeEnum.TENANT_DISABLED_OPERATE);
        }

        if (ObjectUtil.isNotEmpty(tenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!tenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        return tenant;
    }


    @GetMapping("/status")
    public String health() {
        return "ok";
    }

}
