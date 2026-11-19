package co.edu.konradlorenz.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Registry del sistema (Eureka). Cada microservicio se registra al arrancar y
 * renueva su latido cada 30 segundos; el API Gateway le pregunta qué instancias están vivas
 * (sección 8.4 del reporte, página 3a del .drawio).
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
