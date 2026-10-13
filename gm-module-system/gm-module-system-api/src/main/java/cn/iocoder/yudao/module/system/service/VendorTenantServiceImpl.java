package cn.iocoder.yudao.module.system.service;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.model.game.GameApiVendorRequest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.model.vo.tenant.GameRespVO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantPageReqVO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantRespVO;
import cn.iocoder.yudao.module.system.model.vo.vendorTenant.VendorTenantSaveReqVO;
import cn.iocoder.yudao.module.system.model.GameInfoDO;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorTenantDO;
import cn.iocoder.yudao.module.system.mapper.GameInfoMapper;
import cn.iocoder.yudao.module.system.mapper.GameVendorMapper;
import cn.iocoder.yudao.module.system.mapper.VendorTenantMapper;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.VENDOR_TENANT_NOT_EXISTS;


/**
 * 租户厂商 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
public class VendorTenantServiceImpl implements VendorTenantService {

    @Resource
    private VendorTenantMapper vendorTenantMapper;

    @Resource
    private GameVendorMapper gameVendorMapper;

    @Resource
    private GameInfoMapper gameInfoMapper;

    @Override
    public Long createVendorTenant(VendorTenantSaveReqVO createReqVO) {
        // 插入
        VendorTenantDO vendorTenant = BeanUtils.toBean(createReqVO, VendorTenantDO.class);
        vendorTenantMapper.insert(vendorTenant);

        // 返回
        return vendorTenant.getId();
    }

    @Override
    public void updateVendorTenant(VendorTenantSaveReqVO updateReqVO) {
        // 校验存在
        validateVendorTenantExists(updateReqVO.getId());
        // 更新
        VendorTenantDO updateObj = BeanUtils.toBean(updateReqVO, VendorTenantDO.class);
        vendorTenantMapper.updateById(updateObj);
    }

    @Override
    public void deleteVendorTenant(Long id) {
        // 校验存在
        validateVendorTenantExists(id);
        // 删除
        vendorTenantMapper.deleteById(id);
    }

    @Override
        public void deleteVendorTenantListByIds(List<Long> ids) {
        // 删除
        vendorTenantMapper.deleteByIds(ids);
        }


    private void validateVendorTenantExists(Long id) {
        if (vendorTenantMapper.selectById(id) == null) {
            throw exception(VENDOR_TENANT_NOT_EXISTS);
        }
    }

    @Override
    public VendorTenantDO getVendorTenant(Long id) {
        return vendorTenantMapper.selectById(id);
    }

    @Override
    public PageResult<VendorTenantDO> getVendorTenantPage(VendorTenantPageReqVO pageReqVO) {
        return vendorTenantMapper.selectPage(pageReqVO);
    }

    @Override
    public void fillCategoryCode(List<VendorTenantRespVO> list) {
        if (ObjectUtil.isEmpty(list)) {
            return;
        }
        List<String> vendorCodes = list.stream().map(VendorTenantRespVO::getVendorCode).filter(ObjectUtil::isNotEmpty).distinct().toList();
        if (ObjectUtil.isEmpty(vendorCodes)) {
            return;
        }
        List<GameVendorDO> gameVendorDOS = gameVendorMapper.selectList(
                Wrappers.<GameVendorDO>lambdaQuery()
                        .select(GameVendorDO::getVendorCode, GameVendorDO::getCategoryCode)
                        .in(GameVendorDO::getVendorCode, vendorCodes)
        );
        list.forEach(vo -> gameVendorDOS.stream().filter(p->p.getVendorCode().equals(vo.getVendorCode())).findFirst().ifPresent(gameVendorDO -> {
            vo.setCategoryCode(gameVendorDO.getCategoryCode());
        }));

    }

    @Override
    public PageResult<VendorTenantDO> getTenantVendorPage(VendorTenantPageReqVO pageReqVO) {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        // executeIgnore 会关闭拦截器，不会自动拼 tenant_id，必须在 Wrapper 里显式 eq
        return TenantUtils.executeIgnore(() -> vendorTenantMapper.selectPageByTenantId(pageReqVO, tenantId));
    }

    @Override
    @TenantIgnore
    public VendorTenantDO getVendorByCode(String vendor, Long tenantId) {
       return vendorTenantMapper.selectOne(Wrappers.<VendorTenantDO>lambdaQuery()
                .eq(VendorTenantDO::getTenantId, tenantId)
                .eq(ObjectUtil.isNotEmpty(vendor), VendorTenantDO::getVendorCode, vendor));
    }

    @Override
    public VendorTenantDO getVendorByTenantCode(String agent) {
       return vendorTenantMapper.selectOne("tenant_code",agent);
    }

    @Override
    public List<GameVendorDO> getListVendors(GameApiVendorRequest gameApiVendorRequest) {
        boolean includeDisabled = Boolean.TRUE.equals(gameApiVendorRequest.getIncludeDisabled());
        List<VendorTenantDO> tenantVendors = vendorTenantMapper.selectList(new LambdaQueryWrapperX<VendorTenantDO>()
                .eq(!includeDisabled, VendorTenantDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .findContainsOrAll(VendorTenantDO::getSupportLang, gameApiVendorRequest.getLanguage())
                .findContainsOrAll(VendorTenantDO::getCurrency, gameApiVendorRequest.getCurrency()));
        if (ObjectUtil.isEmpty(tenantVendors)) {
            return List.of();
        }
        List<String> vendorCodes = tenantVendors.stream().map(VendorTenantDO::getVendorCode).toList();
        return gameVendorMapper.selectList(Wrappers.<GameVendorDO>lambdaQuery()
                .in(GameVendorDO::getVendorCode, vendorCodes)
                .eq(!includeDisabled, GameVendorDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
    }

    @Override
    public void copyToTenant(List<String> vendorCodes, TenantDO tenant) {
        vendorTenantMapper.delete("tenant_id",tenant.getId().toString());
        List<VendorTenantDO> gameVendorDOS = gameVendorMapper.selectList(Wrappers.<GameVendorDO>lambdaQuery()
                .in(GameVendorDO::getVendorCode, vendorCodes)
        ).stream().map(p->{
            VendorTenantDO vendorTenantDO=BeanUtils.toBean(p, VendorTenantDO.class);
            vendorTenantDO.setId(null);
            vendorTenantDO.setTenantId(tenant.getId());
            vendorTenantDO.setTenantCode(tenant.getCode());
            vendorTenantDO.setTenantName(tenant.getName());
            vendorTenantDO.setCurrency(tenant.getCurrency());
            return  vendorTenantDO;

        }).toList();


        vendorTenantMapper.insertBatch(gameVendorDOS);
    }

    @Override
    public GameRespVO getTenantGameConfig(Long tenantId) {
        GameRespVO result=new GameRespVO();
        var vendorCodes= Optional.ofNullable( vendorTenantMapper.selectList(Wrappers.<VendorTenantDO>lambdaQuery().eq(VendorTenantDO::getTenantId,tenantId)))
               .orElse(new ArrayList<>()).stream().map(VendorTenantDO::getVendorCode).toList();
        result.setVendorCodes(vendorCodes);
        return result;
    }

    @Override
    public Map<GameVendorDO, List<GameInfoDO>> getVendorGames() {

        Map<GameVendorDO, List<GameInfoDO>> collect = Optional.ofNullable(gameInfoMapper.selectList())
                .orElse(new ArrayList<>()).stream().collect(Collectors.groupingBy(p -> {
                            GameVendorDO gameVendorDO = new GameVendorDO();
                            gameVendorDO.setVendorCode(p.getVendorCode());
                            gameVendorDO.setVendorName(p.getVendorName());
                            return gameVendorDO;
                        },
                        Collectors.mapping(p -> {
                            GameInfoDO g = new GameInfoDO();
                            g.setGameCode(p.getGameCode());
                            g.setGameName(p.getGameName());
                            return g;
                        }, Collectors.toList())

                ));
        return collect;
    }

    @Override
    public List<String> getSupportVendors() {
        List<GameVendorDO> gameVendorDOS = gameVendorMapper.selectList(Wrappers.<GameVendorDO>lambdaQuery().eq(GameVendorDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
        if(ObjectUtil.isEmpty(gameVendorDOS)){
            return List.of();
        }

        return gameVendorDOS.stream().map(GameVendorDO::getVendorCode).collect(Collectors.toList());
    }

}