package org.dnsge.fbla.ebkmg.extensions;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;

/**
 * IChangeWrapper that wraps a {@link ChoiceBox}
 *
 * @author dnsge
 * @version 0.2
 * @since 0.2
 * @see IChangeWrapper
 */
public final class ChoiceBoxWrapper<T> implements IChangeWrapper<ChoiceBox<T>, T> {

    private final ChoiceBox<T> wrapped;
    private T lastSavedValue;
    private Boolean changed = false;

    public ChoiceBoxWrapper(ChoiceBox<T> wrapped) {
        this(wrapped, "#5e5e5e");
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
            try {
                changed = !selectedItem().equals(lastSavedValue);
            } catch (NullPointerException e) {
                changed = true; // todo: true or false
                return;
            }
            if (changed) {
                getWrapped().setStyle(String.format("-fx-border-color: %s;", updatedColor));
            } else {
                getWrapped().setStyle("");
            }
        });
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
