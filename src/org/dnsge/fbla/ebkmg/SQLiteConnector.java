package org.dnsge.fbla.ebkmg;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import javafx.beans.property.SimpleBooleanProperty;
import org.dnsge.fbla.ebkmg.models.Ebook;
import org.dnsge.fbla.ebkmg.models.Student;

import java.io.IOException;
import java.sql.SQLException;

/**
 * SQLiteConnector singleton class
 *
 * @author Daniel Sage
 * @version 0.2
 */
public final class SQLiteConnector {
    private static SQLiteConnector ourInstance = new SQLiteConnector();
    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private ConnectionSource connectionSource;
    private Dao<Student, String> studentDao;
    private Dao<Ebook, String> ebookDao;

    /**
     * Gets singleton instance
     *
     * @return Singleton SQLiteConnector instance
     */
    public static SQLiteConnector getInstance() { return ourInstance; }

    private SQLiteConnector() { }

    /**
     * Connects to a database
     * <p>
     * Closes previous connection if already connected
     *
     * @param connectionUrl Connection URL
     * @throws SQLException if there is an issue connecting to the database
     * @throws IOException if something goes wrong
     */
    void connect(String connectionUrl) throws SQLException, IOException {
        disconnectIfConnected();

        connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + connectionUrl);
        connected.set(true);

        try {
            studentDao = DaoManager.createDao(connectionSource, Student.class);
            ebookDao = DaoManager.createDao(connectionSource, Ebook.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes database connection
     *
     * @throws IOException if something goes wrong
     * @throws IllegalStateException if not yet connected to the database
     */
    private void disconnect() throws IOException {
        if (isConnected()) {
            connectionSource.close();
            connected.set(false);
        } else
            throw new IllegalStateException("Not yet connected to SQLite database");
    }

    /**
     * Closes database connection if connected
     *
     * @throws IOException if something goes wrong
     */
    void disconnectIfConnected() throws IOException {
        try {
            disconnect();
        } catch (IllegalStateException ignored) {}
    }

    /**
     * @return If currently connected to a database
     */
    boolean isConnected() {
        return connected.getValue();
    }

    /**
     * Gets the ConnectionSource object for the current connection
     *
     * @return The connection source for the database
     * @throws IllegalStateException if not yet connected to the database
     */
    ConnectionSource getConnectionSource() {
        if (!isConnected()) {
            throw new IllegalStateException("Not yet connected to SQLite database");
        }

        return connectionSource;
    }

    /**
     * @return The {@code connected} SimpleBooleanProperty
     * @see SimpleBooleanProperty
     */
    SimpleBooleanProperty getConnectedProperty() {
        return connected;
    }

    /**
     * Gets the current active {@code Student} DAO
     *
     * @return Active {@code Student} DAO
     * @see Dao
     */
    public Dao<Student, String> getStudentDao() {
        return studentDao;
    }

    public Dao<Ebook, String> getEbookDao() {
        return ebookDao;
    }

    public Ebook getEbook(Integer ebookId) throws SQLException {
        return getEbookDao().queryForId(ebookId.toString());
    }

}
