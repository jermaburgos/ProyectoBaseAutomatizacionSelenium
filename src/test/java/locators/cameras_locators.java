package locators;

import org.openqa.selenium.By;

public interface cameras_locators extends generic_locators {

    By title_cameras = By.xpath("//h2[text()='Cameras']");
    By select_product_option = By.xpath("(//select[contains(@name,'option')])[1]");
    By button_addToCart = By.id("button-cart");
}
