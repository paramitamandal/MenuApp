package com.sip.menuapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sip.menuapp.database.AccountGeneral;
import com.sip.menuapp.database.DbContent;
import com.sip.menuapp.database.SyncAdapter;
import com.sip.menuapp.service.SyncService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final String SETTINGS_FILENAME = "settings.txt";
    private static final String TAG = "MenuItemListActivity";
    SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


//        preferences = this.getApplicationContext().getSharedPreferences(SETTINGS_FILENAME, MODE_PRIVATE);
//        if((preferences.getString("ServerURL", null)) == null) {
//            System.out.println("nothing in SharedPreferences...creating new....");
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString("ServerURL", "http://192.168.59.5:8080/");
//            editor.commit();
//        }
//        setServerURL();


//        String filename="serverURL.txt";
//        String data = "http://192.168.59.5:8080/";
//        FileOutputStream fos;
//        try {
//            fos = openFileOutput(filename, Context.MODE_PRIVATE);
//            //default mode is PRIVATE, can be APPEND etc.
//            fos.write(data.getBytes());
//            fos.close();
//
//            System.out.println(getApplicationContext()+filename + " saved");
//
//        } catch (FileNotFoundException e) {e.printStackTrace();}
//        catch (IOException e) {e.printStackTrace();
//        }
//
//
//        StringBuffer stringBuffer = new StringBuffer();
//        try {
//            //Attaching BufferedReader to the FileInputStream by the help of InputStreamReader
//            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
//                    openFileInput(filename)));
//            String inputString;
//            //Reading data line by line and storing it into the stringbuffer
//            while ((inputString = inputReader.readLine()) != null) {
//                stringBuffer.append(inputString + "\n");
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //Displaying data on the toast
//        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\t"+stringBuffer.toString());
//        String str = stringBuffer.toString();
//        System.out.println("getting from SharedPreferences......." + str);
//        SyncAdapter.serverURL = str;
//        MenuItemAdapter.serverURL = str;


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);

                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {
            writeToFile();
            String str = readFromFile();
            SyncAdapter.serverURL = str;
            MenuItemAdapter.serverURL = str;
        }




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

    private void writeToFile() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), SETTINGS_FILENAME);
            System.out.println("---------------------------->>>" + file.getAbsolutePath());
            if (!file.exists()) {
                Log.e(TAG, "The settings file does not exist....create a new one.......");
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(new String("http://192.168.59.5:8080/").getBytes());
                outputStream.close();
            }
        }
        catch(FileNotFoundException e){
            Log.e("login activity", "File not found: " + e.toString());
        } catch(IOException e){
            Log.e("login activity", "Can not read file: " + e.toString());
        }

    }

//    public void setServerURL(){
//        String str = preferences.getString("ServerURL", null);
//        System.out.println("getting from SharedPreferences......." + str);
//        SyncAdapter.serverURL = str;
//        MenuItemAdapter.serverURL = str;
//    }

    private String readFromFile() {
        String state = Environment.getExternalStorageState();
        boolean isReadable =  (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
        String ret = "";

        if(isReadable) {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), SETTINGS_FILENAME);
            if (!file.exists()) {
                Log.e(TAG, "The settings file does not exist");
            } else {
                try {
                    InputStream inputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "Can not read file: " + e.toString());
                }

            }
        }
        return ret;
    }

    public File getSettingsFile() {
        // Get the directory for the user's public downloads directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), SETTINGS_FILENAME);
        if (!file.exists()) {
            Log.e(TAG, "The settings file does not exist");
        }
        return file;
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
