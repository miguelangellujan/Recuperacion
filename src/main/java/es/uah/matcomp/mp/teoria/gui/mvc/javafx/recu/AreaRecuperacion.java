package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class AreaRecuperacion {

    public void enviarAldeano(Aldeano a, int minMs, int maxMs) {
        new Thread(() -> {
            try {
                Log.log(a.getIdAldeano() + " entra en ÁREA DE RECUPERACIÓN");
                Thread.sleep(FuncionesComunes.randomBetween(minMs, maxMs));
                Log.log(a.getIdAldeano() + " sale de ÁREA DE RECUPERACIÓN");
            } catch (InterruptedException e) {
                Log.log(a.getIdAldeano() + " fue interrumpido en recuperación.");
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void enviarGuerrero(Guerrero g, int minMs, int maxMs) {
        new Thread(() -> {
            try {
                Log.log(g.getIdGuerrero() + " entra en ÁREA DE RECUPERACIÓN");
                Thread.sleep(FuncionesComunes.randomBetween(minMs, maxMs));
                Log.log(g.getIdGuerrero() + " sale de ÁREA DE RECUPERACIÓN");
            } catch (InterruptedException e) {
                Log.log(g.getIdGuerrero() + " fue interrumpido en recuperación.");
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}