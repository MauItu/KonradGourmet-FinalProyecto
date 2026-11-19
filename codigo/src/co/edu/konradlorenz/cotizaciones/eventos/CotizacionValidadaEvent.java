package co.edu.konradlorenz.cotizaciones.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.math.BigDecimal;

/**
 * El resultado de la cadena de reglas (V19). Lleva tipos simples para que otro servicio lo lea sin importar el
 * dominio de ms-cotizaciones (PD-05, DEC-29): el estado como texto (RECHAZADA, SOSPECHOSA u OPCIONADA), la
 * diferencia contra el precio estándar, que ms-analitica promedia para el proveedor con mejores precios (PD-03),
 * y el nombre del proveedor, que el tablero muestra (DEC-89).
 */
public class CotizacionValidadaEvent extends EventoDominio {

    private String numero;
    private Long proveedorId;
    private String proveedor;
    private String estado;
    private BigDecimal valorCotizado;
    private BigDecimal diferenciaPorcentual;

    public CotizacionValidadaEvent(String usuario, String numero, Long proveedorId, String proveedor, String estado,
                                   BigDecimal valorCotizado, BigDecimal diferenciaPorcentual) {
        super(usuario);
        this.numero = numero;
        this.proveedorId = proveedorId;
        this.proveedor = proveedor;
        this.estado = estado;
        this.valorCotizado = valorCotizado;
        this.diferenciaPorcentual = diferenciaPorcentual;
    }

    public String numero() {
        return numero;
    }

    public Long proveedorId() {
        return proveedorId;
    }

    public String proveedor() {
        return proveedor;
    }

    public String estado() {
        return estado;
    }

    public BigDecimal valorCotizado() {
        return valorCotizado;
    }

    public BigDecimal diferenciaPorcentual() {
        return diferenciaPorcentual;
    }

    @Override
    public String tipo() {
        return "cotizacion.validada";
    }

}
