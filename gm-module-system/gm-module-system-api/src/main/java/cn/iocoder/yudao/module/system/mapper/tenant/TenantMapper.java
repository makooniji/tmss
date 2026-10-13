package cn.iocoder.yudao.module.system.mapper.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.vo.tenant.TenantPageReqVO;
import cn.iocoder.yudao.module.system.model.vo.tenant.TenantRespVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
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
                .eqIfPresent(TenantDO::getOperateStatus, reqVO.getOperateStatus())
                .eqIfPresent(TenantDO::getCode, reqVO.getCode())
                .inIfPresent(TenantDO::getId, reqVO.getIds())
                .eqIfPresent(TenantDO::getId, reqVO.getId())
                .eqIfPresent(TenantDO::getParentId, reqVO.getParentId())
                .betweenIfPresent(TenantDO::getCreateTime, reqVO.getCreateTime())
                .geIfPresent(TenantDO::getCreateTime, reqVO.getDays())
                .leIfPresent(TenantDO::getCreateTime, reqVO.getEndDays())
                .eqIfPresent(TenantDO::getCode, reqVO.getTenantCode())
                .eqIfPresent(TenantDO::getName, reqVO.getTenantName())
                .orderByDesc(TenantDO::getId));
    }

    default TenantDO selectByName(String name) {
        return selectOne(TenantDO::getName, name);
    }
    default TenantDO selectByCode(String code) {
        return selectOne(TenantDO::getCode, code);
    }

    default List<TenantDO> selectListByWebsite(String website) {
        return selectList(new LambdaQueryWrapperX<TenantDO>()
                .apply(MyBatisUtils.findInSet("websites"), website));
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


    /**
     * 递归查询当前租户及其所有子孙租户 ID 列表
     */
    @Select("""
            WITH RECURSIVE tenant_tree AS (
                SELECT id, parent_id
                FROM system_tenant
                WHERE id = #{tenantId} AND deleted = 0
                
                UNION ALL
                
                SELECT t.id, t.parent_id
                FROM system_tenant t
                INNER JOIN tenant_tree tt ON t.parent_id = tt.id
                WHERE t.deleted = 0
            )
            SELECT id FROM tenant_tree
            """)
    List<Long> selectSelfAndChildTenantIds(@Param("tenantId") Long tenantId);

    @Data
    class TenantStatusCount {

        /**
         * 开启数量 (status = 0)
         */
        private Integer openCounts;

        /**
         * 关闭数量 (status = 1)
         */
        private Integer closeCounts;

        /**
         * 运营数量 (operateStatus = 0)
         */
        private Integer operCounts;
    }

    @Select("""
               SELECT
                   -- 1. 开启数量 (status = 0)
                   COUNT(CASE WHEN status = 0 THEN 1 END) AS openCounts,
               
                   -- 2. 关闭数量 (status = 1)
                   COUNT(CASE WHEN status = 1 THEN 1 END) AS closeCounts,
               
                   -- 3. 运营数量 (operate_status = 0)
                   COUNT(CASE WHEN operate_status = 0  and status=0 THEN 1 END) AS operCounts
               FROM system_tenant
               ${ew.getCustomSqlSegment}
            """)
    TenantStatusCount selectTenantStatus(@Param("ew") Wrapper<TenantDO> wrapper);
    @Select("""
    SELECT * from system_tenant ${ew.getCustomSqlSegment}
""")
    IPage<TenantDO> selectSupportInfoPage(@Param("page") IPage<?> mpPage, @Param("ew") Wrapper<TenantDO> queryWrapperX);
    @Select("""
        <script>
        SELECT
            DATE_FORMAT(create_time, '%Y-%m-%d') AS days,
            status,
            COUNT(*) AS counts
        FROM system_tenant
        <where> 
            <if test="status != null">
               and status = #{status}
            </if>
            <if test="operateStatus != null">
               and operate_status = #{status}
            </if> 
            <if test="ids != null and ids.size() > 0">
               and id in
               <foreach collection="ids" item="id" open="(" separator="," close=")">
                   #{id}
               </foreach>
            </if>
            and id > 1 
        </where>
        GROUP BY days, status
        ORDER BY days DESC
        </script>
        """)
    List<Map<String, Object>> selectTenantDayCount(@Param("status") Integer status,@Param("operateStatus") Integer operateStatus,@Param("ids") List<Long> ids);

    @Select("""
        <script>
        SELECT
            DATE_FORMAT(operate_time, '%Y-%m-%d') AS days,
            status,
            COUNT(*) AS counts
        FROM system_tenant
        <where> 
            <if test="status != null">
               and status = #{status}
            </if>
            <if test="operateStatus != null">
               and operate_status = #{status}
            </if>
                <if test="ids != null and ids.size() > 0">
               and id in
               <foreach collection="ids" item="id" open="(" separator="," close=")">
                   #{id}
               </foreach>
            </if>
            and id > 1 and operate_time is not null
        </where>
        GROUP BY days, status
        ORDER BY days DESC
        </script>
        """)
    List<Map<String, Object>> selectTenantDayOperateCount(@Param("status") Integer status,@Param("operateStatus") Integer operateStatus,@Param("ids") List<Long> ids);
    @Select("""
        <script>
        SELECT
            DATE_FORMAT(close_time, '%Y-%m-%d') AS days,
            status,
            COUNT(*) AS counts
        FROM system_tenant
        <where> 
            <if test="status != null">
               and status = #{status}
            </if>
            <if test="operateStatus != null">
               and operate_status = #{status}
            </if>
               <if test="ids != null and ids.size() > 0">
               and id in
               <foreach collection="ids" item="id" open="(" separator="," close=")">
                   #{id}
               </foreach>
            </if>
            and id > 1 and close_time is not null
        </where>
        GROUP BY days, status
        ORDER BY days DESC
        </script>
        """)
    List<Map<String, Object>> selectTenantDayCloseCount(@Param("status") Integer status,@Param("operateStatus") Integer operateStatus,@Param("ids") List<Long> ids);

}
