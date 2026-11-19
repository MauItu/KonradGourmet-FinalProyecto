package co.edu.konradlorenz.menu.puertos.entrada;

import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;

import java.util.List;

public interface RegistrarPlatoUseCase {

    Plato registrar(Plato p);

    /** Todos los platos de la sucursal, también los desactivados. */
    List<Plato> consultar(Long sucursalId);

    Plato editar(Long id, Plato p);

    void desactivar(Long id);

    void reactivar(Long id);

    List<Producto> productosPorCategoria(Long categoriaId);

    /** Sube la imagen del plato (PNG, JPEG o WebP, hasta 1 MB) y reemplaza la anterior. */
    void guardarImagen(Long id, byte[] contenido, String tipo);

    /** La imagen del plato; si no tiene, IllegalArgumentException. */
    byte[] imagen(Long id);

}
