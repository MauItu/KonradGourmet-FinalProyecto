package co.edu.konradlorenz.cotizaciones.puertos.salida;

import java.math.BigDecimal;

public interface PrecioSugeridoPort {

    BigDecimal precioSugerido(Long productoId);

}
