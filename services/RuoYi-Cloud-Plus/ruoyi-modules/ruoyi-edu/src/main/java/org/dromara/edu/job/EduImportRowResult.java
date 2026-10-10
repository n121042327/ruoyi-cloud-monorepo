package org.dromara.edu.job;

import lombok.Data;

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

    public static EduImportRowResult success() {
        return of("success", null);
    }

    public static EduImportRowResult skipped(String reason) {
        return of("skipped", reason);
    }

    public static EduImportRowResult failed(String reason) {
        return of("failed", reason);
    }

    private static EduImportRowResult of(String result, String reason) {
        EduImportRowResult row = new EduImportRowResult();
        row.setResult(result);
        row.setFailReason(reason);
        return row;
    }
}
