package com.menuapp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An activity representing a list of MenuItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MenuItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MenuItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ItemObserver itemObserver;
    private ContentResolver resolver;
    static Map<String, List<Item>> itemCategoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Create your sync account
        AccountGeneral.createSyncAccount(this);
        // Perform a manual sync by calling this:
        SyncAdapter.performSync();
        // Setup example content observer
        itemObserver = new ItemObserver();

        if (findViewById(R.id.menu_item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        resolver = this.getContentResolver();
    }

    private void loadView() {
        DbContent.loadData(this);
        itemCategoryMap = DbContent.ITEM_CATEGORY_MAP;
        View recyclerView = findViewById(R.id.category_list);
        assert recyclerView != null;
        Set<String> mapCategory = itemCategoryMap.keySet();
        Iterator<String> iterator = mapCategory.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }

        List<String> categoryList = new ArrayList<String>();
        categoryList.addAll(mapCategory);
        setupRecyclerView((RecyclerView) recyclerView, categoryList);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<String> category) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, category, mTwoPane));
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncFinishedReceiver, new IntentFilter(SyncService.SYNC_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncFinishedReceiver);
    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Sync finished, should refresh nao!!");
            loadView();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Register the observer at the start of our activity
        getContentResolver().registerContentObserver(
                ItemContract.Items.CONTENT_URI, // Uri to observe (our articles)
                true, // Observe its descendants
                itemObserver); // The observer
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (itemObserver != null) {
            // Unregister the observer at the stop of our activity
            getContentResolver().unregisterContentObserver(itemObserver);
        }
    }


    private void refreshItems() {
        Log.i(getClass().getName(), "Items data has changed!");
    }

    private final class ItemObserver extends ContentObserver {
        private ItemObserver() {
            // Ensure callbacks happen on the UI thread
            super(new Handler(Looper.getMainLooper()));
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Handle your data changes here!!!
            refreshItems();
        }
    }


    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final MenuItemListActivity mParentActivity;
        private final List<String> mValues;
        private final boolean mTwoPane;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryName = (String) view.getTag();

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(MenuItemDetailFragment.ITEM_CATEGORY, categoryName);
                    MenuItemDetailFragment fragment = new MenuItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.menu_item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, MenuItemDetailActivity.class);
                    intent.putExtra(MenuItemDetailFragment.ITEM_CATEGORY, categoryName);

                    context.startActivity(intent);
                }
            }
        };


        SimpleItemRecyclerViewAdapter(MenuItemListActivity parent,
                                      List<String> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.categoryNameTextView.setText(mValues.get(position));
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView categoryNameTextView;

            ViewHolder(View view) {
                super(view);
                categoryNameTextView = (TextView) view.findViewById(R.id.category_name_text);
            }
        }
    }
}
