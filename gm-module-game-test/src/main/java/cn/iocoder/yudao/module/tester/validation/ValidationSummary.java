package cn.iocoder.yudao.module.tester.validation;

import lombok.Data;

import java.util.List;

@Data
public class ValidationSummary {
    private String traceId;
    private Boolean success;
    private List<ValidationResult> details;
}
