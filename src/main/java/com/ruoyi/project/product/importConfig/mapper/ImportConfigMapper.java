package com.ruoyi.project.product.importConfig.mapper;

import com.ruoyi.project.product.importConfig.domain.ImportConfig;
import org.apache.ibatis.annotations.Param;


/**
 * 导入配置 数据层
 * 
 * @author sj
 * @date 2019-07-03
 */
public interface ImportConfigMapper 
{
	/**
     * 查询导入配置信息
     *
	 * @param companyId 公司id
     * @param cType 导入配置cType
     * @return 导入配置信息
     */
	public ImportConfig selectImportConfigByType(@Param("companyId") Integer companyId, @Param("cType") Integer cType);

	
	/**
     * 新增导入配置
     * 
     * @param importConfig 导入配置信息
     * @return 结果
     */
	public int insertImportConfig(ImportConfig importConfig);
	

	/**
     * 删除导入配置
     *
	 * @param companyId 公司id
     * @param cType 导入配置cType
     * @return 结果
     */
	public int deleteImportConfigByType(@Param("companyId") Integer companyId, @Param("cType") Integer cType);

	/**
	 * 通过规则名称查询配置信息
	 * @param companyId 公司id
	 * @param conRule 规则信息
	 * @return 结果
	 */
	ImportConfig selectConfigByRule(@Param("companyId") Integer companyId, @Param("conRule") String conRule);

	/**
	 * 修改配置信息
	 * @param importConfig 配置信息
	 * @return 结果
	 */
	int updateConfig(ImportConfig importConfig);
}