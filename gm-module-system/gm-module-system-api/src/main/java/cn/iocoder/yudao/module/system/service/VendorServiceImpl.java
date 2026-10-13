package cn.iocoder.yudao.module.system.service;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.model.vo.vendor.VendorPageReqVO;
import cn.iocoder.yudao.module.system.model.vo.vendor.VendorSaveReqVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorUserVendorDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.system.mapper.GameVendorMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.VENDOR_NOT_EXISTS;


/**
 * 游戏厂商 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class VendorServiceImpl implements VendorService {

    @Resource
    private GameVendorMapper vendorMapper;

    @Override
    public Long createVendor(VendorSaveReqVO createReqVO) {
        // 插入
        GameVendorDO vendor = BeanUtils.toBean(createReqVO, GameVendorDO.class);
        vendorMapper.insert(vendor);

        // 返回
        return vendor.getId();
    }

    @Override
    public void updateVendor(VendorSaveReqVO updateReqVO) {
        // 校验存在
        validateVendorExists(updateReqVO.getId());
        // 更新
        GameVendorDO updateObj = BeanUtils.toBean(updateReqVO, GameVendorDO.class);
        vendorMapper.updateById(updateObj);
    }

    @Override
    public void deleteVendor(Long id) {
        // 校验存在
        validateVendorExists(id);
        // 删除
        vendorMapper.deleteById(id);
    }

    @Override
        public void deleteVendorListByIds(List<Long> ids) {
        // 删除
        vendorMapper.deleteByIds(ids);
        }


    private void validateVendorExists(Long id) {
        if (vendorMapper.selectById(id) == null) {
            throw exception(VENDOR_NOT_EXISTS);
        }
    }

    @Override
    public GameVendorDO getVendor(Long id) {
        return vendorMapper.selectById(id);
    }

    @Override
    public PageResult<GameVendorDO> getVendorPage(VendorPageReqVO pageReqVO) {
        return vendorMapper.selectPage(pageReqVO);
    }

    @Override
    public GameVendorDO getVendorByCode(String vendorCode) {

        return vendorMapper.selectOne("vendor_code",vendorCode);
    }

    @Override
    public GameVendorDO getSubAgentByCode(String vendorCode, String agentCode) {
        LambdaQueryWrapperX<GameVendorDO> queryWrapperX = new LambdaQueryWrapperX<GameVendorDO>()
                .eq(GameVendorDO::getVendorCode, vendorCode);
        GameVendorDO gameVendorDO = vendorMapper.selectOne(queryWrapperX);
        Optional<GameVendorDO.SubVendor> first = gameVendorDO.getSubVendors().stream().filter(p -> p.getVendorAgent().equals(agentCode)).findFirst();
        first.ifPresent(subVendor -> {
            gameVendorDO.setApiUrl(subVendor.getApiUrl());
            gameVendorDO.setCurrency(List.of(subVendor.getCurrency()));
            gameVendorDO.setVendorAgent(subVendor.getVendorAgent());
            gameVendorDO.setVendorKey(subVendor.getVendorKey());

        });
        return gameVendorDO;
    }

    @Override
    public GameVendorDO getSubAgent(String vendorCode, String currency) {
        LambdaQueryWrapperX<GameVendorDO> queryWrapperX = new LambdaQueryWrapperX<GameVendorDO>()
                .eq(GameVendorDO::getVendorCode, vendorCode);
        GameVendorDO gameVendorDO = vendorMapper.selectOne(queryWrapperX);
        Optional<GameVendorDO.SubVendor> first = gameVendorDO.getSubVendors().stream().filter(p -> p.getCurrency().equals(currency)).findFirst();
        first.ifPresent(subVendor -> {
            gameVendorDO.setApiUrl(subVendor.getApiUrl());
            gameVendorDO.setCurrency(List.of(subVendor.getCurrency()));
            gameVendorDO.setVendorAgent(subVendor.getVendorAgent());
            gameVendorDO.setVendorKey(subVendor.getVendorKey());

        });
        return gameVendorDO;
    }

    @Override
    public List<VendorUserVendorDTO> getVendorUserByUsernameAndTenantId(String username, Long tenantId) {
        return vendorMapper.selectVendorUserByUsernameAndTenantId(username, tenantId);
    }

    @Override
    public List<GameVendorDO> getList() {
        return vendorMapper.selectList();
    }

}