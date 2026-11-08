package co.edu.konradlorenz.cotizaciones.puertos.entrada;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GestionarCotizacionUseCase {

    Cotizacion generar(Cotizacion c);

    Cotizacion registrarValores(String numero, Map<Long, BigDecimal> precios);

    Cotizacion validar(String numero);

    List<Cotizacion> consultar(EstadoCotizacion estado);

    byte[] pdf(String numero);

    Cotizacion detalle(String numero);

    void reenviar(String numero);

    void marcarConOrden(String numero);

}
