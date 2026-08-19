package locators;

import org.openqa.selenium.By;

public interface cameras_locators {

    By title_cameras = By.xpath("//h2[text()='Cameras1']");
    By div_added_cart = By.xpath("//div[contains(@class,'alert-success')]");
    By select_product_option = By.xpath("(//select[contains(@name,'option')])[1]");
    By button_addToCart = By.id("button-cart");

    static By elementAByText(String textElement) {
        return By.xpath("//a[normalize-space()='" + textElement + "']");
    }

    static By elementTitleByText(String textElement) {
        return By.xpath("(//h1[normalize-space()='" + textElement + "'] | //h2[normalize-space()='" + textElement + "'])");
    }
}
