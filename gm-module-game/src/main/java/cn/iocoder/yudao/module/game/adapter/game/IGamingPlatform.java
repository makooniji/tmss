package cn.iocoder.yudao.module.game.adapter.game;

import cn.iocoder.yudao.module.game.adapter.IPlatform;
import cn.iocoder.yudao.module.system.dal.dataobject.records.GameRecordsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.model.api.GameApiRequest;
import cn.iocoder.yudao.module.system.model.api.GameApiTransfer;
import cn.iocoder.yudao.module.system.model.api.PlatformRequest;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IGamingPlatform extends IPlatform {

    /**
     * 执行交易操作
     *
     * @param actionEnum
     * @param requestData 请求数据
     * @param request
     * @return 平台响应
     */
    PlatformResponse<Object> forward(ActionEnum actionEnum, PlatformRequest requestData);

    /**
     * 创建厂商会员账号
     *
     * @param vendor
     * @param request
     * @return
     */
    PlatformResponse<String> createMember(GameVendorDO vendor, GameApiRequest request);


    /**
     * 获取厂商登录链接
     *
     * @param vendor
     * @param request
     * @return
     */
    PlatformResponse<String> getGameLink(GameVendorDO vendor, GameApiRequest request);

    /**
     * 退出厂商游戏
     *
     * @param vendor
     * @param account
     * @return
     */
    PlatformResponse<BigDecimal> exitGame(GameVendorDO vendor, String account);

    /**
     * 钱包转入转出
     *
     * @param vendor
     * @param transfer
     * @return
     */

    PlatformResponse<BigDecimal> transfer(GameVendorDO vendor, GameApiTransfer transfer);

    /**
     * 查询注单
     *
     * @param vendor
     * @param startTime
     * @param endTime
     * @return
     */
    List<GameRecordsDO> history(GameVendorDO vendor, String startTime, String endTime);


}