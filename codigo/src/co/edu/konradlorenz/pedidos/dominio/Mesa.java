package co.edu.konradlorenz.pedidos.dominio;

public class Mesa {

    private int numero;
    private Long sucursalId;
    private int capacidad;
    private boolean activa;

    public Mesa(int numero, Long sucursalId, int capacidad, boolean activa) {
        this.numero = numero;
        this.sucursalId = sucursalId;
        this.capacidad = capacidad;
        this.activa = activa;
    }

    public int numero() {
        return numero;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public int capacidad() {
        return capacidad;
    }

    public boolean activa() {
        return activa;
    }

}
