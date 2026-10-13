package cn.iocoder.yudao.module.system.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorUserVendorDTO;

import cn.iocoder.yudao.module.system.model.vo.vendor.VendorPageReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

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
                .findContainsOrAll(GameVendorDO::getCurrency, reqVO.getCurrency())
                .findContainsOrAll(GameVendorDO::getSupportLang, reqVO.getSupportLang())
                .eqIfPresent(GameVendorDO::getStatus, reqVO.getStatus())
                .likeIfPresent(GameVendorDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(GameVendorDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GameVendorDO::getId));
    }

    @Select("SELECT gv.vendor_code, gv.adapter, gvu.vendor_username, gv.vendor_agent, gv.vendor_key, gv.api_url, gv.vendor_channel, gv.vendor_name " +
            "FROM game_vendor_user gvu " +
            "JOIN game_vendor gv ON gv.vendor_code = gvu.vendor_code " +
            "WHERE gvu.username = #{username} AND gvu.tenant_id = #{tenantId} ")
    List<VendorUserVendorDTO> selectVendorUserByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") Long tenantId);

}