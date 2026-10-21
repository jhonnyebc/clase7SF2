package uco.test.api.infraestructura.input.configuracion;

import java.util.UUID;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import uco.test.api.infraestructura.output.repositorios.estructurados.*;
import uco.test.api.infraestructura.output.repositorios.estructurados.JPA.UsuarioEntidadJPA;

@Configuration
@ConditionalOnProperty(name = "app.demo.usuarios", havingValue = "true")
public class UsuariosDemoConfiguracion {
    @Bean
    ApplicationRunner crearUsuariosDemo(UsuarioRepositorioJPA usuarios, DocenteRepositorioJPA docentes,
                                        PasswordEncoder encoder, Environment env) {
        return args -> {
            String[] nombres = {"ana", "carlos"};
            String[] ids = {"11111111-1111-1111-1111-111111111111", "11111111-1111-1111-1111-111111111112"};
            for (int i = 0; i < nombres.length; i++) {
                if (usuarios.findByUsuario(nombres[i]).isPresent()) continue;
                String clave = env.getProperty("app.demo.clave-" + nombres[i]);
                if (clave == null || clave.length() < 12 || clave.length() > 64
                        || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
                    throw new IllegalStateException("Configura app.demo.clave-" + nombres[i] + " con entre 12 y 64 caracteres (maximo 72 bytes UTF-8)");
                var docente = docentes.findById(UUID.fromString(ids[i]))
                        .orElseThrow(() -> new IllegalStateException("Faltan los docentes de demostracion"));
                usuarios.save(new UsuarioEntidadJPA(nombres[i], encoder.encode(clave), docente));
            }
        };
    }
}
