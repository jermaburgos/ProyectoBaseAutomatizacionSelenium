package page;

import base.baseInicializacion;
import locators.home_locators;
import locators.phones_locators;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class page_Home extends page_generic
        implements home_locators, phones_locators {



    public page_Home(WebDriver driver){
        super(driver);

    }



    public void clickPhones() {
        clicElemtent(btn_phones);
        validateTitle("Phones & PDAs", title_phones);
    }
}
