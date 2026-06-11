package cn.iocoder.yudao.module.game.adapter.game.tj.model;

import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TJResponse<T> implements Serializable {
    private Map<String, Object> header;
    private T data;

    public static <T> TJResponse<T> success(T data) {
        TJResponse<T> resp = new TJResponse<>();
        resp.setHeader(Map.of("code", 0,
                "msg", "success", "timestamp", DateUtils.ts()));
        resp.setData(data);
        return resp;
    }

    public static <T> TJResponse<T> error(int code, String msg) {
        TJResponse<T> resp = new TJResponse<>();
        resp.setHeader(Map.of("code", code,
                "msg", msg, "timestamp", DateUtils.ts()));
        return resp;
    }

}
