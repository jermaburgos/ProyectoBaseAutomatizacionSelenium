package locators;

import org.openqa.selenium.By;

public interface home_locators {
    By btn_phones = By.xpath("//a[text()='Phones & PDAs']");
    By btn_cameras = By.xpath("//a[contains(@href,'path=33') and normalize-space()='Cameras']");
    By div_cart = By.id("cart");
    By a_viewCart = By.xpath("//strong[contains(text(),'View Cart')]/parent::a");
}
