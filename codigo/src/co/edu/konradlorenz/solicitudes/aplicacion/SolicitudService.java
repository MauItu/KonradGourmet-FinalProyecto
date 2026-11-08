package co.edu.konradlorenz.solicitudes.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
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
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<SolicitudAlimentos> consultar(CriteriosConsulta c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public SolicitudAlimentos detalle(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void registrarCotizacion(String numero, String cotizacion, Map<Long, BigDecimal> precios) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void completar(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
