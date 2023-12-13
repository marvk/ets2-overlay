module net.marvk.ets2overlay {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires jinput;

    opens net.marvk.ets2overlay to javafx.fxml;
    exports net.marvk.ets2overlay;
}
