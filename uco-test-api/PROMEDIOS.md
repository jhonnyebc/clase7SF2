# Fase 4: promedios ponderados

Abrir un grupo y seleccionar **Ver promedios**. **Ver cálculo** muestra los porcentajes, notas y aportes por evaluación.

- Sin notas: no hay calificaciones; no equivale a cero.
- Parcial: muestra acumulado y porcentaje evaluado; la definitiva queda pendiente.
- Definitiva: todas las evaluaciones tienen nota y los porcentajes son válidos.
- Sin cálculo: porcentajes ausentes, fuera de rango o total distinto de 100 %.

Aporte = nota × porcentaje / 100. Los aportes se suman con precisión decimal antes de redondear el total a dos decimales. Una nota cero sí cuenta como registrada. Las evaluaciones vencidas siguen formando parte del cálculo.

Ejemplo: 4,2 al 70 % aporta 2,94. Con la otra evaluación pendiente es un acumulado parcial. Si la otra nota es 3,0 al 30 %, la definitiva es 3,84.

Los porcentajes se consultan; su edición desde la interfaz no está incluida.

## Verificación

20 pruebas aprobadas con PostgreSQL el 20 de octubre de 2026: 9 de integración HTTP, 4 de ponderación y 7 de promedio. Cubren permisos, pendientes, cero, porcentajes inválidos y precisión.

Revisión visual del 21 de octubre en H2 temporal: login, registro de 4,2, acumulado 2,94 al 70 %, estudiantes sin notas y desglose con evaluación pendiente fuera de plazo. No se modificaron notas reales de PostgreSQL para esta revisión.

API protegida: GET /api/v1/grupos/{idGrupo}/promedios.

## Ver los cambios

Detener la aplicación con Ctrl+C en su terminal. Con PostgreSQL iniciado, ejecutar estos comandos directamente, sin iniciar.ps1:

```powershell
cd C:\Users\Lenovo\IdeaProjects\clase7SF2\uco-test-api
$env:JAVA_HOME = 'C:\Users\Lenovo\.jdks\openjdk-27'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' spring-boot:run
```

Abrir http://localhost:8080 y actualizar con Ctrl+F5. Usar las credenciales entregadas anteriormente.

## Próxima fase

Fase 5: protección en base de datos contra registros duplicados simultáneos y tratamiento claro del conflicto. La búsqueda, los estados y los mensajes principales ya están implementados.

