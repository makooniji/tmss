package cn.iocoder.yudao.module.tester.validation;

import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.DownstreamResult;
import lombok.experimental.UtilityClass;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@UtilityClass
public class SpelValidationEngine {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static ValidationCheckResult validate(
            DownstreamResult result,
            ValidationCaseRule config,
            SportContext ctx
    ) {
        try {
            StandardEvaluationContext context =
                    new StandardEvaluationContext(result);

            // ⭐ 注入变量
            if (ctx.getVars() != null) {
                ctx.getVars().forEach(context::setVariable);
            }

            Boolean pass = PARSER
                    .parseExpression(config.getRule())
                    .getValue(context, Boolean.class);

            if (Boolean.TRUE.equals(pass)) {
                return new ValidationCheckResult(
                        true,
                        config.getCaseCode(),
                        null,
                        "SUCCESS"
                );
            }

            return new ValidationCheckResult(
                    false,
                    config.getCaseCode(),
                    config.getRule(),
                    config.getMessage()
            );

        } catch (Exception e) {
            return new ValidationCheckResult(
                    false,
                    config.getCaseCode(),
                    config.getRule(),
                    "SpEL执行异常: " + e.getMessage()
            );
        }
    }
}