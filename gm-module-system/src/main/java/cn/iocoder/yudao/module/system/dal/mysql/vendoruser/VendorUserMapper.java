package cn.iocoder.yudao.module.system.dal.mysql.vendoruser;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserPageReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 厂商玩家 Mapper
 *
 * @author osca
 */
@Mapper
public interface VendorUserMapper extends BaseMapperX<VendorUserDO> {

    default PageResult<VendorUserDO> selectPage(VendorUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VendorUserDO>()
                .eqIfPresent(VendorUserDO::getVendorId, reqVO.getVendorId())
                .eqIfPresent(VendorUserDO::getVendorCode, reqVO.getVendorCode())
                .eqIfPresent(VendorUserDO::getId, reqVO.getUserId())
                .likeIfPresent(VendorUserDO::getUsername, reqVO.getUsername())
                .eqIfPresent(VendorUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(VendorUserDO::getCreateTime, reqVO.getCreateTime())

                .orderByDesc(VendorUserDO::getId));
    }

    @Update("""
                UPDATE game_vendor_user
                SET balance = balance + #{changeMoney}
                WHERE username = #{username};
            """)
    void updateAndGetBalance(@Param("username") String username, @Param("changeMoney") Double changeMoney);

}