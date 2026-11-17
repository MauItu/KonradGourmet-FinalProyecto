package co.edu.konradlorenz.integracion.adaptadores.salida;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * «Proxy»
 *
 * @Primary: ConsultarPrecioService recibe el Proxy, y el Proxy recibe el adaptador real (DEC-35).
 *
 * Nota del diagrama: La caché vive en Redis y la comparten todas las réplicas de ms-integracion: cache
 * es un mapa respaldado por Redis, no la memoria de la réplica. Cada precio vale un día según
 * consultadoEn (supuesto S3): si venció o no está, el Proxy le pregunta a real y guarda la respuesta.
 */
@Component
@Primary
public class PrecioReferenciaCacheProxy implements PrecioReferenciaPort {

    private final PrecioReferenciaPort real;
    /** {Redis}: el mapa que arma ConfiguracionIntegracion sobre Redis (MapaPreciosRedis), compartido por las réplicas. */
    private final Map<Long, PrecioEstandar> cache;

    public PrecioReferenciaCacheProxy(@Qualifier("ministerioComercioAdapter") PrecioReferenciaPort real, Map<Long, PrecioEstandar> cache) {
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
