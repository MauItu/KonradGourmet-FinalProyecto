package co.edu.konradlorenz.pedidos.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/** Fila de la tabla mesa: la llave es la sucursal y el número. */
@Entity
@Table(name = "mesa")
@IdClass(MesaEntidad.Llave.class)
class MesaEntidad {

    @Id
    @Column(name = "sucursal_id")
    Long sucursalId;

    @Id
    Integer numero;

    @Column(nullable = false)
    int capacidad;

    @Column(nullable = false)
    boolean activa;

    static class Llave implements Serializable {
        Long sucursalId;
        Integer numero;

        @Override
        public boolean equals(Object o) {
            return o instanceof Llave l && Objects.equals(sucursalId, l.sucursalId) && Objects.equals(numero, l.numero);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sucursalId, numero);
        }
    }

}
