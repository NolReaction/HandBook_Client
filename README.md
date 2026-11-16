# HandBook: Android-клиент

HandBook я делал для университетской работы. Это мой первый проект, в котором я связал Android-приложение с собственным сервером и базой данных. На нём я начал разбираться, как клиент отправляет запрос, что с ним делает backend и как результат возвращается на экран.

Я сделал справочник мест с поиском, аккаунтом и историей выбранных мест. В приложении есть регистрация, вход, запрос восстановления пароля и изменение имени в профиле. Для гостевого режима я добавил отдельные экраны профиля и истории, а часть настроек профиля и темы сохраняется локально.

Сервер я вынес в отдельный репозиторий: [HandBook_Backend](https://github.com/NolReaction/HandBook_Backend).

Этот проект я оставил как часть своей истории обучения. Здесь есть незаконченные места: например, переход из поиска к подробной карточке пока отмечен TODO.

## Код и технологии

Я использовал Kotlin, Android Views/XML, Fragments, ViewBinding, Material Components, ViewModel и Coroutines. Запросы к серверу отправляются через Retrofit с Gson и OkHttp, локальные настройки хранятся в Preferences DataStore.

- [ui/authorization](app/src/main/java/com/example/kursach_handbook/ui/authorization): вход, регистрация и гостевые экраны.
- [ui/login](app/src/main/java/com/example/kursach_handbook/ui/login): поиск, профиль и история.
- [data/remote](app/src/main/java/com/example/kursach_handbook/data/remote): API и настройка Retrofit.
- [data/repository](app/src/main/java/com/example/kursach_handbook/data/repository): работа с API.
- [data/token](app/src/main/java/com/example/kursach_handbook/data/token): хранение токена и interceptor.
- [ProfileDataStore.kt](app/src/main/java/com/example/kursach_handbook/data/local/ProfileDataStore.kt): локальные настройки профиля.

В проекте Kotlin 1.9.24, AGP 8.8 и SDK 35. Минимальная версия Android: 10 / API 29. Зависимости указаны в [build.gradle.kts](app/build.gradle.kts) и [libs.versions.toml](gradle/libs.versions.toml).

## Как запустить

1. Подними [backend](https://github.com/NolReaction/HandBook_Backend#запуск) со своей базой данных.
2. Открой этот проект в Android Studio, установи SDK 35 и выполни Gradle Sync.
3. В [RetrofitProvider.kt](app/src/main/java/com/example/kursach_handbook/data/remote/RetrofitProvider.kt) укажи свой сервер. Для Android Emulator и backend на компьютере уже есть `TEST_URL` со значением `http://10.0.2.2:8080/`. Для этого варианта замени `.baseUrl(HTTP_URL)` на `.baseUrl(TEST_URL)`.
4. Для физического телефона укажи LAN-адрес компьютера. Оба устройства должны иметь сетевой доступ друг к другу.
5. Запусти `app` на телефоне или эмуляторе.

Сборка APK из корня:

```bash
./gradlew :app:assembleDebug
```

В Windows используй `gradlew.bat`. APK появится в `app/build/outputs/apk/debug/app-debug.apk`. Старый удалённый адрес в коде оставлен от учебного запуска, на его доступность рассчитывать не стоит.

В этой версии разрешён HTTP и включено подробное логирование HTTP body, поэтому для знакомства с проектом нужны тестовые аккаунты. Перед публичным использованием нужно пересмотреть транспорт, хранение токена и логи. Тесты пока стартовые, из Android-шаблона.
