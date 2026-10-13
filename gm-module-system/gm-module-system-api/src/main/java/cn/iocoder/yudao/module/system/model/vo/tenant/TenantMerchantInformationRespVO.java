package cn.iocoder.yudao.module.system.model.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 商户信息 Response VO")
@Data
public class TenantMerchantInformationRespVO {

    @Schema(description = "登录 ID（商户编码）")
    private String code;

    @Schema(description = "总代理（父商户 ID）")
    private Long parentId;

    @Schema(description = "货币")
    private List<String> currency;

    @Schema(description = "备注（联系人）")
    private String contactName;

    @Schema(description = "回调地址")
    private String notifyUrl;

    @Schema(description = "白名单 IP")
    private List<String> ipAddress;

    @Schema(description = "API Token")
    private String tenantKey;

    @Schema(description = "Auth Token")
    private String secretKey;

    @Schema(description = "当前登录 IP")
    private String loginIp;

}
