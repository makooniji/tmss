package cn.iocoder.yudao.framework.permission.convert;


import cn.iocoder.yudao.framework.permission.model.ConfigDO;
import cn.iocoder.yudao.framework.permission.model.vo.ConfigRespVO;
import cn.iocoder.yudao.framework.permission.model.vo.ConfigSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConfigConvert {

    ConfigConvert INSTANCE = Mappers.getMapper(ConfigConvert.class);



    List<ConfigRespVO> convertList(List<ConfigDO> list);

    @Mapping(source = "configKey", target = "key")
    ConfigRespVO convert(ConfigDO bean);

    @Mapping(source = "key", target = "configKey")
    ConfigDO convert(ConfigSaveReqVO bean);

}
