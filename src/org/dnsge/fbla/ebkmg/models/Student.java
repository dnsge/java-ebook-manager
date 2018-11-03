package org.dnsge.fbla.ebkmg.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.dnsge.fbla.ebkmg.SQLiteConnector;

import java.sql.SQLException;
import java.util.List;

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
    @DatabaseField private String grade;
    @DatabaseField private String studentId;
    @DatabaseField private Boolean hasEbook;
    @DatabaseField(unique = true) private String ebookCode;

    private Ebook ownedEbook;

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

    public String getEbookCode() {
        return ebookCode;
    }

    public void setEbookCode(String ebookCode) {
        this.ebookCode = ebookCode;
        try {
            ownedEbook = Ebook.getOrCreate(ebookCode);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Ebook getOwnedEbook() {
        if (hasEbook) {
            if (ownedEbook != null) {
                return ownedEbook;
            } else {
                try {
                    ownedEbook = Ebook.get(ebookCode);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return ownedEbook;
            }

        } else {
            return null;
        }
    }

    public void setHasEbook(Boolean hasEbook) {
        this.hasEbook = hasEbook;
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
        grade = memento.getGrade();
        studentId = memento.getStudentId();
        ebookCode = memento.getEbookId();
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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
        private final String grade;
        private final String studentId;
        private final String ebookId;
        private final int databaseId;

        Memento(Student originalStudent) {
            this.originalStudent = originalStudent;
            this.firstName = originalStudent.getFirstName();
            this.lastName = originalStudent.getLastName();
            this.grade = originalStudent.getGrade();
            this.studentId = originalStudent.getStudentId();
            this.databaseId = originalStudent.getId();
            this.ebookId = originalStudent.getEbookCode();
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

        String getGrade() {
            return grade;
        }

        String getStudentId() {
            return studentId;
        }

        String getEbookId() {
            return ebookId;
        }
    }

    /**
     * Checks if an E-book code has already been used on another student
     *
     * @param code E-book code to check for
     * @return {@code true} if used, {@code false} if not used
     * @throws SQLException If there's an issue
     */
    public static Boolean codeUsed(String code, Student excluded) throws SQLException {
        Dao<Student, String> dao = SQLiteConnector.getInstance().getStudentDao();
        List<Student> allCodes = dao.queryBuilder().where().eq("ebookCode", code).query();
        System.out.println("ALL CODES FOUND: " + allCodes.toString());
        return allCodes.size() > 0 && allCodes.get(0).equals(excluded);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s) (%s)", firstName, lastName, studentId, ebookCode);
    }
}
