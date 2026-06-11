package cn.iocoder.yudao.module.game.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import cn.iocoder.yudao.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

@Component
@Slf4j
@RequiredArgsConstructor
public class DSManager {


    protected final WebClient webClient;

    private final ApiAccessLogCommonApi apiAccessLogApi;

    @Recover
    public DownstreamResult recover(Exception e, SportContext context, TenantDO tenant, String path, Object request) {
        log.error("--- 下游推送重试耗尽 --- 厂商: {}, 路径: {}, 异常: {}",
                tenant.getCode(), path, e.getMessage());

        // 返回一个特定的失败状态，通知上游不再尝试
        return DownstreamResult.error(context.getTraceId());
    }

    /**
     * 统一推送入口
     *
     * @param context
     * @param tenant  租户信息，用于获取汇率和币种
     * @param path    交易类型，自动匹配 path
     * @param request 填充好的基础模型
     */
    @Retryable(
            retryFor = {GameException.class, TimeoutException.class, IOException.class}, // 哪些异常触发重试
            maxAttempts = 3,                         // 最大重试次数（包括第一次）
            backoff = @Backoff(
                    delay = 500,                        // 初始延迟 500ms
                    multiplier = 2.0,                    // 指数倍数，下次延迟 1s, 2s...
                    maxDelay = 10000                     // 最大延迟 10s
            )
    )
    public DownstreamResult push(SportContext context, TenantDO tenant, String path, Object request) {

        // 2. 执行统一的下游推送逻辑
        try {
            Map<String, Object> body = JsonUtils.convertObject(request, new TypeReference<LinkedHashMap<String, Object>>() {
            });

            context.setApiUrl(tenant.getNotifyUrl() + path);

            String username = body.getOrDefault("username", "").toString();
            if (ObjectUtil.isNotEmpty(username)) {
                Optional.ofNullable(context.getGameVendor())
                        .ifPresent((p) -> body.put("username", context.getPushUsername()));

            }
            // 1. 自动注入公共字段，减少 Adapter 中的重复代码
            body.putIfAbsent("traceId", context.getTraceId());
            body.putIfAbsent("timestamp", context.getTimestamp());
            body.putIfAbsent("currency", context.getWrapCurrency());
            // 移除特定字段
            body.remove("actionEnum");
            Map<String, String> header = new HashMap<>();
            // 1. 构建签名
            String sign = SignUtils.buildSign(body, tenant.getSecretKey());
            header.put("X-Signature", sign);

            context.setHeader(header);
            context.setBody(body);
            JSONObject parseObject;

            log.info("{}，请求参数 paramMap={},header:{}", tenant.getCode(), body, header);
            CompletableFuture<JSONObject> jsonObjectCompletableFuture = doJsonRequest(tenant.getNotifyUrl() + path, body, header, false);
            if (ObjectUtil.isEmpty(jsonObjectCompletableFuture)) {
                throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
            }
            parseObject = jsonObjectCompletableFuture.get(10, TimeUnit.SECONDS);
            DownstreamResult downstreamResult = parseObject.toJavaObject(DownstreamResult.class);
            context.setDsResult(downstreamResult);
            if (!downstreamResult.getStatus().equals(PlatformCodeEnum.OK.getCode())) {
                log.info("{} 下发,结果:{}", tenant.getCode(), downstreamResult);
            }
            if (ObjectUtil.isNotEmpty(downstreamResult.getData())) {
                DownstreamResult.BaseData data = downstreamResult.getData().setUsername(username);
                context.setBalance(data.getBalance());
            }
            buildAccessLog(context);
            return downstreamResult;

        } catch (Exception e) {
            log.error("下游推送异常: type={}, orderId={}", path, request, e);
            throw new GameException(PlatformCodeEnum.DOWN_STREAM_ERROR);
        }
    }
    public void buildAccessLog(SportContext context){
        ApiAccessLogCreateReqDTO accessLog = new ApiAccessLogCreateReqDTO();
        try {
            accessLog.setTraceId(context.getTraceId());
            accessLog.setRequestMethod("POST");
            accessLog.setRequestUrl(context.getApiUrl());
            accessLog.setRequestParams(JsonUtils.toJsonString(context.getBody()));
            accessLog.setResponseBody(JsonUtils.toJsonString(context.getDsResult()));
            accessLog.setBeginTime(Instant.ofEpochMilli(context.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            accessLog.setEndTime(LocalDateTime.now());
            accessLog.setDuration((int) Duration.between(accessLog.getBeginTime(), accessLog.getEndTime()).toMillis());
            accessLog.setApplicationName("gm-gateway");
            accessLog.setOperateName("请求下发");
            accessLog.setOperateType(OperateTypeEnum.GATEWAY.getType());
            accessLog.setUserType(UserTypeEnum.ADMIN.getValue());
            accessLog.setResultMsg(context.getDsResult().getStatus());
            accessLog.setTenantId(1L);

            apiAccessLogApi.createApiAccessLogAsync(accessLog);
        } catch (Throwable th) {
            log.error("[createApiAccessLog][log({}) 发生异常]", toJsonString(accessLog), th);
        }
    }

    public DownstreamResult push(SportContext context, TenantDO tenant, ActionEnum type, Object request, String sign) {

        // 2. 执行统一的下游推送逻辑
        try {
            Map<String, Object> body = BeanUtil.beanToMap(
                    request,
                    new LinkedHashMap<>(),
                    false,
                    true
            );
            // 1. 自动注入公共字段，减少 Adapter 中的重复代码
            body.putIfAbsent("traceId", context.getTraceId());
            body.putIfAbsent("timestamp", context.getTimestamp());
            body.putIfAbsent("currency", context.getCurrency());
            // 移除特定字段
            body.remove("actionEnum");
            Map<String, String> header = new HashMap<>();
            header.put("X-Signature", sign);

            context.setApiUrl(tenant.getNotifyUrl() + type.getPath());
            context.setHeader(header);
            context.setBody(body);

            JSONObject parseObject;

            CompletableFuture<JSONObject> jsonObjectCompletableFuture = doJsonRequest(tenant.getNotifyUrl() + type.getPath(), body, header, false);
            if (ObjectUtil.isEmpty(jsonObjectCompletableFuture)) {
                throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
            }
            parseObject = jsonObjectCompletableFuture.get(30, TimeUnit.SECONDS);
            DownstreamResult downstreamResult = parseObject.toJavaObject(DownstreamResult.class);
            context.setDsResult(downstreamResult);
            if (!downstreamResult.getStatus().equals(PlatformCodeEnum.OK.getCode())) {
                log.info("{} 下发,结果:{}", tenant.getCode(), downstreamResult);
            }
            buildAccessLog(context);
            return downstreamResult;

        } catch (Exception e) {
            log.error("下游推送异常: type={}, orderId={}", type, request, e);
            throw new GameException(PlatformCodeEnum.DOWN_STREAM_ERROR);
        }

    }

    protected CompletableFuture<JSONObject> doJsonRequest(
            String url, Map<String, Object> param, Map<String, String> header, boolean async) {

        WebClient.RequestBodySpec spec = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

        if (header != null) {
            spec.headers(h -> header.forEach(h::add));
        }

        if (async) {
            return spec.bodyValue(param)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(body -> log.info("{}，请求结果 result={}", url, body))
                    .map(JSONObject::parseObject)
                    .toFuture();
        }

        String resp = spec.bodyValue(param)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(10));
        log.info("{}，请求结果 result={}", url, resp);
        return CompletableFuture.completedFuture(JSONObject.parseObject(resp));
    }

}