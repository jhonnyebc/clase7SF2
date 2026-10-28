param([int]$Puerto = 8081)
# Inicia la API (uco-test-api). El frontend se inicia aparte: en uco-test-web ejecutar "npm start".
$ErrorActionPreference = 'Stop'
$jdkAula = Join-Path $env:USERPROFILE '.jdks\openjdk-27'
$mavenAula = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd'
if (!(Test-Path (Join-Path $jdkAula 'bin\java.exe'))) { throw 'No se encontro JDK 27. Configura jdkAula en iniciar.ps1.' }
if (!(Test-Path $mavenAula)) { throw 'No se encontro Maven. Configura mavenAula en iniciar.ps1.' }
$javaAnterior = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $jdkAula
    Push-Location (Join-Path $PSScriptRoot 'uco-test-api')
    Write-Host "La API estara disponible en http://localhost:$Puerto/api/v1 cuando termine el arranque."
    Write-Host 'PostgreSQL debe estar iniciado (docker compose up -d postgres). Para detener la API: Ctrl+C.'
    & $mavenAula spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Puerto"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar la API. Revisa el error anterior y el estado de PostgreSQL.' }
} finally {
    Pop-Location
    $env:JAVA_HOME = $javaAnterior
}
