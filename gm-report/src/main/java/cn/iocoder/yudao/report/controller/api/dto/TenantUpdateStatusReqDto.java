package cn.iocoder.yudao.report.controller.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量更新
 */
@Data
public class TenantUpdateStatusReqDto {
    /**
     * 租户ID
     */
    private Long tid;

    /**
     * 租户编码
     */
    private List<String> codes;
    /**
     * 状态 0 开启 1关闭
     */
    private Integer status=0;

}
