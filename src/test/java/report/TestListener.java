package report;

import com.aventstack.extentreports.ExtentTest;
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

    private final Map<String, GroupStatistics> groupStatistics =
            new ConcurrentHashMap<>();

    @Override
    public void onTestStart(ITestResult result) {

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
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        test.get().pass(
                "Prueba ejecutada correctamente"
        );

        registerResult(result, "PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {

        test.get().fail(
                result.getThrowable()
        );

        registerResult(result, "FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        test.get().skip(
                "Prueba omitida"
        );

        registerResult(result, "SKIPPED");
    }

    @Override
    public void onFinish(ITestContext context) {

        extent.flush();

        generateGroupReport();
    }

    public static void setTest(ExtentTest extentTest) {
        test.set(extentTest);
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void step(String message) {
        getTest().info(message);
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

}
