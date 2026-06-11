package cn.iocoder.yudao.module.system.dal.mysql.vendor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.controller.admin.vendor.vo.VendorPageReqVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏厂商 Mapper
 *
 * @author osca
 */
@Mapper
public interface GameVendorMapper extends BaseMapperX<GameVendorDO> {

    default PageResult<GameVendorDO> selectPage(VendorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GameVendorDO>()
                .eqIfPresent(GameVendorDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(GameVendorDO::getVendorName, reqVO.getVendorName())
                .eqIfPresent(GameVendorDO::getLogo, reqVO.getLogo())
                .eqIfPresent(GameVendorDO::getCurrency, reqVO.getCurrency())
                .eqIfPresent(GameVendorDO::getStatus, reqVO.getStatus())
                .eqIfPresent(GameVendorDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(GameVendorDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GameVendorDO::getId));
    }

}