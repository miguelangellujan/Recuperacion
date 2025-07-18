package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ZonaPreparacionBarbaros {
    private final List<Barbaro> esperando = new ArrayList<>();
    private final Object lock = new Object();
    private final Random rnd = new Random();
    private final AtomicInteger barbarosTotales = new AtomicInteger(0);
    private long ultimoAtaque = 0;
    private Zona objetivoGrupoActual = null;
    private final Set<Barbaro> grupoActual = new HashSet<>();
    private final CentroUrbano centro;

    public ZonaPreparacionBarbaros(CentroUrbano centro) {
        this.centro = centro;
    }

    public int getBarbarosEnPreparacion() {
        synchronized (lock) {
            return esperando.size();
        }
    }

    public String obtenerIdsEnPreparacion() {
        synchronized (lock) {
            return esperando.stream()
                    .map(Barbaro::getIdBarbaro)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Ninguno");
        }
    }

    public Zona esperarGrupo(Barbaro b) throws InterruptedException {
        synchronized (lock) {
            barbarosTotales.incrementAndGet();
            esperando.add(b);
            centro.esperarSiPausado();
            Log.log(b.getIdBarbaro() + " se une a la zona de preparación");

            while (true) {
                centro.esperarSiPausado();

                // Si el bárbaro ya ha sido asignado, salir
                if (grupoActual.contains(b)) {
                    return objetivoGrupoActual;
                }

                // Calcular condiciones del grupo
                int totalCreados = barbarosTotales.get();
                int tamanioGrupo = 3 + (totalCreados / 10);
                long ahora = System.currentTimeMillis();

                // Limpiar grupo de bárbaros inactivos/interrumpidos por seguridad
                grupoActual.removeIf(barb -> !barb.isAlive() || barb.isInterrupted());

                // Verificar si se puede formar nuevo grupo
                if (grupoActual.isEmpty()
                        && esperando.size() >= tamanioGrupo
                        && ahora - ultimoAtaque >= 10_000) {

                    List<Barbaro> grupo = new ArrayList<>(esperando.subList(0, tamanioGrupo));
                    esperando.removeAll(grupo);
                    grupoActual.clear();
                    grupoActual.addAll(grupo);

                    objetivoGrupoActual = seleccionarObjetivoGrupo();
                    ultimoAtaque = ahora;

                    Log.log("Grupo de " + grupo.size() + " bárbaros se dirige a " + objetivoGrupoActual.getNombreZona());

                    lock.notifyAll(); // Despertar a todos los bárbaros en espera
                } else {
                    lock.wait(2000); // Espera con timeout para reevaluar periódicamente
                }
            }
        }
    }

    public void eliminarDelGrupo(Barbaro b) {
        synchronized (lock) {
            grupoActual.remove(b);
            if (grupoActual.isEmpty()) {
                lock.notifyAll(); // Permitir formar un nuevo grupo
            }
        }
    }

    private Zona seleccionarObjetivoGrupo() {
        List<Zona> almacenes = List.of(
                centro.getAlmacen("COMIDA"),
                centro.getAlmacen("MADERA"),
                centro.getAlmacen("ORO")
        );

        List<Zona> recursos = List.of(
                centro.getArea("COMIDA"),
                centro.getArea("MADERA"),
                centro.getArea("ORO")
        );

        return rnd.nextDouble() < 0.6
                ? almacenes.get(rnd.nextInt(almacenes.size()))
                : recursos.get(rnd.nextInt(recursos.size()));
    }
}
