package com.ruoyi.project.quality.mesBatchDetail.service;

import com.ruoyi.common.support.Convert;
import com.ruoyi.project.quality.mesBatchDetail.domain.MesBatchDetail;
import com.ruoyi.project.quality.mesBatchDetail.mapper.MesBatchDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MES个体扫码记录 服务层实现
 *
 * @author sj
 * @date 2019-11-28
 */
@Service
public class MesBatchDetailServiceImpl implements IMesBatchDetailService {
    @Autowired
    private MesBatchDetailMapper mesBatchDetailMapper;

    /**
     * 查询MES个体扫码记录信息
     *
     * @param id MES个体扫码记录ID
     * @return MES个体扫码记录信息
     */
    @Override
    public MesBatchDetail selectMesBatchDetailById(Integer id) {
        return mesBatchDetailMapper.selectMesBatchDetailById(id);
    }

    /**
     * 查询MES个体扫码记录列表
     *
     * @param mesBatchDetail MES个体扫码记录信息
     * @return MES个体扫码记录集合
     */
    @Override
    public List<MesBatchDetail> selectMesBatchDetailList(MesBatchDetail mesBatchDetail) {
        return mesBatchDetailMapper.selectMesBatchDetailList(mesBatchDetail);
    }

    /**
     * 新增MES个体扫码记录
     *
     * @param mesBatchDetail MES个体扫码记录信息
     * @return 结果
     */
    @Override
    public int insertMesBatchDetail(MesBatchDetail mesBatchDetail) {
        return mesBatchDetailMapper.insertMesBatchDetail(mesBatchDetail);
    }

    /**
     * 修改MES个体扫码记录
     *
     * @param mesBatchDetail MES个体扫码记录信息
     * @return 结果
     */
    @Override
    public int updateMesBatchDetail(MesBatchDetail mesBatchDetail) {
        return mesBatchDetailMapper.updateMesBatchDetail(mesBatchDetail);
    }

    /**
     * 删除MES个体扫码记录对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteMesBatchDetailByIds(String ids) {
        return mesBatchDetailMapper.deleteMesBatchDetailByIds(Convert.toStrArray(ids));
    }

}
