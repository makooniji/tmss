package cn.iocoder.yudao.module.system.dal.mysql.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TenantMapper extends BaseMapperX<TenantDO> {

    default PageResult<TenantDO> selectPage(TenantPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TenantDO>()
                .likeIfPresent(TenantDO::getName, reqVO.getName())
                .eqIfPresent(TenantDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TenantDO::getCode, reqVO.getCode())
                .inIfPresent(TenantDO::getId, reqVO.getIds())
                .eqIfPresent(TenantDO::getId, reqVO.getId())
                .eqIfPresent(TenantDO::getParentId, reqVO.getParentId())
                .betweenIfPresent(TenantDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TenantDO::getId));
    }

    default TenantDO selectByName(String name) {
        return selectOne(TenantDO::getName, name);
    }

    default List<TenantDO> selectListByWebsite(String website) {
        return selectList(new LambdaQueryWrapperX<TenantDO>()
                .apply(MyBatisUtils.findInSet("websites", website)));
    }

    default Long selectCountByPackageId(Long packageId) {
        return selectCount(TenantDO::getPackageId, packageId);
    }

    default List<TenantDO> selectListByPackageId(Long packageId) {
        return selectList(TenantDO::getPackageId, packageId);
    }

    default List<TenantDO> selectListByStatus(Integer status) {
        return selectList(TenantDO::getStatus, status);
    }

    default List<TenantDO> selectListByParentId(Long parentId) {
        return selectList(TenantDO::getParentId, parentId);
    }

    @Select("""
                SELECT
                    DATE_FORMAT(create_time,'%Y-%m-%d') AS days,      
                    COUNT(*) AS total
                    FROM system_tenant
                    ${ew.getCustomSqlSegment}
                    GROUP BY DATE_FORMAT(create_time,'%Y-%m-%d')
            
                    
            """)
    List<Map<String,Object>> selectDayCounts(@Param("ew") Wrapper<TenantDO> queryWrapperX1);
    @Select("""
                SELECT
                    status,COUNT(*) AS counts
                    FROM system_tenant
                    ${ew.getCustomSqlSegment}
                    GROUP BY status
            
                    
            """)
    List<Map<String, Object>> selectTenantStatus(@Param("ew") Wrapper<TenantDO> wrapper);
}
