package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AreaRecurso implements Zona {
    private final String tipo;
    private final List<Aldeano> recolectando = new ArrayList<>();
    private final List<Aldeano> esperandoEnCola = new ArrayList<>();
    private final List<Barbaro> barbarosAtacando = new ArrayList<>();
    private CentroUrbano centro;

    private final ReentrantLock lockZona = new ReentrantLock(true);
    private final Condition puedeEntrarAldeano = lockZona.newCondition();

    private boolean enAtaque = false;
    private boolean destruida = false;
    private boolean enReparacion = false;

    private final ReentrantLock lockGuerreros = new ReentrantLock(true);
    private final Condition puedeEntrarGuerrero = lockGuerreros.newCondition();
    private final List<Guerrero> guerrerosDentro = new ArrayList<>();
    private final int MAX_GUERREROS = 3;

    public AreaRecurso(String tipo, CentroUrbano centro) {
        this.tipo = tipo;
        this.centro = centro;
    }

    public String toString(String tipo) {
        return switch (tipo) {
            case "ORO" -> "mina";
            case "MADERA" -> "bosque";
            case "COMIDA" -> "granja";
            default -> "área desconocida";
        };
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
            for (Guerrero g : guerrerosDentro) {
                if (!g.estaLuchando()) {
                    g.setLuchando(true);
                    Log.log(b.getIdBarbaro() + " se enfrenta a " + g.getIdGuerrero());

                    double probVictoriaGuerrero = g.getProbabilidadVictoria();
                    double random = Math.random();

                    boolean guerreroGana = random < probVictoriaGuerrero;

                    if (guerreroGana) {
                        Log.log(g.getIdGuerrero() + " derrota a " + b.getIdBarbaro());
                        g.setLuchando(false);
                        return false;
                    } else {
                        Log.log(b.getIdBarbaro() + " derrota a " + g.getIdGuerrero());
                        guerrerosDentro.remove(g);
                        g.enviarARecuperacion();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void entrar(Aldeano a) throws InterruptedException {
        lockZona.lock();
        try {
            // Si está destruida y nadie la está reparando, este aldeano la repara
            if (destruida && !enReparacion) {
                esperandoEnCola.add(a);
                enReparacion = true;
                Log.log(a.getIdAldeano() + " va a reparar el área destruida (" + toString(tipo) + ").");

                // Soltamos el lock para no bloquear mientras duerme
                lockZona.unlock();
                try {
                    Thread.sleep(FuncionesComunes.Tiempoaleatorio(3000, 5000));
                } finally {
                    lockZona.lock();
                }
                destruida = false;
                enReparacion = false;
                esperandoEnCola.remove(a);

                Log.log(a.getIdAldeano() + " ha terminado la reparación del área de " + toString(tipo));
                puedeEntrarAldeano.signalAll(); // Avisamos a los demás

                lockZona.unlock();
                try {
                    a.moverAPlazaCentral(); // Va a la plaza después de reparar y sigue ciclo
                } finally {
                    lockZona.lock();
                }
                return;
            }

            // El resto espera si está destruida o en reparación
            esperandoEnCola.add(a);
            while (destruida || enReparacion || recolectando.size() >= 3 || enAtaque) {
                puedeEntrarAldeano.await();
            }

            esperandoEnCola.remove(a);
            recolectando.add(a);
            Log.log(a.getIdAldeano() + " ha entrado a " + toString(tipo));
        } finally {
            lockZona.unlock();
        }
    }

    public void salir(Aldeano a) {
        lockZona.lock();
        try {
            recolectando.remove(a);
            puedeEntrarAldeano.signalAll();
        } finally {
            lockZona.unlock();
        }
    }

    public void iniciarAtaque(Barbaro b) {
        lockZona.lock();
        try {
            enAtaque = true;
            barbarosAtacando.add(b);
            Log.log("Ataque bárbaro en el área de " + tipo + " por " + b.getIdBarbaro());
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

            for (Aldeano a : new ArrayList<>(recolectando)) {
                Log.log(a.getIdAldeano() + " es expulsado del área de " + tipo + " tras el ataque.");
                salir(a);
                try {
                    getCentroDe(a).getAreaRecuperacion().entrar(a);
                } catch (Exception e) {
                    Log.log("Error al expulsar a " + a.getIdAldeano() + ": " + e.getMessage());
                }
            }

            // Aquí debe asegurarse que se vacíe la cola también
            for (Aldeano a : new ArrayList<>(esperandoEnCola)) {
                esperandoEnCola.remove(a);
                getCentroDe(a).getAreaRecuperacion().entrar(a);
            }

            puedeEntrarAldeano.signalAll();
        } finally {
            lockZona.unlock();
        }

        synchronized (barbarosAtacando) {
            barbarosAtacando.clear();
        }

        lockGuerreros.lock();
        try {
            puedeEntrarGuerrero.signalAll();
        } finally {
            lockGuerreros.unlock();
        }
    }

    public synchronized void expulsarAldeanos() {
        List<Aldeano> copiarecolectando = new ArrayList<>(recolectando);
        List<Aldeano> copiaEsperando = new ArrayList<>(esperandoEnCola);

            for (Aldeano a : copiarecolectando) {
                a.interrupt();
            }
            for (Aldeano a : copiaEsperando) {
                a.interrupt();
            }

            recolectando.clear();
            esperandoEnCola.clear();
    }

    public boolean fueAtacadoDurante(Aldeano a) {
        lockZona.lock();
        try {
            return enAtaque;
        } finally {
            lockZona.unlock();
        }
    }

    public void eliminarBarbaro(Barbaro b) {
        synchronized (barbarosAtacando) {
            barbarosAtacando.remove(b);
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

            StringBuilder guerr = new StringBuilder();
            for (Guerrero g : guerrerosDentro) {
                guerr.append(g.getIdGuerrero()).append(", ");
            }
            if (guerr.length() > 0) guerr.setLength(guerr.length() - 2);

            StringBuilder barb = new StringBuilder();
            for (Barbaro b : barbarosAtacando) {
                barb.append(b.getIdBarbaro()).append(", ");
            }
            if (barb.length() > 0) barb.setLength(barb.length() - 2);

            return "Recolectando: " + dentroSb + "\nEsperando: " + colaSb + "\nGuerreros: " + guerr + "\nBárbaros: " + barb;
        } finally {
            lockZona.unlock();
        }
    }

    public List<Aldeano> getAldeanos() {
        lockZona.lock();
        try {
            List<Aldeano> todos = new ArrayList<>();
            todos.addAll(recolectando);
            todos.addAll(esperandoEnCola);
            return todos;
        } finally {
            lockZona.unlock();
        }
    }

    public List<Guerrero> getGuerreros() {
        lockGuerreros.lock();
        try {
            return new ArrayList<>(guerrerosDentro);
        } finally {
            lockGuerreros.unlock();
        }
    }

    public List<Barbaro> getBarbaros() {
        synchronized (barbarosAtacando) {
            return new ArrayList<>(barbarosAtacando);
        }
    }
}
