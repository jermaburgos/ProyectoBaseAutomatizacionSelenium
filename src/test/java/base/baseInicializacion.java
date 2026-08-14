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
    protected WebDriverWait wait = new WebDriverWait(
            driver,
            Duration.ofSeconds(10));



    @BeforeClass
    public void beforeSuite() {
        System.out.println("========== BEFORE CLASS ==========");
        String browser = System.getProperty("browser", "chrome");

        driver = driverFactory.getDriver(browser);
        System.out.println("BROWSER: " + browser);
        System.out.println("DRIVER: " + driver);
        driver.manage().window().maximize();

    }

    @AfterClass
    public void afterSuite() {
        if (driver != null) {
            driver.quit();
        }
    }


}
