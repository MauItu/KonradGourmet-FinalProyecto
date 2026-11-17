package co.edu.konradlorenz.identidad.dominio;

public class TokenAcceso {

    private String jwt;
    private ElementoMenu menu;

    public TokenAcceso(String jwt, ElementoMenu menu) {
        this.jwt = jwt;
        this.menu = menu;
    }

    public String jwt() {
        return jwt;
    }

    public ElementoMenu menu() {
        return menu;
    }

}
