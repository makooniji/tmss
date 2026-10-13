package cn.iocoder.yudao.module.system.model;

import lombok.Data;

/**
 * 厂商用户和厂商信息 DTO
 *
 * @author osca
 */
@Data
public class VendorUserVendorDTO {

    /**
     * 游戏厂商编码
     */
    private String vendorCode;

    /**
     * 实现厂商
     */
    private String adapter;

    /**
     * 上游厂商账号名
     */
    private String vendorUsername;

    /**
     * 厂商代理
     */
    private String vendorAgent;

    /**
     * 厂商密钥
     */
    private String vendorKey;

    /**
     * 厂商API地址
     */
    private String apiUrl;

    /**
     * 厂商渠道
     */
    private String vendorChannel;

    /**
     * 厂商名称
     */
    private String vendorName;

}
