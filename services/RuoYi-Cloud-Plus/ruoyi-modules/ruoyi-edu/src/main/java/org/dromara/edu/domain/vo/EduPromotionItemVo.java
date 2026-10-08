package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduPromotionItem;

import java.io.Serial;
import java.io.Serializable;

/**
 * 升班明细视图对象（与前端 PromotionItemVO 对齐）
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduPromotionItem.class)
public class EduPromotionItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long itemId;

    private Long taskId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private Long sourceClassId;

    private String sourceClassName;

    private Long targetClassId;

    private String targetClassName;

    /** promote / repeat / transfer / graduate / skip */
    private String resultType;

    /** pending / success / failed / skipped */
    private String status;

    /** 数据库原值：待处理 / 成功 / 失败 / 已跳过 */
    private String itemStatus;

    private String errorMsg;

    private String adjustMode;

    private String remark;

}
