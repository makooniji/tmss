package cn.iocoder.yudao.module.system.mapper;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.model.game.GameApiVendorRequest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.model.GameInfoDO;
import cn.iocoder.yudao.module.system.model.vo.gameinfo.InfoPageReqVO;

import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏信息 Mapper
 *
 * @author mako
 */
@Mapper
public interface GameInfoMapper extends BaseMapperX<GameInfoDO> {

    default PageResult<GameInfoDO> selectPage(InfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GameInfoDO>()
                .eqIfPresent(GameInfoDO::getVendorCode, reqVO.getVendorCode())
                .likeIfPresent(GameInfoDO::getGameName, reqVO.getGameName())
                .eqIfPresent(GameInfoDO::getGameCode, reqVO.getGameCode())
                .eqIfPresent(GameInfoDO::getCateId, reqVO.getCateId())
                .eqIfPresent(GameInfoDO::getMaintain, reqVO.getMaintain())
                .eqIfPresent(GameInfoDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(GameInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GameInfoDO::getId));
    }



    default PageResult<GameInfoDO> selectPageAllVendor(GameApiVendorRequest reqVO) {
        LambdaQueryWrapperX<GameInfoDO> wrapperX = new LambdaQueryWrapperX<GameInfoDO>()
                .eqIfPresent(GameInfoDO::getVendorCode, reqVO.getVendorCode())
                .eq(!Boolean.TRUE.equals(reqVO.getIncludeDisabled()), GameInfoDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .findContainsOrAll(GameInfoDO::getSupportLang, reqVO.getLanguage())
                .findContainsOrAll(GameInfoDO::getSupportCurrency, reqVO.getCurrency())
                .orderByDesc(GameInfoDO::getId);

        return selectPage(reqVO, wrapperX);

    }
}