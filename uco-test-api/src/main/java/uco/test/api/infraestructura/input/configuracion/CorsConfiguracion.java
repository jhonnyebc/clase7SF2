package uco.test.api.infraestructura.input.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguracion implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public CorsConfiguracion(@Value("${app.cors.origenes-permitidos}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
