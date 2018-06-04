package com.masterwok.simplevlcplayer.models;


/**
 * This model represents a selection list item that has a display
 * name and backing value.
 *
 * @param <T> The backing value type.
 */
public class SelectionItem<T> {
    private boolean isSelected;
    private String displayName;
    private T value;

    /**
     * Create a new SelectionItem instance.
     *
     * @param isSelected  Whether or not the item is selected.
     * @param displayName The display name of the item.
     * @param value       The backing value of the item.
     */
    public SelectionItem(
            boolean isSelected,
            String displayName,
            T value
    ) {

        this.isSelected = isSelected;
        this.displayName = displayName;
        this.value = value;
    }

    /**
     * Get whether or not the item is selected.
     *
     * @return If selected, true. Else, false.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Get the backing value.
     *
     * @return The backing value.
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the item display name.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }
}
