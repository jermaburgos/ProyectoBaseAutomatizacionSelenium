package report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class SupabaseReportService {

    public void enviarSiConfigurado(
            Path reportPath,
            ZonedDateTime executionStartedAt,
            ZonedDateTime executionFinishedAt,
            String veredictoFinal,
            List<ExecutionTestResult> executionTests,
            List<String> xmlTestNames
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

        LinkedHashSet<String> nombresEjecucion = new LinkedHashSet<>();
        if (xmlTestNames != null) {
            for (String name : xmlTestNames) {
                if (name != null && !name.isBlank()) {
                    nombresEjecucion.add(name);
                }
            }
        }

        if (nombresEjecucion.isEmpty()) {
            nombresEjecucion.add("testng");
        }

        for (String testName : nombresEjecucion) {
            List<ExecutionTestResult> resultadosDelTest =
                    filtrarResultadosPorContexto(executionTests, testName);

            GroupStatistics estadisticasDelTest =
                    calcularEstadisticas(resultadosDelTest);

            String veredictoDelTest =
                    estadisticasDelTest.isApproved(
                            Double.parseDouble(System.getProperty("approval.threshold", "95"))
                    )
                            ? "APROBADO"
                            : "NO APROBADO";

            ExecutionRunPayload payload = construirPayload(
                    executionStartedAt,
                    executionFinishedAt,
                    veredictoDelTest,
                    reportPath,
                    resultadosDelTest,
                    testName,
                    estadisticasDelTest
            );

            enviarPayload(functionUrl, ingestToken, testName, payload);
        }
    }

    private ExecutionRunPayload construirPayload(
            ZonedDateTime executionStartedAt,
            ZonedDateTime executionFinishedAt,
            String veredictoDelTest,
            Path reportPath,
            List<ExecutionTestResult> executionTests,
            String executionTestName,
            GroupStatistics statistics
    ) {
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

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("project", "ProyectoBaseAutomatizacionSelenium");
        metadata.put("report_scope", System.getProperty("report.scope", "business"));
        metadata.put("report_file", reportPath != null ? reportPath.toString() : "N/A");
        metadata.put("xml_test_name", executionTestName);
        metadata.put("workflow_mode", System.getProperty("workflow.mode", "unknown"));
        metadata.put("workflow_identifier", System.getProperty("workflow.identifier", executionTestName));
        metadata.put("github_run_id", System.getProperty("github.run_id", ""));
        metadata.put("execution_started_at", formatearTimestampSupabase(executionStartedAt));
        metadata.put("execution_finished_at", formatearTimestampSupabase(executionFinishedAt));

        return new ExecutionRunPayload(
                executionTestName,
                System.getProperty("browser", "chrome"),
                Boolean.parseBoolean(System.getProperty("headless", "false")),
                formatearTimestampSupabase(executionStartedAt != null ? executionStartedAt : ZonedDateTime.now()),
                formatearTimestampSupabase(executionFinishedAt != null ? executionFinishedAt : ZonedDateTime.now()),
                executionStartedAt != null && executionFinishedAt != null
                        ? Duration.between(executionStartedAt, executionFinishedAt).toMillis()
                        : 0L,
                statistics.getTotal(),
                statistics.getPassed(),
                statistics.getFailed(),
                statistics.getSkipped(),
                statistics.getApprovalPercentage(),
                veredictoDelTest,
                reportName,
                "text/html",
                reportBase64,
                metadata,
                executionTests
        );
    }

    private void enviarPayload(
            String functionUrl,
            String ingestToken,
            String testName,
            ExecutionRunPayload payload
    ) {
        try {
            SupabaseExecutionClient client = new SupabaseExecutionClient(functionUrl, ingestToken);
            String response = client.enviar(payload);
            System.out.println(
                    "Supabase execution sync response for test '"
                            + testName
                            + "': "
                            + response
            );
        } catch (Exception e) {
            System.out.println(
                    "No fue posible enviar la ejecución a Supabase para el test '"
                            + testName
                            + "': "
                            + e.getMessage()
            );
        }
    }

    private List<ExecutionTestResult> filtrarResultadosPorContexto(
            List<ExecutionTestResult> executionTests,
            String executionTestName
    ) {
        List<ExecutionTestResult> resultados = new ArrayList<>();

        if (executionTests == null || executionTests.isEmpty()) {
            return resultados;
        }

        for (ExecutionTestResult result : executionTests) {
            if (result != null && executionTestName.equals(result.getTestContextName())) {
                resultados.add(result);
            }
        }

        return resultados;
    }

    private GroupStatistics calcularEstadisticas(List<ExecutionTestResult> executionTests) {
        GroupStatistics statistics = new GroupStatistics();

        for (ExecutionTestResult result : executionTests) {
            statistics.incrementTotal();

            String status = result.getStatus();
            if ("PASSED".equals(status)) {
                statistics.incrementPassed();
            } else if ("FAILED".equals(status)) {
                statistics.incrementFailed();
            } else if ("SKIPPED".equals(status)) {
                statistics.incrementSkipped();
            }
        }

        return statistics;
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
