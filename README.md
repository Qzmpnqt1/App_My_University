# Мой ВУЗ — Android-клиент

Клиентское приложение к backend `Server_My_University`: учебная структура, расписание, зачётная книжка, практики, статистика, чаты, регистрация.

## Требования

- Android **9+** (minSdk 29)
- JDK 17 для сборки
- Backend по **HTTPS** (в debug по умолчанию указан LAN-URL в `app/build.gradle.kts`)

## Сборка

```bash
cd App_My_University
./gradlew :app:assembleDebug
```

Базовый URL API (только HTTPS):

- **Debug:** задать в `gradle.properties`: `apiBaseUrl=https://<IP_ПК>:8443/` или править fallback в `app/build.gradle.kts`.
- **Release:** `-PapiBaseUrlRelease=https://ваш-api.example.com/`

## Стек

- Kotlin, Jetpack Compose, Material 3  
- Hilt, Retrofit, Coroutines  
- Навигация: `ui/navigation/AppNavigation.kt`  
- Уведомления в приложении: экран **Профиль → Уведомления** (данные с `/api/v1/notifications/*`)

## Тесты

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest   # при подключённом устройстве/эмуляторе
```

## CI/CD

См. `docs/CI_CD.md`.
