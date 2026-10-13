package cn.iocoder.yudao.framework.social.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;


import cn.iocoder.yudao.framework.social.enums.SocialTypeEnum;
import cn.iocoder.yudao.framework.social.model.SocialClientDO;
import cn.iocoder.yudao.framework.social.model.vo.SocialClientPageReqVO;
import cn.iocoder.yudao.framework.social.model.vo.SocialClientSaveReqVO;
import jakarta.validation.Valid;
import me.zhyd.oauth.model.AuthUser;


import java.util.List;

/**
 * 社交应用 Service 接口
 *
 * @author osca
 */
public interface SocialClientService {

    /**
     * 获得社交平台的授权 URL
     *
     * @param socialType  社交平台的类型 {@link SocialTypeEnum}
     * @param userType    用户类型
     * @param redirectUri 重定向 URL
     * @return 社交平台的授权 URL
     */
    String getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri);

    /**
     * 请求社交平台，获得授权的用户
     *
     * @param socialType 社交平台的类型
     * @param userType   用户类型
     * @param code       授权码
     * @param state      授权 state
     * @return 授权的用户
     */
    AuthUser getAuthUser(Integer socialType, Integer userType, String code, String state);



    // =================== 客户端管理 ===================

    /**
     * 创建社交客户端
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSocialClient(@Valid SocialClientSaveReqVO createReqVO);

    /**
     * 更新社交客户端
     *
     * @param updateReqVO 更新信息
     */
    void updateSocialClient(@Valid SocialClientSaveReqVO updateReqVO);

    /**
     * 删除社交客户端
     *
     * @param id 编号
     */
    void deleteSocialClient(Long id);

    /**
     * 批量删除社交客户端
     *
     * @param ids 编号数组
     */
    void deleteSocialClientList(List<Long> ids);

    /**
     * 获得社交客户端
     *
     * @param id 编号
     * @return 社交客户端
     */
    SocialClientDO getSocialClient(Long id);

    /**
     * 获得社交客户端分页
     *
     * @param pageReqVO 分页查询
     * @return 社交客户端分页
     */
    PageResult<SocialClientDO> getSocialClientPage(SocialClientPageReqVO pageReqVO);

}
