package co.edu.konradlorenz.identidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ms-identidad: inicio de sesión (V01), alta de usuarios, roles y menú por rol; emite el JWT. */
@SpringBootApplication
public class IdentidadApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentidadApplication.class, args);
    }
}
