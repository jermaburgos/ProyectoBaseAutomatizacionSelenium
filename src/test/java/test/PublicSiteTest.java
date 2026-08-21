package test;
import ai.CodeAnalyzerService;
import jdk.jfr.Description;
import org.testng.annotations.Test;
import page.page_generic;

public class PublicSiteTest extends baseTest {


    @Test(groups = {"smoke","regression"})
    @Description("Test para agregar un producto al carrito de compras en el sitio web de OpenCart")
    public void addCart() {

        generic.navigateTo("https://opencart.abstracta.us/", "your store");
        home.clickPhones();
        phones.validateTitlePhones();
        phones.seleccionarCelular("iPhone",false);
        phones.agregarCarrito();
        home.clickPhones();
        phones.validateTitlePhones();
        phones.seleccionarCelular("Palm Treo Pro",false);
        phones.agregarCarrito();
        home.ingresarCarrito();
        cart.validarProductoAgregado("iPhone;Palm Treo Pro", false);
    }

    @Test(groups = {"smoke","regression"})
    @Description("Test para comprar un producto desde Components > Monitors y validar el total")
    public void goToMonitors() {

        generic.navigateTo("https://opencart.abstracta.us/", "your store");
        components.abrirMonitorsDesdeMenu();
        components.seleccionarProducto("Samsung SyncMaster 941BW", true);
        components.agregarAlCarrito();
        home.ingresarCarrito();
        cart.validarProductoAgregado("Samsung SyncMaster 941BW", true);
    }

    @Test(priority = 1, groups = {"critical", "smoke", "regression"})
    @Description("Caso critico de regresion para agregar productos desde Components y Phones y validar el carrito")
    public void addComponentsAndPhonesProducts() {

        generic.navigateTo("https://opencart.abstracta.us/", "your store");

        components.abrirMonitorsDesdeMenu();
        components.seleccionarProducto("Samsung SyncMaster 941BW", false);
        components.agregarAlCarrito();

        home.clickPhones();
        phones.validateTitlePhones();
        phones.seleccionarCelular("HTC Touch HD", false);
        phones.agregarCarrito();

        home.clickPhones();
        phones.validateTitlePhones();
        phones.seleccionarCelular("Palm Treo Pro", false);
        phones.agregarCarrito();

        home.ingresarCarrito();
        cart.validarProductoAgregado(
                "Samsung SyncMaster 941BW;HTC Touch HD;Palm Treo Pro",
                false
        );
    }


    @Test(enabled = false)
    public void pruebaAnalisisCodigo() {

        String resultado =
                CodeAnalyzerService.analizarMetodo(
                        page_generic.class,
                        "clicElement"
                );

        System.out.println(resultado);
    }
}
