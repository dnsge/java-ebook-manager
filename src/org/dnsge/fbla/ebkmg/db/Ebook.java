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

    public static Ebook getOrCreate(String name, String code) throws SQLException {
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

    public static Ebook get(String code) throws SQLException {
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

    public static boolean exists(String code) {
        try {
            return get(code) != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

    public Date getAssignmentDate() {
        return assignmentDate;
    }

    public String getAssignmentDateString() {
        return new SimpleDateFormat("MM/dd/yy hh:mm aa").format(getAssignmentDate());
    }

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

    public void loadFromMemento(Memento memento) {
        setName(memento.getEbookName());
        setCode(memento.getEbookCode());
        assignmentDate = memento.getEbookDate();
    }

    public Memento saveToMemento() {
        return new Memento(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, assignmentDate);
    }

    public boolean filledOutProperly() {
        return !name.trim().isEmpty() &&
                !code.trim().isEmpty();
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
         * @return Original student used to make the memento
         */
        public Ebook getoriginalEbook() {
            return originalEbook;
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

        Ebook getOriginalEbook() {
            return originalEbook;
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
