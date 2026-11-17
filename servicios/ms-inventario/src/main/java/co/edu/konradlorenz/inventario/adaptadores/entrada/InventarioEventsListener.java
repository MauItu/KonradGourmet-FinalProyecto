package co.edu.konradlorenz.inventario.adaptadores.entrada;

import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * «Listener · menu.actualizado · solicitud.registrada»
 *
 * menu.actualizado reemplaza la copia local de la receta del plato; solicitud.registrada atiende las
 * alertas de los productos pedidos en esa sucursal (HU-16).
 */
@Component
public class InventarioEventsListener implements EventListener<EventoDominio> {

    private final GestionarInventarioUseCase casoDeUso;

    public InventarioEventsListener(GestionarInventarioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    @KafkaListener(topics = {"${konrad.kafka.prefijo-topico:}menu.actualizado", "${konrad.kafka.prefijo-topico:}solicitud.registrada"})
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof MenuActualizadoEvent menu) {
            casoDeUso.actualizarReceta(new RecetaPlato(menu.platoId(), menu.receta()));
        } else if (evento instanceof SolicitudRegistradaEvent solicitud) {
            casoDeUso.atenderAlertas(solicitud.sucursalId(), solicitud.productoIds());
        }
    }

}
