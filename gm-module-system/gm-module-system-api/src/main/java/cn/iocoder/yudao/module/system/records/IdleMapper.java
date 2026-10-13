package cn.iocoder.yudao.module.system.records;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.model.GameIdleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户游戏记录 Mapper
 *
 * @author osca
 */
@Mapper
public interface IdleMapper extends BaseMapperX<GameIdleDO> {


}