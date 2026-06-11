package cn.iocoder.yudao.gateway.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.mq.kafka.StatProducer;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.game.adapter.GamingPlatformFactory;
import cn.iocoder.yudao.module.game.adapter.IPlatform;
import cn.iocoder.yudao.module.game.convert.GameRecordConvert;
import cn.iocoder.yudao.module.game.service.IBalanceService;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.RecordsPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendortenant.VendorTenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserFlowDO;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.*;
import cn.iocoder.yudao.module.system.model.api.response.BalanceFlowRespVO;
import cn.iocoder.yudao.module.system.model.api.response.BalanceRespVO;
import cn.iocoder.yudao.module.system.model.api.response.GameRespVO;
import cn.iocoder.yudao.module.system.model.api.response.VendorRespVO;
import cn.iocoder.yudao.module.system.mq.model.GameStaticDTO;
import cn.iocoder.yudao.module.system.service.info.GameInfoService;
import cn.iocoder.yudao.module.system.service.records.GameRecordsService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.system.service.vendor.VendorService;
import cn.iocoder.yudao.module.system.service.vendortenant.VendorTenantService;
import cn.iocoder.yudao.module.system.service.vendoruser.VendorUserService;
import com.alibaba.fastjson.JSONObject;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.GATEWAY;

/**
 * 下游对接-api接口
 */
@RestController
@Slf4j
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class ApiController {

    private final VendorTenantService vendorTenantService;

    private final VendorService vendorService;
    private final GamingPlatformFactory gamingPlatformFactory;
    private final GameInfoService gameInfoService;
    private final VendorUserService vendorUserService;
    private final TenantService tenantService;
    private final GameRecordsService gameRecordsService;
    private final GameRecordConvert convert;
    private final IBalanceService balanceService;


    protected final StatProducer statProducer;

    /**
     * 获取游戏链接
     *
     */
    @PostMapping("/game/url")
    @ApiAccessLog(operateType = GATEWAY, operateName = "获取游戏链接")
    public PlatformResponse<String> gameUrl(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {

        log.info("获取游戏链接进来了: {}");
        //1. 查询游戏租户信息
        TenantDO currentTenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(currentTenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, currentTenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(currentTenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!currentTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }

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
            //2. 查询游戏是否开启
            GameInfoDO game = gameInfoService.getInfoByCode(body.getGameCode());
            if (ObjectUtil.isEmpty(game)) {
                throw new GameException(PlatformCodeEnum.INVALID_GAME);
            }
            if (!CommonStatusEnum.isEnable(game.getStatus())) {
                throw new GameException(PlatformCodeEnum.GAME_DISABLED);
            }
            if (!CommonStatusEnum.isEnable(game.getMaintain())) {
                throw new GameException(PlatformCodeEnum.UNDER_MAINTENANCE);
            }
            if (ObjectUtil.isNotEmpty(game.getSupportCurrency())) {
                if (!game.getSupportCurrency().contains(body.getCurrency())) {
                    throw new GameException(PlatformCodeEnum.GAME_CURRENCY_NOT_SUPPORTED);
                }
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

            String currency = body.getCurrency();
            if (ObjectUtil.isNotEmpty(gameVendor.getCurrency())) {
                if (!gameVendor.getCurrency().contains(currency)) {
                    throw new GameException(PlatformCodeEnum.GAME_CURRENCY_NOT_SUPPORTED);
                }
            }
            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername(),game.getVendorCode());
            if (ObjectUtil.isEmpty(gameUser)) {
                PlatformResponse<String> response = bean.createMember(gameVendor, body);
                gameUser = vendorUserService.makeUser(currency, gameVendor, currentTenant, body.getUsername(), response.getData());

            }
            // 重构用户名已平台定义为准
            body.setUsername(gameUser.getVendorUsername());
            GameStaticDTO.buildOpen(gameUser, gameVendor, game);

            //5. 调用上游返回链接
            return bean.getGameLink(gameVendor, body);

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
        //1. 查询游戏租户信息
        TenantDO currentTenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(currentTenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, currentTenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(currentTenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!currentTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiTransfer body = JSONObject.parseObject(rawJson, GameApiTransfer.class);

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

            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
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
        //1. 查询游戏租户信息
        TenantDO currentTenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(currentTenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, currentTenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(currentTenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!currentTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiCashDeposit body = JSONObject.parseObject(rawJson, GameApiCashDeposit.class);

        return TenantUtils.execute(currentTenant.getId(), () -> {

            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
            }

            VendorUserFlowDO vendorUserFlowDO = balanceService.transferAmount(body.getReferenceId(), gameUser.getUsername(), body.getTransferAmount(), body.getCurrency());
            return PlatformResponse.success(convert.toFlowVo(vendorUserFlowDO));

        });
    }

    @PostMapping("/cash/withdraw")
    @ApiAccessLog(operateType = GATEWAY, operateName = "金额转出")
    public PlatformResponse<BalanceFlowRespVO> cashWithdraw(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        //1. 查询游戏租户信息
        TenantDO currentTenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(currentTenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, currentTenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(currentTenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!currentTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiCashDeposit body = JSONObject.parseObject(rawJson, GameApiCashDeposit.class);

        return TenantUtils.execute(currentTenant.getId(), () -> {

            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
            }

            VendorUserFlowDO vendorUserFlowDO = balanceService.transferAmount(body.getReferenceId(), gameUser.getUsername(), body.getTransferAmount().negate(), body.getCurrency());
            return PlatformResponse.success(convert.toFlowVo(vendorUserFlowDO));

        });
    }

    @PostMapping("/cash/balance")
    @ApiAccessLog(operateType = GATEWAY, operateName = "查询余额")
    public PlatformResponse<BalanceRespVO> cashBalance(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key) {
        //1. 查询游戏租户信息
        TenantDO currentTenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(currentTenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, currentTenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(currentTenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!currentTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiCashBalance body = JSONObject.parseObject(rawJson, GameApiCashBalance.class);

        return TenantUtils.execute(currentTenant.getId(), () -> {

            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername());
            if (ObjectUtil.isEmpty(gameUser)) {
                throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
            }

            VendorUserBalanceDO vendorUserFlowDO = balanceService.getBalance(gameUser.getUsername(), body.getCurrency());
            return PlatformResponse.success(convert.toBalanceVo(vendorUserFlowDO));

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
        //1. 查询游戏租户信息
        TenantDO currentTenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(currentTenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, currentTenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(currentTenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!currentTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
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

            //4. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername());

            //5. 调用上游返回链接
            return bean.exitGame(gameVendor, gameUser.getVendorUsername());

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
        //1. 查询游戏租户信息
        TenantDO tenantGame = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(tenantGame)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, tenantGame.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(tenantGame.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!tenantGame.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiVendorRequest body = JSONObject.parseObject(rawJson, GameApiVendorRequest.class);
        return TenantUtils.execute(tenantGame.getId(), () -> {

            List<VendorTenantDO> listVendors = vendorTenantService.getListVendors(body);
            List<VendorRespVO> vendorRespVOList = convert.toVendorRespVOList(listVendors);
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
        //1. 查询游戏租户信息
        TenantDO tenantGame = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(tenantGame)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
       /* if (!SignUtils.verifySign(rawJson, tenantGame.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }*/
        if (ObjectUtil.isNotEmpty(tenantGame.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!tenantGame.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
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
    public PlatformResponse<PageResult<GameApiTransaction>> gameRecords(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {
        //1. 查询游戏租户信息
        TenantDO tenantGame = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(tenantGame)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, tenantGame.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(tenantGame.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!tenantGame.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiTransactionRequest body = JSONObject.parseObject(rawJson, GameApiTransactionRequest.class);
        return TenantUtils.execute(tenantGame.getId(), () -> {
            RecordsPageReqVO pageReqVO = new RecordsPageReqVO();
            pageReqVO.setPageNo(body.getPageNo());
            pageReqVO.setPageSize(body.getPageSize());
            pageReqVO.setCreateTime(new LocalDateTime[]{
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(body.getFromTime()), ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(body.getToTime()), ZoneId.systemDefault())
            });
            var pageResult = gameRecordsService.getRecordsPage(pageReqVO);
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
    public PlatformResponse<GameApiTransaction> gameRecordsDetail(@RequestBody String rawJson, @RequestHeader("X-Signature") String sign, @RequestHeader("X-API-Key") String key, HttpServletRequest request) {
        //1. 查询游戏租户信息
        TenantDO tenantGame = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(tenantGame)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (!SignUtils.verifySign(rawJson, tenantGame.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(tenantGame.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!tenantGame.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        GameApiDetailRequest body = JSONObject.parseObject(rawJson, GameApiDetailRequest.class);
        return TenantUtils.execute(tenantGame.getId(), () -> {

            var item = gameRecordsService.getRecordsByOrderId(body.getBetId());
            if (ObjectUtil.isEmpty(item)) {
                return PlatformResponse.success();
            }

            return PlatformResponse.success(convert.to(item));

        });
    }

}
