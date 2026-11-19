package co.edu.konradlorenz.identidad;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.infraestructura.ConfiguracionCompartida;
import co.edu.konradlorenz.compartido.infraestructura.FormatoEventos;
import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.identidad.adaptadores.entrada.UsuarioRestController;
import co.edu.konradlorenz.identidad.aplicacion.RegistrarUsuarioService;
import co.edu.konradlorenz.identidad.dominio.Mesero;
import co.edu.konradlorenz.identidad.dominio.PoliticaContrasena;
import co.edu.konradlorenz.identidad.dominio.Sucursal;
import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;
import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DEC-64: cuando la administradora registra un usuario, auditoria.evento queda a su nombre y con el
 * correlacionId de la petición (el que puso el gateway), aunque RegistrarUsuarioService publica «sistema».
 * Corre el controlador, la seguridad y el publicador de Kafka reales; solo la base y el broker son simulados.
 */
@WebMvcTest(UsuarioRestController.class)
@ImportAutoConfiguration({ConfiguracionCompartida.class, SeguridadServicio.class, ManejoErrores.class})
class AltaUsuarioAuditadaTest {

    @TestConfiguration
    static class Nucleo {
        @Bean
        RegistrarUsuarioUseCase registrarUsuario(UsuarioRepositoryPort usuarios, EventPublisherPort eventos) {
            CifradorPort cifrador = mock(CifradorPort.class);
            when(cifrador.cifrar(any())).thenReturn("$2a$hash");
            return new RegistrarUsuarioService(usuarios, cifrador, new PoliticaContrasena(), eventos);
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private FormatoEventos formato;
    @MockitoBean
    private UsuarioRepositoryPort usuarios;
    @MockitoBean
    private KafkaTemplate<String, String> kafka;

    @Test
    @SuppressWarnings("unchecked")
    void elAltaQuedaANombreDeQuienLaHizo() throws Exception {
        when(usuarios.guardar(any(Usuario.class))).thenAnswer(i -> new Mesero(7L, "Juan Pérez", "juan.perez@konradgourmet.com", new Sucursal(1L, null)));
        UUID recorrido = UUID.randomUUID();

        mvc.perform(post("/api/usuarios")
                        .with(jwt().jwt(j -> j.subject("6").claim("correo", "paula.herrera@konradgourmet.com").claim("rol", "ADMINISTRADOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                        .header("X-Correlation-Id", recorrido.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan Pérez\",\"correo\":\"Juan.Perez@konradgourmet.com\",\"rol\":\"MESERO\",\"sucursalId\":1,\"clave\":\"Konrad2026\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", recorrido.toString()));

        ArgumentCaptor<ProducerRecord<String, String>> enviado = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka).send(enviado.capture());
        AuditoriaEvent auditoria = formato.deJson(enviado.getValue().value(), AuditoriaEvent.class);
        assertEquals("auditoria.evento", enviado.getValue().topic());
        assertEquals("paula.herrera@konradgourmet.com", auditoria.usuario());
        assertEquals(recorrido, auditoria.correlacionId());
        assertEquals("Usuario registrado: juan.perez@konradgourmet.com (MESERO)", auditoria.accion());
    }

    @Test
    void otroRolNoPuedeRegistrarUsuarios() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .with(jwt().jwt(j -> j.claim("correo", "isabella.torres@konradgourmet.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_MESERO")))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

}
