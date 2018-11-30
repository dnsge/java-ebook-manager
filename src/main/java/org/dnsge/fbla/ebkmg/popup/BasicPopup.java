package org.dnsge.fbla.ebkmg.popup;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Class that creates a basic popup with text only
 */
public class BasicPopup {

    private Stage myStage;

    /**
     * BasicPopup basic constructor
     *
     * @param width Popup width
     * @param height Popup height
     * @param title Popup title & text-header
     * @param text Popup text
     */
    public BasicPopup(int width, int height, String title, String text) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(width, height);
        root.setPadding(new Insets(5));

        VBox all = new VBox(10);
        all.setPrefSize(width, height);

        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font(16));

        Label mainTextLabel = new Label(text);
        mainTextLabel.setWrapText(true);

        all.setPadding(new Insets(10));
        all.getChildren().addAll(titleLabel, mainTextLabel);

        root.getChildren().add(all);

        Scene myScene = new Scene(root);
        myStage = new Stage();

        myStage.setScene(myScene);
        myStage.setTitle(title);
        myStage.initModality(Modality.APPLICATION_MODAL);
        myStage.setResizable(false);
    }

    public void showAndWait() {
        myStage.showAndWait();
    }

}
