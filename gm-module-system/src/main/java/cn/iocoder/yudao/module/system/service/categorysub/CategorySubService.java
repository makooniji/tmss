package cn.iocoder.yudao.module.system.service.categorysub;

import java.util.*;

import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubSaveReqVO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 游戏分类 Service 接口
 *
 * @author osca
 */
public interface CategorySubService {

    /**
     * 创建游戏分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategorySub(@Valid CategorySubSaveReqVO createReqVO);

    /**
     * 更新游戏分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCategorySub(@Valid CategorySubSaveReqVO updateReqVO);

    /**
     * 删除游戏分类
     *
     * @param id 编号
     */
    void deleteCategorySub(Long id);

    /**
    * 批量删除游戏分类
    *
    * @param ids 编号
    */
    void deleteCategorySubListByIds(List<Long> ids);

    /**
     * 获得游戏分类
     *
     * @param id 编号
     * @return 游戏分类
     */
    CategorySubDO getCategorySub(Long id);

    /**
     * 获得游戏分类分页
     *
     * @param pageReqVO 分页查询
     * @return 游戏分类分页
     */
    PageResult<CategorySubDO> getCategorySubPage(CategorySubPageReqVO pageReqVO);

    List<CategorySubDO> getList();
}