package com.example.gymbooking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private RecyclerView slotsRecyclerView;
    private AppointmentAdapter adapter;
    private String selectedDay;
    private String selectedTime;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;

    private Button buttonSelectDate;
    private String selectedDate;
    private final List<String> bookedSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);

        loadBookedSlots();
        setupSlotsRecyclerView();
        setupConfirmButton();
        setupGoHomeButton();

        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());
    }
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    Button buttonSelectDate = findViewById(R.id.buttonSelectDate);
                    buttonSelectDate.setText(selectedDate);
                    updateSlotsForDay(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveBooking(String bookingIdentifier) {
        try {
            JSONObject booking = new JSONObject();
            booking.put("name", editTextName.getText().toString());
            booking.put("email", editTextEmail.getText().toString());
            booking.put("phone", editTextPhone.getText().toString());
            booking.put("time", selectedTime);
            booking.put("bookingIdentifier", bookingIdentifier);
            booking.put("date", selectedDate);
            updateSlotsForDay(selectedDate);

            JSONArray bookings = loadBookings();
            bookings.put(booking);

            FileOutputStream fos = openFileOutput("bookings.json", MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(bookings.toString());
            writer.close();

            bookedSlots.add(bookingIdentifier);
            adapter.setBookedSlots(bookedSlots);

            Toast.makeText(this, "Booking confirmed for " + bookingIdentifier, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save booking.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray loadBookings() {
        JSONArray bookings = new JSONArray();
        try {
            FileInputStream fis = openFileInput("bookings.json");
            if (fis.available() != 0) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    private void loadBookedSlots() {
        JSONArray bookings = loadBookings();
        bookedSlots.clear();
        for (int i = 0; i < bookings.length(); i++) {
            try {
                JSONObject booking = bookings.getJSONObject(i);
                if(booking.has("bookingIdentifier")) {
                    String bookingIdentifier = booking.getString("bookingIdentifier");
                    bookedSlots.add(bookingIdentifier);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupSlotsRecyclerView() {
        slotsRecyclerView = findViewById(R.id.recyclerView);
        slotsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        updateSlotsForDay(null);
    }

    private void updateSlotsForDay(String day) {
        List<String> timeSlots = Arrays.asList("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM");
        List<String> bookedSlotsForDay = new ArrayList<>();

        for (String bookedSlot : bookedSlots) {
            if (bookedSlot.startsWith(day + " ")) {
                String timePart = bookedSlot.substring(day.length() + 1);
                bookedSlotsForDay.add(timePart);
            }
        }

        adapter = new AppointmentAdapter(this, timeSlots, bookedSlots, day, timeSlot -> {
            if (bookedSlotsForDay.contains(timeSlot)) {
                Toast.makeText(BookingActivity.this, "This slot is already booked.", Toast.LENGTH_SHORT).show();
            } else {
                selectedTime = timeSlot;

            }
        });
        slotsRecyclerView.setAdapter(adapter);
    }



    private List<String> getBookedSlotsForDay(String day) {
        List<String> slotsForDay = new ArrayList<>();
        if (day != null) {
            for (String bookedSlot : bookedSlots) {
                if (bookedSlot != null && bookedSlot.startsWith(day)) {

                    slotsForDay.add(bookedSlot.substring(day.length() + 1));
                }
            }
        }
        return slotsForDay;
    }

    private void setupConfirmButton() {
        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            if (validateInputs() && selectedTime != null && selectedDate != null) {

                String bookingIdentifier = selectedDate + " " + selectedTime;

                // Check if this slot is already booked
                if (!isSlotBooked(bookingIdentifier)) {
                    saveBooking(bookingIdentifier);
                    Toast.makeText(BookingActivity.this, "Booking confirmed for " + bookingIdentifier, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookingActivity.this, "This slot is already booked.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BookingActivity.this, "Please fill in all fields, select a date, and time slot.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isSlotBooked(String bookingIdentifier) {
        return bookedSlots.contains(bookingIdentifier);
    }


    private boolean validateInputs() {
        return !(editTextName.getText().toString().isEmpty() ||
                editTextEmail.getText().toString().isEmpty() ||
                editTextPhone.getText().toString().isEmpty());
    }

    private void setupGoHomeButton() {
        Button buttonGoHome = findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
