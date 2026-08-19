package page;

import com.aventstack.extentreports.MediaEntityBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import report.TestListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class page_generic {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private boolean tomarCapturas =
            Boolean.parseBoolean(
                    System.getProperty("screenshots", "false")
            );

    public page_generic(WebDriver driver){
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(10)
        );
    }

    public void validateTitle(String title, By locator) {

        TestListener.step("Validating the title of the page: " + title, locator);
        String actualTitle = esperarTextoVisible(locator);
        Assert.assertTrue(
                actualTitle.toLowerCase().trim().contains(title.toLowerCase().trim()),
                "El titulo de la pagina no es el esperado"
        );
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

    public void clicElement(By locator) {
        validarLocator(locator);
        TestListener.step("Clicking on element located by: " + locator.toString(),locator);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

    }

    public void hoverElement(By locator) {
        validarLocator(locator);
        TestListener.step("Hovering over element located by: " + locator.toString(), locator);
        WebElement element = esperarElementoVisible(locator);
        new Actions(driver).moveToElement(element).perform();
    }

    public void selectElement(By locator, String visibleText) {
        validarLocator(locator);
        TestListener.step(
                "Selecting option '" + visibleText + "' from element located by: " + locator.toString(),
                locator
        );
        WebElement element = esperarElementoVisible(locator);
        new Select(element).selectByVisibleText(visibleText);
    }

    public void selectElementContainingText(By locator, String visibleText) {
        validarLocator(locator);
        TestListener.step(
                "Selecting option containing '" + visibleText + "' from element located by: " + locator.toString(),
                locator
        );

        WebElement element = esperarElementoVisible(locator);
        Select select = new Select(element);
        String visibleTextNormalized = visibleText.trim().toLowerCase();

        for (WebElement option : select.getOptions()) {
            String optionText = option.getText().trim().toLowerCase();
            if (optionText.contains(visibleTextNormalized)) {
                option.click();
                return;
            }
        }

        throw new NoSuchElementException(
                "No se encontró una opción que contenga el texto: " + visibleText
        );
    }

    public void writeElement(By locator, String text) {
        validarLocator(locator);
        TestListener.step("Writing text '" + text + "' in element located by: " + locator.toString(), locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    public void uploadFile(By locator, String filePath) {
        cerrarVentanaArchivos();
        validarLocator(locator);

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        TestListener.step("Uploading file '" + path + "' in element located by: " + locator.toString(), locator);

        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        element.sendKeys(path.toString());
    }

    public void ingresarFechaActual(By locator) {
        String fecha = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        ejecutarPaso("Ingresar fecha actual: " + fecha, () ->
                writeElement(locator, fecha)
        );
    }

    public void ingresarHoraActual(By locator) {
        String hora = LocalTime.now()
                .withSecond(0)
                .withNano(0)
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        ejecutarPaso("Ingresar hora actual: " + hora, () ->
                writeElement(locator, hora)
        );
    }

    public void ingresarFechaHoraActual(By locator) {
        String fechaHora = LocalDateTime.now()
                .withSecond(0)
                .withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        ejecutarPaso("Ingresar fecha y hora actual: " + fechaHora, () ->
                writeElement(locator, fechaHora)
        );
    }

    public void aceptarAlertaSiExiste() {
        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
            TestListener.step("Alerta aceptada correctamente");
        } catch (TimeoutException e) {
            TestListener.step("No se detectó alerta para aceptar");
        }
    }

    public String getTextElement(By locator) {
        TestListener.step("Getting text from element located by: " + locator.toString(), locator);
        return esperarTextoVisible(locator);

    }

    public void iterateList(List<String> list, By listElement){
        TestListener.step("Search element list located by: " + listElement.toString(),listElement);
        esperarElementoVisible(listElement);
        List<WebElement> listElementFound = driver.findElements(listElement);
        List<String> listLowerCase = list.stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toList());
        int contador = 0;
        for (WebElement element : listElementFound){
            if(listLowerCase.contains(element.getText().trim().toLowerCase())){
                contador++;
            }
        }
        System.out.println("listado ingredado:"+listLowerCase.size()+" | listado capturado:"+listElementFound.size()+" | listado encontrado:"+contador);
        Assert.assertTrue((contador == listLowerCase.size() && contador == listElementFound.size()), "No se encontraron todos los elementos de la lista");

    }

    protected void ejecutarPaso(String nombre, Runnable accion) {

        tomarCaptura(nombre + "_ANTES");

        try {

            accion.run();

            tomarCaptura(nombre + "_DESPUES");

            TestListener.step(
                    "<div style='font-weight:600; font-size:14px; color:#029e25'>"+"\u2713 Paso ejecutado correctamente: " + nombre +"</div>"
            );

        } catch (Exception e) {

            tomarCaptura(nombre + "_ERROR");

            if (TestListener.getTest() != null) {
                TestListener.getTest()
                        .fail("<div style='font-weight:600; font-size:14px; color:#ad0909'>\u2717 Error en paso: " + nombre + "</div>");
            } else {
                System.out.println("Error en paso: " + nombre);
            }

            throw e;
        }
    }

    public void tomarCaptura(String nombre) {

        if(!tomarCapturas) {
            return;
        }
        try {

            TakesScreenshot screenshot =
                    (TakesScreenshot) driver;

            // Captura directamente en memoria
            byte[] captura =
                    screenshot.getScreenshotAs(OutputType.BYTES);

            // Convertir a Base64
            String base64 =
                    Base64.getEncoder().encodeToString(captura);

            // Agregar al reporte
            TestListener.getTest()
                    .info(
                            nombre,
                            MediaEntityBuilder
                                    .createScreenCaptureFromBase64String(base64)
                                    .build()
                    );

        } catch (Exception e) {

            if (TestListener.getTest() != null) {
                TestListener.getTest()
                        .warning(
                                "No se pudo tomar captura: "
                                        + e.getMessage()
                        );
            } else {
                System.out.println(
                        "No se pudo tomar captura: "
                                + e.getMessage()
                );
            }
        }
    }

    private WebElement esperarElementoVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private String esperarTextoVisible(By locator) {
        return esperarElementoVisible(locator).getText();
    }

    private void validarLocator(By locator) {

        if (locator == null) {
            throw new IllegalArgumentException(
                    "El locator no puede ser null"
            );
        }
    }
    public void cerrarVentanaArchivos() {
        try {
            Robot robot = new Robot();

            robot.delay(500);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);

        } catch (AWTException e) {
            throw new RuntimeException(
                    "No se pudo cerrar el selector de archivos",
                    e
            );
        }
    }
}
