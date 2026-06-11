package cn.iocoder.yudao.module.tester.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationCheckResult {

    private boolean success;
    private String caseCode;
    private String failRule;
    private String message;
}