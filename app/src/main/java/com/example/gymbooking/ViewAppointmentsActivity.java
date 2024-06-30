package com.example.gymbooking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ViewAppointmentsActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextPhone;
    private ListView listView;
    private String selectedAppointmentDetails;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        listView = findViewById(R.id.listView);

        Button buttonViewAppointments = findViewById(R.id.buttonViewAppointments);
        buttonViewAppointments.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            // Check if both name and phone fields are filled
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(ViewAppointmentsActivity.this, "Please enter both name and phone number", Toast.LENGTH_SHORT).show();
            } else {
                displayBookedAppointments(name, phone);
            }
        });

        setupListViewListener();

        // "Go Home" button setup
        Button buttonGoHome = findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(ViewAppointmentsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        Button buttonCancelAppointment = findViewById(R.id.buttonCancelAppointment);
        buttonCancelAppointment.setOnClickListener(v -> {
            if (selectedAppointmentDetails == null || selectedAppointmentDetails.isEmpty()) {
                Toast.makeText(ViewAppointmentsActivity.this, "Please select an appointment to cancel", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(ViewAppointmentsActivity.this)
                        .setTitle("Confirm Cancellation")
                        .setMessage("Are you sure you want to cancel this appointment?")
                        .setPositiveButton("Yes", (dialog, which) -> cancelAppointment())
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // Reschedule button setup
        Button buttonReschedule = findViewById(R.id.buttonReschedule);
        buttonReschedule.setOnClickListener(v -> {
            Intent intent = new Intent(ViewAppointmentsActivity.this, BookingActivity.class);
            startActivity(intent);
        });
    }


    private void displayBookedAppointments(String inputName, String inputPhone) {
        JSONArray bookings = loadBookings();
        ArrayList<String> userAppointments = new ArrayList<>();

        for (int i = 0; i < bookings.length(); i++) {
            try {
                JSONObject booking = bookings.getJSONObject(i);
                String name = booking.getString("name").toLowerCase(); // Convert to lowercase
                String phone = booking.getString("phone");
                String date = booking.optString("date", "N/A");
                String time = booking.getString("time");
                // Compare with input name also in lowercase
                if (name.equals(inputName.toLowerCase()) && phone.equals(inputPhone)) {
                    String appointmentDetails = booking.getString("name") + " - You have a booking on " + date + " at " + time;
                    userAppointments.add(appointmentDetails);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (userAppointments.isEmpty()) {
            userAppointments.add("No appointments found for " + inputName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userAppointments) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);

                if (position == selectedPosition) {
                    textView.setBackgroundColor(Color.RED);
                } else {
                    textView.setBackgroundColor(Color.TRANSPARENT);
                }

                return view;
            }
        };

        listView.setAdapter(adapter);
    }

    private JSONArray loadBookings() {
        JSONArray bookings = new JSONArray();
        try {
            FileInputStream fis = openFileInput("bookings.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
            isr.close();
            fis.close();
            String jsonString = sb.toString();
            if (!jsonString.isEmpty()) {
                bookings = new JSONArray(jsonString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    private void setupListViewListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedAppointmentDetails = (String) parent.getItemAtPosition(position);
            selectedPosition = position;
            updateListView();
        });
    }

    private void updateListView() {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    private void cancelAppointment() {
        if (selectedAppointmentDetails == null || selectedAppointmentDetails.trim().isEmpty()) {
            Toast.makeText(this, "No appointment selected", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONArray bookings = loadBookings();
        JSONArray updatedBookings = new JSONArray();

        try {
            for (int i = 0; i < bookings.length(); i++) {
                JSONObject booking = bookings.getJSONObject(i);
                String name = booking.getString("name");
                String phone = booking.getString("phone");
                String date = booking.optString("date", "N/A");
                String time = booking.getString("time");

                String bookingDetails = name + " - You have a booking on " + date + " at " + time;

                if (!bookingDetails.equals(selectedAppointmentDetails.trim())) {
                    updatedBookings.put(booking);
                }
            }

            FileOutputStream fos = openFileOutput("bookings.json", MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(updatedBookings.toString());
            writer.close();

            selectedPosition = -1;
            updateListView();
            displayBookedAppointments(editTextName.getText().toString(), editTextPhone.getText().toString());

            Toast.makeText(this, "Appointment Cancelled", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BookingActivity", "Error cancelling appointment: " + e.getMessage());
            Toast.makeText(this, "Error in cancelling appointment", Toast.LENGTH_SHORT).show();
        }
    }
}
