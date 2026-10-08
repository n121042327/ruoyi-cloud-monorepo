package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduCampus;

/**
 * 校区业务对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduCampus.class, reverseConvertGenerate = false)
public class EduCampusBo extends BaseEntity {

    /** 校区 ID */
    private Long campusId;

    /** 学校 */
    private Long schoolId;

    /** 校区编码（校内唯一） */
    @NotBlank(message = "校区编码不能为空")
    private String campusCode;

    /** 校区名称（校内唯一） */
    @NotBlank(message = "校区名称不能为空")
    private String campusName;

    /** 地址 */
    private String address;

    /** 负责人 */
    private String leaderName;

    /** 负责人电话 */
    private String leaderPhone;

    /** 校区状态 */
    private String campusStatus;

    /** 关键字：校区名称 / 编码 */
    private String keyword;

    /** 停用原因（removeCampus 必填，至少 5 个字） */
    private String reason;

}
