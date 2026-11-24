package co.edu.konradlorenz.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ms-notificaciones: correos certificados con sello de tiempo (al proveedor, con el PDF) y la alarma de stock bajo de V05. */
@SpringBootApplication
public class NotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }
}
