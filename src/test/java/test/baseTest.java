package test;

import base.baseInicializacion;
import org.testng.annotations.BeforeClass;
import page.page_Home;
import page.page_generic;
import page.page_phones;
import report.ExtentManager;

public class baseTest extends baseInicializacion {
    protected page_Home home;
    protected page_phones phones;
    protected page_generic generic;
    protected ExtentManager report;

    @BeforeClass
    public void inicializarPages() {

        home = new page_Home(driver);
        generic = new page_generic(driver);
        phones = new page_phones(driver);
        report = new ExtentManager();
    }
}
