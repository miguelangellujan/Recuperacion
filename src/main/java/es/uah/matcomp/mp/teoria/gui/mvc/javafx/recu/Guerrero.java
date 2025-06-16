package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.concurrent.atomic.AtomicBoolean;

public class Guerrero extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private final AtomicBoolean luchando = new AtomicBoolean(false);

    public Guerrero(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public boolean estaLuchando() {
        return luchando.get();
    }

    public void setLuchando(boolean estado) {
        luchando.set(estado);
    }

    public void enviarARecuperacion() {
        Log.log(id + " es enviado al área de recuperación tras ser derrotado");
        centro.getAreaRecuperacion().enviarGuerrero(this, 10000, 15000);
    }

    public String getIdGuerrero() {
        return id;
    }

    public double getProbabilidadVictoria() {
        int nivelArmas = centro.getGestorMejoras().getNivelArmas();
        double probBase = 0.40; // 40%
        double incremento = 0.05; // 5% por nivel
        double probTotal = probBase + (nivelArmas * incremento);
        return Math.min(probTotal, 0.70); // máximo 70%
    }

    @Override
    public void run() {
        try {
            centro.esperarSiPausado();
            Log.log(id + " comienza entrenamiento en el CUARTEL");
            centro.getCuartel().entrenar(this);
            Log.log(id + " ha finalizado el entrenamiento y comienza patrullaje");

            while (!Thread.currentThread().isInterrupted()) {
                centro.esperarSiPausado();

                Zona zona = centro.obtenerZonaAleatoriaParaPatrulla();

                if (zona.entrarGuerrero(this)) {
                    Log.log(id + " patrulla en " + zona.getNombreZona());

                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.randomBetween(2000, 3000));

                    zona.salirGuerrero(this);
                    Log.log(id + " sale de " + zona.getNombreZona());
                } else {
                    Log.log(id + " no puede patrullar en " + zona.getNombreZona() + " (llena o en combate), espera...");
                    centro.esperarSiPausado();
                    Thread.sleep(500);
                }
            }

        } catch (InterruptedException e) {
            Log.log(id + " ha sido interrumpido.");
            Thread.currentThread().interrupt();
        }
    }
}
