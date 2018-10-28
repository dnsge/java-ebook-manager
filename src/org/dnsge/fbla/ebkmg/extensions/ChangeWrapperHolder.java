package org.dnsge.fbla.ebkmg.extensions;

/**
 * Class that holds multiple {@link IChangeWrapper} Objects
 * and can apply methods to each of them in one call
 *
 * @author dnsge
 * @version 0.2
 * @since 0.2
 */
public final class ChangeWrapperHolder {

    private final IChangeWrapper[] wrappers;

    /**
     * Basic Constructor for a new {@code ChangeWrapperHolder}
     * @param wrappers {@code IChangeWrapper} objects to be held
     */
    public ChangeWrapperHolder(IChangeWrapper... wrappers) {
        this.wrappers = wrappers;
    }

    /**
     * Calls {@code update()} on each held {@code IChangeWrapper} object
     * @see IChangeWrapper#update()
     */
    public void updateAll() {
        for (IChangeWrapper wrapper : wrappers) {
            wrapper.update();
        }
    }

    /**
     * Checks if any held {@code IChangeWrapper} object has been changed since the last update
     * @return Whether any held {@code IChangeWrapper} object has been changed
     * @see IChangeWrapper#changed()
     */
    public boolean anyChanged() {
        for (IChangeWrapper wrapper : wrappers) {
            if (wrapper.changed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calls {@code clear()} on each held {@code IChangeWrapper} object
     * @see IChangeWrapper#clear()
     */
    public void clearAll() {
        for (IChangeWrapper wrapper : wrappers) {
            wrapper.clear();
        }
    }

    /**
     * Calls {@code clearStyle()} on each held {@code IChangeWrapper} object
     * @see IChangeWrapper#clearStyle()
     */
    public void clearAllStyle() {
        for (IChangeWrapper wrapper : wrappers) {
            wrapper.clearStyle();
        }
    }

    /**
     * Calls {@code setDisabled()} on each held {@code IChangeWrapper} object
     * @param disabled Whether to disable or enable
     * @see IChangeWrapper#setDisabled(boolean)
     */
    public void setAllDisabled(boolean disabled) {
        for (IChangeWrapper wrapper : wrappers) {
            wrapper.setDisabled(disabled);
        }
    }

}
