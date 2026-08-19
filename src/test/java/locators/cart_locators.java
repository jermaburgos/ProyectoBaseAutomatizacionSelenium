package locators;

import org.openqa.selenium.By;

public interface cart_locators {
    By title_cart = By.xpath("//div[@id='content']//h1");
    By td_products = By.xpath("//form//tbody//td[2]/a[1]");
    By td_price = By.xpath("//form//tbody//td[6]");
    By td_priceTotal = By.xpath("//div[@id='checkout-cart']//strong[starts-with(text(),'Total')]/parent::td//following-sibling::td[contains(text(),'$')]");
}
