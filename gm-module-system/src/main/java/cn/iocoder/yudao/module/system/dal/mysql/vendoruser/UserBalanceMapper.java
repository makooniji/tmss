package cn.iocoder.yudao.module.system.dal.mysql.vendoruser;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 厂商玩家 Mapper
 *
 * @author osca
 */
@Mapper
public interface UserBalanceMapper extends BaseMapperX<VendorUserBalanceDO> {


    @Update("""
                UPDATE game_vendor_user_balance
                SET balance = balance + #{changeMoney}
                WHERE username = #{username} and currency=#{currency};
            """)
    void opsBalance(@Param("username") String username, @Param("changeMoney") BigDecimal changeMoney, @Param("currency") String currency);

}