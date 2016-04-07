package com.example.android.shoppingapp2.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.android.shoppingapp2.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by hernandez on 3/3/2016.
 */
public class ShoppingItemAdapter extends ArrayAdapter<ShoppingItem> {

    private final List<ShoppingItem> list;

    private Context mContext;

    public ShoppingItemAdapter(Context context, List<ShoppingItem> list) {
        super(context, R.layout.shopping_list_item, list);
        this.mContext = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ShoppingItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        ShoppingItem shoppingItem = list.get(position);

        if(convertView==null){
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.shopping_list_item, null);

            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            holder.decrementButton = (Button) convertView.findViewById(R.id.decrementButton);
            holder.incrementButton = (Button) convertView.findViewById(R.id.incrementButton);
            holder.quantityEditText = (EditText) convertView.findViewById(R.id.quantityEditText);
            holder.productNameEditText = (EditText) convertView.findViewById(R.id.productNameEditText);
            holder.unitPriceEditText = (EditText) convertView.findViewById(R.id.unitPriceEditText);
            holder.categoryEditText = (EditText) convertView.findViewById(R.id.categoryEditText);
            holder.subtotalEditText = (EditText) convertView.findViewById(R.id.subtotalEditText);

            convertView.setTag(holder);

        }

        else{
            // We have these views set up.
            holder = (ViewHolder) convertView.getTag();
        }

        // Now, set the data:

        DecimalFormat df = new DecimalFormat("$0.00");

        holder.quantity = this.getItem(position).getQuantity();
        holder.productName = this.getItem(position).getProductName();
        holder.itemPrice = this.getItem(position).getItemPrice();
        holder.category = this.getItem(position).getCategory();
        holder.subtotal = this.getItem(position).getSubtotal();

        holder.quantityEditText.setText(holder.quantity + "");
        holder.productNameEditText.setText(holder.productName);
        holder.unitPriceEditText.setText(df.format(holder.itemPrice));
        holder.categoryEditText.setText(holder.category);
        holder.subtotalEditText.setText(df.format(holder.subtotal));

        holder.decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.quantity--;

                // Only accept positive values for the quantity
                if(holder.quantity > 0)

                {
                    holder.quantityEditText.setText(holder.quantity + "");

                    // Some more logic here to refresh subtotal

                    DecimalFormat df = new DecimalFormat("$0.00");

                    holder.subtotal = holder.quantity * holder.itemPrice;

                    holder.subtotalEditText.setText(df.format(holder.subtotal));

                }

                else{

                    // Put the quantity back to where it was.
                    holder.quantity++;
                }

            }
        });

        holder.incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.quantity++;

                // Only accept positive values for the quantity
                if(holder.quantity > 0) {

                    holder.quantityEditText.setText(holder.quantity + "");

                    // Some more logic here to refresh subtotal

                    DecimalFormat df = new DecimalFormat("$0.00");

                    holder.subtotal = holder.quantity * holder.itemPrice;

                    holder.subtotalEditText.setText(df.format(holder.subtotal));

                }

                else{

                    // Put the quantity back to where it was.
                    holder.quantity++;
                }

            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ShoppingItem element = (ShoppingItem) holder.checkBox.getTag();

                element.setSelected(buttonView.isChecked());

            }
        });

        holder.checkBox.setTag(list.get(position));

        return convertView;
    }

    private static class ViewHolder{

        int quantity;
        String productName;
        double itemPrice;
        String category;
        double subtotal;

        CheckBox checkBox;
        Button decrementButton;
        Button incrementButton;
        EditText quantityEditText;
        EditText productNameEditText;
        EditText unitPriceEditText;
        EditText categoryEditText;
        EditText subtotalEditText;

    }

}
