module es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;


    opens es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu to javafx.fxml;
    exports es.uah.matcomp.mp.teoria.gui.mvc.javafx.recu;
}