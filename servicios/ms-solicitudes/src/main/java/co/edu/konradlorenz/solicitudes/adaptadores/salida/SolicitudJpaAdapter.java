package co.edu.konradlorenz.solicitudes.adaptadores.salida;

import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda las solicitudes en ms_solicitudes (PostgreSQL). Una solicitud que llega sin número recibe el
 * siguiente de la secuencia (SOL-031, SOL-032…) y se devuelve con él. buscar() aplica en la base los
 * cinco criterios de V11 (HU-07); un criterio en null no filtra, como en SolicitudEnMemoria.
 */
@Repository
public class SolicitudJpaAdapter implements SolicitudRepositoryPort {

    private final SolicitudJpaRepository solicitudes;

    public SolicitudJpaAdapter(SolicitudJpaRepository solicitudes) {
        this.solicitudes = solicitudes;
    }

    @Override
    @Transactional
    public SolicitudAlimentos guardar(SolicitudAlimentos s) {
        SolicitudAlimentos conNumero = s;
        if (s.numero() == null) {
            conNumero = new SolicitudAlimentos(String.format("SOL-%03d", solicitudes.siguienteNumero()), s.sucursalId(),
                    s.solicitanteId(), s.correoSolicitante(), s.fecha(), s.estado(), s.valorSugeridoTotal(),
                    s.cotizacionesAplicadas(), s.items());
        }
        SolicitudEntidad e = solicitudes.findById(conNumero.numero()).orElseGet(SolicitudEntidad::new);
        e.numero = conNumero.numero();
        e.sucursalId = conNumero.sucursalId();
        e.solicitanteId = conNumero.solicitanteId();
        e.correoSolicitante = conNumero.correoSolicitante();
        e.fecha = conNumero.fecha();
        e.estado = conNumero.estado().name();
        e.valorSugeridoTotal = conNumero.valorSugeridoTotal();
        e.items.clear();
        for (ItemSolicitud i : conNumero.items()) {
            e.items.add(new SolicitudEntidad.Item(i.productoId(), i.cantidad(), i.marca(), i.unidad(), i.cotizado(), i.precioSugerido()));
        }
        e.cotizacionesAplicadas.clear();
        e.cotizacionesAplicadas.addAll(conNumero.cotizacionesAplicadas());
        solicitudes.save(e);
        return conNumero;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudAlimentos> buscar(CriteriosConsulta c) {
        return solicitudes.findAll(criterios(c), Sort.by("numero")).stream().map(SolicitudJpaAdapter::solicitud).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudAlimentos porNumero(String numero) {
        return solicitudes.findById(numero).map(SolicitudJpaAdapter::solicitud).orElse(null);
    }

    /** Las fechas son días completos: «hasta» incluye todo ese día. */
    private static Specification<SolicitudEntidad> criterios(CriteriosConsulta c) {
        return (raiz, consulta, cb) -> {
            List<Predicate> y = new ArrayList<>();
            if (c.desde() != null) {
                y.add(cb.greaterThanOrEqualTo(raiz.get("fecha"), c.desde().atStartOfDay()));
            }
            if (c.hasta() != null) {
                y.add(cb.lessThan(raiz.get("fecha"), c.hasta().plusDays(1).atStartOfDay()));
            }
            if (c.estado() != null) {
                y.add(cb.equal(raiz.get("estado"), c.estado().name()));
            }
            if (c.productoId() != null || c.marca() != null) {
                Join<SolicitudEntidad, SolicitudEntidad.Item> item = raiz.join("items");
                if (c.productoId() != null) {
                    y.add(cb.equal(item.get("productoId"), c.productoId()));
                }
                if (c.marca() != null) {
                    y.add(cb.equal(item.get("marca"), c.marca()));
                }
                consulta.distinct(true);
            }
            return cb.and(y.toArray(Predicate[]::new));
        };
    }

    private static SolicitudAlimentos solicitud(SolicitudEntidad e) {
        List<ItemSolicitud> items = e.items.stream()
                .map(i -> new ItemSolicitud(i.productoId, i.cantidad, i.marca, i.unidad, i.cotizado, i.precioSugerido)).toList();
        return new SolicitudAlimentos(e.numero, e.sucursalId, e.solicitanteId, e.correoSolicitante, e.fecha,
                EstadoSolicitud.valueOf(e.estado), e.valorSugeridoTotal, e.cotizacionesAplicadas, items);
    }

}
