package test;

import base.baseInicializacion;
import org.testng.annotations.BeforeMethod;
import page.page_Home;
import page.page_monitors;
import page.page_components;
import page.page_cart;
import page.page_cameras;
import page.page_generic;
import page.page_phones;
import report.ExtentManager;

public class baseTest extends baseInicializacion {
    protected page_Home home;
    protected page_phones phones;
    protected page_components components;
    protected page_generic generic;
    protected ExtentManager report;
    protected page_cart cart;
    protected page_cameras cameras;
    protected page_monitors monitors;


    @BeforeMethod(alwaysRun = true)
    public void inicializarPages() {

        home = new page_Home(driver);
        generic = new page_generic(driver);
        phones = new page_phones(driver);
        components = new page_components(driver);
        cart = new page_cart(driver);
        cameras = new page_cameras(driver);
        monitors = new page_monitors(driver);
        report = new ExtentManager();
    }
}
