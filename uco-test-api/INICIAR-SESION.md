# Fase 2: login y permisos

La API utiliza sesiones de servidor con cookie `JSESSIONID` HttpOnly, contraseñas almacenadas como hash BCrypt y protección CSRF.
Las sesiones caducan tras 30 minutos de inactividad. Al desplegar con HTTPS, configurar `SESSION_COOKIE_SECURE=true`.
La pantalla visual de login está disponible en la raíz de la aplicación desde la fase 3.

## Crear los usuarios de demostración

PostgreSQL debe estar iniciado. Desde PowerShell, dentro de `uco-test-api`, ejecutar:

```powershell
$env:JAVA_HOME = 'C:\Users\Lenovo\.jdks\openjdk-27'
$mavenProyecto = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd'
$env:DEMO_USUARIOS = 'true'
$claveAna = Read-Host 'Contraseña para ana (12 a 64 caracteres)' -AsSecureString
$claveCarlos = Read-Host 'Contraseña para carlos (12 a 64 caracteres)' -AsSecureString
$env:DEMO_CLAVE_ANA = [System.Net.NetworkCredential]::new('', $claveAna).Password
$env:DEMO_CLAVE_CARLOS = [System.Net.NetworkCredential]::new('', $claveCarlos).Password
try {
    & $mavenProyecto spring-boot:run
} finally {
    Remove-Item Env:DEMO_USUARIOS, Env:DEMO_CLAVE_ANA, Env:DEMO_CLAVE_CARLOS -ErrorAction SilentlyContinue
}
```

`ana` corresponde a Ana Gómez y `carlos` a Carlos Mejía, los docentes de `data.sql`.
Los hashes quedan persistidos en la tabla `usuario`. El arranque no reemplaza las contraseñas de usuarios ya existentes.
La opción está desactivada por defecto y no hay contraseñas predefinidas de acceso.
En arranques posteriores basta con `spring-boot:run`; las credenciales creadas se conservan.
Las contraseñas admiten hasta 72 bytes UTF-8, además del límite de caracteres indicado.

## Login desde PowerShell

Con la API iniciada, abrir otra terminal:

```powershell
$apiNotas = 'http://localhost:8080/api/v1'
$csrf = Invoke-RestMethod "$apiNotas/auth/csrf" -SessionVariable sesionNotas
$credencial = Get-Credential -UserName ana -Message 'Credenciales del docente'
$datosLogin = @{
    usuario = $credencial.UserName
    contrasena = $credencial.GetNetworkCredential().Password
}
Invoke-RestMethod "$apiNotas/auth/login" -Method Post -WebSession $sesionNotas `
    -ContentType 'application/x-www-form-urlencoded' `
    -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -Body $datosLogin
$datosLogin.Clear()

# El login renueva la protección CSRF: obtener el token de la sesión autenticada.
$csrf = Invoke-RestMethod "$apiNotas/auth/csrf" -WebSession $sesionNotas
Invoke-RestMethod "$apiNotas/auth/me" -WebSession $sesionNotas
Invoke-RestMethod "$apiNotas/mis-grupos" -WebSession $sesionNotas
```

Para registrar una nota, enviar `idEstudiante`, `idNota` y `valor`; el servidor obtiene el docente de la sesión.
El campo antiguo `idDocente`, si llega en el cuerpo, se ignora para determinar la identidad.

```powershell
$registro = @{
    idEstudiante = '22222222-2222-2222-2222-222222222222'
    idNota = '55555555-5555-5555-5555-555555555555'
    valor = 4.2
} | ConvertTo-Json
Invoke-RestMethod "$apiNotas/notas-estudiantes" -Method Post -WebSession $sesionNotas `
    -ContentType 'application/json' -Headers @{ 'X-CSRF-TOKEN' = $csrf.token } -Body $registro

Invoke-RestMethod "$apiNotas/auth/logout" -Method Post -WebSession $sesionNotas `
    -Headers @{ 'X-CSRF-TOKEN' = $csrf.token }
```

El ejemplo modifica la base de datos: elegir un estudiante aún sin nota para esa evaluación.

## Contrato para la interfaz

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/v1/auth/csrf` | Obtener token y nombre de cabecera; accesible antes del login. |
| POST | `/api/v1/auth/login` | Formulario URL-encoded con `usuario` y `contrasena`, cookie y token CSRF. |
| GET | `/api/v1/auth/me` | Obtener identificador y nombre del docente autenticado. |
| GET | `/api/v1/mis-grupos` | Consultar exclusivamente los grupos del docente autenticado. |
| POST | `/api/v1/auth/logout` | Invalidar sesión; exige token CSRF. |

Las rutas existentes de docentes, grupos, evaluaciones y registro permanecen disponibles, pero requieren sesión y autorización.
Las peticiones del navegador deben incluir cookies (`credentials: 'include'` o `withCredentials: true`).
El origen permitido por defecto es `http://localhost:4200`, configurable mediante `CORS_ORIGENES`.
Obtener un token nuevo después del login. Enviar la cabecera indicada por `/auth/csrf` en las operaciones POST.

- HTTP 401: sesión ausente/caducada o credenciales incorrectas.
- HTTP 403: recurso ajeno o token CSRF ausente/inválido. Un POST sin sesión ni token puede responder 403 por la comprobación CSRF.
- HTTP 400: datos o reglas de negocio inválidos.
- HTTP 201: nota registrada.

## Pruebas

`SeguridadIntegracionTest` inicia la API real y hace peticiones HTTP con cookies.
Prueba acceso anónimo, contraseñas, login, permisos de ambos docentes, intento de suplantación, registro, duplicado, CSRF, logout y CORS.

```powershell
& $mavenProyecto test
& $mavenProyecto test '-Dtest.db.url=jdbc:postgresql://localhost:5433/ucotestapi'
```

El primer comando usa H2. El segundo crea un esquema PostgreSQL con nombre aleatorio para las pruebas y lo elimina al finalizar.
No utiliza las tablas del esquema de trabajo para guardar sus datos de prueba.
Si cambian las credenciales de PostgreSQL, proporcionar `test.db.user` y `test.db.password` mediante la configuración de pruebas del entorno.

No se incluyen autorregistro, recuperación de contraseña ni administración de usuarios. La interfaz visual se agregó en la fase 3.
