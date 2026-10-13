package cn.iocoder.yudao.report.controller.api.dto;




import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;

import java.util.List;

/**
 * 游戏数据概览- Request VO
 */
@Data
public class MerchantPageReqDto extends PageParam {

    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 租户ID
     */
    private List<Long> tid;

    /**
     * 商户检索
     */
    private String idOrName;


    /**
     * 币种
     */
    private String currency;
    /**
     * 商户状态
     */
    private Integer status;
    /**
     * 运营状态
     */
    private Integer operateStatus;

}