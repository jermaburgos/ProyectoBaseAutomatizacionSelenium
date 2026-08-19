package locators;

import org.openqa.selenium.By;

public interface apple_cinema_locators {

    By title_apple_cinema = By.xpath("//h1[contains(normalize-space(), 'Apple Cinema 30')]");
    By button_add_to_cart = By.id("button-cart");

    static String optionLabelXpath(String optionLabel) {
        return "//h3[normalize-space()='Available Options']"
                + "/following::label[normalize-space()='" + optionLabel + "'][1]";
    }

    static String optionValueXpath(String optionText) {
        return "//h3[normalize-space()='Available Options']"
                + "/following::label[contains(normalize-space(.), '" + optionText + "')][1]";
    }

    static By radioOptionByText(String optionText) {
        return By.xpath(
                optionValueXpath(optionText)
                        + "/input[@type='radio']"
        );
    }

    static By checkboxOptionByText(String optionText) {
        return By.xpath(
                optionValueXpath(optionText)
                        + "/input[@type='checkbox']"
        );
    }

    static By selectOption() {
        return By.xpath(
                optionLabelXpath("Select")
                        + "/following::select[1]"
        );
    }

    static By textOption() {
        return By.xpath(
                optionLabelXpath("Text")
                        + "/following::input[@type='text'][1]"
        );
    }

    static By textareaOption() {
        return By.xpath(
                optionLabelXpath("Textarea")
                        + "/following::textarea[1]"
        );
    }

    static By fileButtonOption() {
        return By.xpath(
                "//button[contains(@id,'button-upload')]"
        );
    }

    static By fileInputOption() {
        return By.xpath("//input[@type='file']");
    }

    static By dateOption() {
        return By.xpath(
                optionLabelXpath("Date")
                        + "/following::input[1]"
        );
    }

    static By timeOption() {
        return By.xpath(
                optionLabelXpath("Time")
                        + "/following::input[1]"
        );
    }

    static By dateTimeOption() {
        return By.xpath(
                optionLabelXpath("Date & Time")
                        + "/following::input[1]"
        );
    }

    static By radioError() {
        return By.xpath(
                "//div[contains(@class,'alert-danger')]"
                        + " | //div[contains(@class,'text-danger')]"
                        + " | //div[contains(@class,'invalid-feedback')]"
        );
    }
}
