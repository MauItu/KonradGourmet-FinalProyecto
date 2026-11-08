package co.edu.konradlorenz.analitica.puertos.salida;

import co.edu.konradlorenz.analitica.dominio.HechoNegocio;

import java.time.LocalDate;
import java.util.List;

public interface KpiRepositoryPort {

    void guardar(HechoNegocio h);

    List<HechoNegocio> hechos(LocalDate desde, LocalDate hasta, Long sucursalId);

}
