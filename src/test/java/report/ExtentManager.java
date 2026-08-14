package report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance(){
        if (extent == null){

            ZonedDateTime ahora = ZonedDateTime.now();

            // 2. Definir el formato requerido
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            // 3. Formatear la fecha
            String fechaFormateada = ahora.format(formato);
            ExtentSparkReporter spark = new    ExtentSparkReporter("reports/AutomationReport"+ fechaFormateada+".html");
            extent = new ExtentReports();
            extent.attachReporter(spark);

            extent.setSystemInfo("Proyecto","Automation Testing");
            extent.setSystemInfo("Framework","Selenium + TestNG + ExtentReports");
            extent.setSystemInfo("Test Framework","TestNG");
            extent.setSystemInfo("Ambiente","QA");
        }
        return extent;
    }


}
