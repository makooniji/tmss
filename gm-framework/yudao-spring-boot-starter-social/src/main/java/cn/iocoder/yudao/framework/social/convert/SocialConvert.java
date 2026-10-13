package cn.iocoder.yudao.framework.social.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.model.AdminUserDO;
import cn.iocoder.yudao.framework.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.framework.social.model.vo.AuthSocialLoginReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;


@Mapper
public interface SocialConvert {

    SocialConvert INSTANCE = Mappers.getMapper(SocialConvert.class);


    SocialUserBindReqDTO convert(Long userId, Integer userType, AuthSocialLoginReqVO reqVO);


}
