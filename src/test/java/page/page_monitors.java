package page;

import locators.apple_cinema_locators;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class page_monitors extends page_generic implements apple_cinema_locators {

    public page_monitors(WebDriver driver) {
        super(driver);
    }

    public void validarTituloAppleCinema() {
        validateTitle("Apple Cinema 30", title_apple_cinema);
    }

    public void seleccionarRadio(String optionText) {
        seleccionarOpcion("Seleccionar radio: " + optionText,
                apple_cinema_locators.radioOptionByText(optionText));
    }

    public void seleccionarCheckbox(String optionText) {
        seleccionarOpcion("Seleccionar checkbox: " + optionText,
                apple_cinema_locators.checkboxOptionByText(optionText));
    }

    public void ingresarTexto(String value) {
        escribirEnCampo("Ingresar texto: " + value,
                apple_cinema_locators.textOption(),
                value);
    }

    public void seleccionarOpcionSelect(String visibleText) {
        ejecutarPaso("Seleccionar opción del select: " + visibleText, () ->
                selectElementContainingText(apple_cinema_locators.selectOption(), visibleText)
        );
    }

    public void ingresarTextarea(String value) {
        escribirEnCampo("Ingresar texto extenso en textarea",
                apple_cinema_locators.textareaOption(),
                value);
    }

    public void subirArchivo(String filePath) {
        ejecutarPaso("Subir archivo txt", () -> {
            clicElement(apple_cinema_locators.fileButtonOption());
            uploadFile(apple_cinema_locators.fileInputOption(), filePath);
            aceptarAlertaSiExiste();
        });
    }

    public void ingresarFechaActual() {
        ingresarFechaActual(apple_cinema_locators.dateOption());
    }

    public void ingresarHoraActual() {
        ingresarHoraActual(apple_cinema_locators.timeOption());
    }

    public void ingresarFechaHoraActual() {
        ingresarFechaHoraActual(apple_cinema_locators.dateTimeOption());
    }

    public void agregarAlCarrito() {
        ejecutarPaso("Agregar producto al carrito", () ->
                clicElement(apple_cinema_locators.button_add_to_cart)
        );
    }

    public void validarErrorRadioRequerido() {
        String mensaje = getTextElement(apple_cinema_locators.radioError()).trim();

        Assert.assertFalse(
                mensaje.isBlank(),
                "No se mostró el mensaje de error en la opción Radio"
        );

        Assert.assertTrue(
                mensaje.toLowerCase().contains("required") || mensaje.toLowerCase().contains("radio"),
                "El mensaje de error no corresponde a la validación del Radio. Mensaje actual: " + mensaje
        );
    }

    private void seleccionarOpcion(String descripcion, org.openqa.selenium.By locator) {
        ejecutarPaso(descripcion, () -> clicElement(locator));
    }

    private void escribirEnCampo(String descripcion, org.openqa.selenium.By locator, String valor) {
        ejecutarPaso(descripcion, () -> writeElement(locator, valor));
    }
}
