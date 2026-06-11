package cn.iocoder.yudao.module.system.model.api;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GameApiVendorRequest extends PageParam {

    /**
     * 由运营商系统为每个API请求生成的通用唯一标识符 (UUID)。
     */
    @NotEmpty
    private String traceId;

    /**
     * 游戏应该打开的选择语言。
     *
     * 默认：'en'
     */
    @NotEmpty
    private String language;

    /**
     * ISO-4217货币代码（例如，USD）
     */
    @NotEmpty
    private String currency;

    @NotEmpty
    private String vendorCode;

    @Override
    public Integer getPageSize() {
        if (super.getPageSize() == null || super.getPageSize() == 10) {
            return 100;
        }
        return super.getPageSize();
    }

}
