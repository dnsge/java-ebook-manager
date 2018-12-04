package org.dnsge.fbla.ebkmg.popup;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that creates a Popup window with multiple pages of text
 * based off of a XML configuration file
 *
 * @author Daniel Sage
 * @version 0.2
 */
public class PagedPopup {

    private Stage myStage;

    public PagedPopup(InputStream inStream) throws ParserConfigurationException, IOException, SAXException {
        // Load and parse the XML config file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inStream);
        PagedPopupConfig config = new PagedPopupConfig(doc);

        int width = config.width;
        int height = config.height;
        String title = config.title;
        List<String> allPages = config.pages;

        SimpleIntegerProperty pageNum = new SimpleIntegerProperty(0);

        // Create containers
        AnchorPane root = new AnchorPane();
        root.setPrefSize(width, height);
        root.setPadding(new Insets(5));

        VBox all = new VBox(10);
        all.setPrefSize(width, height);

        HBox navButtonBox = new HBox(10);
        navButtonBox.setPrefSize(width, 30);

        // Create lables
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 18d));

        Label mainTextLabel = new Label(allPages.get(pageNum.intValue()));
        mainTextLabel.setWrapText(true);
        // mainTextLabel.setPrefHeight(height - 30 - 10);
        mainTextLabel.setTextAlignment(TextAlignment.LEFT);

        // Navigation buttons
        Button nextButton = new Button(config.nextButtonText);
        Button prevButton = new Button(config.prevButtonText);
        nextButton.setDisable(allPages.size() == 1);
        prevButton.setDisable(true);

        Label pageNumLabel = new Label("Page 1 of " + allPages.size());
        pageNumLabel.setFont(new Font(8));
        pageNumLabel.setPrefWidth(50 + wholeQuotient(allPages.size(), 10) / 2d);
        pageNumLabel.setTextAlignment(TextAlignment.CENTER);

        navButtonBox.setAlignment(Pos.CENTER_RIGHT);
        navButtonBox.getChildren().addAll(prevButton, pageNumLabel, nextButton);

        pageNum.addListener((observable, oldValue, newValue) -> {
            nextButton.setDisable(newValue.intValue() + 1 >= allPages.size());
            prevButton.setDisable(newValue.intValue() == 0);
            mainTextLabel.setText(allPages.get(newValue.intValue()));
            pageNumLabel.setText(String.format("Page %d of %d", pageNum.intValue() + 1, allPages.size()));
        });

        nextButton.setOnAction(event -> {
            if (pageNum.intValue() + 1 < allPages.size()) {
                pageNum.set(pageNum.intValue() + 1);
            }
        });

        prevButton.setOnAction(e -> {
            if (pageNum.intValue() != 0) {
                pageNum.set(pageNum.intValue() - 1);
            }
        });

        // Pack everything
        all.setPadding(new Insets(10));
        all.getChildren().addAll(titleLabel, mainTextLabel);

        root.getChildren().addAll(all, navButtonBox);

        AnchorPane.setBottomAnchor(navButtonBox, 10d);

        Scene myScene = new Scene(root);
        myStage = new Stage();

        myStage.setScene(myScene);
        myStage.setTitle(title);
        myStage.initModality(Modality.APPLICATION_MODAL);
        myStage.setResizable(false);
    }


    /**
     * Inner class to parse the values inside the selected XML document
     */
    private static class PagedPopupConfig {

        final int width;
        final int height;
        final String title;
        final String nextButtonText;
        final String prevButtonText;
        final List<String> pages;

        PagedPopupConfig(Document xmlDoc) {
            xmlDoc.getDocumentElement().normalize();

            Node widthNode = xmlDoc.getElementsByTagName("width").item(0);
            Node heightNode = xmlDoc.getElementsByTagName("height").item(0);
            Node titleNode = xmlDoc.getElementsByTagName("title").item(0);
            Node nextNode = xmlDoc.getElementsByTagName("nextButton").item(0);
            Node prevNode = xmlDoc.getElementsByTagName("prevButton").item(0);

            try {
                width = Integer.parseInt(widthNode.getTextContent());
                height = Integer.parseInt(heightNode.getTextContent());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid width or height in XML doc");
            }

            title = titleNode.getTextContent();
            nextButtonText = nextNode.getTextContent();
            prevButtonText = prevNode.getTextContent();

            NodeList nodePages = xmlDoc.getElementsByTagName("page");
            List<String> stringPages = new ArrayList<>();

            for (int i = 0; i < nodePages.getLength(); i++) {
                stringPages.add(nodePages.item(i).getTextContent());
            }

            pages = stringPages;

        }

    }

    /**
     * Gets the whole number quotient from a division
     *
     * @param dividend Number to divide
     * @param divisor Number to divide by
     * @return whole part of dividend / divison
     */
    private int wholeQuotient(int dividend, int divisor) {
        return dividend - (dividend % divisor);
    }

    /**
     * Shows the popup
     */
    public void showAndWait() {
        myStage.showAndWait();
    }

}
