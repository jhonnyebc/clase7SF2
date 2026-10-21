package uco.test.api.inicializador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "uco.test.api")
@EnableJpaRepositories(basePackages = "uco.test.api.infraestructura.output.repositorios.estructurados")
@EntityScan(basePackages = "uco.test.api.infraestructura.output.repositorios.estructurados.JPA")
public class UcoTestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UcoTestApiApplication.class, args);
    }
}
