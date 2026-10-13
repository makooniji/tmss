package cn.iocoder.yudao.report.framework.tenant;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 管理端报表接口租户上下文辅助类。
 *
 * <p>前端虽会传 tenant-id，但经网关/nginx 转发到 gm-report 时 header 可能丢失；
 * 此时需从 LoginUser 兜底，与 TenantSecurityWebFilter 在 admin-api 的行为对齐。</p>
 */
@Component
public class AdminReportTenantHelper {

    public <V> V execute(Supplier<V> supplier) {
        return TenantUtils.execute(resolveTenantId(), supplier::get);
    }

    public Long resolveTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }

        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser != null) {
            tenantId = ObjUtil.defaultIfNull(loginUser.getVisitTenantId(), loginUser.getTenantId());
            if (tenantId == null) {
                tenantId = SecurityFrameworkUtils.getLoginUserTenantId();
            }
        }

        if (tenantId == null) {
            throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
                    "请求的租户标识未传递，请进行排查");
        }
        return tenantId;
    }

}
