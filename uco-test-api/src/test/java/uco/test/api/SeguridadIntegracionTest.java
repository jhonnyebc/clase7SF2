package uco.test.api;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;
import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import uco.test.api.inicializador.UcoTestApiApplication;
import uco.test.api.infraestructura.output.repositorios.estructurados.*;

/**
 * Levanta la API real y le habla por HTTP. En lugar de Auth0, la prueba firma sus propios
 * tokens con una clave RSA temporal y le indica a la API la clave publica correspondiente.
 */
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
    static final String issuer = "https://tenant-de-prueba.invalid/";
    static final String audience = "https://uco-test-api";
    static final String claimCorreo = "https://uco-test-api/email";
    static final Map<String, String> correos = Map.of("ana", "ana.gomez@ucotest.com", "carlos", "carlos.mejia@ucotest.com");
    static PrivateKey clavePrivada;
    static Path clavePublica;
    static String[] argumentosAuth;
    HttpClient client;
    String bearer;

    static Connection postgres() throws SQLException {
        return DriverManager.getConnection(pgUrl,
            System.getProperty("test.db.user", "ucotest"), System.getProperty("test.db.password", "ucotest"));
    }

    @BeforeAll
    static void preparar() throws Exception {
        var generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        KeyPair claves = generador.generateKeyPair();
        clavePrivada = claves.getPrivate();
        clavePublica = Files.createTempFile("clave-publica-prueba", ".pem");
        Files.writeString(clavePublica, "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(claves.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----\n");
        argumentosAuth = new String[] {"--app.auth.issuer=" + issuer, "--app.auth.audience=" + audience,
            "--app.auth.clave-publica=" + clavePublica.toUri()};

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
        var argumentos = new ArrayList<>(List.of(
            "--server.port=0", "--server.address=127.0.0.1",
            "--spring.datasource.url=" + url, "--spring.datasource.driver-class-name=" + driver,
            "--spring.datasource.username=" + user, "--spring.datasource.password=" + password,
            "--spring.jpa.hibernate.ddl-auto=create", "--spring.sql.init.mode=never",
            "--logging.level.root=ERROR", "--spring.main.banner-mode=off"));
        argumentos.addAll(List.of(argumentosAuth));
        context = SpringApplication.run(UcoTestApiApplication.class, argumentos.toArray(String[]::new));
        base = "http://127.0.0.1:" + ((WebServerApplicationContext)context).getWebServer().getPort() + "/api/v1";
        try (var input = SeguridadIntegracionTest.class.getResourceAsStream("/data.sql");
             var connection = context.getBean(DataSource.class).getConnection(); var st = connection.createStatement()) {
            var sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).replace("ON CONFLICT (id) DO NOTHING", "");
            for (String part : sql.split(";")) if (!part.isBlank()) st.execute(part);
        }
    }

    @AfterAll
    static void cerrar() throws Exception {
        if (context != null) context.close();
        if (clavePublica != null) Files.deleteIfExists(clavePublica);
        if (schema != null) {
            try (var connection = postgres(); var st = connection.createStatement()) {
                st.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }

    @BeforeEach
    void cliente() {
        client = HttpClient.newHttpClient();
        bearer = null;
    }

    static String firmar(PrivateKey clave, String iss, String aud, Instant vence, String correo) throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer(iss).audience(aud).subject("auth0|prueba")
            .issueTime(new Date()).expirationTime(Date.from(vence));
        if (correo != null) claims.claim(claimCorreo, correo);
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims.build());
        jwt.sign(new RSASSASigner(clave));
        return jwt.serialize();
    }
    static String tokenPara(String correo) throws Exception {
        return firmar(clavePrivada, issuer, audience, Instant.now().plusSeconds(300), correo);
    }

    HttpResponse<String> get(String ruta) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(base + ruta)).GET();
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> post(String ruta, String contenido) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(base + ruta)).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(contenido));
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    void login(String usuario) throws Exception {
        bearer = tokenPara(correos.get(usuario));
    }
    String nota(String evaluacion, String estudiante, String docente) {
        return "{\"idNota\":\"" + evaluacion + "\",\"idEstudiante\":\"" + estudiante
            + "\",\"valor\":4.2" + (docente == null ? "" : ",\"idDocente\":\"" + docente + "\"") + "}";
    }

    @Test void requiereToken() throws Exception {
        for (var ruta : List.of("/mis-grupos", "/auth/me", "/docentes/"+ana+"/grupos",
                "/grupos/"+grupoAna+"/notas", "/grupos/"+grupoAna+"/ponderacion", "/grupos/"+grupoAna+"/promedios", "/notas/"+notaAna+"/estudiantes"))
            assertEquals(401, get(ruta).statusCode(), ruta);
        assertEquals(401, post("/notas-estudiantes", "{}").statusCode());
    }

    @Test void rechazaTokensInvalidos() throws Exception {
        var otraClave = KeyPairGenerator.getInstance("RSA");
        otraClave.initialize(2048);
        var correo = correos.get("ana");
        var invalidos = Map.of(
            "emisor ajeno", firmar(clavePrivada, "https://otro-tenant.invalid/", audience, Instant.now().plusSeconds(300), correo),
            "audiencia ajena", firmar(clavePrivada, issuer, "https://otra-api", Instant.now().plusSeconds(300), correo),
            "vencido", firmar(clavePrivada, issuer, audience, Instant.now().minusSeconds(3600), correo),
            "firma ajena", firmar(otraClave.generateKeyPair().getPrivate(), issuer, audience, Instant.now().plusSeconds(300), correo),
            "texto basura", "esto-no-es-un-jwt");
        for (var caso : invalidos.entrySet()) {
            bearer = caso.getValue();
            assertEquals(401, get("/auth/me").statusCode(), caso.getKey());
        }
        // Token valido pero de alguien que no es docente, o sin el correo: autenticado pero sin acceso.
        bearer = tokenPara("intruso@ucotest.com");
        assertEquals(403, get("/auth/me").statusCode());
        assertEquals(403, get("/mis-grupos").statusCode());
        bearer = tokenPara(null);
        assertEquals(403, get("/auth/me").statusCode());
        // El correo se compara completo e ignorando mayusculas.
        bearer = tokenPara("ANA.GOMEZ@UCOTEST.COM");
        assertTrue(get("/auth/me").body().contains(ana));
        bearer = tokenPara("na.gomez@ucotest.com");
        assertEquals(403, get("/auth/me").statusCode());
    }

    @Test void identificaAlDocentePorElCorreoDelToken() throws Exception {
        login("ana");
        var yo = get("/auth/me");
        assertEquals(200, yo.statusCode(), yo.body());
        assertTrue(yo.body().contains(ana));
        assertTrue(yo.body().contains("ana.gomez@ucotest.com"));
        assertTrue(yo.headers().allValues("Set-Cookie").isEmpty(), "La API no debe crear sesiones");
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

    @Test void registroUsaIdentidadDelToken() throws Exception {
        login("ana");
        assertEquals(400, post("/notas-estudiantes", "{}").statusCode());
        var ajena = nota(notaCarlos, "22222222-2222-2222-2222-222222222223", carlos);
        assertEquals(403, post("/notas-estudiantes", ajena).statusCode());
        var propia = nota(notaAna, "22222222-2222-2222-2222-222222222222", carlos);
        // El identificador falso de Carlos no cambia la identidad del token de Ana.
        var response = post("/notas-estudiantes", propia);
        assertEquals(201, response.statusCode(), response.body());
        assertTrue(get("/notas/"+notaAna+"/estudiantes").body().contains("\"valor\":4.2"));
        assertEquals(409, post("/notas-estudiantes", propia).statusCode());
        var sinDocente = nota(notaAna, "22222222-2222-2222-2222-222222222225", null);
        assertEquals(201, post("/notas-estudiantes", sinDocente).statusCode());
        bearer = null;
        assertEquals(401, post("/notas-estudiantes", sinDocente).statusCode());
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
                assertEquals(201,post("/notas-estudiantes",nota(notaAna,estudiante,null)).statusCode());
                var parcial=promedioDe(estudiante);
                assertEquals("PARCIAL",parcial.path("estado").asText());
                assertEquals(2.94,parcial.path("acumulado").asDouble(),0.000001);
                assertEquals(70,parcial.path("porcentajeEvaluado").asInt());
                assertTrue(parcial.path("definitiva").isNull());
                // Inserta directamente la segunda nota (como una calificacion ya existente del Quiz 1).
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
                        return post("/notas-estudiantes", nota(notaAna,estudiante,null).replace("4.2", valor));
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


    @Test void rechazaValoresInvalidosYCuerposIncompletos() throws Exception {
        login("ana");
        String valido=nota(notaAna,"22222222-2222-2222-2222-222222222222",null);
        for(String valor : List.of("-0.1","5.1","3.25","null")) {
            var respuesta=post("/notas-estudiantes",valido.replace("4.2",valor));
            assertEquals(400,respuesta.statusCode(),respuesta.body());
            assertTrue(respuesta.body().contains("error"),respuesta.body());
        }
        for(String cuerpo : List.of("{}", "{", valido.replace(notaAna,"no-es-un-uuid"))) {
            var respuesta=post("/notas-estudiantes",cuerpo);
            assertEquals(400,respuesta.statusCode(),respuesta.body());
            assertTrue(respuesta.body().contains("error"),respuesta.body());
        }
    }

    @Test void rechazaInactivosAjenosYEvaluacionesFueraDePlazo() throws Exception {
        login("ana");
        for(String estudiante : List.of("22222222-2222-2222-2222-222222222224",
                "22222222-2222-2222-2222-222222222223",UUID.randomUUID().toString())) {
            assertEquals(400,post("/notas-estudiantes",nota(notaAna,estudiante,null)).statusCode());
        }
        // Evaluaciones temporales: una que ya cerro y otra que aun no abre.
        String vencida=UUID.randomUUID().toString(), futura=UUID.randomUUID().toString();
        try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement()) {
            st.executeUpdate("INSERT INTO nota(id,grupo_id,nombre,fecha_registro_desde,fecha_registro_hasta,porcentaje) VALUES ('"+vencida+"','"+grupoAna+"','Vencida','2020-01-01 00:00:00+00','2020-12-31 00:00:00+00',10)");
            st.executeUpdate("INSERT INTO nota(id,grupo_id,nombre,fecha_registro_desde,fecha_registro_hasta,porcentaje) VALUES ('"+futura+"','"+grupoAna+"','Futura','2099-01-01 00:00:00+00','2099-12-31 00:00:00+00',10)");
            try {
                assertEquals(400,post("/notas-estudiantes",nota(vencida,"22222222-2222-2222-2222-222222222222",null)).statusCode());
                assertEquals(400,post("/notas-estudiantes",nota(futura,"22222222-2222-2222-2222-222222222222",null)).statusCode());
            } finally {
                st.executeUpdate("DELETE FROM nota WHERE id IN ('"+vencida+"','"+futura+"')");
            }
        }
    }

    @Test void aceptaCeroYCincoYConservaValorAlDuplicar() throws Exception {
        login("ana");
        try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement()) {
            for(String valor : List.of("0.0","5.0")) {
                String estudiante=UUID.randomUUID().toString(), matricula=UUID.randomUUID().toString();
                st.executeUpdate("INSERT INTO estudiante(id,identificacion,nombres,apellidos,activo) VALUES ('"+estudiante+"','TEST-LIMITE','Prueba','Limite',true)");
                st.executeUpdate("INSERT INTO estudiante_grupo(id,estudiante_id,grupo_id) VALUES ('"+matricula+"','"+estudiante+"','"+grupoAna+"')");
                try {
                    assertEquals(201,post("/notas-estudiantes",nota(notaAna,estudiante,null).replace("4.2",valor)).statusCode());
                    assertEquals(409,post("/notas-estudiantes",nota(notaAna,estudiante,null)).statusCode());
                    var promedio=promedioDe(estudiante);
                    assertEquals("PARCIAL",promedio.path("estado").asText());
                    assertEquals(70,promedio.path("porcentajeEvaluado").asInt());
                    assertEquals(Double.parseDouble(valor)*0.7,promedio.path("acumulado").asDouble(),0.00001);
                    try(var rs=st.executeQuery("SELECT valor FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"'")) {
                        assertTrue(rs.next()); assertEquals(Double.parseDouble(valor),rs.getDouble(1),0.00001);
                        assertFalse(rs.next());
                    }
                } finally {
                    st.executeUpdate("DELETE FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"'");
                    st.executeUpdate("DELETE FROM estudiante_grupo WHERE id='"+matricula+"'");
                    st.executeUpdate("DELETE FROM estudiante WHERE id='"+estudiante+"'");
                }
            }
        }
    }

    @Test void conservaNotaTrasReiniciarAplicacion() throws Exception {
        String estudiante=UUID.randomUUID().toString(), matricula=UUID.randomUUID().toString();
        try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement()) {
            st.executeUpdate("INSERT INTO estudiante(id,identificacion,nombres,apellidos,activo) VALUES ('"+estudiante+"','TEST-REINICIO','Prueba','Reinicio',true)");
            st.executeUpdate("INSERT INTO estudiante_grupo(id,estudiante_id,grupo_id) VALUES ('"+matricula+"','"+estudiante+"','"+grupoAna+"')");
        }
        try {
            login("ana");
            assertEquals(201,post("/notas-estudiantes",nota(notaAna,estudiante,null)).statusCode());
            var env=context.getEnvironment();
            var argumentos = new ArrayList<>(List.of(
                "--server.port=0","--server.address=127.0.0.1",
                "--spring.datasource.url="+env.getProperty("spring.datasource.url"),
                "--spring.datasource.driver-class-name="+env.getProperty("spring.datasource.driver-class-name"),
                "--spring.datasource.username="+env.getProperty("spring.datasource.username"),
                "--spring.datasource.password="+env.getProperty("spring.datasource.password"),
                "--spring.jpa.hibernate.ddl-auto=update","--spring.sql.init.mode=never",
                "--logging.level.root=ERROR","--spring.main.banner-mode=off"));
            argumentos.addAll(List.of(argumentosAuth));
            context.close();
            context=SpringApplication.run(UcoTestApiApplication.class,argumentos.toArray(String[]::new));
            base="http://127.0.0.1:"+((WebServerApplicationContext)context).getWebServer().getPort()+"/api/v1";
            cliente();
            assertEquals(401,get("/auth/me").statusCode());
            login("ana");
            var promedio=promedioDe(estudiante);
            assertEquals(2.94,promedio.path("acumulado").asDouble(),0.00001);
            assertEquals(1,promedio.path("registradas").asInt());
            assertEquals(409,post("/notas-estudiantes",nota(notaAna,estudiante,null)).statusCode());
        } finally {
            try(var connection=context.getBean(DataSource.class).getConnection(); var st=connection.createStatement()) {
                st.executeUpdate("DELETE FROM nota_estudiante WHERE estudiante_grupo_id='"+matricula+"'");
                st.executeUpdate("DELETE FROM estudiante_grupo WHERE id='"+matricula+"'");
                st.executeUpdate("DELETE FROM estudiante WHERE id='"+estudiante+"'");
            }
        }
    }

    @Test void corsPermiteSoloOrigenConfigurado() throws Exception {
        var allowed = HttpRequest.newBuilder(URI.create(base+"/mis-grupos"))
            .header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var result = client.send(allowed, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, result.statusCode());
        assertEquals("http://localhost:4200", result.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertTrue(result.headers().firstValue("Access-Control-Allow-Headers").orElse("").toLowerCase().contains("authorization"));
        var denied = HttpRequest.newBuilder(URI.create(base+"/mis-grupos"))
            .header("Origin", "https://sitio-ajeno.invalid")
            .header("Access-Control-Request-Method", "GET")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(403, client.send(denied, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
