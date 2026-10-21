package uco.test.api;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import uco.test.api.inicializador.UcoTestApiApplication;
import uco.test.api.infraestructura.output.repositorios.estructurados.*;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.UsuarioEntidadJPA;

class SeguridadIntegracionTest {
    static ConfigurableApplicationContext context;
    static String base;
    static String schema;
    static final String pgUrl = System.getProperty("test.db.url");
    static final String ana = "11111111-1111-1111-1111-111111111111";
    static final String carlos = "11111111-1111-1111-1111-111111111112";
    static final String grupoAna = "33333333-3333-3333-3333-333333333333";
    static final String grupoCarlos = "33333333-3333-3333-3333-333333333334";
    static final String notaAna = "55555555-5555-5555-5555-555555555555";
    static final String notaCarlos = "55555555-5555-5555-5555-555555555557";
    static final String clave = "PruebaTemporal-2026";
    HttpClient client;

    static Connection postgres() throws SQLException {
        return DriverManager.getConnection(pgUrl,
            System.getProperty("test.db.user", "ucotest"), System.getProperty("test.db.password", "ucotest"));
    }

    @BeforeAll
    static void preparar() throws Exception {
        String url = "jdbc:h2:mem:seguridad;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        String driver = "org.h2.Driver", user = "sa", password = "";
        if (pgUrl != null) {
            schema = "test_seguridad_" + UUID.randomUUID().toString().replace("-", "");
            try (var connection = postgres(); var st = connection.createStatement()) {
                st.execute("CREATE SCHEMA " + schema);
            }
            url = pgUrl + (pgUrl.contains("?") ? "&" : "?") + "currentSchema=" + schema;
            driver = "org.postgresql.Driver";
            user = System.getProperty("test.db.user", "ucotest");
            password = System.getProperty("test.db.password", "ucotest");
        }
        context = SpringApplication.run(UcoTestApiApplication.class,
            "--server.port=0", "--server.address=127.0.0.1",
            "--spring.datasource.url=" + url, "--spring.datasource.driver-class-name=" + driver,
            "--spring.datasource.username=" + user, "--spring.datasource.password=" + password,
            "--spring.jpa.hibernate.ddl-auto=create-drop", "--spring.sql.init.mode=never",
            "--app.demo.usuarios=false", "--logging.level.root=ERROR", "--spring.main.banner-mode=off");
        base = "http://127.0.0.1:" + ((WebServerApplicationContext)context).getWebServer().getPort() + "/api/v1";
        try (var input = SeguridadIntegracionTest.class.getResourceAsStream("/data.sql");
             var connection = context.getBean(DataSource.class).getConnection(); var st = connection.createStatement()) {
            var sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).replace("ON CONFLICT (id) DO NOTHING", "");
            for (String part : sql.split(";")) if (!part.isBlank()) st.execute(part);
        }
        var usuarios = context.getBean(UsuarioRepositorioJPA.class);
        var docentes = context.getBean(DocenteRepositorioJPA.class);
        var encoder = context.getBean(PasswordEncoder.class);
        usuarios.save(new UsuarioEntidadJPA("ana", encoder.encode(clave), docentes.findById(UUID.fromString(ana)).orElseThrow()));
        usuarios.save(new UsuarioEntidadJPA("carlos", encoder.encode(clave), docentes.findById(UUID.fromString(carlos)).orElseThrow()));
    }

    @AfterAll
    static void cerrar() throws Exception {
        if (context != null) context.close();
        if (schema != null) {
            try (var connection = postgres(); var st = connection.createStatement()) {
                st.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }

    @BeforeEach
    void cliente() {
        client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
    }

    HttpResponse<String> get(String ruta) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    String csrf() throws Exception {
        var response = get("/auth/csrf");
        assertEquals(200, response.statusCode());
        var matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());
        assertTrue(matcher.find(), response.body());
        return matcher.group(1);
    }
    HttpResponse<String> post(String ruta, String contenido, String tipo, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(base + ruta)).header("Content-Type", tipo)
            .POST(HttpRequest.BodyPublishers.ofString(contenido));
        if (token != null) request.header("X-CSRF-TOKEN", token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    void login(String usuario) throws Exception {
        var result = post("/auth/login", "usuario=" + usuario + "&contrasena=" + clave,
            "application/x-www-form-urlencoded", csrf());
        assertEquals(200, result.statusCode(), result.body());
    }
    String nota(String evaluacion, String estudiante, String docente) {
        return "{\"idNota\":\"" + evaluacion + "\",\"idEstudiante\":\"" + estudiante
            + "\",\"valor\":4.2" + (docente == null ? "" : ",\"idDocente\":\"" + docente + "\"") + "}";
    }

    @Test void requiereSesion() throws Exception {
        for (var ruta : List.of("/mis-grupos", "/auth/me", "/docentes/"+ana+"/grupos",
                "/grupos/"+grupoAna+"/notas", "/grupos/"+grupoAna+"/ponderacion", "/grupos/"+grupoAna+"/promedios", "/notas/"+notaAna+"/estudiantes"))
            assertEquals(401, get(ruta).statusCode(), ruta);
        assertEquals(401, post("/notas-estudiantes", "{}", "application/json", csrf()).statusCode());
    }

    @Test void credencialesYHash() throws Exception {
        var hash = context.getBean(UsuarioRepositorioJPA.class).findByUsuario("ana").orElseThrow().getClaveHash();
        assertNotEquals(clave, hash);
        assertTrue(context.getBean(PasswordEncoder.class).matches(clave, hash));
        assertEquals(401, post("/auth/login", "usuario=ana&contrasena=incorrecta",
            "application/x-www-form-urlencoded", csrf()).statusCode());
        assertEquals(401, get("/auth/me").statusCode());
        assertEquals(403, post("/auth/login", "usuario=ana&contrasena="+clave,
            "application/x-www-form-urlencoded", null).statusCode());
        login("ana");
        assertTrue(get("/auth/me").body().contains(ana));
    }

    @Test void permisosDeConsultas() throws Exception {
        login("ana");
        var grupos = get("/mis-grupos");
        assertEquals(200, grupos.statusCode());
        assertTrue(grupos.body().contains(grupoAna));
        assertFalse(grupos.body().contains(grupoCarlos));
        assertEquals(200, get("/grupos/"+grupoAna+"/notas").statusCode());
        assertEquals(200, get("/notas/"+notaAna+"/estudiantes").statusCode());
        assertEquals(403, get("/docentes/"+carlos+"/grupos").statusCode());
        assertEquals(403, get("/grupos/"+grupoCarlos+"/notas").statusCode());
        assertEquals(403, get("/notas/"+notaCarlos+"/estudiantes").statusCode());
        assertEquals(403, get("/grupos/"+UUID.randomUUID()+"/notas").statusCode());
        assertEquals(403, get("/grupos/"+grupoCarlos+"/ponderacion").statusCode());
        assertEquals(403, get("/grupos/"+grupoCarlos+"/promedios").statusCode());
        var ponderacion = get("/grupos/"+grupoAna+"/ponderacion");
        assertEquals(200, ponderacion.statusCode());
        assertTrue(ponderacion.body().contains("\"valida\":true"), ponderacion.body());
        assertTrue(get("/grupos/"+grupoAna+"/notas").body().contains("\"porcentaje\":70"), "Debe exponer porcentajes persistidos");
    }

    @Test void registroUsaIdentidadDeSesion() throws Exception {
        login("ana");
        String token = csrf();
        assertEquals(400, post("/notas-estudiantes", "{}", "application/json", token).statusCode());
        var ajena = nota(notaCarlos, "22222222-2222-2222-2222-222222222223", carlos);
        assertEquals(403, post("/notas-estudiantes", ajena, "application/json", token).statusCode());
        var propia = nota(notaAna, "22222222-2222-2222-2222-222222222222", carlos);
        assertEquals(403, post("/notas-estudiantes", propia, "application/json", null).statusCode());
        // El identificador falso de Carlos no cambia la identidad autenticada de Ana.
        var response = post("/notas-estudiantes", propia, "application/json", token);
        assertEquals(201, response.statusCode(), response.body());
        assertTrue(get("/notas/"+notaAna+"/estudiantes").body().contains("\"valor\":4.2"));
        assertEquals(409, post("/notas-estudiantes", propia, "application/json", token).statusCode());
        var sinDocente = nota(notaAna, "22222222-2222-2222-2222-222222222225", null);
        assertEquals(201, post("/notas-estudiantes", sinDocente, "application/json", token).statusCode());
    }

    @Test void carlosSoloVeSuGrupo() throws Exception {
        login("carlos");
        assertTrue(get("/auth/me").body().contains(carlos));
        assertTrue(get("/mis-grupos").body().contains(grupoCarlos));
        assertEquals(403, get("/grupos/"+grupoAna+"/notas").statusCode());
        assertEquals(403, get("/notas/"+notaAna+"/estudiantes").statusCode());
        assertEquals(403, get("/grupos/"+grupoAna+"/promedios").statusCode());
        var promedios = get("/grupos/"+grupoCarlos+"/promedios");
        assertEquals(200, promedios.statusCode());
        assertTrue(promedios.body().contains("Laura"));
        assertFalse(promedios.body().contains("Juan"));
    }

    tools.jackson.databind.JsonNode promedioDe(String id) throws Exception {
        var response = get("/grupos/"+grupoAna+"/promedios");
        assertEquals(200, response.statusCode(), response.body());
        var root = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
        for (var student : root.path("estudiantes")) if (student.path("idEstudiante").asText().equals(id)) return student;
        throw new AssertionError("Estudiante no encontrado");
    }

    @Test void promedioSeActualizaTrasRegistrarYCuentaEvaluacionesVencidas() throws Exception {
        String estudiante=UUID.randomUUID().toString(), matricula=UUID.randomUUID().toString();
        String quiz="55555555-5555-5555-5555-555555555556";
        try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement()) {
            st.executeUpdate("INSERT INTO estudiante(id,identificacion,nombres,apellidos,activo) VALUES ('"+estudiante+"','TEST-PROMEDIO','Prueba','Promedio',true)");
            st.executeUpdate("INSERT INTO estudiante_grupo(id,estudiante_id,grupo_id) VALUES ('"+matricula+"','"+estudiante+"','"+grupoAna+"')");
            try {
                login("ana");
                var sinNotas=promedioDe(estudiante);
                assertEquals("SIN_NOTAS",sinNotas.path("estado").asText());
                assertTrue(sinNotas.path("acumulado").isNull());
                assertEquals(201,post("/notas-estudiantes",nota(notaAna,estudiante,null),"application/json",csrf()).statusCode());
                var parcial=promedioDe(estudiante);
                assertEquals("PARCIAL",parcial.path("estado").asText());
                assertEquals(2.94,parcial.path("acumulado").asDouble(),0.000001);
                assertEquals(70,parcial.path("porcentajeEvaluado").asInt());
                assertTrue(parcial.path("definitiva").isNull());
                // Simula una calificacion historica de una evaluacion hoy vencida.
                st.executeUpdate("INSERT INTO nota_estudiante(id,estudiante_grupo_id,nota_id,valor) VALUES ('"+UUID.randomUUID()+"','"+matricula+"','"+quiz+"',3.0)");
                var definitiva=promedioDe(estudiante);
                assertEquals("DEFINITIVA",definitiva.path("estado").asText());
                assertEquals(3.84,definitiva.path("definitiva").asDouble(),0.000001);
                assertEquals(100,definitiva.path("porcentajeEvaluado").asInt());
                st.executeUpdate("UPDATE nota SET porcentaje=20 WHERE id='"+quiz+"'");
                var invalida=promedioDe(estudiante);
                assertEquals("CONFIGURACION_INVALIDA",invalida.path("estado").asText());
                assertTrue(invalida.path("acumulado").isNull());
                assertTrue(invalida.path("definitiva").isNull());
            } finally {
                st.executeUpdate("UPDATE nota SET porcentaje=30 WHERE id='"+quiz+"'");
                st.executeUpdate("DELETE FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"'");
                st.executeUpdate("DELETE FROM estudiante_grupo WHERE id='"+matricula+"'");
                st.executeUpdate("DELETE FROM estudiante WHERE id='"+estudiante+"'");
            }
        }
    }


    @Test void solicitudesSimultaneasConservanUnaSolaNota() throws Exception {
        login("ana");
        String token = csrf();
        String estudiante = UUID.randomUUID().toString(), matricula = UUID.randomUUID().toString();
        try (var connection = context.getBean(DataSource.class).getConnection(); var st = connection.createStatement()) {
            st.executeUpdate("INSERT INTO estudiante(id,identificacion,nombres,apellidos,activo) VALUES ('"+estudiante+"','TEST-CONCURRENCIA','Prueba','Concurrente',true)");
            st.executeUpdate("INSERT INTO estudiante_grupo(id,estudiante_id,grupo_id) VALUES ('"+matricula+"','"+estudiante+"','"+grupoAna+"')");
            try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
                var inicio = new java.util.concurrent.CountDownLatch(1);
                var tareas = new java.util.ArrayList<java.util.concurrent.Future<HttpResponse<String>>>();
                for (int i = 0; i < 2; i++) {
                    String valor = i == 0 ? "4.2" : "3.1";
                    tareas.add(executor.submit(() -> {
                        inicio.await();
                        return post("/notas-estudiantes", nota(notaAna,estudiante,null).replace("4.2", valor), "application/json", token);
                    }));
                }
                inicio.countDown();
                var respuestas = new java.util.ArrayList<HttpResponse<String>>();
                for (var tarea : tareas) respuestas.add(tarea.get(20, java.util.concurrent.TimeUnit.SECONDS));
                assertEquals(List.of(201,409), respuestas.stream().map(HttpResponse::statusCode).sorted().toList());
                var conflicto = respuestas.stream().filter(r -> r.statusCode()==409).findFirst().orElseThrow();
                assertTrue(conflicto.body().contains("Actualizar"), conflicto.body());
                try (var rs = st.executeQuery("SELECT count(*), min(valor) FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"'")) {
                    assertTrue(rs.next());
                    assertEquals(1,rs.getInt(1));
                    assertEquals(respuestas.get(0).statusCode()==201 ? 4.2 : 3.1,rs.getDouble(2),0.00001);
                }
            } finally {
                st.executeUpdate("DELETE FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"'");
                st.executeUpdate("DELETE FROM estudiante_grupo WHERE id='"+matricula+"'");
                st.executeUpdate("DELETE FROM estudiante WHERE id='"+estudiante+"'");
            }
        }
    }

    @Test void repositorioTraduceConflictoAunqueSeOmitaLaConsultaPrevia() throws Exception {
        var repositorio = context.getBean(NotaEstudianteRepositorioAdapter.class);
        var matricula = context.getBean(EstudianteGrupoRepositorioJPA.class).findAll().stream()
                .filter(e -> e.getEstudiante().getId().toString().equals("22222222-2222-2222-2222-222222222223"))
                .findFirst().orElseThrow().getId();
        var inicio = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var tareas = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for (int i = 0; i < 2; i++) tareas.add(executor.submit(() -> {
                inicio.await();
                try {
                    repositorio.crear(new uco.test.api.servicios.output.ports.entidades.NotaEstudianteEntidad(
                        UUID.randomUUID(),
                        new uco.test.api.servicios.output.ports.entidades.EstudianteGrupoEntidad(matricula,null,null),
                        new uco.test.api.servicios.output.ports.entidades.NotaEntidad(UUID.fromString(notaCarlos),null,null,null,null),
                        new java.math.BigDecimal("4.2")));
                    return true;
                } catch (uco.test.api.transversal.excepcion.NotaYaRegistradaException esperada) {
                    return false;
                }
            }));
            inicio.countDown();
            int guardadas = 0;
            for (var tarea : tareas) if(tarea.get(20,java.util.concurrent.TimeUnit.SECONDS)) guardadas++;
            assertEquals(1,guardadas);
        } finally {
            try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement()) {
                st.executeUpdate("DELETE FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"' AND nota_id='"+notaCarlos+"'");
            }
        }
    }

    @Test void migracionProtegeTablaExistenteYNoBorraDuplicados() throws Exception {
        String temporal = "test_unicidad_"+UUID.randomUUID().toString().replace("-","");
        try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement();
            var input=SeguridadIntegracionTest.class.getResourceAsStream("/schema.sql")) {
            String original=connection.getSchema();
            String sql=new String(input.readAllBytes(),StandardCharsets.UTF_8);
            st.execute("CREATE SCHEMA "+temporal);
            try {
                connection.setSchema(temporal);
                st.execute("CREATE TABLE nota_estudiante (estudiante_grupo_id UUID, nota_id UUID)");
                String valores="('"+UUID.randomUUID()+"','"+UUID.randomUUID()+"')";
                st.executeUpdate("INSERT INTO nota_estudiante VALUES "+valores+","+valores);
                assertThrows(SQLException.class,()->st.execute(sql));
                try(var rs=st.executeQuery("SELECT count(*) FROM nota_estudiante")) {
                    assertTrue(rs.next()); assertEquals(2,rs.getInt(1));
                }
                // Solo la tabla desechable de esta prueba; demuestra también instalación y repetición.
                st.executeUpdate("DELETE FROM nota_estudiante");
                st.execute(sql);
                st.execute(sql);
                st.executeUpdate("INSERT INTO nota_estudiante VALUES "+valores);
                assertThrows(SQLException.class,()->st.executeUpdate("INSERT INTO nota_estudiante VALUES "+valores));
            } finally {
                connection.setSchema(original);
                st.execute("DROP SCHEMA "+temporal+" CASCADE");
            }
        }
    }

    @Test void cierreSesionYRotacionCsrf() throws Exception {
        String anterior = csrf();
        login("ana");
        assertEquals(403, post("/auth/logout", "", "application/x-www-form-urlencoded", anterior).statusCode());
        assertEquals(200, post("/auth/logout", "", "application/x-www-form-urlencoded", csrf()).statusCode());
        assertEquals(401, get("/auth/me").statusCode());
        assertEquals(401, get("/mis-grupos").statusCode());
    }

    @Test void corsPermiteSoloOrigenConfigurado() throws Exception {
        var allowed = HttpRequest.newBuilder(URI.create(base+"/mis-grupos"))
            .header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "GET")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var result = client.send(allowed, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, result.statusCode());
        assertEquals("true", result.headers().firstValue("Access-Control-Allow-Credentials").orElse(""));
        var denied = HttpRequest.newBuilder(URI.create(base+"/mis-grupos"))
            .header("Origin", "https://sitio-ajeno.invalid")
            .header("Access-Control-Request-Method", "GET")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(403, client.send(denied, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test void interfazPublicaSinExponerDatosPrivados() throws Exception {
        var root = base.replace("/api/v1", "");
        for (var path : List.of("/", "/app.js", "/styles.css", "/favicon.svg")) {
            var response = client.send(HttpRequest.newBuilder(URI.create(root + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode(), path);
            assertFalse(response.body().contains(clave));
        }
        assertEquals(401, get("/mis-grupos").statusCode());
    }
}
