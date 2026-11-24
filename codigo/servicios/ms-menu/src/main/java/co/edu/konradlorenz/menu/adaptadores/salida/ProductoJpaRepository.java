package co.edu.konradlorenz.menu.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ProductoJpaRepository extends JpaRepository<ProductoEntidad, Long> {

    List<ProductoEntidad> findByCategoriaIdOrderByNombre(Long categoriaId);

    List<ProductoEntidad> findAllByOrderByNombre();

}
