package org.dromara.edu.datascope;

/**
 * 教育域数据范围类型（九类）
 *
 * 取值与层级来自 `docs/10-prd/06-field-dictionary.yaml` 的 `data_scope_type`：
 * `platform`(0) / `tenant_org`(1) / `group_own`(2) / `school`(3) / `grade`(4) /
 * `class`(5) / `teaching_class`(5) / `self`(6) / `self_children`(6)。
 *
 * 见 `docs/30-architecture/09-permission-architecture.md` 第 2 节（四层范围）与第 4.1 节（组件清单）。
 *
 * @author Codex
 */
public enum EduDataScope {

    /** 全平台（仅平台运营 DS-01） */
    PLATFORM("platform", "全平台", 0),
    /** 租户组织 */
    TENANT_ORG("tenant_org", "租户组织", 1),
    /** 集团自有 */
    GROUP_OWN("group_own", "集团自有", 2),
    /** 本校（校领导 / 教务主任 DS-04） */
    SCHOOL("school", "本校", 3),
    /** 本年级（年级主任 DS-05） */
    GRADE("grade", "本年级", 4),
    /** 本班（班主任 DS-06） */
    CLASS("class", "本班", 5),
    /** 任教班级（任课教师 DS-07） */
    TEACHING_CLASS("teaching_class", "任教班级", 5),
    /** 本人 */
    SELF("self", "本人", 6),
    /** 本人子女 */
    SELF_CHILDREN("self_children", "本人子女", 6);

    private final String code;
    private final String label;
    private final int level;

    EduDataScope(String code, String label, int level) {
        this.code = code;
        this.label = label;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 按字典码取枚举；取不到返回 {@code null}（调用方自行决定拒绝或放行）。
     *
     * @param code 字典码
     * @return 枚举；未登记时返回 null
     */
    public static EduDataScope fromCode(String code) {
        for (EduDataScope scope : values()) {
            if (scope.code.equals(code)) {
                return scope;
            }
        }
        return null;
    }

}
