# Interfaz: Angular (uco-test-web)

El frontend es un proyecto Angular 22 aparte, en la carpeta `uco-test-web`. Usa el SDK oficial `@auth0/auth0-angular` para el login.

## Abrir la aplicación

1. PostgreSQL y la API iniciados (ver `README.md` en la carpeta raíz).
2. En `uco-test-web`: `npm install` (solo la primera vez) y `npm start`.
3. Abrir `http://localhost:4200` y pulsar **Iniciar sesión**.

## Estructura

| Archivo | Para qué |
| --- | --- |
| `src/app/nucleo/configuracion.ts` | URL de la API y datos públicos de Auth0. |
| `src/app/app.config.ts` | Auth0 (`provideAuth0`) y el interceptor que agrega el token solo a las peticiones a la API. |
| `src/app/nucleo/api.ts` | Llamadas a la API y mensajes de error. |
| `src/app/nucleo/sesion.ts` | Docente conectado, sus grupos y cierre de sesión. |
| `src/app/nucleo/sesion-guard.ts` | Sin sesión, redirige a `/login`. |
| `src/app/paginas/*` | Pantallas: login, grupos, evaluaciones, estudiantes (registro de nota) y promedios. |
| `src/app/compartido/formato.ts` | Formato de números y fechas, búsqueda sin tildes y validación de la nota. |

## Recorrido

- **Login**: redirige a la pantalla de Auth0 y vuelve a la aplicación. La sesión se conserva al recargar.
- **Mis grupos**: solo los grupos del docente conectado.
- **Evaluaciones del grupo**: porcentaje, vigencia y aviso si los porcentajes no suman 100 %.
- **Estudiantes de una evaluación**: búsqueda sin tildes ni mayúsculas, filtros (pendientes, registradas, inactivos) y botón **Registrar nota** solo para estudiantes activos, sin nota y con la evaluación vigente.
- **Registrar nota**: de 0 a 5 con máximo un decimal (coma o punto). El botón se bloquea mientras guarda y el mensaje del servidor aparece si algo falla (por ejemplo, 409 si otra persona ya la registró).
- **Promedios**: acumulado, definitiva y detalle por evaluación.
- **Cerrar sesión**: cierra la sesión en Auth0 y vuelve al login.

Las notas registradas no se pueden editar desde la interfaz, y los porcentajes solo se consultan.

## Pruebas

`npm test -- --watch=false` ejecuta las pruebas de `formato.spec.ts` (validación de la nota, formato y búsqueda).
