module com.buddybank.mysuperbnak {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    opens com.buddybank.mysuperbnak to javafx.fxml;
    exports com.buddybank.mysuperbnak;
}