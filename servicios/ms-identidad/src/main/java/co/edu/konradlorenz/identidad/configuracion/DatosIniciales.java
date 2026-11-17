package co.edu.konradlorenz.identidad.configuracion;

import co.edu.konradlorenz.identidad.puertos.entrada.RegistrarUsuarioUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Si la base está vacía, registra un usuario por rol con el mismo caso de uso que usa el administrador
 * (así cumplen la política de contraseña y quedan en la auditoría). Los cuatro primeros son los de los
 * mockups; el director comercial y la administradora son supuestos (DEC-63).
 */
@Component
public class DatosIniciales implements ApplicationRunner {

    private static final String[][] USUARIOS = {
            {"Isabella Torres", "isabella.torres@konradgourmet.com", "MESERO"},
            {"Carlos Mendoza", "carlos.mendoza@konradgourmet.com", "JEFE_COCINA"},
            {"Isabella Castro", "isabella.castro@konradgourmet.com", "AUXILIAR_COMPRAS"},
            {"Laura Gómez", "laura.gomez@konradgourmet.com", "DIRECTOR_COMPRAS"},
            {"Andrés Rojas", "andres.rojas@konradgourmet.com", "DIRECTOR_COMERCIAL"},
            {"Paula Herrera", "paula.herrera@konradgourmet.com", "ADMINISTRADOR"},
    };

    private final RegistrarUsuarioUseCase registro;
    private final JdbcTemplate db;
    private final String clave;

    public DatosIniciales(RegistrarUsuarioUseCase registro, JdbcTemplate db,
                          @Value("${konrad.datos-iniciales.clave:Konrad2026}") String clave) {
        this.registro = registro;
        this.db = db;
        this.clave = clave;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer hay = db.queryForObject("SELECT count(*) FROM usuario", Integer.class);
        if (hay != null && hay > 0) {
            return;
        }
        for (String[] u : USUARIOS) {
            registro.registrar(u[0], u[1], u[2], 1L, clave);
        }
    }

}
