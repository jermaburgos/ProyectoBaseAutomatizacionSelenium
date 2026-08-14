package test;
import base.baseInicializacion;
import jdk.jfr.Description;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import page.page_Home;
import page.page_generic;

public class publicSite extends baseTest {


    @Test(groups = {"smoke","regression"})
    @Description("Test para agregar un producto al carrito de compras en el sitio web de OpenCart")
    public void addCart() throws InterruptedException {

        generic.navigateTo("https://opencart.abstracta.us/", "your store");
        home.clickPhones();
        phones.validateTitlePhones();
    }
}
