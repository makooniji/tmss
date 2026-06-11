package cn.iocoder.yudao.report.controller.merchant.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;

import java.util.List;


@Data
public class MerchantStaticPageReqVO extends PageParam {

    /**
     * 会员ID
     */
    private Long userId;
    /**
     * 会员账号
     */
    private String username;

    /**
     * 开始时间
     */
    private String days;
    /**
     * 结束时间
     */
    private String endDays;
    /**
     * 代理编码
     */
    private List<String> agentCodes;
    /**
     * 租户ID
     */
    private List<Long> tenantIds;


}