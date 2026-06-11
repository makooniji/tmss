package cn.iocoder.yudao.framework.mybatis.core;

import cn.iocoder.yudao.framework.mybatis.method.InsertIgnore;
import cn.iocoder.yudao.framework.mybatis.method.InsertIgnoreBatch;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.session.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

public class CustomSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration,Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration,mapperClass,tableInfo);
        methodList.add(new InsertIgnore());
        methodList.add(new InsertIgnoreBatch());

        return methodList;
    }
}