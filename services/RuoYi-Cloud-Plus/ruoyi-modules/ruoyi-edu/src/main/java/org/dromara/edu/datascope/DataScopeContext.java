package org.dromara.edu.datascope;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 当前请求的数据范围片段
 *
 * 解析发生在**接口入口**，注入发生在**查询构造时**，两者必须在同一请求内完成
 * （`docs/30-architecture/09-permission-architecture.md` 第 3 节要点 2）。
 * 解析结果为空集时**返回空结果**，不是拒绝也不是全量（`DS-DENY-03`）。
 *
 * 载体用 ThreadLocal：请求线程内解析一次、查询与导出复用；请求结束必须 `clear()`。
 *
 * @author Codex
 */
@Data
public class DataScopeContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<DataScopeContext> HOLDER = new ThreadLocal<>();

    /** 执行人 */
    private Long userId;

    /** 租户上下文 */
    private String tenantId;

    /** 命中的最高范围类型 */
    private EduDataScope scope;

    /** 是否不受范围限制（仅 super_admin / DS-01 平台运营） */
    private boolean unrestricted;

    /** 可访问学校 */
    private Set<Long> schoolIds = new HashSet<>();

    /** 可访问年级 */
    private Set<Long> gradeIds = new HashSet<>();

    /** 可访问行政班 */
    private Set<Long> classIds = new HashSet<>();

    /** 可访问教学班 */
    private Set<Long> teachingClassIds = new HashSet<>();

    /**
     * 范围是否为空集。
     * 空集时所有查询都要退化为「返回空列表」，绝不退化为全量（`DS-DENY-03` / `REQ-AUD-024`）。
     *
     * @return true 表示空集
     */
    public boolean isEmpty() {
        return !unrestricted && schoolIds.isEmpty() && gradeIds.isEmpty()
            && classIds.isEmpty() && teachingClassIds.isEmpty();
    }

    public Set<Long> getSchoolIds() {
        return schoolIds == null ? Collections.emptySet() : schoolIds;
    }

    public Set<Long> getGradeIds() {
        return gradeIds == null ? Collections.emptySet() : gradeIds;
    }

    public Set<Long> getClassIds() {
        return classIds == null ? Collections.emptySet() : classIds;
    }

    public Set<Long> getTeachingClassIds() {
        return teachingClassIds == null ? Collections.emptySet() : teachingClassIds;
    }

    /**
     * 写入当前请求上下文
     *
     * @param context 范围片段
     */
    public static void set(DataScopeContext context) {
        HOLDER.set(context);
    }

    /**
     * 读取当前请求上下文
     *
     * @return 范围片段；未解析时为 null
     */
    public static DataScopeContext get() {
        return HOLDER.get();
    }

    /**
     * 清理当前请求上下文（请求结束必须调用，避免线程复用串数据）
     */
    public static void clear() {
        HOLDER.remove();
    }

}
