package cn.iocoder.yudao.module.tester.validation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationCaseRule {

    /**
     * 3.04.01
     */
    private String caseCode;

    /**
     * JLS / PPS
     */
    private String vendor;

    /**
     * SpEL 表达式
     */
    private String rule;

    /**
     * 失败提示
     */
    private String message;
}