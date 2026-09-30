# Структура базы данных

Один клиент может иметь много заявок. Каждая заявка принадлежит одному клиенту.

```mermaid
erDiagram
    CLIENTS ||--o{ SERVICE_REQUESTS : "подаёт"
    CLIENTS {
        bigint id PK
        varchar full_name
        varchar phone UK
        varchar email UK
    }
    SERVICE_REQUESTS {
        bigint id PK
        bigint client_id FK
        varchar bike_brand
        varchar bike_model
        varchar bike_type
        varchar problem_description
        varchar status
        numeric estimated_cost
        timestamp created_at
        timestamp completed_at
    }
```

`ON DELETE RESTRICT` запрещает удалять клиента с заявками. `CHECK` ограничивает статусы, типы, стоимость и дату завершения. `UNIQUE` исключает повтор телефона и email. Email может быть NULL; телефон обязателен.
