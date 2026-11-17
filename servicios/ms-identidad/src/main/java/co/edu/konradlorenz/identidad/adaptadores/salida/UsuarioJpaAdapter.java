package co.edu.konradlorenz.identidad.adaptadores.salida;

import co.edu.konradlorenz.identidad.dominio.Administrador;
import co.edu.konradlorenz.identidad.dominio.AuxiliarCompras;
import co.edu.konradlorenz.identidad.dominio.DirectorComercial;
import co.edu.konradlorenz.identidad.dominio.DirectorCompras;
import co.edu.konradlorenz.identidad.dominio.JefeCocina;
import co.edu.konradlorenz.identidad.dominio.Mesero;
import co.edu.konradlorenz.identidad.dominio.Sucursal;
import co.edu.konradlorenz.identidad.dominio.Usuario;
import co.edu.konradlorenz.identidad.puertos.salida.UsuarioRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guarda los usuarios en ms_identidad (PostgreSQL). Al leer, la columna rol decide qué subclase de
 * Usuario se arma, y con ella qué menú construye.
 */
@Repository
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarios;
    private final SucursalJpaRepository sucursales;

    public UsuarioJpaAdapter(UsuarioJpaRepository usuarios, SucursalJpaRepository sucursales) {
        this.usuarios = usuarios;
        this.sucursales = sucursales;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarios.findByCorreo(correo).map(UsuarioJpaAdapter::aDominio).orElse(null);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario u) {
        SucursalEntidad s = sucursales.findById(u.sucursal().id())
                .orElseThrow(() -> new IllegalArgumentException("La sucursal " + u.sucursal().id() + " no existe."));
        UsuarioEntidad e = u.id() == null ? new UsuarioEntidad() : usuarios.findById(u.id()).orElseGet(UsuarioEntidad::new);
        e.nombre = u.nombre();
        e.correo = u.correo();
        e.passwordHash = u.passwordHash();
        e.rol = u.rol();
        e.sucursal = s;
        return aDominio(usuarios.save(e));
    }

    private static Usuario aDominio(UsuarioEntidad e) {
        Sucursal s = new Sucursal(e.sucursal.id, e.sucursal.nombre);
        Usuario u = switch (e.rol) {
            case "MESERO" -> new Mesero(e.id, e.nombre, e.correo, s);
            case "JEFE_COCINA" -> new JefeCocina(e.id, e.nombre, e.correo, s);
            case "AUXILIAR_COMPRAS" -> new AuxiliarCompras(e.id, e.nombre, e.correo, s);
            case "DIRECTOR_COMPRAS" -> new DirectorCompras(e.id, e.nombre, e.correo, s);
            case "DIRECTOR_COMERCIAL" -> new DirectorComercial(e.id, e.nombre, e.correo, s);
            case "ADMINISTRADOR" -> new Administrador(e.id, e.nombre, e.correo, s);
            default -> throw new IllegalStateException("Rol desconocido en la base: " + e.rol);
        };
        u.asignarPasswordHash(e.passwordHash);
        return u;
    }

}
