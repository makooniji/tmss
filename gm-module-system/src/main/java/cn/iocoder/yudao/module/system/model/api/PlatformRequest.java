package cn.iocoder.yudao.module.system.model.api;

import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import lombok.Data;

import java.util.Map;

@Data
public class PlatformRequest {


    private String vendorCode;

    /**
     * 上游厂商
     */
    private GameVendorDO gameVendor;

    private String action;
    private Map<String, Object> extraData;

    private Map<String, String> header;
}
