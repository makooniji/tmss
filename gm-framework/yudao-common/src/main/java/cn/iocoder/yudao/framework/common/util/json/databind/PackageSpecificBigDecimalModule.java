package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.util.AntPathMatcher; // 引入 Spring 的 Ant 路径匹配器

import java.math.BigDecimal;
import java.util.List;

public class PackageSpecificBigDecimalModule extends Module {

    private final String targetPattern;
    // 实例化 Spring 的 Ant 匹配器
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher(".");

    public PackageSpecificBigDecimalModule(String targetPattern) {
        this.targetPattern = targetPattern;
    }

    @Override
    public String getModuleName() {
        return "PackageSpecificBigDecimalModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                    SerializationConfig config,
                    BeanDescription beanDesc,
                    List<BeanPropertyWriter> beanProperties) {

                String beanClassName = beanDesc.getBeanClass().getName();

                // 使用 AntPathMatcher 进行复杂的包名通配符匹配
                if (PATH_MATCHER.match(targetPattern, beanClassName)) {
                    for (BeanPropertyWriter writer : beanProperties) {
                        if (writer.getType().isTypeOrSubTypeOf(BigDecimal.class)) {
                            writer.assignSerializer(new BigDecimalTwoDegreeSerializer());
                        }
                    }
                }
                return beanProperties;
            }
        });
    }
}