package page;

import context.ContextManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public abstract class page_product_base extends page_generic {

    protected page_product_base(WebDriver driver) {
        super(driver);
    }

    protected void seleccionarProductoConValidacion(
            String descripcionPaso,
            String textoProducto,
            By locatorProducto,
            By locatorTitulo
    ) {
        ejecutarPaso(descripcionPaso, () -> clicElement(locatorProducto));

        ejecutarPaso(
                "Validar que se haya seleccionado el producto: " + textoProducto,
                () -> validateTitle(textoProducto, locatorTitulo)
        );
    }

    protected void guardarPrecioEnContexto(By locatorPrecio) {
        String precio = getTextElement(locatorPrecio);
        ContextManager.getContext().setPrecio(precio);
    }

    protected void agregarProductoAlCarrito(By locatorBoton, By locatorMensajeExito) {
        ejecutarPaso("Agregar producto al carrito", () -> clicElement(locatorBoton));

        String mensaje = getTextElement(locatorMensajeExito);
        Assert.assertTrue(
                mensaje.contains("Success: You have added"),
                "El mensaje de éxito no es el esperado"
        );
    }
}
