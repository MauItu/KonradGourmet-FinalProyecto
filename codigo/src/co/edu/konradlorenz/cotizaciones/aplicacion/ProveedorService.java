package co.edu.konradlorenz.cotizaciones.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarProveedorUseCase;
import co.edu.konradlorenz.cotizaciones.puertos.salida.ProveedorRepositoryPort;

import java.util.List;

/**
 * Nota del diagrama: Proveedores (HU-15): registrar() rechaza un NIT repetido (existeNit) y
 * Proveedor.validar() exige NIT y un correo válido. Inactivar no borra: el proveedor deja de salir en
 * el desplegable de V13 (consultar con soloActivos = true). Cada acción publica auditoria.evento.
 */
public class ProveedorService implements GestionarProveedorUseCase {

    private final ProveedorRepositoryPort repositorio;
    private final EventPublisherPort eventos;

    public ProveedorService(ProveedorRepositoryPort repositorio, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    @Override
    public Proveedor registrar(Proveedor p) {
        p.validar();
        if (repositorio.existeNit(p.nit())) {
            throw new IllegalStateException("Ya existe un proveedor con el NIT " + p.nit());
        }
        Proveedor guardado = repositorio.guardar(p);
        eventos.publicar(new AuditoriaEvent("sistema", "Proveedor registrado: " + guardado.razonSocial()));
        return guardado;
    }

    @Override
    public List<Proveedor> consultar(boolean soloActivos) {
        return soloActivos ? repositorio.activos() : repositorio.todos();
    }

    @Override
    public Proveedor detalle(Long id) {
        Proveedor p = repositorio.porId(id);
        if (p == null) {
            throw new IllegalArgumentException("No existe el proveedor " + id);
        }
        return p;
    }

    @Override
    public Proveedor editar(Long id, Proveedor p) {
        Proveedor existente = detalle(id);
        p.validar();
        if (!existente.nit().equals(p.nit()) && repositorio.existeNit(p.nit())) {
            throw new IllegalStateException("Ya existe un proveedor con el NIT " + p.nit());
        }
        existente.editar(p);
        Proveedor guardado = repositorio.guardar(existente);
        eventos.publicar(new AuditoriaEvent("sistema", "Proveedor editado: " + guardado.razonSocial()));
        return guardado;
    }

    @Override
    public void inactivar(Long id) {
        Proveedor p = detalle(id);
        p.inactivar();
        repositorio.guardar(p);
        eventos.publicar(new AuditoriaEvent("sistema", "Proveedor inactivado: " + p.razonSocial()));
    }

}
