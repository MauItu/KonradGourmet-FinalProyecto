package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * En el proyecto real: @Repository.
 *
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
        datos.put(c.numero(), c);
        return c;
    }

    @Override
    public Cotizacion porNumero(String numero) {
        return datos.get(numero);
    }

    @Override
    public List<Cotizacion> porEstado(EstadoCotizacion e) {
        return datos.values().stream()
                .filter(c -> c.estado() == e)
                .sorted(Comparator.comparing(Cotizacion::numero))
                .toList();
    }

}
