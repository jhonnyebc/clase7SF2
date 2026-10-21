# Fase 5: protección contra notas duplicadas

Implementada el 21 de octubre de 2026.

## Comportamiento

La base de datos permite una sola nota por matrícula y evaluación. Si llegan dos registros simultáneos, uno se guarda y el otro recibe HTTP 409. La nota guardada no se reemplaza. El formulario muestra el mensaje del servidor: cerrar y pulsar Actualizar para consultar el valor existente.

La comprobación previa también devuelve 409. Las demás validaciones conservan HTTP 400. Solo se traduce la violación de la restricción de unicidad esperada; otros errores de integridad no se presentan como notas duplicadas.

## Instalación en bases existentes

Al reiniciar, schema.sql crea el índice único de forma repetible. La entidad JPA declara la misma unicidad para bases nuevas. Si existen duplicados históricos, la creación falla y se detiene el arranque, sin borrar ni elegir notas automáticamente.

Consulta de diagnóstico (solo lectura):

```sql
SELECT estudiante_grupo_id, nota_id, COUNT(*)
FROM nota_estudiante
GROUP BY estudiante_grupo_id, nota_id
HAVING COUNT(*) > 1;
```

## Verificación

23 pruebas aprobadas con H2 y PostgreSQL: las 20 anteriores más solicitudes HTTP simultáneas, inserciones concurrentes directas por repositorio y aplicación del índice sobre una tabla existente con y sin duplicados. La prueba HTTP comprueba respuestas 201/409, una sola fila y conservación del valor ganador. La prueba del repositorio omite la consulta previa para comprobar la protección real de la base de datos y la traducción del error.

Verificación PostgreSQL completada el 21 de octubre de 2026: 23 pruebas, cero fallos y cero errores (BUILD SUCCESS). La conexión volvió a funcionar con las credenciales originales; no se determinó la causa del rechazo anterior ni se cambiaron contraseñas.

## Continuación

Para repetir las pruebas, ejecutar desde uco-test-api:

```powershell
$env:JAVA_HOME = 'C:\Users\Lenovo\.jdks\openjdk-27'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' -B test '-Dtest.db.url=jdbc:postgresql://localhost:5433/ucotestapi'
```

Las pruebas usan un esquema temporal independiente, que se elimina al terminar. Después se puede reiniciar la aplicación con las instrucciones de PROMEDIOS.md. No se han cambiado credenciales ni eliminado datos existentes.

