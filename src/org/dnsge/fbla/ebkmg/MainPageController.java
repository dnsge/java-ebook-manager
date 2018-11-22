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
import org.dnsge.fbla.ebkmg.db.Ebook;
import org.dnsge.fbla.ebkmg.db.SQLiteConnector;
import org.dnsge.fbla.ebkmg.db.Student;
import org.dnsge.fbla.ebkmg.extensions.ChangeWrapperHolder;
import org.dnsge.fbla.ebkmg.extensions.ChoiceBoxWrapper;
import org.dnsge.fbla.ebkmg.extensions.TextFieldWrapper;
import org.dnsge.fbla.ebkmg.popup.AlertCreator;
import org.dnsge.fbla.ebkmg.popup.NewEbookPopup;
import org.dnsge.fbla.ebkmg.popup.NewStudentPopup;
import org.dnsge.fbla.ebkmg.popup.PairStudentPopup;
import org.dnsge.fbla.ebkmg.util.Pair;
import org.dnsge.fbla.ebkmg.util.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Controller for the main JavaFX view
 *
 * @author Daniel Sage
 * @version 0.3
 */
public final class MainPageController {
    // Menu bar stuff
    // todo: add more buttons/functionality to menubar
    @FXML private MenuBar menuBar;
    @FXML private MenuItem connectToDatabase, closeConnection;

    @FXML private TabPane mainTabPane;
    @FXML private Tab studentTab;
    @FXML private Tab ebookTab;

    // Table related stuff
    @FXML private TableView<Student> studentTableView;
    @FXML private TableColumn<Student, String> lastNameColumn;
    @FXML private TableColumn<Student, String> firstNameColumn;
    @FXML private TableView<Ebook> ebookTableView;
    @FXML private TableColumn<Ebook, String> ebookCodeColumn;
    @FXML private TableColumn<Ebook, String> ebookRedemptionDateColumn;
    // Right side of the screen input nodes
    // student
    @FXML private TextField firstNameField, lastNameField, studentIdField;
    @FXML private ChoiceBox<String> studentGradeDropdown;
    @FXML private Button updateStudentDataButton, cancelUpdateStudentButton, deleteRecordButton;
    // ebook
    @FXML private TextField ebookNameField, ebookCodeField, redemptionDateField;
    @FXML private Button updateEbookDataButton, cancelUpdateEbookButton, viewStudentButton, pairStudentButton;
    // Bottom toolbar stuff
    @FXML private ToolBar buttonsToolbar;
    @FXML private Button newRecordButton;
    @FXML private Button generateReportButton; // Todo: this

    private Student selectedStudent;
    private Ebook selectedEbook;
    private SQLiteConnector connector = SQLiteConnector.getInstance();

    // IChangeWrappers and ChangeWrapperHolder
    private TextFieldWrapper firstName, lastName, studentId;
    private TextFieldWrapper ebookName, ebookCode, redemptionDate;
    private ChoiceBoxWrapper<String> studentGrade;
    private ChangeWrapperHolder studentWrapperHolder;
    private ChangeWrapperHolder ebookWrapperHolder;

    /**
     * Called by JavaFX once all FXML fields/nodes have been created/registered
     */
    @FXML
    public void initialize() {
        registerMenuBarInteractions();
        registerStudentTableDataInteractions();
        registerEbookTableDataInteractions();
        registerToolBarInteractions();
        createWrappers();

        setDisableOnInteractionsStudent(true);
        setDisableOnInteractionsEbook(true);
        buttonsToolbar.setDisable(true);
    }

    private void reloadTextBoxesStudent() {
        ObservableList<Student> selectedStudentList = studentTableView.getSelectionModel().getSelectedItems();
        if (selectedStudentList.size() > 0) {
            setDisableOnInteractionsStudent(false);
            selectedStudent = selectedStudentList.get(0);
            loadTextFieldsFromStudent(selectedStudent);
            resetFieldsStyle();
        }
    }

    /**
     * Registers event listeners for things related to the student table
     */
    private void registerStudentTableDataInteractions() {
        // Set column cell value factories
        lastNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastName()));
        firstNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFirstName()));

        studentTableView.setOnMouseClicked((MouseEvent event) -> {
            reloadTextBoxesStudent();
        });

        updateStudentDataButton.setOnAction(event -> {
            ObservableList<Student> selectedStudentList = studentTableView.getSelectionModel().getSelectedItems();

            // Make sure we have actually selectedStudent a row, though it shouldn't be possible to happen without
            if (selectedStudentList.size() > 0) {
                if (!Student.otherStudentWithIdExists(studentId.asText(), selectedStudent)) {

                    // Create backup and save
                    Student.Memento preservedStudent = selectedStudent.saveToMemento();
                    saveTextFieldsToStudent(selectedStudent);

                    if (!selectedStudent.filledOutProperly()) {
                        selectedStudent.loadFromMemento(preservedStudent);
                        loadTextFieldsFromStudent(selectedStudent);
                        AlertCreator.errorUser("You need to fill out each entry field!");
                        return;
                    }

                    try {

                        // Use transactionManager to cancel changes if something goes wrong
                        TransactionManager.callInTransaction(connector.getConnectionSource(), (Callable<Void>) () -> {
                            connector.getStudentDao().update(selectedStudent);
                            return null;
                        });

                        studentWrapperHolder.clearAllStyle();
                        studentWrapperHolder.updateAll();

                    } catch (SQLException e) {
                        selectedStudent.loadFromMemento(preservedStudent);
                        loadTextFieldsFromStudent(selectedStudent);

                        e.printStackTrace();
                        AlertCreator.unknownError();
                    }
                } else {
                    AlertCreator.errorUser("A Student with that Student ID already exists!");
                }
            }
            studentTableView.refresh();
        });

        cancelUpdateStudentButton.setOnAction(event -> {
            if (studentWrapperHolder.anyChanged()) {
                if (!AlertCreator.askYesOrNo("You have unsaved changes, are you sure you want to cancel?")) {
                    return;
                }
            }
            finishStudentChanges();
        });

        deleteRecordButton.setOnAction(event -> {
            if (AlertCreator.askYesOrNo("Are you sure you want to delete this record?")) {
                try {
                    connector.getStudentDao().delete(selectedStudent);
                    List<Student> allStudents = connector.getStudentDao().queryForAll();
                    studentTableView.setItems(FXCollections.observableArrayList(allStudents));
                    studentTableView.refresh();
                    finishStudentChanges();
                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertCreator.unknownError();
                }
            }
        });

    }

    /**
     * Registers event listeners for things related to the ebook table
     */
    private void registerEbookTableDataInteractions() {

        ebookCodeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCode()));
        ebookRedemptionDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAssignmentDateString()));

        ebookTableView.setOnMouseClicked(e -> {
            ObservableList<Ebook> selectedEbookList = ebookTableView.getSelectionModel().getSelectedItems();
            if (selectedEbookList.size() > 0) {
                setDisableOnInteractionsEbook(false);
                selectedEbook = selectedEbookList.get(0);
                loadInteractionFieldsFromEbook(selectedEbook);
                resetFieldsStyle();
            }
        });

        updateEbookDataButton.setOnAction(event -> {
            ObservableList<Ebook> selectedEbookList = ebookTableView.getSelectionModel().getSelectedItems();

            // Make sure we have actually selectedStudent a row
            if (selectedEbookList.size() > 0) {
                if (!Ebook.otherExists(ebookCode.asText(), selectedEbook)) {
                    if (!selectedEbook.filledOutProperly()) {
                        AlertCreator.errorUser("You need to fill out each entry field!");
                        return;
                    }

                    Ebook.Memento preservedEbook = selectedEbook.saveToMemento();
                    saveTextFieldsToEbook(selectedEbook);
                    try {
                        TransactionManager.callInTransaction(connector.getConnectionSource(), (Callable<Void>) () -> {
                            connector.getEbookDao().update(selectedEbook);
                            return null;
                        });

                        ebookWrapperHolder.clearAllStyle();
                        ebookWrapperHolder.updateAll();

                    } catch (SQLException e) {
                        selectedEbook.loadFromMemento(preservedEbook);
                        loadInteractionFieldsFromEbook(selectedEbook);

                        e.printStackTrace();
                        AlertCreator.unknownError();
                    }
                } else {
                    AlertCreator.errorUser("An E-Book with that code already exists!");
                }
            }
            ebookTableView.refresh();
        });

        cancelUpdateEbookButton.setOnAction(event -> {
            if (ebookWrapperHolder.anyChanged()) {
                if (!AlertCreator.askYesOrNo("You have unsaved changes, are you sure you want to cancel?")) {
                    return;
                }
            }
            finishEbookChanges();
        });

        viewStudentButton.setOnAction(event -> {
            try {
                Student stu = selectedEbook.getOwner();
                if (stu != null) {
                    mainTabPane.getSelectionModel().select(studentTab);
                    studentTableView.getSelectionModel().select(stu);
                    reloadTextBoxesStudent();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });

        pairStudentButton.setOnAction(event -> {
            PairStudentPopup psp = new PairStudentPopup();
            Pair<Student, Boolean> result = psp.showAndWait();
            if (result.getR()) {
                if (result.getL().getOwnedEbook() != null) {
                    Ebook ebook = result.getL().getOwnedEbook();
                    ebook.setAssignmentDate(null);
                    try {
                        connector.getEbookDao().update(ebook);
                    } catch (SQLException e) {
                        AlertCreator.errorUser("There was a problem removing the date of the previously paired E-book.");
                        e.printStackTrace();
                    }
                }

                selectedEbook.setAssignmentDate(new Date());
                result.getL().setEbook(selectedEbook);
                try {
                    connector.getStudentDao().update(result.getL());
                    connector.getEbookDao().update(selectedEbook);
                    loadInteractionFieldsFromEbook(selectedEbook);
                } catch (SQLException e) {
                    AlertCreator.errorUser("There was a problem pairing that E-Book and Student");
                    e.printStackTrace();
                }
            }
            completeEbookTableRefresh();
        });

    }

    /**
     * Registers event listeners for things related to the menu bar
     */
    private void registerMenuBarInteractions() {
        // Bind 'Connect to Database' menu button
        connectToDatabase.setOnAction((ActionEvent event) -> {
            if (studentWrapperHolder.anyChanged()) {
                if (!AlertCreator.askYesOrNo("You have unsaved changes, are you sure you want to close your previous connection and open a new one?")) {
                    return;
                }
            }
            File databaseFile;
            try {
                databaseFile = Utils.openFilePickerDialog("Select Database", "L:/ebook_data/", menuBar.getScene().getWindow());
                if (databaseFile == null) {
                    return;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                AlertCreator.errorUser("There was an issue opening the filepicker.");
                return;
            }

            clearTextFields();
            setDisableOnInteractionsStudent(true);
            setDisableOnInteractionsEbook(true);

            try {
                // Connect database
                connector.connect(databaseFile.getAbsolutePath());
                ConnectionSource connectionSource = connector.getConnectionSource();

                // Create database tables if they don't exist
                TableUtils.createTableIfNotExists(connectionSource, Student.class);
                TableUtils.createTableIfNotExists(connectionSource, Ebook.class);

                // Get all students
                List<Student> allStudents = connector.getStudentDao().queryForAll();
                List<Ebook> allEbooks = connector.getEbookDao().queryForAll();

                // Generate list and set the items
                studentTableView.setItems(FXCollections.observableArrayList(allStudents));
                ebookTableView.setItems(FXCollections.observableArrayList(allEbooks));
                closeConnection.setDisable(false);

            } catch (SQLException | IOException e) {
                try {
                    connector.disconnectIfConnected();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                AlertCreator.errorUser("There was an issue reading that database file");
            }
        });

        closeConnection.setOnAction(e -> {
            if (studentWrapperHolder.anyChanged() || ebookWrapperHolder.anyChanged()) {
                if (!AlertCreator.askYesOrNo("You have unsaved changes, are you sure you want to close your connection?")) {
                    return;
                }
            }

            try {
                connector.disconnectIfConnected();
                studentWrapperHolder.clearAll();
                studentWrapperHolder.clearAllStyle();
                ebookWrapperHolder.clearAll();
                ebookWrapperHolder.clearAllStyle();

                setDisableOnInteractionsStudent(true);
                setDisableOnInteractionsEbook(true);

                closeConnection.setDisable(true);
                studentTableView.setItems(FXCollections.observableArrayList());
                ebookTableView.setItems(FXCollections.observableArrayList());
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
        connector.getConnectedProperty().addListener((observable, oldValue, newValue) -> buttonsToolbar.setDisable(!newValue));

        newRecordButton.setOnAction(event -> {
            if (mainTabPane.getSelectionModel().getSelectedIndex() == 0) { // Student tab
                NewStudentPopup nsp = new NewStudentPopup();
                Pair<Student, Boolean> result = nsp.showAndWait();
                if (result.getR()) {
                    try {
                        TransactionManager.callInTransaction(connector.getConnectionSource(), (Callable<Void>) () -> {
                            connector.getStudentDao().create(result.getL());

                            List<Student> allStudents = connector.getStudentDao().queryForAll();
                            studentTableView.setItems(FXCollections.observableArrayList(allStudents));
                            studentTableView.refresh();

                            return null;
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                        AlertCreator.unknownError();
                    }
                }
            } else {
                NewEbookPopup nep = new NewEbookPopup();
                Pair<Ebook, Boolean> result = nep.showAndWait();
                if (result.getR()) {
                    try {
                        TransactionManager.callInTransaction(connector.getConnectionSource(), (Callable<Void>) () -> {
                            connector.getEbookDao().create(result.getL());

                            List<Ebook> allEbooks = connector.getEbookDao().queryForAll();
                            ebookTableView.setItems(FXCollections.observableArrayList(allEbooks));
                            ebookTableView.refresh();

                            return null;
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                        AlertCreator.unknownError();
                    }
                }
            }
        });
    }

    private void completeEbookTableRefresh() {
        List<Ebook> allEbooks = null;
        try {
            allEbooks = connector.getEbookDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ebookTableView.setItems(FXCollections.observableArrayList(allEbooks));
        ebookTableView.refresh();
    }


    /**
     * Creates the IChangeWrappers for the Nodes and sets up the combo box(es)
     */
    private void createWrappers() {
        firstName = new TextFieldWrapper(firstNameField);
        lastName = new TextFieldWrapper(lastNameField);
        studentId = new TextFieldWrapper(studentIdField);
        studentGrade = new ChoiceBoxWrapper<>(studentGradeDropdown);

        studentGrade.setItems("9", "10", "11", "12");

        ebookName = new TextFieldWrapper(ebookNameField);
        ebookCode = new TextFieldWrapper(ebookCodeField);
        redemptionDate = new TextFieldWrapper(redemptionDateField);

        studentWrapperHolder = new ChangeWrapperHolder(firstName, lastName, studentId, studentGrade);
        ebookWrapperHolder = new ChangeWrapperHolder(ebookName, ebookCode, redemptionDate);
    }

    /**
     * Sets whether the input nodes on the right half of the screen
     * of the student tab should be disabled
     *
     * @param disabled Whether the input methods should be disabled
     */
    private void setDisableOnInteractionsStudent(boolean disabled) {
        studentWrapperHolder.setAllDisabled(disabled);
        updateStudentDataButton.setDisable(disabled);
        cancelUpdateStudentButton.setDisable(disabled);
        deleteRecordButton.setDisable(disabled);
    }

    /**
     * Sets whether the input nodes on the right half of the screen
     * of the ebook tab should be disabled
     *
     * @param disabled Whether the input methods should be disabled
     */
    private void setDisableOnInteractionsEbook(boolean disabled) {
        ebookWrapperHolder.setAllDisabled(disabled);
        updateEbookDataButton.setDisable(disabled);
        cancelUpdateEbookButton.setDisable(disabled);
        viewStudentButton.setDisable(disabled);
        pairStudentButton.setDisable(disabled);
    }

    /**
     * Clears the text fields and resets their style
     */
    private void clearTextFields() {
        studentWrapperHolder.clearAll();
        ebookWrapperHolder.clearAll();

        resetFieldsStyle();
    }

    /**
     * Resets the style of the text fields
     */
    private void resetFieldsStyle() {
        studentWrapperHolder.clearAllStyle();
        ebookWrapperHolder.clearAllStyle();
    }

    /**
     * Deselects the row in the table and clears/disables the text fields for
     * the student tab
     */
    private void finishStudentChanges() {
        studentWrapperHolder.clearAllStyle();
        studentWrapperHolder.clearAll();
        setDisableOnInteractionsStudent(true);
        studentTableView.getSelectionModel().clearSelection();
    }

    /**
     * Deselects the row in the table and clears/disables the text fields for
     * the ebook tab
     */
    private void finishEbookChanges() {
        ebookWrapperHolder.clearAllStyle();
        ebookWrapperHolder.clearAll();
        setDisableOnInteractionsEbook(true);
        ebookTableView.getSelectionModel().clearSelection();
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
        studentWrapperHolder.updateAll();
    }

    /**
     * Fills in the text fields based off of an Ebook's information
     *
     * @param ebook Ebook to load from
     */
    private void loadInteractionFieldsFromEbook(Ebook ebook) {
        ebookName.setValue(ebook.getName());
        ebookCode.setValue(ebook.getCode());
        redemptionDate.setValue(ebook.getAssignmentDateString());
        ebookWrapperHolder.updateAll();
        if (ebook.getOwner() == null) {
            viewStudentButton.setDisable(true);
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
    }

    /**
     * Saves the information in the text fields to a Student
     *
     * @param ebook Ebook to save to
     */
    private void saveTextFieldsToEbook(Ebook ebook) {
        ebook.setName(ebookName.asText());
        ebook.setCode(ebookCode.asText());
    }

}
