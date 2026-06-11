package cn.iocoder.yudao.module.system.dal.mysql.categorysub;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.categorysub.vo.CategorySubPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏分类 Mapper
 *
 * @author osca
 */
@Mapper
public interface CategorySubMapper extends BaseMapperX<CategorySubDO> {

    default PageResult<CategorySubDO> selectPage(CategorySubPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CategorySubDO>()
                .likeIfPresent(CategorySubDO::getTitle, reqVO.getTitle())
                .eqIfPresent(CategorySubDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CategorySubDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(CategorySubDO::getLang, reqVO.getLang())
                .likeIfPresent(CategorySubDO::getCateName, reqVO.getCateName())
                .eqIfPresent(CategorySubDO::getCateId, reqVO.getCateId())
                .orderByDesc(CategorySubDO::getId));
    }

}