# UGEL - Ilo | Sistema de Gestión de Deudas Administrativas de Docentes

Prototipo funcional desarrollado en **JavaFX 21 + Maven**, adaptado a partir del
laboratorio base de mantenimiento de Distribuidores, siguiendo las instrucciones
de la **Prueba de Nivel de Logro Nivel II (Final) - Programación Orientada a Objetos**.

## Qué se implementó

| Requisito de la prueba | Dónde está en el código |
|---|---|
| CE1 - Interfaces + acceso a datos + POO en capas | `model` / `repository` / `service` / `controller` (arquitectura en capas con inyección de dependencias vía `config.AppContext`) |
| CE2 - Autenticación de usuarios | `login-view.fxml` + `LoginController` + `AuthService` + `util.PasswordUtil` (hash SHA-256 con salt, sin contraseñas en texto plano) |
| CE2 - Encriptación de datos confidenciales | `util.CryptoUtil` (AES-128/CBC). El DNI del docente se cifra antes de guardarse en la BD y se muestra enmascarado en pantalla (`****5678`) |
| CE3 - Consulta con filtros (fecha, tipo de deuda, situación laboral) | `DeudaDocenteController` (filtros en `deuda-view.fxml`) + `DeudaDocenteRepository.findByFilters` |
| CE3 - Exportar a PDF para control/auditoría | Botón "Exportar PDF" -> `util.PdfExportUtil` (Apache PDFBox) |
| CE3 - Algoritmo de análisis de riesgo (ciencia de datos) | `service.RiesgoAnalyzer`: modelo de puntuación ponderada (weighted scoring) que clasifica cada deuda en BAJO/MEDIO/ALTO según monto, días de mora y situación laboral. Está aislado detrás de una única función (`calcularRiesgo`) para poder sustituirse en el futuro por un modelo entrenado con ML/Deep Learning sin tocar el resto de la app |

## Cómo abrir el proyecto en IntelliJ IDEA

1. Abre IntelliJ IDEA -> **File -> Open...** -> selecciona la carpeta `lab12-ugel-ilo` (donde está este `README.md` y el `pom.xml`).
2. IntelliJ detectará que es un proyecto Maven y lo importará automáticamente (dale tiempo a que descargue las dependencias, incluyendo `pdfbox` y `mssql-jdbc`).
3. Verifica en **File -> Project Structure -> Project** que el SDK sea Java 21.
4. Antes de ejecutar, crea la base de datos con el **script SQL** que se entrega por separado (ver más abajo) en SQL Server Management Studio 22.
5. Ajusta usuario/contraseña/puerto en `src/main/java/com/ucv/lab12/config/DatabaseConfig.java` si tu instancia de SQL Server tiene credenciales distintas a las de ejemplo.
6. Ejecuta la clase `com.ucv.lab12.Launcher` (botón ▶ run) o usa la tarea Maven `javafx:run`.

## Usuarios de prueba (creados por el script SQL)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin`   | `Admin123!`    | ADMINISTRADOR |
| `auditor` | `Auditor123!`  | AUDITOR |

Las contraseñas se verifican contra un hash SHA-256 con salt; nunca se guardan en texto plano.

## Notas de seguridad para producción

- La llave AES en `CryptoUtil` está fija en el código solo para fines del prototipo académico. En un entorno real debe vivir en un gestor de secretos (Key Vault / KMS) y rotarse periódicamente.
- Se recomienda forzar conexión TLS real (`encrypt=true`) con un certificado válido al desplegar en producción, en vez de `trustServerCertificate=true`.
