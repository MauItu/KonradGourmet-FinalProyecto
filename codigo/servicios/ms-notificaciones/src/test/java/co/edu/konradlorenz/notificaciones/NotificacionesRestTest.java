package co.edu.konradlorenz.notificaciones;

import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import co.edu.konradlorenz.notificaciones.adaptadores.entrada.DestinatarioRestController;
import co.edu.konradlorenz.notificaciones.configuracion.EnviosController;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La libreta de destinatarios (solo el administrador) y la consulta de envíos de V14 (DEC-76), con sus roles. */
@WebMvcTest({DestinatarioRestController.class, EnviosController.class})
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class NotificacionesRestTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private NotificarUseCase casoDeUso;
    @MockitoBean
    private JdbcTemplate jdbc;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    private static RequestPostProcessor como(String rol, String correo) {
        return jwt().jwt(j -> j.subject("1").claim("correo", correo).claim("rol", rol).claim("sucursalId", 1))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Test
    void elAdministradorConsultaYRegistraDestinatarios() throws Exception {
        when(casoDeUso.destinatarios(1L)).thenReturn(List.of("carlos.mendoza@konradgourmet.com"));
        mvc.perform(get("/api/notificaciones/destinatarios?sucursalId=1").with(como("ADMINISTRADOR", "paula.herrera@konradgourmet.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("carlos.mendoza@konradgourmet.com"));
        mvc.perform(post("/api/notificaciones/destinatarios").with(como("ADMINISTRADOR", "paula.herrera@konradgourmet.com"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sucursalId\":2,\"correo\":\" Jefe.Norte@KonradGourmet.com \"}"))
                .andExpect(status().isCreated());
        verify(casoDeUso).registrarDestinatario(2L, "jefe.norte@konradgourmet.com");
        mvc.perform(get("/api/notificaciones/destinatarios?sucursalId=1").with(como("DIRECTOR_COMPRAS", "laura.gomez@konradgourmet.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void losEnviosDeUnRecorridoLosVenComprasYElAdministrador() throws Exception {
        mvc.perform(get("/api/notificaciones/envios?correlacion=6f1c1b7e-3f0a-4a8e-9a43-1d2f0b8e5c11")
                .with(como("AUXILIAR_COMPRAS", "isabella.castro@konradgourmet.com"))).andExpect(status().isOk());
        mvc.perform(get("/api/notificaciones/envios?correlacion=no-es-un-uuid")
                        .with(como("AUXILIAR_COMPRAS", "isabella.castro@konradgourmet.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La correlación no es válida"));
        mvc.perform(get("/api/notificaciones/envios?correlacion=6f1c1b7e-3f0a-4a8e-9a43-1d2f0b8e5c11")
                .with(como("JEFE_COCINA", "carlos.mendoza@konradgourmet.com"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/notificaciones/envios?correlacion=6f1c1b7e-3f0a-4a8e-9a43-1d2f0b8e5c11")).andExpect(status().isUnauthorized());
    }

}
