package com.ruoyi.project.quality.mesBatchDetail.mapper;

import com.ruoyi.project.quality.mesBatchDetail.domain.MesBatchDetail;
import java.util.List;	

/**
 * MES个体扫码记录 数据层
 * 
 * @author sj
 * @date 2019-11-28
 */
public interface MesBatchDetailMapper 
{
	/**
     * 查询MES个体扫码记录信息
     * 
     * @param id MES个体扫码记录ID
     * @return MES个体扫码记录信息
     */
	public MesBatchDetail selectMesBatchDetailById(Integer id);
	
	/**
     * 查询MES个体扫码记录列表
     * 
     * @param mesBatchDetail MES个体扫码记录信息
     * @return MES个体扫码记录集合
     */
	public List<MesBatchDetail> selectMesBatchDetailList(MesBatchDetail mesBatchDetail);
	
	/**
     * 新增MES个体扫码记录
     * 
     * @param mesBatchDetail MES个体扫码记录信息
     * @return 结果
     */
	public int insertMesBatchDetail(MesBatchDetail mesBatchDetail);
	
	/**
     * 修改MES个体扫码记录
     * 
     * @param mesBatchDetail MES个体扫码记录信息
     * @return 结果
     */
	public int updateMesBatchDetail(MesBatchDetail mesBatchDetail);
	
	/**
     * 删除MES个体扫码记录
     * 
     * @param id MES个体扫码记录ID
     * @return 结果
     */
	public int deleteMesBatchDetailById(Integer id);
	
	/**
     * 批量删除MES个体扫码记录
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deleteMesBatchDetailByIds(String[] ids);
	
}