package com.ruoyi.project.quality.mesBatch.domain;

import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * MES个体追溯建档表 tab_mes_batch
 * 
 * @author sj
 * @date 2019-11-28
 */
public class MesBatch extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	
	/** 主键，自增长 */
	private Integer id;
	/** 公司id */
	private Integer companyId;
	/** 工单号 */
	private String workCode;
	/** 产线Id */
	private Integer lineId;
	/** 产品编码 */
	private String pnCode;
	/** 扫码标记(0、未扫，1、已扫) */
	private String sign;
	/** 产品建档信息 */
	@Excel(name = "产品建档信息", type = Excel.Type.EXPORT)
	private String pnMainInfo;
	/** 工单产品完成标记(0、未完成，1、当24道扫码都完成设置为1状态为已完成) */
	private Integer status;

	public Integer getLineId() {
		return lineId;
	}

	public void setLineId(Integer lineId) {
		this.lineId = lineId;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Integer getId() 
	{
		return id;
	}
	public void setCompanyId(Integer companyId) 
	{
		this.companyId = companyId;
	}

	public Integer getCompanyId() 
	{
		return companyId;
	}
	public void setWorkCode(String workCode) 
	{
		this.workCode = workCode;
	}

	public String getWorkCode() 
	{
		return workCode;
	}
	public void setPnCode(String pnCode) 
	{
		this.pnCode = pnCode;
	}

	public String getPnCode() 
	{
		return pnCode;
	}
	public void setSign(String sign) 
	{
		this.sign = sign;
	}

	public String getSign() 
	{
		return sign;
	}
	public void setPnMainInfo(String pnMainInfo) 
	{
		this.pnMainInfo = pnMainInfo;
	}

	public String getPnMainInfo() 
	{
		return pnMainInfo;
	}
	public void setStatus(Integer status) 
	{
		this.status = status;
	}

	public Integer getStatus() 
	{
		return status;
	}

	@Override
	public String toString() {
		return "MesBatch{" +
				"id=" + id +
				", companyId=" + companyId +
				", workCode='" + workCode + '\'' +
				", lineId=" + lineId +
				", pnCode='" + pnCode + '\'' +
				", sign='" + sign + '\'' +
				", pnMainInfo='" + pnMainInfo + '\'' +
				", status=" + status +
				'}';
	}
}
