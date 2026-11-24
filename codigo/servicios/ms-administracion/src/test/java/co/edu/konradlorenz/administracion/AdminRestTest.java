package co.edu.konradlorenz.administracion;

import co.edu.konradlorenz.administracion.adaptadores.entrada.AdminRestController;
import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Las rutas de /api/admin con sus roles (PD-06, DEC-86), los mensajes de PD-10 y las respuestas de cada ventana. */
@WebMvcTest(AdminRestController.class)
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class AdminRestTest {

    private static final String PAULA = "paula.herrera@konradgourmet.com";

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AdministracionUseCase casoDeUso;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    private static RequestPostProcessor como(String rol, String correo) {
        return jwt().jwt(j -> j.subject("6").claim("correo", correo).claim("rol", rol).claim("sucursalId", 1))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static RequestPostProcessor admin() {
        return como("ADMINISTRADOR", PAULA);
    }

    @Test
    void elAdministradorConsultaYCambiaLosParametros() throws Exception {
        when(casoDeUso.parametros()).thenReturn(Map.of("general.moneda", "COP", "tema.nombre", "Konrad Gourmet"));
        mvc.perform(get("/api/admin/parametros").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['general.moneda']").value("COP"));
        mvc.perform(put("/api/admin/parametros/tema.color-primario").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"#1F7A4D\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Parámetro actualizado."));
        verify(casoDeUso).parametrizar("tema.color-primario", "#1F7A4D");

        doThrow(new IllegalArgumentException("El valor del parámetro general.moneda es obligatorio."))
                .when(casoDeUso).parametrizar("general.moneda", "");
        mvc.perform(put("/api/admin/parametros/general.moneda").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El valor del parámetro general.moneda es obligatorio."));
    }

    @Test
    void soloElAdministradorParametrizaPeroTodosLeenElTema() throws Exception {
        mvc.perform(get("/api/admin/parametros").with(como("DIRECTOR_COMPRAS", "laura.gomez@konradgourmet.com")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/parametros/tema.nombre").with(como("JEFE_COCINA", "carlos.mendoza@konradgourmet.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"Otro\"}"))
                .andExpect(status().isForbidden());
        verify(casoDeUso, never()).parametrizar(any(), any());

        when(casoDeUso.tema()).thenReturn(Map.of("tema.color-primario", "#F28705"));
        for (String rol : List.of("MESERO", "JEFE_COCINA", "AUXILIAR_COMPRAS", "DIRECTOR_COMPRAS", "DIRECTOR_COMERCIAL", "ADMINISTRADOR")) {
            mvc.perform(get("/api/admin/parametros/tema").with(como(rol, "alguien@konradgourmet.com")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$['tema.color-primario']").value("#F28705"));
        }
        mvc.perform(get("/api/admin/parametros/tema")).andExpect(status().isUnauthorized());
    }

    @Test
    void laAuditoriaPideElRangoYSoloLaVeElAdministrador() throws Exception {
        LocalDate hoy = LocalDate.of(2026, 11, 17);
        when(casoDeUso.auditoria(hoy, hoy)).thenReturn(List.of(
                new RegistroAuditoria("Orden OC-0012 generada", "laura.gomez@konradgourmet.com", hoy, LocalTime.of(16, 5, 9, 123_456_000))));
        mvc.perform(get("/api/admin/auditoria?desde=2026-11-17&hasta=2026-11-17").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accion").value("Orden OC-0012 generada"))
                .andExpect(jsonPath("$[0].usuario").value("laura.gomez@konradgourmet.com"))
                .andExpect(jsonPath("$[0].fecha").value("2026-11-17"))
                .andExpect(jsonPath("$[0].hora").value("16:05:09"));

        when(casoDeUso.auditoria(null, null)).thenThrow(new IllegalArgumentException("Seleccione la fecha inicial y la fecha final."));
        mvc.perform(get("/api/admin/auditoria").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Seleccione la fecha inicial y la fecha final."));
        mvc.perform(get("/api/admin/auditoria?desde=17/11/2026&hasta=2026-11-17").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha no es válida."));
        mvc.perform(get("/api/admin/auditoria?desde=2026-11-17&hasta=2026-11-17").with(como("DIRECTOR_COMERCIAL", "andres.rojas@konradgourmet.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void losLogsFiltranPorServicioYAvisanSiElasticsearchNoResponde() throws Exception {
        when(casoDeUso.logs("ms-pedidos")).thenReturn(List.of(
                new LogError("ms-pedidos", "No se pudo guardar el pedido", LocalDateTime.of(2026, 11, 17, 10, 30, 15, 999_000_000))));
        mvc.perform(get("/api/admin/logs?servicio=ms-pedidos").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].servicio").value("ms-pedidos"))
                .andExpect(jsonPath("$[0].fechaHora").value("2026-11-17T10:30:15"));
        mvc.perform(get("/api/admin/logs").with(admin())).andExpect(status().isOk());
        verify(casoDeUso).logs(null);

        when(casoDeUso.logs("ms-ordenes")).thenThrow(new ResourceAccessException("Connection refused"));
        mvc.perform(get("/api/admin/logs?servicio=ms-ordenes").with(admin()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("No se pudieron consultar los logs de errores. Intente de nuevo en unos minutos."));
        mvc.perform(get("/api/admin/logs").with(como("MESERO", "isabella.torres@konradgourmet.com"))).andExpect(status().isForbidden());
    }

}
