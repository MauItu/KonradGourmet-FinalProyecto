package co.edu.konradlorenz.analitica.eventos;

import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.List;

public class TendenciaDetectadaEvent extends EventoDominio {

    private List<SugerenciaPromocion> sugerencias;
    private String fuente;

    public TendenciaDetectadaEvent(String usuario, List<SugerenciaPromocion> sugerencias, String fuente) {
        super(usuario);
        this.sugerencias = sugerencias;
        this.fuente = fuente;
    }

    public List<SugerenciaPromocion> sugerencias() {
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
