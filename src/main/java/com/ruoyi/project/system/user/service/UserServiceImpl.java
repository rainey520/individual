package com.ruoyi.project.system.user.service;


import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.BusinessException;
import com.ruoyi.common.support.Convert;
import com.ruoyi.common.utils.ChineseCharacterUtil;
import com.ruoyi.common.utils.PasswordUtil;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.aspectj.lang.annotation.DataScope;
import com.ruoyi.framework.aspectj.lang.annotation.DataSource;
import com.ruoyi.framework.aspectj.lang.enums.DataSourceType;
import com.ruoyi.framework.jwt.JwtUtil;
import com.ruoyi.project.device.devCompany.domain.DevCompany;
import com.ruoyi.project.device.devCompany.mapper.DevCompanyMapper;
import com.ruoyi.project.device.devCompany.service.IDevCompanyService;
import com.ruoyi.project.system.config.service.IConfigService;
import com.ruoyi.project.system.post.domain.Post;
import com.ruoyi.project.system.post.mapper.PostMapper;
import com.ruoyi.project.system.role.domain.Role;
import com.ruoyi.project.system.role.mapper.RoleMapper;
import com.ruoyi.project.system.user.domain.*;
import com.ruoyi.project.system.user.mapper.UserMapper;
import com.ruoyi.project.system.user.mapper.UserPostMapper;
import com.ruoyi.project.system.user.mapper.UserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 用户 业务层处理
 *
 * @author ruoyi
 */
@Service("user")
public class UserServiceImpl implements IUserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserPostMapper userPostMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private IConfigService configService;

    @Autowired
    private DevCompanyMapper companyMapper;

    /**
     * 根据条件分页查询用户对象
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(tableAlias = "u")
    public List<User> selectUserList(User user) {
        User u = JwtUtil.getTokenUser(ServletUtils.getRequest());
        user.setCompanyId(u.getCompanyId());
//        // 生成数据权限过滤条
        List<User> userList = userMapper.selectUserList(user);
        List<Role> userRoles = null;
        for (User user1 : userList) {
            userRoles = roleMapper.selectRolesByUserId(user1.getUserId());
            user1.setRoles(userRoles);
        }
        return userList;
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    @DataSource(value = DataSourceType.SLAVE)
    public User selectUserByLoginName(String userName) {
        return userMapper.selectUserByLoginName(userName);
    }

    /**
     * 通过手机号码查询用户
     *
     * @param phoneNumber 手机号码
     * @return 用户对象信息
     */
    @Override
    public User selectUserByPhoneNumber(String phoneNumber) {
        return userMapper.selectUserByPhoneNumber(phoneNumber);
    }

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    @Override
    public User selectUserByEmail(String email) {
        return userMapper.selectUserByEmail(email);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public User selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteUserByIds(String ids) throws BusinessException {
        Long[] userIds = Convert.toLongArray(ids);
        User user = userMapper.selectUserById(userIds[0]);
        if (User.isAdmin(user) || User.isJty(user) || user.getSign() == 1) {
            throw new BusinessException("该用户是管理员不能删除");
        }
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int insertUser(User user) {
        User sysUser = JwtUtil.getTokenUser(ServletUtils.getRequest());
        // 用户导入设置登录标记为
        user.randomSalt();
        if (user.getUserName() == null) {
            user.setUserName(user.getLoginName()); // 设置用户名为登录手机号
        }
        user.setPhonenumber(user.getLoginName()); // 设置用户手机号
        user.setPassword(PasswordUtil.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setCreateBy(sysUser.getLoginName());
        // 用户公司设置,通过获取系统登录用户设置系统id
        user.setCompanyId(sysUser.getCompanyId());
        // 设置用户标记
        user.setTag(User.COMPANY_OTHER);
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUser(User user) throws Exception {
        User tokenUser = JwtUtil.getTokenUser(ServletUtils.getRequest());
        User u = userMapper.selectUserById(user.getUserId());
        if(!User.isAdmin(tokenUser) &&(User.isJty(u) || User.isAdmin(u))){
            throw  new Exception("该用户是系统默认用户不能修改");
        }
        Long userId = user.getUserId();

        user.setUpdateBy(tokenUser.getLoginName());
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        if (null == userId) {
            user.setCompanyId(tokenUser.getCompanyId());
            userMapper.updateUserByLoginName(user);
        }
        return userMapper.updateUser(user);

    }

    public int updateUserDelFlag(User user, String token) {
        return userMapper.updateUserDelFlag(user.getUserId().intValue(), user.getCompanyId());
    }

    /**
     * 修改用户个人详细信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserInfo(User user, HttpServletRequest request) throws Exception {
        user.setDevCompany(null);
        user.setCreateTime(null);
        user.setLoginDate(null);
        user.setUpdateTime(null);
//        UserApi userApi = Feign.builder()
//                .encoder(new GsonEncoder())
//                .decoder(new GsonDecoder())
//                .target(UserApi.class, FeignUtils.MAIN_PATH);
//        HashMap<String, Object> result = userApi.editUserInfo(user, JwtUtil.getToken(request));
//        if (Double.valueOf(result.get("code").toString()) == 0) {
        return userMapper.updateUser(user);
//        } else {
//            return 0;
//        }
    }

    /**
     * 修改用户个人详细信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserInfo1(User user, String token) throws Exception {
        user.setDevCompany(null);
        user.setCreateTime(null);
        user.setLoginDate(null);
        user.setUpdateTime(null);
//        UserApi userApi = Feign.builder()
//                .encoder(new GsonEncoder())
//                .decoder(new GsonDecoder())
//                .target(UserApi.class, FeignUtils.MAIN_PATH);
//        HashMap<String, Object> result = userApi.editUserInfo(user, token);
//        if (Double.valueOf(result.get("code").toString()) == 0) {
        return userMapper.updateUser(user);
//        } else {
//            return 0;
//        }
    }

    /**
     * 修改用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetUserPwd(User user) {
        user.setAvatar(null);
        user.randomSalt(); // 生成盐
        user.setLoginTag(UserConstants.LOGIN_TAG_ADD);
        user.setPassword(PasswordUtil.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        return userMapper.updateUser(user);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(User user) {
        Long[] roles = user.getRoleIds();
        if (StringUtils.isNotNull(roles)) {
            // 新增用户与角色管理
            List<UserRole> list = new ArrayList<UserRole>();
            for (Long roleId : user.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getUserId());
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0) {
                userRoleMapper.batchUserRole(list);
            }
        }
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserPost(User user) {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotNull(posts)) {
            // 新增用户与岗位管理
            List<UserPost> list = new ArrayList<UserPost>();
            for (Long postId : user.getPostIds()) {
                UserPost up = new UserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            if (list.size() > 0) {
                userPostMapper.batchUserPost(list);
            }
        }
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param loginName 用户名
     * @return
     */
    @Override
    public String checkLoginNameUnique(String loginName) {
        int count = userMapper.checkLoginNameUnique(loginName);
        if (count > 0) {
            return UserConstants.USER_NAME_NOT_UNIQUE;
        }
        return UserConstants.USER_NAME_UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public String checkEmailUnique(User user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        User info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.USER_EMAIL_NOT_UNIQUE;
        }
        return UserConstants.USER_EMAIL_UNIQUE;
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(Long userId) {
        List<Role> list = roleMapper.selectRolesByUserId(userId);
        StringBuffer idsStr = new StringBuffer();
        for (Role role : list) {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(Long userId) {
        List<Post> list = postMapper.selectPostsByUserId(userId);
        StringBuffer idsStr = new StringBuffer();
        if (list != null) {
            for (Post post : list) {
                idsStr.append(post.getPostName()).append(",");
            }
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 导入用户数据
     *
     * @param userList        用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     */
    @Override
    public String importUser(List<User> userList, Boolean isUpdateSupport, HttpServletRequest request) {
        if (StringUtils.isNull(userList) || userList.size() == 0) {
            throw new BusinessException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String operName = JwtUtil.getTokenUser(request).getLoginName();
        String password = configService.selectConfigByKey("sys.user.initPassword");
        for (User user : userList) {
            try {
                if (!user.getLoginName().matches(UserConstants.MOBILE_PHONE_NUMBER_PATTERN)) { // 不是手机格式
                    throw new BusinessException("用户手机格式不正确！");
                }
                // 验证是否存在这个用户
                User u = userMapper.selectUserByLoginName(user.getLoginName());
                if (StringUtils.isNull(u)) {
                    user.setPassword(password);
                    user.setCreateBy(operName);
                    this.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 导入成功");
                } else if (u.getDelFlag().equals("2")) {
                    user.setUpdateBy(operName);
                    u.setCompanyId(JwtUtil.getTokenUser(request).getCompanyId());
                    this.updateUserDelFlag(u, JwtUtil.getToken(request));
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 导入成功");
                } else if (isUpdateSupport) {
                    user.setUpdateBy(operName);
                    this.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getLoginName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getLoginName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            //successMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下："+failureMsg);
            throw new BusinessException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 用户状态修改
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int changeStatus(User user, HttpServletRequest request) {
        if (User.isAdmin(user)) {
            throw new BusinessException("不允许修改超级管理员用户");
        }
        if (JwtUtil.getTokenUser(request).getUserId() == user.getUserId()) {
            throw new BusinessException("不允许停用本人");
        }
        return userMapper.updateUser(user);
    }

    @Autowired
    private IDevCompanyService devCompanyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int register(User user) throws Exception {
        //新增公司信息
        DevCompany company = new DevCompany();
        company.setComName(user.getComName());
        company.setComType(0);
        company.setCreateTime(new Date());
        if (StringUtils.isEmpty(user.getComName())) {
            throw new BusinessException("请输入公司名称");
        }
        company.setLoginNumber(ChineseCharacterUtil.convertHanzi2Pinyin(user.getComName(),false).toUpperCase());
        companyMapper.insertDevCompany(company);
        //新增用户信息
        user.randomSalt();
        user.setPassword(PasswordUtil.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setSign(1);
        user.setTag("1");
        user.setUserName("普通用户");
        user.setCompanyId(company.getCompanyId());
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        //配置角色
        Role role = roleMapper.selectRoleById(2L);
        if (role == null) {
            throw new Exception("注册失败");
        }
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(new UserRole(user.getUserId(), role.getRoleId()));
        userRoleMapper.batchUserRole(userRoles);
        return rows;
    }

    private void createFile(String path) {
        //创建对应的总文件夹
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        file.mkdir();
    }

    /**
     * 查询对应的公司的所以的员工信息
     *
     * @return
     */
    @Override
    public List<User> selectComAllUser() {
        User user = JwtUtil.getTokenUser(ServletUtils.getRequest());
        if (user == null) return Collections.emptyList();
        return userMapper.selectComAllUser(user.getCompanyId());
    }

    /**
     * 更新用户的登录标记为0
     *
     * @param user
     * @return
     */
    @Override
    public int changeLoginTag(User user, HttpServletRequest request) {
        // 更新用户登录标记
        user.setLoginTag(UserConstants.LOGIN_TAG_ADD);
        return userMapper.updateUser(user);
    }

    /**
     * 获取系统登录用户
     *
     * @return
     */
    @Override
    public User getSysUser(Cookie[] cookies) {
        return JwtUtil.getTokenCookie(cookies);
    }

    /**
     * 查询用户二维码
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public List<UserQrCode> selectUserQrCode(User user) {
        return userMapper.selectUserQrCode(user);
    }

    @Override
    public User selectUserInfo(Long userId) {
        return userMapper.selectUserInfoById(userId.intValue());
    }

    /**
     * 用户app交互查询用户列表
     *
     * @return 结果
     */
    @Override
    public List<UserApp> appSelectUserList(UserApp userApp) {
        User user = JwtUtil.getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        return userMapper.appSelectUserList(user.getCompanyId());
    }

    /**
     * 查询用户信息
     *
     * @param uid
     * @return
     */
    @Override
    public String selectUserInfo(Integer uid) {
        User user = userMapper.selectUserInfoById(uid);
        if (user != null && StringUtils.isNotEmpty(user.getAvatar())) {
            return user.getAvatar();
        }
        return null;
    }

    /**
     * 更新用户语言版本
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserLangVersion(User user) {
        return userMapper.updateUserLangVersion(user);
    }
}
