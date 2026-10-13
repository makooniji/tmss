package cn.iocoder.yudao.module.game.adapter.game.base;


import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.framework.common.model.game.push.DSAdjustment;
import cn.iocoder.yudao.framework.common.model.game.push.DSCredit;
import cn.iocoder.yudao.framework.common.model.game.push.DSDebit;
import cn.iocoder.yudao.framework.common.model.game.push.DSRollback;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSBetRequest;
import cn.iocoder.yudao.framework.common.model.game.push.sports.DSSettle;
import cn.iocoder.yudao.module.game.adapter.BaseAbstractPlatform;
import cn.iocoder.yudao.module.system.model.game.DSBetResultRequest;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.model.game.SportContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 非体育核心基础抽象类
 */
@Slf4j
@Service
public abstract class BaseAdapter extends BaseAbstractPlatform {

    protected abstract PlatformResponse<Object> buildBalanceResult(SportContext sportContext);

    protected abstract PlatformResponse<Object> buildIdleResult(SportContext sportContext);


    protected abstract List<DSBetRequest> buildBet(PlatformRequest param, SportContext context);

    protected abstract List<DSRollback> buildRefund(PlatformRequest param, SportContext context);

    protected abstract List<DSBetResultRequest> buildBetResult(PlatformRequest param, SportContext context);

    protected abstract List<DSSettle> buildSettle(PlatformRequest param, SportContext context);

    protected abstract DSAdjustment buildAdjustment(PlatformRequest param, SportContext context);

    protected abstract DSDebit buildDebit(PlatformRequest param, SportContext context);

    protected abstract DSCredit buildCredit(PlatformRequest param, SportContext context);

}