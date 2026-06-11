package cn.iocoder.yudao.framework.mybatis.i18n;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.LangDO;
import cn.iocoder.yudao.framework.web.LangContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Intercepts({
    @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@Component
@Slf4j
public class I18nInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 执行 SQL 映射，获取原始结果
        Object result = invocation.proceed();
        if (ObjectUtil.isEmpty(result)) return result;

        // 2. 处理集合或单体对象
        if (result instanceof List) {
            for (Object item : (List<?>) result) {
                translateObject(item);
            }
        } else {
            translateObject(result);
        }
        return result;
    }

    private void translateObject(Object target) {
        // 只处理继承了 LangDO 且包含国际化逻辑的对象
        if (!(target instanceof LangDO)) return;
        
        LangDO langTarget = (LangDO) target;
        Map<String, Map<String,String>> langMap = langTarget.getLang(); // 获取数据库存的 {"en": "Game", "zh": "游戏"}
        if (ObjectUtil.isEmpty(langMap)) return;

        // 获取当前上下文语言 (如通过 I18nContextHolder)
        String currentLang = LangContextHolder.get();

        // 3. 反射查找带 @I18nField 的字段并替换值
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            if (field.isAnnotationPresent(I18nField.class)) {
                I18nField annotation = field.getAnnotation(I18nField.class);
                String key = annotation.key(); // 获取注解定义的 Key，如 "gameName"
                
                // 从 langMap 中找到对应语言的值
                String translatedValue = langMap.getOrDefault(currentLang,new HashMap<>()).getOrDefault(key,"");
                if (StrUtil.isNotBlank(translatedValue)) {
                    field.setAccessible(true);
                    field.set(target, translatedValue);
                }
            }
        });
    }
}