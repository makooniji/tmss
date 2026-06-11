package cn.iocoder.yudao.module.system.service.info;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.RedisUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo.InfoSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.gameInfo.GameInfoDO;
import cn.iocoder.yudao.module.system.dal.mysql.info.GameInfoMapper;
import cn.iocoder.yudao.module.system.model.api.GameApiVendorRequest;
import jakarta.annotation.Resource;
import org.redisson.api.RScoredSortedSet;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.INFO_NOT_EXISTS;


/**
 * 游戏信息 Service 实现类
 *
 * @author mako
 */
@Service
@Validated
public class GameInfoServiceImpl implements GameInfoService {

    @Resource
    private GameInfoMapper gameInfoMapper;

    @Override
    public Integer createInfo(InfoSaveReqVO createReqVO) {
        // 插入
        GameInfoDO info = BeanUtils.toBean(createReqVO, GameInfoDO.class);
        gameInfoMapper.insert(info);

        // 返回
        return info.getId();
    }

    @Override
    public void updateInfo(InfoSaveReqVO updateReqVO) {
        // 校验存在
        validateInfoExists(updateReqVO.getId());
        // 更新
        GameInfoDO updateObj = BeanUtils.toBean(updateReqVO, GameInfoDO.class);
        gameInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteInfo(Integer id) {
        // 校验存在
        validateInfoExists(id);
        // 删除
        gameInfoMapper.deleteById(id);
    }

    @Override
        public void deleteInfoListByIds(List<Integer> ids) {
        // 删除
        gameInfoMapper.deleteByIds(ids);
        }


    private void validateInfoExists(Integer id) {
        if (gameInfoMapper.selectById(id) == null) {
            throw exception(INFO_NOT_EXISTS);
        }
    }

    @Override
    public GameInfoDO getInfo(Integer id) {
        return gameInfoMapper.selectById(id);
    }

    @Override
    public PageResult<GameInfoDO> getInfoPage(InfoPageReqVO pageReqVO) {
        PageResult<GameInfoDO> gameInfoDOPageResult = gameInfoMapper.selectPage(pageReqVO);
        if (ObjectUtil.isEmpty(gameInfoDOPageResult.getList())) {
            return new PageResult<>();
        }
        gameInfoDOPageResult.getList().forEach(item -> {
            item.setOnlineCount(getOnlineCount(0L, item.getGameCode()));
        });
        return gameInfoDOPageResult;
    }

    @Override
    public PageResult<GameInfoDO> getInfoPageAllVendor(GameApiVendorRequest pageReqVO) {
        PageResult<GameInfoDO> gameInfoDOPageResult = gameInfoMapper.selectPageAllVendor(pageReqVO);
        if (ObjectUtil.isEmpty(gameInfoDOPageResult.getList())) {
            return gameInfoDOPageResult;
        }
        gameInfoDOPageResult.getList().forEach(item -> {
            item.setOnlineCount(getOnlineCount(0L, item.getGameCode()));
        });
        return gameInfoDOPageResult;
    }


    @Override
    public GameInfoDO getInfoByCode(String gameCode) {

        return gameInfoMapper.selectOne("game_code",gameCode);
    }

    @Override
    public void reportOnlineStatus(String gameCode, String username, Long tenantId) {


        // 构造 Key，建议按 租户:游戏 进行拆分，方便后台报表查看
        String redisKey = String.format("online_users:%s:%s", tenantId, gameCode);
        // 1. 添加/更新当前用户的时间戳
        RScoredSortedSet<String> onlineSet = RedisUtils.getClient().getScoredSortedSet(redisKey);

        long now = System.currentTimeMillis();
        long oneHourAgo = now - (60 * 60 * 1000);

        // 3. 异步执行：提升 forward 接口响应速度
        // 添加当前用户（Score 为当前时间戳），并移除 1 小时前未活动的用户
        onlineSet.addAsync(now, username);
        onlineSet.removeRangeByScoreAsync(0, true, oneHourAgo, false);

        // 4. 设置过期时间（兜底，防止僵尸 Key）
        onlineSet.expireAsync(Duration.ofHours(2));


    }

    /**
     * 获取指定游戏的实时在线人数
     */
    @Override
    public Integer getOnlineCount(Long tenantId, String gameCode) {
        String redisKey = String.format("online_users:%s:%s", tenantId, gameCode);
        RScoredSortedSet<String> onlineSet = RedisUtils.getClient().getScoredSortedSet(redisKey);
        // 统计 60 分钟内活跃的人数
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        return onlineSet.count(oneHourAgo, true, Double.MAX_VALUE, true);
    }
}