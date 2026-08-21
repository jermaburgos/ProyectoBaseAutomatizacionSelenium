package report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SupabaseReportService {

    public void enviarSiConfigurado(
            Path reportPath,
            ZonedDateTime executionStartedAt,
            ZonedDateTime executionFinishedAt,
            GroupStatistics overallStatistics,
            String veredictoFinal,
            List<ExecutionTestResult> executionTests
    ) {
        String functionUrl = firstNonBlank(
                System.getProperty("supabase.ingest.url"),
                System.getenv("SUPABASE_INGEST_URL")
        );

        String ingestToken = firstNonBlank(
                System.getProperty("supabase.ingest.token"),
                System.getenv("SUPABASE_INGEST_TOKEN")
        );

        if (!configuracionSupabaseCompleta(functionUrl, ingestToken)) {
            return;
        }

        String reportName = null;
        String reportBase64 = null;

        if (reportPath != null && Files.exists(reportPath)) {
            reportName = reportPath.getFileName().toString();

            try {
                reportBase64 = SupabaseExecutionClient.fileToBase64(reportPath);
            } catch (IOException e) {
                System.out.println(
                        "No fue posible leer el reporte HTML para enviarlo a Supabase: "
                                + e.getMessage()
                );
            }
        }

        ExecutionRunPayload payload = construirPayload(
                executionStartedAt,
                executionFinishedAt,
                overallStatistics,
                veredictoFinal,
                reportPath,
                reportName,
                reportBase64,
                executionTests
        );

        try {
            SupabaseExecutionClient client = new SupabaseExecutionClient(functionUrl, ingestToken);
            String response = client.enviar(payload);
            System.out.println("Supabase execution sync response: " + response);
        } catch (Exception e) {
            System.out.println(
                    "No fue posible enviar la ejecución a Supabase: "
                            + e.getMessage()
            );
        }
    }

    private ExecutionRunPayload construirPayload(
            ZonedDateTime executionStartedAt,
            ZonedDateTime executionFinishedAt,
            GroupStatistics overallStatistics,
            String veredictoFinal,
            Path reportPath,
            String reportName,
            String reportBase64,
            List<ExecutionTestResult> executionTests
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("project", "ProyectoBaseAutomatizacionSelenium");
        metadata.put("report_scope", System.getProperty("report.scope", "business"));
        metadata.put("report_file", reportPath != null ? reportPath.toString() : "N/A");
        metadata.put("execution_started_at", formatearTimestampSupabase(executionStartedAt));
        metadata.put("execution_finished_at", formatearTimestampSupabase(executionFinishedAt));

        return new ExecutionRunPayload(
                System.getProperty("suite.name", "testng"),
                System.getProperty("browser", "chrome"),
                Boolean.parseBoolean(System.getProperty("headless", "false")),
                formatearTimestampSupabase(executionStartedAt != null ? executionStartedAt : ZonedDateTime.now()),
                formatearTimestampSupabase(executionFinishedAt != null ? executionFinishedAt : ZonedDateTime.now()),
                executionStartedAt != null && executionFinishedAt != null
                        ? Duration.between(executionStartedAt, executionFinishedAt).toMillis()
                        : 0L,
                overallStatistics.getTotal(),
                overallStatistics.getPassed(),
                overallStatistics.getFailed(),
                overallStatistics.getSkipped(),
                overallStatistics.getApprovalPercentage(),
                veredictoFinal,
                reportName,
                "text/html",
                reportBase64,
                metadata,
                executionTests
        );
    }

    private boolean configuracionSupabaseCompleta(String functionUrl, String ingestToken) {
        boolean faltaUrl = functionUrl == null || functionUrl.isBlank();
        boolean faltaToken = ingestToken == null || ingestToken.isBlank();

        if (faltaUrl || faltaToken) {
            System.out.println(
                    "Integración Supabase deshabilitada: faltan "
                            + (faltaUrl ? "supabase.ingest.url o SUPABASE_INGEST_URL" : "")
                            + (faltaUrl && faltaToken ? " y " : "")
                            + (faltaToken ? "supabase.ingest.token o SUPABASE_INGEST_TOKEN" : "")
            );
            return false;
        }

        return true;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return null;
    }

    private String formatearTimestampSupabase(ZonedDateTime fechaHora) {
        if (fechaHora == null) {
            return null;
        }

        return fechaHora.toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
