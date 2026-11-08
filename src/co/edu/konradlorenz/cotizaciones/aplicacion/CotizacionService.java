package co.edu.konradlorenz.cotizaciones.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionValidadaEvent;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
        return fachada.generarYEnviar(c);
    }

    @Override
    public Cotizacion registrarValores(String numero, Map<Long, BigDecimal> precios) {
        Cotizacion c = detalle(numero);
        if (c.estado() != EstadoCotizacion.ENVIADA) {
            throw new IllegalStateException("Solo se registran valores en una cotización enviada (estado actual: " + c.estado() + ")");
        }
        precios.forEach(c::registrarValor);
        return repositorio.guardar(c);
    }

    @Override
    public Cotizacion validar(String numero) {
        Cotizacion c = detalle(numero);
        if (c.estado() != EstadoCotizacion.ENVIADA) {
            throw new IllegalStateException("Solo se valida una cotización enviada (estado actual: " + c.estado() + ")");
        }
        // la cadena de reglas decide: Rechazada → Sospechosa → Opcionada
        c.cambiarEstado(reglas.evaluar(c));
        Cotizacion validada = repositorio.guardar(c);
        eventos.publicar(new CotizacionValidadaEvent("sistema", validada.numero(), validada.proveedor().id(),
                validada.estado(), validada.valorCotizadoTotal()));
        return validada;
    }

    @Override
    public List<Cotizacion> consultar(EstadoCotizacion estado) {
        if (estado != null) {
            return repositorio.porEstado(estado);
        }
        List<Cotizacion> todas = new ArrayList<>();
        for (EstadoCotizacion e : EstadoCotizacion.values()) {
            todas.addAll(repositorio.porEstado(e));
        }
        todas.sort(Comparator.comparing(Cotizacion::numero));
        return todas;
    }

    @Override
    public byte[] pdf(String numero) {
        Cotizacion c = detalle(numero);
        if (c.pdfUrl() == null) {
            throw new IllegalStateException("La cotización " + numero + " todavía no tiene PDF");
        }
        return almacen.obtener(c.pdfUrl());
    }

    @Override
    public Cotizacion detalle(String numero) {
        Cotizacion c = repositorio.porNumero(numero);
        if (c == null) {
            throw new IllegalArgumentException("No existe la cotización " + numero);
        }
        return c;
    }

    @Override
    public void reenviar(String numero) {
        Cotizacion c = detalle(numero);
        if (c.estado() == EstadoCotizacion.PENDIENTE_PRECIO) {
            fachada.generarYEnviar(c); // completa el precio que faltaba
        } else if (c.estado() == EstadoCotizacion.ENVIADA) {
            // el precio ya estaba: se vuelve a publicar cotizacion.enviada por si falló el correo
            Map<Long, BigDecimal> sugeridos = new LinkedHashMap<>();
            c.items().forEach(i -> sugeridos.put(i.productoId(), i.precioEstandar()));
            eventos.publicar(new CotizacionEnviadaEvent("sistema", c.numero(), c.solicitudNumeros(), sugeridos,
                    c.proveedor().correo(), c.pdfUrl()));
        } else {
            throw new IllegalStateException("Solo se reenvía una cotización pendiente de precio o enviada");
        }
    }

    @Override
    public void marcarConOrden(String numero) {
        Cotizacion c = detalle(numero);
        if (c.estado() == EstadoCotizacion.CON_ORDEN) {
            return; // el evento puede llegar dos veces
        }
        c.cambiarEstado(EstadoCotizacion.CON_ORDEN);
        repositorio.guardar(c);
    }

}
