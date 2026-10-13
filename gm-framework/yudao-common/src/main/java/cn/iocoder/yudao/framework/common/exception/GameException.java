package cn.iocoder.yudao.framework.common.exception;



import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏厂商业务逻辑异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class GameException extends RuntimeException {

    /**
     * 业务错误码
     *
     */
    private String code;
    private String message;


    /**
     * 空构造方法，避免反序列化问题
     */
    public GameException() {
    }

    public GameException(PlatformCodeEnum errorCode) {
        this.code = errorCode.getCode();

    }
    public GameException(PlatformCodeEnum errorCode,String message) {
        this.code = errorCode.getCode();
        this.message = message;
    }

    public GameException(String code) {
        this.code = code;

    }
    public GameException(String code,String message) {
        this.code = code;
        this.message = message;

    }

}
