package co.edu.konradlorenz.pedidos.configuracion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.pedidos.aplicacion.PedidoService;
import co.edu.konradlorenz.pedidos.puertos.entrada.RegistrarPedidoUseCase;
import co.edu.konradlorenz.pedidos.puertos.salida.CatalogoRepositoryPort;
import co.edu.konradlorenz.pedidos.puertos.salida.PedidoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Arma PedidoService con su constructor (el núcleo de src/ no lleva anotaciones). */
@Configuration
public class ConfiguracionPedidos {

    @Bean
    public RegistrarPedidoUseCase registrarPedido(PedidoRepositoryPort repositorio, CatalogoRepositoryPort catalogo, EventPublisherPort eventos) {
        return new PedidoService(repositorio, catalogo, eventos);
    }

}
