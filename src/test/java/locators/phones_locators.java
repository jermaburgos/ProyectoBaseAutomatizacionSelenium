package locators;

import org.openqa.selenium.By;

public interface phones_locators {
    By title_phones = By.xpath("//h2[text()='Phones & PDAs']");
    By div_added_cart = By.xpath("//div[contains(@class,'alert-success')]");

    static By elementAByText(String textElement) {
        By locator = By.xpath("//a[text()='" + textElement + "']");
        return locator;
    }

    static By elementTitleByText(String textElement) {
        By locator = By.xpath("(//h1[text()='"+textElement+"'] | //h2[text()='"+textElement+"'])");
        return locator;
    }

    static By elementButtonByText(String textElement) {
        By locator = By.xpath("//button[text()='"+textElement+"']");
        return locator;
    }
    static By elementButtonById(String textElement) {
        By locator = By.id(textElement);
        return locator;
    }
    static By returnPricePhoneToTitleText(String textElement) {
        By locator = By.xpath(elementTitleByText(textElement).toString().replace("By.xpath: ", "")+"/following-sibling::ul//h2");
        return locator;
    }
}
