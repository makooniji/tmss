package cn.iocoder.yudao.module.system.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum LanguageEnum {
    // 亚洲
    ZH_CN("zh-CN", "zh", "简体中文"),
    ZH_TW("zh-TW", "tw", "繁体中文（台湾）"),
    ZH_HK("zh-HK", "hk", "繁体中文（香港）"),
    EN_US("en-US", "en", "英语（美国）"),
    JA_JP("ja-JP", "jp", "日语"),
    KO_KR("ko-KR", "kr", "韩语"),
    TH_TH("th-TH", "th", "泰语"),
    VI_VN("vi-VN", "vi", "越南语"),
    ID_ID("id-ID", "id", "印尼语"),
    MS_MY("ms-MY", "my", "马来语"),
    KM_KH("km-KH", "km", "高棉语（柬埔寨语）"),
    MY_MM("my-MM", "mm", "缅甸语"),

    // 欧洲 & 其他
    RU_RU("ru-RU", "ru", "俄语"),
    ES_ES("es-ES", "es", "西班牙语"),
    PT_PT("pt-PT", "pt", "葡萄牙语"),
    FR_FR("fr-FR", "fr", "法语"),
    DE_DE("de-DE", "de", "德语"),

    TR_TR("tr-TR", "tr", "土耳其语"),
    HI_IN("hi-IN", "hi", "印地语"),
    AR_SA("ar-SA", "ar", "阿拉伯语"),
    // 默认兜底
    UNKNOWN("unknown", "unknown", "Unknown");


    @EnumValue // MyBatis Plus 存储到数据库的代码 (e.g., zh-CN)
    @JsonValue  // Jackson 序列化输出的代码
    private final String code;

    private final String shortCode; // 简写 (e.g., zh, en)
    private final String desc;      // 描述


    /** 核心方法：支持通过全称或简写查找语言
     * 适配：en-US -> EN_US, en -> EN_US
     */
    public static LanguageEnum findByCode(String lang) {
        if (!StringUtils.hasText(lang)) {
            return EN_US;
        }

        // 统一转为小写并处理分隔符差异
        String target = lang.toLowerCase().replace("_", "-");

        return Arrays.stream(values())
                .filter(e -> e.code.toLowerCase().equals(target) || e.shortCode.toLowerCase().equals(target))
                .findFirst()
                .orElse(EN_US); // 找不到则返回英文
    }

    /**
    校验语言是否支持
     */
    public static boolean isValid(String lang) {
        if (!StringUtils.hasText(lang)) return false;
        String target = lang.toLowerCase().replace("_", "-");
        return Arrays.stream(values())
                .anyMatch(e -> e.code.toLowerCase().equals(target) || e.shortCode.toLowerCase().equals(target));
    }
}
