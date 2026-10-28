# Login con Auth0 y permisos

La API no guarda usuarios ni contraseñas. El login lo hace **Auth0** desde Angular y la API solo valida el token JWT que llega en la cabecera `Authorization: Bearer <token>`. La API no usa sesiones ni cookies, así que no necesita CSRF.

## Configuración de Auth0

| Elemento | Valor |
| --- | --- |
| Tenant (Domain) | `dev-x3tskzc0kemi5tlv.us.auth0.com` |
| Aplicación | **UCO Test Web**, tipo *Single Page Application*, Client ID `6aPiRY6X0jsWQ9zeRfTq9tFizSuKBp8E` |
| URLs permitidas de la aplicación | Callback, Logout y Web Origins: `http://localhost:4200` |
| API | **UCO Test API**, Identifier (audience) `https://uco-test-api`, RS256 |
| Acceso de la aplicación a la API | *Application Access* → UCO Test Web con *User-delegated Access* autorizado |
| Action (Login / Post Login) | *Agregar correo al token*, conectada en *Triggers → post-login* |

Código de la Action:

```javascript
exports.onExecutePostLogin = async (event, api) => {
  api.accessToken.setCustomClaim('https://uco-test-api/email', event.user.email);
};
```

Ninguno de estos valores es secreto. El *Client Secret* no se usa: una aplicación Angular no puede guardarlo.

## Cómo identifica la API al docente

`SeguridadConfiguracion` valida cada token:

- firma con las claves públicas del tenant (`/.well-known/jwks.json`, se descargan con el primer token),
- vencimiento,
- emisor `https://dev-x3tskzc0kemi5tlv.us.auth0.com/`,
- audiencia `https://uco-test-api`.

`PermisosDocente` lee el correo del claim `https://uco-test-api/email` y busca el docente con ese correo exacto (ignora mayúsculas). Si el correo no corresponde a ningún docente, la API responde 403. El docente que registra la nota sale siempre del token.

Valores configurables en `application.yml`: `AUTH0_ISSUER`, `AUTH0_AUDIENCE` y `CORS_ORIGENES` (por defecto `http://localhost:4200`).

## Usuarios

Crear en Auth0 (*User Management → Users → Create User*, conexión *Username-Password-Authentication*) un usuario por docente, con el mismo correo de la tabla `docente`:

- `ana.gomez@ucotest.com` → Ana Gómez
- `carlos.mejia@ucotest.com` → Carlos Mejía

Para asociar otro correo a un docente:

```sql
UPDATE docente SET correo = 'nuevo@correo.com' WHERE id = '11111111-1111-1111-1111-111111111111';
```

## Contrato de la API

Todas las rutas exigen token. Base: `http://localhost:8081/api/v1`.

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/auth/me` | Docente del token (id, nombres, apellidos, correo). |
| GET | `/mis-grupos` | Grupos del docente del token. |
| GET | `/grupos/{idGrupo}/notas` | Evaluaciones del grupo, con porcentaje y vigencia. |
| GET | `/grupos/{idGrupo}/ponderacion` | Suma de porcentajes y si es válida. |
| GET | `/grupos/{idGrupo}/promedios` | Promedios ponderados por estudiante. |
| GET | `/notas/{idNota}/estudiantes` | Estudiantes del grupo con su nota en esa evaluación. |
| POST | `/notas-estudiantes` | Registra `{ idEstudiante, idNota, valor }`. |

Respuestas de error:

- **401**: sin token, token vencido, de otro tenant, de otra API o con firma inválida.
- **403**: token válido de alguien que no es docente, o recurso de otro docente.
- **400**: datos o reglas de negocio inválidos.
- **409**: la nota ya estaba registrada.
- **201**: nota registrada.

## Pruebas

`SeguridadIntegracionTest` levanta la API real y le hace peticiones HTTP. En vez de Auth0, la prueba genera una clave RSA temporal, firma sus propios tokens y le pasa a la API la clave pública (`app.auth.clave-publica`). Cubre tokens inválidos (emisor, audiencia, vencido, firma ajena, texto basura), correo no asociado, permisos entre docentes, registro, duplicados, concurrencia, reinicio y CORS.
