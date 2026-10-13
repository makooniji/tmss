package cn.iocoder.yudao.module.game.adapter;


import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.model.game.GameApiRequest;
import cn.iocoder.yudao.framework.common.model.game.GameApiRtp;
import cn.iocoder.yudao.framework.common.model.game.GameApiTransfer;
import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.module.system.model.GameRecordsDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorUserDO;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;

import java.math.BigDecimal;
import java.util.List;

public interface IPlatform {

    String getVendorCode();

    PlatformResponse<Object> balance(ActionEnum type, PlatformRequest requestData);
    /**
     * 执行交易操作
     *
     * @param type        交易类型
     * @param requestData 请求数据
     * @return 平台响应
     */
    PlatformResponse<Object> forward(ActionEnum type, PlatformRequest requestData);


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
     * @param gameUser
     * @return
     */
    PlatformResponse<String> getGameLink(GameVendorDO vendor, GameApiRequest request, VendorUserDO gameUser);

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


    PlatformResponse<Boolean> setRtp(GameVendorDO gameVendor, GameApiRtp body);
}