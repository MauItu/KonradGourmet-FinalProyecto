package co.edu.konradlorenz.pedidos.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.Map;

public class PedidoRegistradoEvent extends EventoDominio {

    private String numero;
    private Long sucursalId;
    private Map<Long, Integer> lineas;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
