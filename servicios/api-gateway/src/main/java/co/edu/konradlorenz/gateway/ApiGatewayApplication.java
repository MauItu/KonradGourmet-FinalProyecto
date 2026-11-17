package co.edu.konradlorenz.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway (Spring Cloud Gateway): la Facade del sistema. La SPA ve una sola dirección
 * y el gateway enruta los 13 prefijos a los once microservicios, que busca en Eureka
 * (tabla 5 y sección 8.4 del reporte). La validación del JWT emitido por ms-identidad
 * se agrega en el corte 2.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
