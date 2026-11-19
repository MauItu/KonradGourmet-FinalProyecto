package co.edu.konradlorenz.analitica.aplicacion;

import co.edu.konradlorenz.analitica.dominio.CalculadorKpi;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ProveedorMejoresPrecios;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.eventos.TendenciaDetectadaEvent;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.analitica.puertos.salida.KpiRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.TendenciasPort;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TableroService implements TableroUseCase {

    // PD-03: sin rango, la demanda se mide sobre el último mes y el proveedor sobre el último año
    private static final int DIAS_DEMANDA = 30;
    private static final int DIAS_PROVEEDOR = 365;
    // PD-04 (supuesto S11): el análisis mira los 7 días anteriores, sugiere los 3 más pedidos y cada sugerencia vale 7 días
    private static final int DIAS_ANALISIS = 7;
    private static final int MAS_PEDIDOS = 3;
    private static final int DIAS_VIGENCIA = 7;

    static final String MOTIVO_REDES = "Tendencia en redes";
    static final String MOTIVO_PEDIDOS = "Entre los 3 más pedidos de la semana";
    static final String MOTIVO_LOS_DOS = "Tendencia en redes y entre los 3 más pedidos de la semana";
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    /**
     * Nota del diagrama (n_kpis): sin rango, la demanda (producto y sucursal) usa los últimos 30 días y el proveedor
     * los últimos 365; con rango, los tres usan ese rango. La sucursal filtra solo la demanda: las compras son
     * centrales. Cada resultado dice el periodo que se aplicó (DEC-88).
     */
    @Override
    public List<ResultadoKpi> kpis(LocalDate desde, LocalDate hasta, Long sucursalId) {
        validarRango(desde, hasta);
        LocalDate hoy = LocalDate.now();
        boolean conRango = desde != null;
        LocalDate fin = conRango ? hasta : hoy;
        LocalDate inicioDemanda = conRango ? desde : hoy.minusDays(DIAS_DEMANDA - 1);
        LocalDate inicioProveedor = conRango ? desde : hoy.minusDays(DIAS_PROVEEDOR - 1);
        List<HechoNegocio> demanda = repositorio.hechos(inicioDemanda, fin, sucursalId);
        List<HechoNegocio> precios = repositorio.hechos(inicioProveedor, fin, null);
        // Strategy: cada KPI es un algoritmo intercambiable sobre los hechos de su periodo
        return estrategias.stream()
                .map(e -> midePrecios(e)
                        ? new ResultadoKpi(e.calcular(precios), inicioProveedor, fin)
                        : new ResultadoKpi(e.calcular(demanda), inicioDemanda, fin))
                .toList();
    }

    /** El proveedor con mejores precios se mide sobre el último año y sin sucursal (PD-03). */
    private static boolean midePrecios(CalculadorKpi e) {
        return e instanceof ProveedorMejoresPrecios;
    }

    @Override
    public void registrarHecho(HechoNegocio h) {
        repositorio.guardar(h);
    }

    /**
     * Nota del diagrama (n_promociones): analiza los 7 días anteriores con los productos en tendencia de las redes y
     * los 3 más pedidos; guarda cada sugerencia con producto, motivo y 7 días de vigencia, y publica
     * tendencia.detectada solo si hoy no se notificó (notificadaEl). Si las redes no responden, la excepción sube a
     * TendenciasJob, que la deja en el log. Una segunda ejecución el mismo día no repite las sugerencias.
     */
    @Override
    public void detectarTendencias() {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(DIAS_ANALISIS);
        LocalDate hasta = hoy.minusDays(1);
        List<String> enRedes = redes.productosEnTendencia();
        List<String> masPedidos = masPedidos(repositorio.hechos(desde, hasta, null));

        Map<String, String> nombres = new LinkedHashMap<>();
        Map<String, String> motivos = new LinkedHashMap<>();
        for (String p : enRedes) {
            if (p != null && !p.isBlank()) {
                nombres.putIfAbsent(clave(p), p.trim());
                motivos.put(clave(p), MOTIVO_REDES);
            }
        }
        for (String p : masPedidos) {
            nombres.putIfAbsent(clave(p), p);
            motivos.merge(clave(p), MOTIVO_PEDIDOS, (antes, nuevo) -> MOTIVO_LOS_DOS);
        }

        Set<String> yaSugeridos = new HashSet<>();
        for (SugerenciaPromocion s : sugerencias.historico(hoy, hoy)) {
            if (s.vigenciaDesde().equals(hoy)) {
                yaSugeridos.add(clave(s.producto()));
            }
        }
        boolean notificadaHoy = sugerencias.notificadaEl(hoy);
        List<SugerenciaPromocion> nuevas = new ArrayList<>();
        for (Map.Entry<String, String> m : motivos.entrySet()) {
            if (yaSugeridos.contains(m.getKey())) {
                continue;
            }
            SugerenciaPromocion s = new SugerenciaPromocion(nombres.get(m.getKey()), m.getValue(), hoy, hoy.plusDays(DIAS_VIGENCIA - 1));
            sugerencias.guardar(s);
            nuevas.add(s);
        }
        if (nuevas.isEmpty()) {
            return;
        }
        eventos.publicar(new AuditoriaEvent("sistema", "Promociones sugeridas: "
                + String.join(", ", nuevas.stream().map(SugerenciaPromocion::producto).toList())));
        if (!notificadaHoy) {
            // HU-19: no más de una notificación de promociones por día
            eventos.publicar(new TendenciaDetectadaEvent("sistema", nuevas.stream().map(TableroService::linea).toList(),
                    "Redes sociales y pedidos del " + DIA.format(desde) + " al " + DIA.format(hasta)));
        }
    }

    /** Los productos más pedidos (hechos DEMANDA_PRODUCTO), de mayor a menor; en un empate, el que apareció primero. */
    private static List<String> masPedidos(List<HechoNegocio> hechos) {
        Map<String, BigDecimal> total = new LinkedHashMap<>();
        for (HechoNegocio h : hechos) {
            if ("DEMANDA_PRODUCTO".equals(h.tipo())) {
                total.merge(h.clave(), h.valor(), BigDecimal::add);
            }
        }
        return total.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(MAS_PEDIDOS)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static String clave(String producto) {
        return producto.trim().toLowerCase(Locale.ROOT);
    }

    /** La línea del correo: «producto · motivo · del dd/mm/aaaa al dd/mm/aaaa» (PD-05). */
    static String linea(SugerenciaPromocion s) {
        return s.producto() + " · " + s.motivo() + " · del " + DIA.format(s.vigenciaDesde()) + " al " + DIA.format(s.vigenciaHasta());
    }

    /** El histórico del jefe de cocina: las sugerencias vigentes en algún día del rango (sin rango, todas), las más recientes primero. */
    @Override
    public List<SugerenciaPromocion> sugerencias(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        return sugerencias.historico(desde, hasta).stream()
                .sorted(Comparator.comparing(SugerenciaPromocion::vigenciaDesde).reversed()
                        .thenComparing(SugerenciaPromocion::id, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public void aceptarSugerencia(Long id) {
        SugerenciaPromocion s = buscar(id);
        s.aceptar();
        sugerencias.guardar(s);
        eventos.publicar(new AuditoriaEvent("sistema", "Promoción sugerida " + s.producto() + " aceptada"));
    }

    @Override
    public void descartarSugerencia(Long id) {
        SugerenciaPromocion s = buscar(id);
        s.descartar();
        sugerencias.guardar(s);
        eventos.publicar(new AuditoriaEvent("sistema", "Promoción sugerida " + s.producto() + " descartada"));
    }

    private SugerenciaPromocion buscar(Long id) {
        SugerenciaPromocion s = id == null ? null : sugerencias.porId(id);
        if (s == null) {
            throw new IllegalArgumentException("No existe la sugerencia " + id);
        }
        return s;
    }

    /** El rango va completo o no va, y «desde» no puede ser posterior a «hasta» (PD-10). */
    private static void validarRango(LocalDate desde, LocalDate hasta) {
        if ((desde == null) != (hasta == null)) {
            throw new IllegalArgumentException("Seleccione la fecha inicial y la fecha final.");
        }
        if (desde != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

}
