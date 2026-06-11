package cn.iocoder.yudao.module.system.exception;

import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 游戏异常处理
 *
 * @author osca
 */
@RestControllerAdvice(basePackages = {"cn.iocoder.yudao.gateway.controller", "cn.iocoder.yudao.module.game.*"})
@AllArgsConstructor
@Slf4j
@Order(1)
public class GameExceptionHandler {
    @ExceptionHandler(value = GameException.class)
    public PlatformResponse<Object> handleGameException(GameException ex) {
        String traceId = MDC.get("traceId");
        // 打日志（一定要打，不然排查没意义）
        log.error("[traceId={}][GameException]", traceId, ex);
        return PlatformResponse.error(ex.getCode(), ex.getMessage()).setTraceId(traceId);
    }

    /**
     * 处理被包装成 RuntimeException 的 GameException
     * 兼容旧代码中 try-catch 后 throw new RuntimeException(e) 的模式
     */
    @ExceptionHandler(value = RuntimeException.class)
    public PlatformResponse<Object> handleRuntimeException(RuntimeException ex) {
        String traceId = MDC.get("traceId");
        // 如果 cause 是 GameException，则按 GameException 处理
        if (ex.getCause() instanceof GameException gameException) {
            return PlatformResponse.error(gameException.getCode(), gameException.getMessage()).setTraceId(traceId);
        }
        // 否则继续抛出，让其他处理器处理
        throw ex;
    }

    @ExceptionHandler(value = Exception.class)
    public PlatformResponse<Object> defaultExceptionHandler(HttpServletRequest req, Throwable ex) {
        String traceId = MDC.get("traceId");
        // 情况二：处理异常
        log.error("[defaultExceptionHandler]", ex);
        return PlatformResponse.error(PlatformCodeEnum.UNKNOWN_ERROR.getCode()).setTraceId(traceId);
    }
}
