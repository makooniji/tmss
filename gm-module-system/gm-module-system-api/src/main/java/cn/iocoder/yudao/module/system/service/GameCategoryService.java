package cn.iocoder.yudao.module.system.service;

import cn.iocoder.yudao.module.system.model.game.response.CategoryRespVO;

import java.util.List;

/**
 * 游戏大类 Service（gateway 对外查询）
 */
public interface GameCategoryService {

    /**
     * 查询已启用的游戏大类列表（含关联小类）
     */
    List<CategoryRespVO> getEnabledListWithChildren();

}
