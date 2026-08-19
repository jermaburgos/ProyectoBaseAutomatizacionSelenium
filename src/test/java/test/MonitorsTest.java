package test;

import jdk.jfr.Description;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MonitorsTest extends baseTest {

    @Test(groups = {"critical", "smoke", "regression"})
    @Description("Flujo de compra del producto Apple Cinema con opciones dinámicas y validación de radio requerido")
    public void configurarAppleCinemaYValidarRadioRequerido() throws Exception {

        String checkboxSeleccionado = System.getProperty("apple.checkbox", "Checkbox 3");
        String selectSeleccionado = System.getProperty("apple.select", "Blue");

        Path archivoTemporal = Files.createTempFile("apple-cinema-", ".txt");
        Files.write(
                archivoTemporal,
                "prueba".getBytes(StandardCharsets.UTF_8)
        );

        generic.navigateTo(
                "https://opencart.abstracta.us/index.php?product_id=42&route=product%2Fproduct",
                "Apple Cinema 30"
        );

        monitors.seleccionarCheckbox(checkboxSeleccionado);
        monitors.ingresarTexto("prueba automatizada");
        monitors.seleccionarOpcionSelect(selectSeleccionado);
        monitors.ingresarTextarea(
                "Esta es una prueba automatizada más extensa para validar el campo textarea del producto Apple Cinema."
        );
        monitors.subirArchivo(archivoTemporal.toAbsolutePath().toString());
        monitors.ingresarFechaActual();
        monitors.ingresarHoraActual();
        monitors.ingresarFechaHoraActual();
        monitors.agregarAlCarrito();
        monitors.validarErrorRadioRequerido();
    }
}
