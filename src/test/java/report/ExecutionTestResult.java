package report;

import ai.FailureContext;
import org.testng.ITestResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ExecutionTestResult {

    private final String testName;
    private final String className;
    private final String groupName;
    private final String status;
    private final long durationMs;
    private final String errorMessage;
    private final String stacktrace;
    private final String lastStep;
    private final String lastLocator;
    private final String screenshotUrl;
    private final Map<String, Object> metadata;

    public ExecutionTestResult(
            String testName,
            String className,
            String groupName,
            String status,
            long durationMs,
            String errorMessage,
            String stacktrace,
            String lastStep,
            String lastLocator,
            String screenshotUrl,
            Map<String, Object> metadata
    ) {
        this.testName = testName;
        this.className = className;
        this.groupName = groupName;
        this.status = status;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
        this.stacktrace = stacktrace;
        this.lastStep = lastStep;
        this.lastLocator = lastLocator;
        this.screenshotUrl = screenshotUrl;
        this.metadata = metadata;
    }

    public static ExecutionTestResult from(
            ITestResult result,
            String status,
            FailureContext failure
    ) {
        String[] groups = result.getMethod().getGroups();
        StringJoiner joiner = new StringJoiner(", ");
        for (String group : groups) {
            joiner.add(group);
        }

        long durationMs = Math.max(0L, result.getEndMillis() - result.getStartMillis());

        String errorMessage = null;
        String stacktrace = null;
        String lastStep = null;
        String lastLocator = null;

        if (failure != null) {
            errorMessage = failure.getError();
            stacktrace = failure.getStackTrace();
            lastStep = failure.getUltimoPaso();
            lastLocator = failure.getLocator();
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("browser", System.getProperty("browser", "chrome"));
        metadata.put("headless", Boolean.parseBoolean(System.getProperty("headless", "false")));
        metadata.put("suite", result.getTestContext() != null ? result.getTestContext().getName() : "N/A");

        return new ExecutionTestResult(
                result.getMethod().getMethodName(),
                result.getTestClass().getName(),
                joiner.toString(),
                status,
                durationMs,
                errorMessage,
                stacktrace,
                lastStep,
                lastLocator,
                null,
                metadata
        );
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        appendString(json, "test_name", testName);
        appendString(json, "class_name", className);
        appendString(json, "group_name", groupName);
        appendString(json, "status", status);
        appendNumber(json, "duration_ms", durationMs);
        appendNullableString(json, "error_message", errorMessage);
        appendNullableString(json, "stacktrace", stacktrace);
        appendNullableString(json, "last_step", lastStep);
        appendNullableString(json, "last_locator", lastLocator);
        appendNullableString(json, "screenshot_url", screenshotUrl);
        appendObject(json, "metadata", metadata);
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

    private static void appendNumber(StringBuilder json, String key, long value) {
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
