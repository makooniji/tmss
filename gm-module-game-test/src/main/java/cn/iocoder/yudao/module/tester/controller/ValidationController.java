package cn.iocoder.yudao.module.tester.controller;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.tester.validation.ValidationCaseMapBuilder;
import cn.iocoder.yudao.module.tester.validation.ValidationMeta;
import cn.iocoder.yudao.module.tester.validation.ValidationRunRequest;
import cn.iocoder.yudao.module.tester.validation.ValidationRunner;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tester/map")
@Validated
@Slf4j
@RequiredArgsConstructor
@PermitAll
public class ValidationController {

    private final ValidationRunner validationRunner;
    private final TenantService tenantService;
    /**
     * 查询游戏单元测试
     * @return
     */
    @GetMapping("/game")
    public Map<String, Map<String, List<ValidationMeta>>> getGameMap(){
       return ValidationCaseMapBuilder.build();
    }

    /**
     * 查询体育单元测试
     * @return
     */
    @GetMapping("/sports")
    public Map<String, Map<String, List<ValidationMeta>>> getSportsMap(){
        return ValidationCaseMapBuilder.buildSport();
    }


    /**
     * 运行验证案例
     */
    @PostMapping("/run")
    public String run(@RequestBody ValidationRunRequest request) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if(loginUser==null){
            throw new ServiceException(405,"请先登录");
        }
        TenantDO tenant = tenantService.getTenant(loginUser.getTenantId());
        request.setTenantDO(tenant);
        request.setUserId(loginUser.getId());
        return validationRunner.runAsync(request);
    }
}
