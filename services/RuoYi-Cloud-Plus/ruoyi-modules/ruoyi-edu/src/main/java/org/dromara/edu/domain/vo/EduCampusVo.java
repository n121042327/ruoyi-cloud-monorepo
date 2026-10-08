package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduCampus;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 校区视图对象
 *
 * classCount 为展示统计；校区不参与数据权限判定（BR-ORG-009 / GAP-045）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduCampus.class)
public class EduCampusVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long campusId;

    private Long schoolId;

    private String campusCode;

    private String campusName;

    private String address;

    private String leaderName;

    private String leaderPhone;

    private String campusStatus;

    /** 班级数（展示统计） */
    private Integer classCount;

    private Date updateTime;

}
