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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that creates a Popup window with multiple pages of text
 * based off of a XML configuration file
 *
 * @author Daniel Sage
 * @version 0.1
 */
public class PagedPopup {

    private static String workaround = "<?xml version=\"1.0\"?>\n" +
            "<multiPagePopup>\n" +
            "    <size>\n" +
            "        <width>350</width>\n" +
            "        <height>175</height>\n" +
            "    </size>\n" +
            "    <title>User Guide</title>\n" +
            "    <nextButton>Next</nextButton>\n" +
            "    <prevButton>Prev</prevButton>\n" +
            "    <pages>\n" +
            "        <page>To get started, create a new database file by selecting ‘New Database’ in the ‘File’ dropdown menu. Save the file in a directory of your choosing.</page>\n" +
            "        <page>Under the ‘File’ menu, use ‘Connect to Database’ to connect to an already existing database file.</page>\n" +
            "        <page>Use the ‘Close Connection’ option to disconnect from the database. This automatically done on closing the application or upon connecting to another database.</page>\n" +
            "        <page>To create a new student entry or Ebook entry, select the appropriate tab and click ‘Add Student’ or ‘New Ebook’. Fill in the appropriate details in the popup.</page>\n" +
            "        <page>To view details about a Student or an Ebook, navigate to the appropriate tab and click on the desired record in the table.</page>\n" +
            "        <page>To modify information about a record, use the fields on the right side of the tab. Changed information is highlighted in a black border. Click the ‘Update Information’ button to save your changes.</page>\n" +
            "        <page>To cancel your changes or deselect the record in the table, press the ‘Cancel’ button.</page>\n" +
            "        <page>To delete a record from the database, press the ‘Delete Record’ button. Alternatively, under the ‘Edit’ menu, select ‘Delete record’.</page>\n" +
            "        <page>Ebooks can be paired to students. When an ebook is selected in the ebook tab, press the ‘Pair’ button to pair the ebook to a student. Press ‘View Student’ to jump to the student in the Student tab.</page>\n" +
            "        <page>To view the ebook paired with a specific student, under the student tab, select a student and then press ‘View Ebook’. This option is only available if the ‘Paired’ checkbox is enabled. Press ‘Unpair’ to unpair.</page>\n" +
            "        <page>Press the ‘Generate Report’ button to create a .PDF file detailing who has what book currently paired to them.</page>\n" +
            "        <page>Under the ‘File’ menu, select ‘Export to .csv’ to export both the students and ebooks to their respective .csv file in a directory. (Note: Creates two separate .csv files)</page>\n" +
            "    </pages>\n" +
            "</multiPagePopup>\n";

    private Stage myStage;

    public PagedPopup() throws ParserConfigurationException, IOException, SAXException {
        // Load and parse the XML config file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse(xmlDoc);
        Document doc = builder.parse(new InputSource(new StringReader(workaround)));
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
