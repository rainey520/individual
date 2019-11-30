package com.ruoyi.project.production.devWorkOrder.service;

import com.ruoyi.project.production.devWorkOrder.domain.DevWorkOrder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 工单 服务层
 *
 * @author zqm
 * @date 2019-04-12
 */
public interface IDevWorkOrderService {
    /**
     * 查询工单信息
     *
     * @param id 工单ID
     * @return 工单信息
     */
    public DevWorkOrder selectDevWorkOrderById(Integer id);

    /**
     * 查询工单列表
     *
     * @param devWorkOrder 工单信息
     * @return 工单集合
     */
    public List<DevWorkOrder> selectDevWorkOrderList(DevWorkOrder devWorkOrder);

    /**
     * 新增工单
     *
     * @param devWorkOrder 工单信息
     * @return 结果
     */
    public int insertDevWorkOrder(DevWorkOrder devWorkOrder) throws Exception;

    /**
     * 修改工单
     *
     * @param devWorkOrder 工单信息
     * @return 结果
     */
    public int updateDevWorkOrder(DevWorkOrder devWorkOrder);

    /**
     * 删除工单信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteDevWorkOrderByIds(String ids);

    /**
     * 校验工单号是否存在
     * @return
     */
    public String checkWorkOrderNumber(DevWorkOrder devWorkOrder, HttpServletRequest request);

    /**
     * 工单开始暂停状态修改，第一次点击开始初始化数据
     * @param id 工单id
     * @return 结果
     */
    int editWorkerOrderById(Integer id);

    /**
     * 结束工单
     * @param id
     * @return
     */
    int finishWorkerOrder(Integer id);

    /**
     * 提交工单确认工单
     * @param id
     * @return
     */
    int submitWorkOrder(Integer id);

    /**
     * 根据工单编号查询对应的工单信息
     * @param work_id 工单编号
     * @return
     */
    DevWorkOrder findWorkInfoById(int work_id);

    /**
     * 通过产线Id查询该产线正在生产的工单
     * @param lineId
     * @return
     */
    DevWorkOrder selectWorkOrderBeInByLineId(Integer lineId);

    /**
     * 工单变更
     * @param order
     * @return
     */
    int changeOrder(DevWorkOrder order);

    /**
     * 根据工单id查询对应的ECN信息
     * @param workId 工单id
     * @return
     */
    DevWorkOrder selectWorkOrderEcn(int workId);

    /**
     * 查询所有下到车间的工单信息
     * @param wlSign 工单车间流水线标记状态
     * @return 结果
     */
    public List<DevWorkOrder> selectWorkListInSw(Integer wlSign);

    /**
     * 通过工单的进行状态查询所有的工单信息
     * @param workOrderStatus 工作进行状态
     * @return 结果
     */
    public List<DevWorkOrder> selectWorkListInWorkStatus(Integer workOrderStatus);

    /**
     * 查询工位未配置的工单信息
     * @param lineId 车间id
     * @param workStatus 工单状态
     * @param wlSign 工单下到车间标记
     * @param singleId 单工位id
     * @param companyId 公司id
     * @return 结果
     */
    List<DevWorkOrder> selectAllNotConfigBySwId(Integer lineId,Integer workStatus, Integer wlSign, Integer singleId, Integer companyId);

    /**
     * app查询工单列表
     * @param workOrder 工单信息
     * @return 结果
     */
    List<DevWorkOrder> appSelectDevWorkOrderList(DevWorkOrder workOrder);

    /**
     * 删除工单信息
     */
    int deleteDevWorkOrderById(Integer id, Integer uid);

    /**
     * app查询两条工单信息
     * @return 工单信息
     */
    List<DevWorkOrder> appSelectWorkListTwo();


    /**
     * app 端保存工单
     * @param workOrder 工单信息
     * @return 结果
     */
    int appSaveWorkOrder(DevWorkOrder workOrder) throws Exception;
}
