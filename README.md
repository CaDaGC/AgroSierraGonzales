# AgroSierraGonzales
Aplicación Android de Kotlin y Compose con Clean Architecture. Módulos `shared` (KMP Android target, dominio y datos) y `androidApp`; no configura destinos iOS.
Capas: `domain` (modelos, contratos, casos de uso), `data` (Ktor, DTO, SQLDelight, repositorios), `presentation` (Compose y StateFlow), `di` (Koin). Token de sesión cifrado AES-GCM con Android Keystore.
Requiere Android Studio, Android SDK 36 y backend iniciado en el puerto 8080. En emulador `10.0.2.2` accede al localhost del equipo; URL en `shared/.../HttpClientFactory.kt`.
Abre la carpeta en Android Studio, sincroniza Gradle y ejecuta `androidApp`. Implementa login, lista/detalle de lotes, registro de labores offline y online, indicador pendiente, sincronización manual (sube pendientes antes de descargar) y cierre de sesión.

