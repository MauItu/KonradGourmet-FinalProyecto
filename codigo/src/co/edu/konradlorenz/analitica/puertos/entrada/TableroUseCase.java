package co.edu.konradlorenz.analitica.puertos.entrada;

import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;

import java.time.LocalDate;
import java.util.List;

public interface TableroUseCase {

    List<ResultadoKpi> kpis(LocalDate desde, LocalDate hasta, Long sucursalId);

    void registrarHecho(HechoNegocio h);

    void detectarTendencias();

    List<SugerenciaPromocion> sugerencias(LocalDate desde, LocalDate hasta);

    void aceptarSugerencia(Long id);

    void descartarSugerencia(Long id);

}
