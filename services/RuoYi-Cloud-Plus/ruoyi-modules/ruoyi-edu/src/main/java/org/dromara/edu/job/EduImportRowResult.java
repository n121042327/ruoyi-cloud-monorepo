package org.dromara.edu.job;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 导入执行的行结果。
 *
 * @author Codex
 */
@Data
public class EduImportRowResult {

    /** success 成功 / skipped 跳过（幂等）/ failed 失败 */
    private String result;

    private String failReason;

    /**
     * 附加输出（如学生导入生成的学号），用于写「学号对照表」（REQ-STU-058）。
     * 只有成功行才需要填。
     */
    private Map<String, String> extras = new HashMap<>();

    public static EduImportRowResult success() {
        return of("success", null);
    }

    public static EduImportRowResult skipped(String reason) {
        return of("skipped", reason);
    }

    public static EduImportRowResult failed(String reason) {
        return of("failed", reason);
    }

    /** 带附加输出的成功结果（如学生导入回填学号） */
    public static EduImportRowResult success(Map<String, String> extras) {
        EduImportRowResult row = success();
        if (extras != null) {
            row.setExtras(extras);
        }
        return row;
    }

    private static EduImportRowResult of(String result, String reason) {
        EduImportRowResult row = new EduImportRowResult();
        row.setResult(result);
        row.setFailReason(reason);
        return row;
    }
}
