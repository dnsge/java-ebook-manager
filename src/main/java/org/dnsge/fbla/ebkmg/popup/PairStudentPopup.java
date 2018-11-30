package org.dnsge.fbla.ebkmg.popup;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dnsge.fbla.ebkmg.db.SQLiteConnector;
import org.dnsge.fbla.ebkmg.db.Student;
import org.dnsge.fbla.ebkmg.util.Pair;

import java.sql.SQLException;
import java.util.List;

/**
 * Class that allows for the creation of a popup to select a student for pairing with an ebook
 *
 * @author Daniel Sage
 * @version 0.1
 */
public class PairStudentPopup {

    private Stage myStage;
    private TableView<Student> tableView;
    private Button selectButton;

    private boolean wantSave = false;

    public PairStudentPopup() {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(400, 600);
        root.setPadding(new Insets(5));

        GridPane mainGrid = new GridPane();
        // mainGrid.setHgap(5);
        mainGrid.setVgap(5);
        mainGrid.setPrefSize(375, 600);
        mainGrid.setPadding(new Insets(5));
        mainGrid.setCenterShape(true);

        // table setup

        tableView = new TableView<>();

        TableColumn<Student, String> lastNameColumn = new TableColumn<>();
        TableColumn<Student, String> firstNameColumn = new TableColumn<>();

        lastNameColumn.setText("Last Name");
        firstNameColumn.setText("First Name");

        lastNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastName()));
        firstNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFirstName()));

        tableView.getColumns().add(lastNameColumn);
        tableView.getColumns().add(firstNameColumn);

        double halfWidth = (370f-30) / 2;
        lastNameColumn.setPrefWidth(halfWidth);
        firstNameColumn.setPrefWidth(halfWidth);

        try {
            List<Student> allStudents = SQLiteConnector.getInstance().getStudentDao().queryForAll();
            tableView.setItems(FXCollections.observableArrayList(allStudents));
        } catch (SQLException e) {
            AlertCreator.errorUser("There was an issue fetching all Students");
            e.printStackTrace();
            return;
        }

        GridPane.setHgrow(tableView, Priority.ALWAYS);
        GridPane.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPrefWidth(375);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons

        selectButton = new Button("Pair");
        Button cancelButton = new Button("Cancel");
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, selectButton);

        cancelButton.setOnAction(event -> {
            wantSave = false;
            myStage.close();
        });

        selectButton.setOnAction(event -> {
            wantSave = true;
            myStage.close();
        });


        tableView.setOnMouseClicked(event -> {
            updateTableState();
        });
        updateTableState();
        // Grid and Root setup

        mainGrid.add(tableView, 0, 0);
        mainGrid.add(buttonBox, 0, 1);
        root.getChildren().add(mainGrid);
        AnchorPane.setLeftAnchor(mainGrid, (double)25);
        AnchorPane.setRightAnchor(mainGrid, (double)25);
        Scene myScene = new Scene(root);
        myStage = new Stage();

        myStage.setScene(myScene);
        myStage.setTitle("Pair E-Book with Student");
        myStage.initModality(Modality.APPLICATION_MODAL);
        myStage.setResizable(false);
    }

    private void updateTableState() {
        ObservableList<Student> selectedStudentList = tableView.getSelectionModel().getSelectedItems();
        int size = selectedStudentList.size();
        selectButton.setDisable(size != 1);
    }

    public Pair<Student, Boolean> showAndWait() {
        myStage.showAndWait();
        return new Pair<>(tableView.getSelectionModel().getSelectedItem(), wantSave);
    }

}
