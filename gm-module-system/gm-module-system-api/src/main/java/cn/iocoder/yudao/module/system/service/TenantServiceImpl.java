package cn.iocoder.yudao.module.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.mybatis.model.AdminUserDO;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.convert.TenantConvert;
import cn.iocoder.yudao.module.system.mapper.AdminUserMapper;
import cn.iocoder.yudao.module.system.model.permission.MenuDO;
import cn.iocoder.yudao.module.system.model.vo.UserSaveReqVO;
import cn.iocoder.yudao.module.system.model.vo.tenant.*;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.service.handler.TenantInfoHandler;
import cn.iocoder.yudao.module.system.service.handler.TenantMenuHandler;

import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

/**
 * 租户 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
@Slf4j
public class TenantServiceImpl implements TenantService {

    /**
     * 商户租户管理员使用的全局模板角色
     */
    private static final Long ROLE_ID_MERCHANT = 2L;
    /**
     * 子代理租户管理员使用的全局模板角色
     */
    private static final Long ROLE_ID_AGENT = 3L;
    /**
     * 商户套餐编号
     */
    private static final Long PACKAGE_ID_MERCHANT = 1L;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 yudao.tenant.enable 配置项，可以关闭多租户的功能，所以这里只能不强制注入
    private TenantProperties tenantProperties;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TenantPackageService tenantPackageService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private AdminUserService userService;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;

    @Resource
    private VendorTenantService vendorTenantService;
    @Resource
    private AdminUserMapper adminUserMapper;


    @Override
    public List<Long> getTenantIdList() {
        List<TenantDO> tenants = tenantMapper.selectList();
        return CollectionUtils.convertList(tenants, TenantDO::getId);
    }

    @Override
    public void validTenant(Long id) {
        TenantDO tenant = getTenant(id);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        if (tenant.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(TENANT_DISABLE, tenant.getName());
        }
    }

    @Override
    public TenantDO getTenantByKey(String name) {
        return tenantMapper.selectOne("tenant_key", name);
    }

    @Override
    public TenantDO getTenantByCode(String code) {
        return tenantMapper.selectOne("code", code);
    }

    @Override
    public void updateVendorGame(GameReqVO updateReqVO) {
        validateTenantVisible(updateReqVO.getTenantId());
        TenantDO tenantDO = tenantMapper.selectById(updateReqVO.getTenantId());

        tenantDO.setVendorCodes(updateReqVO.getVendorCodes());
        if (ObjectUtil.isNotEmpty(updateReqVO.getVendorCodes())) {
            TenantUtils.executeIgnore(() -> vendorTenantService.copyToTenant(updateReqVO.getVendorCodes(), tenantDO));
        }

        tenantDO.setGameCodes(updateReqVO.getGameCodes());
        tenantMapper.updateById(tenantDO);
    }

    @Override
    public GameRespVO getVendorGameConfig(Long tenantId) {
        validateTenantVisible(tenantId);
        TenantDO tenantDO = tenantMapper.selectById(tenantId);

        return BeanUtils.toBean(tenantDO, GameRespVO.class);
    }

    @Override
    public void updateMerchantInfo(UpdateMerchantInfoReqVO updateReqVO) {
        Long tenantId = updateReqVO.getId();
        if (tenantId == null) {
            tenantId = TenantContextHolder.getRequiredTenantId();
        }
        validateTenantVisible(tenantId);
        TenantDO tenant = getTenant(tenantId);
        tenant.setNotifyUrl(updateReqVO.getNotifyUrl());
        tenantMapper.updateById(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenantListByTid(List<Long> tid) {
        List<TenantDO> tenantDOS = tenantMapper.selectList("tid", tid);
        if (ObjectUtil.isNotEmpty(tenantDOS)) {
            List<String> list = tenantDOS.stream().map(TenantDO::getName).toList();
            if (!list.isEmpty()) {
                adminUserMapper.delete(Wrappers.<AdminUserDO>lambdaQuery().in(AdminUserDO::getUsername, list));
            }
        }

        tenantMapper.delete("tid", tid.toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenantListByCode(String code) {
        List<TenantDO> tenantDOS = tenantMapper.selectList("code", code);
        if (ObjectUtil.isNotEmpty(tenantDOS)) {
            List<String> list = tenantDOS.stream().map(TenantDO::getName).toList();
            if (!list.isEmpty()) {
                adminUserMapper.delete(Wrappers.<AdminUserDO>lambdaQuery().in(AdminUserDO::getUsername, list));
            }
        }
        tenantMapper.delete("code", code);
    }

    @Override
    public void updateOperateStatus(TenantUpdateStatusReqVO updateReqVO) {

        tenantMapper.update(null, Wrappers.<TenantDO>lambdaUpdate()
                .set(TenantDO::getOperateStatus, updateReqVO.getStatus())
                .set(updateReqVO.getStatus().equals(CommonStatusEnum.ENABLE.getStatus()), TenantDO::getOperateTime, LocalDateTime.now())
                .in(updateReqVO.getCodes() != null, TenantDO::getCode, updateReqVO.getCodes())
                .eq(updateReqVO.getTid() != null, TenantDO::getTid, updateReqVO.getTid())
        );
    }


    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    @DataPermission(enable = false) // 参见 https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1154 说明
    public Long createTenant(TenantSaveReqVO createReqVO) {
        //validateCreateTenantScope(createReqVO);
        // 校验租户名称是否重复
        validTenantNameDuplicate(createReqVO.getName(), null);
        // 校验租户域名是否重复
        validTenantWebsiteDuplicate(createReqVO.getWebsites(), null);
        // 校验套餐被禁用
        // TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(createReqVO.getPackageId());
        // 校验子代理不能绑定商户套餐
//        validateTenantPackageMatchParent(createReqVO.getParentId(), createReqVO.getPackageId());
        // 校验所属租户
        validateTenantParent(createReqVO.getParentId(), null);

        // 创建租户
        TenantDO tenant = BeanUtils.toBean(createReqVO, TenantDO.class);
        tenant.setOperateStatus(CommonStatusEnum.ENABLE.getStatus());
        tenant.setOperateTime(LocalDateTime.now());
        tenant.setTenantKey(SignUtils.generateKey());
        tenant.setSecretKey(SignUtils.generateKey());
        tenantMapper.insert(tenant);
        // 创建租户的管理员
        TenantUtils.execute(tenant.getId(), () -> {
            // 创建用户，并分配角色
            Long userId = createUser(isChildTenant(createReqVO.getParentId()) ? ROLE_ID_AGENT : ROLE_ID_MERCHANT,
                    tenant.getId(), createReqVO);
            // 修改租户的管理员
            tenantMapper.updateById(new TenantDO().setId(tenant.getId()).setContactUserId(userId));
        });
        if (ObjectUtil.isNotEmpty(createReqVO.getVendorCodes())) {
            TenantUtils.executeIgnore(() -> vendorTenantService.copyToTenant(createReqVO.getVendorCodes(), tenant));
        }

        return tenant.getId();
    }

    private Long createUser(Long roleId, Long tenantId, TenantSaveReqVO createReqVO) {
        // 创建用户，显式传入新租户编号，写入 system_users.tenant_id
        UserSaveReqVO userSaveReqVO = TenantConvert.INSTANCE.convert02(createReqVO);
        userSaveReqVO.setTenantId(tenantId);
        Long userId = userService.createUser(userSaveReqVO);
        // 分配角色
        permissionService.assignUserRole(userId, singleton(roleId));
        return userId;
    }

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    public void updateTenant(TenantSaveReqVO updateReqVO) {
        // 校验存在
        validateUpdateTenant(updateReqVO.getId());
        // 校验租户名称是否重复
        validTenantNameDuplicate(updateReqVO.getName(), updateReqVO.getId());
        // 校验租户域名是否重复
        validTenantWebsiteDuplicate(updateReqVO.getWebsites(), updateReqVO.getId());
        // 校验套餐被禁用
        TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(updateReqVO.getPackageId());
        // 校验子代理不能绑定商户套餐
//        validateTenantPackageMatchParent(updateReqVO.getParentId(), updateReqVO.getPackageId());
        // 校验所属租户
        validateTenantParent(updateReqVO.getParentId(), updateReqVO.getId());

        // 更新租户
        TenantDO updateObj = BeanUtils.toBean(updateReqVO, TenantDO.class);
        if(updateObj.getStatus()!=null&& updateObj.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())){
            updateObj.setCloseTime(LocalDateTime.now());
        }
        if(updateObj.getOperateStatus()!=null && updateObj.getOperateStatus().equals(CommonStatusEnum.ENABLE.getStatus())){
            updateObj.setOperateTime(LocalDateTime.now());
        }

        tenantMapper.updateById(updateObj);

    }

    @Override
    public void updateTenantByCode(TenantSaveReqVO updateReqVO) {
        TenantDO updateObj = tenantMapper.selectByCode(updateReqVO.getCode());
        if (ObjectUtil.isEmpty(updateObj)) {
            throw exception(TENANT_NOT_EXISTS);
        }
        BeanUtils.copyProperties(updateReqVO, updateObj);
        tenantMapper.updateById(updateObj);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantStatus(TenantUpdateStatusReqVO updateReqVO) {
        LambdaQueryWrapper<TenantDO> queryWrapper = Wrappers.<TenantDO>lambdaQuery()
                .in(updateReqVO.getCodes() != null, TenantDO::getCode, updateReqVO.getCodes())
                .eq(updateReqVO.getTid() != null, TenantDO::getTid, updateReqVO.getTid());

        List<TenantDO> tenantDOS = tenantMapper.selectList(queryWrapper);
        if (ObjectUtil.isEmpty(tenantDOS)) {
            throw exception(TENANT_NOT_EXISTS);
        }


        tenantMapper.update(null, Wrappers.<TenantDO>lambdaUpdate()
                .set(TenantDO::getStatus, updateReqVO.getStatus())
                .in(updateReqVO.getCodes() != null, TenantDO::getCode, updateReqVO.getCodes())
                .eq(updateReqVO.getTid() != null, TenantDO::getTid, updateReqVO.getTid())
        );
        //禁用登录账号
        List<String> list = tenantDOS.stream().map(TenantDO::getName).toList();
        if (ObjectUtil.isNotEmpty(list)) {
            adminUserMapper.update(Wrappers.<AdminUserDO>lambdaUpdate().set(AdminUserDO::getStatus, updateReqVO.getStatus())
                    .in(AdminUserDO::getUsername, list)
            );
        }


    }

    private void validTenantNameDuplicate(String name, Long id) {
        TenantDO tenant = tenantMapper.selectByName(name);
        if (tenant == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同名字的租户
        if (id == null) {
            throw exception(TENANT_NAME_DUPLICATE, name);
        }
        if (!tenant.getId().equals(id)) {
            throw exception(TENANT_NAME_DUPLICATE, name);
        }
    }

    private void validTenantWebsiteDuplicate(List<String> websites, Long excludeId) {
        if (CollUtil.isEmpty(websites)) {
            return;
        }
        websites.forEach(website -> {
            List<TenantDO> tenants = tenantMapper.selectListByWebsite(website);
            if (excludeId != null) {
                tenants.removeIf(tenant -> tenant.getId().equals(excludeId));
            }
            if (CollUtil.isNotEmpty(tenants)) {
                throw exception(TENANT_WEBSITE_DUPLICATE, website);
            }
        });
    }

    private void validateTenantPackageMatchParent(Long parentId, Long packageId) {
        if (isChildTenant(parentId) && Objects.equals(packageId, PACKAGE_ID_MERCHANT)) {
            throw exception(TENANT_AGENT_PACKAGE_ERROR);
        }
    }

    @Override
    @DSTransactional
    public void updateTenantRoleMenu(Long tenantId, Set<Long> menuIds) {
        TenantDO tenant = getTenant(tenantId);
        Long templateRoleId = resolveTemplateRoleId(tenant);
        // system_role_menu 不按租户隔离，直接按模板角色 role_id 维护菜单
        permissionService.assignRoleMenu(templateRoleId, menuIds);
        log.info("[updateTenantRoleMenu][租户({}) 模板角色({}) 权限同步为({})]", tenantId, templateRoleId, menuIds);
    }

    private static Long resolveTemplateRoleId(TenantDO tenant) {
        return isAgentTenant(tenant) ? ROLE_ID_AGENT : ROLE_ID_MERCHANT;
    }

    private static boolean isChildTenant(Long parentId) {
        return parentId != null && parentId > 0;
    }

    @Override
    public void deleteTenant(Long id) {
        // 校验存在
        validateUpdateTenant(id);
        // 存在子代理，不允许删除
        validateTenantChildren(id);
        // 删除
        tenantMapper.deleteById(id);
    }

    @Override
    public void deleteTenantList(List<Long> ids) {
        // 1. 校验存在
        ids.forEach(this::validateUpdateTenant);
        ids.forEach(this::validateTenantChildren);

        // 2. 批量删除
        tenantMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenantListByCode(List<String> codes) {
        List<TenantDO> tenantDOS = tenantMapper.selectList("code", codes);
        if (ObjectUtil.isNotEmpty(tenantDOS)) {
            List<String> list = tenantDOS.stream().map(TenantDO::getName).toList();
            if (ObjectUtil.isNotEmpty(list)) {
                adminUserMapper.delete(Wrappers.<AdminUserDO>lambdaQuery().in(AdminUserDO::getUsername,list));
            }
        }

        tenantMapper.delete(Wrappers.<TenantDO>lambdaQuery().in(TenantDO::getCode, codes));
    }

    private TenantDO validateUpdateTenant(Long id) {
        validateTenantVisible(id);
        TenantDO tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        // 内置租户，不允许删除
        if (isSystemTenant(tenant)) {
            throw exception(TENANT_CAN_NOT_UPDATE_SYSTEM);
        }
        return tenant;
    }

    @Override
    public void validateTenantVisible(Long id) {
        if (id == null) {
            return;
        }
        List<Long> visibleIds = getVisibleTenantIds();
        if (visibleIds == null) {
            return;
        }
        if (!visibleIds.contains(id)) {
            throw exception(TENANT_VISIBLE_FORBIDDEN);
        }
    }

    @Override
    public List<Long> resolveQueryTenantIds(Long tenantId) {
        // 用 getScopedTenantIds 纯计算，避免先 set 全量可见范围再缩窄时拦截器范围不一致
        List<Long> visibleIds = getScopedTenantIds(TenantContextHolder.getRequiredTenantId());
        List<Long> result;
        // 系统租户：不限制；仅在手工指定 tenantId 时等值过滤
        if (visibleIds == null) {
            result = tenantId == null ? null : singletonList(tenantId);
        } else if (tenantId != null) {
            // 商户/代理：指定 tenantId 时必须在可见范围内
            if (!visibleIds.contains(tenantId)) {
                throw exception(TENANT_VISIBLE_FORBIDDEN);
            }
            result = singletonList(tenantId);
        } else {
            // 商户/代理未指定 tenantId：默认查询全部可见租户（含直属子代理）
            result = visibleIds;
        }
        // 同步给拦截器；业务侧无需再手写 tenant_id IN（否则会与拦截器条件重复）
        if (result != null) {
            TenantContextHolder.setTenantIds(result);
        }
        return result;
    }

    private void validateCreateTenantScope(TenantSaveReqVO createReqVO) {
        List<Long> visibleIds = getVisibleTenantIds();
        if (visibleIds == null) {
            return;
        }
        Long loginTenantId = TenantContextHolder.getTenantId();
        if (loginTenantId == null) {
            return;
        }

        TenantDO loginTenant = tenantMapper.selectById(loginTenantId);
        if (loginTenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        // 代理不能创建租户
        if (isAgentTenant(loginTenant)) {
            throw exception(TENANT_VISIBLE_FORBIDDEN);
        }
        // 非系统租户不能创建顶级租户，只能在自己名下创建代理
        Long parentId = createReqVO.getParentId();
        if (parentId == null || Objects.equals(parentId, 0L)) {
            throw exception(TENANT_VISIBLE_FORBIDDEN);
        }
        if (!Objects.equals(parentId, loginTenantId)) {
            throw exception(TENANT_VISIBLE_FORBIDDEN);
        }
    }

    private void applyVisibleTenantScope(TenantPageReqVO pageReqVO) {
        List<Long> visibleIds = getVisibleTenantIds();
        if (visibleIds == null) {
            return;
        }
        if (pageReqVO.getId() != null) {
            if (!visibleIds.contains(pageReqVO.getId())) {
                pageReqVO.setIds(singletonList(-1L));
            } else {
                pageReqVO.setIds(singletonList(pageReqVO.getId()));
            }
            pageReqVO.setId(null);
            return;
        }
        pageReqVO.setIds(visibleIds);
    }

    private void validateTenantParent(Long parentId, Long id) {
        if (parentId == null || Objects.equals(parentId, 0L)) {
            return;
        }
        if (Objects.equals(parentId, id)) {
            throw exception(TENANT_PARENT_ERROR);
        }
        TenantDO parentTenant = tenantMapper.selectById(parentId);
        if (parentTenant == null) {
            throw exception(TENANT_PARENT_NOT_EXISTS);
        }
        if (isAgentTenant(parentTenant)) {
            throw exception(TENANT_PARENT_IS_AGENT);
        }
    }

    private void validateTenantChildren(Long id) {
        if (CollUtil.isNotEmpty(tenantMapper.selectListByParentId(id))) {
            throw exception(TENANT_EXISTS_CHILDREN);
        }
    }

    @Override
    public TenantDO getTenant(Long id) {
        return tenantMapper.selectById(id);
    }

    @Override
    public PageResult<TenantDO> getTenantPage(TenantPageReqVO pageReqVO) {
        applyVisibleTenantScope(pageReqVO);
        return tenantMapper.selectPage(pageReqVO);
    }

    @Override
    public TenantDO getTenantByName(String name) {
        return tenantMapper.selectByName(name);
    }

    @Override
    public TenantDO getTenantByWebsite(String website) {
        List<TenantDO> tenants = tenantMapper.selectListByWebsite(website);
        return CollUtil.getFirst(tenants);
    }

    @Override
    public Long getTenantCountByPackageId(Long packageId) {
        return tenantMapper.selectCountByPackageId(packageId);
    }

    @Override
    public List<TenantDO> getTenantListByPackageId(Long packageId) {
        return tenantMapper.selectListByPackageId(packageId);
    }

    @Override
    public List<TenantDO> getTenantListByStatus(Integer status) {
        return tenantMapper.selectListByStatus(status);
    }

    @Override
    public List<TenantDO> getTenantListByParentId(Long parentId) {
        return tenantMapper.selectListByParentId(parentId);
    }

    @Override
    public List<Long> getVisibleTenantIds() {
        List<Long> visibleIds = getScopedTenantIds(TenantContextHolder.getRequiredTenantId());
        // 同步给租户拦截器：调用可见范围的业务默认按总代+子代理查
        if (visibleIds != null) {
            TenantContextHolder.setTenantIds(visibleIds);
        }
        return visibleIds;
    }

    @Override
    public List<Long> getScopedTenantIds(Long tenantId) {
        TenantDO tenant = getTenant(tenantId);
        if (tenant == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
        if (isSystemTenant(tenant)) {
            return null;
        }
        if (isAgentTenant(tenant)) {
            return singletonList(tenantId);
        }
        return tenantMapper.selectSelfAndChildTenantIds(tenantId);
    }

    @Override
    public void handleTenantInfo(TenantInfoHandler handler) {
        // 如果禁用，则不执行逻辑
        if (isTenantDisable()) {
            return;
        }
        // 获得租户
        TenantDO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        // 执行处理器
        handler.handle(tenant);
    }

    @Override
    public void handleTenantMenu(TenantMenuHandler handler) {
        // 如果禁用，则不执行逻辑
        if (isTenantDisable()) {
            return;
        }
        // 获得租户，然后获得菜单
        TenantDO tenant = getTenant(TenantContextHolder.getRequiredTenantId());
        Set<Long> menuIds;
        if (isSystemTenant(tenant)) { // 系统租户，菜单是全量的
            menuIds = CollectionUtils.convertSet(menuService.getMenuList(), MenuDO::getId);
        } else {
            menuIds = tenantPackageService.getTenantPackage(tenant.getPackageId()).getMenuIds();
        }
        // 执行处理器
        handler.handle(menuIds);
    }

    private static boolean isSystemTenant(TenantDO tenant) {
        return Objects.equals(tenant.getPackageId(), TenantDO.PACKAGE_ID_SYSTEM);
    }

    private static boolean isAgentTenant(TenantDO tenant) {
        return tenant.getParentId() != null && !Objects.equals(tenant.getParentId(), 0L);
    }

    private boolean isTenantDisable() {
        return tenantProperties == null || Boolean.FALSE.equals(tenantProperties.getEnable());
    }

}
