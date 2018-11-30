package org.dnsge.fbla.ebkmg.popup;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dnsge.fbla.ebkmg.db.Student;
import org.dnsge.fbla.ebkmg.util.Pair;


/**
 * Class that allows for the creation of popups for new Students
 *
 * @author Daniel Sage
 * @version 0.1
 */
public class NewStudentPopup {

    private Stage myStage;
    private TextField firstNameField;
    private TextField lastNameField;
    private ChoiceBox<String> gradeField;
    private TextField studentIdField;

    private boolean wantSave = false;

    public NewStudentPopup() {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(300, 175);
        root.setPadding(new Insets(5));

        GridPane mainGrid = new GridPane();
        mainGrid.setPrefSize(300, 175);
        GridPane.setHgrow(mainGrid, Priority.ALWAYS);
        GridPane.setVgrow(mainGrid, Priority.ALWAYS);
        GridPane.setMargin(mainGrid, new Insets(5));
        mainGrid.setHgap(5);
        mainGrid.setVgap(7);
        mainGrid.setPadding(new Insets(10));


        Text header = new Text("New Student");
        header.setFont(new Font(20));
        GridPane.setHalignment(header, HPos.CENTER);
        GridPane.setValignment(header, VPos.CENTER);


        Label firstNameLabel = new Label("First Name");
        Label lastNameLabel = new Label("Last Name");
        Label gradeLabel = new Label("Grade");
        Label studentIdLabel = new Label("Student ID");

        HBox buttonsBox = new HBox(5);
        buttonsBox.setPadding(new Insets(5));
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        buttonsBox.getChildren().addAll(saveButton, cancelButton);

        firstNameField = new TextField();
        lastNameField = new TextField();
        gradeField = new ChoiceBox<>();
        studentIdField = new TextField();
        gradeField.setItems(FXCollections.observableArrayList("9", "10", "11", "12"));

        GridPane.setHgrow(firstNameField, Priority.ALWAYS);
        GridPane.setHgrow(lastNameField, Priority.ALWAYS);
        gradeField.setPrefWidth(50);
        GridPane.setHgrow(studentIdField, Priority.ALWAYS);

        mainGrid.add(header, 0, 0);
        mainGrid.add(firstNameLabel, 0, 1);
        mainGrid.add(lastNameLabel, 0, 2);
        mainGrid.add(gradeLabel, 0, 3);
        mainGrid.add(studentIdLabel, 0, 4);
        mainGrid.add(firstNameField, 1, 1);
        mainGrid.add(lastNameField, 1, 2);
        mainGrid.add(gradeField, 1, 3);
        mainGrid.add(studentIdField, 1, 4);
        mainGrid.add(buttonsBox, 1, 5);

        root.getChildren().add(mainGrid);
        Scene myScene = new Scene(root);
        myStage = new Stage();

        myStage.setScene(myScene);
        myStage.setTitle("New Student");
        myStage.initModality(Modality.APPLICATION_MODAL);
        myStage.setResizable(false);

        saveButton.setOnAction(e -> {
            if (filledOutProperly()) {
                if (!Student.studentWithIdExists(studentIdField.getText().trim())) {
                    wantSave = true;
                    myStage.close();
                } else {
                    AlertCreator.errorUser("A Student with that Student ID already exists!");
                }
            } else {
                AlertCreator.errorUser("You need to give a value for each field");
            }
        });

        cancelButton.setOnAction(e -> {
            myStage.close();
        });

    }

    private boolean filledOutProperly() {
        return !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                !gradeField.getSelectionModel().isEmpty() &&
                !studentIdField.getText().trim().isEmpty();
    }

    public Pair<Student, Boolean> showAndWait() {
        myStage.showAndWait();
        return new Pair<>(
                new Student(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        gradeField.getSelectionModel().getSelectedItem(),
                        studentIdField.getText()),
                wantSave);
    }

}
