package co.edu.konradlorenz.analitica.puertos.salida;

import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;

import java.time.LocalDate;
import java.util.List;

public interface SugerenciaRepositoryPort {

    void guardar(SugerenciaPromocion s);

    SugerenciaPromocion porId(Long id);

    List<SugerenciaPromocion> historico(LocalDate desde, LocalDate hasta);

    boolean notificadaEl(LocalDate fecha);

}
