package org.dromara.edu.job;

import cn.idev.excel.FastExcel;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入文件解析器（xlsx / csv）。
 *
 * 读成「表头名 → 单元格文本」的行列表；表头缺失或与模板列顺序不一致时直接拒绝
 * （REQ-STU-052：模板列顺序固定）。全空行跳过；xlsx 用 fastexcel，csv 按引号规则做最小解析。
 *
 * @author Codex
 */
@Slf4j
@Component
public class EduImportFileReader {

    /** 表头所在行（第 1 行） */
    private static final int HEADER_INDEX = 0;

    private static final long MAX_ROW_COUNT = 5000L;

    public List<EduImportRow> read(byte[] bytes, String fileName, List<String> expectedHeaders) {
        if (bytes == null || bytes.length == 0) {
            throw new ServiceException("文件内容为空，请重新上传");
        }
        List<Map<Integer, String>> raw = isCsv(fileName) ? readCsv(bytes) : readExcel(bytes);
        if (raw.size() <= HEADER_INDEX) {
            throw new ServiceException("文件没有表头行，请使用最新模板");
        }
        Map<Integer, String> headerCells = raw.get(HEADER_INDEX);
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < expectedHeaders.size(); i++) {
            String actual = headerCells == null ? null : trim(headerCells.get(i));
            if (!expectedHeaders.get(i).equals(actual)) {
                throw new ServiceException("第 " + (i + 1) + " 列表头应为「" + expectedHeaders.get(i)
                    + "」，实际为「" + StringUtils.defaultString(actual) + "」；请使用最新模板（模板列顺序固定，REQ-STU-052）");
            }
            headers.add(actual);
        }
        List<EduImportRow> rows = new ArrayList<>();
        for (int r = HEADER_INDEX + 1; r < raw.size(); r++) {
            Map<Integer, String> cells = raw.get(r);
            if (cells == null) {
                continue;
            }
            Map<String, String> byName = new LinkedHashMap<>();
            boolean blank = true;
            for (int i = 0; i < headers.size(); i++) {
                String value = trim(cells.get(i));
                byName.put(headers.get(i), value);
                if (StringUtils.isNotBlank(value)) {
                    blank = false;
                }
            }
            if (blank) {
                continue;
            }
            EduImportRow row = new EduImportRow();
            row.setRowNo(r + 1);
            row.setCells(byName);
            rows.add(row);
        }
        if (rows.size() > MAX_ROW_COUNT) {
            throw new ServiceException("单批不得超过 " + MAX_ROW_COUNT + " 行（REQ-IMP-051），当前 "
                + rows.size() + " 行，请拆分后重新上传");
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private List<Map<Integer, String>> readExcel(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            List<Object> raw = FastExcel.read(in).sheet().headRowNumber(0).doReadSync();
            List<Map<Integer, String>> result = new ArrayList<>();
            for (Object item : raw) {
                result.add(item instanceof Map<?, ?> map ? (Map<Integer, String>) map : null);
            }
            return result;
        } catch (Exception e) {
            throw new ServiceException("解析 Excel 失败：" + e.getMessage() + "，请确认为 .xlsx 且未损坏");
        }
    }

    private List<Map<Integer, String>> readCsv(byte[] bytes) {
        String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        if (text.startsWith("\ufeff")) {
            text = text.substring(1);
        }
        List<Map<Integer, String>> result = new ArrayList<>();
        for (String line : text.split("\r?\n")) {
            if (line.isEmpty()) {
                continue;
            }
            List<String> values = splitCsvLine(line);
            Map<Integer, String> cells = new LinkedHashMap<>();
            for (int i = 0; i < values.size(); i++) {
                cells.put(i, values.get(i));
            }
            result.add(cells);
        }
        return result;
    }

    /** CSV 行解析：支持双引号包裹与 "" 转义 */
    private List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    private boolean isCsv(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".csv");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
