package cn.iocoder.yudao.module.system.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ResultTypeEnum {

    WIN( "WIN", "胜利"),
    LOSE( "LOSE", "失败"),
    BET_WIN( "BET_WIN", "下注赢"),
    BET_LOSE( "BET_LOSE", "下注输"),
    END("END", "结束");

    /**
     * 数字/状态 Code
     */
    @JsonValue
    private final String code;

    /**
     * 原始字符串标识
     */

    private final String value;



    // 静态缓存提升查找性能（缓存 Map<Code, Enum>）
    private static final Map<String, ResultTypeEnum> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(ResultTypeEnum::getCode, Function.identity()));


    /**
     * 根据 数字 code 获取枚举
     */
    public static ResultTypeEnum getByCode(String code) {
        return code == null ? null : CODE_MAP.get(code);
    }


}