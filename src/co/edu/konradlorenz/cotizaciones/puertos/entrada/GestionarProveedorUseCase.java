package co.edu.konradlorenz.cotizaciones.puertos.entrada;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;

import java.util.List;

public interface GestionarProveedorUseCase {

    Proveedor registrar(Proveedor p);

    List<Proveedor> consultar(boolean soloActivos);

    Proveedor detalle(Long id);

    Proveedor editar(Long id, Proveedor p);

    void inactivar(Long id);

}
