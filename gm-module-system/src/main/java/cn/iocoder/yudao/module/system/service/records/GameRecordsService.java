package cn.iocoder.yudao.module.system.service.records;

import java.util.*;

import cn.iocoder.yudao.module.system.controller.admin.records.vo.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import jakarta.validation.*;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 用户游戏记录 Service 接口
 *
 * @author osca
 */
public interface GameRecordsService {

    /**
     * 创建用户游戏记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRecords(@Valid RecordsSaveReqVO createReqVO);

    /**
     * 更新用户游戏记录
     *
     * @param updateReqVO 更新信息
     */
    void updateRecords(@Valid RecordsSaveReqVO updateReqVO);

    /**
     * 删除用户游戏记录
     *
     * @param id 编号
     */
    void deleteRecords(Long id);

    /**
    * 批量删除用户游戏记录
    *
    * @param ids 编号
    */
    void deleteRecordsListByIds(List<Long> ids);

    /**
     * 获得用户游戏记录
     *
     * @param id 编号
     * @return 用户游戏记录
     */
    GameRecordsDO getRecords(Long id);

    /**
     * 获得用户游戏记录分页
     *
     * @param pageReqVO 分页查询
     * @return 用户游戏记录分页
     */
    PageResult<GameRecordsDO> getRecordsPage(RecordsPageReqVO pageReqVO);

    Boolean exists(String transferId);

    GameRecordsDO getRecordsByOrderId(String orderId);

    void update(Wrapper<GameRecordsDO> set);

    PageResult<RecordsTotalRespVO> getRecordsTotalPage(@Valid RecordsTotalPageReqVO pageReqVO);

    /**
     * 查询场馆统计
     * @param pageReqVO
     * @return
     */
    VendorTotalRespVO getVendorDataTotal(@Valid RecordsTotalReqVO pageReqVO);
}