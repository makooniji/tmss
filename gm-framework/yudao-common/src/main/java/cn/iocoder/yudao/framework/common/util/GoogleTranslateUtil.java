package cn.iocoder.yudao.framework.common.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Google 通用翻译工具。
 */
@Slf4j
public final class GoogleTranslateUtil {

    private static final String GOOGLE_TRANSLATE_URL =
            "https://translate.googleapis.com/translate_a/single";

    private static final int MAX_RETRY_COUNT = 2;
    private static final long RETRY_BASE_DELAY_MS = 1000L;
    private static final long MIN_REQUEST_INTERVAL_MS = 1000L;
    private static final AtomicLong LAST_REQUEST_TIME = new AtomicLong(0L);

    private GoogleTranslateUtil() {
    }

    /**
     * 翻译文本。
     */
    public static String translate(String gameName, String sourceLanguage, String targetLanguage) {
        if (StringUtils.isBlank(gameName)
                || StringUtils.isBlank(sourceLanguage)
                || StringUtils.isBlank(targetLanguage)) {
            return gameName;
        }
        String source = toGoogleLanguage(sourceLanguage);
        String target = toGoogleLanguage(targetLanguage);
        if (source.equalsIgnoreCase(target)) {
            return gameName;
        }

        for (int retry = 0; retry <= MAX_RETRY_COUNT; retry++) {
            try {
                waitForRequestSlot();
                log.info("Google翻译请求：{} -> {}，内容：{}", source, target, gameName);

                String url = new URIBuilder(GOOGLE_TRANSLATE_URL)
                        .addParameter("client", "gtx")
                        .addParameter("sl", source)
                        .addParameter("tl", target)
                        .addParameter("dt", "t")
                        .addParameter("q", gameName)
                        .build()
                        .toString();

                String response = HttpUtil.get(url, 10000);
                String translated = parseTranslateResponse(response);
                if (StringUtils.isNotBlank(translated)) {
                    log.info("Google翻译完成：{} -> {}，结果：{}", source, target, translated);
                    return translated;
                }
                log.warn("Google翻译返回空结果，gameName={}，sourceLanguage={}，targetLanguage={}，retry={}",
                        gameName, source, target, retry);
            } catch (Exception e) {
                if (retry >= MAX_RETRY_COUNT) {
                    log.error("Google翻译最终失败，gameName={}，sourceLanguage={}，targetLanguage={}，retry={}",
                            gameName, source, target, retry, e);
                    return gameName;
                }
                long delay = calculateRetryDelay(retry);
                log.warn("Google翻译失败，准备重试，gameName={}，retry={}，delay={}ms，error={}",
                        gameName, retry + 1, delay, e.getMessage());
                sleep(delay);
            }
        }
        return gameName;
    }

    /**
     * 解析英文名称：原名是英文直接用，是中文则翻译为英文。
     */
    public static String resolveEnglishName(String gameName) {
        if (StringUtils.isBlank(gameName)) {
            return gameName;
        }
        if (!containsChinese(gameName)) {
            return gameName;
        }
        return translate(gameName, "zh-CN", "en");
    }

    /** 解析中文名称：原名是中文直接用，否则从英文翻译。 */
    public static String resolveChineseName(String gameName) {
        if (StringUtils.isBlank(gameName)) {
            return gameName;
        }
        if (containsChinese(gameName)) {
            return gameName;
        }
        return translate(gameName, "en", "zh-CN");
    }

    /** 解析越南语名称：从中文或英文翻译。 */
    public static String resolveVietnameseName(String gameName) {
        if (StringUtils.isBlank(gameName)) {
            return gameName;
        }
        String source = containsChinese(gameName) ? "zh-CN" : "en";
        return translate(gameName, source, "vi");
    }

    /**
     * 构建 game_info.lang JSON。
     * 英文原名：英文字段直填，其他语言才翻译。
     * 中文原名：中文字段直填，英文字段翻译，其他语言从中文翻译。
     */
    public static String buildGameNameLangJson(String gameName, String... localeTags) {
        if (StringUtils.isBlank(gameName) || localeTags == null || localeTags.length == 0) {
            return "{}";
        }
        JSONObject lang = new JSONObject(true);
        int total = localeTags.length;
        int index = 0;
        for (String localeTag : localeTags) {
            if (StringUtils.isBlank(localeTag)) {
                continue;
            }
            index++;
            String tag = localeTag.trim();
            log.info("构建 lang [{}/{}]，游戏：{}，locale：{}", index, total, gameName, tag);
            String value = resolveFieldValue(gameName, tag);
            JSONObject field = new JSONObject(true);
            field.put("gameName", value);
            lang.put(tag, field);
        }
        return lang.toJSONString();
    }

    /**
     * 已有中英文名时直填对应语种，其他语种优先用英文名翻译。
     * 与 {@link #buildGameNameLangJson(String, String...)} 互不影响（供 TJ 等双语源场景）。
     *
     * @param nameZh 中文名，可空
     * @param nameEn 英文名，可空；其他语种翻译优先用它
     */
    public static String buildGameNameLangJsonWithZhEn(String nameZh, String nameEn, String... localeTags) {
        if (localeTags == null || localeTags.length == 0) {
            return "{}";
        }
        String zh = StringUtils.trimToEmpty(nameZh);
        String en = StringUtils.trimToEmpty(nameEn);
        if (StringUtils.isAllBlank(zh, en)) {
            return "{}";
        }
        // 其他语种：优先英文源
        String translateSource = StringUtils.isNotBlank(en) ? en : zh;
        String sourceLang = StringUtils.isNotBlank(en) ? "en" : "zh-CN";
        String label = StringUtils.isNotBlank(en) ? en : zh;

        JSONObject lang = new JSONObject(true);
        int total = localeTags.length;
        int index = 0;
        for (String localeTag : localeTags) {
            if (StringUtils.isBlank(localeTag)) {
                continue;
            }
            index++;
            String tag = localeTag.trim();
            log.info("构建 lang(双语文) [{}/{}]，游戏：{}，locale：{}", index, total, label, tag);
            String value;
            if (isEnglishLocale(tag)) {
                value = StringUtils.isNotBlank(en) ? en
                        : translate(zh, "zh-CN", "en");
            } else if (isChineseLocale(tag)) {
                value = StringUtils.isNotBlank(zh) ? zh
                        : translate(en, "en", "zh-CN");
            } else {
                value = translate(translateSource, sourceLang, toGoogleLanguage(tag));
            }
            JSONObject field = new JSONObject(true);
            field.put("gameName", value);
            lang.put(tag, field);
        }
        return lang.toJSONString();
    }

    private static String resolveFieldValue(String gameName, String localeTag) {
        boolean chinese = containsChinese(gameName);
        if (isEnglishLocale(localeTag)) {
            return chinese ? translate(gameName, "zh-CN", "en") : gameName;
        }
        if (isChineseLocale(localeTag)) {
            return chinese ? gameName : translate(gameName, "en", "zh-CN");
        }
        String source = chinese ? "zh-CN" : "en";
        return translate(gameName, source, toGoogleLanguage(localeTag));
    }

    private static boolean isEnglishLocale(String localeTag) {
        return localeTag.trim().toLowerCase(Locale.ROOT).startsWith("en");
    }

    private static boolean isChineseLocale(String localeTag) {
        String value = localeTag.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("zh") || "hk".equals(value);
    }

    public static boolean containsChinese(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    /**
     * 厂商语言简码 -> BCP47 locale，如 en -> en-US，zh -> zh-CN
     */
    public static String toLocale(String code) {
        if (StringUtils.isBlank(code)) {
            return "en-US";
        }
        return switch (code.trim().toLowerCase(Locale.ROOT)) {
            case "en" -> "en-US";
            case "zh" -> "zh-CN";
            case "hk" -> "zh-HK";
            case "id", "ind" -> "id-ID";
            case "pt" -> "pt-PT";
            case "th" -> "th-TH";
            case "vi" -> "vi-VN";
            default -> code.trim();
        };
    }

    private static String toGoogleLanguage(String localeOrCode) {
        if (StringUtils.isBlank(localeOrCode)) {
            return localeOrCode;
        }
        String value = localeOrCode.trim();
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "en", "en-us", "en-gb", "en-au" -> "en";
            case "zh", "zh-cn", "zh-hans" -> "zh-CN";
            case "zh-hk", "hk" -> "zh-TW";
            case "zh-tw", "zh-hant" -> "zh-TW";
            case "vi", "vi-vn" -> "vi";
            case "id", "ind", "id-id" -> "id";
            case "pt", "pt-pt", "pt-br" -> "pt";
            case "th", "th-th" -> "th";
            case "ms", "ms-my" -> "ms";
            case "ja", "ja-jp" -> "ja";
            case "ko", "ko-kr" -> "ko";
            case "es", "es-es", "es-mx" -> "es";
            case "fr", "fr-fr" -> "fr";
            case "de", "de-de" -> "de";
            case "ru", "ru-ru" -> "ru";
            case "ar", "ar-sa" -> "ar";
            case "hi", "hi-in" -> "hi";
            default -> {
                // 前端传 en-US / xx-YY 时，取主语言子标签给谷歌（zh-* 已在上面处理）
                int dash = value.indexOf('-');
                if (dash > 0) {
                    yield value.substring(0, dash).toLowerCase(Locale.ROOT);
                }
                yield value;
            }
        };
    }

    /**
     * BCP47 / 业务 locale -> 谷歌翻译语言码。
     * 例：en-US -> en，zh-CN -> zh-CN，ms-MY -> ms。
     */
    public static String toGoogleLanguageCode(String localeOrCode) {
        return toGoogleLanguage(localeOrCode);
    }

    private static String parseTranslateResponse(String response) {
        if (StringUtils.isBlank(response)) {
            return null;
        }
        JSONArray root = JSON.parseArray(response);
        if (root == null || root.isEmpty()) {
            return null;
        }
        JSONArray parts = root.getJSONArray(0);
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        StringBuilder translated = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            JSONArray part = parts.getJSONArray(i);
            if (part != null && !part.isEmpty()) {
                String text = part.getString(0);
                if (StringUtils.isNotBlank(text)) {
                    translated.append(text);
                }
            }
        }
        return translated.isEmpty() ? null : translated.toString();
    }

    private static void waitForRequestSlot() {
        while (true) {
            long now = System.currentTimeMillis();
            long last = LAST_REQUEST_TIME.get();
            long next = Math.max(now, last + MIN_REQUEST_INTERVAL_MS);
            if (LAST_REQUEST_TIME.compareAndSet(last, next)) {
                long wait = next - now;
                if (wait > 0) {
                    sleep(wait);
                }
                return;
            }
        }
    }

    private static long calculateRetryDelay(int retry) {
        long exponential = RETRY_BASE_DELAY_MS * (1L << retry);
        long jitter = ThreadLocalRandom.current().nextLong(200, 800);
        return exponential + jitter;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
