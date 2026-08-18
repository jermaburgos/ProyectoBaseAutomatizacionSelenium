package page;

import context.ContextManager;
import locators.cart_locators;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class page_cart extends page_generic
    implements cart_locators{
    public page_cart(WebDriver driver) {
        super(driver);
    }

    public void validarProductoAgregado(String listProductName, boolean validatePrice) {

        List<String> listProduct = Arrays.asList(listProductName.split(";"));

        ejecutarPaso("Validar que se haya agregado el producto al carrito", () -> {
            iterateList(listProduct,td_products);
        });

        if (validatePrice){
            ejecutarPaso("Validar que el precio del producto agregado al carrito sea el esperado", () -> {;
                iterateList(ContextManager.getContext().getPrecios(), td_price);
            });

            BigDecimal total = BigDecimal.ZERO;

            for (String price : ContextManager.getContext().getPrecios()) {
                String normalizedPrice = price.replace("$", "").trim();
                total = total.add(new BigDecimal(normalizedPrice));
            }

            String totalEsperado = "$" + total.setScale(2, RoundingMode.HALF_UP);
            String totalEncontrado = getTextElement(td_priceTotal).trim();

            Assert.assertEquals(totalEncontrado, totalEsperado, "El precio total no es el esperado");

        }

    };
}
