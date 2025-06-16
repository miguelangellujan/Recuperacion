package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.Random;

public class Barbaro extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private static final Random rnd = new Random();
    private boolean puedeAtacar = true;

    public Barbaro(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    public String getIdBarbaro() {
        return id;
    }

    public boolean estaAtacando() {
        return !puedeAtacar;
    }

    @Override
    public void run() {
        try {
            while (true) {
                centro.esperarSiPausado();
                Log.log(id + " se dirige a la zona de preparación");
                ZonaPreparacionBarbaros zonaPrep = centro.getZonaPreparacion();

                Zona objetivo = zonaPrep.esperarGrupo(this);

                centro.esperarSiPausado();
                Log.log(id + " ataca la zona: " + objetivo.getNombreZona());

                centro.esperarSiPausado();
                boolean ganoCombate;
                boolean huboEnfrentamiento = objetivo.enfrentarABarbaro(this);

                if (huboEnfrentamiento) {
                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.randomBetween(500, 1000));

                    double probVictoria = 1.0 - (0.5 + 0.05 * Math.min(5, centro.getGestorMejoras().getNivelArmas()));
                    ganoCombate = rnd.nextDouble() < probVictoria;

                    if (ganoCombate) {
                        Log.log(id + " gana su combate en " + objetivo.getNombreZona());
                    } else {
                        Log.log(id + " pierde el combate y se retira al campamento (60s)");
                        centro.esperarSiPausado();
                        Thread.sleep(60000); // castigo por perder
                        continue; // se salta el saqueo y vuelve a la zona de preparación
                    }
                } else {
                    Log.log(id + " no encontró defensores en " + objetivo.getNombreZona());
                    centro.esperarSiPausado();
                    Thread.sleep(1000); // espera obligatoria sin combate
                }

                centro.esperarSiPausado();
                Thread.sleep(1000); // observación antes del saqueo

                if (objetivo instanceof AreaRecurso recurso) {
                    recurso.iniciarAtaque(this);
                    recurso.expulsarAldeanos(); // según el enunciado
                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.randomBetween(1000, 2000)); // tiempo saqueo
                    recurso.finalizarAtaque(true);
                    recurso.eliminarBarbaro(this);
                    Log.log(id + " ha destruido el área de recurso: " + recurso.getNombreZona());
                } else if (objetivo instanceof Almacen almacen) {
                    almacen.expulsarAldeanos(); // según el enunciado
                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.randomBetween(1000, 2000)); // tiempo saqueo
                    almacen.saquear(this);
                    Log.log(id + " ha saqueado el almacén: " + almacen.getNombreZona());
                }

                Log.log(id + " finaliza el ataque en " + objetivo.getNombreZona());

                // Tras completar un ataque exitoso
                puedeAtacar = false;
                Log.log(id + " regresa al campamento y espera 40s");
                centro.esperarSiPausado();
                Thread.sleep(40000);
                puedeAtacar = true;
            }
        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido.");
            Thread.currentThread().interrupt();
        }
    }
}