package cn.iocoder.yudao.module.system.dal.mysql.vendoruser;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserFlowDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 玩家转账记录 Mapper
 *
 * @author osca
 */
@Mapper
public interface UserFlowMapper extends BaseMapperX<VendorUserFlowDO> {

}