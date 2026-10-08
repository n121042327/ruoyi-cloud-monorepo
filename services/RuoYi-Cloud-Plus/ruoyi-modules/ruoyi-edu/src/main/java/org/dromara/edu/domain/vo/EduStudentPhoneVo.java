package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学生联系电话视图对象
 *
 * 与前端 `apps/plus-ui/src/api/edu/student/index.ts` 的 `viewStudentPhone` 声明一致：
 * 响应体形如 `{ studentPhone: "..." }`（`REQ-STU-013` / `CR-046`）。
 *
 * @author Codex
 */
@Data
public class EduStudentPhoneVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 完整联系电话（接口本身只对持有 read_contact 的调用方开放） */
    private String studentPhone;

}
