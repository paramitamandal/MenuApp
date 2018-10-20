package com.sip.menuapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sip.menuapp.database.DatabaseContent;

import java.util.List;

/**
 * A fragment representing a single MenuItem detail screen.
 * This fragment is either contained in a {@link MenuItemListActivity}
 * in two-pane mode (on tablets) or a MenuItemDetailActivity
 * on handsets.
 */
public class MenuItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ITEM_CATEGORY = "item_category";
    private List<Item> itemList;
    private MenuItemAdapter menuItemAdapter;
    public MenuItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ITEM_CATEGORY)) {
            itemList = DatabaseContent.ITEM_CATEGORY_MAP.get(getArguments().getString(ITEM_CATEGORY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_item_detail, container, false);

        if (itemList != null) {
            RecyclerView recyclerView = rootView.findViewById(R.id.items_list_recycler_view);
            menuItemAdapter = new MenuItemAdapter( this.getActivity(), itemList);
            menuItemAdapter.setListener((MenuItemAdapter.Listener) this.getActivity());
            recyclerView.setAdapter(menuItemAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this.getContext()));
        }
        return rootView;
    }
}
