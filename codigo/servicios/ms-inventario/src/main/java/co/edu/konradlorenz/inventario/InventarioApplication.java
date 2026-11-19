package co.edu.konradlorenz.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ms-inventario: existencias por sucursal, cierre diario (23:59) y umbral del 25 % (V05 y V07). */
@SpringBootApplication
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}
