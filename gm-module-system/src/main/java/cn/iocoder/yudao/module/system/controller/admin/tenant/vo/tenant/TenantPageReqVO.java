package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 租户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantPageReqVO extends PageParam {

    /**
     * 商户名
     */
    @Schema(description = "租户名", example = "芋道")
    private String name;
    /**
     * 商户编码
     */
    private String code;
    /**
     * 父商户ID
     */
    private Long parentId;
    /**
     * 商户私钥
     */
    private String tenantKey;

    @Schema(description = "租户状态（0正常 1停用）", example = "1")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;
    /**
     * 指定商户ID
     */
    private Long id;

    private Long package_id;

    /**
     * 可见租户编号列表（权限过滤，由后端设置，前端勿传）
     */
    @Schema(hidden = true)
    private List<Long> ids;

}
