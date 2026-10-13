package cn.iocoder.yudao.module.system.mapper;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.model.CategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 游戏大类 Mapper（gateway / 对外 API 使用）
 */
@Mapper
public interface GameCategoryMapper extends BaseMapperX<CategoryDO> {

    default List<CategoryDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<CategoryDO>()
                .eq(CategoryDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(CategoryDO::getSort)
                .orderByDesc(CategoryDO::getId));
    }

}
