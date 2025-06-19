package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZonaCampamentoBarbaros {
    private final Set<Barbaro> barbarosEnCampamento = ConcurrentHashMap.newKeySet();

    public void entrarCampamento(Barbaro b) {
        barbarosEnCampamento.add(b);
    }

    public void salirCampamento(Barbaro b) {
        barbarosEnCampamento.remove(b);
    }


    public String obtenerIds() {
        return barbarosEnCampamento.stream()
                .map(Barbaro::getIdBarbaro)
                .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
    }

    public int contarBarbarosEnString(String ids) {
        if (ids == null) return 0;
        String trimmed = ids.trim().toLowerCase();
        if (trimmed.isEmpty() || trimmed.equals("ninguno")) return 0;
        return (int) Arrays.stream(ids.split(","))
                .filter(s -> !s.trim().isEmpty())
                .count();
    }
    public List<Barbaro> getBarbaros() {
        return new ArrayList<>(barbarosEnCampamento);
    }
}

