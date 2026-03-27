# Roadmap

## Цель

Сделать Android-клиент с максимальным покрытием функционала backend из `/root/custle/services/api`, не меняя исходный проект.

## Текущий статус

Собран стартовый shell:
- auth
- workspace selection
- dashboard
- базовая архитектура для расширения

## Очереди реализации

### Wave 1. User Core

- login/register/oauth callback
- workspace picker / switch
- dashboard
- profile
- global search
- todos

### Wave 2. Project Core

- objects list
- object tree
- object card
- discussions/messages/mentions
- participants
- news

### Wave 3. Knowledge + Documents

- notes list/detail
- articles
- knowledge graph word cloud
- documents list/download/upload
- document index status
- generated docs entrypoints

### Wave 4. Planning + Data

- plans
- dependencies
- ref tables list/detail
- ref records
- reports read mode

### Wave 5. Admin

- users
- permissions
- object types
- requisites
- workspace settings
- admin settings

### Wave 6. Extended / Hybrid

- widget store
- widget layouts
- marketplace
- superadmin
- document template editor

Для сложных desktop-heavy экранов допускается hybrid mode:
- native shell
- authenticated WebView fallback

## API Coverage Matrix

### Already wired

- `/api/auth/login`
- `/api/auth/workspaces`
- `/api/auth/switch-workspace`
- `/api/auth/me`
- `/api/dashboard/requests`
- `/api/dashboard/directions`
- `/api/dashboard/events`

### Next recommended endpoints

- `/api/search`
- `/api/todos`
- `/api/objects`
- `/api/objects/tree`
- `/api/objects/{id}`
- `/api/objects/{id}/discussions`
- `/api/discussions/{id}/messages`
- `/api/notes`
- `/api/articles`
- `/api/documents/{docId}/download`

## Технические решения

- `AppContainer` вместо тяжёлого DI на старте
- `DataStore` для session persistence
- `Retrofit + kotlinx.serialization` для API
- root state machine для bootstrap и переходов

## Ограничения среды

- В этой среде нет `java`, `gradle`, `kotlinc`
- Нельзя автоматически проверить сборку и сгенерировать wrapper jar
- Все изменения ограничены `/root/custle-android`
