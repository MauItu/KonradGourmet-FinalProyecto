package co.edu.konradlorenz.analitica.aplicacion;

import co.edu.konradlorenz.analitica.dominio.CalculadorKpi;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.analitica.puertos.salida.KpiRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.TendenciasPort;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;

import java.time.LocalDate;
import java.util.List;

public class TableroService implements TableroUseCase {

    private final List<CalculadorKpi> estrategias;
    private final KpiRepositoryPort repositorio;
    private final TendenciasPort redes;
    private final EventPublisherPort eventos;
    private final SugerenciaRepositoryPort sugerencias;

    public TableroService(List<CalculadorKpi> estrategias, KpiRepositoryPort repositorio, TendenciasPort redes, EventPublisherPort eventos, SugerenciaRepositoryPort sugerencias) {
        this.estrategias = estrategias;
        this.repositorio = repositorio;
        this.redes = redes;
        this.eventos = eventos;
        this.sugerencias = sugerencias;
    }

    @Override
    public List<ResultadoKpi> kpis(LocalDate desde, LocalDate hasta, Long sucursalId) {
        List<HechoNegocio> hechos = repositorio.hechos(desde, hasta, sucursalId);
        // Strategy: cada KPI es un algoritmo intercambiable sobre los mismos hechos
        return estrategias.stream().map(e -> e.calcular(hechos)).toList();
    }

    @Override
    public void registrarHecho(HechoNegocio h) {
        repositorio.guardar(h);
    }

    @Override
    public void detectarTendencias() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<SugerenciaPromocion> sugerencias(LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void aceptarSugerencia(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void descartarSugerencia(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
