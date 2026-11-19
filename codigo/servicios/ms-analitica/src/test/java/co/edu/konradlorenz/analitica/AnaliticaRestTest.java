package co.edu.konradlorenz.analitica;

import co.edu.konradlorenz.analitica.adaptadores.entrada.PromocionRestController;
import co.edu.konradlorenz.analitica.adaptadores.entrada.TableroRestController;
import co.edu.konradlorenz.analitica.dominio.EstadoSugerencia;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.puertos.entrada.TableroUseCase;
import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Las rutas de /api/bam con sus roles: el tablero del director comercial (HU-18) y las promociones del jefe de cocina (HU-19). */
@WebMvcTest({TableroRestController.class, PromocionRestController.class})
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class AnaliticaRestTest {

    private static final String ANDRES = "andres.rojas@konradgourmet.com";
    private static final String CARLOS = "carlos.mendoza@konradgourmet.com";
    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private TableroUseCase casoDeUso;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    private static RequestPostProcessor como(String rol, String correo) {
        return jwt().jwt(j -> j.subject("5").claim("correo", correo).claim("rol", rol).claim("sucursalId", 1))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static RequestPostProcessor director() {
        return como("DIRECTOR_COMERCIAL", ANDRES);
    }

    private static RequestPostProcessor jefe() {
        return como("JEFE_COCINA", CARLOS);
    }

    @Test
    void elDirectorComercialVeLosTresIndicadoresConSuRankingYSuPeriodo() throws Exception {
        Map<String, BigDecimal> ranking = new LinkedHashMap<>();
        ranking.put("Bandeja paisa", new BigDecimal("667"));
        ranking.put("Ajiaco", new BigDecimal("593"));
        when(casoDeUso.kpis(null, null, null)).thenReturn(List.of(
                new ResultadoKpi(new ResultadoKpi("Producto con mayor demanda", "Bandeja paisa", new BigDecimal("667"), ranking), HOY.minusDays(29), HOY),
                new ResultadoKpi(new ResultadoKpi("Sucursal con mayor demanda", null, BigDecimal.ZERO), HOY.minusDays(29), HOY)));
        mvc.perform(get("/api/bam/kpis").with(director()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ganador").value("Bandeja paisa"))
                .andExpect(jsonPath("$[0].valor").value(667))
                .andExpect(jsonPath("$[0].ranking[1].clave").value("Ajiaco"))
                .andExpect(jsonPath("$[0].desde").value("2026-10-20"))
                .andExpect(jsonPath("$[0].hasta").value("2026-11-18"))
                .andExpect(jsonPath("$[1].ganador").doesNotExist())
                .andExpect(jsonPath("$[1].valor").doesNotExist());
    }

    @Test
    void elRangoYLaSucursalLleganAlCasoDeUsoYSusErroresSonLosDePd10() throws Exception {
        when(casoDeUso.kpis(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 2L)).thenReturn(List.of());
        mvc.perform(get("/api/bam/kpis?desde=2026-09-01&hasta=2026-09-30&sucursalId=2").with(director())).andExpect(status().isOk());
        verify(casoDeUso).kpis(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 2L);

        when(casoDeUso.kpis(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1), null))
                .thenThrow(new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final."));
        mvc.perform(get("/api/bam/kpis?desde=2026-09-30&hasta=2026-09-01").with(director()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha inicial no puede ser posterior a la fecha final."));
        mvc.perform(get("/api/bam/kpis?desde=30-09-2026&hasta=2026-10-01").with(director()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha no es válida."));
        mvc.perform(get("/api/bam/kpis?sucursalId=centro").with(director()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La sucursal no es válida."));
    }

    @Test
    void cadaVentanaEsDeSuRol() throws Exception {
        mvc.perform(get("/api/bam/kpis").with(jefe())).andExpect(status().isForbidden());
        mvc.perform(get("/api/bam/kpis").with(como("ADMINISTRADOR", "paula.herrera@konradgourmet.com"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/bam/promociones").with(director())).andExpect(status().isForbidden());
        mvc.perform(patch("/api/bam/promociones/1/aceptar").with(como("MESERO", "isabella.torres@konradgourmet.com"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/bam/kpis")).andExpect(status().isUnauthorized());
        verify(casoDeUso, never()).kpis(any(), any(), any());
        verify(casoDeUso, never()).aceptarSugerencia(any());
    }

    @Test
    void elJefeConsultaElHistoricoYAceptaODescarta() throws Exception {
        when(casoDeUso.sugerencias(null, null)).thenReturn(List.of(
                new SugerenciaPromocion(7L, "Ajiaco", "Tendencia en redes", HOY, HOY.plusDays(6), EstadoSugerencia.SUGERIDA)));
        mvc.perform(get("/api/bam/promociones").with(jefe()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].producto").value("Ajiaco"))
                .andExpect(jsonPath("$[0].motivo").value("Tendencia en redes"))
                .andExpect(jsonPath("$[0].vigenciaHasta").value("2026-11-24"))
                .andExpect(jsonPath("$[0].estado").value("SUGERIDA"));
        mvc.perform(patch("/api/bam/promociones/7/aceptar").with(jefe()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sugerencia aceptada."));
        verify(casoDeUso).aceptarSugerencia(7L);
        mvc.perform(patch("/api/bam/promociones/8/descartar").with(jefe()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sugerencia descartada."));
        verify(casoDeUso).descartarSugerencia(8L);
    }

    @Test
    void unaSugerenciaResueltaDa409YUnaQueNoExiste404() throws Exception {
        doThrow(new IllegalStateException("Esta sugerencia ya fue aceptada o descartada.")).when(casoDeUso).aceptarSugerencia(7L);
        mvc.perform(patch("/api/bam/promociones/7/aceptar").with(jefe()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Esta sugerencia ya fue aceptada o descartada."));
        doThrow(new IllegalArgumentException("No existe la sugerencia 99")).when(casoDeUso).descartarSugerencia(99L);
        mvc.perform(patch("/api/bam/promociones/99/descartar").with(jefe()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la sugerencia 99"));
        when(casoDeUso.sugerencias(HOY, HOY.minusDays(1)))
                .thenThrow(new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final."));
        mvc.perform(get("/api/bam/promociones?desde=2026-11-18&hasta=2026-11-17").with(jefe()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha inicial no puede ser posterior a la fecha final."));
    }

}
