package cn.iocoder.yudao.module.system.service.categorysub;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.system.dal.mysql.categorysub.CategorySubMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.CATEGORY_SUB_NOT_EXISTS;


/**
 * 游戏分类 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class CategorySubServiceImpl implements CategorySubService {

    @Resource
    private CategorySubMapper categorySubMapper;

    @Override
    public Long createCategorySub(CategorySubSaveReqVO createReqVO) {
        // 插入
        CategorySubDO categorySub = BeanUtils.toBean(createReqVO, CategorySubDO.class);
        categorySubMapper.insert(categorySub);

        // 返回
        return categorySub.getId();
    }

    @Override
    public void updateCategorySub(CategorySubSaveReqVO updateReqVO) {
        // 校验存在
        validateCategorySubExists(updateReqVO.getId());
        // 更新
        CategorySubDO updateObj = BeanUtils.toBean(updateReqVO, CategorySubDO.class);
        categorySubMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategorySub(Long id) {
        // 校验存在
        validateCategorySubExists(id);
        // 删除
        categorySubMapper.deleteById(id);
    }

    @Override
        public void deleteCategorySubListByIds(List<Long> ids) {
        // 删除
        categorySubMapper.deleteByIds(ids);
        }


    private void validateCategorySubExists(Long id) {
        if (categorySubMapper.selectById(id) == null) {
            throw exception(CATEGORY_SUB_NOT_EXISTS);
        }
    }

    @Override
    public CategorySubDO getCategorySub(Long id) {
        return categorySubMapper.selectById(id);
    }

    @Override
    public PageResult<CategorySubDO> getCategorySubPage(CategorySubPageReqVO pageReqVO) {
        return categorySubMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CategorySubDO> getList() {
        return categorySubMapper.selectList(CategorySubDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
    }

}