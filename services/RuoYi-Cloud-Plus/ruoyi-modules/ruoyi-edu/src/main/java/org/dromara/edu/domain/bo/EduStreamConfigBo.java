package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduStreamConfig;

/**
 * 选科配置业务对象（saveStreamConfig）
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduStreamConfig.class, reverseConvertGenerate = false)
public class EduStreamConfigBo extends BaseEntity {

    /** 配置 ID */
    private Long configId;

    /** 学校 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 开放期起点（yyyy-MM-dd HH:mm:ss） */
    private String openFrom;

    /** 截止时间（yyyy-MM-dd HH:mm:ss） */
    private String deadline;

    /** 逾期变更是否需校级管理员审批（BR-STREAM-005） */
    private Boolean overdueRequiresApproval;

    /** 生效 / 已失效 */
    private String configStatus;

}
