# Registro de notas

Aplicación Java con Spring Boot y PostgreSQL. El módulo ejecutable es `uco-test-api`.

## Fase 4: promedios ponderados disponibles

Dentro de cada grupo, «Ver promedios» muestra el acumulado, la definitiva y el detalle por evaluación. Consultar `uco-test-api/PROMEDIOS.md` para el cálculo y el arranque sin scripts. Verificado con 20 pruebas en PostgreSQL y revisión visual en H2 temporal.

La aplicación ahora incluye pantallas de login, grupos, evaluaciones y estudiantes, además del formulario de registro de notas.
Con PostgreSQL iniciado, ejecutar `./iniciar.ps1` desde esta carpeta y abrir `http://localhost:8080`.
Consultar `uco-test-api/INTERFAZ.md` para el recorrido, los porcentajes y el alcance pendiente.

## Fase 2: autenticación y permisos implementados

Consultar `uco-test-api/INICIAR-SESION.md` para crear usuarios de demostración e iniciar/cerrar sesión.
La API ahora exige autenticación y limita las consultas y el registro a los grupos del docente conectado.
El docente que registra se obtiene de la sesión; no se confía en `idDocente` enviado por el cliente.
Se incorporaron pruebas de integración HTTP con H2 y PostgreSQL.
Los comandos sin sesión de la sección histórica de fase 1 deben ejecutarse ahora con cookie y token CSRF según esa guía.

## Verificación histórica de fase 1 — 19 de octubre de 2026

- JDK 27 instalado y ejecutable.
- Maven 3.9.16 incluido en IntelliJ IDEA disponible; `mvn` no está en el PATH de la terminal.
- `mvn test`: BUILD SUCCESS. No hay pruebas automatizadas en `src/test`; este resultado no significa que exista una suite de pruebas.
- Compilación independiente de todas las fuentes Java completada con JDK 27 y conservación de nombres de parámetros (`-parameters`).
- API arrancada y probada por HTTP contra una base H2 aislada en archivo: 16 comprobaciones satisfactorias, incluido guardar una nota y consultarla después de reiniciar la aplicación.
- PostgreSQL verificado: después de que el usuario inició el contenedor, la API arrancó con la configuración normal de base de datos (puerto HTTP de prueba 18080).
- Se repitieron las 16 comprobaciones con PostgreSQL 18.6: consultas, validaciones, registro, rechazo de duplicado y persistencia después de reiniciar la aplicación.
- Las pruebas PostgreSQL utilizaron entidades con UUID nuevos. Al terminar se retiraron en una transacción las 16 filas temporales creadas por la prueba.
- Docker no puede administrarse desde el entorno del asistente, pero la conexión JDBC a PostgreSQL funciona.

La fase 1 está completada: entorno, arranque, consultas, registro y persistencia comprobados con PostgreSQL.
No se modificó la lógica de negocio ni la configuración de producción durante esta verificación.

## Ejecutar en este equipo

Abrir PowerShell en `uco-test-api`. Docker Desktop debe estar iniciado y disponible para la terminal.

```powershell
$env:JAVA_HOME = 'C:\Users\Lenovo\.jdks\openjdk-27'
$mavenProyecto = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd'

docker compose up -d postgres
docker compose ps
docker compose exec -T postgres pg_isready -U ucotest -d ucotestapi

& $mavenProyecto -B test
& $mavenProyecto spring-boot:run
```

La API utiliza el puerto 8080 y PostgreSQL el puerto local 5433. Las variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` permiten cambiar la conexión.
Si `pg_isready` todavía no indica que acepta conexiones, repetir la comprobación antes de iniciar la API.
En otro equipo, usar su ruta de JDK 27 y Maven.

## Comprobar el flujo con PostgreSQL

En otra terminal, con la API iniciada:

```powershell
$apiNotas = 'http://localhost:8080/api/v1'
$docenteDemo = '11111111-1111-1111-1111-111111111111'
$grupoDemo = '33333333-3333-3333-3333-333333333333'
$evaluacionDemo = '55555555-5555-5555-5555-555555555555'

Invoke-RestMethod "$apiNotas/docentes/$docenteDemo/grupos"
Invoke-RestMethod "$apiNotas/grupos/$grupoDemo/notas"
Invoke-RestMethod "$apiNotas/notas/$evaluacionDemo/estudiantes"
```

Para crear una nota de demostración, seleccionar un estudiante activo que aún no tenga nota.
El siguiente ejemplo registra 4,2 para Juan y modifica la base de datos:

```powershell
$registroDemo = @{
    idEstudiante = '22222222-2222-2222-2222-222222222222'
    idNota = $evaluacionDemo
    idDocente = $docenteDemo
    valor = 4.2
} | ConvertTo-Json

Invoke-RestMethod "$apiNotas/notas-estudiantes" -Method Post -ContentType 'application/json' -Body $registroDemo
Invoke-RestMethod "$apiNotas/notas/$evaluacionDemo/estudiantes"
```

Se espera una creación exitosa y que la consulta incluya el valor 4,2. Reiniciar la API y consultar nuevamente para verificar persistencia.
Repetir el POST devuelve HTTP 409 indicando que la nota ya está registrada (actualizado en fase 5).
No eliminar datos existentes para repetir la demostración.

## Alcance de la entrega

1. Login y cierre de sesión de docentes; permisos basados en el usuario autenticado.
2. Consulta de grupos propios, evaluaciones y estudiantes.
3. Registro de nota entre 0 y 5 con máximo un decimal.
4. Mensajes claros, estados de carga, búsqueda y distinción entre pendientes y registradas.
5. Promedios ponderados por estudiante dentro del grupo, con un porcentaje por evaluación.

### Promedios ponderados

Elección confirmada por el usuario: cada evaluación tiene un porcentaje.
El cálculo individual del promedio ponderado y su desglose están implementados en la fase 4.

Reglas implementadas:

- Cada evaluación tiene un porcentaje mayor que 0 y menor o igual a 100.
- Las evaluaciones de un grupo deben sumar 100 % antes de habilitar el cálculo definitivo.
- Acumulado ponderado: suma de `nota * porcentaje / 100`.
- Si faltan notas, mostrar el acumulado como parcial, el porcentaje evaluado y las evaluaciones pendientes. No presentar ese acumulado como nota definitiva.
- Con todas las notas registradas y los porcentajes completos, el acumulado es la nota definitiva.
- Sin notas registradas: mostrar “Sin notas”.
- Calcular con precisión decimal y redondear al presentar, no en cada contribución.
- Las evaluaciones vencidas siguen formando parte del cálculo; la vigencia solo controla el registro.
- Propuesta de alcance: porcentajes precargados y visibles para la entrega inicial. La edición de porcentajes necesita reglas adicionales si ya hay notas registradas.

Ejemplo: 4,0 al 30 %, 3,5 al 30 % y 4,5 al 40 % producen 4,05. Con solo la primera evaluación registrada, mostrar “Acumulado parcial: 1,20; evaluado: 30 %”.

## Fases restantes

| Día | Objetivo |
| --- | --- |
| 1 | Completado: compilación, arranque y 16 comprobaciones tanto en H2 como en PostgreSQL, incluida persistencia después del reinicio. |
| 2 | Implementado: login, cierre de sesión, hash de contraseñas, protección CSRF y permisos por docente en consultas y registro. |
| 3 | Implementado: interfaz de navegación, porcentaje por evaluación y validación del total por grupo. |
| 4 | Completado: cálculo ponderado, acumulado parcial, definitiva y desglose; 20 pruebas aprobadas y revisión visual completada. |
| 5 | Completado: restricción de unicidad y respuesta 409 ante duplicados; 23 pruebas aprobadas tanto en H2 como en PostgreSQL. Ver `uco-test-api/CONCURRENCIA.md`. |
| 6 | Pruebas de negocio, permisos, persistencia, concurrencia y cálculo ponderado. |
| 7 | Correcciones, documentación y ensayo de entrega. |

## Límites de la comprobación aislada

Se utilizó el H2 ya declarado como dependencia de prueba, con base en archivo independiente y puerto HTTP asignado automáticamente.
Se cargaron los datos de ejemplo sin la cláusula PostgreSQL `ON CONFLICT (id) DO NOTHING`, únicamente en esa base nueva.
La configuración normal y `data.sql` del proyecto permanecieron sin cambios.
La prueba H2 por sí sola no certificaba PostgreSQL; posteriormente se repitió satisfactoriamente el flujo con PostgreSQL real.
La autenticación y los permisos se incorporaron en la fase 2. La prevención de duplicados concurrentes está implementada en la fase 5 y verificada con PostgreSQL.
