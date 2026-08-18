package locators;

import org.openqa.selenium.By;

public interface components_locators {
    By menu_components = By.xpath("//nav[@id='menu']//a[normalize-space()='Components']");
    By menu_monitors = By.xpath("//nav[@id='menu']//a[contains(text(),'Monitors')]");
    By title_components = By.xpath("//div[@id='content']//h2[normalize-space()='Components']");
    By title_monitors = By.xpath("//div[@id='content']//h2[normalize-space()='Monitors']");

    static By elementAByText(String textElement) {
        return By.xpath("//a[normalize-space()='" + textElement + "']");
    }

    static By elementTitleByText(String textElement) {
        return By.xpath("(//h1[normalize-space()='" + textElement + "'] | //h2[normalize-space()='" + textElement + "'])");
    }

    static By elementButtonById(String textElement) {
        return By.id(textElement);
    }

    static By returnPriceComponentToTitleText(String textElement) {
        return By.xpath(elementTitleByText(textElement).toString().replace("By.xpath: ", "") + "/following-sibling::ul//h2");
    }

    By alert_success = By.xpath("//div[contains(@class,'alert-success')]");
}
