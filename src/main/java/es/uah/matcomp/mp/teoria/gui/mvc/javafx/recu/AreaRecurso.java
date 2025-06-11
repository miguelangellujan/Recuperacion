package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AreaRecurso implements Zona {
    private final String tipo;

    private final List<Aldeano> recolectando = new ArrayList<>();
    private final List<Aldeano> esperandoEnCola = new ArrayList<>();
    private final List<Aldeano> dentro = new ArrayList<>();

    private final ReentrantLock lockZona = new ReentrantLock(true);
    private final Condition puedeEntrarAldeano = lockZona.newCondition();

    private boolean enAtaque = false;
    private boolean destruida = false;

    private final ReentrantLock lockGuerreros = new ReentrantLock(true);
    private final Condition puedeEntrarGuerrero = lockGuerreros.newCondition();
    private final List<Guerrero> guerrerosDentro = new ArrayList<>();
    private final int MAX_GUERREROS = 3;

    public AreaRecurso(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean entrarGuerrero(Guerrero g) throws InterruptedException {
        lockGuerreros.lock();
        try {
            while (guerrerosDentro.size() >= MAX_GUERREROS || enAtaque) {
                puedeEntrarGuerrero.await();
            }
            guerrerosDentro.add(g);
            return true;
        } finally {
            lockGuerreros.unlock();
        }
    }

    @Override
    public void salirGuerrero(Guerrero g) {
        lockGuerreros.lock();
        try {
            guerrerosDentro.remove(g);
            puedeEntrarGuerrero.signalAll();
        } finally {
            lockGuerreros.unlock();
        }
    }

    @Override
    public String getNombreZona() {
        return "Área de " + tipo;
    }

    @Override
    public boolean enfrentarABarbaro(Barbaro b) {
        synchronized (guerrerosDentro) {
            if (guerrerosDentro.isEmpty()) return false;

            Guerrero g = guerrerosDentro.remove(0);
            Log.log(b.getIdBarbaro() + " se enfrenta a " + g.getIdGuerrero());

            boolean barbaroGana = new Random().nextBoolean();
            if (barbaroGana) {
                Log.log(b.getIdBarbaro() + " derrota a " + g.getIdGuerrero());
                return true;
            } else {
                Log.log(b.getIdBarbaro() + " pierde contra " + g.getIdGuerrero());
                g.enviarARecuperacion();
                return false;
            }
        }
    }

    public void entrar(Aldeano a) throws InterruptedException {
        lockZona.lock();
        try {
            if (destruida) {
                Log.log(a.getIdAldeano() + " no puede entrar, el área está destruida.");
                return;
            }

            esperandoEnCola.add(a);
            while (recolectando.size() >= 3 || enAtaque) {
                puedeEntrarAldeano.await();
            }
            esperandoEnCola.remove(a);
            recolectando.add(a);

            while (dentro.size() >= 4 || enAtaque) {
                Log.log(a.getIdAldeano() + " espera para entrar al área de " + tipo +
                        (enAtaque ? " (está siendo atacada)" : " (área llena)"));
                puedeEntrarAldeano.await();
            }

            dentro.add(a);
            Log.log(a.getIdAldeano() + " ha entrado al área de " + tipo);

        } finally {
            lockZona.unlock();
        }
    }

    public void salir(Aldeano a) {
        lockZona.lock();
        try {
            recolectando.remove(a);
            dentro.remove(a);
            puedeEntrarAldeano.signalAll();
        } finally {
            lockZona.unlock();
        }
    }

    public void iniciarAtaque() {
        lockZona.lock();
        try {
            enAtaque = true;
            Log.log("Ataque bárbaro en el área de " + tipo);
        } finally {
            lockZona.unlock();
        }
    }

    public void finalizarAtaque(boolean destruir) {
        lockZona.lock();
        try {
            enAtaque = false;
            if (destruir) {
                destruida = true;
                Log.log("El área de " + tipo + " ha sido destruida y requiere reparación.");
            }

            for (Aldeano a : new ArrayList<>(dentro)) {
                Log.log(a.getIdAldeano() + " es expulsado del área de " + tipo + " tras el ataque.");
                salir(a);
                try {
                    CentroUrbano cu = getCentroDe(a);
                    if (cu != null)
                        cu.getAreaRecuperacion().enviarAldeano(a, 12000, 15000);
                } catch (Exception e) {
                    Log.log("Error al expulsar a " + a.getIdAldeano() + ": " + e.getMessage());
                }
            }

            puedeEntrarAldeano.signalAll();

        } finally {
            lockZona.unlock();
        }

        // Notificar también a los guerreros
        lockGuerreros.lock();
        try {
            puedeEntrarGuerrero.signalAll();
        } finally {
            lockGuerreros.unlock();
        }
    }

    private CentroUrbano getCentroDe(Aldeano a) {
        try {
            java.lang.reflect.Field campo = Aldeano.class.getDeclaredField("centro");
            campo.setAccessible(true);
            return (CentroUrbano) campo.get(a);
        } catch (Exception e) {
            return null;
        }
    }

    public String obtenerEstadoAldeanos() {
        lockZona.lock();
        try {
            StringBuilder dentroSb = new StringBuilder();
            for (Aldeano a : recolectando) {
                dentroSb.append(a.getIdAldeano()).append(", ");
            }
            if (dentroSb.length() > 0) dentroSb.setLength(dentroSb.length() - 2);

            StringBuilder colaSb = new StringBuilder();
            for (Aldeano a : esperandoEnCola) {
                colaSb.append(a.getIdAldeano()).append(", ");
            }
            if (colaSb.length() > 0) colaSb.setLength(colaSb.length() - 2);

            return "Recolectando: "+ dentroSb + "\n\nEsperando: " + colaSb ;
        } finally {
            lockZona.unlock();
        }
    }

    public boolean fueAtacadoDurante(Aldeano a) {
        lockZona.lock();
        try {
            return enAtaque;
        } finally {
            lockZona.unlock();
        }
    }
}
