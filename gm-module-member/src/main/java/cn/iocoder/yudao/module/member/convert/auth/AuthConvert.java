package cn.iocoder.yudao.module.member.convert.auth;

import cn.iocoder.yudao.framework.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.framework.social.dto.SocialUserUnbindReqDTO;
import cn.iocoder.yudao.framework.social.dto.SocialWxJsapiSignatureRespDTO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.*;
import cn.iocoder.yudao.module.member.controller.app.social.vo.AppSocialUserUnbindReqVO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserResetPasswordReqVO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.iocoder.yudao.module.system.sms.dto.code.SmsCodeSendReqDTO;
import cn.iocoder.yudao.module.system.sms.dto.code.SmsCodeUseReqDTO;
import cn.iocoder.yudao.module.system.sms.dto.code.SmsCodeValidateReqDTO;

import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    SocialUserBindReqDTO convert(Long userId, Integer userType, AppAuthSocialLoginReqVO reqVO);
    SocialUserUnbindReqDTO convert(Long userId, Integer userType, AppSocialUserUnbindReqVO reqVO);

    SmsCodeSendReqDTO convert(AppAuthSmsSendReqVO reqVO);
    SmsCodeUseReqDTO convert(AppMemberUserResetPasswordReqVO reqVO, SmsSceneEnum scene, String usedIp);
    SmsCodeUseReqDTO convert(AppAuthSmsLoginReqVO reqVO, Integer scene, String usedIp);

    cn.iocoder.yudao.module.system.model.vo.oauth2.AppAuthLoginRespVO convert(OAuth2AccessTokenRespDTO bean, String openid);

    SmsCodeValidateReqDTO convert(AppAuthSmsValidateReqVO bean);

    SocialWxJsapiSignatureRespDTO convert(SocialWxJsapiSignatureRespDTO bean);

}
