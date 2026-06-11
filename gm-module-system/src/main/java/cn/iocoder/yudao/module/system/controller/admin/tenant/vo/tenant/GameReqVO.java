package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;


/**
 * 游戏配置
 */
@Data
public class GameReqVO {


    @NotNull
    private Long tenantId;

    /**
     * 选择需要开启的厂商
     */
    @NotNull
    private List<String> vendorCodes;
    /**
     * 选择需要开启的游戏
     */
    @NotNull
    private List<String> gameCodes;
}
