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
        centro.getAreaRecuperacion().entrarGuerrero(this);
    }

    public String getIdGuerrero() {
        return id;
    }

    public double getProbabilidadVictoria() {
        int nivelArmas = centro.getGestorMejoras().getNivelArmas();
        double probBase = 0.5; // 50%
        double incremento = 0.05; // 5% por nivel
        double probTotal = probBase + (nivelArmas * incremento);
        return Math.min(probTotal, 0.75); // máximo 75%, empieza en 50% y 5% más por nivel
    }

    @Override
    public void run() {
        try {
            centro.esperarSiPausado();
            centro.getCuartel().entrar(this);// Entra primero al cuartel y hasta que no hay suficientes recursos no empieza
            // Esperar hasta que haya recursos para entrenar
            while (!Thread.currentThread().isInterrupted()) {
                Almacen comida = centro.getGranero();
                Almacen madera = centro.getAserradero();
                Almacen oro = centro.getTesoreria();

                synchronized (comida) {
                    synchronized (madera) {
                        synchronized (oro) {
                            if (comida.getCantidadActual() >= 50
                                    && madera.getCantidadActual() >= 50
                                    && oro.getCantidadActual() >= 80) {

                                comida.consumir(50);
                                madera.consumir(50);
                                oro.consumir(80);

                                Log.log(id + " comienza entrenamiento en el CUARTEL");
                                centro.getCuartel().entrenar(this);
                                Log.log(id + " ha finalizado el entrenamiento y comienza patrullaje");
                                break;
                            }
                        }
                    }
                }

                Log.log(id + " esperando recursos para entrenar...");
                Thread.sleep(2000);
            }

            // Patrullaje tras entrenamiento
            while (!Thread.currentThread().isInterrupted()) {
                centro.esperarSiPausado();

                Zona zona = centro.obtenerZonaAleatoriaParaPatrulla();

                if (zona.entrarGuerrero(this)) {
                    Log.log(id + " patrulla en " + zona.getNombreZona());

                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.Tiempoaleatorio(2000, 3000));

                    zona.salirGuerrero(this);
                    centro.esperarSiPausado();
                    Log.log(id + " sale de " + zona.getNombreZona());
                } else {
                    centro.esperarSiPausado();
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
