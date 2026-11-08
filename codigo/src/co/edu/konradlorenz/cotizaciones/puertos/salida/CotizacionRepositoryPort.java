package co.edu.konradlorenz.cotizaciones.puertos.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;

import java.util.List;

public interface CotizacionRepositoryPort {

    Cotizacion guardar(Cotizacion c);

    Cotizacion porNumero(String numero);

    List<Cotizacion> porEstado(EstadoCotizacion e);

}
