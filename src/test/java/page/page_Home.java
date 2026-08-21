package page;

import locators.home_locators;
import locators.components_locators;
import org.openqa.selenium.WebDriver;

public class page_Home extends page_generic
        implements home_locators, components_locators {



    public page_Home(WebDriver driver){
        super(driver);

    }



    public void clickPhones() {
        clicElement(btn_phones);
        validateTitle("Phones & PDAs", locators.phones_locators.title_phones);
    }

    public void clickCameras() {
        clicElement(btn_cameras);
        validateTitle("Cameras", locators.cameras_locators.title_cameras);
    }

    public void ingresarCarrito() {
        ejecutarPaso("Ingresar al carrito", () -> {
            clicElement(div_cart);
            clicElement(a_viewCart);
        });
    }

    public void irAComponentsYMonitors() {
        ejecutarPaso("Ir a Components", () -> {
            hoverElement(menu_components);
        });

        ejecutarPaso("Ingresar a Monitors", () -> {
            clicElement(menu_monitors);
        });
        validateTitle("Monitors", title_monitors);
    }
}
