package co.edu.konradlorenz.analitica.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.List;

/**
 * Las promociones sugeridas de la semana (HU-19). Cada sugerencia viaja como una línea de texto lista para el
 * correo, «producto · motivo · vigencia», para que ms-notificaciones no importe el dominio de ms-analitica
 * (PD-05, DEC-29).
 */
public class TendenciaDetectadaEvent extends EventoDominio {

    private List<String> sugerencias;
    private String fuente;

    public TendenciaDetectadaEvent(String usuario, List<String> sugerencias, String fuente) {
        super(usuario);
        this.sugerencias = sugerencias;
        this.fuente = fuente;
    }

    public List<String> sugerencias() {
        return sugerencias;
    }

    public String fuente() {
        return fuente;
    }

    @Override
    public String tipo() {
        return "tendencia.detectada";
    }

}
