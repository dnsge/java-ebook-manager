package org.dnsge.fbla.ebkmg.extensions;

import javafx.scene.Node;

/**
 * Interface for Object that wraps a {@code javafx.scene.Node} object
 *
 * @param <W> Type of {@code javafx.scene.Node} object
 * @param <V> Type of value that the {@code Node} object stores
 * @author dnsge
 * @version 0.2
 * @since 0.2
 */
public interface IChangeWrapper<W extends Node, V> {

    /**
     * @return Has the value been changed since the last #update()
     */
    Boolean changed();

    /**
     * @return Wrapped node's value as a {@code String}
     */
    String asText();

    /**
     * @return The wrapped node object
     */
    W getWrapped();

    /**
     * Saves the current value of the wrapped node for change-watching
     */
    void update();

    /**
     * Clears the current value of the wrapped node
     */
    void clear();

    /**
     * @param value Sets the value of the wrapped node
     */
    void setValue(V value);

    /**
     * @param disabled Whether the wrapped node is disabled or enabled
     */
    default void setDisabled(boolean disabled) {
        getWrapped().setDisable(disabled);
    }

    /**
     * @param style New style of the wrapped node
     */
    default void setStyle(String style) {
        getWrapped().setStyle(style);
    }

    /**
     * Clears the style of the wrapped node
     */
    default void clearStyle() {
        setStyle("");
    }

    /**
     * Highlights the wrapped node with a border color of {@code red}
     */
    default void highlightError() {
        setStyle("-fx-border-color: red;");
    }

}
