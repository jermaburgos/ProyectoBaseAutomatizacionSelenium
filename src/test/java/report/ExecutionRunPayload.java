package report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExecutionRunPayload {

    private final String xmlTestName;
    private final String browser;
    private final boolean headless;
    private final String startedAt;
    private final String finishedAt;
    private final long durationMs;
    private final int totalTests;
    private final int passedTests;
    private final int failedTests;
    private final int skippedTests;
    private final double approvalPercentage;
    private final String verdict;
    private final String reportName;
    private final String reportMimeType;
    private final String reportBase64;
    private final Map<String, Object> metadata;
    private final List<ExecutionTestResult> tests;

    public ExecutionRunPayload(
            String xmlTestName,
            String browser,
            boolean headless,
            String startedAt,
            String finishedAt,
            long durationMs,
            int totalTests,
            int passedTests,
            int failedTests,
            int skippedTests,
            double approvalPercentage,
            String verdict,
            String reportName,
            String reportMimeType,
            String reportBase64,
            Map<String, Object> metadata,
            List<ExecutionTestResult> tests
    ) {
        this.xmlTestName = xmlTestName;
        this.browser = browser;
        this.headless = headless;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationMs = durationMs;
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.skippedTests = skippedTests;
        this.approvalPercentage = approvalPercentage;
        this.verdict = verdict;
        this.reportName = reportName;
        this.reportMimeType = reportMimeType;
        this.reportBase64 = reportBase64;
        this.metadata = metadata;
        this.tests = new ArrayList<>(tests);
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        appendString(json, "xml_test_name", xmlTestName);
        appendString(json, "browser", browser);
        appendBoolean(json, "headless", headless);
        appendString(json, "started_at", startedAt);
        appendString(json, "finished_at", finishedAt);
        appendNumber(json, "duration_ms", durationMs);
        appendNumber(json, "total_tests", totalTests);
        appendNumber(json, "passed_tests", passedTests);
        appendNumber(json, "failed_tests", failedTests);
        appendNumber(json, "skipped_tests", skippedTests);
        appendNumber(json, "approval_percentage", approvalPercentage);
        appendString(json, "verdict", verdict);
        appendNullableString(json, "report_name", reportName);
        appendNullableString(json, "report_mime_type", reportMimeType);
        appendNullableString(json, "report_base64", reportBase64);
        appendObject(json, "metadata", metadata);
        appendArray(json, "tests", tests);
        trimTrailingComma(json);
        json.append("}");
        return json.toString();
    }

    private static void appendString(StringBuilder json, String key, String value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        json.append("\"").append(escapeJson(value)).append("\",");
    }

    private static void appendNullableString(StringBuilder json, String key, String value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("null,");
        } else {
            json.append("\"").append(escapeJson(value)).append("\",");
        }
    }

    private static void appendBoolean(StringBuilder json, String key, boolean value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        json.append(value).append(",");
    }

    private static void appendNumber(StringBuilder json, String key, Number value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        json.append(value).append(",");
    }

    private static void appendObject(StringBuilder json, String key, Map<String, Object> value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        json.append("{");
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                json.append("null,");
            } else if (entryValue instanceof Boolean || entryValue instanceof Number) {
                json.append(entryValue).append(",");
            } else {
                json.append("\"").append(escapeJson(String.valueOf(entryValue))).append("\",");
            }
        }
        trimTrailingComma(json);
        json.append("},");
    }

    private static void appendArray(StringBuilder json, String key, List<ExecutionTestResult> value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        json.append("[");
        for (ExecutionTestResult item : value) {
            json.append(item.toJson()).append(",");
        }
        trimTrailingComma(json);
        json.append("],");
    }

    private static void trimTrailingComma(StringBuilder json) {
        int length = json.length();
        if (length > 0 && json.charAt(length - 1) == ',') {
            json.deleteCharAt(length - 1);
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
