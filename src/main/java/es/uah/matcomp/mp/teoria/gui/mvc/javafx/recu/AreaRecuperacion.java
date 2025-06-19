package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import javafx.application.Platform;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AreaRecuperacion {
    private final Set<String> enRecuperacion = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> guerreroenRecuperacion = Collections.synchronizedSet(new HashSet<>());

    private final Object lock = new Object();

    public void entrar(Aldeano a) {
        String id = a.getIdAldeano();
        boolean primeraVez = false;

        synchronized (lock) {
            if (!enRecuperacion.contains(id)) {
                enRecuperacion.add(id);
                primeraVez = true;
            }
        }

        if (primeraVez) {
            Log.log(id + " entra en ÁREA DE RECUPERACIÓN");
        }

        try {
            Thread.sleep(FuncionesComunes.Tiempoaleatorio(12000, 15000));
        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido durante la recuperación.");
        }

        synchronized (lock) {
            enRecuperacion.remove(id);
            Log.log(id + " sale de ÁREA DE RECUPERACIÓN");
            lock.notifyAll();
        }
        a.moverAPlazaCentral();
    }
    public void entrarGuerrero(Guerrero g) {
        String id = g.getIdGuerrero();
        boolean primeraVez = false;

        synchronized (lock) {
            if (!guerreroenRecuperacion.contains(id)) {
                guerreroenRecuperacion.add(id);
                primeraVez = true;
            }
        }

        if (primeraVez) {
            Platform.runLater(() -> Log.log(id + " entra en ÁREA DE RECUPERACIÓN"));
        }

        try {
            Thread.sleep(FuncionesComunes.Tiempoaleatorio(10000, 15000));
        } catch (InterruptedException e) {
            Log.log(id + " fue interrumpido en recuperación.");
            Thread.currentThread().interrupt();
        }

        synchronized (lock) {
            guerreroenRecuperacion.remove(id);
        }
        Platform.runLater(() -> Log.log(id + " sale de ÁREA DE RECUPERACIÓN"));
    }

    public String obtenerIdsEnRecuperacion() {
        synchronized (lock) {
            if (enRecuperacion.isEmpty() && guerreroenRecuperacion.isEmpty()) return "vacía";
            return String.join(", ", enRecuperacion) + (guerreroenRecuperacion.isEmpty() ? "" : ", " + String.join(", ", guerreroenRecuperacion));
        }
    }
    public List<String> getAldeanosEnRecuperacion() {
        synchronized (lock) {
            return enRecuperacion.stream()
                    .filter(id -> id.startsWith("Aldeano"))
                    .collect(Collectors.toList());
        }
    }


}
