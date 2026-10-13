//package cn.iocoder.yudao.module.game.service.impl;
//
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import io.game.modules.game.constants.IgsConstants;
//import io.game.modules.game.constants.PgsConstants;
//import io.game.modules.game.constants.YsDictionary;
//import io.game.modules.game.service.GameYsService;
//import io.game.modules.game.vo.LoginRequest;
//import io.game.modules.gameinfo.dao.ClientVendorDao;
//import io.game.modules.gameinfo.dao.VendorDao;
//import io.game.modules.gameinfo.entity.ClientEntity;
//import io.game.modules.gameinfo.entity.ClientVendorEntity;
//import io.game.modules.gameinfo.entity.VendorEntity;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//
//
///**
// * 批量退出其他游戏平台
// * @author koke
// * @email koke@gmail.com
// * @date 2025-11-08 09:53:43
// *
// */
//@Service
//@Slf4j
//public class ExitGameService {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//    @Lazy
//    @Autowired
//    private GameYsService gameYsService;
//    @Autowired
//    private ClientVendorDao clientVendorDao;
//    @Autowired
//    private VendorDao vendorDao;
//
//    @Async("asyncExitGameExecutor")
//    public void exitOtherGames(LoginRequest param) {
//        try {
//            ClientEntity client = param.getClient();
//            VendorEntity vendor = param.getVendor();
//            String vendorCode = vendor.getVendorCode();
////            param.getGameCode();
////            param.getAccount();
//
//            LambdaQueryWrapper<ClientVendorEntity> wp = new LambdaQueryWrapper<>();
//            wp.eq(ClientVendorEntity::getClientCode, client.getClientCode());
//            wp.ne(ClientVendorEntity::getVendorId, vendor.getVendorId());
//            List<ClientVendorEntity> list = clientVendorDao.selectList(wp);
//            if (CollectionUtils.isEmpty(list)) {
//                return;
//            }
//            List<Long> vendorIds = list.stream()
//                    .map(ClientVendorEntity::getVendorId)
//                    .collect(Collectors.toList());
//            LambdaQueryWrapper<VendorEntity> vp = new LambdaQueryWrapper<>();
//            vp.eq(VendorEntity::getStatus, 1);
//            vp.in(VendorEntity::getVendorId, vendorIds);
//            List<VendorEntity> vps = vendorDao.selectList(vp);
//            if (CollectionUtils.isEmpty(vps)) {
//                return;
//            }
//            log.info("批量退出其他游戏厂商ID:{}", vendorIds);
//            for (VendorEntity vo : vps) {
//                try {
//
//                    boolean sameVendorGroup;
//
//                    if (IgsConstants.IGS_VENDOR_LIST.contains(vendorCode)) {
//                        sameVendorGroup = IgsConstants.IGS_VENDOR_LIST.contains(vo.getVendorCode());
//                    } else if (PgsConstants.VENDOR_LIST.contains(vendorCode)) {
//                        sameVendorGroup = PgsConstants.VENDOR_LIST.contains(vo.getVendorCode());
//                    } else {
//                        sameVendorGroup = vendorCode.equals(vo.getVendorCode());
//                    }
//
//                    if (!sameVendorGroup) {
//                        log.info("登出其他厂商游戏:{}",vo.getVendorCode());
//                        gameYsService.exitGame(client, vo, param.getUserId(), param.getAccount());
//                    }
//
//                } catch (Exception e) {
//                    log.error("批量退出游戏平台异常错误信息={}", e.getMessage(), e);
//                }
//            }
//        } catch (Exception e) {
//            log.error("批量退出游戏平台异常 param:{}", param, e);
//        } finally {
//            redisTemplate.delete(YsDictionary.TOKEN_LOCK_PREFIX + param.getUserId());
//        }
//    }
//}
