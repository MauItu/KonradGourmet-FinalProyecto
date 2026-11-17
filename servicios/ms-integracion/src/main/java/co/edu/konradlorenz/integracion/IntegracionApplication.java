package co.edu.konradlorenz.integracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ms-integracion: la frontera SOA. Hacia adentro, el precio estándar del Ministerio de Comercio (REST
 * interno para ms-cotizaciones); hacia afuera, el endpoint SOAP del restaurante (/api/integracion/**).
 */
@SpringBootApplication
public class IntegracionApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegracionApplication.class, args);
    }
}
