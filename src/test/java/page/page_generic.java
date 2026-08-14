package page;

import base.baseInicializacion;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import report.TestListener;

import java.time.Duration;

public class page_generic {

    private WebDriver driver;
    private WebDriverWait wait;

    public page_generic(WebDriver driver){
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(10)
        );
    }

    public void validateTitle(String title, By locator) {

        TestListener.step("Validating the title of the page: " + title);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        String actualTitle = driver.findElement(locator).getText();
        Assert.assertEquals(actualTitle.toLowerCase().trim(), title.toLowerCase().trim(), "El titulo de la pagina no es el esperado");
        TestListener.getTest().pass("Title validation passed. Expected: " + title + ", Actual: " + actualTitle);
    }

    //metodos basicos

    /**
     * Navega a la URL especificada y verifica que el título de la página coincida con el título esperado.
     * @param url   La URL a la que se desea navegar.
     * @param title El título esperado de la página.
     */
    public void navigateTo(String url, String title) {
        TestListener.step("Navigating to URL: " + url + " and validating title: " + title);
        driver.get(url);
        Assert.assertEquals(driver.getTitle().toLowerCase().trim(),title.toLowerCase().trim(),"El titulo de la pagina no es el esperado");
    }

    public void clicElemtent(By locator) {
        TestListener.step("Clicking on element located by: " + locator.toString());
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        driver.findElement(locator).click();
    }
}
