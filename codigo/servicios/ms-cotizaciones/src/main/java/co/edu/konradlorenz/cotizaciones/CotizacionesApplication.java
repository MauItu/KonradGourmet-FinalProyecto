package co.edu.konradlorenz.cotizaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ms-cotizaciones: proveedores (HU-15) y cotizaciones, del auxiliar que las envía (V13–V15) al director que las valida (V16–V19). */
@SpringBootApplication
public class CotizacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CotizacionesApplication.class, args);
    }
}
