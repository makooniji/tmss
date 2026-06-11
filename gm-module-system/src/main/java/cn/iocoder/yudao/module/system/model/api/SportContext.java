package cn.iocoder.yudao.module.system.model.api;

import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendortenant.VendorTenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class SportContext {

    private String transferId;
    private String externalTransactionId;
    /**
     * 上游厂商会员账号
     */
    private String pullUsername;
    /**
     * 下游商户账号名
     */
    private String pushUsername;

    private VendorUserDO user;
    private String orderId;
    private String currency;
    /**
     * 转换的货币
     */
    private String wrapCurrency;
    private String sign;
    @NotNull
    private Double currencyRate;
    private String roundId;
    private String txns;
    private String gameCode;
    /**
     * 账户余额
     */
    private BigDecimal balance=BigDecimal.ZERO;
    private Long timestamp;
    private String traceId;
    /**
     * 上游厂商信息
     */
    private GameVendorDO gameVendor;
    /**
     * 租户开启的厂商
     */
    private VendorTenantDO vendorTenant;

    private String vendorCode;

    private TenantDO tenant;

    /**
     * 动态变量池（关键）
     */
    private Map<String, Object> vars = new HashMap<>();

    private String apiUrl;
    private Map<String,String> header;

    private Long adminId;

    private Map<String,Object> body;

    private DownstreamResult dsResult;


}