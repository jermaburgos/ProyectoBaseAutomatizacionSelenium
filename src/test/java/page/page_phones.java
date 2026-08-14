package page;

import locators.phones_locators;
import org.openqa.selenium.WebDriver;

public class page_phones extends page_generic
        implements phones_locators {

    public page_phones(WebDriver driver) {
        super(driver);
    }

    public void validateTitlePhones() {
        validateTitle("Phones & PDAs", title_phones);
    }
}
