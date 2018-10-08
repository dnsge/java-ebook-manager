package org.dnsge.fbla.ebkmg.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a student in a SQLite database
 *
 * @author Daniel Sage
 * @version 0.1
 */
@DatabaseTable(tableName = "students")
public final class Student {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true) private int id;
    @DatabaseField private String firstName;
    @DatabaseField private String lastName;
    @DatabaseField private String studentId;
    @DatabaseField(foreign = true, foreignAutoRefresh = true) private Ebook ebook;

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
     * Student's ebook (Can be null)
     *
     * @return Student's ebook
     */
    public Ebook getEbook() {
        return ebook;
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
     * Set student ebook
     *
     * @param ebook Ebook object
     * @see Ebook
     */
    public void setEbook(Ebook ebook) {
        this.ebook = ebook;
    }

    /**
     * Set student's database ID
     *
     * @param id Database ID
     */
    protected void setId(int id) {
        this.id = id;
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
        ebook.setEbookName(memento.getEbookName());
        ebook.setEbookCode(memento.getEbookCode());
    }


    /**
     * Memento (<a href="https://www.oodesign.com/memento-pattern.html">oodesign.com/memento-pattern</a>)
     * <p>
     * Stores the state of a Student object
     *
     * @author Daniel Sage
     * @version 0.1
     */
    public static final class Memento {
        private final Student originalStudent;
        private final String firstName;
        private final String lastName;
        private final String studentId;
        private final Ebook  originalEbook;
        private final String ebookName;
        private final String ebookCode;
        private final int databaseId;

        Memento(Student originalStudent) {
            this.originalStudent = originalStudent;
            this.firstName = originalStudent.getFirstName();
            this.lastName = originalStudent.getLastName();
            this.studentId = originalStudent.getStudentId();
            this.originalEbook = originalStudent.getEbook();
            this.databaseId = originalStudent.getId();

            if (this.originalEbook != null) {
                this.ebookName = this.originalEbook.getEbookName();
                this.ebookCode = this.originalEbook.getEbookCode();
            } else {
                this.ebookName = null;
                this.ebookCode = null;
            }
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

        /**
         * @return Original Ebook of the original student
         */
        Ebook getOriginalEbook() {
            return originalEbook;
        }

        String getEbookName() {
            return ebookName;
        }

        String getEbookCode() {
            return ebookCode;
        }
    }
}