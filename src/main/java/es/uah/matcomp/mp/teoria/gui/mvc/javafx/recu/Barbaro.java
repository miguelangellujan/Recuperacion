package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.Random;

public class Barbaro extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private static final Random rnd = new Random();
    private boolean puedeAtacar = true;

    // Constructor
    public Barbaro(String id, CentroUrbano centro) {
        this.id = id;
        this.centro = centro;
    }

    // Getter
    public String getIdBarbaro() {
        return id;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (!puedeAtacar) {
                    Log.log(id + " descansa en campamento por 40s");
                    Thread.sleep(40000);
                    puedeAtacar = true;
                }

                Log.log(id + " se dirige a la zona de preparación");
                ZonaPreparacionBarbaros zonaPrep = centro.getZonaPreparacion();

                Zona objetivo = zonaPrep.esperarGrupo(this);
                Log.log(id + " ataca la zona: " + objetivo.getNombreZona());

                boolean ganoCombate = true;
                if (objetivo.enfrentarABarbaro(this)) {
                    Thread.sleep(FuncionesComunes.randomBetween(500, 1000));
                    ganoCombate = rnd.nextBoolean();
                    if (ganoCombate) {
                        Log.log(id + " gana su combate en " + objetivo.getNombreZona());
                    } else {
                        Log.log(id + " pierde el combate y se retira");
                        puedeAtacar = false;
                        return;
                    }
                } else {
                    Log.log(id + " no encontró defensores en " + objetivo.getNombreZona());
                }

                Thread.sleep(1000);
                if (objetivo instanceof AreaRecurso recurso) {
                    recurso.iniciarAtaque();
                    recurso.finalizarAtaque(true);
                } else if (objetivo instanceof Almacen almacen) {
                    almacen.saquear();
                }

                Log.log(id + " finaliza el ataque en " + objetivo.getNombreZona());
                puedeAtacar = false;
            }
        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido.");
            Thread.currentThread().interrupt();
        }
    }
}
