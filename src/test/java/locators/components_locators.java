package locators;

import org.openqa.selenium.By;

public interface components_locators extends generic_locators {
    By menu_components = By.xpath("//nav[@id='menu']//a[normalize-space()='Components']");
    By menu_monitors = By.xpath("//nav[@id='menu']//a[contains(text(),'Monitors')]");
    By title_components = By.xpath("//div[@id='content']//h2[normalize-space()='Components']");
    By title_monitors = By.xpath("//div[@id='content']//h2[normalize-space()='Monitors']");

    static By returnPriceComponentToTitleText(String textElement) {
        return generic_locators.returnPriceToTitleText(textElement);
    }
}
