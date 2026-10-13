//package cn.iocoder.yudao.module.game.service.impl;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import io.game.common.exception.RRException;
//import io.game.modules.game.base.GameServiceResolver;
//import io.game.modules.game.constants.StatusCode;
//import io.game.modules.game.constants.YsDictionary;
//import io.game.modules.game.service.GameService;
//import io.game.modules.game.service.GameYsService;
//import io.game.modules.game.vo.BalanceRequest;
//import io.game.modules.game.vo.LoginRequest;
//import io.game.modules.gameinfo.dao.ClientDao;
//import io.game.modules.gameinfo.dao.ClientVendorDao;
//import io.game.modules.gameinfo.dao.VendorDao;
//import io.game.modules.gameinfo.dao.VendorUserDao;
//import io.game.modules.gameinfo.entity.ClientEntity;
//import io.game.modules.gameinfo.entity.VendorEntity;
//import io.game.modules.gameinfo.entity.VendorUserEntity;
//import io.game.modules.sys.service.SysLangService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ObjectUtils;
//
//import java.util.concurrent.TimeUnit;
//
//
///**
// * 游戏聚合服务
// *
// * @author koke
// */
//@Service
//@Slf4j
//public class GameYsServiceImpl implements GameYsService, YsDictionary {
//
//    @Autowired
//    private ClientDao clientDao;
//    @Autowired
//    private VendorDao vendorDao;
//    @Autowired
//    private GameServiceResolver gameServiceResolver;
//    @Autowired
//    private ComGameService comGameService;
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//    @Autowired
//    private ExitGameService exitGameService;
//    @Autowired
//    private SysLangService sysLangService;
//    @Autowired
//    private VendorUserDao vendorUserDao;
//    @Autowired
//    private ClientVendorDao clientVendorDao;
//
//
//
//
//    @Override
//    public void createMember(String clientCode, String vendorCode, Long userId,
//                             String account, String userIp) {
//        try {
//            LambdaQueryWrapper<ClientEntity> wp = new LambdaQueryWrapper<>();
//            wp.eq(ClientEntity::getStatus, 1);
//            wp.eq(ClientEntity::getDeleteStatus, 1);
//            wp.eq(ClientEntity::getClientCode, clientCode);
//            ClientEntity client = clientDao.selectOne(wp);
//            if (ObjectUtils.isEmpty(client)) {
//                log.info("游戏厂商创建用户失败,商户编码:{}, 用户:{}", clientCode, account);
//                return;
//            }
//
//            //所有在厂商注册玩家必须加前缀
//            String username = client.getUserPrefix() + userId;
//            LambdaQueryWrapper<VendorEntity> vp = new LambdaQueryWrapper<>();
//            vp.eq(VendorEntity::getStatus, 1);
//            vp.eq(VendorEntity::getDeleteStatus, 1);
//            vp.eq(VendorEntity::getVendorCode, vendorCode);
//            VendorEntity vendor = vendorDao.selectOne(vp);
//            if (ObjectUtils.isEmpty(vendor)) {
//                log.info("游戏厂商创建用户失败,厂商编码:{}, 用户:{}", vendorCode, account);
//                return;
//            }
//
////            LambdaQueryWrapper<ClientVendorEntity> cv = new LambdaQueryWrapper<>();
////            cv.eq(ClientVendorEntity::getStatus, 1);
////            //cv.eq(ClientVendorEntity::getDeleteStatus, 1);
////            cv.eq(ClientVendorEntity::getVendorCode, vendorCode);
////            cv.eq(ClientVendorEntity::getClientCode, clientCode);
////            ClientVendorEntity clientVendor = clientVendorDao.selectOne(cv);
////            if (ObjectUtils.isEmpty(clientVendor)) {
////                log.info("游戏厂商创建用户失败,商户厂商编码:{}, 用户:{}", vendorCode, account);
////                return;
////            }
//
//            GameService<?> gameService = gameServiceResolver.resolve(vendorCode);
//            if (!comGameService.isExistingUser(client.getClientId(), vendor.getVendorId(), username)) {
//                gameService.createMember(client, vendor, userId, username, userIp);
//                this.comGameService.createVendorUser(client, vendor, userId, username, vendor.getAgent());
//            }
//        } catch (Exception e) {
//            log.error("游戏厂商创建用户失败,厂商编码:{}", vendorCode, e);
//        }
//    }
//
//    @Override
//    public String getToken(LoginRequest login) {
//        String clientCode = login.getClientCode();
//        String vendorCode = login.getVendorCode();
//        Long userId = login.getUserId();
//        if (StringUtils.isBlank(clientCode) || StringUtils.isBlank(vendorCode)
//                || userId == null ) {
//            log.info("创建token参数错误:{}, 厂商:{}, 用户:{}", clientCode, vendorCode, userId);
//            throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR120003), Integer.parseInt(StatusCode.ERR120003));
//        }
//
//        LambdaQueryWrapper<ClientEntity> wp = new LambdaQueryWrapper<>();
//        wp.eq(ClientEntity::getStatus, 1);
//        wp.eq(ClientEntity::getDeleteStatus, 1);
//        wp.eq(ClientEntity::getClientCode, clientCode);
//        ClientEntity client = clientDao.selectOne(wp);
//        if (ObjectUtils.isEmpty(client)) {
//            log.info("创建token商户数据为空:{}, 用户:{}", clientCode, userId);
//            throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR120051), Integer.parseInt(StatusCode.ERR120051));
//        }
//
//        LambdaQueryWrapper<VendorEntity> vp = new LambdaQueryWrapper<>();
//        vp.eq(VendorEntity::getStatus, 1);
//        vp.eq(VendorEntity::getDeleteStatus, 1);
//        vp.eq(VendorEntity::getVendorCode, vendorCode);
//        VendorEntity vendor = vendorDao.selectOne(vp);
//        if (ObjectUtils.isEmpty(vendor)) {
//            log.info("创建token厂商数据为空:{}, 用户:{}", vendorCode, userId);
//            throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR120050), Integer.parseInt(StatusCode.ERR120050));
//        }
//
////        LambdaQueryWrapper<ClientVendorEntity> cv = new LambdaQueryWrapper<>();
////        cv.eq(ClientVendorEntity::getStatus, 1);
////        //cv.eq(ClientVendorEntity::getDeleteStatus, 1);
////        cv.eq(ClientVendorEntity::getVendorCode, vendorCode);
////        cv.eq(ClientVendorEntity::getClientCode, clientCode);
////        ClientVendorEntity clientVendor = clientVendorDao.selectOne(cv);
////        if (ObjectUtils.isEmpty(clientVendor)) {
////            log.info("创建token商户厂商数据为空:{}, 用户:{}", vendorCode, userId);
////            throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR120053), Integer.parseInt(StatusCode.ERR120053));
////        }
//
//        //所有在厂商注册玩家必须加前缀
//        String username = client.getUserPrefix() + userId;
//        String tooManyKey = TOKEN_LOCK_PREFIX + username;
//        Long count = redisTemplate.opsForValue().increment(tooManyKey);
//        if (count != null && count == 1) {
//            // 只在第一次设置过期时间（防止 TTL 被覆盖）
//            redisTemplate.expire(tooManyKey, 5, TimeUnit.SECONDS);
//        }
//        if (count != null && count > 1) {
//            throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR120049), Integer.parseInt(StatusCode.ERR120049));
//        }
//
//        LambdaQueryWrapper<VendorUserEntity> vu = new LambdaQueryWrapper<>();
//        vu.eq(VendorUserEntity::getClientId, client.getClientId());
//        vu.eq(VendorUserEntity::getVendorId, vendor.getVendorId());
//        vu.eq(VendorUserEntity::getUsername, username);
//        VendorUserEntity user = vendorUserDao.selectOne(vu);
//        if (!ObjectUtils.isEmpty(user)) {
//            if (user.getStatus() != 1 || user.getDeleteStatus() != 1) {
//                log.info("厂商玩家数据验证不通过username:{}, vendorCode:{}, 用户:{}", username, vendorCode, userId);
//                throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR120052), Integer.parseInt(StatusCode.ERR120052));
//            }
//        }
//
//        login.setClient(client);
//        login.setVendor(vendor);
//        login.setAccount(username);
//        GameService<?> gameService = gameServiceResolver.resolve(vendorCode);
//        if (!comGameService.isExistingUser(client.getClientId(), vendor.getVendorId(), username)) {
//            gameService.createMember(client, vendor, userId, username, login.getClientIp());
//            this.comGameService.createVendorUser(client, vendor, userId, username, vendor.getAgent());
//        }
//        try {
//            String gameUrl = gameService.getToken(login);
//            if (StringUtils.isNotBlank(gameUrl)) {
//                // 批量退出其他游戏平台
//                exitGameService.exitOtherGames(login);
//            }
//            return gameUrl;
//        } catch (Exception e) {
//            log.info("创建token游戏地址失败 param:{}", login, e);
//            redisTemplate.delete(TOKEN_LOCK_PREFIX + userId);
//            throw new RRException(sysLangService.getSysLangValueEn(StatusCode.ERR100999), Integer.parseInt(StatusCode.ERR100999));
//        }
//    }
//
//
//    @Override
//    public JSONObject getUserBalance(BalanceRequest param) {
//        try {
//            String agent = param.getAgent();
//            String vendorCode = param.getVendorCode();
//            String username = param.getAccount();
//            if (StringUtils.isBlank(agent) || StringUtils.isBlank(vendorCode)
//                    || StringUtils.isBlank(username)) {
//                log.info("查询余额参数错误:{}, 厂商:{}, 用户:{}", agent, vendorCode, username);
//                return null;
//            }
//            LambdaQueryWrapper<VendorEntity> vp = new LambdaQueryWrapper<>();
//            vp.eq(VendorEntity::getStatus, 1);
//            vp.eq(VendorEntity::getDeleteStatus, 1);
//            vp.eq(VendorEntity::getVendorCode, vendorCode);
//            VendorEntity vendor = vendorDao.selectOne(vp);
//            if (ObjectUtils.isEmpty(vendor)) {
//                log.info("查询余额厂商验证不通过:{}, 用户:{}", vendorCode, username);
//                return null;
//            }
//
//            if (!agent.equals(vendor.getAgent())) {
//                log.info("查询余额代理验证不通过:{}, 用户:{}", vendorCode, username);
//                return null;
//            }
//
//            VendorUserEntity vendorUser = comGameService.getVendorUser(vendor.getVendorId(), username, agent);
//            if (ObjectUtils.isEmpty(vendorUser)) {
//                log.info("查询余额用户没有在厂商注册:{}, 用户:{} 代理商:{}", vendorCode, username, agent);
//                return null;
//            }
//
//            ClientEntity client = clientDao.selectById(vendorUser.getClientId());
//            if (ObjectUtils.isEmpty(client) || client.getStatus() == 2) {
//                log.info("查询余额商户验证不通过:{}, 用户:{}", param.getClientCode(), username);
//                return null;
//            }
//            if (!username.startsWith(client.getUserPrefix())) {
//                log.info("查询余额用户验证不通过:{}, 用户:{}", param.getClientCode(), username);
//                return null;
//            }
//            param.setClient(client);
//            param.setVendor(vendor);
//            GameService<?> gameService = gameServiceResolver.resolve(vendorCode);
//            return gameService.getUserBalance(param);
//
//        }catch (RRException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            log.error("查询余额异常, param={}, error={}", param, e.getMessage());
//            return null;
//        }
//    }
//
//    @Override
//    public JSONObject changeUserBalance(BalanceRequest param) {
//        try {
//            String agent = param.getAgent();
//            String vendorCode = param.getVendorCode();
//            String username = param.getAccount();
//            if (StringUtils.isBlank(agent) || StringUtils.isBlank(vendorCode)
//                    || StringUtils.isBlank(username)) {
//                log.error("变更玩家余额参数错误:{}, 厂商:{}, 用户:{}", agent, vendorCode, username);
//                return null;
//            }
//
//            LambdaQueryWrapper<VendorEntity> vp = new LambdaQueryWrapper<>();
//            vp.eq(VendorEntity::getVendorCode, vendorCode);
//            VendorEntity vendor = vendorDao.selectOne(vp);
//            if (ObjectUtils.isEmpty(vendor)) {
//                log.error("变更玩家余额厂商验证不通过:{}, 用户:{}", vendorCode, username);
//                return null;
//            }
//
//            if (!agent.equals(vendor.getAgent())) {
//                log.error("变更玩家余额代理验证不通过:{}, 用户:{}", vendor.getVendorCode(), username);
//                return null;
//            }
//
//            VendorUserEntity vendorUser = comGameService.getVendorUser(vendor.getVendorId(), username, agent);
//            if (ObjectUtils.isEmpty(vendorUser)) {
//                log.error("变更玩家余额用户没有在厂商注册:{}, 用户:{} 代理商:{}", vendorCode, username, agent);
//                return null;
//            }
//
//            ClientEntity client = clientDao.selectById(vendorUser.getClientId());
//            if (ObjectUtils.isEmpty(client)) {
//                log.error("变更玩家余额商户验证不通过:{}, 用户:{}", param.getClientCode(), username);
//                return null;
//            }
//            if (!username.startsWith(client.getUserPrefix())) {
//                log.error("变更玩家余额用户验证不通过:{}, 用户:{}", param.getClientCode(), username);
//                return null;
//            }
//            param.setClient(client);
//            param.setVendor(vendor);
//            GameService<?> gameService = gameServiceResolver.resolve(vendorCode);
//            return gameService.changeUserBalance(param);
//        }
//        catch (RRException e) {
//            throw e;
//        }
//        catch (Exception e) {
//
//            log.error("变更玩家余额异常, param={}, error={}", param, e.getMessage(), e);
//            return null;
//        }
//
//    }
//
//
//
//
//    @Override
//    public void exitGame(ClientEntity client, VendorEntity vendor, Long userId, String account) {
//        try {
//            log.info("用户退出游戏厂商:{}, 用户:{}", vendor.getVendorCode(), userId);
//            GameService<?> gameService = gameServiceResolver.resolve(vendor.getVendorCode());
//            gameService.exitGame(client, vendor, userId, account);
//        } catch (Exception e) {
//            log.error("用户退出游戏失败,厂商编码:{}", vendor, e);
//        }
//    }
//
//}
