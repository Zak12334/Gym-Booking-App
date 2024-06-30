package com.example.gymbooking;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.SlotViewHolder> {

    private final List<String> slotList;
    private final LayoutInflater inflater;
    private final OnSlotClickListener slotClickListener;
    private List<String> bookedSlots;
    private String selectedDay;
    private Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;


    public interface OnSlotClickListener {
        void onSlotClicked(String timeSlot);
    }

    public AppointmentAdapter(Context context, List<String> slotList, List<String> bookedSlots, String selectedDay, OnSlotClickListener slotClickListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.slotList = slotList;
        this.bookedSlots = bookedSlots;
        this.selectedDay = selectedDay;
        this.slotClickListener = slotClickListener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.appointment_slots, parent, false);
        return new SlotViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        String slot = slotList.get(position);
        holder.slotTextView.setText(slot);

        String fullSlotIdentifier = selectedDay + " " + slot;

        // Check if this slot on the selected day is booked
        if (bookedSlots.contains(fullSlotIdentifier)) {
            holder.slotTextView.setBackgroundColor(Color.RED); // Red for booked slots
        } else {
            holder.slotTextView.setBackgroundColor(Color.GREEN); // Green for available slots
        }

        holder.itemView.setOnClickListener(v -> {
            if (!bookedSlots.contains(fullSlotIdentifier)) {
                int previousItem = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousItem);
                notifyItemChanged(selectedPosition);
                slotClickListener.onSlotClicked(slot);
            } else {
                Toast.makeText(context, "This slot is already booked.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public void setBookedSlots(List<String> bookedSlots) {
        this.bookedSlots = bookedSlots;
        notifyDataSetChanged();
    }

    public static class SlotViewHolder extends RecyclerView.ViewHolder {
        final TextView slotTextView;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            slotTextView = itemView.findViewById(R.id.slotTextView);
        }
    }
}
