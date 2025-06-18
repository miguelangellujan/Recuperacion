package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AreaRecuperacion {
    private final List<String> enRecuperacion = new CopyOnWriteArrayList<>();
    private final List<String> guerreroenRecuperacion = new CopyOnWriteArrayList<>();


    public void entrar(Aldeano a) {
        String id = a.getIdAldeano();
        enRecuperacion.add(id);
        Log.log(id + " entra en ÁREA DE RECUPERACIÓN. Estado: " + obtenerIdsEnRecuperacion());

        try {
            Thread.sleep(FuncionesComunes.randomBetween(12000, 15000));
        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido durante la recuperación, pero continúa normalmente.");
            // NO marcamos el hilo como interrumpido: el aldeano sigue su ciclo
        }

        enRecuperacion.remove(id);
        Log.log(id + " sale de ÁREA DE RECUPERACIÓN. Estado: " + obtenerIdsEnRecuperacion());
    }


    public void entrarGuerrero(Guerrero g) {
        String id = g.getIdGuerrero();
        guerreroenRecuperacion.add(id);
        Platform.runLater(() -> Log.log(id + " entra en ÁREA DE RECUPERACIÓN. Estado: " + obtenerIdsEnRecuperacion()));
        try {
            Thread.sleep(FuncionesComunes.randomBetween(10000, 15000));
        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido en recuperación.");
            Thread.currentThread().interrupt();
        }

        guerreroenRecuperacion.remove(id);
        Platform.runLater(() -> Log.log(id + " sale de ÁREA DE RECUPERACIÓN. Estado: " + obtenerIdsEnRecuperacion()));
    }


    public String obtenerIdsEnRecuperacion() {
        if (enRecuperacion.isEmpty()) return "vacía";
        return String.join(", ", enRecuperacion);
    }

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
