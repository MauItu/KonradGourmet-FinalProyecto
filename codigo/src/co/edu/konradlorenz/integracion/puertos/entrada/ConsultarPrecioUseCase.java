package co.edu.konradlorenz.integracion.puertos.entrada;

import java.math.BigDecimal;

public interface ConsultarPrecioUseCase {

    BigDecimal precioSugerido(Long productoId);

}
