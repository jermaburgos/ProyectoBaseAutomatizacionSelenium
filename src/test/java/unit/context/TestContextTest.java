package unit.context;

import context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestContextTest {

    @Test
    public void setProducto_y_getProducto_debenCoincidir() {
        TestContext context = new TestContext();

        context.setProducto("Apple Cinema 30");

        Assert.assertEquals(context.getProducto(), "Apple Cinema 30");
    }

    @Test
    public void setPrecio_debeAgregarValoresALaLista() {
        TestContext context = new TestContext();

        context.setPrecio("$10.00");
        context.setPrecio("$20.00");

        Assert.assertEquals(context.getPrecios().size(), 2);
        Assert.assertEquals(context.getPrecios().get(0), "$10.00");
        Assert.assertEquals(context.getPrecios().get(1), "$20.00");
    }

    @Test
    public void ultimoPaso_y_ultimoLocator_debenPersistir() {
        TestContext context = new TestContext();

        context.setUltimoPaso("Agregar producto");
        context.setUltimoLocator("//button[@id='button-cart']");

        Assert.assertEquals(context.getUltimoPaso(), "Agregar producto");
        Assert.assertEquals(context.getUltimoLocator(), "//button[@id='button-cart']");
    }
}
