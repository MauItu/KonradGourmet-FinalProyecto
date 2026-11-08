package co.edu.konradlorenz.cotizaciones.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;

import java.math.BigDecimal;

public class CotizacionValidadaEvent extends EventoDominio {

    private String numero;
    private Long proveedorId;
    private EstadoCotizacion estado;
    private BigDecimal valorCotizado;

    public CotizacionValidadaEvent(String usuario, String numero, Long proveedorId, EstadoCotizacion estado, BigDecimal valorCotizado) {
        super(usuario);
        this.numero = numero;
        this.proveedorId = proveedorId;
        this.estado = estado;
        this.valorCotizado = valorCotizado;
    }

    public String numero() {
        return numero;
    }

    public Long proveedorId() {
        return proveedorId;
    }

    public EstadoCotizacion estado() {
        return estado;
    }

    public BigDecimal valorCotizado() {
        return valorCotizado;
    }

    @Override
    public String tipo() {
        return "cotizacion.validada";
    }

}
