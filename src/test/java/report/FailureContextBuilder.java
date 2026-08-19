package report;

import ai.FailureContext;
import context.ContextManager;
import driver.driverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

public class FailureContextBuilder {

    public FailureContext build(ITestResult result) {
        FailureContext failure = new FailureContext();

        failure.setTestName(valorSeguro(result.getMethod().getMethodName()));
        failure.setClassName(valorSeguro(result.getTestClass().getName()));
        failure.setBrowser(valorSeguro(System.getProperty("browser", "chrome")));

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            failure.setError(construirErrorResumen(throwable));
            failure.setStackTrace(obtenerStackTraceLimitado(throwable, 10));
        } else {
            failure.setError("No se registró excepción");
            failure.setStackTrace("No stack trace disponible");
        }

        failure.setUltimoPaso(valorSeguro(ContextManager.getContext().getUltimoPaso()));
        failure.setLocator(valorSeguro(ContextManager.getContext().getUltimoLocator()));
        failure.setUrl(valorSeguro(obtenerUrlActual()));

        return failure;
    }

    private String obtenerUrlActual() {
        WebDriver driver = driverFactory.getCurrentDriver();

        if (driver != null) {
            try {
                return driver.getCurrentUrl();
            } catch (Exception e) {
                return "No fue posible obtener la URL";
            }
        }

        return "Driver no disponible";
    }

    private String obtenerStackTraceLimitado(Throwable throwable, int limite) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StringBuilder resultado = new StringBuilder();
        int cantidad = Math.min(stackTrace.length, limite);

        for (int i = 0; i < cantidad; i++) {
            resultado.append(stackTrace[i]).append("\n");
        }

        return resultado.toString();
    }

    private String limitarTexto(String texto, int maximo) {
        if (texto == null) {
            return "";
        }

        if (texto.length() <= maximo) {
            return texto;
        }

        return texto.substring(0, maximo) + "...";
    }

    private String construirErrorResumen(Throwable throwable) {
        String mensaje = limitarTexto(throwable.getMessage(), 1000);

        if (mensaje.isBlank()) {
            return throwable.getClass().getSimpleName();
        }

        return throwable.getClass().getSimpleName() + ": " + mensaje;
    }

    private String valorSeguro(String valor) {
        if (valor == null || valor.isBlank()) {
            return "N/A";
        }

        return valor;
    }
}
