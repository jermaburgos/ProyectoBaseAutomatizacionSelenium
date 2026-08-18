package report;

import com.aventstack.extentreports.ExtentTest;
import context.ContextManager;
import org.openqa.selenium.By;
import org.testng.ITestContext;
import org.testng.ITestListener;
import com.aventstack.extentreports.ExtentReports;
import org.testng.ITestResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestListener implements ITestListener {
    private static ExtentReports extent =
            ExtentManager.getInstance();

    private static ThreadLocal<ExtentTest> test =
            new ThreadLocal<>();

    private final ListenerReportHelper reportHelper =
            new ListenerReportHelper();

    private final FailureContextBuilder failureContextBuilder =
            new FailureContextBuilder();

    private final Map<String, GroupStatistics> groupStatistics =
            new ConcurrentHashMap<>();

    @Override
    public void onTestStart(ITestResult result) {
        try {
            ExtentTest extentTest =
                    extent.createTest(
                            result.getMethod().getMethodName()
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
        reportHelper.safePass(getTest(), "Prueba ejecutada correctamente");

        registerResult(result, "PASSED");
        ContextManager.removeContext();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {

            // Reporte normal del error
            reportHelper.safeFail(getTest(), result.getThrowable());

            // Registrar resultado
            registerResult(result, "FAILED");

            // Construir contexto para IA
            ai.FailureContext failure =
                    failureContextBuilder.build(result);

            // Llamada a IA, protegida para que nunca rompa el listener
            try {
                String analisisIA = ai.AIAnalyzerService.analizarError(failure);

                reportHelper.safeInfo(
                        getTest(),
                        "<b>🤖 ANÁLISIS IA</b><br><pre>"
                                + analisisIA
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
        reportHelper.safeSkip(getTest(), "Prueba omitida");

        registerResult(result, "SKIPPED");
        ContextManager.removeContext();
    }

    @Override
    public void onFinish(ITestContext context) {
        try {
            extent.flush();
        } catch (Exception e) {
            System.out.println(
                    "No fue posible cerrar el reporte Extent: "
                            + e.getMessage()
            );
        }

        generateGroupReport();
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
                            " | Skipped: " + statistics.getSkipped()
            );
        });
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

}
