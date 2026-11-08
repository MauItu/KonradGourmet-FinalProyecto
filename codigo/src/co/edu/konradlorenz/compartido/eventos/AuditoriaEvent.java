package co.edu.konradlorenz.compartido.eventos;

public class AuditoriaEvent extends EventoDominio {

    private String accion;

    public AuditoriaEvent(String usuario, String accion) {
        super(usuario);
        this.accion = accion;
    }

    public String accion() {
        return accion;
    }

    @Override
    public String tipo() {
        return "auditoria.evento";
    }

}
