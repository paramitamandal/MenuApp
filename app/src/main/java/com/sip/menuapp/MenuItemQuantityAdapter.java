package com.sip.menuapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

public class MenuItemQuantityAdapter extends RecyclerView.Adapter<MenuItemQuantityAdapter.ViewHolder> {

    private Map<String, Integer> orderList = new HashMap<String, Integer>();
    private String[] orderItemNames;
//    private final Activity parentActivity;
//    private Context context;
    private MenuItemQuantityAdapter.Listener listener;
    public interface Listener {
        void onUpdateOrder();
    }

    public MenuItemQuantityAdapter(Listener parent,
                           Map<String, Integer> orderList) {
//        parentActivity = parent;
        listener = parent;
        this.orderList = orderList;
        orderItemNames = orderList.keySet().toArray(new String[orderList.size()]);
    }

    @NonNull
    @Override
    public MenuItemQuantityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
//        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_item_in_order_summary, parent, false);
        return new MenuItemQuantityAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemQuantityAdapter.ViewHolder holder, final int i) {
        String itemName = this.orderItemNames[i];
        holder.quantity = this.orderList.get(itemName);
        holder.itemNameTextView.setText(itemName);
        holder.itemQuantityEditText.setText(holder.quantity+"");
        holder.itemView.setTag(itemName);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView itemNameTextView;
        final EditText itemQuantityEditText;
        final Button incrementBtn;
        final Button decrementBtn;
        int quantity = 0;

        ViewHolder(View view) {
            super(view);
            itemNameTextView = (TextView) view.findViewById(R.id.item_name_text);
            itemQuantityEditText = (EditText) view.findViewById(R.id.item_quantity_text);
            incrementBtn = view.findViewById(R.id.btn_increment);
            decrementBtn = view.findViewById(R.id.btn_decrement);

            incrementBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quantity++;
                    itemQuantityEditText.setText(quantity+"");
                    String itemName = (String) itemNameTextView.getText();
                    if(MenuItemAdapter.quantityList.containsKey(itemName)){
                        MenuItemAdapter.quantityList.put(itemName, quantity);
                        orderList.put(itemName,quantity);
                    }
                    listener.onUpdateOrder();
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
                    if(MenuItemAdapter.quantityList.containsKey(itemName)){
                        MenuItemAdapter.quantityList.put(itemName, quantity);
                        orderList.put(itemName,quantity);
                    }
                    listener.onUpdateOrder();
                }
            });

        }
    }
}

