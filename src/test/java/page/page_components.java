package page;

import context.ContextManager;
import locators.components_locators;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class page_components extends page_generic implements components_locators {

    public page_components(WebDriver driver) {
        super(driver);
    }

    public void abrirMonitorsDesdeMenu() {
        ejecutarPaso("Desplegar Components", () -> hoverElement(menu_components));
        ejecutarPaso("Ingresar a Monitors", () -> clicElement(menu_monitors));
        validateTitle("Monitors", title_monitors);
    }

    public void seleccionarProducto(String textElement, boolean recoverPrice) {
        ejecutarPaso("Seleccionar producto: " + textElement, () -> {
            clicElement(components_locators.elementAByText(textElement));
        });

        if (recoverPrice) {
            String price = getTextElement(components_locators.returnPriceComponentToTitleText(textElement));
            ContextManager.getContext().setPrecio(price);
        }

        ejecutarPaso("Validar que se haya seleccionado el producto: " + textElement, () -> {
            validateTitle(textElement, components_locators.elementTitleByText(textElement));
        });
    }

    public void agregarAlCarrito() {
        ejecutarPaso("Agregar producto al carrito", () -> clicElement(components_locators.elementButtonById("button-cart")));

        String mensaje = getTextElement(alert_success);
        Assert.assertTrue(
                mensaje.contains("Success: You have added"),
                "El mensaje de éxito no es el esperado"
        );
    }
}
