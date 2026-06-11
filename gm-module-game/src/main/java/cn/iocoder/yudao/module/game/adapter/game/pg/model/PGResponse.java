package cn.iocoder.yudao.module.game.adapter.game.pg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PGResponse<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> PGResponse<T> success(T data) {
        PGResponse<T> resp = new PGResponse<>();
        resp.setCode(1);
        resp.setMsg("success");
        resp.setData(data);
        return resp;
    }

    public static <T> PGResponse<T> success() {
        PGResponse<T> resp = new PGResponse<>();
        resp.setCode(1);
        resp.setMsg("success");
        return resp;
    }

}
