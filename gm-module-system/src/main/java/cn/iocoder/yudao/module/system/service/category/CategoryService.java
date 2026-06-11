package cn.iocoder.yudao.module.system.service.category;

import java.util.*;

import cn.iocoder.yudao.module.system.controller.admin.category.vo.CategoryPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.category.vo.CategorySaveReqVO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 游戏大类 Service 接口
 *
 * @author osca
 */
public interface CategoryService {

    /**
     * 创建游戏大类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategory(@Valid CategorySaveReqVO createReqVO);

    /**
     * 更新游戏大类
     *
     * @param updateReqVO 更新信息
     */
    void updateCategory(@Valid CategorySaveReqVO updateReqVO);

    /**
     * 删除游戏大类
     *
     * @param id 编号
     */
    void deleteCategory(Long id);

    /**
    * 批量删除游戏大类
    *
    * @param ids 编号
    */
    void deleteCategoryListByIds(List<Long> ids);

    /**
     * 获得游戏大类
     *
     * @param id 编号
     * @return 游戏大类
     */
    CategoryDO getCategory(Long id);

    /**
     * 获得游戏大类分页
     *
     * @param pageReqVO 分页查询
     * @return 游戏大类分页
     */
    PageResult<CategoryDO> getCategoryPage(CategoryPageReqVO pageReqVO);

    List<CategoryDO> getList();
}