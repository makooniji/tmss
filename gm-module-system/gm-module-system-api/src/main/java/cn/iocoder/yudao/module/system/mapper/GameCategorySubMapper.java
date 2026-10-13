package cn.iocoder.yudao.module.system.mapper;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.model.CategorySubDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 游戏小类 Mapper（gateway / 对外 API 使用）
 */
@Mapper
public interface GameCategorySubMapper extends BaseMapperX<CategorySubDO> {

    default List<CategorySubDO> selectEnabledByCateIds(Collection<Long> cateIds) {
        if (cateIds == null || cateIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CategorySubDO>()
                .in(CategorySubDO::getCateId, cateIds)
                .eq(CategorySubDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(CategorySubDO::getSort)
                .orderByDesc(CategorySubDO::getId));
    }

}
