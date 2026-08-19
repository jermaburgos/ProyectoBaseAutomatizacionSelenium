# ProyectoBaseAutomatizacionSelenium

Framework base de automatización UI con **Java 11**, **Selenium**, **TestNG** y **ExtentReports**.
Incluye:
- Page Object Model
- `DriverFactory`
- `ContextManager`
- `TestListener`
- reportes HTML
- resumen de aprobación por grupo y global
- veredicto automático de ejecución
- soporte opcional de análisis con IA
- pruebas unitarias para lógica pura

## Stack

- Java 11
- Maven
- Selenium 4.35.0
- TestNG 7.12.0
- ExtentReports 5.1.2
- OpenAI SDK Java 4.43.0

## Estructura relevante

- Tests funcionales: `src/test/java/test`
- Tests unitarios: `src/test/java/unit`
- Pages: `src/test/java/page`
- Locators: `src/test/java/locators`
- Base de inicialización: `src/test/java/base`
- Driver factory: `src/test/java/driver/driverFactory.java`
- Contexto compartido: `src/test/java/context`
- Reportes y listener: `src/test/java/report`
- Suite TestNG: `src/test/resources/testng.xml`

## Arquitectura actual

El proyecto separa dos niveles de validación:

1. **Unit tests**
   - Validan lógica pura sin navegador.
   - Ejemplos actuales:
     - `context/TestContext`
     - `context/ContextManager`
     - helpers de `report/GroupStatistics`
     - helpers privados de `ai/CodeAnalyzerService`

2. **Tests UI / integración**
   - Validan flujos reales con Selenium.
   - Las interacciones Selenium viven en `page/*`.
   - Los tests solo describen el flujo de negocio.

## Requisitos previos

1. Java 11 instalado y en el `PATH`
   - Verifica con `java -version`

2. Maven instalado y en el `PATH`
   - Verifica con `mvn -v`

3. Navegador instalado
   - Chrome
   - Firefox
   - Edge

4. Driver disponible en el entorno
   - El proyecto instancia `ChromeDriver`, `FirefoxDriver` y `EdgeDriver`.

## Ejecución rápida

### Suite completa

```powershell
mvn test
```

### Navegador específico

```powershell
mvn test -Dbrowser=chrome
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
```

### Headless

```powershell
mvn test -Dbrowser=chrome -Dheadless=true
```

### Capturas en Extent

```powershell
mvn test -Dscreenshots=true
```

## Veredicto automático

El listener calcula una aprobación global y por grupo con esta fórmula:

`passed / (passed + failed) * 100`

Los `skipped` no entran en el porcentaje de aprobación.

### Umbral

El umbral se controla con:

```powershell
mvn test -Dapproval.threshold=95
```

Valor por defecto:
- `95`

### Comportamiento

- Si el porcentaje global alcanza el umbral, la ejecución termina como `APROBADO`.
- Si no lo alcanza, la ejecución termina como `NO APROBADO`.
- Cuando no aprueba, el listener lanza una excepción al final para que Maven devuelva código de salida no cero.

### Dónde se ve

- Consola
- ExtentReports
- archivo markdown en `reports/summary`

## Pruebas unitarias

Se agregaron pruebas unitarias para lógica pura:

- `unit/context/TestContextTest`
- `unit/context/ContextManagerTest`
- `unit/report/GroupStatisticsTest`
- `unit/ai/CodeAnalyzerServiceTest`

### Ejecutarlas

```powershell
mvn test "-Dtest=TestContextTest,ContextManagerTest,CodeAnalyzerServiceTest,GroupStatisticsTest"
```

## Pruebas UI

### Suite TestNG

La suite está en `src/test/resources/testng.xml` y registra `report.TestListener`.

`testng.xml` separa:

- `Regression`
  - ejecuta todos los tests del paquete `test` excluyendo `critical`
- `Critical`
  - ejecuta solo el grupo `critical`

### Casos principales

| Caso | Grupo | Propósito | Ejecución |
| --- | --- | --- | --- |
| `test.MonitorsTest#configurarAppleCinemaYValidarRadioRequerido` | `critical`, `smoke`, `regression` | Flujo de Apple Cinema con opciones dinámicas, subida interna de archivo, validación de radio requerido y veredicto de aprobación. | `mvn test -Dtest=MonitorsTest -Dheadless=true` |
| `test.CamerasTest#agregarProductosDesdeCameras` | `smoke`, `regression` | Flujo de compra desde Cameras con opción dinámica para `Canon EOS 5D`. | `mvn test -Dtest=CamerasTest -Dheadless=true` |
| `test.PublicSiteTest#addCart` | `smoke`, `regression` | Flujo histórico de Phones. | `mvn test -Dtest=PublicSiteTest#addCart -Dheadless=true` |
| `test.PublicSiteTest#goToMonitors` | `smoke`, `regression` | Flujo histórico de Components > Monitors validando total. | `mvn test -Dtest=PublicSiteTest#goToMonitors -Dheadless=true` |
| `test.PublicSiteTest#addComponentsAndPhonesProducts` | `critical`, `smoke`, `regression` | Flujo crítico de regresión con varios productos en el carrito. | `mvn test -Dgroups=critical` o `mvn test` |

## Reportes generados

Al finalizar la ejecución:

- Reporte HTML de Extent:
  - `reports/AutomationReport<timestamp>.html`
- Resumen de ejecución:
  - `reports/summary/TestSummary<timestamp>.md`
- Resumen de code review IA:
  - `reports/codereview/*.md`

El reporte HTML muestra:

- total ejecutado
- passed
- failed
- skipped
- approval percentage
- threshold configurado
- veredicto final
- aprobación por grupo

## Componentes del framework

### `page_generic`

Contiene helpers reutilizables:

- click
- hover
- select
- select por texto parcial
- escritura
- upload interno al `input[type='file']`
- captura de pantalla
- validaciones genéricas

### `page_monitors`

Contiene la lógica específica de Apple Cinema:

- seleccionar radio
- seleccionar checkbox
- escribir texto
- select dinámico
- textarea
- subir archivo
- fechas y hora
- agregar al carrito
- validar error requerido

### `TestListener`

Administra:

- estado de cada test
- reportes Extent
- resumen global y por grupo
- veredicto final
- exportación de resumen markdown
- análisis de fallos con IA

## IA opcional

El proyecto mantiene un análisis opcional con OpenAI para fallos y revisión de código.

### Recomendación

Si vas a usar esa parte, configura la API key en tu entorno o como property según tu implementación:

```powershell
-Dopenai.apikey=TU_API_KEY
```

## Comandos útiles

```powershell
# Suite completa
mvn test

# Suite headless
mvn test -Dheadless=true

# Umbral de aprobación personalizado
mvn test -Dapproval.threshold=97

# Headless + umbral personalizado
mvn test -Dheadless=true -Dapproval.threshold=95

# Solo unit tests
mvn test "-Dtest=TestContextTest,ContextManagerTest,CodeAnalyzerServiceTest,GroupStatisticsTest"

# Solo un flujo UI
mvn test -Dtest=MonitorsTest -Dheadless=true
```

## CI/CD en GitHub Actions

El workflow está en `.github/workflows/automation.yml` y expone `inputs` para ejecución manual.

### Disparadores

- `push` a `main` y `develop`
- `pull_request` hacia `main`
- ejecución manual con `workflow_dispatch`

### Inputs manuales

Cuando ejecutas el workflow manualmente puedes definir:

- `suite`
  - `smoke`
  - `regression`
- `approval_threshold`
  - umbral de aprobación del run
  - valor por defecto: `95`

### Ejemplo de inputs

```text
suite: smoke
approval_threshold: 95
```

### Comportamiento

- Si eliges `smoke`, el workflow ejecuta solo smoke.
- Si eliges `regression`, el workflow ejecuta solo regression.
- En `push` y `pull_request`, el workflow mantiene el comportamiento automático.
- El umbral se pasa a Maven como `-Dapproval.threshold=<valor>`.

### Resultado del workflow

- Genera reportes HTML de Extent.
- Guarda resúmenes markdown en `reports/summary`.
- Sube artifacts con:
  - `reports/`
  - `target/surefire-reports/`

## Troubleshooting

### 1) `Unsupported browser`

Usa solo:

- `chrome`
- `firefox`
- `edge`

### 2) El driver no inicia

- Verifica compatibilidad entre navegador y driver.
- Prueba sin headless.

### 3) No se genera el reporte

- Revisa permisos de escritura en `reports/`.
- Valida que `testng.xml` cargue `report.TestListener`.

### 4) No aparecen capturas

- Ejecuta con `-Dscreenshots=true`.
- Asegúrate de que el flujo llame a `tomarCaptura(...)`.

### 5) El run termina como `NO APROBADO`

- Revisa el porcentaje global.
- Ajusta `-Dapproval.threshold=...` si el umbral es más estricto de lo deseado.

## Nota final

La ejecución está pensada para funcionar tanto en modo visual como en headless, y para dejar evidencia tanto en consola como en archivos de reporte.
