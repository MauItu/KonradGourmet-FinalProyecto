package co.edu.konradlorenz.integracion.adaptadores.salida;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * «Proxy»
 *
 * En el proyecto real: @Component y @Primary: ConsultarPrecioService recibe el Proxy, y el Proxy
 * recibe el adaptador real.
 *
 * Nota del diagrama: La caché vive en Redis y la comparten todas las réplicas de ms-integracion: cache
 * es un mapa respaldado por Redis, no la memoria de la réplica. Cada precio vale un día según
 * consultadoEn (supuesto S3): si venció o no está, el Proxy le pregunta a real y guarda la respuesta.
 */
public class PrecioReferenciaCacheProxy implements PrecioReferenciaPort {

    private final PrecioReferenciaPort real;
    /** {Redis}: en producción, un mapa respaldado por Redis y compartido por todas las réplicas; en la demo, un HashMap. */
    private final Map<Long, PrecioEstandar> cache;

    public PrecioReferenciaCacheProxy(PrecioReferenciaPort real, Map<Long, PrecioEstandar> cache) {
        this.real = real;
        this.cache = cache;
    }

    @Override
    public PrecioEstandar precioEstandar(Long productoId) {
        PrecioEstandar guardado = cache.get(productoId);
        // cada precio vale un día según consultadoEn (supuesto S3)
        if (guardado != null && guardado.consultadoEn().plusDays(1).isAfter(LocalDateTime.now())) {
            return guardado;
        }
        PrecioEstandar nuevo = real.precioEstandar(productoId);
        cache.put(productoId, nuevo);
        return nuevo;
    }

}
