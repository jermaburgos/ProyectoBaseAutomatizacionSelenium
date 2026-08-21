package page;

import locators.cameras_locators;
import org.openqa.selenium.WebDriver;

public class page_cameras extends page_product_base implements cameras_locators {

    public page_cameras(WebDriver driver) {
        super(driver);
    }

    public void validateTitleCameras() {
        validateTitle("Cameras", title_cameras);
    }

    public void seleccionarProducto(String textElement) {
        seleccionarProductoConValidacion(
                "Seleccionar producto: " + textElement,
                textElement,
                locators.generic_locators.elementAByText(textElement),
                locators.generic_locators.elementTitleByText(textElement)
        );
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
        agregarProductoAlCarrito(button_addToCart, div_added_cart);
    }
}
