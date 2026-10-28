$root = Split-Path -Parent $MyInvocation.MyCommand.Path
docker compose --env-file (Join-Path $root ".env") -f (Join-Path $root "docker-compose.yml") down
