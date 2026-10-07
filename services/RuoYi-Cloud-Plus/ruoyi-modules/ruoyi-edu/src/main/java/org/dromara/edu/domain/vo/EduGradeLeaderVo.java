package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduGradeLeader;

import java.io.Serial;
import java.io.Serializable;

/**
 * 年级主任任职视图对象
 *
 * 年级主任 DS-05 数据范围的判定依据；userName / teacherName 由自定义 SQL 或账号服务填充。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduGradeLeader.class)
public class EduGradeLeaderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long leaderId;

    private Long gradeId;

    private String gradeName;

    private Long termId;

    private Long userId;

    private String userName;

    private Long teacherId;

    private String teacherName;

    /** 1 是 / 0 否 */
    private String isPrimary;

    /** 1 在职 / 0 离任 */
    private String status;

}
