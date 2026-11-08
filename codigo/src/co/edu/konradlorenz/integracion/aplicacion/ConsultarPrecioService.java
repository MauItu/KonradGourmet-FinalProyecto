package co.edu.konradlorenz.integracion.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;

import java.math.BigDecimal;

public class ConsultarPrecioService implements ConsultarPrecioUseCase {

    private final PrecioReferenciaPort precios;
    private final EventPublisherPort eventos;

    public ConsultarPrecioService(PrecioReferenciaPort precios, EventPublisherPort eventos) {
        this.precios = precios;
        this.eventos = eventos;
    }

    @Override
    public BigDecimal precioSugerido(Long productoId) {
        BigDecimal valor = precios.precioEstandar(productoId).valor();
        eventos.publicar(new AuditoriaEvent("sistema", "Precio sugerido consultado: producto " + productoId));
        return valor;
    }

}
