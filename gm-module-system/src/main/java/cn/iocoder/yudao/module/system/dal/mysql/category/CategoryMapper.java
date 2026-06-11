package cn.iocoder.yudao.module.system.dal.mysql.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.system.controller.admin.category.vo.CategoryPageReqVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏大类 Mapper
 *
 * @author osca
 */
@Mapper
public interface CategoryMapper extends BaseMapperX<CategoryDO> {

    default PageResult<CategoryDO> selectPage(CategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CategoryDO>()
                .eqIfPresent(CategoryDO::getTitle, reqVO.getTitle())
                .eqIfPresent(CategoryDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CategoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CategoryDO::getId));
    }

}