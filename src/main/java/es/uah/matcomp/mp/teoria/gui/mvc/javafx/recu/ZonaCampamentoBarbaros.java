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

    public boolean estaEnCampamento(Barbaro b) {
        return barbarosEnCampamento.contains(b);
    }

    public String obtenerIds() {
        return barbarosEnCampamento.stream()
                .map(Barbaro::getIdBarbaro)
                .reduce((a, b) -> a + ", " + b).orElse("Ninguno");
    }

    public int contar() {
        return barbarosEnCampamento.size();
    }

    public List<Barbaro> getBarbaros() {
        return new ArrayList<>(barbarosEnCampamento);
    }
}

