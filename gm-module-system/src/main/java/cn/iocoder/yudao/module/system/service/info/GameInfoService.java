package cn.iocoder.yudao.module.system.service.info;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.model.api.GameApiVendorRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 游戏信息 Service 接口
 *
 * @author mako
 */
public interface GameInfoService {

    /**
     * 创建游戏信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createInfo(@Valid InfoSaveReqVO createReqVO);

    /**
     * 更新游戏信息
     *
     * @param updateReqVO 更新信息
     */
    void updateInfo(@Valid InfoSaveReqVO updateReqVO);

    /**
     * 删除游戏信息
     *
     * @param id 编号
     */
    void deleteInfo(Integer id);

    /**
    * 批量删除游戏信息
    *
    * @param ids 编号
    */
    void deleteInfoListByIds(List<Integer> ids);

    /**
     * 获得游戏信息
     *
     * @param id 编号
     * @return 游戏信息
     */
    GameInfoDO getInfo(Integer id);

    /**
     * 获得游戏信息分页
     *
     * @param pageReqVO 分页查询
     * @return 游戏信息分页
     */
    PageResult<GameInfoDO> getInfoPage(InfoPageReqVO pageReqVO);

    PageResult<GameInfoDO> getInfoPageAllVendor(GameApiVendorRequest pageReqVO);

    GameInfoDO getInfoByCode(@NotEmpty String gameCode);

    void reportOnlineStatus(String gameCode, String username, Long tenantId);

    Integer getOnlineCount(Long tenantId, String gameCode);
}