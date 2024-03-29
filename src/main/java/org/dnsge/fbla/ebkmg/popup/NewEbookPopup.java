package org.dnsge.fbla.ebkmg.popup;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import org.dnsge.fbla.ebkmg.db.Ebook;
import org.dnsge.fbla.ebkmg.util.Pair;

/**
 * Class that allows for the creation of popups for new Ebooks
 *
 * @author Daniel Sage
 * @version 0.1
 */
public class NewEbookPopup {

    private TextField nameField;
    private TextField redemptionField;
    private Stage myStage;

    private boolean wantSave = false;

    public NewEbookPopup() {
        // Create containers
        AnchorPane root = new AnchorPane();
        root.setPrefSize(300, 150);
        root.setPadding(new Insets(5));

        GridPane mainGrid = new GridPane();
        mainGrid.setPrefSize(300, 150);
        GridPane.setHgrow(mainGrid, Priority.ALWAYS);
        GridPane.setVgrow(mainGrid, Priority.ALWAYS);
        GridPane.setMargin(mainGrid, new Insets(5));
        mainGrid.setHgap(5);
        mainGrid.setVgap(7);
        mainGrid.setPadding(new Insets(10));

        // Create labels
        Text header = new Text("New Ebook");
        header.setFont(new Font(20));
        GridPane.setHalignment(header, HPos.CENTER);
        GridPane.setValignment(header, VPos.CENTER);
        GridPane.setHgrow(header, Priority.ALWAYS);

        Label nameLabel = new Label("Ebook Name");
        Label redemptionLabel = new Label("Redemption Code");

        // Create buttons
        HBox buttonsBox = new HBox(5);
        buttonsBox.setPadding(new Insets(5));
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        // Create text fields
        nameField = new TextField();
        redemptionField = new TextField();

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(redemptionField, Priority.ALWAYS);

        // Fill containers
        buttonsBox.getChildren().addAll(saveButton, cancelButton);

        mainGrid.add(header, 0, 0);
        mainGrid.add(nameLabel, 0, 1);
        mainGrid.add(redemptionLabel, 0, 2);
        mainGrid.add(nameField, 1, 1);
        mainGrid.add(redemptionField, 1, 2);
        mainGrid.add(buttonsBox, 1, 3);

        root.getChildren().add(mainGrid);

        // Set up scene & stage & event listeners
        Scene myScene = new Scene(root);
        myStage = new Stage();

        myStage.setScene(myScene);
        myStage.setTitle("New Ebook");
        myStage.initModality(Modality.APPLICATION_MODAL);
        myStage.setResizable(false);

        saveButton.setOnAction(e -> {
            if (filledOutProperly()) {
                if (!Ebook.exists(redemptionField.getText().trim())) {
                    System.out.println(redemptionField.getText().trim());
                    System.out.println(redemptionField.getText().trim().isEmpty());
                    wantSave = true;
                    myStage.close();
                } else {
                    AlertCreator.errorUser("An Ebook with that code already exists!");
                }
            } else {
                AlertCreator.errorUser("You need to give a value for each field");
            }
        });

        cancelButton.setOnAction(e -> myStage.close());

        root.setPadding(new Insets(5));

    }

    /**
     * @return whether the fields are properly filled out and ready for submission
     */
    private boolean filledOutProperly() {
        return !nameField.getText().trim().isEmpty() &&
                !redemptionField.getText().trim().isEmpty();
    }

    public Pair<Ebook, Boolean> showAndWait() {
        myStage.showAndWait();
        return new Pair<>(new Ebook(nameField.getText().trim(), redemptionField.getText().trim(), null), wantSave);
    }

}
