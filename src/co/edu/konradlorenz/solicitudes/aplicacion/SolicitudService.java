package co.edu.konradlorenz.solicitudes.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudEnTramiteEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SolicitudService implements GestionarSolicitudUseCase {

    private final SolicitudRepositoryPort repositorio;
    private final EventPublisherPort eventos;

    public SolicitudService(SolicitudRepositoryPort repositorio, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    @Override
    public SolicitudAlimentos registrar(SolicitudAlimentos s) {
        s.validarFormato();
        SolicitudAlimentos guardada = repositorio.guardar(s);
        List<Long> productoIds = guardada.items().stream().map(ItemSolicitud::productoId).toList();
        eventos.publicar(new SolicitudRegistradaEvent(guardada.correoSolicitante(), guardada.numero(), guardada.sucursalId(),
                guardada.solicitanteId(), guardada.correoSolicitante(), productoIds));
        return guardada;
    }

    @Override
    public List<SolicitudAlimentos> consultar(CriteriosConsulta c) {
        return repositorio.buscar(c);
    }

    @Override
    public SolicitudAlimentos detalle(String numero) {
        SolicitudAlimentos s = repositorio.porNumero(numero);
        if (s == null) {
            throw new IllegalArgumentException("No existe la solicitud " + numero);
        }
        return s;
    }

    @Override
    public void registrarCotizacion(String numero, String cotizacion, Map<Long, BigDecimal> precios) {
        SolicitudAlimentos s = detalle(numero);
        if (s.yaAplicada(cotizacion)) {
            return; // un reenvío de la cotización no suma dos veces
        }
        precios.forEach(s::marcarCotizado);
        s.cotizacionesAplicadas().add(cotizacion);
        boolean pasaAEnTramite = s.estado() == EstadoSolicitud.PENDIENTE && s.todoCotizado();
        if (pasaAEnTramite) {
            s.pasarAEnTramite();
        }
        repositorio.guardar(s);
        if (pasaAEnTramite) {
            eventos.publicar(new SolicitudEnTramiteEvent(s.correoSolicitante(), s.numero(), s.correoSolicitante()));
        }
    }

    @Override
    public void completar(String numero) {
        SolicitudAlimentos s = detalle(numero);
        s.completar();
        repositorio.guardar(s);
    }

}
