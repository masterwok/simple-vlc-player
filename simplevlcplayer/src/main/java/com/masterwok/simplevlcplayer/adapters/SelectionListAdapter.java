package com.masterwok.simplevlcplayer.adapters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.masterwok.simplevlcplayer.R;
import com.masterwok.simplevlcplayer.models.SelectionItem;
import com.masterwok.simplevlcplayer.common.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This adapter supplies selection list items. The selection items
 * show an image and some display text aligned next to each other.
 *
 * @param <T> The value type of the selection item.
 */
public class SelectionListAdapter<T> extends BaseAdapter {

    private final Drawable selectedDrawable;
    private List<SelectionItem<T>> selectionItems;

    /**
     * Create a new SelectionListAdapter instance.
     *
     * @param selectedDrawableId The selected item drawable resource marker.
     * @param tintColorId        The tint color to be applied to the drawable resource.
     */
    public SelectionListAdapter(
            Context context,
            int selectedDrawableId,
            int tintColorId
    ) {
        this(
                context,
                new ArrayList<>(),
                selectedDrawableId,
                tintColorId
        );
    }

    /**
     * Create a new SelectionListAdapter instance.
     *
     * @param selectionItems     The possible selection items.
     * @param selectedDrawableId The selected item drawable resource marker.
     * @param tintColorId        The tint color to be applied to the drawable resource.
     */
    public SelectionListAdapter(
            Context context,
            List<SelectionItem<T>> selectionItems,
            int selectedDrawableId,
            int tintColorId
    ) {
        this.selectionItems = selectionItems;

        selectedDrawable = ResourceUtil.getTintedDrawable(
                context,
                selectedDrawableId,
                tintColorId
        );
    }


    @Override
    public int getCount() {
        return selectionItems.size();
    }

    @Override
    public SelectionItem<T> getItem(int position) {
        return selectionItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get the selection item view for some selection item at some position.
     * If the item is selected, then the drawable resource is visible next
     * to the display name of the SelectionItem instance.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SelectionItem<T> selectionItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(
                            R.layout.selectionlist_item,
                            parent,
                            false
                    );
        }

        fillValues(convertView, selectionItem);

        return convertView;
    }


    /**
     * Set the values of a ListView item.
     *
     * @param item          The item to set the values on.
     * @param selectionItem The selection item to fill the view item with.
     */
    private void fillValues(
            View item,
            SelectionItem<T> selectionItem
    ) {
        ImageView checkMarkImageView = item.findViewById(R.id.imageview_check_mark);
        TextView itemNameTextView = item.findViewById(R.id.textview_item_name);

        if (checkMarkImageView.getDrawable() == null) {
            checkMarkImageView.setImageDrawable(selectedDrawable);
        }

        checkMarkImageView.setVisibility(
                selectionItem.isSelected()
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        itemNameTextView.setText(
                selectionItem.getDisplayName()
        );
    }

    /**
     * Set the current items in the adapter.
     *
     * @param selectionItems The items to set.
     */
    public void configure(List<SelectionItem<T>> selectionItems) {
        this.selectionItems = selectionItems;

        notifyDataSetChanged();
    }

}