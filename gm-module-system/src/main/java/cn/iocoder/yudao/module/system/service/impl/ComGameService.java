//package cn.iocoder.yudao.module.game.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import io.game.modules.game.constants.YsDictionary;
//import io.game.modules.gameinfo.dao.VendorUserDao;
//import io.game.modules.gameinfo.entity.ClientEntity;
//import io.game.modules.gameinfo.entity.VendorEntity;
//import io.game.modules.gameinfo.entity.VendorUserEntity;
//import io.game.modules.gameinfo.service.VendorUserService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.ObjectUtils;
//
//import java.util.Date;
//
//
///**
// * 游戏通用层
// *
// * @author koke
// */
//@Slf4j
//@Service
//public class ComGameService implements YsDictionary {
//
//    @Autowired
//    private VendorUserDao vendorUserDao;
//    @Autowired
//    private VendorUserService vendorUserService;
//
//
//    /**
//     * 创建厂商用户
//     *
//     */
//    @Transactional(rollbackFor = {Exception.class})
//    public void createVendorUser(ClientEntity client, VendorEntity vendor, Long userId, String username, String agentName) {
//        VendorUserEntity record = new VendorUserEntity();
//        record.setClientId(client.getClientId());
//        record.setClientCode(client.getClientCode());
//        record.setVendorCode(vendor.getVendorCode());
//        record.setVendorId(vendor.getVendorId());
//        record.setUserId(userId);
//        record.setUsername(username);
//        record.setPassword(vendor.getPassword());
//        record.setStatus(STATUS.ENABLE);
//        record.setAgentName(agentName);
//        record.setCreateTime(new Date());
//        record.setUpdateTime(new Date());
//        record.setDeleteStatus(STATUS.ENABLE);
//        this.vendorUserService.saveOrUpdate(record);
//    }
//
//    /**
//     * 判断该用户是否已经存在注册
//     *
//     */
//    public boolean isExistingUser(long clientId, long vendorId, String username) {
//        LambdaQueryWrapper<VendorUserEntity> wp = new LambdaQueryWrapper<>();
//        wp.eq(VendorUserEntity::getClientId, clientId);
//        wp.eq(VendorUserEntity::getVendorId, vendorId);
//        wp.eq(VendorUserEntity::getUsername, username);
//        VendorUserEntity user = vendorUserDao.selectOne(wp);
//        if (ObjectUtils.isEmpty(user)) {
//            return false;
//        }
//        return true;
//    }
//
//
//    public VendorUserEntity getVendorUser(long vendorId, String username, String agent) {
//        LambdaQueryWrapper<VendorUserEntity> wp = new LambdaQueryWrapper<>();
//        wp.eq(VendorUserEntity::getAgentName, agent);
//        wp.eq(VendorUserEntity::getVendorId, vendorId);
//        wp.eq(VendorUserEntity::getUsername, username);
//        return vendorUserDao.selectOne(wp);
//    }
//
//
//}
