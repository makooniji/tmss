package cn.iocoder.yudao.module.game.adapter.game.one.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

@Data
@AllArgsConstructor

public class JLSResponse<T> implements Serializable {

    private String traceId;
    private String status;
    private T data;

    private JLSResponse(){
        String traceId1 = MDC.get("traceId");
        this.setTraceId(traceId1);
    }


    // ================== success ==================

    public static <T> JLSResponse<T> success(T data) {
        JLSResponse<T> resp = new JLSResponse<>();
        resp.setStatus("SC_OK");
        resp.setData(data);
        return resp;
    }

    public static <T> JLSResponse<T> success() {
        JLSResponse<T> resp = new JLSResponse<>();
        resp.setStatus("SC_OK");
        return resp;
    }
}
