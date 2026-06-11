package cn.iocoder.yudao.module.system.dal.mysql.info;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoPageReqVO;
import cn.iocoder.yudao.module.system.model.api.GameApiVendorRequest;
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
                .betweenIfPresent(GameInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GameInfoDO::getId));
    }



    default PageResult<GameInfoDO> selectPageAllVendor(GameApiVendorRequest reqVO) {
        LambdaQueryWrapperX<GameInfoDO> wrapperX = new LambdaQueryWrapperX<GameInfoDO>()
                .eqIfPresent(GameInfoDO::getVendorCode, reqVO.getVendorCode())
                .eq(GameInfoDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .findContainsOrAll(GameInfoDO::getSupportLang, reqVO.getLanguage())
                .findContainsOrAll(GameInfoDO::getSupportCurrency, reqVO.getCurrency())
                .orderByDesc(GameInfoDO::getId);

        return selectPage(reqVO, wrapperX);

    }
}