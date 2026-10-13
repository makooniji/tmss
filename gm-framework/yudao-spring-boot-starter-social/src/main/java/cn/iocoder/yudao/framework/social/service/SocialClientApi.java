package cn.iocoder.yudao.framework.social.service;


import cn.iocoder.yudao.framework.social.enums.SocialTypeEnum;

/**
 * 社交应用的 API 接口
 *
 * @author osca
 */
public interface SocialClientApi {

    /**
     * 获得社交平台的授权 URL
     *
     * @param socialType  社交平台的类型 {@link SocialTypeEnum}
     * @param userType    用户类型
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri);



}
