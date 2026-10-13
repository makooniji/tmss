package cn.iocoder.yudao.framework.common.util.servlet;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 客户端工具类
 *
 * @author osca
 */
@Slf4j
public class ServletUtils extends JakartaServletUtil {

    /**
     * 返回 JSON 字符串
     *
     * @param response 响应
     * @param object   对象，会序列化成 JSON 字符串
     */
    @SuppressWarnings("deprecation") // 必须使用 APPLICATION_JSON_UTF8_VALUE，否则会乱码
    public static void writeJSON(HttpServletResponse response, Object object) {
        String content = JsonUtils.toJsonString(object);
        JakartaServletUtil.write(response, content, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * @param request 请求
     * @return ua
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "";
    }

    /**
     * 获得请求
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return getUserAgent(request);
    }

    public static String getClientIP() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return JakartaServletUtil.getClientIP(request);
    }

    public static boolean isJsonRequest(ServletRequest request) {
        return StrUtil.startWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE);
    }

    public static String getBody(HttpServletRequest request) {
        // 只有在 json 请求在读取，因为只有 CacheRequestBodyFilter 才会进行缓存，支持重复读取
        if (isJsonRequest(request)) {
            return JakartaServletUtil.getBody(request);
        }
        return null;
    }

    public static byte[] getBodyBytes(HttpServletRequest request) {
        // 只有在 json 请求在读取，因为只有 CacheRequestBodyFilter 才会进行缓存，支持重复读取
        if (isJsonRequest(request)) {
            return JakartaServletUtil.getBodyBytes(request);
        }
        return null;
    }

    public static String getClientIP(HttpServletRequest request) {
        return JakartaServletUtil.getClientIP(request);
    }

    public static Map<String, String> getParamMap(HttpServletRequest request) {
        return JakartaServletUtil.getParamMap(request);
    }

    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        return JakartaServletUtil.getHeaderMap(request);
    }

    public static Map<String, Object> getBodyMap(HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            String contentType = request.getContentType();
            log.info("请求方式:{},{}", contentType, request.getHeader("Content-Encoding"));

            if (StrUtil.isNotEmpty(contentType)) {
                String lowerContentType = contentType.toLowerCase();

                // 1. JSON
                if (lowerContentType.contains("application/json")) {
                    String body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
                    log.info("body={}", body);
                    if (StrUtil.isNotEmpty(body)) {
                        Map<String, Object> map = JsonUtils.parseObject(body, new TypeReference<LinkedHashMap<String, Object>>() {});
                        if (map != null) {
                            result.putAll(map);
                        }
                    }
                }
                // 2. FORM (application/x-www-form-urlencoded)
                else if (lowerContentType.contains("application/x-www-form-urlencoded")) {
                    Map<String, String[]> params = request.getParameterMap();
                    params.forEach((k, v) -> {
                        if (v == null || v.length == 0) {
                            result.put(k, null);
                        } else if (v.length == 1) {
                            result.put(k, v[0]);
                        } else {
                            // 多值转换为 List<String>，便于 JSON 序列化和统一处理
                            result.put(k, Arrays.asList(v));
                        }
                    });
                }
                // 3. MULTIPART (multipart/form-data)
                else if (lowerContentType.contains("multipart/form-data")) {
                    Collection<Part> parts = request.getParts();
                    for (Part part : parts) {
                        String name = part.getName();
                        String submittedFileName = part.getSubmittedFileName();

                        Object value;
                        if (StrUtil.isNotEmpty(submittedFileName)) {
                            // 文件类型：保留 Part 对象本身（或按需改为 part.getInputStream() / byte[]）
                            value = part;
                        } else {
                            // 普通文本字段
                            value = IoUtil.read(part.getInputStream(), StandardCharsets.UTF_8);
                        }

                        // 处理同名 Key（如多文件上传或多选框），自动合并为 List
                        if (result.containsKey(name)) {
                            Object existing = result.get(name);
                            if (existing instanceof List) {
                                ((List<Object>) existing).add(value);
                            } else {
                                List<Object> list = new ArrayList<>();
                                list.add(existing);
                                list.add(value);
                                result.put(name, list);
                            }
                        } else {
                            result.put(name, value);
                        }
                    }
                }
            } else {
                // 没有 Content-Type（如普通 GET 请求），直接获取 URL 拼接参数
                Map<String, String[]> params = request.getParameterMap();
                params.forEach((k, v) -> {
                    if (v == null || v.length == 0) {
                        result.put(k, null);
                    } else if (v.length == 1) {
                        result.put(k, v[0]);
                    } else {
                        result.put(k, Arrays.asList(v));
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("request parse error", e);
        }

        return result;
    }


}
