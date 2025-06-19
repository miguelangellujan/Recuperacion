package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.Random;

public class FuncionesComunes {
    private static final Random rnd = new Random();

    public static int Tiempoaleatorio(int min, int max) {
        return rnd.nextInt(max - min + 1) + min;
    }
}
