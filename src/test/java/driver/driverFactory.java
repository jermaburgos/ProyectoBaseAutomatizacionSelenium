package driver;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class driverFactory {
    /*
     * Guarda el WebDriver correspondiente al hilo actual.
     *
     * Esto nos permitirá recuperar el mismo driver desde
     * TestListener cuando ocurra un fallo.
     */
    private static final ThreadLocal<WebDriver> driver =
            new ThreadLocal<>();

    public static WebDriver getDriver(String browser) {
        String browserNormalizado = normalizarBrowser(browser);
        boolean headless = esHeadless();

        WebDriver newDriver;

        switch (browserNormalizado) {

            case "chrome":
                newDriver = crearChrome(headless);
                break;

            case "firefox":
                newDriver = crearFirefox(headless);
                break;

            case "edge":
                newDriver = crearEdge(headless);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported browser: " + browser
                );
        }

        /*
         * Guardamos el driver creado en ThreadLocal
         */
        driver.set(newDriver);

        /*
         * Retornamos el mismo driver para que
         * baseInicializacion pueda utilizarlo.
         */
        return newDriver;
    }


    /*
     * Devuelve el driver correspondiente
     * al test que se está ejecutando.
     */
    public static WebDriver getCurrentDriver() {

        return driver.get();
    }


    /*
     * Cierra el driver y limpia ThreadLocal.
     */
    public static void quitDriver() {

        WebDriver currentDriver =
                driver.get();

        try {
            if (currentDriver != null) {
                currentDriver.quit();
            }
        } catch (Exception e) {
            System.out.println(
                    "No fue posible cerrar el driver: "
                            + e.getMessage()
            );
        } finally {
            driver.remove();
        }
    }

    private static String normalizarBrowser(String browser) {
        if (browser == null || browser.isBlank()) {
            return "chrome";
        }

        return browser.toLowerCase();
    }

    private static boolean esHeadless() {
        return Boolean.parseBoolean(
                System.getProperty("headless", "false")
        );
    }

    private static WebDriver crearChrome(boolean headless) {
        ChromeOptions chromeOptions = new ChromeOptions();
        configurarOpcionesComunes(chromeOptions, headless);
        chromeOptions.addArguments("--incognito");
        return new ChromeDriver(chromeOptions);
    }

    private static WebDriver crearFirefox(boolean headless) {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        configurarOpcionesComunes(firefoxOptions, headless);
        firefoxOptions.addArguments("-private");

        WebDriver newDriver = new FirefoxDriver(firefoxOptions);
        if (headless) {
            newDriver.manage()
                    .window()
                    .setSize(new Dimension(1920, 1080));
        }
        return newDriver;
    }

    private static WebDriver crearEdge(boolean headless) {
        EdgeOptions edgeOptions = new EdgeOptions();
        configurarOpcionesComunes(edgeOptions, headless);
        return new EdgeDriver(edgeOptions);
    }

    private static void configurarOpcionesComunes(ChromeOptions options, boolean headless) {
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.setAcceptInsecureCerts(true);


        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--force-device-scale-factor=0.8");
        }
    }

    private static void configurarOpcionesComunes(FirefoxOptions options, boolean headless) {
        options.setAcceptInsecureCerts(true);

        options.addPreference(
                "layout.css.devPixelsPerPx",
                "0.8"
        );

        if (headless) {
            options.addArguments("-headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }
    }

    private static void configurarOpcionesComunes(EdgeOptions options, boolean headless) {
        options.addArguments("--start-maximized");
        options.setAcceptInsecureCerts(true);

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--force-device-scale-factor=0.8");
        }
    }
}
