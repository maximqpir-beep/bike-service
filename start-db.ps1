$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
    Copy-Item (Join-Path $root ".env.example") $envFile
    throw "Создан файл .env. Замените change_this_password на свой пароль и запустите скрипт снова."
}

docker compose --env-file $envFile -f (Join-Path $root "docker-compose.yml") up -d --wait
Write-Host "PostgreSQL запущен. Данные сохраняются в Docker volume bike_service_data."
