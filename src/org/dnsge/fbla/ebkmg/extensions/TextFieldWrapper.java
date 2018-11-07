package org.dnsge.fbla.ebkmg.extensions;

import javafx.scene.control.TextField;

/**
 * IChangeWrapper that wraps a {@link TextField}
 *
 * @author dnsge
 * @version 0.2
 * @since 0.2
 * @see IChangeWrapper
 */
public final class TextFieldWrapper implements IChangeWrapper<TextField, String> {

    private final TextField wrapped;
    private String lastSavedValue;
    private Boolean changed = false;

    private static final String modificationColor = "#5e5e5e";

    public TextFieldWrapper(TextField wrapped) {
        this(wrapped, modificationColor);
    }

    private TextFieldWrapper(TextField wrapped, String updatedColor) {
        this.wrapped = wrapped;
        lastSavedValue = asText();
        setModificationListeners(updatedColor);
    }

    public boolean isEmpty() {
        return asText().trim().isEmpty();
    }

    private void setModificationListeners(String updatedColor) {
        getWrapped().setOnKeyTyped(event -> {
            updateModifiedHighlight(updatedColor);
        });
    }

    private void updateModifiedHighlight(String updatedColor) {
        changed = !asText().equals(lastSavedValue);
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
    public TextField getWrapped() {
        return wrapped;
    }

    @Override
    public Boolean changed() {
        return changed;
    }

    @Override
    public void update() {
        changed = false;
        lastSavedValue = asText();
        updateModifiedHighlight(modificationColor);
    }

    @Override
    public void clear() {
        getWrapped().setText("");
    }

    @Override
    public void setValue(String value) {
        getWrapped().setText(value);
    }

    @Override
    public String asText() {
        return getWrapped().getText();
    }

    @Override
    public String toString() {
        return asText();
    }
}
