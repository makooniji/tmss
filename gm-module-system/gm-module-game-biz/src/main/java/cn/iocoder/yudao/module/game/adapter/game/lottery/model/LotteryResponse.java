package cn.iocoder.yudao.module.game.adapter.game.lottery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

@Data
@AllArgsConstructor

public class LotteryResponse<T> implements Serializable {

    private String traceId;
    private Integer code;
    private String msg;
    private T data;

    private LotteryResponse() {
        String traceId1 = MDC.get("traceId");
        this.setTraceId(traceId1);
    }


    // ================== success ==================

    public static <T> LotteryResponse<T> success(T data) {
        LotteryResponse<T> resp = new LotteryResponse<>();
        resp.setMsg("success");
        resp.setData(data);
        return resp;
    }

    public static <T> LotteryResponse<T> success() {
        LotteryResponse<T> resp = new LotteryResponse<>();
        resp.setMsg("success");
        return resp;
    }
}
