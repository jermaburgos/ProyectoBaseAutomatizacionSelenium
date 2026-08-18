package ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AIAnalyzerService {

    // Modelo configurable por system property; mantiene un default seguro y actualizable.
    private static final String MODEL = System.getProperty("openai.model", "gpt-4o-mini");
    private static final String FALLBACK_MODEL = System.getProperty("openai.model.fallback", "gpt-4.1-mini");

    private static final Map<String, String> cacheIA =
            new ConcurrentHashMap<>();

    public static String analizarError(FailureContext context) {

        String errorKey =
                generarErrorKey(context);

        // ¿Ya analizamos este error?
        String analisisCache =
                cacheIA.get(errorKey);

        if (analisisCache != null) {

            return analisisCache
                    + "\n\n[Análisis reutilizado]";
        }

        // Si no existe, llamamos a OpenAI
        String analisis =
                llamarOpenAI(context);

        // Guardamos respuesta
        cacheIA.put(
                errorKey,
                analisis
        );

        return analisis;
    }

    private static String llamarOpenAI(
            FailureContext context) {

        String apiKey =
                System.getProperty("openai.apikey");

        if (apiKey == null || apiKey.isBlank()) {
            return "Análisis IA deshabilitado: no se configuró openai.apikey.";
        }

        String prompt =
                "Eres un QA Automation Senior experto en Selenium, Java y TestNG.\n" +
                        "Analiza el fallo usando solo la evidencia.\n\n" +

                        "TEST: " + context.getTestName() + "\n" +
                        "URL: " + context.getUrl() + "\n" +
                        "BROWSER: " + context.getBrowser() + "\n" +
                        "PASO: " + context.getUltimoPaso() + "\n" +
                        "LOCATOR: " + context.getLocator() + "\n" +
                        "ERROR: " + context.getError() + "\n" +
                        "STACKTRACE:\n" + context.getStackTrace() + "\n\n" +

                        "Responde máximo en 6 líneas:\n" +
                        "Tipo:\n" +
                        "Causa:\n" +
                        "Evidencia:\n" +
                        "Solución:\n" +
                        "Clasificación: AUTOMATION_ERROR | APPLICATION_BUG | DATA_ERROR | ENVIRONMENT_ERROR | NETWORK_ERROR | UNKNOWN\n" +
                        "Confianza: 0-100";

        try {
            return ejecutarRespuesta(prompt, MODEL);
        } catch (Exception primary) {
            // Fallback útil cuando el modelo fue deprecado/no disponible.
            try {
                String texto = ejecutarRespuesta(prompt, FALLBACK_MODEL);
                return texto + "\n\n[Modelo alterno: " + FALLBACK_MODEL + "]";
            } catch (Exception fallback) {
                return "No fue posible ejecutar el análisis de IA: "
                        + primary.getMessage();
            }
        }
    }

    private static String ejecutarRespuesta(String prompt, String model) {
        String apiKey =
                System.getProperty("openai.apikey");

        if (apiKey == null || apiKey.isBlank()) {
            return "Análisis IA deshabilitado: no se configuró openai.apikey.";
        }

        OpenAIClient client =
                OpenAIOkHttpClient.builder()
                        .apiKey(apiKey)
                        .build();

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .model(model)
                        .input(prompt)
                        .build();

        Response response =
                client.responses().create(params);

        return extraerTexto(response);
    }

    private static String extraerTexto(Response response) {
        return response.output()
                .stream()
                .flatMap(output -> output.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(text -> text.text())
                .findFirst()
                .orElse("No fue posible obtener análisis de IA.");
    }

    private static String generarErrorKey(FailureContext context) {

        return valorSeguro(context.getError())
                + "|"
                + valorSeguro(context.getLocator());
    }

    private static String valorSeguro(String valor) {

        return valor == null ? "" : valor;
    }

}
