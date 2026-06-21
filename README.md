# MultiSegure

Navegador Android multi-perfil con aislamiento de sesiones, proxy por perfil y Jetpack Compose.

## Características

- **Perfiles ilimitados**: Crea tantos perfiles como necesites
- **Aislamiento real**: Cada perfil corre en proceso separado (`:browser`). Al cerrar el navegador, el proceso se mata, eliminando cookies, localStorage, cache e historial de esa sesión
- **Proxy por perfil**: HTTP/HTTPS/SOCKS4/SOCKS5 con autenticación básica
- **User-Agent personalizable**: Selecciona entre múltiples User-Agents predefinidos
- **Modo incógnito**: Desactiva cookies y cache
- **JavaScript toggle**: Activa/desactiva JS por perfil
- **Bloqueo de trackers**: Preparado para futura implementación
- **Navegación tipo Chrome**: Barra inferior con atrás/adelante/recargar/inicio
- **UI moderna**: Jetpack Compose + Material 3

## Arquitectura

| Capa | Tecnología |
|------|-----------|
| Motor | WebView nativo de Android |
| UI | Jetpack Compose + Material 3 |
| Persistencia | Room Database |
| Red | OkHttp con proxy configurable |
| Async | Kotlin Coroutines + Flow |

## Compilación

### Requisitos
- Java 17
- Android SDK 35
- Gradle 8.4 (wrapper incluido)

### Comandos

```bash
# En Linux/macOS
chmod +x gradlew
./gradlew assembleDebug

# En Windows
gradlew.bat assembleDebug
```

### GitHub Actions

El proyecto incluye un workflow de GitHub Actions que compila automáticamente el APK en cada push. Ve a la pestaña **Actions** de tu repositorio para descargar el APK generado.

## Estructura del proyecto

```
app/src/main/java/com/multisegure/
├── MultiSegureApplication.kt
├── data/
│   ├── model/BrowserProfile.kt
│   ├── local/AppDatabase.kt
│   ├── local/ProfileDao.kt
│   └── repository/ProfileRepository.kt
├── network/
│   ├── ProxyManager.kt          # OkHttp con proxy por perfil
│   └── WebViewCookieManager.kt  # Gestión de cookies por perfil
├── viewmodel/
│   ├── ProfileViewModel.kt
│   └── BrowserViewModel.kt
├── ui/
│   ├── theme/                   # Color, Type, Theme
│   ├── main/MainActivity.kt     # Lista de perfiles
│   ├── browser/
│   │   ├── BrowserActivity.kt   # Activity en proceso :browser
│   │   └── BrowserScreen.kt    # WebView + barra de navegación
│   └── profile/
│       ├── ProfileEditActivity.kt
│       └── ProfileEditScreen.kt # Formulario de perfil
```

## Aislamiento de sesiones

El `AndroidManifest.xml` declara `BrowserActivity` con `android:process=":browser"`. Esto significa:

1. Cada vez que abres un perfil, se crea un nuevo proceso `:browser`
2. WebView almacena cookies, localStorage, IndexedDB, cache en directorios privados de ese proceso
3. Al cerrar `BrowserActivity` (botón X o back), `onDestroy()` ejecuta `Process.killProcess(Process.myPid())`
4. El proceso `:browser` muere junto con TODOS sus datos de sesión
5. El siguiente perfil que abras obtendrá un proceso `:browser` completamente limpio

**Limitación**: Solo un perfil puede estar abierto a la vez. Si abres otro, el anterior se cierra y su proceso se mata.

## Proxy por perfil

El proxy se implementa interceptando todas las peticiones del WebView mediante `shouldInterceptRequest()` y reenviándolas a través de OkHttp configurado con el proxy del perfil.

Soporta:
- HTTP proxy (sin autenticación o con Basic Auth)
- HTTPS proxy
- SOCKS4 / SOCKS5 (vía `java.net.Proxy.Type.SOCKS`)

## Versiones

| Componente | Versión |
|-----------|---------|
| compileSdk | 35 |
| minSdk | 28 (Android 9) |
| targetSdk | 35 |
| Kotlin | 1.9.22 |
| Compose Compiler | 1.5.8 |
| Compose BOM | 2024.06.00 |
| AGP | 8.2.2 |
| Gradle | 8.4 |
| OkHttp | 4.12.0 |
| Room | 2.6.1 |

## Licencia

Proyecto privado - MultiSegure Project
