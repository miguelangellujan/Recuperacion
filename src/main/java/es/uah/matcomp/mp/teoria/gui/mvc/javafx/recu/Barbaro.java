package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.Random;

public class Barbaro extends Thread {
    private final String id;
    private final CentroUrbano centro;
    private static final Random rnd = new Random();
    private boolean puedeAtacar = true;
    private Zona zonaAsignada;

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
    public Zona getZonaAsignada() {
        return zonaAsignada;
    }

    public void setZonaAsignada(Zona zona) {
        this.zonaAsignada = zona;
    }

    @Override
    public void run() {
        try {
            // Bárbaro entra al campamento al crearse
            centro.getZonaCampamento().entrarCampamento(this);
            Log.log(id + " entra al campamento.");
            Thread.sleep(1000); // pequeña pausa para que se note en el estado
            while (true) {
                centro.esperarSiPausado();

                // Espera si no puede atacar aún
                while (!puedeAtacar) {
                    Thread.sleep(500);
                    centro.esperarSiPausado();
                }

                // Salir del campamento para dirigirse a la zona de preparación
                centro.getZonaCampamento().salirCampamento(this);
                Log.log(id + " se dirige a la zona de preparación");
                ZonaPreparacionBarbaros zonaPrep = centro.getZonaPreparacion();

                // Esperar formación de grupo
                Zona objetivo = zonaPrep.esperarGrupo(this);

                centro.esperarSiPausado();
                Log.log(id + " ataca la zona: " + objetivo.getNombreZona());

                // Combate
                boolean ganoCombate = true;
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
                        puedeAtacar = false;

                        // Entra al campamento tras perder
                        centro.getZonaCampamento().entrarCampamento(this);
                        centro.esperarSiPausado();
                        Thread.sleep(60000);

                        puedeAtacar = true;
                        continue; // Salta el saqueo, vuelve a la zona de preparación
                    }
                } else {
                    Log.log(id + " no encontró defensores en " + objetivo.getNombreZona());
                    centro.esperarSiPausado();
                    Thread.sleep(1000); // Espera obligatoria sin defensores
                }

                // Saqueo
                centro.esperarSiPausado();
                Thread.sleep(1000); // Observación previa

                if (objetivo instanceof AreaRecurso recurso) {
                    recurso.iniciarAtaque(this);
                    recurso.expulsarAldeanos();
                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                    recurso.finalizarAtaque(true);
                    recurso.eliminarBarbaro(this);
                    Log.log(id + " ha destruido el área de recurso: " + recurso.getNombreZona());
                } else if (objetivo instanceof Almacen almacen) {
                    almacen.expulsarAldeanos();
                    centro.esperarSiPausado();
                    Thread.sleep(FuncionesComunes.randomBetween(1000, 2000));
                    almacen.saquear(this);
                    Log.log(id + " ha saqueado el almacén: " + almacen.getNombreZona());
                }

                Log.log(id + " finaliza el ataque en " + objetivo.getNombreZona());

                // Penalización de 40s tras ataque
                puedeAtacar = false;
                Log.log(id + " regresa al campamento y espera 40s");

                // Entra al campamento tras ganar y espera
                centro.getZonaCampamento().entrarCampamento(this);
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