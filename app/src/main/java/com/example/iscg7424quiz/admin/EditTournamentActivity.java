package com.example.iscg7424quiz.admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iscg7424quiz.R;

import android.app.DatePickerDialog;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;


import com.example.iscg7424quiz.model.Tournament;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class EditTournamentActivity extends AppCompatActivity {

    private DatabaseReference tournamentRef;
    EditText txtTournamentName, txtStartDate, txtEndDate;
    Button btnSaveTournament, btnDeleteTournament;
    TextView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_tournament);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Views
        txtTournamentName = findViewById(R.id.txt_tournamentName);
        txtStartDate = findViewById(R.id.txt_startDate);
        txtEndDate = findViewById(R.id.txt_endDate);
        btnSaveTournament = findViewById(R.id.btnSaveTournament);
        btnDeleteTournament = findViewById(R.id.btnDeleteTournament);
        btnBack = findViewById(R.id.btn_back);
        btnSaveTournament.setEnabled(false);
        btnDeleteTournament.setEnabled(false);
        // Get Tournament ID
        String tournamentId = getIntent().getStringExtra("tournamentId");
        if (tournamentId == null || tournamentId.isEmpty()) {
            Toast.makeText(
                    this,
                    "Tournament ID not found",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }
        //firebase
        tournamentRef = FirebaseDatabase.getInstance()
                .getReference("tournaments")
                .child(tournamentId);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(txtStartDate);
            }
        });
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(txtEndDate);
            }
        });
        loadTournament();
        btnSaveTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTournament();
            }
        });
        btnDeleteTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(EditTournamentActivity.this)
                        .setTitle("Delete Tournament")
                        .setMessage("Are you sure you want to delete this tournament?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteTournament();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }
    //load tournament from firebase
    private void loadTournament() {
        tournamentRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {
                        Tournament tournament =    snapshot.getValue(Tournament.class);
                        if (tournament != null) {
                            txtTournamentName.setText(  tournament.getName()
                            );
                            txtStartDate.setText(formatOldDate(tournament.getStartDate())
                            );
                            txtEndDate.setText(     formatOldDate(tournament.getEndDate())
                            );
                            btnSaveTournament.setEnabled(true);
                            btnDeleteTournament.setEnabled(true);
                        } else {
                            Toast.makeText(
                                    EditTournamentActivity.this,
                                    "Tournament not found",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {
                        Toast.makeText(
                                EditTournamentActivity.this,
                                "Failed to load tournament: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    //save Tournament
    private void saveTournament() {
        String name = txtTournamentName.getText().toString().trim();
        String startDate = txtStartDate.getText().toString().trim();
        String endDate = txtEndDate.getText().toString().trim();
        if (name.isEmpty()
                || startDate.isEmpty()
                || endDate.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please fill in all required fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateFormat.setLenient(false);
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            if (start == null || end == null || start.after(end)) {
                Toast.makeText(
                        this,
                        "End date cannot be before start date",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(
                    this,
                    "Invalid date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        //update only three fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("startDate", startDate);
        updates.put("endDate", endDate);
        btnSaveTournament.setEnabled(false);
        tournamentRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            EditTournamentActivity.this,
                            "Tournament updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveTournament.setEnabled(true);
                    Toast.makeText(
                            EditTournamentActivity.this,
                            "Failed to update tournament",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
    //delete Tournament
    private void deleteTournament() {
        btnDeleteTournament.setEnabled(false);
        tournamentRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            EditTournamentActivity.this,
                            "Tournament deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnDeleteTournament.setEnabled(true);
                    Toast.makeText(
                            EditTournamentActivity.this,
                            "Failed to delete tournament",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
    private String formatOldDate(String date) {
        if (date != null && date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date.substring(8, 10) + "/"
                    + date.substring(5, 7) + "/"
                    + date.substring(0, 4);
        }
        return date;
    }
    //date Picker
    private void showDatePicker(EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditTournamentActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(
                            DatePicker view,
                            int year,
                            int month,
                            int dayOfMonth) {
                        String date = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );
                        dateInput.setText(date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}