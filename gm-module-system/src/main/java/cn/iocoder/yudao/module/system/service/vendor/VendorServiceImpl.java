package cn.iocoder.yudao.module.system.service.vendor;

import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorSaveReqVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.system.dal.mysql.vendor.GameVendorMapper;

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

}