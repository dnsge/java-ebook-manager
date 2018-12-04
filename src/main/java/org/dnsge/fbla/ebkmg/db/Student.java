package org.dnsge.fbla.ebkmg.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.dnsge.fbla.ebkmg.csv.CsvBeanWritable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents a student in a SQLite database
 *
 * @author Daniel Sage
 * @version 0.5
 */
@DatabaseTable(tableName = "students")
public final class Student implements CsvBeanWritable {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true) private int id;
    @DatabaseField private String firstName;
    @DatabaseField private String lastName;
    @DatabaseField private String grade;
    @DatabaseField(unique = true) private String studentId;
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
     * @param grade Student grade (9-12)
     * @param studentId Student ID
     */
    public Student(String firstName, String lastName, String grade, String studentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.grade = grade;
        this.studentId = studentId;
        this.ebookCode = null;
    }

    /**
     * Gets the Student that owns an Ebook with a certain code
     *
     * @param code Ebook code to find
     * @return Student with that Ebook, or null if there isn't one
     * @throws SQLException if something goes wrong
     */
    static Student whoOwns(String code) throws SQLException {
        Dao<Student, String> dao = SQLiteConnector.getInstance().getStudentDao();
        List<Student> allCodes = dao.queryBuilder().where().eq("ebookCode", code).query();
        return allCodes.size() > 0 ? allCodes.get(0) : null;
    }

    /**
     * Gets the Student with a certain studentId
     *
     * @param studentId studentId to find
     * @return Student with that studentId, or null if there isn't one
     * @throws SQLException if something goes wrong
     */
    private static Student getFromStudentId(String studentId) throws SQLException {
        SQLiteConnector connector = SQLiteConnector.getInstance();
        Dao<Student, String> dao = connector.getStudentDao();

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("studentId", studentId);

        List<Student> list = dao.queryForFieldValues(hm);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Checks whether a Student with a certain studentID exists
     *
     * @param studentId studentId to check for
     * @return Whether a Student exists with a certain studentID
     */
    public static boolean studentWithIdExists(String studentId) {
        try {
            return getFromStudentId(studentId) != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks whether a Student with a certain studentID exists, excluding a certain student
     *
     * @param studentId studentId to check for
     * @param me Student to ignore
     * @return Whether a Student exists with a certain studentID excluding a certain student
     */
    public static boolean otherStudentWithIdExists(String studentId, Student me) {
        try {
            Student got = getFromStudentId(studentId);
            return got != null && !got.equals(me);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the Student database id, NOT the studentId
     *
     * @return Database ID Key
     */
    private int getId() {
        return id;
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
     * @return Student first name
     */
    public String getFirstName() {
        return firstName;
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
     * @return Student last name
     */
    public String getLastName() {
        return lastName;
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
     * @return Student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Set student ID
     *
     * @param studentId StudentId
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Boolean hasEbook() {
        return getOwnedEbook() != null;
    }

    private String getEbookCode() {
        return ebookCode;
    }

    public void setEbookCode(String ebookName, String ebookCode) {
        this.ebookCode = ebookCode;
        try {
            ownedEbook = Ebook.getOrCreate(ebookName, ebookCode);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setEbook(Ebook ebook) {
        ebookCode = ebook.getCode();
        ownedEbook = ebook;
    }

    public void clearEbook() {
        ebookCode = null;
        ownedEbook = null;
    }

    /**
     * Gets this Student's Ebook
     *
     * @return This Student's Ebook, null if there isn't one
     */
    public Ebook getOwnedEbook() {
        if (ownedEbook != null) {
            return ownedEbook;
        } else {
            if (ebookCode != null) {
                try {
                    ownedEbook = Ebook.get(ebookCode);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
                return ownedEbook;
            }
            return null;
        }
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

    @Override
    public String toString() {
        return String.format("%s %s (%s) (%s)", firstName, lastName, studentId, ebookCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(firstName, student.firstName) &&
                Objects.equals(lastName, student.lastName) &&
                Objects.equals(grade, student.grade) &&
                Objects.equals(studentId, student.studentId) &&
                Objects.equals(ebookCode, student.ebookCode) &&
                Objects.equals(ownedEbook, student.ownedEbook);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, grade, studentId, ebookCode, ownedEbook);
    }

    public boolean filledOutProperly() {
        return !firstName.trim().isEmpty() &&
                !lastName.trim().isEmpty() &&
                !studentId.trim().isEmpty();
    }

    @Override
    public String[] asCsvLine() {
        return new String[]{firstName, lastName, grade, studentId, hasEbook().toString(), hasEbook() ? Objects.requireNonNull(getOwnedEbook()).getCode() : ""};
    }

    @Override
    public String[] csvHeaders() {
        return new String[]{"firstName", "lastName", "grade", "studentId", "hasEbook", "ebookCode"};
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
}
