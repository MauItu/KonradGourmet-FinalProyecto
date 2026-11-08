package co.edu.konradlorenz.cotizaciones.aplicacion;

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
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Proveedor> consultar(boolean soloActivos) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Proveedor detalle(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Proveedor editar(Long id, Proveedor p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void inactivar(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
