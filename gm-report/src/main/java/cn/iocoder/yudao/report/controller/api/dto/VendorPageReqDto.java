package cn.iocoder.yudao.report.controller.api.dto;




import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;

/**
 * 游戏数据概览- Request VO
 */
@Data
public class VendorPageReqDto extends PageParam {

    /**
     * 商户编码(对应租户商户code)
     */
    private String merchantCode;

    /**
     * 分类编码
     */
    private String categoryCode;
    /**
     * 场馆编码
     */
    private String vendorCode;
    /**
     * 租户ID
     */
    private Integer tid;


}