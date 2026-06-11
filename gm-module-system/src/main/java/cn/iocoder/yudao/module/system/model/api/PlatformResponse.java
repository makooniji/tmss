package cn.iocoder.yudao.module.system.model.api;

import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

@Data
@AllArgsConstructor

public class PlatformResponse<T> implements Serializable {

    private String traceId;
    private String status;
    private String message;
    private T data;

    private PlatformResponse(){
        String traceId1 = MDC.get("traceId");
        this.setTraceId(traceId1);
    }

    public static <T> PlatformResponse<T> error() {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(PlatformCodeEnum.INTERNAL_ERROR.getCode());
        resp.setMessage("Error response.");
        return resp;
    }

    public static <T> PlatformResponse<T> error(T data) {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(PlatformCodeEnum.INTERNAL_ERROR.getCode());
        resp.setMessage("Error response.");
        resp.setData(data);
        return resp;
    }

    public static <T> PlatformResponse<T> error(String code) {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(code);
        resp.setMessage("Error response.");
        return resp;
    }

    public static <T> PlatformResponse<T> error(String code, T data) {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(code);
        resp.setData(data);
        resp.setMessage("Error response.");
        return resp;
    }

    public static <T> PlatformResponse<T> error(String code, String message) {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(code);
        resp.setMessage(message != null ? message : "Error response.");
        return resp;
    }

    // ================== success ==================

    public static <T> PlatformResponse<T> success(T data) {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(PlatformCodeEnum.OK.getCode());
        resp.setData(data);
        resp.setMessage("Successful response.");
        return resp;
    }

    public static <T> PlatformResponse<T> success() {
        PlatformResponse<T> resp = new PlatformResponse<>();
        resp.setStatus(PlatformCodeEnum.OK.getCode());
        resp.setMessage("Successful response.");
        return resp;
    }
}
