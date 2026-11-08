package co.edu.konradlorenz.identidad.puertos.salida;

public interface CifradorPort {

    String cifrar(String clave);

    boolean coincide(String clave, String hash);

}
