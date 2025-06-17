package es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AreaRecurso implements Zona {
    private final String tipo;
    private final List<Aldeano> recolectando = new ArrayList<>();
    private final List<Aldeano> esperandoEnCola = new ArrayList<>();
    private final List<Barbaro> barbarosAtacando = new ArrayList<>();
    private AreaRecuperacion areaRecuperacion;


    private final ReentrantLock lockZona = new ReentrantLock(true);
    private final Condition puedeEntrarAldeano = lockZona.newCondition();

    private boolean enAtaque = false;
    private boolean destruida = false;

    private final ReentrantLock lockGuerreros = new ReentrantLock(true);
    private final Condition puedeEntrarGuerrero = lockGuerreros.newCondition();
    private final List<Guerrero> guerrerosDentro = new ArrayList<>();
    private final int MAX_GUERREROS = 3;

    // Constructor
    public AreaRecurso(String tipo) {
        this.tipo = tipo;
    }
    public String toString(String tipo) {
        return switch (tipo) {
            case "ORO" -> "mina";
            case "MADERA" -> "bosque";
            case "COMIDA" -> "granja";
            default -> "área desconocida";
        };
    }


    // Funciones Interface Zona
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

                    double probVictoriaGuerrero = g.getProbabilidadVictoria(); // ej: 0.5 + mejora
                    double random = Math.random();

                    boolean guerreroGana = random < probVictoriaGuerrero;

                    if (guerreroGana) {
                        Log.log(g.getIdGuerrero() + " derrota a " + b.getIdBarbaro());
                        // Aquí podrías llamar a un método para que el bárbaro pierda o salga
                        // b.serDerrotado(); // si tienes este método
                        g.setLuchando(false); // Guerrero ya libre para pelear otra vez
                        return false;  // Indica que bárbaro NO ganó (guerrero ganó)
                    } else {
                        Log.log(b.getIdBarbaro() + " derrota a " + g.getIdGuerrero());
                        guerrerosDentro.remove(g); // Guerrero derrotado, sale de zona
                        g.enviarARecuperacion();
                        // No ponemos g.setLuchando(false) porque guerrero ya eliminado de zona
                        return true; // Indica que bárbaro ganó
                    }
                }
            }
            // No hay guerreros disponibles para pelear
            return false;
        }
    }
    // Acceso al área de recurso
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
            Log.log(a.getIdAldeano() + " ha entrado a " +toString(tipo));

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
    // Ataque
    public void iniciarAtaque(Barbaro b) {
        lockZona.lock();
        try {
            enAtaque = true;
            synchronized (barbarosAtacando) {
                barbarosAtacando.add(b);
            }
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

            // Expulsar aldeanos
            for (Aldeano a : new ArrayList<>(recolectando)) {
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
        // Limpiar bárbaros que estaban atacando
        synchronized (barbarosAtacando) {
            barbarosAtacando.clear();
        }
        // Notificar a los guerreros
        lockGuerreros.lock();
        try {
            puedeEntrarGuerrero.signalAll();
        } finally {
            lockGuerreros.unlock();
        }
    }
    public synchronized void expulsarAldeanos() {
        for (Aldeano a : recolectando) {
            areaRecuperacion.enviarAldeano(a,12000,15000);
            a.interrupt();
        }
        for (Aldeano a : esperandoEnCola) {
            areaRecuperacion.enviarAldeano(a,12000,15000);
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
            for (Guerrero g : getGuerreros()) {
                guerr.append(g.getIdGuerrero()).append(", ");
            }
            if (guerr.length() > 0) guerr.setLength(guerr.length() - 2);
            StringBuilder barb = new StringBuilder();
            for (Barbaro b : getBarbaros()) {
                barb.append(b.getIdBarbaro()).append(", ");
            }
            if (barb.length() > 0) barb.setLength(barb.length() - 2);


            return "Recolectando: "+ dentroSb + "\nEsperando: " + colaSb +"\nGuerreros: "+guerr+"\nBárbaros: "+barb;
        } finally {
            lockZona.unlock();
        }
    }
    public List<Aldeano> getAldeanos() {
        lockZona.lock();
        try {
            // Devuelvo una nueva lista con todos los aldeanos de las tres listas internas
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
            // Devuelvo copia de la lista de guerreros dentro
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
