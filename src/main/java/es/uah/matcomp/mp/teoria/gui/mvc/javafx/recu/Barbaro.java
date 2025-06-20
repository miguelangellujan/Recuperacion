package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.Random;

public class Barbaro extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private static final Random rnd = new Random();
    private boolean puedeAtacar = true;
    private final Object lockAtaque = new Object();

    public Barbaro(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdBarbaro() {
        return id;
    }

    @Override
    public void run() {
        try {
            // El bárbaro entra al campamento al crearse
            centro.getZonaCampamento().entrarCampamento(this);
            centro.esperarSiPausado();
            Log.log(id + " entra al campamento.");
            Thread.sleep(2000); // Pequeña pausa inicial

            while (!Thread.currentThread().isInterrupted()) {
                centro.esperarSiPausado();

                // Espera pasiva hasta poder atacar
                synchronized (lockAtaque) {
                    while (!puedeAtacar) {
                        lockAtaque.wait();
                    }
                }

                centro.esperarSiPausado();
                centro.getZonaCampamento().salirCampamento(this);
                Log.log(id + " se dirige a la zona de preparación");
                ZonaPreparacionBarbaros zonaPrep = centro.getZonaPreparacion();

                // Espera a que se forme grupo y recibe objetivo
                Zona objetivo = zonaPrep.esperarGrupo(this);
                Log.log(id + " ataca la zona: " + objetivo.getNombreZona());

                boolean ganoCombate;
                boolean huboEnfrentamiento = objetivo.enfrentarABarbaro(this);
                if (huboEnfrentamiento) {
                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.Tiempoaleatorio(500, 1000));

                    double probVictoria = 1.0 - (0.5 + 0.05 * Math.min(5, centro.getGestorMejoras().getNivelArmas()));
                    ganoCombate = rnd.nextDouble() < probVictoria;

                    if (ganoCombate) {
                        Log.log(id + " gana su combate en " + objetivo.getNombreZona());
                    } else {
                        Log.log(id + " pierde el combate y se retira al campamento (60s)");

                        zonaPrep.eliminarDelGrupo(this);
                        centro.getZonaCampamento().entrarCampamento(this);

                        synchronized (lockAtaque) {
                            puedeAtacar = false;
                        }

                        try {
                            centro.esperarSiPausado();
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            Log.log(id + " fue interrumpido durante la espera en el campamento.");
                            Thread.currentThread().interrupt();
                            return;
                        }

                        synchronized (lockAtaque) {
                            puedeAtacar = true;
                            lockAtaque.notify();
                        }

                        continue;
                    }
                } else {
                    Log.log(id + " no encontró defensores en " + objetivo.getNombreZona());
                    Thread.sleep(1000);
                }

                // Saqueo
                if (objetivo instanceof AreaRecurso recurso) {
                    recurso.expulsarAldeanos();
                    recurso.iniciarAtaque(this);
                    Thread.sleep(FuncionesComunes.Tiempoaleatorio(1000, 2000));
                    recurso.finalizarAtaque(true);
                    recurso.eliminarBarbaro(this);
                    Log.log(id + " ha destruido el área de recurso: " + recurso.getNombreZona());

                } else if (objetivo instanceof Almacen almacen) {
                    almacen.expulsarAldeanos();
                    Thread.sleep(FuncionesComunes.Tiempoaleatorio(1000, 2000));
                    almacen.saquear(this);
                    Log.log(id + " ha saqueado el almacén: " + almacen.getNombreZona());
                }

                zonaPrep.eliminarDelGrupo(this);
                Log.log(id + " finaliza el ataque en " + objetivo.getNombreZona());

                // Regreso al campamento y descanso
                synchronized (lockAtaque) {
                    puedeAtacar = false;
                }

                Log.log(id + " regresa al campamento y espera 40s");
                centro.getZonaCampamento().entrarCampamento(this);

                try {
                    Thread.sleep(40000);
                } catch (InterruptedException e) {
                    Log.log(id + " fue interrumpido durante el descanso.");
                    Thread.currentThread().interrupt();
                    return;
                }

                synchronized (lockAtaque) {
                    puedeAtacar = true;
                    lockAtaque.notify();
                }
            }

        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido y finaliza su ejecución.");
            Thread.currentThread().interrupt();
            centro.getZonaCampamento().salirCampamento(this);
            centro.getZonaPreparacion().eliminarDelGrupo(this);
        }
    }
}
