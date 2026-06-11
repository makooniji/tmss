package cn.iocoder.yudao.module.system.service.vendor;

import java.util.*;

import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorSaveReqVO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 游戏厂商 Service 接口
 *
 * @author osca
 */
public interface VendorService {

    /**
     * 创建游戏厂商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVendor(@Valid VendorSaveReqVO createReqVO);

    /**
     * 更新游戏厂商
     *
     * @param updateReqVO 更新信息
     */
    void updateVendor(@Valid VendorSaveReqVO updateReqVO);

    /**
     * 删除游戏厂商
     *
     * @param id 编号
     */
    void deleteVendor(Long id);

    /**
    * 批量删除游戏厂商
    *
    * @param ids 编号
    */
    void deleteVendorListByIds(List<Long> ids);

    /**
     * 获得游戏厂商
     *
     * @param id 编号
     * @return 游戏厂商
     */
    GameVendorDO getVendor(Long id);

    /**
     * 获得游戏厂商分页
     *
     * @param pageReqVO 分页查询
     * @return 游戏厂商分页
     */
    PageResult<GameVendorDO> getVendorPage(VendorPageReqVO pageReqVO);

    GameVendorDO getVendorByCode(String vendorCode);
}