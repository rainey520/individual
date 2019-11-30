package com.ruoyi.project.quality.mesBatch.service;

import com.ruoyi.common.support.Convert;
import com.ruoyi.framework.jwt.JwtUtil;
import com.ruoyi.project.quality.mesBatch.domain.MesBatch;
import com.ruoyi.project.quality.mesBatch.mapper.MesBatchMapper;
import com.ruoyi.project.system.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * MES个体追溯建档 服务层实现
 *
 * @author sj
 * @date 2019-11-28
 */
@Service
public class MesBatchServiceImpl implements IMesBatchService {
    @Autowired
    private MesBatchMapper mesBatchMapper;

    /**
     * 查询MES个体追溯建档信息
     *
     * @param id MES个体追溯建档ID
     * @return MES个体追溯建档信息
     */
    @Override
    public MesBatch selectMesBatchById(Integer id) {
        return mesBatchMapper.selectMesBatchById(id);
    }

    /**
     * 查询MES个体追溯建档列表
     *
     * @param mesBatch MES个体追溯建档信息
     * @return MES个体追溯建档集合
     */
    @Override
    public List<MesBatch> selectMesBatchList(MesBatch mesBatch) {
        User user = JwtUtil.getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        mesBatch.setCompanyId(user.getCompanyId());
        return mesBatchMapper.selectMesBatchList(mesBatch);
    }

    /**
     * 新增MES个体追溯建档
     *
     * @param mesBatch MES个体追溯建档信息
     * @return 结果
     */
    @Override
    public int insertMesBatch(MesBatch mesBatch) {
        return mesBatchMapper.insertMesBatch(mesBatch);
    }

    /**
     * 修改MES个体追溯建档
     *
     * @param mesBatch MES个体追溯建档信息
     * @return 结果
     */
    @Override
    public int updateMesBatch(MesBatch mesBatch) {
        return mesBatchMapper.updateMesBatch(mesBatch);
    }

    /**
     * 删除MES个体追溯建档对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteMesBatchByIds(String ids) {
        return mesBatchMapper.deleteMesBatchByIds(Convert.toStrArray(ids));
    }

}
