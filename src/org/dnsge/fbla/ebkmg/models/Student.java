package org.dnsge.fbla.ebkmg.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a student in a SQLite database
 *
 * @author Daniel Sage
 * @version 0.2
 */
@DatabaseTable(tableName = "students")
public final class Student {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true) private int id;
    @DatabaseField private String firstName;
    @DatabaseField private String lastName;
    @DatabaseField private String studentId;
    @DatabaseField private Boolean hasEbook;
    @DatabaseField private String ebookCode;
    @DatabaseField private String ebookName;

    /**
     * Student constructor for ORMLite
     */
    public Student() { }

    /**
     * Student constructor based on name and studentId
     *
     * @param firstName Student first name
     * @param lastName Student last name
     * @param studentId Student ID
     */
    public Student(String firstName, String lastName, String studentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
    }

    /**
     * Returns the Student database id, NOT the studentId
     *
     * @return Database ID Key
     */
    public int getId() {
        return id;
    }

    /**
     * @return Student first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return Student last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return Student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Set student first name
     *
     * @param firstName First name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Set student last name
     *
     * @param lastName Last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Set student ID
     *
     * @param studentId StudentId
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * Set student's database ID
     *
     * @param id Database ID
     */
    protected void setId(int id) {
        this.id = id;
    }

    public Boolean hasEbook() {
        return hasEbook;
    }

    public void setHasEbook(Boolean hasEbook) {
        this.hasEbook = hasEbook;
    }

    public String getEbookCode() {
        return ebookCode;
    }

    public String getEbookName() {
        return ebookName;
    }

    public void setEbookCode(String ebookCode) {
        this.ebookCode = ebookCode;
    }

    public void setEbookName(String ebookName) {
        this.ebookName = ebookName;
    }


    /**
     * Creates a memento savepoint for this student
     *
     * @return The Memento object
     * @see Memento
     */
    public Memento saveToMemento() {
        return new Memento(this);
    }

    /**
     * Loads information from a Memento
     * <p>
     * Allows for 'backups' and 'undos'
     *
     * @param memento Memento with information to load from
     * @see Memento
     */
    public void loadFromMemento(Memento memento) {
        firstName = memento.getFirstName();
        lastName = memento.getLastName();
        studentId = memento.getStudentId();
        ebookCode = memento.getEbookCode();
        ebookName = memento.getEbookName();
    }

    /**
     * Memento (<a href="https://www.oodesign.com/memento-pattern.html">oodesign.com/memento-pattern</a>)
     * that Stores the state of a {@code Student} object
     *
     * @author Daniel Sage
     * @version 0.2
     */
    public static final class Memento {
        private final Student originalStudent;
        private final String firstName;
        private final String lastName;
        private final String studentId;
        private final String ebookName;
        private final String ebookCode;
        private final int databaseId;

        Memento(Student originalStudent) {
            this.originalStudent = originalStudent;
            this.firstName = originalStudent.getFirstName();
            this.lastName = originalStudent.getLastName();
            this.studentId = originalStudent.getStudentId();
            this.databaseId = originalStudent.getId();
            this.ebookName = originalStudent.getEbookName();
            this.ebookCode = originalStudent.getEbookCode();
        }

        /**
         * @return Original student used to make the memento
         */
        public Student getOriginalStudent() {
            return originalStudent;
        }

        /**
         * Returns a replica student
         * Doesn't have a database ID
         *
         * @return new Student Object
         */
        public Student replica() {
            Student t = new Student();
            t.loadFromMemento(this);
            return t;
        }

        String getFirstName() {
            return firstName;
        }

        String getLastName() {
            return lastName;
        }

        String getStudentId() {
            return studentId;
        }

        String getEbookName() {
            return ebookName;
        }

        String getEbookCode() {
            return ebookCode;
        }
    }
}
