package co.edu.konradlorenz.menu;

import co.edu.konradlorenz.compartido.infraestructura.ManejoErrores;
import co.edu.konradlorenz.compartido.infraestructura.SeguridadServicio;
import co.edu.konradlorenz.menu.adaptadores.entrada.PlatoRestController;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * C7-1: cada jefe de cocina gestiona solo los platos de su sucursal (la sucursal sale del JWT). Un plato
 * de otra sucursal responde 404, como si no existiera, y el caso de uso no se llama.
 */
@WebMvcTest(PlatoRestController.class)
@ImportAutoConfiguration({SeguridadServicio.class, ManejoErrores.class})
class PlatoSucursalTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RegistrarPlatoUseCase casoDeUso;

    private static RequestPostProcessor como(String rol, long sucursal) {
        return jwt().jwt(j -> j.subject("2").claim("correo", "jefe@konradgourmet.com").claim("rol", rol).claim("sucursalId", sucursal))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @BeforeEach
    void ajiacoDelCentro() {
        Plato ajiaco = new Plato(1L, 2L, "Ajiaco", "Sopas", "", new BigDecimal("18000"));
        ajiaco.asignarId(7L);
        when(casoDeUso.consultar(1L)).thenReturn(List.of(ajiaco));
        when(casoDeUso.consultar(2L)).thenReturn(List.of());
    }

    @Test
    void elJefeDeOtraSucursalNoDesactivaNiReactivaNiEditaNiCambiaLaImagen() throws Exception {
        mvc.perform(patch("/api/platos/7/desactivar").with(como("JEFE_COCINA", 2))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El plato consultado no está disponible"));
        mvc.perform(patch("/api/platos/7/reactivar").with(como("JEFE_COCINA", 2))).andExpect(status().isNotFound());
        mvc.perform(put("/api/platos/7").with(como("JEFE_COCINA", 2)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ajiaco\",\"categoria\":\"Sopas\",\"precioVenta\":1}")).andExpect(status().isNotFound());
        mvc.perform(put("/api/platos/7/imagen").with(como("JEFE_COCINA", 2)).contentType(MediaType.IMAGE_PNG)
                .content(new byte[]{(byte) 0x89, 'P', 'N', 'G'})).andExpect(status().isNotFound());
        mvc.perform(get("/api/platos/7/imagen").with(como("MESERO", 2))).andExpect(status().isNotFound());
        verify(casoDeUso, never()).desactivar(anyLong());
        verify(casoDeUso, never()).reactivar(anyLong());
        verify(casoDeUso, never()).editar(anyLong(), any());
        verify(casoDeUso, never()).guardarImagen(anyLong(), any(), any());
        verify(casoDeUso, never()).imagen(anyLong());
    }

    @Test
    void elJefeDeLaSucursalSiLoDesactivaYLoReactiva() throws Exception {
        mvc.perform(patch("/api/platos/7/desactivar").with(como("JEFE_COCINA", 1))).andExpect(status().isNoContent());
        mvc.perform(patch("/api/platos/7/reactivar").with(como("JEFE_COCINA", 1))).andExpect(status().isNoContent());
        verify(casoDeUso).desactivar(7L);
        verify(casoDeUso).reactivar(7L);
    }

}
