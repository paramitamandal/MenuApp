package com.sip.menuapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private List<Item> itemList = new ArrayList<Item>();
    private final FragmentActivity fragmentActivity;
    private Context context;
    int selectedPosition=-1;
    public static Map<String, Integer> quantityList = new HashMap<String, Integer>();
    private MenuItemAdapter.Listener listener;

    public interface Listener {
        void onUpdate(String itemName, int quantity);
    }

    public static Map<String, Integer> getCurrentOrder(){
        Map<String, Integer> currentOrderMap =  new HashMap<String, Integer>();

        Iterator iterator = quantityList.keySet().iterator();
        while(iterator.hasNext()){
            String itemName = (String) iterator.next();
            int itemQuantity = quantityList.get(itemName);
            if(itemQuantity != 0) {
                currentOrderMap.put(itemName, itemQuantity);
            }
        }
        return currentOrderMap;
    }

    public static boolean exists(String URLName){
        boolean existing = false;
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =  (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK){
                existing = true;
            }
            con.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return existing;
    }

    public MenuItemAdapter(FragmentActivity parent,
                           List<Item> items) {
        fragmentActivity = parent;
        itemList = items;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.single_menu_item, parent, false);
        return new MenuItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,final int i) {
        if(selectedPosition==i) {
            holder.itemView.setBackgroundColor(Color.parseColor("#808080"));
        }
        else {
            holder.itemView.setBackgroundColor(Color.parseColor("#D3D3D3"));
        }
        Item item = itemList.get(i);
        holder.itemNameTextView.setText(item.getName());
        holder.itemDescriptionTextView.setText(item.getDescription());
        holder.itemPriceTextView.setText(item.getPrice());
        holder.videoPath = item.getVideoPath(); // set path of videoURL
        holder.itemView.setTag(item.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = i;
                notifyDataSetChanged();
            }
        });
        if(!quantityList.containsKey(item.getName())) {
            quantityList.put(item.getName(), 0);
        }
        else{
            String itemName = item.getName();
            int itemQuantity = quantityList.get(itemName);
            holder.quantity = itemQuantity;
            holder.itemQuantityEditText.setText(itemQuantity + "");
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView itemNameTextView;
        final TextView itemDescriptionTextView;
        final TextView itemPriceTextView;
        final ImageView videoPlayBtn;
        final Button incrementBtn;
        final Button decrementBtn;
        final EditText itemQuantityEditText;
        String videoPath;
        int quantity = 0;

        ViewHolder(View view) {
            super(view);
            itemNameTextView = (TextView) view.findViewById(R.id.item_name_text);
            itemDescriptionTextView = (TextView) view.findViewById(R.id.item_description_text);
            itemPriceTextView = (TextView) view.findViewById(R.id.item_price_text);
            videoPlayBtn = view.findViewById(R.id.btn_play);
            incrementBtn = view.findViewById(R.id.btn_increment);
            decrementBtn = view.findViewById(R.id.btn_decrement);
            itemQuantityEditText = (EditText) view.findViewById(R.id.item_quantity_text);
            itemQuantityEditText.setKeyListener(null);

//            final String serverURLWithEndPoint = ResourceConstant.SERVER_URL + "api/getVideo?id=80";
            final String serverURLWithEndPoint = "http://172.26.95.91:8080/api/getVideo?id=80";
            boolean bResponse = exists(serverURLWithEndPoint);
            if (bResponse==true)
            {
//                videoPlayBtn.setEnabled(true);
                videoPlayBtn.setVisibility(View.VISIBLE);
            }
            else
            {
//                videoPlayBtn.setImageAlpha(250);
                videoPlayBtn.setVisibility(View.GONE);
            }

//            MyTask task = new MyTask();
//            task.execute(serverURLWithEndPoint);


//            final ScheduledThreadPoolExecutor myTimer = new ScheduledThreadPoolExecutor(1);
//            myTimer.scheduleAtFixedRate(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    try {
//                        HttpURLConnection.setFollowRedirects(false);
//                        HttpURLConnection con =  (HttpURLConnection) new URL(serverURLWithEndPoint).openConnection();
//                        con.setRequestMethod("HEAD");
//                        System.out.println(con.getResponseCode());
//
//                        if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
//                            videoPlayBtn.setVisibility(View.VISIBLE);
//                            System.out.println("Having Video URL.....................................");
//                        }else{
//                            System.out.println("Noooooooooooooooo URL.....................................");
//                            videoPlayBtn.setVisibility(View.GONE);
//                        }
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                        return;
//                    }
//
//                }
//            }, 0,10000, TimeUnit.MILLISECONDS);



            videoPlayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    View video_screen = LayoutInflater.from(context).inflate(R.layout.video_popup_screen, null);

                    dialog.setContentView(video_screen);
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    dialog.getWindow().setAttributes(lp);


                    final WebView webView = (WebView) dialog.findViewById(R.id.video_view);

                    webView.setWebViewClient(new WebViewClient());

                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    webView.getSettings().setPluginState(WebSettings.PluginState.ON);
                    webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                    webView.setWebChromeClient(new WebChromeClient());

                    String serverURLWithEndPoint = ResourceConstant.SERVER_URL + "api/getVideo?id=80";
                    webView.loadUrl(serverURLWithEndPoint);

                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            webView.destroy();
                        }
                    });
                }
            });

            incrementBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quantity++;
                    itemQuantityEditText.setText(quantity+"");
                    String itemName = (String) itemNameTextView.getText();
                    if(quantityList.containsKey(itemName)){
                        quantityList.put(itemName, quantity);
                    }
                    listener.onUpdate(itemName, quantity);
                }
            });

            decrementBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity > 0){
                        quantity--;
                        itemQuantityEditText.setText(quantity + "");
                    }
                    String itemName = (String) itemNameTextView.getText();
                    if(quantityList.containsKey(itemName)){
                        quantityList.put(itemName, quantity);
                    }
                    listener.onUpdate(itemName, quantity);
                }
            });

        }


//        private class MyTask extends AsyncTask<String, Void, Boolean> {
//
//            @Override
//            protected void onPreExecute() {
//
//            }
//
//            @Override
//            protected Boolean doInBackground(String... params) {
//
//                try {
//                    HttpURLConnection.setFollowRedirects(false);
//                    HttpURLConnection con =  (HttpURLConnection) new URL(params[0]).openConnection();
//                    con.setRequestMethod("HEAD");
//                    System.out.println(con.getResponseCode());
//                    return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(Boolean result) {
//                boolean bResponse = result;
//                if (bResponse==true)
//                {
//                    videoPlayBtn.setVisibility(View.VISIBLE);
//                }
//                else
//                {
//                    videoPlayBtn.setVisibility(View.GONE);
//                }
//            }
//        }
    }
}
