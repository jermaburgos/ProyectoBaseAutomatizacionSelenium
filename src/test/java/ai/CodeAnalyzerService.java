package ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CodeAnalyzerService {

    private static final OpenAIClient client = OpenAIOkHttpClient.builder()
            .apiKey(System.getProperty("openai.apikey")).build();

    public static String analizarCodigo(
            String codigo,
            String contextoPrevio) {

        try {

            String prompt =
                    "Actua como Senior Java Developer y QA Automation Architect.\n" +
                            "Revisa este metodo de un framework Selenium + TestNG.\n\n" +

                            "REGLAS:\n" +
                            "- Se preciso y conciso.\n" +
                            "- No inventes problemas.\n" +
                            "- Reporta solo problemas demostrables con el codigo.\n" +
                            "- No repitas mejoras cubiertas por decisiones previas.\n" +
                            "- Si depende de codigo no visible, marca REQUIERE CONTEXTO.\n" +
                            "- Sugiere solo mejoras con impacto real.\n" +
                            "- No expliques conceptos basicos de Java o Selenium.\n\n" +

                            "DECISIONES PREVIAS:\n" +
                            (contextoPrevio == null || contextoPrevio.isBlank()
                                    ? "Ninguna.\n"
                                    : contextoPrevio + "\n") +

                            "\nCODIGO:\n" +
                            codigo +
                            "\n\n" +

                            "Responde solo en Markdown con esta estructura:\n\n" +

                            "# Analisis\n\n" +

                            "## Problemas confirmados\n" +
                            "- Problema | Prioridad | Evidencia breve | Impacto\n" +
                            "Si no hay: Ninguno.\n\n" +

                            "## Requiere contexto\n" +
                            "- Metodo/clase | Que verificar\n" +
                            "Si no aplica: Ninguno.\n\n" +

                            "## Mejoras nuevas\n" +
                            "- Mejora | Beneficio\n" +
                            "Si no hay: Ninguna.\n\n" +

                            "## Codigo recomendado\n" +
                            "Solo si hay una mejora confirmada. Muestra solo el codigo necesario.\n\n" +

                            "## Conclusion\n" +
                            "Maximo 1 linea.\n\n" +

                            "## Contexto futuro\n" +
                            "Maximo 3 decisiones cortas. No repitas informacion.";

            ResponseCreateParams params =
                    ResponseCreateParams.builder()
                            .model(ChatModel.GPT_5_2)
                            .input(prompt)
                            .maxOutputTokens(250L)
                            .build();

            Response response =
                    client.responses().create(params);

            return response.output()
                    .stream()
                    .flatMap(output -> output.message().stream())
                    .flatMap(message -> message.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .map(text -> text.text())
                    .findFirst()
                    .orElse("No fue posible analizar el codigo.");

        } catch (Exception e) {

            return "Error analizando codigo: "
                    + e.getMessage();
        }
    }

    public static String analizarMetodo(
            Class<?> clase,
            String nombreMetodo) {

        try {

            // Convertir el package de la clase en una ruta
            String rutaClase =
                    clase.getName()
                            .replace(".", "/");

            /*
             * Buscar primero en src/test/java
             * porque tu framework está ahí.
             */
            Path archivo =
                    Paths.get(
                            "src/test/java/" +
                                    rutaClase +
                                    ".java"
                    );

            /*
             * Si no existe, buscar en src/main/java
             */
            if (!Files.exists(archivo)) {

                archivo =
                        Paths.get(
                                "src/main/java/" +
                                        rutaClase +
                                        ".java"
                        );
            }

            if (!Files.exists(archivo)) {

                return "No se encontró el archivo fuente de la clase: "
                        + clase.getName();
            }

            // Leer código completo de la clase
            String codigoClase =
                    Files.readString(archivo);

            // Extraer únicamente el método solicitado
            String codigoMetodo =
                    extraerMetodo(
                            codigoClase,
                            nombreMetodo
                    );

            if (codigoMetodo == null) {

                return "No se encontró el método: "
                        + nombreMetodo
                        + " en "
                        + clase.getName();
            }

            /*
             * Reutilizamos el método que ya tienes
             * para enviar código a OpenAI.
             */

            String contextoPrevio =
                    leerContextoPrevio(
                            clase.getSimpleName(),
                            nombreMetodo
                    );
            String analisis =
                    analizarCodigo(codigoMetodo, contextoPrevio);
            String contextoFuturo =
                    extraerContextoFuturo(analisis);
            guardarContextoPrevio(
                    clase.getSimpleName(),
                    nombreMetodo,
                    contextoFuturo
            );
            guardarReporteMarkdown(
                    clase.getSimpleName(),
                    nombreMetodo,
                    analisis
            );

            return analisis;

        } catch (Exception e) {

            return "Error analizando método: "
                    + e.getMessage();
        }
    }


    private static String extraerMetodo(
            String codigoClase,
            String nombreMetodo) {

        /*
         * Buscamos:
         *
         * clicElement(
         */
        String patronMetodo =
                nombreMetodo + "(";

        int posicionNombre =
                codigoClase.indexOf(patronMetodo);

        if (posicionNombre == -1) {
            return null;
        }

        /*
         * Buscar la llave { que abre el método
         */
        int inicioLlave =
                codigoClase.indexOf(
                        "{",
                        posicionNombre
                );

        if (inicioLlave == -1) {
            return null;
        }

        /*
         * Buscar hacia atrás para obtener también:
         *
         * public void clicElement(...)
         */
        int inicioMetodo =
                codigoClase.lastIndexOf(
                        "\n",
                        posicionNombre
                );

        if (inicioMetodo == -1) {
            inicioMetodo = 0;
        } else {
            inicioMetodo++;
        }

        /*
         * Contaremos llaves para saber exactamente
         * dónde termina el método.
         */
        int contadorLlaves = 0;

        for (int i = inicioLlave;
             i < codigoClase.length();
             i++) {

            char caracter =
                    codigoClase.charAt(i);

            if (caracter == '{') {

                contadorLlaves++;

            } else if (caracter == '}') {

                contadorLlaves--;

                /*
                 * Cuando vuelve a 0 encontramos
                 * la llave final del método.
                 */
                if (contadorLlaves == 0) {

                    return codigoClase.substring(
                            inicioMetodo,
                            i + 1
                    );
                }
            }
        }

        return null;
    }

    private static void guardarReporteMarkdown(
            String nombreClase,
            String nombreMetodo,
            String contenido) {

        ZonedDateTime ahora = ZonedDateTime.now();

        // 2. Definir el formato requerido
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // 3. Formatear la fecha
        String fechaFormateada = ahora.format(formato);
        try {

            Path carpeta =
                    Paths.get(
                            "reports",
                            "codereview"
                    );

            // Crea la carpeta si no existe
            Files.createDirectories(carpeta);

            String nombreArchivo =
                    nombreClase
                            + "_"
                            + nombreMetodo+fechaFormateada
                            + "_CodeReview.md";

            Path archivo =
                    carpeta.resolve(nombreArchivo);

            Files.writeString(
                    archivo,
                    contenido
            );

            System.out.println(
                    "Code Review generado en: "
                            + archivo.toAbsolutePath()
            );

        } catch (IOException e) {

            System.out.println(
                    "Error guardando Code Review: "
                            + e.getMessage()
            );
        }
    }
    private static String leerContextoPrevio(
            String nombreClase,
            String nombreMetodo) {

        try {

            Path archivo = Paths.get(
                    "reports",
                    "codereview",
                    "history",
                    nombreClase + "_" + nombreMetodo + "_context.md"
            );

            if (!Files.exists(archivo)) {
                return "";
            }

            return Files.readString(archivo);

        } catch (IOException e) {
            return "";
        }
    }
    private static void guardarContextoPrevio(
            String nombreClase,
            String nombreMetodo,
            String contenido) {

        try {

            Path carpeta = Paths.get(
                    "reports",
                    "codereview",
                    "history"
            );

            Files.createDirectories(carpeta);

            String nombreArchivo =
                    nombreClase
                            + "_"
                            + nombreMetodo
                            + "_context.md";

            Path archivo =
                    carpeta.resolve(nombreArchivo);

            Files.writeString(
                    archivo,
                    contenido
            );

            System.out.println(
                    "Contexto guardado en: "
                            + archivo.toAbsolutePath()
            );

        } catch (IOException e) {

            System.out.println(
                    "Error guardando contexto: "
                            + e.getMessage()
            );
        }
    }
    private static String extraerContextoFuturo(
            String analisis) {

        String marcador =
                "## Contexto futuro";

        int inicio =
                analisis.indexOf(marcador);

        if (inicio == -1) {
            return "";
        }

        return analisis.substring(inicio);
    }
}