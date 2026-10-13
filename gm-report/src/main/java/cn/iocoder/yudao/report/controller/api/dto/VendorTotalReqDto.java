package cn.iocoder.yudao.report.controller.api.dto;




import lombok.Data;

import java.io.Serializable;

/**
 * 游戏数据概览- Request VO
 */
@Data
public class VendorTotalReqDto implements Serializable {

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 分类编码
     */
    private String categoryCode;
    /**
     * 厂商编码
     */
    private String vendorCode;

    /**
     * 商户编码(对应租户商户code)
     */
    private String merchantCode;
    /**
     * 租户ID
     */
    private Long tid;


}