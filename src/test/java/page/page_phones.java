package page;

import context.ContextManager;
import locators.phones_locators;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

public class page_phones extends page_generic
        implements phones_locators {

    public page_phones(WebDriver driver) {
        super(driver);
    }

    public void validateTitlePhones() {
        validateTitle("Phones & PDAs", title_phones);
    }

    public void seleccionarCelular(String textElement, boolean recoverPrice) {
        ejecutarPaso("Seleccionar celular: " + textElement, () -> {;
            clicElement(phones_locators.elementAByText(textElement));
        });
        if(recoverPrice){
            String price = getTextElement(phones_locators.returnPricePhoneToTitleText(textElement));
            ContextManager.getContext().setPrecio(price);
            System.out.println("Precio recuperado: " + ContextManager.getContext().getPrecios().toString());
        }
        ejecutarPaso("Validar que se haya seleccionado el celular: " + textElement, () -> {;
            validateTitle(textElement, phones_locators.elementTitleByText(textElement));
        });

    }

    public void agregarCarrito() throws InterruptedException {
        ejecutarPaso("Presionar en botón agregar carrito", () -> {;
            clicElement(phones_locators.elementButtonById("button-cart"));
        });
        String textElement =getTextElement(div_added_cart);

        Assert.assertTrue(textElement.contains("Success: You have added"), "El mensaje de éxito no es el esperado");

    }
}
