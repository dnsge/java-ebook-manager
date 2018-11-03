package org.dnsge.fbla.ebkmg;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.dnsge.fbla.ebkmg.extensions.ChangeWrapperHolder;
import org.dnsge.fbla.ebkmg.extensions.ChoiceBoxWrapper;
import org.dnsge.fbla.ebkmg.extensions.TextFieldWrapper;
import org.dnsge.fbla.ebkmg.models.Ebook;
import org.dnsge.fbla.ebkmg.models.Student;
import org.dnsge.fbla.ebkmg.util.Pair;
import org.dnsge.fbla.ebkmg.util.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static javafx.scene.control.Alert.AlertType;


/**
 * Controller for the main JavaFX view
 *
 * @author Daniel Sage
 * @version 0.2
 */
public final class MainPageController {
    // Menu bar stuff
    // todo: add more buttons/functionality to menubar
        @FXML private MenuBar  menuBar;
        @FXML private MenuItem connectToDatabase, closeConnection;
    // Table related stuff
        @FXML private TableView<Student> mainTable;
        @FXML private TableColumn<Student, String> lastNameColumn;
        @FXML private TableColumn<Student, String> firstNameColumn;
    // Right side of the screen input nodes
        @FXML private TextField firstNameField, lastNameField, studentIdField, ebookCodeField;
        @FXML private TextField ebookNameField, redemptionDateField;
        @FXML private ChoiceBox<String> studentGradeDropdown;
        @FXML private Button updateDataButton;
        @FXML private Button cancelUpdateButton;
        @FXML private Button deleteRecordButton;
    // Bottom toolbar stuff
        @FXML private ToolBar buttonsToolbar;
        @FXML private Button newRecordButton;
        @FXML private Button generateReportButton; // Todo: this

    private Student selected;
    private SQLiteConnector sqLiteConnector = SQLiteConnector.getInstance();

    // IChangeWrappers and ChangeWrapperHolder
    private TextFieldWrapper firstName, lastName, studentId, ebookCode;
    private ChoiceBoxWrapper<String> studentGrade;
    private ChangeWrapperHolder wrapperHolder;

    /**
     * Called by JavaFX once all FXML fields/nodes have been created/registered
     */
    @FXML
    public void initialize() {
        registerMenuBarInteractions();
        registerStudentTableDataInteractions();
        registerToolBarInteractions();
        createWrappers();
    }

    /**
     * Registers event listeners for things related to the main table
     */
    private void registerStudentTableDataInteractions() {
        // Set column cell value factories
        lastNameColumn.setCellValueFactory(  param -> new SimpleStringProperty(param.getValue().getLastName())  );
        firstNameColumn.setCellValueFactory( param -> new SimpleStringProperty(param.getValue().getFirstName()) );

        mainTable.setOnMouseClicked((MouseEvent event) -> {
            ObservableList<Student> selectedStudentList = mainTable.getSelectionModel().getSelectedItems();
            if (selectedStudentList.size() > 0) {
                setDisableOnInteractions(false);
                selected = selectedStudentList.get(0);
                loadTextFieldsFromStudent(selected);
                resetFieldsStyle();
                wrapperHolder.updateAll();

                System.out.println(Objects.requireNonNull(selected.getOwnedEbook()).getAssignmentDate());
            }
        });

        updateDataButton.setOnAction(event -> {
            ObservableList<Student> selectedStudentList = mainTable.getSelectionModel().getSelectedItems();

            // Make sure we have actually selected a row
            if (selectedStudentList.size() > 0) {
                if (selected.equals(selectedStudentList.get(0))) { // Check if its the same object, should always be

                    // Create backup and save
                    Student.Memento preservedStudent = selected.saveToMemento();
                    saveTextFieldsToStudent(selected);

                    try {
                        boolean codeUsed = Student.codeUsed(ebookCode.toString(), selected);
                        if (codeUsed) {
                            ebookCode.highlightError();
                            selected.loadFromMemento(preservedStudent);

                            warnUser("This E-Book code has already been paired with another student.");

                            return;
                        }
                    } catch (SQLException ignored) {}


                    try {
                        ConnectionSource connectionSource = sqLiteConnector.getConnectionSource();

                        // Use transactionManager to cancel changes if something goes wrong
                        TransactionManager.callInTransaction(connectionSource, (Callable<Void>) () -> {
                            sqLiteConnector.getStudentDao().update(selected);
                            return null;
                        });

                        resetFieldsStyle();

                    } catch (SQLException e) {
                        // If unique failed, then ebook code is already used
                        if (e.getCause().getMessage().contains("constraint failed")) {
                            // reload from backup
                            selected.loadFromMemento(preservedStudent);
                            loadTextFieldsFromStudent(selected);

                            resetFieldsStyle();
                            ebookCode.highlightError();
                            warnUser("This E-Book code has already been used!");

                            // TODO: add error message popup/label
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mainTable.refresh();
            wrapperHolder.updateAll();
        });

        cancelUpdateButton.setOnAction(event -> {
            if (wrapperHolder.anyChanged()) {
                if (!askYesOrNo("You have unsaved changes, are you sure you want to cancel?")) {
                    return;
                }
            }
            finishChanges();
        });

        deleteRecordButton.setOnAction(event -> {
            if (askYesOrNo("Are you sure you want to delete this record?")) {
                try {
                    sqLiteConnector.getStudentDao().delete(selected);
                    List<Student> allStudents = sqLiteConnector.getStudentDao().queryForAll();
                    mainTable.setItems(FXCollections.observableArrayList(allStudents));
                    mainTable.refresh();
                    finishChanges();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Registers event listeners for things related to the menu bar
     */
    private void registerMenuBarInteractions() {
        // Bind 'Connect to Database' menu button
        connectToDatabase.setOnAction((ActionEvent event) -> {
            if (wrapperHolder.anyChanged()) {
                if (!askYesOrNo("You have unsaved changes, are you sure you want to close your previous connection and open a new one?")) {
                    return;
                }
            }


            File databaseFile = Utils.openFilePickerDialog("Select Database", "L:/ebook_data/", menuBar.getScene().getWindow());
            if (databaseFile == null) {
                return;
            }

            wrapperHolder.clearAll();
            wrapperHolder.clearAllStyle();
            setDisableOnInteractions(true);

            try {
                // Connect database
                sqLiteConnector.connect(databaseFile.getAbsolutePath());
                ConnectionSource connectionSource = sqLiteConnector.getConnectionSource();

                // Create database tables if they don't exist
                TableUtils.createTableIfNotExists(connectionSource, Student.class);
                TableUtils.createTableIfNotExists(connectionSource, Ebook.class);

                // Get all students
                List<Student> allStudents = sqLiteConnector.getStudentDao().queryForAll();

                // Generate list and set the items
                mainTable.setItems(FXCollections.observableArrayList(allStudents));
                closeConnection.setDisable(false);

            } catch (SQLException | IOException e) {
                e.printStackTrace();
                // todo: error popup
            }
        });

        closeConnection.setOnAction(e -> {
            if (wrapperHolder.anyChanged()) {
                if (!askYesOrNo("You have unsaved changes, are you sure you want to close your connection?")) {
                    return;
                }
            }

            try {
                sqLiteConnector.disconnectIfConnected();
                wrapperHolder.clearAll();
                wrapperHolder.clearAllStyle();

                setDisableOnInteractions(true);

                closeConnection.setDisable(true);
                mainTable.setItems(FXCollections.observableArrayList());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    /**
     * Registers event listeners for things related to the toolbar
     */
    private void registerToolBarInteractions() {
        // Set listener for when database connection state is changed
        // If connected, enable bottom toolbar, else disable it
        sqLiteConnector.getConnectedProperty().addListener((observable, oldValue, newValue) -> buttonsToolbar.setDisable(!newValue));

        newRecordButton.setOnAction(event -> {
            // todo: set up popup window to make new record

        });
    }


    /**
     * Creates the IChangeWrappers for the Nodes
     */
    private void createWrappers() {
        firstName = new TextFieldWrapper(firstNameField);
        lastName = new TextFieldWrapper(lastNameField);
        studentId = new TextFieldWrapper(studentIdField);
        ebookCode = new TextFieldWrapper(ebookCodeField);
        studentGrade = new ChoiceBoxWrapper<>(studentGradeDropdown);
        wrapperHolder = new ChangeWrapperHolder(firstName, lastName, studentId, ebookCode, studentGrade);

        studentGrade.setItems("9", "10", "11", "12");
    }

    /**
     * Sets whether the input nodes on the right half of the screen should be disabled
     *
     * @param disabled Whether the input methods should be disabled
     */
    private void setDisableOnInteractions(boolean disabled) {
        wrapperHolder.setAllDisabled(disabled);
        updateDataButton.setDisable(disabled);
        cancelUpdateButton.setDisable(disabled);
        deleteRecordButton.setDisable(disabled);
    }

    /**
     * Clears the text fields and resets their style
     */
    private void clearTextFields() {
        wrapperHolder.clearAll();

        resetFieldsStyle();
    }

    /**
     * Resets the style of the text fields
     */
    private void resetFieldsStyle() {
        wrapperHolder.clearAllStyle();
    }

    /**
     * Deselects the row in the table and clears/disables the text fields
     */
    private void finishChanges() {
        clearTextFields();
        setDisableOnInteractions(true);
        mainTable.getSelectionModel().clearSelection();
    }

    /**
     * Fills in the text fields based off of a Student's information
     *
     * @param stu Student to load from
     */
    private void loadTextFieldsFromStudent(Student stu) {
        firstName.setValue(stu.getFirstName());
        lastName.setValue(stu.getLastName());
        studentGrade.setValue(stu.getGrade());
        studentId.setValue(stu.getStudentId());
        if (stu.hasEbook()) {
            ebookCode.setValue(stu.getEbookCode());
        } else {
            ebookCode.clear();
        }
    }

    /**
     * Saves the information in the text fields to a Student
     *
     * @param stu Student to save to
     */
    private void saveTextFieldsToStudent(Student stu) {
        stu.setFirstName(firstName.asText());
        stu.setLastName(lastName.asText());
        stu.setGrade(studentGrade.asText());
        stu.setStudentId(studentId.asText());
        stu.setEbookCode(ebookCode.asText());

        stu.setHasEbook(!stu.getEbookCode().isEmpty());
    }

    /**
     * Confirms that the user want's to do something
     *
     * @param prompt String prompt to show the user
     * @return If the user clicked Yes
     */
    private boolean askYesOrNo(String prompt) {
        Alert alert = new Alert(AlertType.CONFIRMATION, prompt, ButtonType.NO, ButtonType.YES);
        alert.setTitle("Confirm");
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }

    private void warnUser(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    /**
     * @param baseStudent Student that is being popped up
     * @return {@code Pair<String, Boolean>} object with the value of the new code and if the user clicked save or not
     */
    private Pair<String, Boolean> showBookPopup(Student baseStudent) {
        BookPopup p = new BookPopup(baseStudent);
        return p.showAndWait();
    }
}
