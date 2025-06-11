package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

public class Guerrero extends Thread {
    private final String id;
    private final CentroUrbano centro;

    public Guerrero(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;

    }

    public void enviarARecuperacion() {
        Log.log(id + " es enviado al área de recuperación tras ser derrotado");
        centro.getAreaRecuperacion().enviarGuerrero(this, 10000, 15000);
    }

    public String getIdGuerrero() {
        return id;
    }

    @Override
    public void run() {
        try {
            centro.getPaso().mirar();
            Log.log(id + " comienza entrenamiento en el CUARTEL");
            Thread.sleep(FuncionesComunes.randomBetween(5000, 8000));
            Log.log(id + " ha finalizado el entrenamiento y comienza patrullaje");

            while (!Thread.currentThread().isInterrupted()) {
                centro.getPaso().mirar();
                Zona zona = centro.obtenerZonaAleatoriaParaPatrulla();

                if (zona.entrarGuerrero(this)) {
                    Log.log(id + " patrulla en " + zona.getNombreZona());

                    centro.getPaso().mirar();
                    Thread.sleep(FuncionesComunes.randomBetween(2000, 3000));

                    zona.salirGuerrero(this);
                    Log.log(id + " sale de " + zona.getNombreZona());
                } else {
                    Log.log(id + " no puede patrullar en " + zona.getNombreZona() + " (llena o en combate), espera...");
                    centro.getPaso().mirar();
                    Thread.sleep(500);
                }
            }

        } catch (InterruptedException e) {
            Log.log(id + " ha sido interrumpido.");
            Thread.currentThread().interrupt();
        }
    }
}
