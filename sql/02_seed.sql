-- Вымышленные учебные данные. Запустить один раз после 01_schema.sql.
BEGIN;
INSERT INTO clients (full_name,phone,email) VALUES
('Иван Петров','+79000000001','ivan@example.com'),
('Анна Смирнова','+79000000002','anna@example.com'),
('Алексей Иванов','+79000000003','alex@example.com'),
('Мария Кузнецова','+79000000004','maria@example.com'),
('Дмитрий Соколов','+79000000005','dmitry@example.com');
INSERT INTO service_requests (client_id,bike_brand,bike_model,bike_type,problem_description,status,estimated_cost,created_at,completed_at)
SELECT c.id,v.brand,v.model,v.kind,v.problem,v.status,v.cost,v.created_at::timestamp,v.completed_at::timestamp
FROM (VALUES
('+79000000001','Trek','Marlin 7','MOUNTAIN','Не переключается задняя передача','CREATED',NULL::numeric,'2026-09-01 10:00',NULL),
('+79000000002','Cube','Aim Pro','MOUNTAIN','Скрип тормозов','ACCEPTED',1500.00,'2026-09-02 10:00',NULL),
('+79000000003','Giant','Talon 3','MOUNTAIN','Настройка переключателя','IN_PROGRESS',2200.00,'2026-09-03 10:00',NULL),
('+79000000004','Merida','Big Nine','MOUNTAIN','Замена кассеты','WAITING_FOR_PARTS',5000.00,'2026-09-04 10:00',NULL),
('+79000000005','Stels','Navigator','CITY','Прокол камеры','COMPLETED',800.00,'2026-09-05 10:00','2026-09-05 12:00'),
('+79000000001','Format','1412','MOUNTAIN','Обслуживание вилки','CANCELLED',3500.00,'2026-09-06 10:00',NULL),
('+79000000002','Forward','Apache','MOUNTAIN','Скрип каретки','IN_PROGRESS',1800.00,'2026-09-07 10:00',NULL),
('+79000000003','Scott','Speedster','ROAD','Замена тормозных колодок','COMPLETED',2500.00,'2026-09-08 10:00','2026-09-09 15:00'),
('+79000000004','GT','Slammer','BMX','Люфт рулевой колонки','CREATED',NULL,'2026-09-09 10:00',NULL),
('+79000000005','Eltreco','XT 750','ELECTRIC','Диагностика электропривода','ACCEPTED',3000.00,'2026-09-10 10:00',NULL),
('+79000000001','Trek','FX 2','CITY','Сезонное обслуживание','COMPLETED',4200.00,'2026-09-11 10:00','2026-09-12 14:00'),
('+79000000002','Author','Compact','OTHER','Замена цепи','WAITING_FOR_PARTS',1900.00,'2026-09-12 10:00',NULL)
) AS v(phone,brand,model,kind,problem,status,cost,created_at,completed_at)
JOIN clients c ON c.phone=v.phone;
COMMIT;
