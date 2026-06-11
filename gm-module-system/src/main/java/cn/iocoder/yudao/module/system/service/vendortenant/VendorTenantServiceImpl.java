package cn.iocoder.yudao.module.system.service.vendortenant;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.GameRespVO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo.VendorTenantSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendortenant.VendorTenantDO;
import cn.iocoder.yudao.module.system.dal.mysql.info.GameInfoMapper;
import cn.iocoder.yudao.module.system.dal.mysql.vendor.GameVendorMapper;
import cn.iocoder.yudao.module.system.dal.mysql.vendortenant.VendorTenantMapper;
import cn.iocoder.yudao.module.system.model.api.GameApiVendorRequest;
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
    public VendorTenantDO getVendorByCode(String vendor) {

        return vendorTenantMapper.selectOne(Wrappers.<VendorTenantDO>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(vendor), VendorTenantDO::getVendorCode,vendor));
    }

    @Override
    public VendorTenantDO getVendorByTenantCode(String agent) {
       return vendorTenantMapper.selectOne("tenant_code",agent);
    }

    @Override
    public List<VendorTenantDO> getListVendors(GameApiVendorRequest gameApiVendorRequest) {
        LambdaQueryWrapperX<VendorTenantDO> wrapperX = new LambdaQueryWrapperX<VendorTenantDO>()
                .eq(VendorTenantDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .findContainsOrAll(VendorTenantDO::getSupportLang, gameApiVendorRequest.getLanguage())
                .findContainsOrAll(VendorTenantDO::getCurrency, gameApiVendorRequest.getCurrency())
                ;

        return vendorTenantMapper.selectList(wrapperX);
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

}