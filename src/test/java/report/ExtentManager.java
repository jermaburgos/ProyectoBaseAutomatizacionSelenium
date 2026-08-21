package report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentManager {
    private static ExtentReports extent;
    private static Path reportFilePath;

    public static ExtentReports getInstance() {
        if (extent == null) {
            extent = new ExtentReports();

            try {
                Path reportsDir = crearDirectorioReportes();
                ExtentSparkReporter spark = crearReporter(reportsDir);

                extent.attachReporter(spark);
                configurarSistema();
            } catch (IOException e) {
                System.out.println(
                        "No fue posible inicializar ExtentReports con archivo HTML: "
                                + e.getMessage()
                );
            } catch (Exception e) {
                System.out.println(
                        "No fue posible configurar ExtentReports: "
                                + e.getMessage()
                );
            }
        }
        return extent;
    }

    private static Path crearDirectorioReportes() throws IOException {
        Path reportsDir = Paths.get(
                "reports",
                System.getProperty("report.scope", "business")
        );
        Files.createDirectories(reportsDir);
        return reportsDir;
    }

    private static ExtentSparkReporter crearReporter(Path reportsDir) {
        String fechaFormateada =
                formatearFechaActual();

        Path rutaReporte =
                reportsDir.resolve(
                        "AutomationReport"
                                + fechaFormateada
                                + ".html"
                );

        reportFilePath = rutaReporte;

        ExtentSparkReporter spark =
                new ExtentSparkReporter(
                        rutaReporte.toString()
                );

        configurarReporter(spark);

        return spark;
    }

    private static void configurarReporter(
            ExtentSparkReporter spark) {

        spark.config().setDocumentTitle(
                "Reporte de Automatizacion"
        );

        spark.config().setReportName(
                "Ejecucion Selenium + TestNG - "
                        + System.getProperty("report.scope", "business")
        );

        spark.config().setTheme(
                Theme.STANDARD
        );

        spark.config().setEncoding(
                "UTF-8"
        );

        spark.config().setTimeStampFormat(
                "dd-MM-yyyy HH:mm:ss"
        );

        spark.viewConfigurer()
                .viewOrder()
                .as(new ViewName[]{
                        ViewName.DASHBOARD,
                        ViewName.TEST,
                        ViewName.CATEGORY,
                        ViewName.EXCEPTION,
                        ViewName.LOG
                })
                .apply();
    }

    private static void configurarSistema() {
        extent.setSystemInfo("Proyecto", "Automation Testing");
        extent.setSystemInfo("Framework", "Selenium + TestNG + ExtentReports");
        extent.setSystemInfo("Test Framework", "TestNG");
        extent.setSystemInfo("Ambiente", "QA");
    }

    private static String formatearFechaActual() {
        ZonedDateTime ahora = ZonedDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return ahora.format(formato);
    }

    public static Path getReportFilePath() {
        return reportFilePath;
    }
}
