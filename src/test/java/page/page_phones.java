package page;

import locators.phones_locators;
import org.openqa.selenium.WebDriver;

public class page_phones extends page_product_base
        implements phones_locators {

    public page_phones(WebDriver driver) {
        super(driver);
    }

    public void validateTitlePhones() {
        validateTitle("Phones & PDAs", title_phones);
    }

    public void seleccionarCelular(String textElement, boolean recoverPrice) {
        seleccionarProductoConValidacion(
                "Seleccionar celular: " + textElement,
                textElement,
                locators.generic_locators.elementAByText(textElement),
                locators.generic_locators.elementTitleByText(textElement)
        );

        if (recoverPrice) {
            ejecutarPaso(
                    "Recuperar precio del celular: " + textElement,
                    () -> guardarPrecioEnContexto(
                            locators.generic_locators.returnPriceToTitleText(textElement)
                    )
            );
        }
    }

    public void agregarCarrito() {
        agregarProductoAlCarrito(
                locators.generic_locators.elementButtonById("button-cart"),
                div_added_cart
        );
    }
}
