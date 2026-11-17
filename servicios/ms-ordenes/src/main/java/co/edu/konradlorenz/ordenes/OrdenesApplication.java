package co.edu.konradlorenz.ordenes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ms-ordenes: la directora de compras genera la orden de compra de una cotización OPCIONADA (V21–V22) y consulta las órdenes (V23). */
@SpringBootApplication
public class OrdenesApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdenesApplication.class, args);
    }
}
