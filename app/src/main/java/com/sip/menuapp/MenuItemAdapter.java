package com.sip.menuapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private List<Item> itemList = new ArrayList<>();
    private final FragmentActivity fragmentActivity;
    Context context;
    public static String serverURL;

    public MenuItemAdapter(FragmentActivity parent,
                           List<Item> items) {
        fragmentActivity = parent;
        itemList = items;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Item item = itemList.get(i);
        holder.itemNameTextView.setText(item.getName());
        holder.itemDescriptionTextView.setText(item.getDescription());
        holder.itemPriceTextView.setText(item.getPrice());

        holder.videoPath = item.getVideoPath(); // set path of videoURL

        holder.itemView.setTag(item.getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView itemNameTextView;
        final TextView itemDescriptionTextView;
        final TextView itemPriceTextView;
        final Button videoPlayBtn;
        String videoPath;

        ViewHolder(View view) {
            super(view);
            itemNameTextView = (TextView) view.findViewById(R.id.item_name_text);
            itemDescriptionTextView = (TextView) view.findViewById(R.id.item_description_text);
            itemPriceTextView = (TextView) view.findViewById(R.id.item_price_text);
            videoPlayBtn = view.findViewById(R.id.btn_play);

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
//                    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    dialog.getWindow().setAttributes(lp);


                    final WebView webView = (WebView) dialog.findViewById(R.id.video_view);

                    webView.setWebViewClient(new WebViewClient());
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    webView.getSettings().setPluginState(WebSettings.PluginState.ON);
                    webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                    webView.setWebChromeClient(new WebChromeClient());

//                    MenuItemListActivity activity = (MenuItemListActivity) context;
//                    String serverURL = activity.getServerURL();
                    String serverURLWithEndPoint = serverURL + "api/getVideo?id=80";
                    webView.loadUrl(serverURLWithEndPoint);

                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            System.out.print("######################Dialog is dismissed.................");
                            webView.destroy();
                        }
                    });
                }
            });
        }
    }
}
