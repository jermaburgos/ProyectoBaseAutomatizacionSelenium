package base;
import com.aventstack.extentreports.ExtentTest;
import driver.driverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import report.ExtentManager;
import report.TestListener;

import java.lang.reflect.Method;
import java.time.Duration;


public class baseInicializacion {

    protected WebDriver driver;
    protected WebDriverWait wait;



    @BeforeMethod(alwaysRun = true)
    public void beforeSuite() {
        System.out.println("========== BEFORE METHOD ==========");
        String browser = System.getProperty("browser", "chrome");

        try {
            driver = driverFactory.getDriver(browser);
            wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(10));
            System.out.println("BROWSER: " + browser);
            System.out.println("DRIVER: " + driver);
            driver.manage().window().maximize();
        } catch (Exception e) {
            System.out.println(
                    "No fue posible inicializar el navegador: "
                            + e.getMessage()
            );
            driverFactory.quitDriver();
            throw e;
        }

    }

    @AfterMethod(alwaysRun = true)
    public void afterSuite(java.lang.reflect.Method method) {
        try {
            System.out.println(
                    "CERRANDO NAVEGADOR DEL TEST: "
                            + method.getName()
            );
            driverFactory.quitDriver();
            System.out.println(
                    "NAVEGADOR CERRADO DEL TEST: "
                            + method.getName()
            );
        } catch (Exception e) {
            System.out.println(
                    "No fue posible ejecutar el cierre del navegador: "
                            + e.getMessage()
            );
        }
    }


}
