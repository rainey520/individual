package com.ruoyi.project.quality.mesBatch.service;

import com.ruoyi.project.quality.mesBatch.domain.MesBatch;
import java.util.List;

/**
 * MES个体追溯建档 服务层
 * 
 * @author sj
 * @date 2019-11-28
 */
public interface IMesBatchService 
{
	/**
     * 查询MES个体追溯建档信息
     * 
     * @param id MES个体追溯建档ID
     * @return MES个体追溯建档信息
     */
	public MesBatch selectMesBatchById(Integer id);
	
	/**
     * 查询MES个体追溯建档列表
     * 
     * @param mesBatch MES个体追溯建档信息
     * @return MES个体追溯建档集合
     */
	public List<MesBatch> selectMesBatchList(MesBatch mesBatch);
	
	/**
     * 新增MES个体追溯建档
     * 
     * @param mesBatch MES个体追溯建档信息
     * @return 结果
     */
	public int insertMesBatch(MesBatch mesBatch);
	
	/**
     * 修改MES个体追溯建档
     * 
     * @param mesBatch MES个体追溯建档信息
     * @return 结果
     */
	public int updateMesBatch(MesBatch mesBatch);
		
	/**
     * 删除MES个体追溯建档信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deleteMesBatchByIds(String ids);
	
}
