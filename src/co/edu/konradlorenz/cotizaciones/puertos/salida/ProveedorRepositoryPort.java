package co.edu.konradlorenz.cotizaciones.puertos.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;

import java.util.List;

public interface ProveedorRepositoryPort {

    Proveedor guardar(Proveedor p);

    Proveedor porId(Long id);

    boolean existeNit(String nit);

    List<Proveedor> activos();

    List<Proveedor> todos();

}
