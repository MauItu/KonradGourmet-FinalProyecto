package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

interface EnvioJpaRepository extends JpaRepository<EnvioEntidad, Long> {
}
