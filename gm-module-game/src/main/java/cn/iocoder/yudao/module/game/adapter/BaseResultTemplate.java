package cn.iocoder.yudao.module.game.adapter;

import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.mysql.vendor.GameVendorMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class BaseResultTemplate extends BasePullTemplate {

    @Resource
    private GameVendorMapper gameVendorMapper;

    @Data
    @AllArgsConstructor
    public static class TimeRange {
        private String start;
        private String end;
    }

    @Override
    protected Integer interval() {
        return 2000;
    }

    // 拉单入口
    public final List<GameRecordsDO> execute(String params) {
        GameVendorDO vendor = loadVendor();
        if (vendor == null) return null;

        TimeRange time = buildTimeRange(params, vendor);

        return pull(time.getStart(), time.getEnd(), 1, getPageSize(), vendor);

    }

    public GameVendorDO loadVendor() {
        return gameVendorMapper.selectOne(
                new LambdaQueryWrapper<GameVendorDO>()
                        .eq(GameVendorDO::getVendorCode, this.getPlatform()));
    }

    protected abstract TimeRange buildTimeRange(String params, GameVendorDO vendor);


}