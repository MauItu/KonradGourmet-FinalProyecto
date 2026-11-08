package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioSoapClient;

import java.util.HashMap;
import java.util.Map;

/**
 * El servicio web del Ministerio de Comercio, simulado: una tabla de precios por kilo y un contador
 * de llamadas, para que se vea cuándo responde el servicio y cuándo responde la caché del Proxy.
 */
public class MinisterioSoapSimulado extends MinisterioSoapClient {

    private final Map<String, Double> precios = new HashMap<>();
    private int llamadas;

    public MinisterioSoapSimulado() {
        super("https://ministerio.demo/precios");
    }

    public void definirPrecio(String codigoProducto, double precio) {
        precios.put(codigoProducto, precio);
    }

    /** Cuántas veces se llamó al «servicio web». */
    public int llamadas() {
        return llamadas;
    }

    @Override
    public double consultarPrecioEstandar(String codigoProducto) {
        llamadas++;
        Double precio = precios.get(codigoProducto);
        if (precio == null) {
            throw new IllegalArgumentException("El Ministerio no tiene precio para el producto " + codigoProducto);
        }
        return precio;
    }

}
