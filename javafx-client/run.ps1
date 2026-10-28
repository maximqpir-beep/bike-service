$ErrorActionPreference = "Stop"
$clientDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Split-Path -Parent $clientDir
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
    throw "Файл .env не найден. Скопируйте .env.example в .env и задайте пароль."
}

$settings = @{}
Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $pair = $line.Split("=", 2)
        if ($pair.Count -eq 2) { $settings[$pair[0].Trim()] = $pair[1].Trim() }
    }
}

$env:DB_URL = "jdbc:postgresql://127.0.0.1:$($settings.POSTGRES_PORT)/$($settings.POSTGRES_DB)"
$env:DB_USER = $settings.POSTGRES_USER
$env:DB_PASSWORD = $settings.POSTGRES_PASSWORD

Push-Location $clientDir
try { mvn javafx:run } finally { Pop-Location }
