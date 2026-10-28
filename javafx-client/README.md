# Велосервис — КР №2 (JavaFX)

Настольное приложение для работы с клиентами велосервиса и заявками на обслуживание велосипедов.
JavaFX подключается **напрямую через JDBC** к таблицам `clients` и `service_requests`, созданным в КР №1.

## Реализовано

- `TableView` с выбором заявки и связанными данными клиента;
- добавление и редактирование через отдельную FXML-форму;
- удаление только после подтверждения;
- клиент выбирается из `ComboBox`, ID вручную вводить не требуется;
- поиск по клиенту, телефону, марке, модели и описанию проблемы;
- фильтры по статусу и типу велосипеда;
- сортировка по ID, клиенту, стоимости и дате;
- восемь статистических показателей на отдельной вкладке;
- экспорт текущего отфильтрованного списка в `.xlsx` через `FileChooser`;
- проверка обязательных полей, стоимости, связей и переходов статуса;
- понятные диалоги ошибок без вывода stack trace пользователю;
- FXML, CSS, Controller, Service, DAO/JDBC и PostgreSQL разделены по слоям.

## Что установить на Windows

1. Docker Desktop и дождаться состояния **Running**.
2. JDK 21. После установки `java -version` и `javac -version` должны показывать 21.
3. Maven 3.9 или новее. Проверка: `mvn -version` должен использовать Java 21.
4. VS Code с Extension Pack for Java или IntelliJ IDEA.

Java 17 можно оставить для КР №1. Для запуска КР №2 активной должна быть Java 21.

## Первый запуск

Откройте PowerShell в корне репозитория:

```powershell
Copy-Item .env.example .env
notepad .env
```

В `.env` замените `change_this_password` на свой пароль. Файл игнорируется Git и не попадёт в репозиторий.

Запустите PostgreSQL:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-db.ps1
```

Проверьте контейнер:

```powershell
docker compose --env-file .env ps
```

Состояние должно быть `healthy`. При первом создании volume Docker автоматически выполнит `sql/01_schema.sql` и `sql/02_seed.sql`.

Запустите JavaFX:

```powershell
cd javafx-client
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Скрипт читает параметры из корневого `.env`, задаёт `DB_URL`, `DB_USER`, `DB_PASSWORD` только для процесса и выполняет `mvn javafx:run`.

## Повторный запуск

```powershell
powershell -ExecutionPolicy Bypass -File .\start-db.ps1
cd javafx-client
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Остановка без удаления данных:

```powershell
powershell -ExecutionPolicy Bypass -File .\stop-db.ps1
```

Не выполняйте `docker compose down -v`, если данные нужно сохранить.

## Подключение к уже установленному PostgreSQL

Если используется база КР №1 на локальном порту `5433`, Docker для самой базы можно не использовать при разработке, но Docker Desktop всё равно нужен для выполнения требования и демонстрации.

```powershell
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5433/bike_service"
$env:DB_USER = "postgres"
$credential = Get-Credential -UserName postgres -Message "Пароль PostgreSQL"
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
cd javafx-client
mvn javafx:run
```

Переменные окружения имеют приоритет над `src/main/resources/database.properties`. Пароль в исходном коде не хранится.

## Команды Maven

Из каталога `javafx-client`:

```powershell
mvn clean compile
mvn test
mvn javafx:run
mvn clean package
```

## Архитектура

```text
FXML / CSS → Controller → Service → DAO / JDBC → PostgreSQL
```

| Слой | Пакет / каталог | Ответственность |
|---|---|---|
| View | `resources/fxml`, `resources/css` | интерфейс и оформление |
| Controller | `controller` | действия пользователя и обновление UI |
| Service | `service` | валидация, переходы статуса, статистика |
| DAO | `dao`, `dao.jdbc` | SQL, `PreparedStatement`, `ResultSet` |
| Database | `database` | конфигурация и `Connection` |
| Model | `model` | клиент, заявка, enum и строка таблицы |

`AbstractJdbcDao` — абстрактный базовый класс с общим `ConnectionProvider` и преобразованием `SQLException` в понятную ошибку. `JdbcClientDao` и `JdbcServiceRequestDao` наследуются от него.

## Материалы для защиты

- [Краткие ответы по JavaFX, FXML и JDBC](../docs/defense-kr2.md)
- [Сценарий демонстрации КР №2](../docs/demo-kr2.md)
- [Схема базы данных](../docs/er-diagram.md)
