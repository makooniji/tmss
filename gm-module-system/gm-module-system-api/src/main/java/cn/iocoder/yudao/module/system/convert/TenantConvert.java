package cn.iocoder.yudao.module.system.convert;

import cn.iocoder.yudao.module.system.model.vo.UserSaveReqVO;
import cn.iocoder.yudao.module.system.model.vo.tenant.TenantSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 租户 Convert
 *
 * @author osca
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    default UserSaveReqVO convert02(TenantSaveReqVO bean) {
        UserSaveReqVO reqVO = new UserSaveReqVO();
        reqVO.setUsername(bean.getUsername());
        reqVO.setPassword(bean.getPassword());
        reqVO.setNickname(bean.getUsername()).setMobile(bean.getContactMobile());
        return reqVO;
    }

}
