package co.edu.konradlorenz.cotizaciones.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CotizacionService implements GestionarCotizacionUseCase {

    private final GenerarCotizacionFacade fachada;
    private final CotizacionRepositoryPort repositorio;
    private final ReglaValidacion reglas;
    private final AlmacenArchivosPort almacen;
    private final EventPublisherPort eventos;

    public CotizacionService(GenerarCotizacionFacade fachada, CotizacionRepositoryPort repositorio, ReglaValidacion reglas, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        this.fachada = fachada;
        this.repositorio = repositorio;
        this.reglas = reglas;
        this.almacen = almacen;
        this.eventos = eventos;
    }

    @Override
    public Cotizacion generar(Cotizacion c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Cotizacion registrarValores(String numero, Map<Long, BigDecimal> precios) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Cotizacion validar(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Cotizacion> consultar(EstadoCotizacion estado) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public byte[] pdf(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Cotizacion detalle(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void reenviar(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void marcarConOrden(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
