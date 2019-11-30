package com.ruoyi.project.production.devWorkOrder.service;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;
import com.ruoyi.common.constant.FileConstants;
import com.ruoyi.common.constant.ImportConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.constant.WorkConstants;
import com.ruoyi.common.exception.BusinessException;
import com.ruoyi.common.support.Convert;
import com.ruoyi.common.utils.CodeUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.TimeUtil;
import com.ruoyi.framework.jwt.JwtUtil;
import com.ruoyi.project.device.devCompany.domain.DevCompany;
import com.ruoyi.project.device.devCompany.mapper.DevCompanyMapper;
import com.ruoyi.project.product.importConfig.domain.ImportConfig;
import com.ruoyi.project.product.importConfig.mapper.ImportConfigMapper;
import com.ruoyi.project.product.list.domain.DevProductList;
import com.ruoyi.project.product.list.mapper.DevProductListMapper;
import com.ruoyi.project.production.devWorkData.domain.DevWorkData;
import com.ruoyi.project.production.devWorkData.mapper.DevWorkDataMapper;
import com.ruoyi.project.production.devWorkDayHour.domain.DevWorkDayHour;
import com.ruoyi.project.production.devWorkDayHour.mapper.DevWorkDayHourMapper;
import com.ruoyi.project.production.devWorkOrder.domain.DevWorkOrder;
import com.ruoyi.project.production.devWorkOrder.mapper.DevWorkOrderMapper;
import com.ruoyi.project.production.filesource.domain.FileSourceInfo;
import com.ruoyi.project.production.filesource.mapper.FileSourceInfoMapper;
import com.ruoyi.project.production.productionLine.domain.ProductionLine;
import com.ruoyi.project.production.productionLine.mapper.ProductionLineMapper;
import com.ruoyi.project.production.singleWork.domain.SingleWork;
import com.ruoyi.project.production.singleWork.domain.SingleWorkOrder;
import com.ruoyi.project.production.singleWork.mapper.SingleWorkMapper;
import com.ruoyi.project.production.singleWork.mapper.SingleWorkOrderMapper;
import com.ruoyi.project.production.workOrderChange.domain.WorkOrderChange;
import com.ruoyi.project.production.workOrderChange.mapper.WorkOrderChangeMapper;
import com.ruoyi.project.production.workstation.domain.Workstation;
import com.ruoyi.project.production.workstation.mapper.WorkstationMapper;
import com.ruoyi.project.quality.mesBatch.domain.MesBatch;
import com.ruoyi.project.quality.mesBatch.mapper.MesBatchMapper;
import com.ruoyi.project.system.config.mapper.JpushInfoMapper;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.mapper.UserMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 工单 服务层实现
 *
 * @author zqm
 * @date 2019-04-12
 */
@Service("workOrder")
public class DevWorkOrderServiceImpl implements IDevWorkOrderService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DevWorkOrderServiceImpl.class);

    @Autowired
    private DevWorkOrderMapper devWorkOrderMapper;

    @Autowired
    private DevWorkDataMapper devWorkDataMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductionLineMapper productionLineMapper;

    @Autowired
    private DevProductListMapper productListMapper;

    @Autowired
    private WorkOrderChangeMapper orderChangeMapper;

    @Autowired
    private WorkstationMapper workstationMapper;

    @Autowired
    private DevWorkDayHourMapper workDayHourMapper;

    @Autowired
    private SingleWorkMapper singleWorkMapper;

    @Autowired
    private SingleWorkOrderMapper singleWorkOrderMapper;

    @Autowired
    private DevCompanyMapper companyMapper;

    @Autowired
    private ImportConfigMapper configMapper;

    @Autowired
    private MesBatchMapper mesBatchMapper;

    @Autowired
    private FileSourceInfoMapper fileSourceInfoMapper;

    @Value("${file.iso}")
    private String fileUrl;

    /**
     * 查询工单信息
     *
     * @param id 工单ID
     * @return 工单信息
     */
    @Override
    public DevWorkOrder selectDevWorkOrderById(Integer id) {
        DevWorkOrder workOrder = devWorkOrderMapper.selectDevWorkOrderById(id);
        if (workOrder.getWlSign() == 0) {
            ProductionLine productionLine = productionLineMapper.selectProductionLineById(workOrder.getLineId());
            workOrder.setParam1(productionLine.getLineName());
            User user = userMapper.selectUserInfoById(productionLine.getDeviceLiable());
            workOrder.setParam2(user == null ? "" : user.getUserName());
            productionLine.setDeviceLiableName(user == null ? "" : user.getUserName());
            user = userMapper.selectUserInfoById(productionLine.getDeviceLiableTow());
            workOrder.setParam3(user == null ? "" : user.getUserName());
            productionLine.setDeviceLiableTowName(user == null ? "" : user.getUserName());
            workOrder.setProductionLine(productionLine);
        } else if (workOrder.getWlSign() == 1) {
            //查询车间
            SingleWork work = singleWorkMapper.selectSingleWorkById(workOrder.getLineId());
            if (work != null) {
                workOrder.setSingle(work.getId());
                workOrder.setParam1(work.getWorkshopName());
                User user = userMapper.selectUserInfoById(work.getLiableOne());
                workOrder.setParam2(user == null ? "" : user.getUserName());
                user = userMapper.selectUserInfoById(work.getLiableTwo());
                workOrder.setParam3(user == null ? "" : user.getUserName());
            }
        }
        return workOrder;
    }

    /**
     * 查询工单列表
     *
     * @param devWorkOrder 工单信息
     * @return 工单集合
     */
    @Override
    public List<DevWorkOrder> selectDevWorkOrderList(DevWorkOrder devWorkOrder) {
        User sysUser = JwtUtil.getUser();
        if (sysUser == null) {
            return Collections.emptyList();
        }
        devWorkOrder.setCompanyId(sysUser.getCompanyId());
        List<DevWorkOrder> workOrders = devWorkOrderMapper.selectDevWorkOrderList(devWorkOrder);
        getLineOrHouseName(workOrders, sysUser);
        return workOrders;
    }

    /**
     * 新增工单
     *
     * @param devWorkOrder 工单信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDevWorkOrder(DevWorkOrder devWorkOrder) throws Exception {
        User u = JwtUtil.getTokenUser(ServletUtils.getRequest());
        if (u == null) return 0;
        Integer productId = Integer.valueOf(devWorkOrder.getProductCode());
        DevProductList devProductList = productListMapper.selectDevProductListById(productId);
        if (devProductList == null) return 0;//产品不存在
        devWorkOrder.setMakeType(devProductList.getSign());
        // 设置工单产品的名称
        devWorkOrder.setProductName(devProductList.getProductName());
        // 设置工单产品编码
        devWorkOrder.setProductCode(devProductList.getProductCode());
        devWorkOrder.setProductStandardHour(devProductList.getStandardHourYield());
        //产品型号
        devWorkOrder.setProductModel(devProductList.getProductModel());
        // 设置工单属于哪个公司
        devWorkOrder.setCompanyId(u.getCompanyId());
        // 创建者
        devWorkOrder.setCreateBy(u.getUserName());
        devWorkOrderMapper.insertDevWorkOrder(devWorkOrder);
        return 1;
    }


    /**
     * app端新增工单
     *
     * @param workOrder 工单信息
     * @return 结果
     */
    @Override
    public int appSaveWorkOrder(DevWorkOrder workOrder) throws Exception {
        User user = JwtUtil.getUser();
        if (user == null) {
            return 0;
        }
        // 验证产品是否存在
        DevProductList product = productListMapper.selectDevProductByCode(user.getCompanyId(), workOrder.getProductCode());
        if (product == null) {
            throw new BusinessException("产品不存在或被删除");
        }
        // 制作类型
        workOrder.setMakeType(product.getSign());
        // 设置工单产品的名称
        workOrder.setProductName(product.getProductName());
        // 设置工单产品编码
        workOrder.setProductCode(product.getProductCode());
        workOrder.setProductStandardHour(product.getStandardHourYield());
        //产品型号
        workOrder.setProductModel(product.getProductModel());
        // 设置工单属于哪个公司
        workOrder.setCompanyId(user.getCompanyId());
        // 创建者
        workOrder.setCreateBy(user.getUserName());
        return devWorkOrderMapper.insertDevWorkOrder(workOrder);
    }

    /**
     * 修改工单
     *
     * @param devWorkOrder 工单信息
     * @return 结果
     */
    @Override
    public int updateDevWorkOrder(DevWorkOrder devWorkOrder) {
        User u = JwtUtil.getUser();
        DevWorkOrder workOrder = devWorkOrderMapper.selectDevWorkOrderById(devWorkOrder.getId());
        Long userId = u.getUserId();
        if (workOrder == null) {
            throw new BusinessException("工单不存在或已经删除");
        }
        if (workOrder.getWorkSign().equals(WorkConstants.WORK_SIGN_YES)) {
            throw new BusinessException("已经提交数据的工单不能进行修改");
        }
        int one = 0;
        int tow = 0;
        if (workOrder.getWlSign() == 0) {
            ProductionLine productionLine = productionLineMapper.selectProductionLineById(workOrder.getLineId());
            one = productionLine.getDeviceLiable();
            tow = productionLine.getDeviceLiableTow();
        } else if (workOrder.getWlSign() == 1) {
            /**
             * 判断修改的工单车间是否已经分配了单工位
             */
            if (workOrder.getWorkorderStatus().equals(WorkConstants.WORK_STATUS_NOSTART)) {
                List<SingleWorkOrder> singleWorkOrders = singleWorkOrderMapper.selectSingleWorkByWorkIdAndPid(workOrder.getLineId(), workOrder.getId());
                if (StringUtils.isNotEmpty(singleWorkOrders)) {
                    throw new BusinessException("该工单已分配单工位不能修改车间");
                }
            }
            SingleWork work = singleWorkMapper.selectSingleWorkById(workOrder.getLineId());
            one = work.getLiableOne();
            tow = work.getLiableTwo();
        }
        // 不是工单负责人
        if (one != userId.intValue() && tow != userId.intValue() && !u.getLoginName().equals("admin")) {
            throw new BusinessException("不是工单负责人");
        }
        return devWorkOrderMapper.updateDevWorkOrder(devWorkOrder);
    }

    /**
     * 删除工单对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDevWorkOrderByIds(String ids) {
        return devWorkOrderMapper.deleteDevWorkOrderByIds(Convert.toStrArray(ids));
    }

    /**
     * 校验工单号是否存在
     *
     * @return 结果
     */
    @Override
    public String checkWorkOrderNumber(DevWorkOrder devWorkOrder, HttpServletRequest request) {
        Integer companyId = JwtUtil.getTokenUser(request).getCompanyId();
        DevWorkOrder uniqueWork = devWorkOrderMapper.checkWorkOrderNumber(devWorkOrder.getWorkorderNumber(), companyId);
        if (uniqueWork != null) {
            return WorkConstants.WORKERORDER_NUMBER_NOT_UNIQUE;
        }
        return WorkConstants.WORKERORDER_NUMBER_UNIQUE;
    }

    /**
     * 工单状态控制 <br>
     * 点击开始，开始工单，数据进行初始化
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int editWorkerOrderById(Integer id) {
        User user = JwtUtil.getUser();
        if (user == null) {
            throw new BusinessException("用户不存在或被删除");
        }
        // 查询公司信息
        DevCompany company = companyMapper.selectDevCompanyById(user.getCompanyId());
        if (company == null) {
            throw new BusinessException("公司不存在或被删除");
        }
        DevWorkOrder devWorkOrder = devWorkOrderMapper.selectDevWorkOrderById(id);
        if (devWorkOrder == null) {
            throw new BusinessException("工单信息不存在或被删除");
        }

        ProductionLine productionLine = productionLineMapper.selectProductionLineById(devWorkOrder.getLineId());
        if (productionLine == null) {
            throw new BusinessException("产线不存在或被删除");
        }
        // 不是工单负责人
        if (productionLine.getDeviceLiable() != user.getUserId().intValue() &&
                productionLine.getDeviceLiableTow() != user.getUserId().intValue() &&
                !user.getLoginName().equals("admin")) {
            throw new BusinessException("不是工单负责人");
        }
        if (devWorkOrder.getOperationStatus() == 0) {
            DevWorkOrder uniqueWork = devWorkOrderMapper.checkWorkLineUnique(devWorkOrder.getLineId(), WorkConstants.SING_LINE);
            // 判断流水线是否只有一个正在进行生产的工单
            if (uniqueWork != null) {
                throw new BusinessException("该流水线有工单未完成");
            }
        }
        // 工单生产状态处于进行中
        if (devWorkOrder.getWorkorderStatus().equals(WorkConstants.WORK_STATUS_STARTING)) {
            // 页面点击暂停按钮暂时暂停工单生产
            if (devWorkOrder.getOperationStatus().equals(WorkConstants.OPERATION_STATUS_STARTING)) {
                devWorkOrder.setOperationStatus(WorkConstants.OPERATION_STATUS_PAUSE);
                devWorkOrder.setUpdateBy(user.getUserName());
                //将其工单对应的数据需要重新记录初始值
                devWorkDataMapper.updateWorkSigInit(devWorkOrder.getId());
                //计数时间
                devWorkOrder.setSignHuor(devWorkOrder.getSignHuor() + TimeUtil.getDateDel(devWorkOrder.getSignTime(), new Date()));

                //页面点击开始按钮继续工单生产
            } else if (devWorkOrder.getOperationStatus().equals(WorkConstants.OPERATION_STATUS_PAUSE)) {
                // 计数状态标志重新标志位计数0
                devWorkOrder.setPbSign(WorkConstants.PB_SIGN_YES);
                devWorkOrder.setOperationStatus(WorkConstants.OPERATION_STATUS_STARTING);
                devWorkOrder.setUpdateBy(user.getUserName());
                devWorkOrder.setSignTime(new Date());
            }
        }
        //首次点击开始，工单处于未进行、未开始的状态，页面点击开始按钮
        if (devWorkOrder.getWorkorderStatus().equals(WorkConstants.WORK_STATUS_NOSTART)) {
            if (devWorkOrder.getOperationStatus().equals(WorkConstants.OPERATION_STATUS_NOSTART)) {
                // 实际开始时间
                devWorkOrder.setStartTime(new Date());
                devWorkOrder.setSignTime(new Date());
                devWorkOrder.setSignHuor(0F);
            }
            // 修改工单的状态为进行中
            devWorkOrder.setWorkorderStatus(WorkConstants.WORK_STATUS_STARTING);
            // 修改工单的操作状态为正在进行，页面显示暂停按钮
            devWorkOrder.setOperationStatus(WorkConstants.OPERATION_STATUS_STARTING);
            // 工单的更新者
            devWorkOrder.setUpdateBy(user.getUserName());

            // 通过产线id获取各个工位信息
            List<Workstation> workstationList = workstationMapper.selectWorkstationListByLineId(user.getCompanyId(), devWorkOrder.getLineId());
            DevWorkData workData = null;
            DevWorkDayHour workDayHour = null;
            if (StringUtils.isNotEmpty(workstationList)) {
                for (Workstation workstation : workstationList) {
                    // 初始化工单数据
                    workData = new DevWorkData();
                    workData.setWorkId(devWorkOrder.getId());
                    workData.setCompanyId(devWorkOrder.getCompanyId());
                    workData.setLineId(devWorkOrder.getLineId());
                    workData.setScType(WorkConstants.SING_LINE);
                    // 设置计数器硬件
                    workData.setDevId(workstation.getDevId());
                    workData.setDevName(workstation.getDevName());
                    // 设置工位
                    workData.setIoId(workstation.getId());
                    workData.setCreateTime(new Date());
                    workData.setIoSign(workstation.getSign());
                    devWorkDataMapper.insertDevWorkData(workData);

                    // 初始化工单各个IO口每小时数据
                    workDayHour = new DevWorkDayHour();
                    workDayHour.setWorkId(devWorkOrder.getId());
                    workDayHour.setCompanyId(devWorkOrder.getCompanyId());
                    workDayHour.setLineId(devWorkOrder.getLineId());
                    // 初始化硬件名称以及工位信息
                    workDayHour.setDevId(workstation.getDevId());
                    workDayHour.setDevName(workstation.getDevName());
                    workDayHour.setIoId(workstation.getId());
                    workDayHour.setDataTime(new Date());
                    workDayHour.setCreateTime(new Date());
                    workDayHourMapper.insertDevWorkDayHour(workDayHour);
                }
            }

            /**
             * MES规则信息
             */
            // 查询配置的规则信息
            ImportConfig config = configMapper.selectImportConfigByType(user.getCompanyId(), ImportConstants.TYPE_WORK_MES);
            if (config == null) {
                throw new BusinessException("请先配置建档规则");
            }
            // 生成建档信息
            String mainInfo = "";
            MesBatch mesBatch = null;
            if (devWorkOrder.getProductNumber() != null && devWorkOrder.getProductNumber() > 0) {
                int totalNum = devWorkOrder.getProductNumber();
                System.out.println("=============进入生成建档信息==============" + totalNum);
                for (int i = 0; i < totalNum; i++) {
                    mainInfo = config.getConRule() + CodeUtils.getRandomStr(config.getCon1());
                    // 查询建档信息是否存在
                    mesBatch = mesBatchMapper.selectMesBatchByMainInfo(user.getCompanyId(), mainInfo);
                    int index = 0;
                    while (mesBatch != null && index < 10) {
                        System.out.println(mainInfo);
                        System.out.println("进入重新生成建档逻辑");
                        mainInfo = config.getConRule() + CodeUtils.getRandomStr(config.getCon1());
                        mesBatch = mesBatchMapper.selectMesBatchByMainInfo(user.getCompanyId(), mainInfo);
                        System.out.println(mainInfo);
                        index++;
                    }
                    if (mesBatch != null) {
                        throw new BusinessException("工单开启失败");
                    }
                    mesBatch = new MesBatch();
                    mesBatch.setCompanyId(user.getCompanyId());
                    mesBatch.setLineId(productionLine.getId());
                    mesBatch.setPnCode(devWorkOrder.getProductCode());
                    mesBatch.setWorkCode(devWorkOrder.getWorkorderNumber());
                    mesBatch.setPnMainInfo(mainInfo);
                    mesBatchMapper.insertMesBatch(mesBatch);
                }
            }

            //推送看板
            JPushWatchMsg(company);
            // 推送ASOP
            JPushMsg(1, devWorkOrder);
        }
        return devWorkOrderMapper.updateDevWorkOrder(devWorkOrder);
    }

    /**
     * 结束工单
     *
     * @param id
     * @return
     */
    @Override
    public int finishWorkerOrder(Integer id) {
        User user = JwtUtil.getUser();
        if (user == null) {
            throw new BusinessException(UserConstants.NOT_LOGIN);
        }
        DevCompany company = companyMapper.selectDevCompanyById(user.getCompanyId());
        if (company == null) {
            throw new BusinessException("公司不存在或被删除");
        }
        Long userId = user.getUserId();
        DevWorkOrder devWorkOrder = devWorkOrderMapper.selectDevWorkOrderById(id);
        ProductionLine productionLine = productionLineMapper.selectProductionLineById(devWorkOrder.getLineId());
        // 不是工单负责人
        if (productionLine.getDeviceLiable() != userId.intValue() &&
                productionLine.getDeviceLiableTow() != userId.intValue() &&
                !user.getLoginName().equals("admin")) {
            throw new BusinessException("不是工单负责人");
        }

        //推送看板
        JPushWatchMsg(company);
        // 推送ASOP
        JPushMsg(1, devWorkOrder);
        updateWork(user, devWorkOrder);
        return devWorkOrderMapper.updateDevWorkOrder(devWorkOrder);
    }

    /**
     * 结束工单更新工单相关信息
     *
     * @param user         用户
     * @param devWorkOrder 工单信息
     */
    private void updateWork(User user, DevWorkOrder devWorkOrder) {
        // 设置工单的生产状态为已经完成
        devWorkOrder.setWorkorderStatus(WorkConstants.WORK_STATUS_END);
        // 确认数据
        devWorkOrder.setWorkSign(WorkConstants.WORK_SIGN_YES);
        // 设置工单的操作状态为结束
        devWorkOrder.setOperationStatus(WorkConstants.OPERATION_STATUS_FINISH);
        // 设置结束时间
        devWorkOrder.setEndTime(new Date());
        // 设置统计用时
        devWorkOrder.setSignHuor(devWorkOrder.getSignHuor() + TimeUtil.getDateDel(devWorkOrder.getSignTime(), new Date()));
        devWorkOrder.setUpdateBy(user.getUserName());
        devWorkOrder.setUpdateTime(new Date());
    }

    /**
     * 提交工单确认工单
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitWorkOrder(Integer id) {
        User user = JwtUtil.getUser();
        if (user == null) {
            throw new BusinessException(UserConstants.NOT_LOGIN);
        }
        Long userId = user.getUserId();
        DevWorkOrder devWorkOrder = devWorkOrderMapper.selectDevWorkOrderById(id);
        if (!devWorkOrder.getWorkorderStatus().equals(WorkConstants.WORK_STATUS_END)) {
            throw new BusinessException("未完成的工单不能提交");
        }
        /**
         * 产线工单
         */
        if (devWorkOrder.getWlSign().equals(WorkConstants.SING_LINE)) {
            ProductionLine productionLine = productionLineMapper.selectProductionLineById(devWorkOrder.getLineId());
            // 不是工单负责人
            if (!productionLine.getDeviceLiable().equals(userId.intValue()) &&
                    !productionLine.getDeviceLiableTow().equals(userId.intValue()) &&
                    !user.getLoginName().equals("admin")) {
                throw new BusinessException("不是工单负责人");
            }
        }
        if (devWorkOrder.getWorkSign().equals(WorkConstants.WORK_SIGN_YES)) {
            throw new BusinessException("该工单已经提交过，不能重复提交");
        }
        // 设置状态为已确认数据不可进行修改和删除
        devWorkOrder.setWorkSign(WorkConstants.WORK_SIGN_YES);
        devWorkOrder.setUpdateTime(new Date());
        devWorkOrder.setUpdateBy(user.getUserName());
        return devWorkOrderMapper.updateDevWorkOrder(devWorkOrder);
    }


    /**
     * 查询当天所有工单
     *
     * @return
     */
    public List<DevWorkOrder> selectWorkOrderAllToday(Cookie[] cookies) {
        User user = JwtUtil.getTokenCookie(cookies);
        List<DevWorkOrder> workOrders = devWorkOrderMapper.selectWorkOrderAllToday(user.getCompanyId());
        getLineOrHouseName(workOrders, user);
        return workOrders;
    }


    /**
     * 通过产线Id查询该产线正在生产的工单
     *
     * @param lineId
     * @return
     */
    @Override
    public DevWorkOrder selectWorkOrderBeInByLineId(Integer lineId) {
        return devWorkOrderMapper.selectWorkByCompandAndLine(JwtUtil.getUser().getCompanyId(), lineId, WorkConstants.SING_LINE);
    }

    /**
     * 根据工单编号查询对应的工单信息
     *
     * @param work_id 工单编号
     * @return
     */
    @Override
    public DevWorkOrder findWorkInfoById(int work_id) {
        //查询对应的工单是否存在
        DevWorkOrder order = devWorkOrderMapper.selectDevWorkOrderById(work_id);
        if (order == null || order.getLineId() == null) {
            return null;
        }
        //查询对应的产线信息
        ProductionLine line = productionLineMapper.selectProductionLineById(order.getLineId());
        if (line == null) return null;
        //判断产线是否是手动
        if (line.getManual() == 0) {
            //默认为0
            order.setCumulativeNumber(0);
            //为自动、查询对应的产线的警戒线标记IO口
            Workstation workstation = workstationMapper.selectWorkstationSignByLineId(line.getId(), line.getCompanyId());
            if (workstation != null) {
                //查询对应的累计数据
                DevWorkData data = devWorkDataMapper.selectWorkDataByCompanyLineWorkDev(order.getCompanyId(), line.getId(),
                        order.getId(), workstation.getDevId(), workstation.getId(), WorkConstants.SING_LINE);
                if (data != null) order.setCumulativeNumber(data.getCumulativeNum());
            }
        }
        float standardHour = order.getSignHuor();
        //达成率默认为0
        order.setReachRate(0.0F);
        if (order.getWorkorderStatus() == WorkConstants.WORK_STATUS_STARTING && order.getCumulativeNumber() != null) {
            //计数标准产量
            if (order.getOperationStatus() == WorkConstants.OPERATION_STATUS_STARTING) {
                standardHour += TimeUtil.getDateDel(order.getSignTime(), new Date());
            }
            int standardTotal = (int) (order.getProductStandardHour() * standardHour);
            order.setReachRate(standardTotal == 0 ? 0.0F : new BigDecimal(((float) order.getCumulativeNumber() / ((float) standardTotal)) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
        }
        order.setSignHuor(standardHour);
        return order;
    }

    /**
     * 工单变更
     *
     * @param order 工单信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeOrder(DevWorkOrder order) {
        if (order == null) return 0;
        User user = JwtUtil.getTokenUser(ServletUtils.getRequest());
        if (user == null) return 0;
        DevWorkOrder devWorkOrder = devWorkOrderMapper.selectDevWorkOrderById(order.getId());
        if (devWorkOrder == null) return 0;
        WorkOrderChange change = new WorkOrderChange();
        change.setCompanyId(user.getCompanyId());
        change.setOrderId(devWorkOrder.getId());
        change.setOrderCode(devWorkOrder.getWorkorderNumber());
        change.setOrderCodeInfo(devWorkOrder.getOrderCode());
        if (order.getWlSign() == WorkConstants.SING_LINE) {
            //查询对应的产线信息
            ProductionLine line = productionLineMapper.selectProductionLineById(order.getLineId());
            if (line == null) return 0;
            //保存变更记录
            change.setLineId(line.getId());
            change.setLineName(line.getLineName());
            User u1 = userMapper.selectUserInfoById(line.getDeviceLiable());
            change.setDeviceLiable(u1 == null ? "" : u1.getUserName());
            u1 = userMapper.selectUserInfoById(line.getDeviceLiableTow());
            change.setDeviceLiable2(u1 == null ? "" : u1.getUserName());
        } else if (order.getWlSign() == WorkConstants.SING_SINGLE) {
            /**
             * 查询该工单是否已经配置过单工位
             */
            List<SingleWorkOrder> singleWorkOrders = singleWorkOrderMapper.selectSingleWorkByWorkIdAndPid(order.getSingle(), order.getId());
            if (StringUtils.isNotEmpty(singleWorkOrders)) {
                throw new BusinessException("该工单已分配单工位不允许变更");
            }
            SingleWork work = singleWorkMapper.selectSingleWorkById(order.getSingle());
            if (work == null) return 0;
            //保存变更记录
            change.setLineId(work.getId());
            change.setLineName(work.getWorkshopName());
            User u1 = userMapper.selectUserInfoById(work.getLiableOne());
            change.setDeviceLiable(u1 == null ? "" : u1.getUserName());
            u1 = userMapper.selectUserInfoById(work.getLiableTwo());
            change.setDeviceLiable2(u1 == null ? "" : u1.getUserName());
            change.setWorkPrice(devWorkOrder.getWorkPrice());
            order.setLineId(order.getSingle());
        } else {
            return 0;
        }
        change.setProductNumber(devWorkOrder.getProductNumber());
        change.setProductionStart(devWorkOrder.getProductionStart());
        change.setProductionEnd(devWorkOrder.getProductionEnd());
        change.setCreatePeople(user.getUserName());
        change.setCreateTime(new Date());
        change.setRemark(order.getRemark());
        orderChangeMapper.insertWorkOrderChange(change);
        return devWorkOrderMapper.updateDevWorkOrder(order);
    }

    /**
     * 根据工单id查询对应的ECN信息
     *
     * @param workId 工单id
     * @return
     */
    @Override
    public DevWorkOrder selectWorkOrderEcn(int workId) {
        DevWorkOrder order = devWorkOrderMapper.selectDevWorkOrderById(workId);
        return order;
    }


    /**
     * 查询所有下到车间的工单信息
     *
     * @return 结果
     */
    @Override
    public List<DevWorkOrder> selectWorkListInSw(Integer wlSign) {
        User user = JwtUtil.getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        return devWorkOrderMapper.selectWorkListInSw(user.getCompanyId(), wlSign);
    }

    /**
     * 通过工作的状态查询所有的工单信息
     *
     * @param workOrderStatus 工作进行状态
     * @return 结果
     */
    @Override
    public List<DevWorkOrder> selectWorkListInWorkStatus(Integer workOrderStatus) {
        User user = JwtUtil.getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        return devWorkOrderMapper.selectWorkListInWorkStatus(user.getCompanyId(), workOrderStatus);
    }

    /**
     * 查询工单未配置的未确认数据的工单信息
     *
     * @param lineId     车间id
     * @param workStatus 工单状态
     * @param wlSign     工单下到车间标记
     * @param singleId   单工位id
     * @param companyId  公司id
     * @return 结果过
     */
    @Override
    public List<DevWorkOrder> selectAllNotConfigBySwId(Integer lineId, Integer workStatus, Integer wlSign, Integer singleId, Integer companyId) {
        return devWorkOrderMapper.selectAllNotConfigBySwId(lineId, workStatus, wlSign, singleId, companyId);
    }

    /******************************************************************************************************
     *********************************** app端工单业务逻辑 *************************************************
     ******************************************************************************************************/

    /**
     * app端查询工单信息
     *
     * @param workOrder 工单信息
     * @return
     */
    @Override
    public List<DevWorkOrder> appSelectDevWorkOrderList(DevWorkOrder workOrder) {
        User user = JwtUtil.getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        workOrder.setCompanyId(user.getCompanyId());
        List<DevWorkOrder> workOrders = devWorkOrderMapper.selectDevWorkOrderList(workOrder);
        return workOrders;
    }


    /**
     * 删除工单信息
     */
    @Override
    public int deleteDevWorkOrderById(Integer id, Integer uid) {
        User user = null;
        if (uid == null) {
            user = JwtUtil.getUser();
        } else {
            user = userMapper.selectUserInfoById(uid);
        }
        if (user == null) {
            throw new BusinessException(UserConstants.NOT_LOGIN);
        }
        DevWorkOrder workOrder = devWorkOrderMapper.selectDevWorkOrderById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在或者已经删除");
        }
        if (!workOrder.getWorkorderStatus().equals(WorkConstants.WORK_STATUS_NOSTART)) {
            throw new BusinessException("已经开始或结束的工单不能删除");
        }

        return devWorkOrderMapper.deleteDevWorkOrderById(id);
    }

    /**
     * app端查询两条工单信息
     *
     * @return 工单信息
     */
    @Override
    public List<DevWorkOrder> appSelectWorkListTwo() {
        User user = JwtUtil.getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        List<DevWorkOrder> workOrders = devWorkOrderMapper.selectWorkOrderAllToday(user.getCompanyId());
        return workOrders;
    }


    /***************************消息推送开始**********************************/

    @Value("${jpush.mastersecret}")
    private String MASTER_SECRET;

    @Value("${jpush.appkey}")
    private String APP_KEY;

    @Autowired
    private JpushInfoMapper jpushInfoMapper;

    /**
     * 推送生产看板
     *
     * @param company
     */
    private void JPushWatchMsg(DevCompany company) {
        List<String> alias = jpushInfoMapper.selectJPushInfoList(company.getLoginNumber());
        JSONObject data = new JSONObject();
        data.put("msg", "1");
        //进行消息推送生产看板
        JPushClient jpushClient = new JPushClient("d2a09226055d96209ef6a0a5", "641ae46722063ecb6673ad2e", null, ClientConfig.getInstance());
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.alert(data.toString()))
                .build();
        try {
            PushResult result = jpushClient.sendPush(payload);
        } catch (APIConnectionException e) {
            LOGGER.error("消息推送出现异常：" + e.getMessage());
            // e.printStackTrace();
        } catch (APIRequestException e) {
            LOGGER.error("消息推送出现异常：" + e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     * 推送ASOP
     */
    private void JPushMsg(int type, DevWorkOrder order) {
        if (order == null)
            return;
        List<String> alias = null;
        if (type == 1) {
            //流水线信息推送
            //1、查询对应产线
            ProductionLine line = productionLineMapper.selectProductionLineById(order.getLineId());
            //2、查询对应产线所有配置SOP看板硬件的硬件编码
            if (line != null) {
                alias = workstationMapper.countLineKBCode(line.getCompanyId(), line.getId());
            }
        }
        if (alias == null || alias.size() <= 0) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put("msg", "1");
        //进行消息推送
        JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, ClientConfig.getInstance());
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.alert(data.toString()))
                .build();
        try {
            PushResult result = jpushClient.sendPush(payload);
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }

    }
    /********************************消息推送结束*******************************/

    /**
     * 获取工单属于流水线或车间的名称信息
     *
     * @param workOrders 工单信息
     */
    private void getLineOrHouseName(List<DevWorkOrder> workOrders, User user) {
        DevProductList product;
        List<FileSourceInfo> pnMesfiles = null;
        for (DevWorkOrder workOrder : workOrders) {
            // 查询工单产品信息
            product = productListMapper.selectDevProductByCode(user.getCompanyId(), workOrder.getProductCode());
            if (product != null) {
                // 查询MES文件信息
                pnMesfiles = fileSourceInfoMapper.selectFileListBySaveIdAndType(user.getCompanyId(), FileConstants.FILE_SAVETYPE_PRO_MES, product.getId());
                if (StringUtils.isNotEmpty(pnMesfiles)) {
                    workOrder.setProductEcn(1);
                }
            }
            if (workOrder.getWlSign() == 0) {
                ProductionLine productionLine = productionLineMapper.selectProductionLineById(workOrder.getLineId());
                if (null != productionLine) {
                    workOrder.setParam1(productionLine.getLineName());
                }
            } else if (workOrder.getWlSign() == 1) {
                SingleWork work = singleWorkMapper.selectSingleWorkById(workOrder.getLineId());
                if (work != null) {
                    workOrder.setParam1(work.getWorkshopName());
                }
            }
        }
    }

}
