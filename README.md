# HandBook — Android-клиент справочника мест

Университетский учебный проект и первый опыт автора в разработке связки Android-клиента и собственного backend. Здесь собраны интерфейс справочника, аккаунт пользователя и работа с HTTP API.

Серверная часть: [HandBook_Backend](https://github.com/NolReaction/HandBook_Backend).

**Статус:** историческая учебная работа. Исходный код сохранён как этап перехода от отдельных экранов к приложению с сервером и базой данных.

## Возможности

- Регистрация, вход и запрос восстановления пароля через сервер.
- Профиль пользователя и изменение имени.
- Получение списка мест, поиск по списку и сохранение последних поисковых запросов.
- Отправка и получение истории выбранных мест.
- Гостевые экраны профиля и истории.
- Сохранение части настроек профиля и темы локально.

## Стек и структура

**Kotlin · Android Views/XML · Fragments · ViewBinding · Material Components · ViewModel · Coroutines · Retrofit/Gson · OkHttp · Preferences DataStore.**

| Часть | Где смотреть |
| --- | --- |
| Авторизация, регистрация и гостевые экраны | [ui/authorization](app/src/main/java/com/example/kursach_handbook/ui/authorization) |
| Поиск, профиль и история | [ui/login](app/src/main/java/com/example/kursach_handbook/ui/login) |
| Контракт API и создание Retrofit | [data/remote](app/src/main/java/com/example/kursach_handbook/data/remote) |
| Репозиторий работы с API | [data/repository](app/src/main/java/com/example/kursach_handbook/data/repository) |
| Хранение токена и HTTP interceptor | [data/token](app/src/main/java/com/example/kursach_handbook/data/token) |
| Локальные настройки профиля | [ProfileDataStore.kt](app/src/main/java/com/example/kursach_handbook/data/local/ProfileDataStore.kt) |

Проект использует Kotlin 1.9.24, AGP 8.8 и Android SDK 35. Минимальная версия устройства — Android 10 / API 29. Точные зависимости: [build.gradle.kts](app/build.gradle.kts) и [libs.versions.toml](gradle/libs.versions.toml).

## Локальный запуск

1. Поднять [HandBook_Backend](https://github.com/NolReaction/HandBook_Backend#запуск) со своей базой данных и конфигурацией.
2. Открыть этот проект в Android Studio, установить SDK 35 и выполнить Gradle Sync.
3. В [RetrofitProvider.kt](app/src/main/java/com/example/kursach_handbook/data/remote/RetrofitProvider.kt) выбрать адрес своего сервера. Для стандартного Android Emulator и сервера на компьютере уже предусмотрен `TEST_URL` — `http://10.0.2.2:8080/`; заменить `.baseUrl(HTTP_URL)` на `.baseUrl(TEST_URL)`.
4. Для физического телефона указать LAN-адрес компьютера с сервером; телефон и компьютер должны иметь сетевой доступ друг к другу.
5. Запустить `app` на устройстве или эмуляторе.

Сборка APK из корня:

```bash
./gradlew :app:assembleDebug
```

В Windows — `gradlew.bat`. APK появляется в `app/build/outputs/apk/debug/app-debug.apk`. Работоспособность старого удалённого адреса из исходного кода не предполагается: для воспроизведения проекта нужен свой backend.

## Что учитывать

Это учебная реализация: в клиенте разрешён HTTP и включено подробное логирование HTTP body. Для локального знакомства используйте тестовые аккаунты. Настройки транспорта, хранения токенов и логирования требуют отдельной доработки перед публичным использованием.

В коде поиска переход к подробной карточке места оставлен как TODO. Стартовые тесты Android-шаблона не подтверждают полное покрытие сценариев. Ценность проекта — в первом сквозном опыте: экран → репозиторий → API → сервер → база данных.
