# Fase 3: interfaz local de Aula

La interfaz se sirve desde Spring Boot. No necesita instalar Node ni ejecutar un frontend separado.

## Abrir la aplicación

1. Iniciar Docker Desktop y ejecutar `docker compose up -d postgres` en `uco-test-api`.
2. Desde la carpeta raíz `clase7SF2`, ejecutar `./iniciar.ps1` en PowerShell. También puede iniciarse `UcoTestApiApplication` desde IntelliJ.
3. Abrir `http://localhost:8080` e iniciar sesión.

El script usa las rutas de JDK 27 e IntelliJ de este equipo. Admite otro puerto: `./iniciar.ps1 -Puerto 18080`.
Las cuentas de demostración `ana` y `carlos` fueron creadas en PostgreSQL durante la verificación. Sus contraseñas aleatorias se entregan por separado y no forman parte del código fuente.
Para crear usuarios en una base nueva, seguir `INICIAR-SESION.md`.

## Recorrido implementado

- Login y cierre de sesión conectados con la API; recuperación de sesión al recargar.
- Mis grupos, lista de evaluaciones y estudiantes de cada evaluación.
- Búsqueda por nombre o identificación, ignorando tildes y mayúsculas.
- Filtros por pendientes, registradas e inactivos.
- Cantidades de estudiantes y notas registradas/pendientes.
- Evaluaciones vencidas y estudiantes inactivos con una explicación visible.
- Formulario de nota con validación de 0 a 5 y máximo un decimal; admite coma o punto.
- Botón bloqueado durante el guardado, confirmación de éxito y conservación del valor ante errores.
- Diseño adaptable, etiquetas de formulario, diálogo accesible y acceso por teclado.
- Manejo de errores de conexión y regreso al login cuando termina la sesión.

El formulario de registro adelanta parte de la fase 4. La edición de notas registradas no está incluida.

## Porcentajes

Se agregó `porcentaje` a las evaluaciones, al mapeo de persistencia y a las respuestas de la API.
La ruta protegida `GET /api/v1/grupos/{idGrupo}/ponderacion` indica total, número de evaluaciones sin porcentaje, validez y mensaje.
Una configuración válida requiere evaluaciones con porcentaje mayor que 0 y menor o igual a 100, sumando exactamente 100.
Se emplea BigDecimal para validar los decimales.

Los datos de demostración inicializan únicamente porcentajes ausentes: parcial del Grupo 1 al 70 %, quiz al 30 % y la evaluación del Grupo 2 al 100 %.
No se reemplazan porcentajes ya configurados. Las evaluaciones vencidas también forman parte del total.

La interfaz permite consultar los porcentajes; no editarlos. El cálculo y la presentación del promedio ponderado individual están implementados en la fase 4. Consultar `PROMEDIOS.md` para uso y arranque sin scripts.

## Verificación histórica de fase 3

Actualización de fase 4: 20 pruebas aprobadas con PostgreSQL el 20 de octubre de 2026. La revisión visual del 21 de octubre en H2 temporal confirmó registro de 4,2 al 70 %, acumulado 2,94, definitiva pendiente y desglose. No se modificaron notas reales para esa revisión.

La suite incluye 12 pruebas: 8 de integración HTTP y 4 de reglas de ponderación. Se verifican también los recursos públicos de la interfaz y la protección de sus datos.
La suite pasó con PostgreSQL en un esquema temporal independiente. Los datos de prueba se eliminan al cerrar la suite.
El JavaScript pasó la comprobación de sintaxis `node --check`.

Revisión del 20 de octubre de 2026: las 12 pruebas también pasaron con H2. En el navegador se verificaron login, navegación, porcentajes, búsqueda con tildes, rechazo de 5,5, guardado de 4,2, consulta tras recarga y logout. El registro visual se realizó en una base H2 temporal, sin modificar notas en PostgreSQL.
Tras reiniciar PostgreSQL se volvió a verificar el arranque normal, el login de Ana, sus grupos, la ponderación válida de 100 % y el cierre de sesión por HTTP.
