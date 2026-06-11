package cn.iocoder.yudao.module.system.dal.mysql.records;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.RecordsPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.RecordsTotalRespVO;
import cn.iocoder.yudao.module.system.controller.admin.records.vo.VendorTotalRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameIdleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户游戏记录 Mapper
 *
 * @author osca
 */
@Mapper
public interface IdleMapper extends BaseMapperX<GameIdleDO> {


}