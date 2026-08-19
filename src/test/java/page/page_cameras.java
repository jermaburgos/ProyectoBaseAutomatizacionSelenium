package page;

import locators.cameras_locators;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class page_cameras extends page_generic implements cameras_locators {

    public page_cameras(WebDriver driver) {
        super(driver);
    }

    public void validateTitleCameras() {
        validateTitle("Cameras", title_cameras);
    }

    public void seleccionarProducto(String textElement) {
        ejecutarPaso("Seleccionar producto: " + textElement, () -> {
            clicElement(cameras_locators.elementAByText(textElement));
        });

        ejecutarPaso("Validar que se haya seleccionado el producto: " + textElement, () -> {
            validateTitle(textElement, cameras_locators.elementTitleByText(textElement));
        });
    }

    public void seleccionarProducto(String textElement, String optionVisibleText) {
        seleccionarProducto(textElement);

        if (optionVisibleText != null && !optionVisibleText.isBlank()) {
            ejecutarPaso("Seleccionar opción: " + optionVisibleText, () -> {
                selectElement(select_product_option, optionVisibleText);
            });
        }
    }

    public void agregarCarrito() {
        ejecutarPaso("Agregar producto al carrito", () -> {
            clicElement(button_addToCart);
        });

        String mensaje = getTextElement(div_added_cart);
        Assert.assertTrue(
                mensaje.contains("Success: You have added"),
                "El mensaje de éxito no es el esperado"
        );
    }
}
