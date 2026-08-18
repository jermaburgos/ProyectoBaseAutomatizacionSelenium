# ProyectoBaseAutomatizacionSelenium

Framework base de automatización UI con **Java 11**, **Selenium**, **TestNG** y **ExtentReports**.
Incluye un **listener de TestNG** que genera reporte HTML y, de forma opcional, un análisis de fallos con IA (SDK `openai-java`).

## Stack y versiones
- Java: **11** (configurado en `pom.xml`)
- Maven
- Selenium: **4.35.0**
- TestNG: **7.12.0**
- ExtentReports: **5.1.2**
- OpenAI SDK Java: **4.43.0**

## Estructura relevante
- Tests: `src/test/java/` (ej.: `src/test/java/test/publicSite.java`)
- Suite TestNG: `src/test/resources/testng.xml`
- Listener/reportes:
  - `src/test/java/report/TestListener.java`
  - `src/test/java/report/ExtentManager.java`
- Driver factory: `src/test/java/driver/driverFactory.java`
- Reportes generados:
  - UI: `reports/AutomationReport<timestamp>.html`
  - Code review (IA): `reports/codereview/*.md` (si usas `CodeAnalyzerService`)

## Mejoras recientes
- El driver ahora se crea y se cierra por cada `@Test`, evitando reutilizar la sesión de la prueba anterior.
- El cierre del navegador queda explícito en consola por nombre de test.
- `TestListener` fue endurecido para no romper la suite si falla `ExtentReports`, `ExtentTest` o el análisis IA.
- `AIAnalyzerService` ahora es opcional: si no existe `openai.apikey`, la ejecución sigue y solo se informa que la IA quedó deshabilitada.
- `ExtentManager` crea la carpeta `reports/` y encapsula su inicialización para evitar caídas por configuración de reporte.
- `driverFactory` fue refactorizado con helpers privados para Chrome, Firefox y Edge.
- `page_generic` fue simplificado con helpers internos para esperas visibles y captura de pantalla.
- Se agregó soporte de ejecución por grupo `critical` en `testng.xml`.
- Se creó el flujo mixto de compra desde `Components` y `Phones` como caso crítico de regresión.

## Requisitos previos
1) **Java 11** instalado y en el `PATH`
   - Verifica: `java -version`

2) **Maven** instalado y en el `PATH`
   - Verifica: `mvn -v`

3) **Navegador instalado** (según ejecución)
   - Chrome / Firefox / Edge

4) **Drivers de navegador**
   - Este proyecto instancia `ChromeDriver`, `FirefoxDriver` y `EdgeDriver` directamente.
   - Asegúrate de tener el driver correspondiente disponible en el `PATH` (o configurado según tu entorno).

## Instalación
Desde la raíz del proyecto:
```powershell
mvn -q -DskipTests clean test
```
> La primera vez descargará dependencias.

## Configuración de ejecución
### Browser
El browser se controla por System Property `browser` (ver `baseInicializacion`):
- `chrome` (default)
- `firefox`
- `edge`

Ejemplos:
```powershell
mvn test -Dbrowser=chrome
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
```

### Headless
El modo headless se controla por `headless` (ver `driverFactory`):
```powershell
mvn test -Dbrowser=chrome -Dheadless=true
```

### Captura de pantalla en el reporte (screenshots)
Las capturas se controlan por el parámetro **`screenshots`** (ver `page/page_generic.java`).
- `false` (default): no adjunta capturas.
- `true`: adjunta capturas al **ExtentReport** en los pasos donde se invoque `tomarCaptura(...)`.

Ejemplo:
```powershell
mvn test -Dscreenshots=true
```

> Nota: este flag habilita/deshabilita la captura; la captura se adjunta cuando el framework llama explícitamente a `tomarCaptura("nombre")`.

## Ejecución de pruebas (TestNG)
La suite configurada está en `src/test/resources/testng.xml` y registra el listener `report.TestListener`.

Ejecución estándar:
```powershell
mvn test
```

Si necesitas ejecutar una suite específica desde Maven (opcional, según tu configuración/IDE):
```powershell
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

### Ejecución por grupo
El suite `testng.xml` está dividido en dos bloques:
- `Regression`: ejecuta `test` excluyendo `critical`
- `Critical`: ejecuta solo el grupo `critical`

Comandos útiles:
```powershell
# Ejecutar solo el grupo critical
mvn -Dgroups=critical test

# Ejecutar la suite completa definida en testng.xml
mvn test

# Ejecutar solo la suite TestNG explícita
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

### Casos principales
| Caso | Grupo | Propósito | Ejecución |
| --- | --- | --- | --- |
| `test.PublicSiteTest#addComponentsAndPhonesProducts` | `critical`, `smoke`, `regression` | Agregar un producto desde `Components > Monitors` y dos productos desde `Phones`, validando 3 productos en el carrito sin precios ni total. | `mvn -Dgroups=critical test` o `mvn test` |
| `test.CamerasTest#agregarProductosDesdeCameras` | `smoke`, `regression` | Flujo de compra desde `Cameras` con opción dinámica para `Canon EOS 5D`, sin validar precios ni total. | `mvn -Dtest=CamerasTest -Dheadless=true test` |
| `test.PublicSiteTest#addCart` | `smoke`, `regression` | Flujo histórico de `Phones`. | `mvn -Dtest=PublicSiteTest#addCart -Dheadless=true test` |
| `test.PublicSiteTest#goToMonitors` | `smoke`, `regression` | Flujo histórico de `Components > Monitors` validando total. | `mvn -Dtest=PublicSiteTest#goToMonitors -Dheadless=true test` |

- `test.PublicSiteTest#addComponentsAndPhonesProducts`
  - Caso crítico de regresión.
  - Agrega un producto desde `Components > Monitors` y dos productos desde `Phones`.
  - Valida el carrito con 3 productos, sin validar precios ni total.
- `test.CamerasTest#agregarProductosDesdeCameras`
  - Flujo de compra desde `Cameras`.
  - Selecciona opción dinámica para `Canon EOS 5D`.
  - Valida el carrito sin precios ni total.
- `test.PublicSiteTest#addCart`
  - Flujo histórico de Phones.
- `test.PublicSiteTest#goToMonitors`
  - Flujo histórico de Monitors.

## Reportes
Al finalizar la ejecución:
- Se genera un HTML de ExtentReports en: `reports/AutomationReport<timestamp>.html`
- En consola se imprime el resumen por grupo (si usas grupos en TestNG) desde `TestListener`.

## IA (opcional) – análisis de fallos y code review
El listener (`TestListener`) invoca `AIAnalyzerService.analizarError(...)` cuando un test falla.

### Modelo
El modelo se controla por System Property (sin cambios de código):
- `openai.model` (default actual: `gpt-4o-mini`)
- `openai.model.fallback` (default actual: `gpt-4.1-mini`)

Ejemplo:
```powershell
mvn test -Dopenai.model=gpt-4o-mini -Dopenai.model.fallback=gpt-4.1-mini
```

### API key
Actualmente la API key está en el código (pendiente de mover a variable/secret). Recomendación para cuando la migres:
- `OPENAI_API_KEY` o `-Dopenai.apiKey=...` (según implementación futura)

## Troubleshooting
**1) “Unsupported browser: …”**
- Usa solo: `chrome | firefox | edge`.

**2) Error creando el driver / sesión no inicia**
- Verifica que el driver del navegador esté disponible y sea compatible con la versión del browser.
- Prueba sin headless (`-Dheadless=false`).

**3) No se genera reporte**
- Revisa que exista la carpeta `reports/` (se crea al escribir el archivo, pero permisos pueden bloquear).
- Valida que el listener esté cargando (`testng.xml` tiene `report.TestListener`).

**4) No aparecen capturas**
- Asegúrate de ejecutar con `-Dscreenshots=true`.
- Verifica que el flujo esté llamando `tomarCaptura(...)` (la bandera por sí sola no toma capturas automáticamente).

**5) La IA falla (401/403/404)**
- 401/403: API key inválida o sin permisos.
- 404: modelo no disponible/deprecado → ajusta `-Dopenai.model=...`.

---

### Comandos rápidos
```powershell
# Chrome normal
mvn test -Dbrowser=chrome

# Chrome headless
mvn test -Dbrowser=chrome -Dheadless=true

# Chrome + capturas
mvn test -Dbrowser=chrome -Dscreenshots=true

# Forzar modelo IA
mvn test -Dopenai.model=gpt-4o-mini -Dopenai.model.fallback=gpt-4.1-mini
```
