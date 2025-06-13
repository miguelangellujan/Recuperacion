package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AreaRecuperacion {
    private final List<String> enRecuperacion = new CopyOnWriteArrayList<>();

    public void enviarAldeano(Aldeano a, int minMs, int maxMs) {
        new Thread(() -> {
            try {
                String id = a.getIdAldeano();
                enRecuperacion.add(id);
                Log.log(id + " entra en ÁREA DE RECUPERACIÓN");

                Thread.sleep(FuncionesComunes.randomBetween(minMs, maxMs));

                Log.log(id + " sale de ÁREA DE RECUPERACIÓN");
                enRecuperacion.remove(id);
            } catch (InterruptedException e) {
                Log.log(a.getIdAldeano() + " fue interrumpido en recuperación.");
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void enviarGuerrero(Guerrero g, int minMs, int maxMs) {
        new Thread(() -> {
            try {
                String id = g.getIdGuerrero();
                enRecuperacion.add(id);
                Log.log(id + " entra en ÁREA DE RECUPERACIÓN");

                Thread.sleep(FuncionesComunes.randomBetween(minMs, maxMs));

                Log.log(id + " sale de ÁREA DE RECUPERACIÓN");
                enRecuperacion.remove(id);
            } catch (InterruptedException e) {
                Log.log(g.getIdGuerrero() + " fue interrumpido en recuperación.");
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public String obtenerIdsEnRecuperacion() {
        if (enRecuperacion.isEmpty()) return "Área de Recuperación: vacía";
        return "En recuperación: " + String.join(", ", enRecuperacion);
    }
    // === FUNCIONES ADICIONALES ===

    public List<String> getAldeanosEnRecuperacion() {
        return enRecuperacion.stream()
                .filter(id -> id.startsWith("Aldeano"))
                .collect(Collectors.toList());
    }

    public List<String> getGuerrerosEnRecuperacion() {
        return enRecuperacion.stream()
                .filter(id -> id.startsWith("Guerrero"))
                .collect(Collectors.toList());
    }

    public int contarAldeanosEnRecuperacion() {
        return (int) enRecuperacion.stream()
                .filter(id -> id.startsWith("Aldeano"))
                .count();
    }

    public int contarGuerrerosEnRecuperacion() {
        return (int) enRecuperacion.stream()
                .filter(id -> id.startsWith("Guerrero"))
                .count();
    }
}