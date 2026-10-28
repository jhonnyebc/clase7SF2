# Aula · Registro de notas

Funcionalidad **registrar nota de estudiante** con login, frontend y persistencia.

| Carpeta | Qué es | Tecnología |
| --- | --- | --- |
| `uco-test-api` | API REST con arquitectura hexagonal | Java 27, Spring Boot 4.1, PostgreSQL 18 |
| `uco-test-web` | Frontend | Angular 22 |
| Auth0 | Inicio de sesión de los docentes | Tenant `dev-x3tskzc0kemi5tlv.us.auth0.com` (plan gratuito) |

```
Angular (localhost:4200) ──login──▶ Auth0
        │  token JWT (Authorization: Bearer)
        ▼
API Spring Boot (localhost:8081) ──▶ PostgreSQL en Docker (localhost:5434)
```

## Cómo funciona el login

1. El docente pulsa **Iniciar sesión** en Angular y Auth0 le muestra su pantalla de login.
2. Auth0 devuelve a Angular un token para la API (`audience` = `https://uco-test-api`). La Action *Agregar correo al token* incluye el correo en el claim `https://uco-test-api/email`.
3. Angular envía el token en cada petición. La API valida firma, vencimiento, emisor y audiencia.
4. La API busca el docente cuyo `correo` coincide con el del token. Si no existe, responde 403.
5. El docente que registra la nota siempre sale del token: no se confía en un `idDocente` enviado por el cliente.

Los usuarios de Auth0 y los docentes de la base se relacionan por el correo:

| Docente | Correo (columna `docente.correo`) | Grupo |
| --- | --- | --- |
| Ana Gómez | `ana.gomez@ucotest.com` | Grupo 1 |
| Carlos Mejía | `carlos.mejia@ucotest.com` | Grupo 2 |

Para entrar como Ana, crea en Auth0 (*User Management → Users*) un usuario con ese correo. Si quieres usar otro correo, cámbialo en la tabla `docente`.

## Ejecutar en este equipo

Los puertos 5433 y 8080 los usa el proyecto FlowRide; por eso este proyecto usa **5434** y **8081**.

1. Base de datos, desde `uco-test-api`:

   ```powershell
   docker compose up -d postgres
   ```

2. API, desde esta carpeta (o `UcoTestApiApplication` en IntelliJ):

   ```powershell
   ./iniciar.ps1
   ```

3. Frontend, desde `uco-test-web` (la primera vez, `npm install`):

   ```powershell
   npm start
   ```

4. Abrir **http://localhost:4200** e iniciar sesión.

## Pruebas

```powershell
# Backend (H2 en memoria). Firma sus propios tokens con una clave RSA temporal.
cd uco-test-api
$env:JAVA_HOME = 'C:\Users\Lenovo\.jdks\openjdk-27'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' -B test

# Backend contra PostgreSQL real (crea y borra un esquema temporal)
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' -B test '-Dtest.db.url=jdbc:postgresql://localhost:5434/ucotestapi'

# Frontend
cd ..\uco-test-web
npm test -- --watch=false
```

## Alcance

1. Login y cierre de sesión con Auth0; cada docente solo ve y califica sus grupos.
2. Consulta de grupos, evaluaciones y estudiantes, con búsqueda y filtros.
3. Registro de nota de 0 a 5 con máximo un decimal. Reglas: estudiante existente, activo y del grupo; evaluación vigente; docente del grupo; una sola nota por estudiante y evaluación (409 si se repite, también con peticiones simultáneas).
4. Promedios ponderados por porcentaje de cada evaluación. Ver `uco-test-api/PROMEDIOS.md`.

Más detalle: `uco-test-api/INICIAR-SESION.md` (Auth0 y contrato de la API), `uco-test-api/INTERFAZ.md` (pantallas), `uco-test-api/CONCURRENCIA.md` (duplicados).
