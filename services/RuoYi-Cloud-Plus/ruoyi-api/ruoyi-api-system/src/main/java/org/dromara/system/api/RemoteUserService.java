package org.dromara.system.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.system.api.domain.bo.RemoteUserBo;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.api.model.XcxLoginUser;

import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author Lion Li
 */
public interface RemoteUserService {

    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @param tenantId 租户id
     * @return 结果
     */
    LoginUser getUserInfo(String username, String tenantId) throws UserException;

    /**
     * 通过用户id查询用户信息
     *
     * @param userId   用户id
     * @param tenantId 租户id
     * @return 结果
     */
    LoginUser getUserInfo(Long userId, String tenantId) throws UserException;

    /**
     * 通过手机号查询用户信息
     *
     * @param phonenumber 手机号
     * @param tenantId    租户id
     * @return 结果
     */
    LoginUser getUserInfoByPhonenumber(String phonenumber, String tenantId) throws UserException;

    /**
     * 通过邮箱查询用户信息
     *
     * @param email    邮箱
     * @param tenantId 租户id
     * @return 结果
     */
    LoginUser getUserInfoByEmail(String email, String tenantId) throws UserException;

    /**
     * 通过openid查询用户信息
     *
     * @param openid openid
     * @return 结果
     */
    XcxLoginUser getUserInfoByOpenid(String openid) throws UserException;

    /**
     * 注册用户信息
     *
     * @param remoteUserBo 用户信息
     * @return 结果
     */
    Boolean registerUserInfo(RemoteUserBo remoteUserBo) throws UserException, ServiceException;

    /**
     * 重置指定用户的登录密码
     *
     * 教育域教师 / 学生账号重置入口（`GAP-091`）。入参收**明文密码**，由 provider 侧加密后落库，
     * 避免摘要值在服务之间传输。
     *
     * @param userId      用户id
     * @param rawPassword 明文新密码
     * @param tenantId    租户id；为空时按调用方当前租户处理
     * @return 结果
     */
    Boolean resetPassword(Long userId, String rawPassword, String tenantId) throws ServiceException;

    /**
     * 修改指定用户的账号状态（启用 / 停用）
     *
     * 教育域教师账号启停用入口（`GAP-091`）。状态码取 `UserStatus`：`0` 正常 / `1` 停用。
     * provider 侧在停用时会让该用户已签发的 token 立即失效。
     *
     * @param userId   用户id
     * @param status   账号状态码
     * @param tenantId 租户id；为空时按调用方当前租户处理
     * @return 结果
     */
    Boolean changeAccountStatus(Long userId, String status, String tenantId) throws ServiceException;

    /**
     * 通过userId查询用户账户
     *
     * @param userId 用户id
     * @return 结果
     */
    String selectUserNameById(Long userId);

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    String selectNicknameById(Long userId);

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户昵称
     */
    String selectNicknameByIds(String userIds);

    /**
     * 通过用户ID查询用户手机号
     *
     * @param userId 用户id
     * @return 用户手机号
     */
    String selectPhonenumberById(Long userId);

    /**
     * 通过用户ID查询用户邮箱
     *
     * @param userId 用户id
     * @return 用户邮箱
     */
    String selectEmailById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param ip     IP地址
     */
    void recordLoginInfo(Long userId, String ip);

    /**
     * 通过用户ID查询用户列表
     *
     * @param userIds 用户ids
     * @return 用户列表
     */
    List<RemoteUserVo> selectListByIds(List<Long> userIds);

    /**
     * 通过角色ID查询用户ID
     *
     * @param roleIds 角色ids
     * @return 用户ids
     */
    List<Long> selectUserIdsByRoleIds(List<Long> roleIds);

    /**
     * 通过角色ID查询用户
     *
     * @param roleIds 角色ids
     * @return 用户
     */
    List<RemoteUserVo> selectUsersByRoleIds(List<Long> roleIds);

    /**
     * 通过部门ID查询用户
     *
     * @param deptIds 部门ids
     * @return 用户
     */
    List<RemoteUserVo> selectUsersByDeptIds(List<Long> deptIds);

    /**
     * 通过岗位ID查询用户
     *
     * @param postIds 岗位ids
     * @return 用户
     */
    List<RemoteUserVo> selectUsersByPostIds(List<Long> postIds);

    /**
     * 根据用户 ID 列表查询用户昵称映射关系
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户昵称
     */
    Map<Long, String> selectUserNicksByIds(List<Long> userIds);

}
