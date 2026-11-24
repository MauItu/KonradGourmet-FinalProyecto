package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Fila de la tabla proveedor con sus categorías (proveedor_categoria). La usan los dos adaptadores JPA. */
@Entity
@Table(name = "proveedor")
class ProveedorEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "razon_social", nullable = false)
    String razonSocial;

    @Column(nullable = false)
    String nit;

    @Column(nullable = false)
    String correo;

    String telefono;

    String direccion;

    @Column(nullable = false)
    boolean activo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "proveedor_categoria", joinColumns = @JoinColumn(name = "proveedor_id"))
    @OrderColumn(name = "orden")
    @Column(name = "categoria")
    List<String> categorias = new ArrayList<>();

}
