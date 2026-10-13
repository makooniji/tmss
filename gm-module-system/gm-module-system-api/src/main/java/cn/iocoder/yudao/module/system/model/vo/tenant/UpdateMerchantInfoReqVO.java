package cn.iocoder.yudao.module.system.model.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 修改商户资料 Request VO")
@Data
public class UpdateMerchantInfoReqVO {

    @Schema(description = "租户编号，不传则取当前登录租户")
    private Long id;

    @Schema(description = "回调地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回调地址不能为空")
    private String notifyUrl;

}
