package com.sip.menuapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sip.menuapp.database.AccountGeneral;
import com.sip.menuapp.database.DatabaseContent;
import com.sip.menuapp.database.SyncAdapter;
import com.sip.menuapp.service.SyncService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An activity representing a list of MenuItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a  MenuItemDetailActivity representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MenuItemListActivity extends AppCompatActivity implements MenuItemAdapter.Listener, MenuItemQuantityAdapter.Listener{

    private static final String DATABASE_FILE = "database";
    private boolean mTwoPane;
//    private ItemObserver itemObserver;
//    private ContentResolver resolver;
    static Map<String, List<Item>> itemCategoryMap;
    private static final String TAG = "MenuItemListActivity";
    SharedPreferences preferences = null;
    TextView counterTextView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        LayoutInflater mInflater= LayoutInflater.from(this);
        View badgeLayoutView = mInflater.inflate(R.layout.badge_icon_layout, null);
        toolbar.addView(badgeLayoutView, new Toolbar.LayoutParams(Gravity.RIGHT));
        counterTextView = (TextView) badgeLayoutView.findViewById(R.id.counter);

        badgeLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MenuItemAdapter.getCurrentOrder().size() > 0) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    View orderSummaryView = LayoutInflater.from(context).inflate(R.layout.order_summary, null);

                    if (MenuItemAdapter.quantityList != null) {
                        RecyclerView recyclerView = orderSummaryView.findViewById(R.id.order_item_list_recycler_view);
                        MenuItemQuantityAdapter menuItemQuantityAdapter = new MenuItemQuantityAdapter((MenuItemQuantityAdapter.Listener) context, MenuItemAdapter.getCurrentOrder());
                        recyclerView.setAdapter(menuItemQuantityAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
                    }

                    dialog.setContentView(orderSummaryView);
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    dialog.getWindow().setAttributes(lp);

                    final Button closeBtn = orderSummaryView.findViewById(R.id.btn_close);
                    final Button okBtn = orderSummaryView.findViewById(R.id.btn_Ok);
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });


        if(!isDB_SyncDone()) {
            // Create your sync account
            AccountGeneral.createSyncAccount(this);
            // Perform a manual sync by calling this:
            SyncAdapter.performSync();
        }
        else{
            loadView();
        }
        // Setup example content observer
//        itemObserver = new ItemObserver();

        if (findViewById(R.id.menu_item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
//  /      resolver = this.getContentResolver();
    }

//    public void setServerURL(){
//        String str = preferences.getString("ServerURL", null);
//        System.out.println("getting from SharedPreferences......." + str);
//        SyncAdapter.serverURL = str;
//        MenuItemAdapter.serverURL = str;
//    }

    private void loadView() {
        DatabaseContent.loadData(this);
        itemCategoryMap = DatabaseContent.ITEM_CATEGORY_MAP;
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
        recyclerView.setAdapter(new CategoryRecyclerViewAdapter(this, category, mTwoPane));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
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
    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            preferences = getApplicationContext().getSharedPreferences(DATABASE_FILE, MODE_PRIVATE);
            if((preferences.getString("DB_SYNC", null)) == null) {
                System.out.println("nothing in SharedPreferences...creating new....");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("DB_SYNC", "done");
                editor.commit();
            }
            System.out.println("Sync finished, should refresh now!!");
            loadView();
        }
    };

    private boolean isDB_SyncDone() {
        preferences = getApplicationContext().getSharedPreferences(DATABASE_FILE, MODE_PRIVATE);
        if ((preferences.getString("DB_SYNC", null)) == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register the observer at the start of our activity
//        getContentResolver().registerContentObserver(
//                ItemContract.Items.CONTENT_URI, // Uri to observe (our articles)
//                true, // Observe its descendants
//                itemObserver); // The observer
    }

    @Override
    protected void onStop() {
        super.onStop();

//        if (itemObserver != null) {
//            // Unregister the observer at the stop of our activity
//            getContentResolver().unregisterContentObserver(itemObserver);
//        }
    }

    @Override
    public void onUpdate(String itemName, int quantity) {
        counterTextView.setText(MenuItemAdapter.getCurrentOrder().size() + "");
    }

    @Override
    public void onUpdateOrder() {
        counterTextView.setText(MenuItemAdapter.getCurrentOrder().size() + "");
    }

//    private void refreshItems() {
//        Log.i(getClass().getName(), "Items data has changed!");
//    }

//    private final class ItemObserver extends ContentObserver {
//        private ItemObserver() {
//            // Ensure callbacks happen on the UI thread
//            super(new Handler(Looper.getMainLooper()));
//        }
//
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            // Handle your data changes here!!!
//            refreshItems();
//        }
//    }

    public static class CategoryRecyclerViewAdapter
            extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {

        private final MenuItemListActivity mParentActivity;
        private final List<String> mValues;
        private final boolean mTwoPane;
        int selectedPosition=-1;

        CategoryRecyclerViewAdapter(MenuItemListActivity parent,
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if(selectedPosition==position) {
                holder.itemView.setBackgroundColor(Color.parseColor("#808080"));
            }
            else {
                holder.itemView.setBackgroundColor(Color.parseColor("#D3D3D3"));
            }
            holder.categoryNameTextView.setText(mValues.get(position));
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition=position;
                    notifyDataSetChanged();
                    String categoryName = (String) view.getTag();

//                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(MenuItemDetailFragment.ITEM_CATEGORY, categoryName);
                        MenuItemDetailFragment fragment = new MenuItemDetailFragment();
                        fragment.setArguments(arguments);
                        mParentActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.menu_item_detail_container, fragment)
                                .commit();
//                    } else {
//                        Context context = view.getContext();
//                        Intent intent = new Intent(context, MenuItemDetailActivity.class);
//                        intent.putExtra(MenuItemDetailFragment.ITEM_CATEGORY, categoryName);
//
//                        context.startActivity(intent);
//                    }
                }
            });
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
