package cn.iocoder.yudao.module.system.runner;

import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import cn.iocoder.yudao.module.system.dal.mysql.category.CategoryMapper;
import cn.iocoder.yudao.module.system.dal.mysql.categorysub.CategorySubMapper;
import cn.iocoder.yudao.module.system.service.category.CategoryService;
import cn.iocoder.yudao.module.system.service.categorysub.CategorySubService;
import com.fhs.trans.service.impl.DictionaryTransService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;


@Component
@Slf4j
public class EasyTransRunner implements ApplicationRunner {
    @Autowired  //注入字典翻译服务
    private DictionaryTransService dictionaryTransService;
    @Autowired
    private CategoryService easyQuery;
    @Autowired
    private CategorySubService subCateGory;
    @Override
    public void run(ApplicationArguments args) {

        List<CategoryDO> list = easyQuery.getList();
        HashMap<String, String> transMap = new HashMap<>();
        list.forEach(item-> {
                    transMap.put(item.getId().toString(),item.getTitle());
                }
        );
        dictionaryTransService.refreshCache("cate-gory",transMap);


        List<CategorySubDO> subDOS = subCateGory.getList();
        HashMap<String, String> subMap = new HashMap<>();
        subDOS.forEach(item-> {
                    subMap.put(item.getId().toString(),item.getTitle());
                }
        );
        dictionaryTransService.refreshCache("sub-cate-gory",subMap);
        log.info("数据字典加载完成");
    }
}