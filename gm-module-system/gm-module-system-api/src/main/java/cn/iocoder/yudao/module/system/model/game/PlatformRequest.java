package cn.iocoder.yudao.module.system.model.game;


import cn.iocoder.yudao.module.system.model.GameVendorDO;
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

    private Map<String, String> paramMap;

    private String body;
}
