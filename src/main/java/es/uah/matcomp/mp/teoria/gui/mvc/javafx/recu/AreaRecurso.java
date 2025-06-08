package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AreaRecurso implements Zona {
    private final String tipo;

    private final List<Aldeano> aldeanos = new ArrayList<>();
    private final int MAX_ALDEANOS = 4;

    private boolean enAtaque = false;
    private boolean destruida = false;

    private FuncionesComunes funcionesComunes;

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
    public boolean enfrentarABarbaro(Barbaro b) throws InterruptedException {
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

    public synchronized boolean entrar(Aldeano a) throws InterruptedException {
        if (destruida) {
            Log.log(a.getIdAldeano() + " repara el área de " + tipo);
            Thread.sleep(funcionesComunes.numRandom(3000, 5000));
            destruida = false;
            notifyAll();
            salir(a);
            return false;
        }

        while (aldeanos.size() >= MAX_ALDEANOS || enAtaque) {
            Log.log(a.getIdAldeano() + " espera para entrar al área de " + tipo +
                    (enAtaque ? " (está siendo atacada)" : " (área llena)"));
            wait();
        }

        aldeanos.add(a);
        Log.log(a.getIdAldeano() + " ha entrado al área de " + tipo);
        return true;
    }

    public synchronized boolean fueAtacadoDurante(Aldeano a) {
        return enAtaque;
    }

    public void salir(Aldeano a) {
        synchronized (this) {
            aldeanos.remove(a);
            notifyAll();
        }
    }

    public synchronized void iniciarAtaque() {
        enAtaque = true;
        Log.log("Ataque bárbaro en el área de " + tipo);
    }

    public synchronized void finalizarAtaque(boolean destruir) {
        enAtaque = false;
        if (destruir) {
            destruida = true;
            Log.log("El área de " + tipo + " ha sido destruida y requiere reparación.");
        }

        for (Aldeano a : aldeanos) {
            Log.log(a.getIdAldeano() + " es expulsado del área de " + tipo + " tras el ataque.");
            try {
                salir(a);
                CentroUrbano cu = getCentroDe(a);
                if (cu != null)
                    cu.getAreaRecuperacion().enviarAldeano(a, 12000, 15000);
            } catch (Exception e) {
                Log.log("Error al expulsar a " + a.getIdAldeano() + ": " + e.getMessage());
            }
        }
        aldeanos.clear();

        // Notificar a los guerreros que el ataque terminó
        lockGuerreros.lock();
        try {
            puedeEntrarGuerrero.signalAll();
        } finally {
            lockGuerreros.unlock();
        }

        notifyAll();
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
}
