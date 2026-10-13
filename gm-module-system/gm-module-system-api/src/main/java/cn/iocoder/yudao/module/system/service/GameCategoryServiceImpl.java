package cn.iocoder.yudao.module.system.service;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.mapper.GameCategoryMapper;
import cn.iocoder.yudao.module.system.mapper.GameCategorySubMapper;
import cn.iocoder.yudao.module.system.model.CategoryDO;
import cn.iocoder.yudao.module.system.model.CategorySubDO;
import cn.iocoder.yudao.module.system.model.game.response.CategoryRespVO;
import cn.iocoder.yudao.module.system.model.game.response.CategorySubRespVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameCategoryServiceImpl implements GameCategoryService {

    @Resource
    private GameCategoryMapper gameCategoryMapper;
    @Resource
    private GameCategorySubMapper gameCategorySubMapper;

    @Override
    public List<CategoryRespVO> getEnabledListWithChildren() {
        List<CategoryDO> categories = gameCategoryMapper.selectEnabledList();
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptyList();
        }
        List<Long> cateIds = categories.stream().map(CategoryDO::getId).collect(Collectors.toList());
        List<CategorySubDO> subList = gameCategorySubMapper.selectEnabledByCateIds(cateIds);
        Map<Long, List<CategorySubRespVO>> subMap = subList.stream()
                .collect(Collectors.groupingBy(CategorySubDO::getCateId,
                        Collectors.mapping(sub -> BeanUtils.toBean(sub, CategorySubRespVO.class), Collectors.toList())));

        return categories.stream().map(category -> {
            CategoryRespVO vo = BeanUtils.toBean(category, CategoryRespVO.class);
            vo.setSubCategory(subMap.getOrDefault(category.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

}
