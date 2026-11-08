package co.edu.konradlorenz.administracion.adaptadores.entrada;

import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.compartido.eventos.EventListener;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;

/**
 * «Listener · auditoria.evento»
 *
 * En el proyecto real: @Component, y alRecibir() lleva @KafkaListener con sus tópicos.
 */
public class AuditoriaListener implements EventListener<EventoDominio> {

    private final AdministracionUseCase casoDeUso;

    public AuditoriaListener(AdministracionUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Override
    public void alRecibir(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
