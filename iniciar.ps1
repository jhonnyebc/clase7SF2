param([int]$Puerto = 8080)
$ErrorActionPreference = 'Stop'
$jdkAula = Join-Path $env:USERPROFILE '.jdks\openjdk-27'
$mavenAula = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd'
if (!(Test-Path (Join-Path $jdkAula 'bin\java.exe'))) { throw 'No se encontro JDK 27. Configura jdkAula en iniciar.ps1.' }
if (!(Test-Path $mavenAula)) { throw 'No se encontro Maven. Configura mavenAula en iniciar.ps1.' }
$javaAnterior = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $jdkAula
    Push-Location (Join-Path $PSScriptRoot 'uco-test-api')
    Write-Host "Aula estara disponible en http://localhost:$Puerto cuando termine el arranque."
    Write-Host 'PostgreSQL debe estar iniciado. Para detener Aula: Ctrl+C.'
    & $mavenAula spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Puerto --server.address=127.0.0.1"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar Aula. Revisa el error anterior y el estado de PostgreSQL.' }
} finally {
    Pop-Location
    $env:JAVA_HOME = $javaAnterior
}
