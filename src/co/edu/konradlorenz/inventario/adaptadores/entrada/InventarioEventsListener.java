package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;

/**
 * «Listener · menu.actualizado · solicitud.registrada»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 */
public class InventarioEventsListener implements EventListener<EventoDominio> {

    private final GestionarInventarioUseCase casoDeUso;

    public InventarioEventsListener(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof MenuActualizadoEvent menu) {
            casoDeUso.actualizarReceta(new RecetaPlato(menu.platoId(), menu.receta()));
        } else if (evento instanceof SolicitudRegistradaEvent solicitud) {
            casoDeUso.atenderAlertas(solicitud.sucursalId(), solicitud.productoIds());
        }
    }

}
