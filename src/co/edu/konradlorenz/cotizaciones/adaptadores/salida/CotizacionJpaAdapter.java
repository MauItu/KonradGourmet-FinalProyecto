package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;

import java.util.List;
import java.util.Map;

/**
 * En el esqueleto, «datos» simula la tabla en memoria; en el proyecto real este adaptador
 * usa un repositorio JPA sobre la base de datos de ms-cotizaciones.
 */
public class CotizacionJpaAdapter implements CotizacionRepositoryPort {

    private final Map<String, Cotizacion> datos;

    public CotizacionJpaAdapter(Map<String, Cotizacion> datos) {
        this.datos = datos;
    }

    @Override
    public Cotizacion guardar(Cotizacion c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Cotizacion porNumero(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Cotizacion> porEstado(EstadoCotizacion e) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
