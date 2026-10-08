package org.dromara.edu.datascope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduDataGrant;
import org.dromara.edu.domain.EduDataGrantScope;
import org.dromara.edu.domain.EduGradeLeader;
import org.dromara.edu.domain.EduTeacher;
import org.dromara.edu.domain.EduTeachingAssignment;
import org.dromara.edu.domain.EduUserRole;
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduDataGrantMapper;
import org.dromara.edu.mapper.EduDataGrantScopeMapper;
import org.dromara.edu.mapper.EduGradeLeaderMapper;
import org.dromara.edu.mapper.EduTeacherMapper;
import org.dromara.edu.mapper.EduTeachingAssignmentMapper;
import org.dromara.edu.mapper.EduUserRoleMapper;
import org.dromara.system.api.model.LoginUser;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 教育域数据范围解析器
 *
 * 按 `docs/30-architecture/09-permission-architecture.md` 第 3 节的流程收集范围片段：
 * `edu_user_role`（校级角色）→ `edu_grade_leader`（年级主任 DS-05）→
 * `edu_class.head_teacher_id`（班主任 DS-06）→ `edu_teaching_assignment`（任课教师 DS-07），
 * 再叠加 `edu_data_grant` + `edu_data_grant_scope` 中**生效中**的共享授权（仅教学资源，只读）。
 *
 * 硬约束：缺少租户或执行人上下文一律拒绝（`DS-DENY-01` / `DS-DENY-02` / `NFR-SEC-05`）；
 * 不继承租户管理员的放行逻辑（`DS-DENY-05`）；`super_admin` 是**唯一例外**（`DP-07`），但仍要写审计。
 *
 * 缓存：本批用**进程内缓存**（60 秒 TTL，键含租户与用户），Redis 版本待接入
 * `ruoyi-common-redis` 后替换（见 CR-093）。
 *
 * @author Codex
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DataScopeResolver {

    /** 进程内缓存 TTL（毫秒） */
    private static final long CACHE_TTL_MILLIS = 60_000L;

    /** 生效中的授权状态 */
    private static final String GRANT_ACTIVE = "active";

    /** 首轮授权只开 read / export（BR-DATA-015） */
    private static final List<String> GRANT_READ_LEVELS = List.of("read", "export");

    /** 校级角色（有 school_id 就归入本校范围） */
    private static final String ROLE_ACTIVE = "1";

    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private final EduUserRoleMapper userRoleMapper;
    private final EduGradeLeaderMapper gradeLeaderMapper;
    private final EduClassMapper classMapper;
    private final EduTeacherMapper teacherMapper;
    private final EduTeachingAssignmentMapper teachingAssignmentMapper;
    private final EduDataGrantMapper dataGrantMapper;
    private final EduDataGrantScopeMapper dataGrantScopeMapper;

    /**
     * 解析当前请求的数据范围，并写入 {@link DataScopeContext}
     *
     * @return 范围片段
     */
    public DataScopeContext resolve() {
        DataScopeContext context = doResolve();
        DataScopeContext.set(context);
        return context;
    }

    /**
     * 解析但不写上下文（导出 / 异步任务等需要把范围快照带走的场景）
     *
     * @return 范围片段
     */
    public DataScopeContext resolveWithoutHold() {
        return doResolve();
    }

    /**
     * 失效指定用户的范围缓存（业务写操作后调用，`DataScopeCacheInvalidator` 会转发到这里）
     *
     * @param tenantId 租户
     * @param userId   用户
     */
    public void evict(String tenantId, Long userId) {
        if (StringUtils.isBlank(tenantId) || userId == null) {
            CACHE.clear();
            return;
        }
        CACHE.remove(cacheKey(tenantId, userId));
    }

    /**
     * 全量失效（角色 / 任职 / 授权变更后调用）
     */
    public void evictAll() {
        CACHE.clear();
    }

    private DataScopeContext doResolve() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new ServiceException("缺少执行人上下文，拒绝执行（DS-DENY-01）");
        }
        if (StringUtils.isBlank(loginUser.getTenantId())) {
            throw new ServiceException("缺少租户上下文，拒绝执行（DS-DENY-01）");
        }
        String key = cacheKey(loginUser.getTenantId(), loginUser.getUserId());
        CacheEntry cached = CACHE.get(key);
        if (cached != null && !cached.expired()) {
            return cached.context;
        }
        DataScopeContext context = new DataScopeContext();
        context.setUserId(loginUser.getUserId());
        context.setTenantId(loginUser.getTenantId());
        if (LoginHelper.isSuperAdmin()) {
            // super_admin 是数据范围的唯一例外（DP-07），但仍要写访问与写操作审计
            context.setUnrestricted(true);
            context.setScope(EduDataScope.PLATFORM);
        } else {
            collectRoleScope(context);
            collectGradeLeaderScope(context);
            collectHeadTeacherScope(context);
            collectTeachingScope(context);
            collectGrantScope(context);
        }
        CACHE.put(key, new CacheEntry(context));
        return context;
    }

    /** 校级角色：edu_user_role 生效记录带 school_id（校领导 / 教务主任 DS-04） */
    private void collectRoleScope(DataScopeContext context) {
        List<EduUserRole> roles = userRoleMapper.selectList(new LambdaQueryWrapper<EduUserRole>()
            .eq(EduUserRole::getUserId, context.getUserId())
            .eq(EduUserRole::getStatus, ROLE_ACTIVE));
        for (EduUserRole role : roles) {
            if (role.getSchoolId() != null) {
                context.getSchoolIds().add(role.getSchoolId());
                context.setScope(higher(context.getScope(), EduDataScope.SCHOOL));
            }
        }
    }

    /** 年级主任：edu_grade_leader 生效记录（DS-05，本年级只读） */
    private void collectGradeLeaderScope(DataScopeContext context) {
        List<EduGradeLeader> leaders = gradeLeaderMapper.selectList(new LambdaQueryWrapper<EduGradeLeader>()
            .eq(EduGradeLeader::getUserId, context.getUserId())
            .eq(EduGradeLeader::getStatus, ROLE_ACTIVE));
        for (EduGradeLeader leader : leaders) {
            if (leader.getGradeId() != null) {
                context.getGradeIds().add(leader.getGradeId());
                context.setScope(higher(context.getScope(), EduDataScope.GRADE));
            }
        }
    }

    /** 班主任：edu_class.head_teacher_id 指向本人（DS-06，本班只读） */
    private void collectHeadTeacherScope(DataScopeContext context) {
        List<EduClass> classes = classMapper.selectList(new LambdaQueryWrapper<EduClass>()
            .eq(EduClass::getHeadTeacherId, context.getUserId()));
        for (EduClass clazz : classes) {
            if (clazz.getClassId() != null) {
                context.getClassIds().add(clazz.getClassId());
                context.setScope(higher(context.getScope(), EduDataScope.CLASS));
            }
        }
    }

    /** 任课教师：edu_teaching_assignment 生效记录（DS-07，任教班级） */
    private void collectTeachingScope(DataScopeContext context) {
        EduTeacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<EduTeacher>()
            .eq(EduTeacher::getUserId, context.getUserId())
            .last("limit 1"));
        if (teacher == null || teacher.getTeacherId() == null) {
            return;
        }
        List<EduTeachingAssignment> assignments =
            teachingAssignmentMapper.selectList(new LambdaQueryWrapper<EduTeachingAssignment>()
                .eq(EduTeachingAssignment::getTeacherId, teacher.getTeacherId())
                .eq(EduTeachingAssignment::getStatus, ROLE_ACTIVE));
        for (EduTeachingAssignment assignment : assignments) {
            if (assignment.getClassId() == null) {
                continue;
            }
            if ("teaching".equals(assignment.getClassType())) {
                context.getTeachingClassIds().add(assignment.getClassId());
            } else {
                context.getClassIds().add(assignment.getClassId());
            }
            context.setScope(higher(context.getScope(), EduDataScope.TEACHING_CLASS));
        }
    }

    /**
     * 叠加共享授权：`edu_data_grant` 中生效中、且被授权对象是本租户的授权，
     * 取其 `edu_data_grant_scope` 里 read / export 的范围（`BR-DATA-012` / `BR-DATA-013` / `BR-DATA-015`）。
     */
    private void collectGrantScope(DataScopeContext context) {
        Date now = new Date();
        List<EduDataGrant> grants = dataGrantMapper.selectList(new LambdaQueryWrapper<EduDataGrant>()
            .eq(EduDataGrant::getGrantStatus, GRANT_ACTIVE)
            .eq(EduDataGrant::getGranteeType, "tenant")
            .eq(EduDataGrant::getGranteeId, context.getTenantId())
            .le(EduDataGrant::getEffectiveStart, now)
            .and(w -> w.isNull(EduDataGrant::getEffectiveEnd).or().ge(EduDataGrant::getEffectiveEnd, now)));
        if (grants.isEmpty()) {
            return;
        }
        List<Long> grantIds = grants.stream().map(EduDataGrant::getGrantId).toList();
        List<EduDataGrantScope> scopes = dataGrantScopeMapper.selectList(
            new LambdaQueryWrapper<EduDataGrantScope>()
                .in(EduDataGrantScope::getGrantId, grantIds)
                .in(EduDataGrantScope::getAccessLevel, GRANT_READ_LEVELS));
        for (EduDataGrantScope scope : scopes) {
            if (StringUtils.isBlank(scope.getScopeId())) {
                continue;
            }
            try {
                Long scopeId = Long.valueOf(scope.getScopeId().trim());
                switch (StringUtils.isBlank(scope.getScopeType()) ? "" : scope.getScopeType()) {
                    case "school_tenant" -> context.getSchoolIds().add(scopeId);
                    case "grade" -> context.getGradeIds().add(scopeId);
                    case "class" -> context.getClassIds().add(scopeId);
                    default -> log.debug("忽略未登记的授权范围类型：{}", scope.getScopeType());
                }
            } catch (NumberFormatException e) {
                log.warn("授权范围 ID 不是数字，已忽略：grantScopeId={}, scopeId={}",
                    scope.getGrantScopeId(), scope.getScopeId());
            }
        }
    }

    /** 取更宽（层级更小）的范围类型作为命中范围 */
    private EduDataScope higher(EduDataScope current, EduDataScope candidate) {
        if (current == null) {
            return candidate;
        }
        return candidate.getLevel() < current.getLevel() ? candidate : current;
    }

    private String cacheKey(String tenantId, Long userId) {
        return tenantId + ":" + userId;
    }

    /** 进程内缓存条目 */
    private static final class CacheEntry {
        private final DataScopeContext context;
        private final long expireAt;

        private CacheEntry(DataScopeContext context) {
            this.context = context;
            this.expireAt = System.currentTimeMillis() + CACHE_TTL_MILLIS;
        }

        private boolean expired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

}
