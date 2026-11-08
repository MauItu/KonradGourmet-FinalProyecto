package co.edu.konradlorenz.analitica.dominio;

import java.time.LocalDate;

public class SugerenciaPromocion {

    private Long id;
    private String producto;
    private String motivo;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private EstadoSugerencia estado;

    public void aceptar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void descartar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
