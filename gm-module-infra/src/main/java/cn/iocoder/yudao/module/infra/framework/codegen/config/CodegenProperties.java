package cn.iocoder.yudao.module.infra.framework.codegen.config;

import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenVOTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

@ConfigurationProperties(prefix = "yudao.codegen")
@Validated
@Data
public class CodegenProperties {

    /**
     * 生成的 Java 代码的基础包
     */

    private String basePackage;

    /**
     * 数据库名数组
     */

    private Collection<String> dbSchemas;

    /**
     * 代码生成的前端类型（默认）
     *
     * 枚举 {@link CodegenFrontTypeEnum#getType()}
     */

    private Integer frontType;

    /**
     * 代码生成的 VO 类型
     *
     * 枚举 {@link CodegenVOTypeEnum#getType()}
     */

    private Integer voType;

    /**
     * 是否生成批量删除接口
     */

    private Boolean deleteBatchEnable;

    /**
     * 是否生成单元测试
     */

    private Boolean unitTestEnable;

}
