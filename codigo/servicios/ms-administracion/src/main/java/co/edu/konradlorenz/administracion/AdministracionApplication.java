package co.edu.konradlorenz.administracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** ms-administracion: parámetros y tema, auditoría de todos los servicios y logs de errores (página 4e). */
@SpringBootApplication
@EnableScheduling
public class AdministracionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministracionApplication.class, args);
    }
}
