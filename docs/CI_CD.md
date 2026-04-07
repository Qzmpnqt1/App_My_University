# CI/CD — App_My_University (Android)

## Структура Git и ограничение

- **Android** — отдельный git-репозиторий: корень = `App_My_University` (здесь `.github/workflows/`).
- **Backend** — отдельный репозиторий `Server_My_University`; см. `Server_My_University/docs/CI_CD.md`.

Родительская папка `MyUniversity` без общего `.git` **не** запускает единый pipeline на обе части.

---

## Реализованные pipelines

| Workflow        | Файл                         | Назначение                                      |
|-----------------|------------------------------|-------------------------------------------------|
| Android CI      | `.github/workflows/android-ci.yml`     | `assembleDebug`, unit tests, `lintDebug`, артефакты |
| Android Release | `.github/workflows/android-release.yml` | Release APK + AAB, `lintVitalRelease`, опциональная подпись |

---

## Триггеры

### Android CI

- **push** (любая ветка)  
- **pull_request** (любой PR)  

### Android Release

- **push** тегов **`v*`** (например `v1.0.0`)  
- **workflow_dispatch** — поле **api_base_url** (обязательно **https://**)

Для сборки по **тегу** нужен repository secret **`ANDROID_RELEASE_API_BASE_URL`** (HTTPS URL API). Для ручного запуска URL берётся из input workflow.

---

## Секреты

### Обязательно для release по тегу `v*`

| Secret                           | Назначение                    |
|----------------------------------|-------------------------------|
| `ANDROID_RELEASE_API_BASE_URL`   | HTTPS base URL (например `https://api.example.com/`) |

### Опционально: подпись release (Play / собственный keystore)

| Secret                          | Назначение                                      |
|---------------------------------|-------------------------------------------------|
| `ANDROID_KEYSTORE_BASE64`       | Файл keystore в Base64                          |
| `ANDROID_SIGNING_STORE_PASSWORD`| Пароль хранилища                                |
| `ANDROID_SIGNING_KEY_ALIAS`     | Alias ключа                                     |
| `ANDROID_SIGNING_KEY_PASSWORD`  | Пароль ключа (часто совпадает с store password) |

Если `ANDROID_KEYSTORE_BASE64` **не** задан, сборка идёт **без** кастомного keystore (как локально без переменных `ANDROID_SIGNING_*`).

Gradle читает переменные окружения **`ANDROID_SIGNING_*`** (см. `app/build.gradle.kts`).

---

## Артефакты

### CI

- **`android-debug-apk`** — `app/build/outputs/apk/debug/*.apk`  
- **`android-ci-reports`** — отчёты lint и тестов в `app/build/reports/`, `app/build/test-results/`

### Release

- **`android-release-<ref>`** — APK, AAB, при наличии — `mapping.txt` (`if-no-files-found: warn`)

---

## Локальные команды (как в CI)

**JDK 17** в `JAVA_HOME`. Из корня репозитория:

```bash
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:lintDebug --no-daemon
./gradlew :app:assembleRelease :app:bundleRelease -PapiBaseUrlRelease=https://example.com/ --no-daemon
```

Опциональная подпись release:

```bash
export ANDROID_SIGNING_STORE_FILE=/path/to/release.keystore
export ANDROID_SIGNING_STORE_PASSWORD=...
export ANDROID_SIGNING_KEY_ALIAS=...
export ANDROID_SIGNING_KEY_PASSWORD=...
./gradlew :app:assembleRelease -PapiBaseUrlRelease=https://api.example.com/ --no-daemon
```

Путь к JDK локально: `~/.gradle/gradle.properties` → `org.gradle.java.home=...` (не коммитить).

---

## Исправления ради CI/CD

- **Lint `RemoveWorkManagerInitializer`:** в `AndroidManifest.xml` добавлено слияние `InitializationProvider` с удалением метаданных `WorkManagerInitializer` (приложение использует Hilt + `Configuration.Provider`).
- Удалён **machine-specific** `org.gradle.java.home` из versioned `gradle.properties`.
- Опциональная **release signing** через переменные окружения `ANDROID_SIGNING_*`.

---

## Ограничения

- **NDK** в проекте не требуется для текущего `app/build.gradle.kts`.
- Доверие к dev-сертификату backend на устройстве по-прежнему задаётся в `res/xml/network_security_config.xml` и `apiBaseUrl` — для production API нужен доверенный сертификат или корректный pinning/конфиг.
