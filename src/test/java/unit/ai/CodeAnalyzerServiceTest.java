package unit.ai;

import ai.CodeAnalyzerService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class CodeAnalyzerServiceTest {

    private String previousApiKey;

    @BeforeClass
    public void setUp() {
        previousApiKey = System.getProperty("openai.apikey");
        System.setProperty("openai.apikey", "unit-test-api-key");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (previousApiKey == null) {
            System.clearProperty("openai.apikey");
        } else {
            System.setProperty("openai.apikey", previousApiKey);
        }
    }

    @Test
    public void extraerMetodo_debeRetornarElMetodoCompleto() throws Exception {
        String codigoClase = ""
                + "package demo;\n"
                + "public class Demo {\n"
                + "    public void hola() {\n"
                + "        if (true) {\n"
                + "            System.out.println(\"ok\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        Method method = CodeAnalyzerService.class.getDeclaredMethod(
                "extraerMetodo",
                String.class,
                String.class
        );
        method.setAccessible(true);

        String codigoMetodo = (String) method.invoke(null, codigoClase, "hola");

        Assert.assertNotNull(codigoMetodo);
        Assert.assertTrue(codigoMetodo.contains("public void hola()"));
        Assert.assertTrue(codigoMetodo.contains("System.out.println(\"ok\");"));
    }

    @Test
    public void extraerContextoFuturo_debeRetornarDesdeElMarcador() throws Exception {
        String analisis = ""
                + "# Analisis\n"
                + "## Problemas confirmados\n"
                + "Ninguno.\n"
                + "## Contexto futuro\n"
                + "- Regla 1\n"
                + "- Regla 2\n";

        Method method = CodeAnalyzerService.class.getDeclaredMethod(
                "extraerContextoFuturo",
                String.class
        );
        method.setAccessible(true);

        String contexto = (String) method.invoke(null, analisis);

        Assert.assertTrue(contexto.startsWith("## Contexto futuro"));
        Assert.assertTrue(contexto.contains("Regla 1"));
        Assert.assertTrue(contexto.contains("Regla 2"));
    }
}
