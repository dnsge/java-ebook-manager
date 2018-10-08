package org.dnsge.fbla.ebkmg;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;

import java.io.IOException;
import java.sql.SQLException;

/**
 * SQLiteConnector singleton class
 *
 * @author Daniel Sage
 * @version 0.1
 */
public final class SQLiteConnector {
    private static SQLiteConnector ourInstance = new SQLiteConnector();
    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private ConnectionSource connectionSource;

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
    public void connect(String connectionUrl) throws SQLException, IOException {
        disconnectIfConnected();

        connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + connectionUrl);
        connected.set(true);
    }

    /**
     * Closes database connection
     *
     * @throws IOException if something goes wrong
     * @throws IllegalStateException if not yet connected to the database
     */
    public void disconnect() throws IOException {
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
    public void disconnectIfConnected() throws IOException {
        try {
            disconnect();
        } catch (IllegalStateException ignored) {}
    }

    /**
     * @return If currently connected to a database
     */
    public boolean isConnected() {
        return connected.getValue();
    }

    /**
     * Gets the ConnectionSource object for the current connection
     *
     * @return The connection source for the database
     * @throws IllegalStateException if not yet connected to the database
     */
    public ConnectionSource getConnectionSource() {
        if (!isConnected()) {
            throw new IllegalStateException("Not yet connected to SQLite database");
        }

        return connectionSource;
    }

    /**
     * @return The {@code connected} SimpleBooleanProperty
     * @see SimpleBooleanProperty
     */
    public SimpleBooleanProperty getConnectedProperty() {
        return connected;
    }

}
