package cn.iocoder.yudao.module.tester.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationMeta {

    /**
     * 1.01.01
     */
    private String caseCode;

    /**
     * wallet/balance
     */
    private String api;

    /**
     * 中文描述
     */
    private String result;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 耗时
     */
    private Long duration;
    /**
     * 头部
     */
    private Map<String, String> header;
    /**
     * 请求体
     */
    private String body;
    /**
     * 备注
     */
    private String remark;

    /**
     * 预期结果
     */
    private String expectedResult;
    /**
     * 实际结果
     */
    private String actualResult;

    public ValidationMeta(String caseCode, String api, String result) {
        this.caseCode = caseCode;
        this.api = api;
        this.result = result;
    }
}