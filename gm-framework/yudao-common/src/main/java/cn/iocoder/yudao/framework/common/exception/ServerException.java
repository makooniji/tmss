package cn.iocoder.yudao.framework.common.exception;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.MessageUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务器异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ServerException extends RuntimeException {

    /**
     * 全局错误码
     *
     * @see GlobalErrorCodeConstants
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;
    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServerException() {
    }

    public ServerException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public ServerException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public ServerException(String code, Object[] args) {

        this.message=code;
        this.args=args;
    }
    public Integer getCode() {
        return code;
    }

    public ServerException setCode(Integer code) {
        this.code = code;
        return this;
    }




    @Override
    public String getMessage() {
        String message = null;
        if (!StrUtils.isEmpty(this.message)) {
            message = MessageUtils.message(this.message, args);
        }
        if (message == null) {
            message = this.message;
        }
        return message;
    }

    public ServerException setMessage(String message) {
        this.message = message;
        return this;
    }

}
