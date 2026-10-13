package cn.iocoder.yudao.report.controller.api.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
public class TenantStaticRespDto implements Serializable {


    /**
     * 租户ID
      */
    private Long tenantId;


    /**
     * 租户账号
     */
    private String tenantName;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 商户账号
     */
    private String merchantName;


    /**
     * 国家
     */
    private String country;

    /**
     * slot
     */
    private BigDecimal slot;

    /**
     * sport
     */
    private BigDecimal sport;

    /**
     * live
     */

    private BigDecimal live;


    /**
     * lottery
     */
    private BigDecimal lottery;

    /**
     * chess
     */
    private BigDecimal chess;

    /**
     * fishing
     */
    private BigDecimal fishing;
    /**
     * 创建时间
     */
    private Date createTime;
}
