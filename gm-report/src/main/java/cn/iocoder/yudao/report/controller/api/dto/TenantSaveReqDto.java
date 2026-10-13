package cn.iocoder.yudao.report.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "游戏模块内部服务 - 租户创建/修改 Request VO")
@Data
public class TenantSaveReqDto {


    /**
     * 租户编码
     */
    @NotEmpty
    private String code;
    /**
     * 租户账号
     */
    private String name;

    /**
     * 密码
     */
    private String password;

    /**
     * 国家
     */
    private String country;

    /**
     * 币种
     */
    private String currency;


    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系手机
     */
    private String contactMobile;

    private String email;

    /**
     * 状态 0 开启 1关闭
     */
    private Integer status=0;

    /**
     * 租户ID
     */
    private Long tid;
    /**
     * 0免转 1转账
     */
    private Integer implType;

}
