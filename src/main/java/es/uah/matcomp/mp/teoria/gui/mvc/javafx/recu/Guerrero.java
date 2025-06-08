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
            // Entrenamiento inicial
            Log.log(id + " comienza entrenamiento en el CUARTEL");
            Thread.sleep(FuncionesComunes.randomBetween(5000, 8000));
            Log.log(id + " ha finalizado el entrenamiento y comienza patrullaje");

            // Patrullaje continuo
            while (!Thread.currentThread().isInterrupted()) {
                Zona zona = centro.obtenerZonaAleatoriaParaPatrulla();

                // Intenta entrar a la zona (controla si está llena o en combate)
                if (zona.entrarGuerrero(this)) {
                    Log.log(id + " patrulla en " + zona.getNombreZona());

                    // Permanece un tiempo aleatorio en la zona
                    Thread.sleep(FuncionesComunes.randomBetween(2000, 3000));

                    zona.salirGuerrero(this);
                    Log.log(id + " sale de " + zona.getNombreZona());
                } else {
                    Log.log(id + " no puede patrullar en " + zona.getNombreZona() + " (llena o en combate), espera...");
                    Thread.sleep(500); // Espera antes de reintentar
                }
            }

        } catch (InterruptedException e) {
            Log.log(id + " ha sido interrumpido.");
            Thread.currentThread().interrupt();
        }
    }
}
