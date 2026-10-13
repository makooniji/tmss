package cn.iocoder.yudao.module.game.convert;

import cn.iocoder.yudao.framework.common.model.game.GameApiTransaction;
import cn.iocoder.yudao.module.system.model.GameRecordsDO;
import cn.iocoder.yudao.module.system.model.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.model.VendorUserFlowDO;
import cn.iocoder.yudao.module.system.model.game.response.BalanceFlowRespVO;
import cn.iocoder.yudao.module.system.model.game.response.BalanceRespVO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GameRecordConvert {


    List<GameApiTransaction> toList(List<GameRecordsDO> list);



    @Mapping(target = "betId", source = "orderNo")
    @Mapping(target = "externalTransactionId", source = "refOrderNo")
    @Mapping(target = "currencyCode", source = "currency")
    @Mapping(target = "roundId", source = "issueNo")
    @Mapping(target = "status", expression = "java(item.getAction().getType())")
    @Mapping(target = "winLoss", expression = "java(item.getWinAmount().subtract(item.getBetAmount()))")
    @Mapping(target = "vendorBetTime", expression = "java(item.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())")
    @Mapping(target = "vendorSettleTime", expression = "java(item.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())")
    GameApiTransaction to(GameRecordsDO item);

    default BalanceFlowRespVO toFlowVo(VendorUserFlowDO flowDO) {
        BalanceFlowRespVO flowVO = new BalanceFlowRespVO();
        flowVO.setTransferAmount(flowDO.getAmount());
        flowVO.setAfterBalance(flowDO.getAfterAmount());
        flowVO.setBeforeBalance(flowDO.getBeforeAmount());
        flowVO.setTransactionId(flowDO.getId());
        flowVO.setReferenceId(flowDO.getReferenceId());
        flowVO.setCurrencyCode(flowDO.getCurrency());
        flowVO.setUsername(flowDO.getUsername());
        flowVO.setTimestamp(Instant.now().toEpochMilli());
        flowVO.setRemark(flowDO.getRemark());
        flowVO.setStatus(flowDO.getStatus());

        return flowVO;
    }

    default BalanceRespVO toBalanceVo(VendorUserBalanceDO flowDO) {
        BalanceRespVO flowVO = new BalanceRespVO();
        flowVO.setAmount(flowDO.getBalance());
        flowVO.setCurrency(flowDO.getCurrency());
        flowVO.setUsername(flowDO.getUsername());
        flowVO.setTimestamp(Instant.now().toEpochMilli());

        return flowVO;
    }
}
