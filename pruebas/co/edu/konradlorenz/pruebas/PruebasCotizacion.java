package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaOpcionada;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaRechazada;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaSospechosa;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** La cotización, su diferencia porcentual y la cadena de reglas (Chain of Responsibility). */
final class PruebasCotizacion {

    private PruebasCotizacion() {
    }

    static Proveedor proveedor() {
        return new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", "601", "Cra 7", List.of());
    }

    /** La cotización de V19: res 20 kg y arroz 15 kg, con el precio estándar del Ministerio y el del proveedor. */
    static Cotizacion cotizacion(String numero, String precioRes, String precioArroz) {
        return new Cotizacion(numero, List.of("SOL-031"), proveedor(), LocalDate.of(2026, 9, 30), List.of(
                new ItemCotizacion(1L, new BigDecimal("20"), "kg", new BigDecimal("20000"), new BigDecimal(precioRes)),
                new ItemCotizacion(2L, new BigDecimal("15"), "kg", new BigDecimal("8000"), new BigDecimal(precioArroz))));
    }

    /** Una cotización de un solo producto con estándar 100, para probar los límites de la cadena. */
    private static Cotizacion conPrecio(String precio) {
        return new Cotizacion("COT-X", List.of("SOL-031"), proveedor(), LocalDate.now(), List.of(
                new ItemCotizacion(1L, BigDecimal.ONE, "kg", new BigDecimal("100"), new BigDecimal(precio))));
    }

    static void ejecutar() {
        Prueba.grupo("Cotización: valores de la ventana V19");
        Cotizacion c18 = cotizacion("COT-018", "21250", "8000");
        Prueba.igualValor("COT-018: valor estándar $520.000", "520000", c18.valorEstandarTotal());
        Prueba.igualValor("COT-018: valor cotizado $545.000", "545000", c18.valorCotizadoTotal());
        Prueba.igualValor("COT-018: diferencia +4,81 %", "4.81", c18.diferenciaPorcentual());
        Prueba.igualValor("COT-019: diferencia +34,62 %", "34.62", cotizacion("COT-019", "29000", "8000").diferenciaPorcentual());
        Prueba.igualValor("COT-020: diferencia −61,54 %", "-61.54", cotizacion("COT-020", "4000", "8000").diferenciaPorcentual());

        Prueba.grupo("Cadena de reglas: cadenaPorDefecto()");
        ReglaValidacion cadena = ReglaValidacion.cadenaPorDefecto();
        Prueba.igual("COT-018 (+4,81 %) queda OPCIONADA", EstadoCotizacion.OPCIONADA, cadena.evaluar(c18));
        Prueba.igual("COT-019 (+34,62 %) queda RECHAZADA", EstadoCotizacion.RECHAZADA, cadena.evaluar(cotizacion("COT-019", "29000", "8000")));
        Prueba.igual("COT-020 (−61,54 %) queda SOSPECHOSA", EstadoCotizacion.SOSPECHOSA, cadena.evaluar(cotizacion("COT-020", "4000", "8000")));
        Prueba.igual("justo +25,00 % sigue OPCIONADA", EstadoCotizacion.OPCIONADA, cadena.evaluar(conPrecio("125")));
        Prueba.igual("+25,01 % ya es RECHAZADA", EstadoCotizacion.RECHAZADA, cadena.evaluar(conPrecio("125.01")));
        Prueba.igual("justo −50,00 % sigue OPCIONADA", EstadoCotizacion.OPCIONADA, cadena.evaluar(conPrecio("50")));
        Prueba.igual("−50,01 % ya es SOSPECHOSA", EstadoCotizacion.SOSPECHOSA, cadena.evaluar(conPrecio("49.99")));
        Prueba.igual("sin diferencia (0 %) es OPCIONADA", EstadoCotizacion.OPCIONADA, cadena.evaluar(conPrecio("100")));

        ReglaRechazada rechazada = new ReglaRechazada();
        ReglaSospechosa sospechosa = new ReglaSospechosa();
        Prueba.verificar("enlazar() devuelve la regla que se le pasó, para encadenar", rechazada.enlazar(sospechosa) == sospechosa);
        Prueba.igual("una regla suelta evalúa lo suyo", EstadoCotizacion.RECHAZADA, new ReglaRechazada().evaluar(conPrecio("200")));
        Prueba.lanza("si ninguna regla aplica, lo dice", IllegalStateException.class, "Ninguna regla aplica",
                () -> new ReglaRechazada().evaluar(conPrecio("100")));
        ReglaValidacion otra = new ReglaSospechosa();
        otra.enlazar(new ReglaOpcionada());
        Prueba.igual("el orden de la cadena es el que se arma", EstadoCotizacion.OPCIONADA, otra.evaluar(conPrecio("100")));

        Prueba.grupo("Cotización: validaciones");
        Prueba.lanza("rechaza la lista vacía de solicitudes", IllegalStateException.class, "Debe seleccionar al menos una solicitud",
                () -> new Cotizacion("COT-X", List.of(), proveedor(), LocalDate.now(),
                        List.of(new ItemCotizacion(1L, BigDecimal.ONE, "kg"))).validarSolicitudes());
        Prueba.lanza("registrarValor() de un producto que no está en la cotización", IllegalArgumentException.class, "no está en la cotización",
                () -> c18.registrarValor(99L, BigDecimal.TEN));
        Prueba.lanza("el precio del proveedor debe ser mayor que cero", IllegalArgumentException.class, "mayor que cero",
                () -> c18.registrarValor(1L, BigDecimal.ZERO));
        Prueba.lanza("sin el precio del proveedor no hay valor cotizado", IllegalStateException.class, "Falta el precio del proveedor",
                () -> new Cotizacion("COT-X", List.of("SOL-031"), proveedor(), LocalDate.now(),
                        List.of(new ItemCotizacion(1L, BigDecimal.ONE, "kg", BigDecimal.TEN, null))).valorCotizadoTotal());
        Cotizacion enviada = cotizacion("COT-021", "21250", "8000");
        enviada.cambiarEstado(EstadoCotizacion.ENVIADA);
        Prueba.lanza("CON_ORDEN solo se alcanza desde OPCIONADA", IllegalStateException.class, "opcionada",
                () -> enviada.cambiarEstado(EstadoCotizacion.CON_ORDEN));
        enviada.cambiarEstado(EstadoCotizacion.OPCIONADA);
        enviada.cambiarEstado(EstadoCotizacion.CON_ORDEN);
        Prueba.igual("desde OPCIONADA sí pasa a CON_ORDEN", EstadoCotizacion.CON_ORDEN, enviada.estado());
    }

}
