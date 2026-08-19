package context;

import java.util.ArrayList;
import java.util.List;

public class TestContext {

    private String producto;
    private final List<String> precios;
    private String ultimoPaso;
    private String ultimoLocator;

    public TestContext() {
        precios = new ArrayList<>();
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public List<String> getPrecios() {
        return precios;
    }

    public void setPrecio(String precio) {
        precios.add(precio);
    }

    public String getUltimoPaso() {
        return ultimoPaso;
    }

    public void setUltimoPaso(String ultimoPaso) {
        this.ultimoPaso = ultimoPaso;
    }

    public String getUltimoLocator() {
        return ultimoLocator;
    }

    public void setUltimoLocator(String ultimoLocator) {
        this.ultimoLocator = ultimoLocator;
    }
}
