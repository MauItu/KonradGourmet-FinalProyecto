package co.edu.konradlorenz.identidad.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UsuarioJpaRepository extends JpaRepository<UsuarioEntidad, Long> {

    Optional<UsuarioEntidad> findByCorreo(String correo);

}
