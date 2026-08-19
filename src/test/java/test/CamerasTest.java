package test;

import jdk.jfr.Description;
import org.testng.annotations.Test;

public class CamerasTest extends baseTest {

    @Test(groups = {"smoke", "regression"})
    @Description("Flujo de compra de productos desde Cameras sin validar precios ni total")
    public void agregarProductosDesdeCameras() {

        generic.navigateTo("https://opencart.abstracta.us/", "your store");

        home.clickCameras();
        cameras.validateTitleCameras();
        cameras.seleccionarProducto("Canon EOS 5D", "Red");
        cameras.agregarCarrito();

        home.clickCameras();
        cameras.validateTitleCameras();
        cameras.seleccionarProducto("Nikon D300");
        cameras.agregarCarrito();

        home.ingresarCarrito();
        cart.validarProductoAgregado("Canon EOS 5D;Nikon D300", false);
    }
}
