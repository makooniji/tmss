package cn.iocoder.yudao.module.game.adapter.game.base;

import cn.iocoder.yudao.module.game.adapter.game.AbstractGamingPlatform;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.system.model.api.PlatformRequest;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;
import cn.iocoder.yudao.module.system.model.api.SportContext;
import cn.iocoder.yudao.module.system.model.api.push.*;
import cn.iocoder.yudao.module.system.model.api.push.sports.DSBetRequest;
import cn.iocoder.yudao.module.system.model.api.push.sports.DSSettle;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 非体育核心基础抽象类
 */
@Slf4j
@Service
public abstract class BaseAdapter extends AbstractGamingPlatform {

    @Resource
    protected DSManager dsManager;

    protected abstract PlatformResponse<Object> buildBalanceResult(SportContext sportContext);

    protected abstract PlatformResponse<Object> buildIdleResult(SportContext sportContext);


    protected abstract DSBetRequest buildBet(PlatformRequest param, SportContext context);
    protected abstract List<DSRollback> buildRefund(PlatformRequest param, SportContext context);
    protected abstract DSBetResultRequest buildBetResult(PlatformRequest param, SportContext context);
    protected abstract List<DSSettle> buildSettle(PlatformRequest param, SportContext context);
    protected abstract DSAdjustment buildAdjustment(PlatformRequest param,  SportContext context);
    protected abstract DSDebit buildDebit(PlatformRequest param, SportContext context);
    protected abstract DSCredit buildCredit(PlatformRequest param, SportContext context);

}