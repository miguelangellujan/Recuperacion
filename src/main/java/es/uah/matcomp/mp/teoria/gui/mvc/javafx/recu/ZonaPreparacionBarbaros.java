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
    private Set<Barbaro> grupoActual = new HashSet<>();

    private final CentroUrbano centro;

    public ZonaPreparacionBarbaros(CentroUrbano centro) {
        this.centro = centro;
    }

    public Zona esperarGrupo(Barbaro b) throws InterruptedException {
        synchronized (lock) {
            barbarosTotales.incrementAndGet();
            esperando.add(b);
            Log.log(b.getIdBarbaro() + " se une a la zona de preparación");

            while (true) {
                int totalCreados = barbarosTotales.get();
                int tamanioGrupo = 3 + (totalCreados / 10);

                long ahora = System.currentTimeMillis();
                if (grupoActual.contains(b)) {
                    // Este bárbaro ya está en un grupo formado
                    return objetivoGrupoActual;
                }

                if (esperando.size() >= tamanioGrupo && ahora - ultimoAtaque >= 10_000) {
                    // Formar grupo nuevo
                    List<Barbaro> grupo = new ArrayList<>(esperando.subList(0, tamanioGrupo));
                    esperando.removeAll(grupo);
                    grupoActual.clear();
                    grupoActual.addAll(grupo);

                    objetivoGrupoActual = seleccionarObjetivoGrupo();
                    ultimoAtaque = ahora;

                    Log.log("Grupo de " + grupo.size() + " bárbaros se dirige a " + objetivoGrupoActual.getNombreZona());

                    lock.notifyAll(); // Notificar a todos los bárbaros que puedan estar en este grupo
                } else {
                    lock.wait(500);
                }
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
