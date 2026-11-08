package co.edu.konradlorenz.pedidos.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.Map;

public class PedidoRegistradoEvent extends EventoDominio {

    private String numero;
    private Long sucursalId;
    private Map<Long, Integer> lineas;

    public PedidoRegistradoEvent(String usuario, String numero, Long sucursalId, Map<Long, Integer> lineas) {
        super(usuario);
        this.numero = numero;
        this.sucursalId = sucursalId;
        this.lineas = lineas;
    }

    public String numero() {
        return numero;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public Map<Long, Integer> lineas() {
        return lineas;
    }

    @Override
    public String tipo() {
        return "pedido.registrado";
    }

}
