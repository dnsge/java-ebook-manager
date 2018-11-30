package org.dnsge.fbla.ebkmg.extensions;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;

/**
 * IChangeWrapper that wraps a {@link ChoiceBox}
 *
 * @author dnsge
 * @version 0.2
 * @see IChangeWrapper
 * @since 0.2
 */
public final class ChoiceBoxWrapper<T> implements IChangeWrapper<ChoiceBox<T>, T> {

    private static final String modificationColor = "#5e5e5e";
    private final ChoiceBox<T> wrapped;
    private T lastSavedValue;
    private Boolean changed = false;

    public ChoiceBoxWrapper(ChoiceBox<T> wrapped) {
        this(wrapped, modificationColor);
    }

    private ChoiceBoxWrapper(ChoiceBox<T> wrapped, String updatedColor) {
        this.wrapped = wrapped;
        lastSavedValue = wrapped.getSelectionModel().getSelectedItem();
        setModificationListeners(updatedColor);
    }

    @SafeVarargs
    public final void setItems(T... items) {
        getWrapped().setItems(FXCollections.observableArrayList(items));
    }

    private T selectedItem() {
        return getWrapped().getSelectionModel().getSelectedItem();
    }

    private void setModificationListeners(String updatedColor) {
        getWrapped().setOnAction(event -> {
            updateModifiedHighlight(updatedColor);
        });
    }

    private void updateModifiedHighlight(String updatedColor) {
        try {
            changed = !selectedItem().equals(lastSavedValue);
        } catch (NullPointerException e) {
            changed = false;
            return;
        }
        if (changed) {
            getWrapped().setStyle(String.format("-fx-border-color: %s;", updatedColor));
        } else {
            getWrapped().setStyle("");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // IChangeWrapper methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ChoiceBox<T> getWrapped() {
        return wrapped;
    }

    @Override
    public Boolean changed() {
        return changed;
    }

    @Override
    public void update() {
        changed = false;
        lastSavedValue = selectedItem();
        updateModifiedHighlight(modificationColor);
    }

    @Override
    public String asText() {
        return selectedItem().toString();
    }

    @Override
    public void clearStyle() {
        getWrapped().setStyle("");
    }

    @Override
    public void clear() {
        getWrapped().getSelectionModel().clearSelection();
    }

    @Override
    public void setValue(T value) {
        getWrapped().getSelectionModel().select(value);
    }
}
