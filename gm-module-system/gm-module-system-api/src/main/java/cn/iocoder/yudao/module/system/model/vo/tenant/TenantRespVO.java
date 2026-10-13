package cn.iocoder.yudao.module.system.model.vo.tenant;

import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 租户 Response VO")
@Data
@TableName(autoResultMap = true)
public class TenantRespVO {

    /**
     * 租户ID
     */
    private Long id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 父租户编号
     */
    private Long parentId;

    /**
     * 租户编码
     */
    private String code;

    /**
     * 商户KEY
     */
    private String tenantKey;
    /**
     * 商户私钥
     */
    private String secretKey;
    /**
     * 开通的货币
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> currency;

    /**
     * 商户状态
     */
    private Integer status;

    /**
     * 账号数量
     */
    private Integer accountCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String creator;

    /**
     * 接入方式0 免转 1转入
     */
    private Integer implType = 0;

    /**
     * 开启的场馆
     */
    private List<String> vendorCodes;
    /**
     * 开启的游戏
     */
    private List<String> gameCodes;
    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * ip加白
     */
    private List<String> ipAddress;

    private Long packageId;

    private Long tid;
    private Integer operateStatus;
    private LocalDateTime operateTime;

}
