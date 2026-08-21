package locators;

import org.openqa.selenium.By;

public interface generic_locators {
    By div_added_cart = By.xpath("//div[contains(@class,'alert-success')]");

    static By elementAByText(String textElement) {
        return By.xpath("//a[normalize-space()='" + textElement + "']");
    }

    static By elementTitleByText(String textElement) {
        return By.xpath("(//h1[normalize-space()='" + textElement + "'] | //h2[normalize-space()='" + textElement + "'])");
    }

    static By elementButtonByText(String textElement) {
        return By.xpath("//button[normalize-space()='" + textElement + "']");
    }

    static By elementButtonById(String textElement) {
        return By.id(textElement);
    }

    static By returnPriceToTitleText(String textElement) {
        return By.xpath(
                elementTitleByText(textElement).toString().replace("By.xpath: ", "")
                        + "/following-sibling::ul//h2"
        );
    }
}
