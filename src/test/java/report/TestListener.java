package report;

import ai.FailureContext;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.ExtentTest;
import context.ContextManager;
import driver.driverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import com.aventstack.extentreports.ExtentReports;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestListener implements ITestListener, IExecutionListener {
    private static ExtentReports extent =
            ExtentManager.getInstance();

    private static ThreadLocal<ExtentTest> test =
            new ThreadLocal<>();

    private final ListenerReportHelper reportHelper =
            new ListenerReportHelper();

    private final FailureContextBuilder failureContextBuilder =
            new FailureContextBuilder();

    private final SupabaseReportService supabaseReportService =
            new SupabaseReportService();

    private final Map<String, GroupStatistics> groupStatistics =
            new ConcurrentHashMap<>();

    private final GroupStatistics overallStatistics =
            new GroupStatistics();

    private final List<ExecutionTestResult> executionTests =
            new CopyOnWriteArrayList<>();

    private static final AtomicBoolean executionFinished =
            new AtomicBoolean(false);

    private final double approvalThreshold =
            Double.parseDouble(
                    System.getProperty("approval.threshold", "95")
            );

    private ZonedDateTime executionStartedAt;
    private ZonedDateTime executionFinishedAt;

    @Override
    public void onTestStart(ITestResult result) {
        try {
            ExtentTest extentTest =
                    extent.createTest(
                            result.getMethod().getMethodName(),
                            result.getTestClass().getName()
                    );

            // Obtener grupos de TestNG
            String[] groups =
                    result.getMethod().getGroups();

            // Agregar grupos a Extent
            for (String group : groups) {
                extentTest.assignCategory(group);
            }

            test.set(extentTest);
        } catch (Exception e) {
            System.out.println(
                    "No fue posible inicializar el reporte del test: "
                            + e.getMessage()
            );
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        registrarResultadoTest(result, "PASSED", null);
        reportHelper.safePass(getTest(), "Resultado: PASSED");

        registerOverallResult("PASSED");
        registerResult(result, "PASSED");
        ContextManager.removeContext();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {

            // Reporte normal del error
            reportHelper.safeFail(getTest(), result.getThrowable());

            adjuntarCapturaEnFallo(result);

            // Registrar resultado
            registerOverallResult("FAILED");
            registerResult(result, "FAILED");

            // Construir contexto para IA
            FailureContext failure =
                    failureContextBuilder.build(result);

            registrarResultadoTest(result, "FAILED", failure);

            reportHelper.safeInfo(
                    getTest(),
                    construirBloqueDiagnostico(failure)
            );

            // Llamada a IA, protegida para que nunca rompa el listener
            try {
                String analisisIA = ai.AIAnalyzerService.analizarError(failure);

                reportHelper.safeInfo(
                        getTest(),
                        "<b>\uD83E\uDD16 ANÁLISIS IA</b><br><pre>"
                                + escaparHtml(analisisIA)
                                + "</pre>"
                );
            } catch (Exception e) {
                reportHelper.safeWarning(
                        getTest(),
                        "No fue posible ejecutar el análisis IA: "
                                + e.getMessage()
                );
            }
        } finally {
            ContextManager.removeContext();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        registrarResultadoTest(result, "SKIPPED", null);
        reportHelper.safeSkip(getTest(), "Prueba omitida");

        registerOverallResult("SKIPPED");
        registerResult(result, "SKIPPED");
        ContextManager.removeContext();
    }

    @Override
    public void onFinish(ITestContext context) {
        ContextManager.removeContext();
    }

    @Override
    public void onExecutionStart() {
        executionStartedAt = ZonedDateTime.now();
    }

    @Override
    public void onExecutionFinish() {
        if (!executionFinished.compareAndSet(false, true)) {
            return;
        }

        executionFinishedAt = ZonedDateTime.now();
        String veredictoFinal = obtenerVeredictoFinal();

        try {
            generarResumenFinal();
            aplicarResumenAExtent();
            registrarVeredictoFinal();
        } catch (Exception e) {
            System.out.println(
                    "No fue posible preparar el reporte Extent: "
                            + e.getMessage()
            );
        }

        try {
            extent.flush();
        } catch (Exception e) {
            System.out.println(
                    "No fue posible escribir el reporte Extent a disco: "
                            + e.getMessage()
            );
        }

        supabaseReportService.enviarSiConfigurado(
                ExtentManager.getReportFilePath(),
                executionStartedAt,
                executionFinishedAt,
                overallStatistics,
                veredictoFinal,
                executionTests
        );

        generateGroupReport();
        generateOverallReport();

        if (!"APROBADO".equals(veredictoFinal)) {
            throw new IllegalStateException(
                    "La ejecucion no alcanzo el umbral de aprobacion configurado: "
                            + formatearPorcentaje(approvalThreshold)
                            + ". Veredicto: "
                            + veredictoFinal
            );
        }
    }

    public static void setTest(ExtentTest extentTest) {
        test.set(extentTest);
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void step(String message) {
        ExtentTest currentTest = getTest();
        if (currentTest != null) {
            currentTest.info(message);
        } else {
            System.out.println(message);
        }
        ContextManager.getContext()
                .setUltimoPaso(message);
    }

    public static void step(String message, By locator) {
        ExtentTest currentTest = getTest();
        if (currentTest != null) {
            currentTest.info(message);
        } else {
            System.out.println(message);
        }
        ContextManager.getContext()
                .setUltimoPaso(message);
        ContextManager.getContext()
                .setUltimoLocator(locator.toString());
    }


    // ==========================================
    // REGISTRO DE RESULTADOS POR GRUPO
    // ==========================================

    private void registerResult(
            ITestResult result,
            String status) {

        String[] groups =
                result.getMethod().getGroups();

        for (String group : groups) {

            GroupStatistics statistics =
                    groupStatistics.computeIfAbsent(
                            group,
                            key -> new GroupStatistics()
                    );

            statistics.incrementTotal();

            switch (status) {

                case "PASSED":
                    statistics.incrementPassed();
                    break;

                case "FAILED":
                    statistics.incrementFailed();
                    break;

                case "SKIPPED":
                    statistics.incrementSkipped();
                    break;
            }
        }
    }

    // ==========================================
    // GENERAR REPORTE POR GRUPO
    // ==========================================

    private void generateGroupReport() {

        groupStatistics.forEach((group, statistics) -> {

            System.out.println(
                    group +
                            " | Total: " + statistics.getTotal() +
                            " | Passed: " + statistics.getPassed() +
                            " | Failed: " + statistics.getFailed() +
                            " | Skipped: " + statistics.getSkipped() +
                            " | Approval: " + formatearPorcentaje(statistics.getApprovalPercentage())
            );
        });
    }

    private void generateOverallReport() {
        System.out.println(
                "OVERALL" +
                        " | Total: " + overallStatistics.getTotal() +
                        " | Passed: " + overallStatistics.getPassed() +
                        " | Failed: " + overallStatistics.getFailed() +
                        " | Skipped: " + overallStatistics.getSkipped() +
                        " | Approval: " + formatearPorcentaje(overallStatistics.getApprovalPercentage()) +
                        " | Verdict: " + obtenerVeredictoFinal()
        );
    }

    private void generarResumenFinal() {
        String resumenMarkdown = construirResumenMarkdown();
        escribirResumenEnArchivo(resumenMarkdown);
    }

    private void registrarResumenEnExtent(String resumenHtml, String resumenMarkdown) {
        try {
            ExtentTest resumen = extent.createTest("Resumen final");
            resumen.assignCategory("summary");
            resumen.info(resumenHtml);
            resumen.info("<details><summary>Versión Markdown</summary><pre>"
                    + escaparHtml(resumenMarkdown)
                    + "</pre></details>");
        } catch (Exception e) {
            System.out.println(
                    "No fue posible registrar el resumen en Extent: "
                            + e.getMessage()
            );
        }
    }

    private void aplicarResumenAExtent() {
        extent.setSystemInfo("Resumen Final", " ");
        extent.setSystemInfo(
                "Resumen Final - Overall Approval",
                formatearPorcentaje(overallStatistics.getApprovalPercentage())
        );
        extent.setSystemInfo(
                "Resumen Final - Overall Passed",
                String.valueOf(overallStatistics.getPassed())
        );
        extent.setSystemInfo(
                "Resumen Final - Overall Failed",
                String.valueOf(overallStatistics.getFailed())
        );
        extent.setSystemInfo(
                "Resumen Final - Overall Skipped",
                String.valueOf(overallStatistics.getSkipped())
        );
        extent.setSystemInfo(
                "Resumen Final - Approval Threshold",
                formatearPorcentaje(approvalThreshold)
        );
        extent.setSystemInfo(
                "Resumen Final - Final Verdict",
                obtenerVeredictoFinal()
        );

        groupStatistics.forEach((group, statistics) -> {
            extent.setSystemInfo(
                    "Resumen Final - Approval " + group,
                    formatearPorcentaje(statistics.getApprovalPercentage())
            );
        });
    }

    private void escribirResumenEnArchivo(String resumenMarkdown) {
        try {
            Path carpeta = Paths.get(
                    "reports",
                    "summary",
                    System.getProperty("report.scope", "business")
            );
            Files.createDirectories(carpeta);

            String timestamp = ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Path archivo = carpeta.resolve("TestSummary" + timestamp + ".md");
            Files.writeString(archivo, resumenMarkdown);

            System.out.println(
                    "Resumen final guardado en: "
                            + archivo.toAbsolutePath()
            );
        } catch (IOException e) {
            System.out.println(
                    "No fue posible guardar el resumen final: "
                            + e.getMessage()
            );
        }
    }

    private String construirResumenMarkdown() {
        StringBuilder markdown = new StringBuilder();

        markdown.append("# Resumen Final\n\n");
        markdown.append("## Global\n");
        markdown.append("- Total: ").append(overallStatistics.getTotal()).append("\n");
        markdown.append("- Passed: ").append(overallStatistics.getPassed()).append("\n");
        markdown.append("- Failed: ").append(overallStatistics.getFailed()).append("\n");
        markdown.append("- Skipped: ").append(overallStatistics.getSkipped()).append("\n");
        markdown.append("- Approval: ").append(formatearPorcentaje(overallStatistics.getApprovalPercentage())).append("\n\n");

        markdown.append("## Por Grupo\n");
        if (groupStatistics.isEmpty()) {
            markdown.append("- Sin datos.\n");
        } else {
            List<Map.Entry<String, GroupStatistics>> entries = new ArrayList<>(groupStatistics.entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getKey));

            for (Map.Entry<String, GroupStatistics> entry : entries) {
                GroupStatistics statistics = entry.getValue();
                markdown.append("- ").append(entry.getKey()).append("\n");
                markdown.append("  - Total: ").append(statistics.getTotal()).append("\n");
                markdown.append("  - Passed: ").append(statistics.getPassed()).append("\n");
                markdown.append("  - Failed: ").append(statistics.getFailed()).append("\n");
                markdown.append("  - Skipped: ").append(statistics.getSkipped()).append("\n");
                markdown.append("  - Approval: ").append(formatearPorcentaje(statistics.getApprovalPercentage())).append("\n");
            }
        }

        markdown.append("\n## Criterio de Aprobacion\n");
        markdown.append("- Passed/(Passed+Failed) * 100\n");
        markdown.append("- Skipped no se incluye en el porcentaje de aprobacion\n");
        markdown.append("- Threshold: ").append(formatearPorcentaje(approvalThreshold)).append("\n");
        markdown.append("- Verdict: ").append(obtenerVeredictoFinal()).append("\n");

        return markdown.toString();
    }

    private String construirResumenHtml() {
        StringBuilder html = new StringBuilder();

        html.append("<h2>Resumen de ejecución</h2>");
        html.append("<table style='border-collapse:collapse;width:100%;'>");
        html.append("<tr><th style='text-align:left;padding:6px;'>Métrica</th>");
        html.append("<th style='text-align:left;padding:6px;'>Valor</th></tr>");
        agregarFilaHtml(html, "Total", String.valueOf(overallStatistics.getTotal()));
        agregarFilaHtml(html, "Passed", String.valueOf(overallStatistics.getPassed()));
        agregarFilaHtml(html, "Failed", String.valueOf(overallStatistics.getFailed()));
        agregarFilaHtml(html, "Skipped", String.valueOf(overallStatistics.getSkipped()));
        agregarFilaHtml(html, "Approval", formatearPorcentaje(overallStatistics.getApprovalPercentage()));
        agregarFilaHtml(html, "Threshold", formatearPorcentaje(approvalThreshold));
        agregarFilaHtml(html, "Verdict", obtenerVeredictoFinal());
        html.append("</table>");

        html.append("<h3>Por Grupo</h3>");
        if (groupStatistics.isEmpty()) {
            html.append("<p>Sin datos.</p>");
        } else {
            List<Map.Entry<String, GroupStatistics>> entries = new ArrayList<>(groupStatistics.entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getKey));

            html.append("<ul>");
            for (Map.Entry<String, GroupStatistics> entry : entries) {
                GroupStatistics statistics = entry.getValue();
                html.append("<li><b>")
                        .append(escaparHtml(entry.getKey()))
                        .append("</b>: ")
                        .append("Total ")
                        .append(statistics.getTotal())
                        .append(", Passed ")
                        .append(statistics.getPassed())
                        .append(", Failed ")
                        .append(statistics.getFailed())
                        .append(", Skipped ")
                        .append(statistics.getSkipped())
                        .append(", Approval ")
                        .append(formatearPorcentaje(statistics.getApprovalPercentage()))
                        .append("</li>");
            }
            html.append("</ul>");
        }

        return html.toString();
    }

    private void agregarFilaHtml(StringBuilder html, String etiqueta, String valor) {
        html.append("<tr>")
                .append("<td style='padding:6px;border-top:1px solid #ddd;'>")
                .append(escaparHtml(etiqueta))
                .append("</td>")
                .append("<td style='padding:6px;border-top:1px solid #ddd;'>")
                .append(escaparHtml(valor))
                .append("</td>")
                .append("</tr>");
    }

    private void registerOverallResult(String status) {
        overallStatistics.incrementTotal();

        switch (status) {
            case "PASSED":
                overallStatistics.incrementPassed();
                break;
            case "FAILED":
                overallStatistics.incrementFailed();
                break;
            case "SKIPPED":
                overallStatistics.incrementSkipped();
                break;
        }
    }

    private void registrarVeredictoFinal() {
        String veredicto = obtenerVeredictoFinal();
        System.out.println("FINAL VERDICT | " + veredicto);
    }

    private void registrarResultadoTest(
            ITestResult result,
            String status,
            FailureContext failure) {

        executionTests.add(
                ExecutionTestResult.from(result, status, failure)
        );
    }

    private String obtenerVeredictoFinal() {
        if (overallStatistics.getExecuted() == 0) {
            return "NO APROBADO";
        }

        return overallStatistics.isApproved(approvalThreshold)
                ? "APROBADO"
                : "NO APROBADO";
    }

    private String formatearPorcentaje(double porcentaje) {
        return String.format(Locale.US, "%.2f%%", porcentaje);
    }

    private String obtenerStackTraceLimitado(
            Throwable throwable,
            int limite) {

        StackTraceElement[] stackTrace =
                throwable.getStackTrace();

        StringBuilder resultado =
                new StringBuilder();

        int cantidad =
                Math.min(stackTrace.length, limite);

        for (int i = 0; i < cantidad; i++) {

            resultado
                    .append(stackTrace[i])
                    .append("\n");
        }

        return resultado.toString();
    }

    private String limitarTexto(
            String texto,
            int maximo) {

        if (texto == null) {
            return "";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo)
                + "...";
    }

    private String construirBloqueDiagnostico(FailureContext failure) {
        StringBuilder html = new StringBuilder();

        html.append("<b>DIAGNOSTICO DEL FALLO</b><br>");
        html.append("<ul>");
        agregarItem(html, "Test", failure.getTestName());
        agregarItem(html, "Clase", failure.getClassName());
        agregarItem(html, "Browser", failure.getBrowser());
        agregarItem(html, "URL", failure.getUrl());
        agregarItem(html, "Ultimo paso", failure.getUltimoPaso());
        agregarItem(html, "Locator", failure.getLocator());
        agregarItem(html, "Error", failure.getError());
        html.append("</ul>");
        html.append("<b>Stack trace corto</b><br><pre>");
        html.append(escaparHtml(limitarTexto(failure.getStackTrace(), 1500)));
        html.append("</pre>");

        return html.toString();
    }

    private void agregarItem(StringBuilder html, String etiqueta, String valor) {
        html.append("<li><b>")
                .append(escaparHtml(etiqueta))
                .append(":</b> ")
                .append(escaparHtml(valorSeguro(valor)))
                .append("</li>");
    }

    private void adjuntarCapturaEnFallo(ITestResult result) {
        try {
            if (getTest() == null) {
                return;
            }

            if (!(driverFactory.getCurrentDriver() instanceof TakesScreenshot)) {
                reportHelper.safeWarning(
                        getTest(),
                        "No fue posible adjuntar captura: el driver no soporta screenshots"
                );
                return;
            }

            byte[] screenshot =
                    ((TakesScreenshot) driverFactory.getCurrentDriver())
                            .getScreenshotAs(OutputType.BYTES);

            String base64 =
                    java.util.Base64.getEncoder().encodeToString(screenshot);

            getTest().fail(
                    "Captura de pantalla al fallar: " + result.getMethod().getMethodName(),
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build()
            );
        } catch (Exception e) {
            reportHelper.safeWarning(
                    getTest(),
                    "No fue posible adjuntar captura de fallo: " + e.getMessage()
            );
        }
    }

    private String valorSeguro(String valor) {
        if (valor == null || valor.isBlank()) {
            return "N/A";
        }

        return valor;
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
