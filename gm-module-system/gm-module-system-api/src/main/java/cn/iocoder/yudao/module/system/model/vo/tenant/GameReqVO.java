package cn.iocoder.yudao.module.system.model.vo.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
    private List<String> gameCodes;
}
