package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public interface Zona {
    boolean entrarGuerrero(Guerrero g) throws InterruptedException;
    void salirGuerrero(Guerrero g);
    String getNombreZona();

    // Método usado por bárbaros para combate simulado
    boolean enfrentarABarbaro(Barbaro b) throws InterruptedException;
}
