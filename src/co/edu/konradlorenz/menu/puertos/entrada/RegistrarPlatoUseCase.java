package co.edu.konradlorenz.menu.puertos.entrada;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;

import java.util.List;

public interface RegistrarPlatoUseCase {

    Plato registrar(Plato p);

    List<Plato> consultar(Long sucursalId);

    Plato editar(Long id, Plato p);

    void desactivar(Long id);

    List<Producto> productosPorCategoria(Long categoriaId);

}
