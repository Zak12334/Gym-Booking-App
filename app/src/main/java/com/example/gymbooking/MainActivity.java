package com.example.gymbooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bookAppointmentButton = findViewById(R.id.bookAppointmentButton);
        bookAppointmentButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, BookingActivity.class))
        );

        Button viewAppointmentsButton = findViewById(R.id.viewAppointmentsButton);
        viewAppointmentsButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ViewAppointmentsActivity.class))
        );

    }
}
