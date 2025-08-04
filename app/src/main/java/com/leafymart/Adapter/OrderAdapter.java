package com.leafymart.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leafymart.Activities.TrackProductActivity;
import com.leafymart.Manager.SessionManager;
import com.leafymart.Model.OrderModel;
import com.leafymart.R;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderModel> orderList;
    private Context context;

    public OrderAdapter(List<OrderModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.orderId.setText("Order ID: #" + order.getOrderId());
        holder.orderDate.setText("Date: " + order.getDate());
        holder.orderStatus.setText("Status: " + order.getStatus());

        // In OrderAdapter.java, ensure correct IDs are passed
        holder.btnTrackOrder.setOnClickListener(v -> {
            int userId = SessionManager.getInstance(context).getUserId();
            int orderId = order.getOrderId(); // Use the actual order object

            if (userId == -1) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, TrackProductActivity.class);
            intent.putExtra("order_id", orderId);
            intent.putExtra("user_id", userId); // Also pass user_id
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderDate, orderStatus;
        Button btnTrackOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            orderDate = itemView.findViewById(R.id.order_date);
            orderStatus = itemView.findViewById(R.id.order_status);
            btnTrackOrder = itemView.findViewById(R.id.btn_track_order);
        }
    }
}
