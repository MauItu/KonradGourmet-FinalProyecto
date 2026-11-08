package co.edu.konradlorenz.analitica.eventos;

import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.List;

public class TendenciaDetectadaEvent extends EventoDominio {

    private List<SugerenciaPromocion> sugerencias;
    private String fuente;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
