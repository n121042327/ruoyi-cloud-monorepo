package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 校区 edu_campus
 *
 * 校区**不参与数据权限判定**，只用于组织与统计（BR-ORG-009 / GAP-045）：
 * 数据范围仍按学校 / 年级 / 班级解析，校区只作为班级的参考信息。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_campus")
public class EduCampus extends TenantEntity {

    /** 校区 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long campusId;

    /** 学校归属 */
    private Long schoolId;

    /** 校区编码（校内唯一） */
    private String campusCode;

    /** 校区名称（校内唯一） */
    private String campusName;

    /** 地址 */
    private String address;

    /** 负责人 */
    private String leaderName;

    /** 负责人电话 */
    private String leaderPhone;

    /** 校区状态：active 正常 / disabled 已停用 */
    private String campusStatus;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
