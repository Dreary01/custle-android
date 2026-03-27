# Custle Android

Android-клиент для `/root/custle`, разрабатывается изолированно внутри этой папки.

Сейчас в репозитории добавлен bootstrap-проект:
- Kotlin + Jetpack Compose
- Retrofit/OkHttp
- DataStore для JWT и выбранного workspace
- базовый контур `login -> workspace picker -> dashboard/inbox/projects/news/knowledge/todos/search/profile`
- Яндекс OAuth через встроенный WebView поверх существующего backend web callback
- news и discussions для mobile read-first сценариев
- roadmap по дальнейшему покрытию API
- автоматический deploy debug APK в S3-compatible storage через `./gradlew deployDebugApk`

## Структура

- `app/src/main/java/ru/custle/mobile/core` — сеть, модели, контейнер зависимостей, тема
- `app/src/main/java/ru/custle/mobile/feature` — фичи по экранам
- `app/src/main/java/ru/custle/mobile/navigation` — root state и orchestration
- `docs/roadmap.md` — карта функционального покрытия относительно `/root/custle`

## Что уже покрыто

- `POST /api/auth/login`
- `GET /api/auth/oauth/yandex`
- `GET /api/auth/workspaces`
- `POST /api/auth/switch-workspace`
- `GET /api/auth/me`
- `GET /api/dashboard/requests`
- `GET /api/dashboard/directions`
- `GET /api/dashboard/events`
- `GET /api/news`
- `GET/POST/PATCH/DELETE /api/todos`
- `POST /api/search`
- `GET /api/my/mentions`
- `PATCH /api/mentions/{id}/resolve`
- `PUT /api/auth/profile`
- `GET /api/telegram/status`
- `POST /api/telegram/link-code`
- `PUT /api/telegram/settings`
- `GET /api/workspaces/members`
- `POST /api/workspaces/members/invite`
- `PUT /api/workspaces/members/{userId}`
- `DELETE /api/workspaces/members/{userId}`
- `POST /api/invitations/accept`
- `POST /api/workspaces/members/invite`
- `PUT /api/workspaces/members/{userId}`
- `DELETE /api/workspaces/members/{userId}`
- `GET /api/objects/tree`
- `GET /api/objects/{id}`
- `GET /api/objects/{id}/ancestors`
- `GET /api/objects/{id}/participants`
- `GET /api/objects/{id}/plans`
- `GET /api/objects/{id}/dependencies`
- `GET /api/objects/{id}/discussions`
- `POST /api/objects/{id}/discussions`
- `GET /api/discussions/{id}/messages`
- `POST /api/discussions/{id}/messages`
- `GET /api/objects/{id}/documents/files`
- `GET /api/objects/{id}/documents/index-status`
- `GET /api/objects/{id}/documents/info`
- `POST /api/objects/{id}/documents/upload`
- `GET /api/documents/{docId}/download`
- `GET /api/notes`
- `GET /api/notes/{id}`
- `GET /api/articles`
- `GET /api/articles/{id}`

## Локальный запуск

В текущем окружении проект собирается через локальный SDK и Gradle wrapper.

Чтобы довести проект до запуска в Android Studio:
1. Открыть эту папку как Gradle project.
2. Проверить версии AGP/Kotlin из `gradle/libs.versions.toml`.
3. При необходимости поменять `BuildConfig.DEFAULT_API_URL` и `BuildConfig.DEFAULT_WEB_URL` в `app/build.gradle.kts`.

По умолчанию API адрес для эмулятора: `http://10.0.2.2:8080/api/`.

## Deploy APK

Для upload собранного debug APK:

```bash
GRADLE_USER_HOME=/root/custle-android/.gradle-user GRADLE_OPTS='-Dorg.gradle.native=false' ./gradlew deployDebugApk
```

Task соберёт `app-debug.apk` и загрузит его в настроенное S3-compatible storage.
