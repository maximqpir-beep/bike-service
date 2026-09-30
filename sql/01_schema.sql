-- Выполнить внутри пустой базы bike_service. Повторный запуск не удаляет данные и завершится ошибкой.
BEGIN;
CREATE TABLE clients (
 id BIGSERIAL PRIMARY KEY,
 full_name VARCHAR(150) NOT NULL CHECK (TRIM(full_name) <> ''),
 phone VARCHAR(30) NOT NULL UNIQUE CHECK (TRIM(phone) <> ''),
 email VARCHAR(150) UNIQUE
);
CREATE TABLE service_requests (
 id BIGSERIAL PRIMARY KEY,
 client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
 bike_brand VARCHAR(100) NOT NULL CHECK (TRIM(bike_brand) <> ''),
 bike_model VARCHAR(100),
 bike_type VARCHAR(30) NOT NULL CHECK (bike_type IN ('ROAD','MOUNTAIN','CITY','BMX','ELECTRIC','OTHER')),
 problem_description VARCHAR(2000) NOT NULL CHECK (TRIM(problem_description) <> ''),
 status VARCHAR(30) NOT NULL CHECK (status IN ('CREATED','ACCEPTED','IN_PROGRESS','WAITING_FOR_PARTS','COMPLETED','CANCELLED')),
 estimated_cost NUMERIC(10,2) CHECK (estimated_cost >= 0),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 completed_at TIMESTAMP,
 CONSTRAINT completed_state CHECK (
  (status = 'COMPLETED' AND completed_at IS NOT NULL AND estimated_cost IS NOT NULL)
  OR (status <> 'COMPLETED' AND completed_at IS NULL)
 ),
 CONSTRAINT completed_date CHECK (completed_at IS NULL OR completed_at >= created_at)
);
CREATE INDEX idx_requests_client ON service_requests(client_id);
CREATE INDEX idx_requests_status ON service_requests(status);
COMMIT;
