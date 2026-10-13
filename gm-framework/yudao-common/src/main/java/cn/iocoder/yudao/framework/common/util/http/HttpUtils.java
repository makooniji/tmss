package cn.iocoder.yudao.framework.common.util.http;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.TableMap;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;

import org.apache.hc.core5.util.TimeValue;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * HTTP 工具类
 *
 * @author osca
 */
@Slf4j
public class HttpUtils extends HttpUtil {

    /**
     * 编码 URL 参数
     *
     * @param value 参数
     * @return 编码后的参数
     */
    public static String encodeUtf8(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 解码 URL 参数
     *
     * @param value 参数
     * @return 解码后的参数
     */
    public static String decodeUtf8(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public static String replaceUrlQuery(String url, String key, String value) {
        UrlBuilder builder = UrlBuilder.of(url, Charset.defaultCharset());
        // 先移除
        TableMap<CharSequence, CharSequence> query = (TableMap<CharSequence, CharSequence>)
                ReflectUtil.getFieldValue(builder.getQuery(), "query");
        query.remove(key);
        // 后添加
        builder.addQuery(key, value);
        return builder.build();
    }

    public static String removeUrlQuery(String url) {
        if (!StrUtil.contains(url, '?')) {
            return url;
        }
        UrlBuilder builder = UrlBuilder.of(url, Charset.defaultCharset());
        // 移除 query、fragment
        builder.setQuery(null);
        builder.setFragment(null);
        return builder.build();
    }

    /**
     * 拼接 URL
     * <p>
     * copy from Spring Security OAuth2 的 AuthorizationEndpoint 类的 append 方法
     *
     * @param base     基础 URL
     * @param query    查询参数
     * @param keys     query 的 key，对应的原本的 key 的映射。例如说 query 里有个 key 是 xx，实际它的 key 是 extra_xx，则通过 keys 里添加这个映射
     * @param fragment URL 的 fragment，即拼接到 # 中
     * @return 拼接后的 URL
     */
    public static String append(String base, Map<String, ?> query, Map<String, String> keys, boolean fragment) {
        UriComponentsBuilder template = UriComponentsBuilder.newInstance();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(base);
        URI redirectUri;
        try {
            // assume it's encoded to start with (if it came in over the wire)
            redirectUri = builder.build(true).toUri();
        } catch (Exception e) {
            // ... but allow client registrations to contain hard-coded non-encoded values
            redirectUri = builder.build().toUri();
            builder = UriComponentsBuilder.fromUri(redirectUri);
        }
        template.scheme(redirectUri.getScheme()).port(redirectUri.getPort()).host(redirectUri.getHost())
                .userInfo(redirectUri.getUserInfo()).path(redirectUri.getPath());

        if (fragment) {
            StringBuilder values = new StringBuilder();
            if (redirectUri.getFragment() != null) {
                String append = redirectUri.getFragment();
                values.append(append);
            }
            for (String key : query.keySet()) {
                if (values.length() > 0) {
                    values.append("&");
                }
                String name = key;
                if (keys != null && keys.containsKey(key)) {
                    name = keys.get(key);
                }
                values.append(name).append("={").append(key).append("}");
            }
            if (values.length() > 0) {
                template.fragment(values.toString());
            }
            UriComponents encoded = template.build().expand(query).encode();
            builder.fragment(encoded.getFragment());
        } else {
            for (String key : query.keySet()) {
                String name = key;
                if (keys != null && keys.containsKey(key)) {
                    name = keys.get(key);
                }
                template.queryParam(name, "{" + key + "}");
            }
            template.fragment(redirectUri.getFragment());
            UriComponents encoded = template.build().expand(query).encode();
            builder.query(encoded.getQuery());
        }
        return builder.build().toUriString();
    }

    public static String[] obtainBasicAuthorization(HttpServletRequest request) {
        String clientId;
        String clientSecret;
        // 先从 Header 中获取
        String authorization = request.getHeader("Authorization");
        authorization = StrUtil.subAfter(authorization, "Basic ", true);
        if (StringUtils.hasText(authorization)) {
            authorization = Base64.decodeStr(authorization);
            clientId = StrUtil.subBefore(authorization, ":", false);
            clientSecret = StrUtil.subAfter(authorization, ":", false);
            // 再从 Param 中获取
        } else {
            clientId = request.getParameter("client_id");
            clientSecret = request.getParameter("client_secret");
        }

        // 如果两者非空，则返回
        if (StrUtil.isNotEmpty(clientId) && StrUtil.isNotEmpty(clientSecret)) {
            return new String[]{clientId, clientSecret};
        }
        return null;
    }

    /**
     * HTTP post 请求，基于 {@link cn.hutool.http.HttpUtil} 实现
     * <p>
     * 为什么要封装该方法，因为 HttpUtil 默认封装的方法，没有允许传递 headers 参数
     *
     * @param url         URL
     * @param headers     请求头
     * @param requestBody 请求体
     * @return 请求结果
     */
    public static String postJson(String url, Map<String, String> headers, String requestBody) {
        try (HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)
                .body(requestBody)
                .execute()) {
            return response.body();
        }
    }

    /**
     * HTTP get 请求，基于 {@link cn.hutool.http.HttpUtil} 实现
     * <p>
     * 为什么要封装该方法，因为 HttpUtil 默认封装的方法，没有允许传递 headers 参数
     *
     * @param url     URL
     * @param headers 请求头
     * @return 请求结果
     */
    public static String getJson(String url, Map<String, String> headers) {
        try (HttpResponse response = HttpRequest.get(url)
                .addHeaders(headers)
                .execute()) {
            return response.body();
        }
    }

    // 1. 创建 HttpClient 5 (注意：TimeValue 和 Builder 的包名变了)
    private final static CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(200)
                    .setMaxConnPerRoute(50)
                    .build())
            // 关键：避免连接重置 (Connection Reset) 的驱逐策略，Spring Boot 3 支持这样写
            .evictIdleConnections(TimeValue.of(1, TimeUnit.MINUTES))
            .build();

    // 2. 声明为全局单例 RestClient
    private final static RestClient restClient = RestClient.builder()
            // Spring Boot 3 的这个构造函数完美支持上面的 HttpClient 5
            .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
            .build();

    /**
     * 1. JSON 请求实现 (RestClient 版)
     */
    public static CompletableFuture<String> doJsonRequest(
            String url, Map<String, Object> param, Map<String, String> header, boolean async) {

        // 抽取公共的同步请求逻辑
        Supplier<String> requestSupplier = () -> {
            String resp = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders -> {
                        if (header != null) header.forEach(httpHeaders::add);
                    })
                    .body(param) // RestClient 会自动将 Map 序列化为 JSON
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("上游请求错误: {}, URL: {}, body：{}", errorBody, url,param);
                        throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),errorBody);
                    })
                    .body(String.class);

            log.info("{}，请求结果 result={}", url, resp);
            return resp;
        };

        // 根据 async 标志分流
        if (async) {
            // 异步：提交到线程池（生产环境建议指定自定义的线程池，如 ForkJoinPool 或自定义 Executor）
            return CompletableFuture.supplyAsync(requestSupplier);
        } else {
            // 同步：直接执行并包裹为已完成的 Future
            return CompletableFuture.completedFuture(requestSupplier.get());
        }
    }


    public static CompletableFuture<JSONObject> doFormRequest(
            String url, Map<String, Object> param, Map<String, String> header, boolean async) {

        // 1. 构建表单数据
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        if (param != null) {
            param.forEach((k, v) -> form.add(k, String.valueOf(v)));
        }

        // 2. 抽取公共的同步请求逻辑
        // 利用 Java 17 的 lambda 和局部变量推导，逻辑非常清晰
        Supplier<JSONObject> requestSupplier = () -> {
            String resp = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(httpHeaders -> {
                        if (header != null) header.forEach(httpHeaders::add);
                    })
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // RestClient 的异常处理可以直接抛出，不需要像 Mono 那样包裹
                        String errorBody = new String(response.getBody().readAllBytes());
                        log.error("下游 4xx 错误: {}, URL: {}", errorBody, url);
                        throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
                    })
                    .body(String.class);

            log.info("{}，请求结果 result={}", url, resp);
            return JSONObject.parseObject(resp);
        };

        // 3. 根据 async 标志决定是否走线程池异步
        if (async) {
            // Java 17+ 配合虚拟线程（如果你用的是 Java 21 效果更佳），或自定义线程池
            return CompletableFuture.supplyAsync(requestSupplier);
        } else {
            return CompletableFuture.completedFuture(requestSupplier.get());
        }
    }

}
