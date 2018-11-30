package org.dnsge.fbla.ebkmg.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents an ebook in a SQLite database
 *
 * @author Daniel Sage
 * @version 0.5
 */
@DatabaseTable(tableName = "ebooks")
public final class Ebook {

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true) private int id;
    @DatabaseField(unique = true) private String code;
    @DatabaseField private String name;
    @DatabaseField private Date assignmentDate;

    public Ebook() { }

    public Ebook(String name, String code, Date assignmentDate) {
        this.name = name;
        this.code = code;
        this.assignmentDate = assignmentDate;
    }

    /**
     * Gets or creates an {@code Ebook} object that has a name & code
     *
     * @param name Name to use
     * @param code Code to use
     * @return Found or new {@code Ebook} object
     * @throws SQLException if something goes wrong
     */
    static Ebook getOrCreate(String name, String code) throws SQLException {
        SQLiteConnector connector = SQLiteConnector.getInstance();
        Dao<Ebook, String> dao = connector.getEbookDao();

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("code", code);

        List<Ebook> list = dao.queryForFieldValues(hm);
        System.out.println("Found " + list.size() + " values in the list");

        if (list.size() > 0) {
            return list.get(0);
        } else {
            Ebook newEbook = new Ebook(name, code, new Date());
            dao.create(newEbook);
            return newEbook;
        }
    }

    /**
     * Gets the corresponding {@code Ebook} from a redemption code
     *
     * @param code Redemption code to select
     * @return Found {@code Ebook} object, or null if not found
     * @throws SQLException if something goes wrong
     */
    static Ebook get(String code) throws SQLException {
        SQLiteConnector connector = SQLiteConnector.getInstance();
        Dao<Ebook, String> dao = connector.getEbookDao();

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("code", code);

        List<Ebook> list = dao.queryForFieldValues(hm);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Checks for an already existing redemption code
     *
     * @param code Redemption code to check against
     * @return Whether that code already exists in the database
     */
    public static boolean exists(String code) {
        try {
            return get(code) != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks for an already existing redemption code besides a certain ebook
     *
     * @param code Redemption code to check against
     * @param me Ebook to ignore
     * @return Whether that code already exists and isn't the specified Ebook
     */
    public static boolean otherExists(String code, Ebook me) {
        try {
            Ebook got = get(code);
            return got != null && !got.equals(me);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private Date getAssignmentDate() {
        return assignmentDate;
    }

    /**
     * Formats the redemption date to a string
     *
     * @return Formatted redemption date in "MM/dd/yy hh:mm aa" form
     */
    public String getAssignmentDateString() {
        try {
            return new SimpleDateFormat("MM/dd/yy hh:mm aa").format(getAssignmentDate());
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Gets the owner of this Ebook
     *
     * @return The owner of this Ebook, or null if there isn't one
     */
    public Student getOwner() {
        try {
            return Student.whoOwns(getCode());
        } catch (NullPointerException e) {
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ebook ebook = (Ebook) o;
        return Objects.equals(code, ebook.code) &&
                Objects.equals(name, ebook.name) &&
                Objects.equals(assignmentDate, ebook.assignmentDate);
    }

    /**
     * Loads values from a Memento into this Ebook
     *
     * @param memento Memento to load from
     * @see Memento
     */
    public void loadFromMemento(Memento memento) {
        setName(memento.getEbookName());
        setCode(memento.getEbookCode());
        assignmentDate = memento.getEbookDate();
    }

    /**
     * Saves values from this Ebook into a Memento
     *
     * @return Memento object with this Ebook's values
     * @see Memento
     */
    public Memento saveToMemento() {
        return new Memento(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, assignmentDate);
    }

    /**
     * @return Whether this Ebook has a valid name and code
     */
    public boolean filledOutProperly() {
        return !name.trim().isEmpty() &&
                !code.trim().isEmpty();
    }

    public void setAssignmentDate(Date assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    /**
     * Memento (<a href="https://www.oodesign.com/memento-pattern.html">oodesign.com/memento-pattern</a>)
     * that Stores the state of a {@code Ebook} object
     *
     * @author Daniel Sage
     * @version 0.1
     */
    public static final class Memento {

        private final Ebook originalEbook;
        private final String ebookName;
        private final String ebookCode;
        private final Date ebookDate;

        Memento(Ebook originalEbook) {
            this.originalEbook = originalEbook;
            this.ebookName = originalEbook.getName();
            this.ebookCode = originalEbook.getCode();
            this.ebookDate = originalEbook.getAssignmentDate();
        }

        /**
         * Returns a replica student
         * Doesn't have a database ID
         *
         * @return new Student Object
         */
        public Ebook replica() {
            Ebook t = new Ebook();
            t.loadFromMemento(this);
            return t;
        }

        String getEbookName() {
            return ebookName;
        }

        String getEbookCode() {
            return ebookCode;
        }

        Date getEbookDate() {
            return ebookDate;
        }
    }
}
