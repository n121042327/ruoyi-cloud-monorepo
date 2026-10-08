package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduUserRole;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户教育角色视图对象（学校级角色：校领导 / 教务主任）
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduUserRole.class)
public class EduUserRoleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userRoleId;

    private Long schoolId;

    private Long userId;

    private String userName;

    private Long teacherId;

    private String teacherName;

    private String eduRole;

    /** 1 启用 / 0 停用 */
    private String status;

    private Date startDate;

    private Date endDate;

}
