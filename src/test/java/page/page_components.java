package page;

import locators.components_locators;
import org.openqa.selenium.WebDriver;

public class page_components extends page_product_base implements components_locators {

    public page_components(WebDriver driver) {
        super(driver);
    }

    public void abrirMonitorsDesdeMenu() {
        ejecutarPaso("Desplegar Components", () -> hoverElement(menu_components));
        ejecutarPaso("Ingresar a Monitors", () -> clicElement(menu_monitors));
        validateTitle("Monitors", title_monitors);
    }

    public void seleccionarProducto(String textElement, boolean recoverPrice) {
        seleccionarProductoConValidacion(
                "Seleccionar producto: " + textElement,
                textElement,
                locators.generic_locators.elementAByText(textElement),
                locators.generic_locators.elementTitleByText(textElement)
        );

        if (recoverPrice) {
            ejecutarPaso(
                    "Recuperar precio del componente: " + textElement,
                    () -> guardarPrecioEnContexto(
                            components_locators.returnPriceComponentToTitleText(textElement)
                    )
            );
        }
    }

    public void agregarAlCarrito() {
        agregarProductoAlCarrito(
                locators.generic_locators.elementButtonById("button-cart"),
                div_added_cart
        );
    }
}
